package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public class AttributeOperation implements Operation<LivingEntity, EntityAttributeInstance> {
	private final EntityAttribute attribute;

	private AttributeOperation(EntityAttribute attribute) {
		this.attribute = attribute;
	}

	public static void register() {
		BuiltinPrototypes.LIVING_ENTITY.registerOperation(
				SkillsMod.createIdentifier("attribute"),
				BuiltinPrototypes.ENTITY_ATTRIBUTE_INSTANCE,
				AttributeOperation::parse
		);
	}

	public static Result<AttributeOperation, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(AttributeOperation::parse);
	}

	public static Result<AttributeOperation, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optAttribute = rootObject.get("attribute")
				.andThen(JsonParseUtils::parseAttribute)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new AttributeOperation(
					optAttribute.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<EntityAttributeInstance> apply(LivingEntity entity) {
		return Optional.ofNullable(entity.getAttributeInstance(attribute));
	}
}
