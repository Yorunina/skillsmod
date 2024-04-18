package net.puffish.skillsmod.calculation.operation.builtin;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class DamageTypeCondition implements Operation<String, Boolean> {
	private final String damageType;

	private DamageTypeCondition(String damageType) {
		this.damageType = damageType;
	}

	public static void register() {
		BuiltinPrototypes.DAMAGE_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				DamageTypeCondition::parse
		);
	}

	public static Result<DamageTypeCondition, Problem> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<DamageTypeCondition, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		var optDamageType = rootObject.get("damage") // Backwards compatibility.
				.orElse(problem -> rootObject.get("damage_type"))
				.andThen(JsonElement::getAsString)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new DamageTypeCondition(
					optDamageType.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public Optional<Boolean> apply(String damageType) {
		return Optional.of(this.damageType.equals(damageType));
	}
}
