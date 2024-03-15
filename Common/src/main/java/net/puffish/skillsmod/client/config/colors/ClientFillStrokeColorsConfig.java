package net.puffish.skillsmod.client.config.colors;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public record ClientFillStrokeColorsConfig(
		ClientColorConfig fill,
		ClientColorConfig stroke
) {
	public static Result<ClientFillStrokeColorsConfig, Problem> parse(
			JsonElement rootElement,
			ClientFillStrokeColorsConfig defaultColors
	) {
		return ClientColorConfig.parse(rootElement)
				.mapSuccess(fill -> new ClientFillStrokeColorsConfig(fill, defaultColors.stroke))
				.orElse(failure -> rootElement.getAsObject()
						.andThen(rootObject -> parse(rootObject, defaultColors))
				);
	}

	private static Result<ClientFillStrokeColorsConfig, Problem> parse(
			JsonObject rootObject,
			ClientFillStrokeColorsConfig defaultColors
	) {
		var problems = new ArrayList<Problem>();

		var fill = rootObject.get("fill")
				.getSuccess()
				.flatMap(element -> ClientColorConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(defaultColors.fill);

		var stroke = rootObject.get("stroke")
				.getSuccess()
				.flatMap(element -> ClientColorConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(defaultColors.stroke);

		if (problems.isEmpty()) {
			return Result.success(new ClientFillStrokeColorsConfig(
					fill,
					stroke
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}
}
