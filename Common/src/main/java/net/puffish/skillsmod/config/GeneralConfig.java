package net.puffish.skillsmod.config;

import net.minecraft.text.Text;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public class GeneralConfig {
	private final Text title;
	private final IconConfig icon;
	private final BackgroundConfig background;
	private final com.google.gson.JsonElement colors;
	private final boolean unlockedByDefault;
	private final int startingPoints;
	private final boolean exclusiveRoot;
	private final int spentPointsLimit;

	private GeneralConfig(
			Text title,
			IconConfig icon,
			BackgroundConfig background,
			com.google.gson.JsonElement colors,
			boolean unlockedByDefault,
			int startingPoints,
			boolean exclusiveRoot,
			int spentPointsLimit
	) {
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.colors = colors;
		this.unlockedByDefault = unlockedByDefault;
		this.startingPoints = startingPoints;
		this.exclusiveRoot = exclusiveRoot;
		this.spentPointsLimit = spentPointsLimit;
	}

	public static Result<GeneralConfig, Problem> parse(JsonElement rootElement) {
		return rootElement.getAsObject()
				.andThen(GeneralConfig::parse);
	}

	public static Result<GeneralConfig, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optTitle = rootObject.get("title")
				.andThen(BuiltinJson::parseText)
				.ifFailure(problems::add)
				.getSuccess();

		var optIcon = rootObject.get("icon")
				.andThen(IconConfig::parse)
				.ifFailure(problems::add)
				.getSuccess();

		var optBackground = rootObject.get("background")
				.andThen(BackgroundConfig::parse)
				.ifFailure(problems::add)
				.getSuccess();

		var colors = rootObject.get("colors")
				.getSuccess() // ignore failure because this property is optional
				.map(JsonElement::getJson)
				.orElse(new com.google.gson.JsonObject());

		var unlockedByDefault = rootObject.get("unlocked_by_default")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(true);

		var startingPoints = rootObject.get("starting_points")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(0);

		var exclusiveRoot = rootObject.get("exclusive_root")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsBoolean()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(false);

		var spentPointsLimit = rootObject.get("spent_points_limit")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> element.getAsInt()
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElse(Integer.MAX_VALUE);

		if (problems.isEmpty()) {
			return Result.success(new GeneralConfig(
					optTitle.orElseThrow(),
					optIcon.orElseThrow(),
					optBackground.orElseThrow(),
					colors,
					unlockedByDefault,
					startingPoints,
					exclusiveRoot,
					spentPointsLimit
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public Text getTitle() {
		return title;
	}

	public boolean isUnlockedByDefault() {
		return unlockedByDefault;
	}

	public int getStartingPoints() {
		return startingPoints;
	}

	public boolean isExclusiveRoot() {
		return exclusiveRoot;
	}

	public IconConfig getIcon() {
		return icon;
	}

	public BackgroundConfig getBackground() {
		return background;
	}

	public com.google.gson.JsonElement getColors() {
		return colors;
	}

	public int getSpentPointsLimit() {
		return spentPointsLimit;
	}
}
