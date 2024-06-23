package net.puffish.skillsmod.config.experience;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.DisposeContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExperienceConfig {
	private final ExperiencePerLevelConfig experiencePerLevel;
	private final List<ExperienceSourceConfig> experienceSources;

	private ExperienceConfig(ExperiencePerLevelConfig experiencePerLevel, List<ExperienceSourceConfig> experienceSources) {
		this.experiencePerLevel = experiencePerLevel;
		this.experienceSources = experienceSources;
	}

	public static Result<Optional<ExperienceConfig>, Problem> parse(JsonElement rootElement, ConfigContext context) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, context));
	}

	public static Result<Optional<ExperienceConfig>, Problem> parse(JsonObject rootObject, ConfigContext context) {
		var problems = new ArrayList<Problem>();

		// Deprecated
		var enabled = rootObject.getBoolean("enabled")
				.getSuccess()
				.orElse(true);

		var optExperiencePerLevel = rootObject.get("experience_per_level")
				.andThen(ExperiencePerLevelConfig::parse)
				.ifFailure(problems::add)
				.getSuccess();

		var experienceSources = rootObject.getArray("sources")
				.andThen(array -> array.getAsList((i, element) -> ExperienceSourceConfig.parse(element, context)).mapFailure(Problem::combine))
				.ifFailure(problems::add)
				.getSuccess()
				.orElseGet(List::of);

		if (problems.isEmpty()) {
			if (enabled) {
				return Result.success(Optional.of(new ExperienceConfig(
						optExperiencePerLevel.orElseThrow(),
						experienceSources
				)));
			} else {
				return Result.success(Optional.empty());
			}
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public int getRequiredExperience(int level) {
		return experiencePerLevel.getFunction().apply(level);
	}

	public int getRequiredTotalExperience(int level) {
		var total = 0;
		for (var i = 0; i < level; i++) {
			total += getRequiredExperience(level);
		}
		return total;
	}

	public int getCurrentExperience(int earnedExperience) {
		var level = 0;

		while (true) {
			var requiredExperience = getRequiredExperience(level);

			if (earnedExperience < requiredExperience) {
				return earnedExperience;
			}

			earnedExperience -= requiredExperience;
			level++;
		}
	}

	public int getCurrentLevel(int earnedExperience) {
		var level = 0;

		while (true) {
			var requiredExperience = getRequiredExperience(level);

			if (earnedExperience < requiredExperience) {
				return level;
			}

			earnedExperience -= requiredExperience;
			level++;
		}
	}

	public void dispose(DisposeContext context) {
		for (var experienceSource : experienceSources) {
			experienceSource.dispose(context);
		}
	}

	public ExperiencePerLevelConfig getExperiencePerLevel() {
		return experiencePerLevel;
	}

	public List<ExperienceSourceConfig> getExperienceSources() {
		return experienceSources;
	}
}
