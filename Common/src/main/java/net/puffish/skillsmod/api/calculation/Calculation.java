package net.puffish.skillsmod.api.calculation;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.impl.calculation.CalculationImpl;

public interface Calculation<T> {

	static <T> Result<Calculation<T>, Failure> parse(
			JsonElementWrapper rootElement,
			Variables<T, Double> variables,
			ConfigContext context
	) {
		return CalculationImpl.create(rootElement, variables, context)
				.mapSuccess(c -> c);
	}

	double evaluate(T t);
}
