package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyDamageTypeTagCondition implements Operation<DamageType, Boolean> {
	private final RegistryEntryList<DamageType> entries;

	private LegacyDamageTypeTagCondition(RegistryEntryList<DamageType> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.DAMAGE_TYPE.registerOperation(
				SkillsMod.createIdentifier("legacy_damage_type_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyDamageTypeTagCondition::parse
		);
	}

	public static Result<LegacyDamageTypeTagCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<LegacyDamageTypeTagCondition, Failure> parse(JsonObjectWrapper rootObject, ConfigContext context) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(element -> JsonParseUtils.parseDamageTypeTag(element, context.getDynamicRegistryManager()))
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new LegacyDamageTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(DamageType damageType) {
		return Optional.of(entries.stream().anyMatch(entry -> entry.value() == damageType));
	}
}
