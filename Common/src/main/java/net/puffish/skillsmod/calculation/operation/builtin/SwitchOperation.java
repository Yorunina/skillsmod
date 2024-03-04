package net.puffish.skillsmod.calculation.operation.builtin;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class SwitchOperation implements Operation<Boolean, Double> {
	private final double trueValue;
	private final double falseValue;

	private SwitchOperation(double trueValue, double falseValue) {
		this.trueValue = trueValue;
		this.falseValue = falseValue;
	}

	public static void register() {
		BuiltinPrototypes.BOOLEAN.registerOperation(
				SkillsMod.createIdentifier("switch"),
				BuiltinPrototypes.NUMBER,
				SwitchOperation::parse
		);
	}

	public static Result<SwitchOperation, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(SwitchOperation::parse);
	}

	public static Result<SwitchOperation, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTrue = rootObject.getDouble("true")
				.ifFailure(failures::add)
				.getSuccess();

		var optFalse = rootObject.getDouble("true")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new SwitchOperation(
					optTrue.orElseThrow(),
					optFalse.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Double> apply(Boolean value) {
		return Optional.of(value ? trueValue : falseValue);
	}
}
