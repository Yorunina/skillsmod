package net.puffish.skillsmod.config.skill;

import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public class SkillConfig {
	private final String id;
	private final int x;
	private final int y;
	private final String definitionId;
	private final boolean isRoot;

	private SkillConfig(String id, int x, int y, String definitionId, boolean isRoot) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.definitionId = definitionId;
		this.isRoot = isRoot;
	}

	public static Result<SkillConfig, Problem> parse(String id, JsonElement rootElement, SkillDefinitionsConfig definitions) {
		return rootElement.getAsObject().andThen(
				rootObject -> SkillConfig.parse(id, rootObject, definitions)
		);
	}

	public static Result<SkillConfig, Problem> parse(String id, JsonObject rootObject, SkillDefinitionsConfig definitions) {
		var problems = new ArrayList<Problem>();

		var optX = rootObject.getInt("x")
				.ifFailure(problems::add)
				.getSuccess();

		var optY = rootObject.getInt("y")
				.ifFailure(problems::add)
				.getSuccess();

		var optDefinitionId = rootObject.get("definition")
				.andThen(definitionElement -> definitionElement.getAsString()
						.andThen(definitionId -> {
							if (definitions.getById(definitionId).isPresent()) {
								return Result.success(definitionId);
							} else {
								return Result.failure(definitionElement.getPath().createProblem("Expected a valid definition"));
							}
						})
				)
				.ifFailure(problems::add)
				.getSuccess();

		var isRoot = rootObject.getBoolean("root")
				.getSuccess() // ignore failure because this property is optional
				.orElse(false);

		if (problems.isEmpty()) {
			return Result.success(new SkillConfig(
					id,
					optX.orElseThrow(),
					optY.orElseThrow(),
					optDefinitionId.orElseThrow(),
					isRoot
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public String getId() {
		return id;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getDefinitionId() {
		return definitionId;
	}

	public boolean isRoot() {
		return isRoot;
	}
}
