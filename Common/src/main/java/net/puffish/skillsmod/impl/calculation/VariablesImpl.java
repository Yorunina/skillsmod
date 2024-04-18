package net.puffish.skillsmod.impl.calculation;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.impl.calculation.operation.OperationConfigContextImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VariablesImpl<T, R> implements Variables<T, R> {
	private final Map<String, Function<T, R>> operations;

	private VariablesImpl(Map<String, Function<T, R>> operations) {
		this.operations = operations;
	}

	@Override
	public Stream<String> streamNames() {
		return operations.keySet().stream();
	}

	@Override
	public Map<String, R> evaluate(T t) {
		return operations.entrySet()
				.stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().apply(t)));
	}

	public static <T, R> Variables<T, R> create(
			Map<String, Function<T, R>> operations
	) {
		return new VariablesImpl<>(Map.copyOf(operations));
	}

	public static <T, R> Variables<T, R> combine(
			Collection<Variables<T, R>> variables
	) {
		return new CombineVariables<>(List.copyOf(variables));
	}

	@SafeVarargs
	public static <T, R> Variables<T, R> combine(
			Variables<T, R>... variables
	) {
		return new CombineVariables<>(Arrays.asList(variables));
	}

	public static <T> Result<VariablesImpl<T, Double>, Problem> parse(
			JsonElement rootElement,
			PrototypeView<T> prototypeView,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(rootObject -> parse(rootObject, prototypeView, context));
	}

	public static <T> Result<VariablesImpl<T, Double>, Problem> parse(
			JsonObject rootObject,
			PrototypeView<T> prototypeView,
			ConfigContext context
	) {
		return rootObject.getAsMap((key, value) -> parseVariable(value, prototypeView, context))
				.mapFailure(problems -> Problem.combine(problems.values()))
				.mapSuccess(VariablesImpl::new);
	}

	public static <T> Result<Function<T, Double>, Problem> parseVariable(
			JsonElement rootElement,
			PrototypeView<T> prototypeView,
			ConfigContext context) {
		return rootElement.getAsObject().andThen(rootObject -> parseVariable(rootObject, prototypeView, context));
	}

	public static <T> Result<Function<T, Double>, Problem> parseVariable(
			JsonObject rootObject,
			PrototypeView<T> prototypeView,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var optPrototypeView = parseOperation(rootObject, prototypeView, context, "legacy_")
				.getSuccess() // Backwards compatibility.
				.or(() -> rootObject.getArray("operations")
						.ifFailure(problems::add)
						.getSuccess()
						.flatMap(array -> {
							var view = Optional.of(prototypeView);
							for (var element : (Iterable<JsonElement>) array.stream()::iterator) {
								view = view.flatMap(
										v -> parseOperation(element, v, context)
												.ifFailure(problems::add)
												.getSuccess()
								);
							}
							return view;
						})
				);

		var optFallback = rootObject.get("fallback")
				.getSuccess()
				.flatMap(fallbackElement -> fallbackElement.getAsDouble()
						.ifFailure(problems::add)
						.getSuccess()
				);

		var required = rootObject.getBoolean("required")
				.getSuccessOrElse(e -> true);

		if (problems.isEmpty()) {
			return buildVariable(
					optPrototypeView.orElseThrow(),
					optFallback,
					rootObject.getPath().getObject("operations")
			).orElse(problem -> {
				if (required || optFallback.isEmpty()) {
					return Result.failure(problem);
				} else {
					context.emitWarning(problem.toString());
					var fallback = optFallback.orElseThrow();
					return Result.success(t -> fallback);
				}
			});
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public static <T> Result<PrototypeView<T>, Problem> parseOperation(
			JsonElement rootElement,
			PrototypeView<T> prototypeView,
			ConfigContext context
	) {
		return rootElement.getAsObject().andThen(rootObject -> parseOperation(rootObject, prototypeView, context, ""));
	}

	public static <T> Result<PrototypeView<T>, Problem> parseOperation(
			JsonObject rootObject,
			PrototypeView<T> prototypeView,
			ConfigContext context,
			String prefix
	) {
		var problems = new ArrayList<Problem>();

		var optType = rootObject.get("type")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		var maybeDataElement = rootObject.get("data");

		if (problems.isEmpty()) {
			return buildOperation(
					prototypeView,
					optType.orElseThrow().withPrefixedPath(prefix),
					rootObject.getPath().getObject("type"),
					maybeDataElement,
					context
			);
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static <T> Result<PrototypeView<T>, Problem> buildOperation(
			PrototypeView<T> prototypeView,
			Identifier type,
			JsonPath typePath,
			Result<JsonElement, Problem> maybeDataElement,
			ConfigContext context
	) {
		if (type.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
			type = new Identifier(prototypeView.getId().getNamespace(), type.getPath());
		}
		var factory = prototypeView.getView(type, new OperationConfigContextImpl(context, maybeDataElement));
		if (factory.isEmpty()) {
			return Result.failure(typePath.createProblem("Expected a valid operation type"));
		}
		return factory.orElseThrow();
	}

	private static <T> Result<Function<T, Double>, Problem> buildVariable(
			PrototypeView<T> prototypeView,
			Optional<Double> fallback,
			JsonPath operationsPath
	) {
		var optOperation = prototypeView.getOperation(BuiltinPrototypes.NUMBER)
				.or(() -> prototypeView.getOperation(BuiltinPrototypes.BOOLEAN)
						.map(o -> t -> o.apply(t).map(b -> b ? 1.0 : 0.0))
				);
		if (optOperation.isPresent()) {
			return Result.success(t -> optOperation.orElseThrow().apply(t).orElseGet(() -> {
				if (fallback.isEmpty()) {
					SkillsMod.getInstance().getLogger().warn(
							operationsPath.createProblem("Fallback is not specified but operations returned no value").toString()
					);
				}
				return fallback.orElse(Double.NaN);
			}));
		} else {
			return Result.failure(operationsPath.createProblem("Expected operations to provide a number"));
		}
	}

	private record CombineVariables<T, R>(List<Variables<T, R>> variablesList) implements Variables<T, R> {
		@Override
		public Stream<String> streamNames() {
			return variablesList.stream().flatMap(Variables::streamNames);
		}

		@Override
		public Map<String, R> evaluate(T t) {
			return variablesList.stream()
					.flatMap(variables -> variables.evaluate(t).entrySet().stream())
					.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		}
	}
}