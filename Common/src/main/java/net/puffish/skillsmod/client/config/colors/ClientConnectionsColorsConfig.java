package net.puffish.skillsmod.client.config.colors;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public record ClientConnectionsColorsConfig(
		ClientFillStrokeColorsConfig locked,
		ClientFillStrokeColorsConfig available,
		ClientFillStrokeColorsConfig affordable,
		ClientFillStrokeColorsConfig unlocked,
		ClientFillStrokeColorsConfig excluded
) {
	private static final ClientFillStrokeColorsConfig DEFAULT_LOCKED = new ClientFillStrokeColorsConfig(
			new ClientColorConfig(0xffffffff),
			new ClientColorConfig(0xff000000)
	);
	private static final ClientFillStrokeColorsConfig DEFAULT_AVAILABLE_AFFORDABLE = new ClientFillStrokeColorsConfig(
			new ClientColorConfig(0xffffffff),
			new ClientColorConfig(0xff000000)
	);
	private static final ClientFillStrokeColorsConfig DEFAULT_UNLOCKED = new ClientFillStrokeColorsConfig(
			new ClientColorConfig(0xffffffff),
			new ClientColorConfig(0xff000000)
	);
	private static final ClientFillStrokeColorsConfig DEFAULT_EXCLUDED = new ClientFillStrokeColorsConfig(
			new ClientColorConfig(0xffff0000),
			new ClientColorConfig(0xff000000)
	);

	public static ClientConnectionsColorsConfig createDefault() {
		return new ClientConnectionsColorsConfig(
				DEFAULT_LOCKED,
				DEFAULT_AVAILABLE_AFFORDABLE,
				DEFAULT_AVAILABLE_AFFORDABLE,
				DEFAULT_UNLOCKED,
				DEFAULT_EXCLUDED
		);
	}

	public static Result<ClientConnectionsColorsConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject().andThen(ClientConnectionsColorsConfig::parse);
	}

	private static Result<ClientConnectionsColorsConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var locked = rootObject.get("locked")
				.getSuccess()
				.flatMap(element -> ClientFillStrokeColorsConfig.parse(element, DEFAULT_LOCKED)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_LOCKED);

		var available = rootObject.get("available")
				.getSuccess()
				.flatMap(element -> ClientFillStrokeColorsConfig.parse(element, DEFAULT_AVAILABLE_AFFORDABLE)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_AVAILABLE_AFFORDABLE);

		var affordable = rootObject.get("affordable")
				.getSuccess()
				.flatMap(element -> ClientFillStrokeColorsConfig.parse(element, DEFAULT_AVAILABLE_AFFORDABLE)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(available);

		var unlocked = rootObject.get("unlocked")
				.getSuccess()
				.flatMap(element -> ClientFillStrokeColorsConfig.parse(element, DEFAULT_UNLOCKED)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_UNLOCKED);

		var excluded = rootObject.get("excluded")
				.getSuccess()
				.flatMap(element -> ClientFillStrokeColorsConfig.parse(element, DEFAULT_EXCLUDED)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(DEFAULT_EXCLUDED);

		if (problems.isEmpty()) {
			return Result.success(new ClientConnectionsColorsConfig(
					locked,
					available,
					affordable,
					unlocked,
					excluded
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

}