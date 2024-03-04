package net.puffish.skillsmod.api.calculation;

import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.impl.calculation.VariablesImpl;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

public interface Variables<T, R> {

	static <T> Result<Variables<T, Double>, Failure> parse(
			JsonElementWrapper rootElement,
			PrototypeView<T> prototypeView,
			ConfigContext context
	) {
		return VariablesImpl.parse(rootElement, prototypeView, context)
				.mapSuccess(v -> v);
	}

	static <T, R> Variables<T, R> create(
			Map<String, Function<T, R>> operations
	) {
		return VariablesImpl.create(operations);
	}

	static <T, R> Variables<T, R> combine(
			Collection<Variables<T, R>> variables
	) {
		return VariablesImpl.combine(variables);
	}

	@SafeVarargs
	static <T, R> Variables<T, R> combine(
			Variables<T, R>... variables
	) {
		return VariablesImpl.combine(variables);
	}

	Stream<String> streamNames();

	Map<String, R> evaluate(T t);
}
