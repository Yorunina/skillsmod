package net.puffish.skillsmod.client.data;

import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;

import java.util.Map;
import java.util.Objects;

public class ClientCategoryData {
	private final ClientCategoryConfig config;
	private final Map<String, Skill.State> skillStates;

	private int spentPoints;
	private int earnedPoints;

	private int currentLevel;
	private int currentExperience;
	private int requiredExperience;

	public ClientCategoryData(
			ClientCategoryConfig config,
			Map<String, Skill.State> skillStates,
			int spentPoints,
			int earnedPoints,
			int currentLevel,
			int currentExperience,
			int requiredExperience
	) {
		this.config = config;
		this.skillStates = skillStates;
		this.spentPoints = spentPoints;
		this.earnedPoints = earnedPoints;
		this.currentLevel = currentLevel;
		this.currentExperience = currentExperience;
		this.requiredExperience = requiredExperience;
	}

	private void setSkillState(ClientSkillConfig skill, Skill.State state) {
		skillStates.put(skill.id(), state);
	}

	public Skill.State getSkillState(ClientSkillConfig skill) {
		return skillStates.get(skill.id());
	}

	public void unlock(String skillId) {
		var skill = config.skills().get(skillId);
		if (skill == null) {
			return;
		}
		setSkillState(skill, Skill.State.UNLOCKED);
		if (skill.isRoot() && config.exclusiveRoot()) {
			config.skills()
					.values()
					.stream()
					.filter(ClientSkillConfig::isRoot)
					.filter(other -> switch (getSkillState(other)) {
						case AVAILABLE, AFFORDABLE -> true;
						default -> false;
					})
					.forEach(other -> setSkillState(other, Skill.State.LOCKED));
		}
		var normalNeighborsIds = config.normalNeighbors().get(skillId);
		if (normalNeighborsIds != null) {
			normalNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == Skill.State.LOCKED)
					.forEach(neighbor -> {
						if (isAffordable(neighbor)) {
							setSkillState(neighbor, Skill.State.AFFORDABLE);
						} else {
							setSkillState(neighbor, Skill.State.AVAILABLE);
						}
					});
		}
		var exclusiveNeighborsIds = config.exclusiveNeighbors().get(skillId);
		if (exclusiveNeighborsIds != null) {
			exclusiveNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) != Skill.State.UNLOCKED)
					.forEach(neighbor -> setSkillState(neighbor, Skill.State.EXCLUDED));
		}
	}

	public void lock(String skillId) {
		var skill = config.skills().get(skillId);
		if (skill == null) {
			return;
		}
		if (isExcluded(skill)) {
			setSkillState(skill, Skill.State.EXCLUDED);
		} else if (isAvailable(skill)) {
			if (isAffordable(skill)) {
				setSkillState(skill, Skill.State.AFFORDABLE);
			} else {
				setSkillState(skill, Skill.State.AVAILABLE);
			}
		} else {
			setSkillState(skill, Skill.State.LOCKED);
		}
		if (skill.isRoot()) {
			if (config.exclusiveRoot()) {
				if (config.skills()
						.values()
						.stream()
						.filter(ClientSkillConfig::isRoot)
						.allMatch(other -> getSkillState(other) != Skill.State.UNLOCKED)) {
					config.skills()
							.values()
							.stream()
							.filter(ClientSkillConfig::isRoot)
							.filter(other -> getSkillState(other) == Skill.State.LOCKED)
							.forEach(other -> {
								if (isAffordable(other)) {
									setSkillState(other, Skill.State.AFFORDABLE);
								} else {
									setSkillState(other, Skill.State.AVAILABLE);
								}
							});
				}
			}
		}
		var normalNeighborsIds = config.normalNeighbors().get(skillId);
		if (normalNeighborsIds != null) {
			normalNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> switch (getSkillState(neighbor)) {
						case AVAILABLE, AFFORDABLE -> true;
						default -> false;
					})
					.forEach(neighbor -> {
						if (!isAvailable(neighbor)) {
							setSkillState(neighbor, Skill.State.LOCKED);
						}
					});
		}
		var exclusiveNeighborsIds = config.exclusiveNeighbors().get(skillId);
		if (exclusiveNeighborsIds != null) {
			exclusiveNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == Skill.State.EXCLUDED)
					.forEach(neighbor -> {
						if (!isExcluded(neighbor)) {
							if (isAvailable(neighbor)) {
								if (isAffordable(neighbor)) {
									setSkillState(neighbor, Skill.State.AFFORDABLE);
								} else {
									setSkillState(neighbor, Skill.State.AVAILABLE);
								}
							} else {
								setSkillState(neighbor, Skill.State.LOCKED);
							}
						}
					});
		}
	}

	private boolean isExcluded(ClientSkillConfig skill) {
		var exclusiveNeighborsReversedIds = config.exclusiveNeighborsReversed().get(skill.id());
		if (exclusiveNeighborsReversedIds == null) {
			return false;
		}
		return exclusiveNeighborsReversedIds.stream()
				.map(id -> config.skills().get(id))
				.filter(Objects::nonNull)
				.anyMatch(neighbor -> getSkillState(neighbor) == Skill.State.UNLOCKED);
	}

	private boolean isAvailable(ClientSkillConfig skill) {
		if (skill.isRoot()) {
			return !config.exclusiveRoot() || config.skills()
					.values()
					.stream()
					.filter(ClientSkillConfig::isRoot)
					.allMatch(other -> getSkillState(other) != Skill.State.UNLOCKED);
		}
		var normalNeighborsReversedIds = config.normalNeighborsReversed().get(skill.id());
		if (normalNeighborsReversedIds == null) {
			return false;
		}
		return normalNeighborsReversedIds.stream()
				.map(id -> config.skills().get(id))
				.filter(Objects::nonNull)
				.anyMatch(neighbor -> getSkillState(neighbor) == Skill.State.UNLOCKED);
	}

	private boolean isAffordable(ClientSkillConfig skill) {
		return config.getDefinitionById(skill.definitionId()).map(this::isAffordable).orElse(false);
	}

	private boolean isAffordable(ClientSkillDefinitionConfig definition) {
		return getPointsLeft() >= Math.max(definition.requiredPoints(), definition.cost())
				&& spentPoints >= definition.requiredSpentPoints();
	}

	public boolean hasAnySkillLeft() {
		return config.skills()
				.values()
				.stream()
				.map(this::getSkillState)
				.anyMatch(state -> state == Skill.State.AVAILABLE || state == Skill.State.AFFORDABLE);
	}

	public void updatePointsDependencies() {
		config.skills()
				.values()
				.stream()
				.filter(skill -> switch (getSkillState(skill)) {
					case AVAILABLE, AFFORDABLE -> true;
					default -> false;
				})
				.forEach(skill -> {
					if (isAffordable(skill)) {
						setSkillState(skill, Skill.State.AFFORDABLE);
					} else {
						setSkillState(skill, Skill.State.AVAILABLE);
					}
				});
	}

	public ClientCategoryConfig getConfig() {
		return config;
	}

	public int getPointsLeft() {
		return Math.max(Math.min(earnedPoints, config.spentPointsLimit()) - spentPoints, 0);
	}

	public int getSpentPoints() {
		return spentPoints;
	}

	public void setSpentPoints(int spentPoints) {
		this.spentPoints = spentPoints;
	}

	public int getEarnedPoints() {
		return earnedPoints;
	}

	public void setEarnedPoints(int earnedPoints) {
		this.earnedPoints = earnedPoints;
	}

	public int getSpentPointsLeft() {
		return Math.max(config.spentPointsLimit() - spentPoints, 0);
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public boolean hasExperience() {
		return currentLevel >= 0;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public int getCurrentExperience() {
		return currentExperience;
	}

	public void setCurrentExperience(int currentExperience) {
		this.currentExperience = currentExperience;
	}

	public int getRequiredExperience() {
		return requiredExperience;
	}

	public void setRequiredExperience(int requiredExperience) {
		this.requiredExperience = requiredExperience;
	}

	public float getExperienceProgress() {
		return ((float) currentExperience) / ((float) requiredExperience);
	}

	public int getExperienceToNextLevel() {
		return requiredExperience - currentExperience;
	}
}
