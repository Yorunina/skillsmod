package net.puffish.skillsmod.mixin;

import net.minecraft.world.PersistentStateManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PersistentStateManager.class)
public abstract class PersistentStateManagerMixin {

/*	@Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtHelper;getDataVersion(Lnet/minecraft/nbt/NbtCompound;I)I"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	private void injectAtReadNbt(
			String id,
			DataFixTypes dataFixTypes,
			int currentSaveVersion,
			CallbackInfoReturnable<NbtCompound> cir,
			File file,
			InputStream fileInputStream,
			NbtCompound nbtCompoundUpdated,
			PushbackInputStream pushbackInputStream,
			NbtCompound nbtCompound,
			DataInputStream datainputstream
	) {
		if (dataFixTypes == null) {
			cir.setReturnValue(nbtCompound);
		}
	}*/

}
