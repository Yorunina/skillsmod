package net.puffish.skillsmod.expression;

import net.minecraft.util.math.MathHelper;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.api.utils.Failure;

import java.util.List;
import java.util.Set;

public class DefaultParser {
	private static final List<BinaryOperator<Double>> BINARY_OPERATORS = List.of(
			BinaryOperator.createLeft("|", 1, (l, r) -> v -> l.eval(v) == 0.0 && r.eval(v) == 0.0 ? 0.0 : 1.0),
			BinaryOperator.createLeft("&", 2, (l, r) -> v -> l.eval(v) == 0.0 || r.eval(v) == 0.0 ? 0.0 : 1.0),

			BinaryOperator.createLeft("+", 3, (l, r) -> v -> l.eval(v) + r.eval(v)),
			BinaryOperator.createLeft("-", 3, (l, r) -> v -> l.eval(v) - r.eval(v)),
			BinaryOperator.createLeft("*", 4, (l, r) -> v -> l.eval(v) * r.eval(v)),
			BinaryOperator.createLeft("/", 4, (l, r) -> v -> l.eval(v) / r.eval(v)),
			BinaryOperator.createRight("^", 6, (l, r) -> v -> Math.pow(l.eval(v), r.eval(v)))
	);

	private static final List<UnaryOperator<Double>> UNARY_OPERATORS = List.of(
			UnaryOperator.create("!", 5, e -> v -> e.eval(v) == 0.0 ? 1.0 : 0.0),
			
			UnaryOperator.create("+", 5, e -> v -> +e.eval(v)),
			UnaryOperator.create("-", 5, e -> v -> -e.eval(v))
	);

	private static final List<GroupOperator> GROUP_OPERATORS = List.of(
			GroupOperator.create("(", ")")
	);

	private static final List<FunctionOperator<Double>> FUNCTIONS = List.of(
			FunctionOperator.create("sin", "(", ",", ")", 1, l -> v -> Math.sin(l.get(0).eval(v))),
			FunctionOperator.create("cos", "(", ",", ")", 1, l -> v -> Math.cos(l.get(0).eval(v))),
			FunctionOperator.create("tan", "(", ",", ")", 1, l -> v -> Math.tan(l.get(0).eval(v))),

			FunctionOperator.create("asin", "(", ",", ")", 1, l -> v -> Math.asin(l.get(0).eval(v))),
			FunctionOperator.create("acos", "(", ",", ")", 1, l -> v -> Math.acos(l.get(0).eval(v))),
			FunctionOperator.create("atan", "(", ",", ")", 1, l -> v -> Math.atan(l.get(0).eval(v))),
			FunctionOperator.create("atan2", "(", ",", ")", 2, l -> v -> Math.atan2(l.get(0).eval(v), l.get(1).eval(v))),

			FunctionOperator.create("sinh", "(", ",", ")", 1, l -> v -> Math.sinh(l.get(0).eval(v))),
			FunctionOperator.create("cosh", "(", ",", ")", 1, l -> v -> Math.cosh(l.get(0).eval(v))),
			FunctionOperator.create("tanh", "(", ",", ")", 1, l -> v -> Math.tanh(l.get(0).eval(v))),

			FunctionOperator.create("log", "(", ",", ")", 1, l -> v -> Math.log(l.get(0).eval(v))),
			FunctionOperator.create("exp", "(", ",", ")", 1, l -> v -> Math.exp(l.get(0).eval(v))),
			FunctionOperator.create("sqrt", "(", ",", ")", 1, l -> v -> Math.sqrt(l.get(0).eval(v))),
			FunctionOperator.create("cbrt", "(", ",", ")", 1, l -> v -> Math.cbrt(l.get(0).eval(v))),

			FunctionOperator.create("floor", "(", ",", ")", 1, l -> v -> Math.floor(l.get(0).eval(v))),
			FunctionOperator.create("ceil", "(", ",", ")", 1, l -> v -> Math.ceil(l.get(0).eval(v))),
			FunctionOperator.create("round", "(", ",", ")", 1, l -> v -> (double) Math.round(l.get(0).eval(v))),
			FunctionOperator.create("trunc", "(", ",", ")", 1, l -> v -> (double) l.get(0).eval(v).longValue()),

			FunctionOperator.create("abs", "(", ",", ")", 1, l -> v -> Math.abs(l.get(0).eval(v))),
			FunctionOperator.create("sign", "(", ",", ")", 1, l -> v -> Math.signum(l.get(0).eval(v))),
			FunctionOperator.create("fract", "(", ",", ")", 1, l -> v -> MathHelper.fractionalPart(l.get(0).eval(v))),
			FunctionOperator.create("mod", "(", ",", ")", 2, l -> v -> MathHelper.floorMod(l.get(0).eval(v), l.get(1).eval(v))),
			FunctionOperator.create("mix", "(", ",", ")", 3, l -> v -> MathHelper.lerp(l.get(2).eval(v), l.get(0).eval(v), l.get(1).eval(v))),
			FunctionOperator.create("clamp", "(", ",", ")", 3, l -> v -> MathHelper.clamp(l.get(0).eval(v), l.get(1).eval(v), l.get(2).eval(v))),

			FunctionOperator.createVariadic("min", "(", ",", ")", l -> v -> l.stream().mapToDouble(e -> e.eval(v)).min().orElse(+Double.MAX_VALUE)),
			FunctionOperator.createVariadic("max", "(", ",", ")", l -> v -> l.stream().mapToDouble(e -> e.eval(v)).max().orElse(-Double.MAX_VALUE))
	);

	public static Result<Expression<Double>, Failure> parse(String expression, Set<String> variables) {
		return Parser.parse(expression, UNARY_OPERATORS, BINARY_OPERATORS, GROUP_OPERATORS, FUNCTIONS, token -> {
			if (variables.contains(token)) {
				return Result.success(v -> v.get(token));
			} else {
				try {
					var value = Double.parseDouble(token);
					return Result.success(v -> value);
				} catch (Exception e) {
					return Result.failure(Failure.message("Unknown variable `" + token + "`"));
				}
			}
		});
	}
}
