package net.puffish.skillsmod.calculation;

import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.List;

public class LegacyCalculation {
	public static <T> Result<Calculation<T>, Problem> parse(
			JsonElement rootElement,
			PrototypeView<T> prototype,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, prototype, context));
	}

	public static <T> Result<Calculation<T>, Problem> parse(
			JsonObject rootObject,
			PrototypeView<T> prototype,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var variablesList = new ArrayList<Variables<T, Double>>();

		for (var keys : List.of("parameters", "conditions", "variables")) {
			rootObject.get(keys)
					.getSuccess() // ignore failure because this property is optional
					.ifPresent(variablesElement -> Variables.parse(variablesElement, prototype, context)
							.ifFailure(problems::add)
							.ifSuccess(variablesList::add)
					);
		}

		if (problems.isEmpty()) {
			return rootObject.get("experience")
					.andThen(experienceElement -> Calculation.parse(
							experienceElement,
							Variables.combine(variablesList), context)
					);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}
}
