package net.puffish.skillsmod.client.rendering;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec2f;

public class ConnectionBatchedRenderer {
	private final BufferBuilder bufferBuilderNormal = new BufferBuilder(256);
	private final BufferBuilder bufferBuilderExclusive = new BufferBuilder(256);
	private final BufferBuilder bufferBuilderOutline = new BufferBuilder(256);

	public ConnectionBatchedRenderer() {
		bufferBuilderNormal.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
		bufferBuilderExclusive.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
		bufferBuilderOutline.begin(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
	}

	public void emitNormalConnection(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional
	) {
		emitConnection(
				matrices,
				startX,
				startY,
				endX,
				endY,
				bidirectional,
				bufferBuilderNormal
		);
	}

	public void emitExclusiveConnection(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional
	) {
		emitConnection(
				matrices,
				startX,
				startY,
				endX,
				endY,
				bidirectional,
				bufferBuilderExclusive
		);
	}

	public void emitConnection(
			MatrixStack matrices,
			float startX,
			float startY,
			float endX,
			float endY,
			boolean bidirectional,
			BufferBuilder bufferBuilder
	) {
		var matrix = matrices.peek().getPositionMatrix();

		emitLine(matrix, bufferBuilderOutline, startX, startY, endX, endY, 3);
		if (!bidirectional) {
			emitArrow(matrix, bufferBuilderOutline, startX, startY, endX, endY, 8);
		}
		emitLine(matrix, bufferBuilder, startX, startY, endX, endY, 1);
		if (!bidirectional) {
			emitArrow(matrix, bufferBuilder, startX, startY, endX, endY, 6);
		}
	}

	private void emitLine(
			Matrix4f matrix,
			BufferBuilder bufferBuilder,
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

		bufferBuilder.vertex(matrix, startX + side.x, startY + side.y, 0).next();
		bufferBuilder.vertex(matrix, startX - side.x, startY - side.y, 0).next();
		bufferBuilder.vertex(matrix, endX + side.x, endY + side.y, 0).next();

		bufferBuilder.vertex(matrix, endX - side.x, endY - side.y, 0).next();
		bufferBuilder.vertex(matrix, endX + side.x, endY + side.y, 0).next();
		bufferBuilder.vertex(matrix, startX - side.x, startY - side.y, 0).next();
	}

	private void emitArrow(
			Matrix4f matrix,
			BufferBuilder bufferBuilder,
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

		bufferBuilder.vertex(matrix, center.x + forward.x, center.y + forward.y, 0).next();
		bufferBuilder.vertex(matrix, back.x - side.x, back.y - side.y, 0).next();
		bufferBuilder.vertex(matrix, back.x + side.x, back.y + side.y, 0).next();
	}

	public void draw() {
		RenderSystem.setShader(GameRenderer::getPositionShader);
		RenderSystem.setShaderColor(0f, 0f, 0f, 1f);
		bufferBuilderOutline.end();
		BufferRenderer.draw(bufferBuilderOutline);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		bufferBuilderNormal.end();
		BufferRenderer.draw(bufferBuilderNormal);
		RenderSystem.setShaderColor(1f, 0f, 0f, 1f);
		bufferBuilderExclusive.end();
		BufferRenderer.draw(bufferBuilderExclusive);
		RenderSystem.applyModelViewMatrix();
	}
}
