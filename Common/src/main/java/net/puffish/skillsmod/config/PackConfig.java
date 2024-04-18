package net.puffish.skillsmod.config;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.List;

public class PackConfig {
	private final List<String> categories;

	private PackConfig(List<String> categories) {
		this.categories = categories;
	}

	public static Result<PackConfig, Problem> parse(String name, JsonElement rootElement) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(name, rootObject));
	}

	public static Result<PackConfig, Problem> parse(String name, JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var version = rootObject.getInt("version")
				.getSuccessOrElse(e -> Integer.MIN_VALUE);

		if (version < SkillsMod.CONFIG_VERSION) {
			return Result.failure(Problem.message("Data pack `" + name + "` is outdated. Check out the mod's wiki to learn how to update the data pack."));
		}
		if (version > SkillsMod.CONFIG_VERSION) {
			return Result.failure(Problem.message("Data pack `" + name + "` is for a newer version of the mod. Please update the mod."));
		}

		var optCategories = rootObject.getArray("categories")
				.andThen(array -> array.getAsList((i, element) -> BuiltinJson.parseIdentifierPath(element))
						.mapFailure(Problem::combine)
				)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new PackConfig(
					optCategories.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public List<String> getCategories() {
		return categories;
	}
}
