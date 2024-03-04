package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class LegacyEntityTypeTagCondition implements Operation<EntityType<?>, Boolean> {
	private final RegistryEntryList<EntityType<?>> entries;

	private LegacyEntityTypeTagCondition(RegistryEntryList<EntityType<?>> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY_TYPE.registerOperation(
				SkillsMod.createIdentifier("legacy_entity_type_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyEntityTypeTagCondition::parse
		);
	}

	public static Result<LegacyEntityTypeTagCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(LegacyEntityTypeTagCondition::parse);
	}

	public static Result<LegacyEntityTypeTagCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseEntityTypeTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new LegacyEntityTypeTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(EntityType<?> entityType) {
		return Optional.of(entries.contains(Registries.ENTITY_TYPE.getEntry(entityType)));
	}
}
