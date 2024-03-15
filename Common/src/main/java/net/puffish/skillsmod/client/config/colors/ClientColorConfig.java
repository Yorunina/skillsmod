package net.puffish.skillsmod.client.config.colors;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public record ClientColorConfig(int argb) {
	public static Result<ClientColorConfig, Problem> parse(JsonElement element) {
		return element.getAsString().andThen(string -> {
			try {
				if (string.startsWith("#")) {
					string = string.substring(1);
					switch (string.length()) {
						case 3 -> {
							var color = Integer.parseInt(string, 16);
							return Result.success(new ClientColorConfig(
									(((color & 0xf00) << 8) | ((color & 0xf0) << 4) | (color & 0xf)) * 0x11 | 0xff000000
							));
						}
						case 6 -> {
							return Result.success(new ClientColorConfig(
									Integer.parseInt(string, 16) | 0xff000000
							));
						}
						default -> { }
					}
				}
			} catch (Exception ignored) { }
			return Result.failure(element.getPath().createProblem("Expected a valid color"));
		});
	}
}
