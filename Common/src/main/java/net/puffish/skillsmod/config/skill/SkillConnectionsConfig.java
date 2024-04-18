package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public class SkillConnectionsConfig {
	private final SkillConnectionsGroupConfig normal;
	private final SkillConnectionsGroupConfig exclusive;

	private SkillConnectionsConfig(SkillConnectionsGroupConfig normal, SkillConnectionsGroupConfig exclusive) {
		this.normal = normal;
		this.exclusive = exclusive;
	}

	public static Result<SkillConnectionsConfig, Problem> parse(JsonElement rootElement, SkillsConfig skills) {
		return rootElement.getAsObject()
				.andThen(rootObject -> parse(rootObject, skills))
				.orElse(problem -> rootElement.getAsArray()
						.andThen(rootArray -> parseLegacy(rootArray, skills))
				);
	}

	private static Result<SkillConnectionsConfig, Problem> parse(JsonObject rootObject, SkillsConfig skills) {
		var problems = new ArrayList<Problem>();

		var normal = rootObject.get("normal")
				.getSuccess()
				.flatMap(element -> SkillConnectionsGroupConfig.parse(element, skills)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(SkillConnectionsGroupConfig::empty);

		var exclusive = rootObject.get("exclusive")
				.getSuccess()
				.flatMap(element -> SkillConnectionsGroupConfig.parse(element, skills)
						.ifFailure(problems::add)
						.getSuccess()
				)
				.orElseGet(SkillConnectionsGroupConfig::empty);

		if (problems.isEmpty()) {
			return Result.success(new SkillConnectionsConfig(
					normal,
					exclusive
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private static Result<SkillConnectionsConfig, Problem> parseLegacy(JsonArray rootArray, SkillsConfig skills) {
		return SkillConnectionsGroupConfig.parseLegacy(rootArray, skills)
				.mapSuccess(normal -> new SkillConnectionsConfig(
						normal,
						SkillConnectionsGroupConfig.empty()
				));
	}

	public SkillConnectionsGroupConfig getNormal() {
		return normal;
	}

	public SkillConnectionsGroupConfig getExclusive() {
		return exclusive;
	}
}