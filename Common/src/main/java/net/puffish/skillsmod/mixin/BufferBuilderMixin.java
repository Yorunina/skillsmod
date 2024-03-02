package net.puffish.skillsmod.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.puffish.skillsmod.access.BufferBuilderAccess;
import net.puffish.skillsmod.access.BuiltBufferAccess;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin implements BufferBuilderAccess {
	@Unique
	private List<Matrix4f> emits;

	@Override
	@Unique
	public void setEmits(List<Matrix4f> emits) {
		this.emits = emits;
	}

	@Inject(method = "build", at = @At("RETURN"))
	private void injectAtBuild(CallbackInfoReturnable<BufferBuilder.BuiltBuffer> cir) {
		((BuiltBufferAccess) cir.getReturnValue()).setEmits(emits);
		this.emits = null;
	}
}
