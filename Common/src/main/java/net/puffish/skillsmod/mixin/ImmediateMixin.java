package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.puffish.skillsmod.access.BufferBuilderAccess;
import net.puffish.skillsmod.access.ImmediateAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(VertexConsumerProvider.Immediate.class)
public class ImmediateMixin implements ImmediateAccess {

	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@Inject(
			method = "getBufferInternal",
			at = @At("RETURN")
	)
	private void injectAtGetBufferInternal(RenderLayer renderLayer, CallbackInfoReturnable<BufferBuilder> cir) {
		((BufferBuilderAccess) cir.getReturnValue()).setEmits(emits);
	}
}
