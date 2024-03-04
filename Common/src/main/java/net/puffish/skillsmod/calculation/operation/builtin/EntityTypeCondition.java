package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class EntityTypeCondition implements Operation<EntityType<?>, Boolean> {
	private final RegistryEntryList<EntityType<?>> entityTypeEntries;

	private EntityTypeCondition(RegistryEntryList<EntityType<?>> entityTypeEntries) {
		this.entityTypeEntries = entityTypeEntries;
	}

	public static void register() {
		BuiltinPrototypes.ENTITY_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				EntityTypeCondition::parse
		);
	}

	public static Result<EntityTypeCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(EntityTypeCondition::parse);
	}

	public static Result<EntityTypeCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optEntityType = rootObject.get("entity") // Backwards compatibility.
				.orElse(failure -> rootObject.get("entity_type"))
				.andThen(JsonParseUtils::parseEntityTypeOrEntityTypeTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new EntityTypeCondition(
					optEntityType.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(EntityType<?> entityType) {
		return Optional.of(entityType.isIn(entityTypeEntries));
	}
}
