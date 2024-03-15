package net.puffish.skillsmod.client.config;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public record ClientBackgroundConfig(
		Identifier texture,
		int width,
		int height,
		Position position
) {
	public enum Position {
		NONE,
		TILE,
		FILL,
		FILL_WIDTH,
		FILL_HEIGHT;

		public static Result<Position, Problem> parse(JsonElement rootElement) {
			return rootElement.getAsString().andThen(string -> switch (string) {
				case "none" -> Result.success(Position.NONE);
				case "tile" -> Result.success(Position.TILE);
				case "fill" -> Result.success(Position.FILL);
				case "fill_width" -> Result.success(Position.FILL_WIDTH);
				case "fill_height" -> Result.success(Position.FILL_HEIGHT);
				default -> Result.failure(rootElement.getPath().createProblem("Expected valid background position"));
			});
		}
	}

	public static ClientBackgroundConfig createMissing() {
		return new ClientBackgroundConfig(
				TextureManager.MISSING_IDENTIFIER,
				16,
				16,
				Position.TILE
		);
	}

	public static Result<ClientBackgroundConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject().andThen(ClientBackgroundConfig::parse);
	}

	public static Result<ClientBackgroundConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTexture = rootObject.get("texture")
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		var optWidth = rootObject.getInt("width")
				.ifFailure(problems::add)
				.getSuccess();

		var optHeight = rootObject.getInt("height")
				.ifFailure(problems::add)
				.getSuccess();

		var position = rootObject.get("position")
				.getSuccess()
				.flatMap(element -> Position.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(Position.NONE);

		if (problems.isEmpty()) {
			return Result.success(new ClientBackgroundConfig(
					optTexture.orElseThrow(),
					optWidth.orElseThrow(),
					optHeight.orElseThrow(),
					position
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}
}
