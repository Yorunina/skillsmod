package net.puffish.skillsmod.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public class GeneralConfig {
	private final Text title;
	private final IconConfig icon;
	private final Identifier background;
	private final boolean unlockedByDefault;
	private final boolean exclusiveRoot;
	private final int spentPointsLimit;

	private GeneralConfig(Text title, IconConfig icon, Identifier background, boolean unlockedByDefault, boolean exclusiveRoot, int spentPointsLimit) {
		this.title = title;
		this.icon = icon;
		this.background = background;
		this.unlockedByDefault = unlockedByDefault;
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
				.andThen(BuiltinJson::parseIdentifier)
				.ifFailure(problems::add)
				.getSuccess();

		var optUnlockedByDefault = rootObject.getBoolean("unlocked_by_default")
				.ifFailure(problems::add)
				.getSuccess();

		var exclusiveRoot = rootObject.getBoolean("exclusive_root")
				.getSuccess() // ignore failure because this property is optional
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
					optUnlockedByDefault.orElseThrow(),
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

	public boolean isExclusiveRoot() {
		return exclusiveRoot;
	}

	public IconConfig getIcon() {
		return icon;
	}

	public Identifier getBackground() {
		return background;
	}

	public int getSpentPointsLimit() {
		return spentPointsLimit;
	}
}
