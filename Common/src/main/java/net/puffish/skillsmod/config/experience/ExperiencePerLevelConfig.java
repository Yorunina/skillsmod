package net.puffish.skillsmod.config.experience;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.expression.DefaultParser;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class ExperiencePerLevelConfig {
	private final Function<Integer, Integer> function;

	private ExperiencePerLevelConfig(Function<Integer, Integer> function) {
		this.function = function;
	}

	public static Result<ExperiencePerLevelConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject().andThen(ExperiencePerLevelConfig::parse);
	}

	public static Result<ExperiencePerLevelConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var maybeDataElement = rootObject.get("data");

		var optFunction = rootObject.get("type")
				.andThen(typeElement -> typeElement.getAsString()
						.andThen(type -> parseType(type, maybeDataElement, typeElement.getPath()))
				)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new ExperiencePerLevelConfig(
					optFunction.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<Function<Integer, Integer>, Problem> parseType(String type, Result<JsonElement, Problem> maybeDataElement, JsonPath typeElementPath) {
		return switch (type) {
			case "expression" -> parseExpression(maybeDataElement);
			case "values" -> parseValues(maybeDataElement);
			default -> Result.failure(typeElementPath.createProblem("Expected a valid condition type"));
		};
	}

	private static Result<Function<Integer, Integer>, Problem> parseExpression(Result<JsonElement, Problem> maybeDataElement) {
		return maybeDataElement
				.andThen(JsonElement::getAsObject)
				.andThen(dataObject -> dataObject.get("expression"))
				.andThen(expressionElement -> expressionElement.getAsString()
						.andThen(expression -> DefaultParser.parse(expression, Set.of("level")))
						.mapSuccess(expression -> level -> {
							var value = expression.eval(Map.ofEntries(Map.entry("level", (double) level)));
							if (Double.isFinite(value)) {
								return (int) Math.round(value);
							} else {
								SkillsMod.getInstance().getLogger().warn(
										expressionElement.getPath()
												.createProblem("Expression returned a value that is not finite")
												.toString()
								);
								return 0;
							}
						})
				);
	}

	private static Result<Function<Integer, Integer>, Problem> parseValues(Result<JsonElement, Problem> maybeDataElement) {
		return maybeDataElement
				.andThen(JsonElement::getAsObject)
				.andThen(dataObject -> dataObject.getArray("values"))
				.andThen(valueArray -> valueArray.getAsList((k, element) -> element.getAsInt()).mapFailure(Problem::combine))
				.mapSuccess(values -> level -> values.get(Math.min(level, values.size() - 1)));
	}

	public Function<Integer, Integer> getFunction() {
		return function;
	}
}
