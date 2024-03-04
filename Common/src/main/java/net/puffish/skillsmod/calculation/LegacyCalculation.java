package net.puffish.skillsmod.calculation;

import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.List;

public class LegacyCalculation {
	public static <T> Result<Calculation<T>, Failure> parse(
			JsonElementWrapper rootElement,
			PrototypeView<T> prototype,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, prototype, context));
	}

	public static <T> Result<Calculation<T>, Failure> parse(
			JsonObjectWrapper rootObject,
			PrototypeView<T> prototype,
			ConfigContext context
	) {
		var failures = new ArrayList<Failure>();

		var variablesList = new ArrayList<Variables<T, Double>>();

		for (var keys : List.of("parameters", "conditions", "variables")) {
			rootObject.get(keys)
					.getSuccess() // ignore failure because this property is optional
					.ifPresent(variablesElement -> Variables.parse(variablesElement, prototype, context)
							.ifFailure(failures::add)
							.ifSuccess(variablesList::add)
					);
		}

		if (failures.isEmpty()) {
			return rootObject.get("experience")
					.andThen(experienceElement -> Calculation.parse(
							experienceElement,
							Variables.combine(variablesList), context)
					);
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}
}
