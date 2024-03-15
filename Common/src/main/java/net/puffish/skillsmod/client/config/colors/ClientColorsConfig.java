package net.puffish.skillsmod.client.config.colors;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public record ClientColorsConfig(
		ClientConnectionsColorsConfig connections,
		ClientFillStrokeColorsConfig points
) {
	private static final ClientFillStrokeColorsConfig DEFAULT_POINTS = new ClientFillStrokeColorsConfig(
			new ClientColorConfig(0xff80ff20),
			new ClientColorConfig(0xff000000)
	);

	public static ClientColorsConfig createDefault() {
		return ClientColorsConfig.parse(
				JsonElement.parseString("{}", JsonPath.create("Client Colors Default"))
						.getSuccess().orElseThrow()
		).getSuccess().orElseThrow();
	}

	public static Result<ClientColorsConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject().andThen(ClientColorsConfig::parse);
	}

	private static Result<ClientColorsConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var connections = rootObject.get("connections")
				.getSuccess()
				.flatMap(element -> ClientConnectionsColorsConfig.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(ClientConnectionsColorsConfig::createDefault);

		var points = rootObject.get("points")
				.getSuccess()
				.flatMap(element -> ClientFillStrokeColorsConfig.parse(element, DEFAULT_POINTS)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_POINTS);

		if (problems.isEmpty()) {
			return Result.success(new ClientColorsConfig(
					connections,
					points
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}
