package net.puffish.skillsmod.calculation;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.expression.DefaultParser;
import net.puffish.skillsmod.expression.Expression;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CalculationCase {
	private final Expression<Double> condition;
	private final Expression<Double> expression;
	private final JsonPath expressionElementPath;

	private CalculationCase(Expression<Double> condition, Expression<Double> expression, JsonPath expressionElementPath) {
		this.condition = condition;
		this.expression = expression;
		this.expressionElementPath = expressionElementPath;
	}

	public static Result<CalculationCase, Failure> parseSimplified(JsonElementWrapper rootElement, Set<String> expressionVariables) {
		var failures = new ArrayList<Failure>();

		var optExpression = rootElement.getAsString()
				.andThen(string -> DefaultParser.parse(string, expressionVariables)
						.mapFailure(failure -> Failure.fromMany(
								failure.getMessages()
										.stream()
										.map(msg -> rootElement.getPath().createFailure(msg))
										.toList()
						))
				)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new CalculationCase(
					v -> 1.0,
					optExpression.orElseThrow(),
					rootElement.getPath()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public static Result<CalculationCase, Failure> parse(JsonElementWrapper rootElement, Set<String> expressionVariables) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, expressionVariables));
	}

	public static Result<CalculationCase, Failure> parse(JsonObjectWrapper rootObject, Set<String> expressionVariables) {
		var failures = new ArrayList<Failure>();

		var condition = rootObject.get("condition")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsString()
						.andThen(string -> DefaultParser.parse(string, expressionVariables)
								.mapFailure(failure -> Failure.fromMany(
										failure.getMessages()
												.stream()
												.map(msg -> element.getPath().createFailure(msg))
												.toList()
								))
						)
						.ifFailure(failures::add)
						.getSuccess()
				)
				.orElse(p -> 1.0); // no condition, so always true

		var optExpressionElement = rootObject.get("expression")
				.ifFailure(failures::add)
				.getSuccess();

		var optExpression = optExpressionElement
				.flatMap(element -> element.getAsString()
						.andThen(string -> DefaultParser.parse(string, expressionVariables)
								.mapFailure(failure -> Failure.fromMany(
										failure.getMessages()
												.stream()
												.map(msg -> element.getPath().createFailure(msg))
												.toList()
								))
						)
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return Result.success(new CalculationCase(
					condition,
					optExpression.orElseThrow(),
					optExpressionElement.orElseThrow().getPath()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	public boolean test(Map<String, Double> variables) {
		return condition.eval(variables) != 0;
	}

	public double eval(Map<String, Double> variables) {
		return expression.eval(variables);
	}

	public Optional<Double> getValue(Map<String, Double> variables) {
		if (test(variables)) {
			var value = eval(variables);
			if (Double.isFinite(value)) {
				return Optional.of(value);
			} else {
				for (var message : expressionElementPath.createFailure("Expression returned a value that is not finite").getMessages()) {
					SkillsMod.getInstance().getLogger().warn(message);
				}
				return Optional.of(0.0);
			}
		}
		return Optional.empty();
	}

}
