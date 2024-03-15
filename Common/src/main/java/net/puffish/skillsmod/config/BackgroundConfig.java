package net.puffish.skillsmod.config;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public class BackgroundConfig {
	private final com.google.gson.JsonElement data;

	private BackgroundConfig(com.google.gson.JsonElement data) {
		this.data = data;
	}

	public static Result<BackgroundConfig, Problem> parse(JsonElement rootElement) {
		// Backwards compatibility.
		return rootElement.getAsString()
				.andThen(texture -> {
					var data = new com.google.gson.JsonObject();
					data.addProperty("texture", texture);
					data.addProperty("width", 16);
					data.addProperty("height", 16);
					data.addProperty("position", "tile");
					return Result.success(new BackgroundConfig(data));
				})
				.orElse(failure -> Result.success(new BackgroundConfig(rootElement.getJson())));
	}

	public com.google.gson.JsonElement getData() {
		return data;
	}
}
