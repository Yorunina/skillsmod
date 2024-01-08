package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureBatchedRenderer {
	private final Map<Identifier, List<TextureEmit>> batch = new HashMap<>();

	private record TextureEmit(
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3,
			float x4, float y4, float z4,

			float minU, float minV, float maxU, float maxV,
			Vector4f color
	) { }

	public void emitTexture(
			MatrixStack matrices, Identifier texture,
			int x, int y, int width, int height,
			Vector4f color
	) {
		emitTextureBatched(
				matrices,
				texture,
				x, y, x + width, y + height,
				0f, 0f, 1f, 1f,
				color
		);
	}

	public void emitTexture(
			MatrixStack matrices, Identifier texture,
			int x, int y, int width, int height,
			float minU, float minV, float maxU, float maxV,
			Vector4f color
	) {
		emitTextureBatched(
				matrices,
				texture,
				x, y, x + width, y + height,
				minU, minV, maxU, maxV,
				color
		);
	}

	public void emitSpriteStretch(
			MatrixStack matrices, Sprite sprite,
			int x, int y, int width, int height,
			Vector4f color
	) {
		emitTextureBatched(
				matrices,
				sprite.getAtlas().getId(),
				x, y, x + width, y + height,
				sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV(),
				color
		);
	}

	private void emitTextureBatched(
			MatrixStack matrices, Identifier texture,
			float minX, float minY, float maxX, float maxY,
			float minU, float minV, float maxU, float maxV,
			Vector4f color
	) {
		var emits = batch.computeIfAbsent(texture, key -> new ArrayList<>());

		var matrix = matrices.peek().getPositionMatrix();

		var v1 = new Vector4f(minX, minY, 0f, 1f);
		var v2 = new Vector4f(minX, maxY, 0f, 1f);
		var v3 = new Vector4f(maxX, maxY, 0f, 1f);
		var v4 = new Vector4f(maxX, minY, 0f, 1f);

		v1.transform(matrix);
		v2.transform(matrix);
		v3.transform(matrix);
		v4.transform(matrix);

		emits.add(new TextureEmit(
				v1.getX(), v1.getY(), v1.getZ(),
				v2.getX(), v2.getY(), v2.getZ(),
				v3.getX(), v3.getY(), v3.getZ(),
				v4.getX(), v4.getY(), v4.getZ(),
				minU, minV, maxU, maxV,
				color
		));
	}

	public void draw() {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
		for (var entry : batch.entrySet()) {
			RenderSystem.setShaderTexture(0, entry.getKey());
			var bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
			for (var emit : entry.getValue()) {
				bufferBuilder.vertex(emit.x1, emit.y1, emit.z1).color(emit.color.getX(), emit.color.getY(), emit.color.getZ(), emit.color.getW()).texture(emit.minU, emit.minV).next();
				bufferBuilder.vertex(emit.x2, emit.y2, emit.z2).color(emit.color.getX(), emit.color.getY(), emit.color.getZ(), emit.color.getW()).texture(emit.minU, emit.maxV).next();
				bufferBuilder.vertex(emit.x3, emit.y3, emit.z3).color(emit.color.getX(), emit.color.getY(), emit.color.getZ(), emit.color.getW()).texture(emit.maxU, emit.maxV).next();
				bufferBuilder.vertex(emit.x4, emit.y4, emit.z4).color(emit.color.getX(), emit.color.getY(), emit.color.getZ(), emit.color.getW()).texture(emit.maxU, emit.minV).next();
			}
			BufferRenderer.drawWithShader(bufferBuilder.end());
		}
		batch.clear();
	}
}
