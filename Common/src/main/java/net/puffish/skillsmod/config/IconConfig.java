package net.puffish.skillsmod.config;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public class IconConfig {
	private final String type;
	private final com.google.gson.JsonElement data;

	private IconConfig(String type, com.google.gson.JsonElement data) {
		this.type = type;
		this.data = data;
	}

	public static Result<IconConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject()
				.andThen(IconConfig::parse);
	}

	public static Result<IconConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var type = rootObject.getString("type")
				.ifFailure(problems::add)
				.getSuccess();

		var data = rootObject.get("data")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new IconConfig(
					type.orElseThrow(),
					data.orElseThrow().getJson()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public String getType() {
		return type;
	}

	public com.google.gson.JsonElement getData() {
		return data;
	}
}
