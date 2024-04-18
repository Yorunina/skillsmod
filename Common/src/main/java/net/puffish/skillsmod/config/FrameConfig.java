package net.puffish.skillsmod.config;

import net.minecraft.advancement.AdvancementFrame;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public class FrameConfig {
	private final String type;
	private final com.google.gson.JsonElement data;

	private FrameConfig(String type, com.google.gson.JsonElement data) {
		this.type = type;
		this.data = data;
	}

	public static FrameConfig fromAdvancementFrame(AdvancementFrame frame) {
		var data = new com.google.gson.JsonObject();
		data.addProperty("frame", frame.getId());
		return new FrameConfig(
				"advancement",
				data
		);
	}

	public static Result<FrameConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject()
				.andThen(FrameConfig::parse);
	}

	public static Result<FrameConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var type = rootObject.getString("type")
				.ifFailure(problems::add)
				.getSuccess();

		var data = rootObject.get("data")
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new FrameConfig(
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
