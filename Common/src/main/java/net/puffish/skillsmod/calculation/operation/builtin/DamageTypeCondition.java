package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class DamageTypeCondition implements Operation<DamageType, Boolean> {
	private final RegistryEntryList<DamageType> damageTypeEntries;

	private DamageTypeCondition(RegistryEntryList<DamageType> damageTypeEntries) {
		this.damageTypeEntries = damageTypeEntries;
	}

	public static void register() {
		BuiltinPrototypes.DAMAGE_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				DamageTypeCondition::parse
		);
	}

	public static Result<DamageTypeCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<DamageTypeCondition, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optDamageType = rootObject.get("damage") // Backwards compatibility.
				.orElse(failure -> rootObject.get("damage_type"))
				.andThen(damageElement -> JsonParseUtils.parseDamageTypeOrDamageTypeTag(damageElement, context.getDynamicRegistryManager()))
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new DamageTypeCondition(
					optDamageType.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(DamageType damageType) {
		return Optional.of(
				damageTypeEntries.stream().anyMatch(entry -> entry.value() == damageType)
		);
	}
}
