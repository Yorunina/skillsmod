package net.puffish.skillsmod.config;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.config.experience.ExperienceConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.DisposeContext;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Function;

public class CategoryConfig {
	private final Identifier id;
	private final GeneralConfig general;
	private final SkillDefinitionsConfig definitions;
	private final SkillsConfig skills;
	private final SkillConnectionsConfig connections;
	private final Optional<ExperienceConfig> optExperience;

	private CategoryConfig(
			Identifier id,
			GeneralConfig general,
			SkillDefinitionsConfig definitions,
			SkillsConfig skills,
			SkillConnectionsConfig connections,
			Optional<ExperienceConfig> optExperience
	) {
		this.id = id;
		this.general = general;
		this.definitions = definitions;
		this.skills = skills;
		this.connections = connections;
		this.optExperience = optExperience;
	}

	public static Result<CategoryConfig, Problem> parse(
			Identifier id,
			JsonElement generalElement,
			JsonElement definitionsElement,
			JsonElement skillsElement,
			JsonElement connectionsElement,
			Optional<JsonElement> optExperienceElement,
			ConfigContext context
	) {
		var problems = new ArrayList<Problem>();

		var optGeneral = GeneralConfig.parse(generalElement)
				.ifFailure(problems::add)
				.getSuccess();

		var optExperience = optExperienceElement
				.flatMap(experience -> ExperienceConfig.parse(experience, context)
						.ifFailure(problems::add)
						.getSuccess()
						.flatMap(Function.identity())
				);

		var optDefinitions = SkillDefinitionsConfig.parse(definitionsElement, context)
				.ifFailure(problems::add)
				.getSuccess();

		var optSkills = optDefinitions.flatMap(
				definitions -> SkillsConfig.parse(skillsElement, definitions)
						.ifFailure(problems::add)
						.getSuccess()
		);

		var optConnections = optSkills.flatMap(
				skills -> SkillConnectionsConfig.parse(connectionsElement, skills)
						.ifFailure(problems::add)
						.getSuccess()
		);

		if (problems.isEmpty()) {
			return Result.success(new CategoryConfig(
					id,
					optGeneral.orElseThrow(),
					optDefinitions.orElseThrow(),
					optSkills.orElseThrow(),
					optConnections.orElseThrow(),
					optExperience
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	public void dispose(DisposeContext context) {
		definitions.dispose(context);
		optExperience.ifPresent(experience -> experience.dispose(context));
	}

	public Identifier getId() {
		return id;
	}

	public GeneralConfig getGeneral() {
		return general;
	}

	public SkillDefinitionsConfig getDefinitions() {
		return definitions;
	}

	public SkillsConfig getSkills() {
		return skills;
	}

	public SkillConnectionsConfig getConnections() {
		return connections;
	}

	public Optional<ExperienceConfig> getExperience() {
		return optExperience;
	}
}
