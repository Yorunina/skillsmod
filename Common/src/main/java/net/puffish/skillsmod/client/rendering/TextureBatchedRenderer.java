package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vector4f;

import java.util.HashMap;
import java.util.Map;

public class TextureBatchedRenderer {
	private final Map<Identifier, BufferBuilder> batch = new HashMap<>();

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
		var vertexConsumer = batch.computeIfAbsent(texture, key -> {
			var bufferBuilder = new BufferBuilder(256);
			bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
			return bufferBuilder;
		});

		var matrix = matrices.peek().getPositionMatrix();
		vertexConsumer.vertex(matrix, minX, minY, 0f).color(color.getX(), color.getY(), color.getZ(), color.getW()).texture(minU, minV).next();
		vertexConsumer.vertex(matrix, minX, maxY, 0f).color(color.getX(), color.getY(), color.getZ(), color.getW()).texture(minU, maxV).next();
		vertexConsumer.vertex(matrix, maxX, maxY, 0f).color(color.getX(), color.getY(), color.getZ(), color.getW()).texture(maxU, maxV).next();
		vertexConsumer.vertex(matrix, maxX, minY, 0f).color(color.getX(), color.getY(), color.getZ(), color.getW()).texture(maxU, minV).next();
	}

	public void draw() {
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		for (var entry : batch.entrySet()) {
			RenderSystem.setShaderTexture(0, entry.getKey());
			RenderSystem.setShader(GameRenderer::getPositionColorTexShader);
			entry.getValue().end();
			BufferRenderer.draw(entry.getValue());
		}
		batch.clear();
	}
}
