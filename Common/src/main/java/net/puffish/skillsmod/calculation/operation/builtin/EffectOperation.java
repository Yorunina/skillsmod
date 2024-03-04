package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public class EffectOperation implements Operation<LivingEntity, StatusEffectInstance> {
	private final StatusEffect effect;

	private EffectOperation(StatusEffect effect) {
		this.effect = effect;
	}

	public static void register() {
		BuiltinPrototypes.LIVING_ENTITY.registerOperation(
				SkillsMod.createIdentifier("effect"),
				BuiltinPrototypes.STATUS_EFFECT_INSTANCE,
				EffectOperation::parse
		);
	}

	public static Result<EffectOperation, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(EffectOperation::parse);
	}

	public static Result<EffectOperation, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optEffect = rootObject.get("effect")
				.andThen(JsonParseUtils::parseEffect)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new EffectOperation(
					optEffect.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<StatusEffectInstance> apply(LivingEntity entity) {
		return Optional.ofNullable(entity.getStatusEffect(effect));
	}
}
