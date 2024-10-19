package net.puffish.skillsmod.config.colors;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public record ColorsConfig(
		ConnectionsColorsConfig connections,
		FillStrokeColorsConfig points
) {
	private static final FillStrokeColorsConfig DEFAULT_POINTS = new FillStrokeColorsConfig(
			new ColorConfig(0xff80ff20),
			new ColorConfig(0xff000000)
	);

	public static ColorsConfig createDefault() {
		return new ColorsConfig(
				ConnectionsColorsConfig.createDefault(),
				DEFAULT_POINTS
		);
	}

	public static Result<ColorsConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject().andThen(ColorsConfig::parse);
	}

	private static Result<ColorsConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var connections = rootObject.get("connections")
				.getSuccess()
				.flatMap(element -> ConnectionsColorsConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ConnectionsColorsConfig::createDefault);

		var points = rootObject.get("points")
				.getSuccess()
				.flatMap(element -> FillStrokeColorsConfig.parse(element, DEFAULT_POINTS)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_POINTS);

		if (problems.isEmpty()) {
			return Result.success(new ColorsConfig(
					connections,
					points
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}
