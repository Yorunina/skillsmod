package net.puffish.skillsmod.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.puffish.skillsmod.access.EntityAttributeInstanceAccess;
import net.puffish.skillsmod.attributes.PlayerAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

	@Inject(method = "getMaxAir", at = @At("RETURN"), cancellable = true)
	private void injectAtGetMaxAir(CallbackInfoReturnable<Integer> cir) {
		if (((Entity) (Object) this) instanceof PlayerEntity player) {
			var attributes = player.getAttributes();
			if (attributes != null) {
				var attribute = ((EntityAttributeInstanceAccess) attributes.getCustomInstance(PlayerAttributes.MAX_AIR));
				cir.setReturnValue((int) Math.round(attribute.computeValueForInitial(cir.getReturnValueI())));
			}
		}
	}
}
