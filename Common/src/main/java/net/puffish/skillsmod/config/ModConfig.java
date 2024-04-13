package net.puffish.skillsmod.config;

import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.List;

public class ModConfig {
	private final boolean showWarnings;
	private final List<String> categories;

	private ModConfig(boolean showWarnings, List<String> categories) {
		this.showWarnings = showWarnings;
		this.categories = categories;
	}

	public static Result<ModConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject()
				.andThen(ModConfig::parse);
	}

	public static Result<ModConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var version = rootObject.getInt("version")
				.getSuccessOrElse(e -> Integer.MIN_VALUE);

		var showWarnings = rootObject.getBoolean("show_warnings")
				.getSuccessOrElse(e -> false);

		if (version < SkillsMod.MIN_CONFIG_VERSION) {
			return Result.failure(Problem.message("Configuration is outdated. Check out the mod's wiki to learn how to update the configuration."));
		}
		if (version > SkillsMod.MAX_CONFIG_VERSION) {
			return Result.failure(Problem.message("Configuration is for a newer version of the mod. Please update the mod."));
		}

		var categories = rootObject.get("categories")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(categoriesElement -> categoriesElement.getAsArray()
						.andThen(array -> array.getAsList((i, element) -> BuiltinJson.parseIdentifierPath(element))
								.mapFailure(Problem::combine)
						)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(List::of);

		if (problems.isEmpty()) {
			return Result.success(new ModConfig(
					showWarnings,
					categories
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public boolean getShowWarnings() {
		return showWarnings;
	}

	public List<String> getCategories() {
		return categories;
	}
}
