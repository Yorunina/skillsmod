package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class ConnectionBatchedRenderer {
	private final Int2ObjectMap<List<TriangleEmit>> strokeBatch = new Int2ObjectOpenHashMap<>();
	private final Int2ObjectMap<List<TriangleEmit>> fillBatch = new Int2ObjectOpenHashMap<>();

	private record TriangleEmit(
			float x1, float y1, float z1,
			float x2, float y2, float z2,
			float x3, float y3, float z3
	) { }

	public void emitConnection(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional,
			int fillColor,
			int strokeColor
	) {
		var matrix = matrices.peek().getPositionMatrix();

		emitLine(strokeBatch, matrix, strokeColor, startX, startY, endX, endY, 3);
		if (!bidirectional) {
			emitArrow(strokeBatch, matrix, strokeColor, startX, startY, endX, endY, 8);
		}
		emitLine(fillBatch, matrix, fillColor, startX, startY, endX, endY, 1);
		if (!bidirectional) {
			emitArrow(fillBatch, matrix, fillColor, startX, startY, endX, endY, 6);
		}
	}

	private void emitLine(
			Int2ObjectMap<List<TriangleEmit>> batch,
			Matrix4f matrix,
			int color,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness
	) {
		var side = new Vec2f(endX, endY)
				.add(new Vec2f(-startX, -startY))
				.normalize();

		side = new Vec2f(side.y, -side.x).multiply(thickness / 2f);

		emitTriangle(
				batch, matrix, color,
				startX + side.x, startY + side.y,
				startX - side.x, startY - side.y,
				endX + side.x, endY + side.y
		);
		emitTriangle(
				batch, matrix, color,
				endX - side.x, endY - side.y,
				endX + side.x, endY + side.y,
				startX - side.x, startY - side.y
		);
	}

	private void emitArrow(
			Int2ObjectMap<List<TriangleEmit>> batch,
			Matrix4f matrix,
			int color,
			float startX,
			float startY,
			float endX,
			float endY,
			float thickness
	) {
		var center = new Vec2f(endX, endY)
				.add(new Vec2f(startX, startY))
				.multiply(0.5f);
		var normal = new Vec2f(endX, endY)
				.add(new Vec2f(-startX, -startY))
				.normalize();
		var forward = new Vec2f(normal.x, normal.y)
				.multiply(thickness);
		var backward = new Vec2f(forward.x, forward.y)
				.multiply(-0.5f);
		var back = new Vec2f(center.x, center.y)
				.add(backward);
		var side = new Vec2f(backward.y, -backward.x)
				.multiply(MathHelper.sqrt(3f));

		emitTriangle(
				batch, matrix, color,
				center.x + forward.x, center.y + forward.y,
				back.x - side.x, back.y - side.y,
				back.x + side.x, back.y + side.y
		);
	}

	private void emitTriangle(
			Int2ObjectMap<List<TriangleEmit>> batch,
			Matrix4f matrix,
			int color,
			float x1, float y1,
			float x2, float y2,
			float x3, float y3

	) {
		var v1 = new Vector4f(x1, y1, 0f, 1f);
		var v2 = new Vector4f(x2, y2, 0f, 1f);
		var v3 = new Vector4f(x3, y3, 0f, 1f);

		v1.transform(matrix);
		v2.transform(matrix);
		v3.transform(matrix);

		var emits = batch.computeIfAbsent(color, key -> new ArrayList<>());

		emits.add(new TriangleEmit(
				v1.getX(), v1.getY(), v1.getZ(),
				v2.getX(), v2.getY(), v2.getZ(),
				v3.getX(), v3.getY(), v3.getZ()
		));
	}

	public void draw() {
		RenderSystem.setShader(GameRenderer::getPositionShader);

		drawBatch(strokeBatch);
		drawBatch(fillBatch);
	}

	private void drawBatch(Int2ObjectMap<List<TriangleEmit>> batch) {
		RenderSystem.setShader(GameRenderer::getPositionShader);

		for (var entry : batch.int2ObjectEntrySet()) {
			var color = entry.getIntKey();
			var a = (float) ((color >> 24) & 0xff) / 255f;
			var r = (float) ((color >> 16) & 0xff) / 255f;
			var g = (float) ((color >> 8) & 0xff) / 255f;
			var b = (float) (color & 0xff) / 255f;
			RenderSystem.setShaderColor(r, g, b, a);

			var bufferBuilder = Tessellator.getInstance().getBuffer();
			bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
			for (var emit : entry.getValue()) {
				bufferBuilder.vertex(emit.x1, emit.y1, emit.z1).next();
				bufferBuilder.vertex(emit.x2, emit.y2, emit.z2).next();
				bufferBuilder.vertex(emit.x3, emit.y3, emit.z3).next();
			}
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
		}

		batch.clear();
	}
}
