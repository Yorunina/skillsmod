package net.puffish.skillsmod.client.data;

import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.skill.SkillState;

import java.util.Map;
import java.util.Objects;

public class ClientCategoryData {
	private final ClientCategoryConfig config;
	private final Map<String, SkillState> skillStates;

	private int spentPoints;
	private int earnedPoints;

	private int currentLevel;
	private int currentExperience;
	private int requiredExperience;

	public ClientCategoryData(
			ClientCategoryConfig config,
			Map<String, SkillState> skillStates,
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

	private void setSkillState(ClientSkillConfig skill, SkillState state) {
		skillStates.put(skill.id(), state);
	}

	public SkillState getSkillState(ClientSkillConfig skill) {
		return skillStates.get(skill.id());
	}

	public void unlock(String skillId) {
		var skill = config.skills().get(skillId);
		if (skill == null) {
			return;
		}
		setSkillState(skill, SkillState.UNLOCKED);
		if (skill.isRoot() && config.exclusiveRoot()) {
			config.skills()
					.values()
					.stream()
					.filter(ClientSkillConfig::isRoot)
					.filter(other -> getSkillState(other) == SkillState.AVAILABLE)
					.forEach(other -> setSkillState(other, SkillState.LOCKED));
		}
		var normalNeighborsIds = config.normalNeighbors().get(skillId);
		if (normalNeighborsIds != null) {
			normalNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == SkillState.LOCKED)
					.forEach(neighbor -> setSkillState(neighbor, SkillState.AVAILABLE));
		}
		var exclusiveNeighborsIds = config.exclusiveNeighbors().get(skillId);
		if (exclusiveNeighborsIds != null) {
			exclusiveNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) != SkillState.UNLOCKED)
					.forEach(neighbor -> setSkillState(neighbor, SkillState.EXCLUDED));
		}
	}

	public void lock(String skillId) {
		var skill = config.skills().get(skillId);
		if (skill == null) {
			return;
		}
		if (isExcluded(skill)) {
			setSkillState(skill, SkillState.EXCLUDED);
		} else if (isAvailable(skill)) {
			setSkillState(skill, SkillState.AVAILABLE);
		} else {
			setSkillState(skill, SkillState.LOCKED);
		}
		if (skill.isRoot()) {
			if (config.exclusiveRoot()) {
				if (config.skills()
						.values()
						.stream()
						.filter(ClientSkillConfig::isRoot)
						.allMatch(other -> getSkillState(other) != SkillState.UNLOCKED)) {
					config.skills()
							.values()
							.stream()
							.filter(ClientSkillConfig::isRoot)
							.filter(other -> getSkillState(other) == SkillState.LOCKED)
							.forEach(other -> setSkillState(other, SkillState.AVAILABLE));
				}
			}
		}
		var normalNeighborsIds = config.normalNeighbors().get(skillId);
		if (normalNeighborsIds != null) {
			normalNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == SkillState.AVAILABLE)
					.forEach(neighbor -> {
						if (!isAvailable(neighbor)) {
							setSkillState(neighbor, SkillState.LOCKED);
						}
					});
		}
		var exclusiveNeighborsIds = config.exclusiveNeighbors().get(skillId);
		if (exclusiveNeighborsIds != null) {
			exclusiveNeighborsIds.stream()
					.map(id -> config.skills().get(id))
					.filter(Objects::nonNull)
					.filter(neighbor -> getSkillState(neighbor) == SkillState.EXCLUDED)
					.forEach(neighbor -> {
						if (!isExcluded(neighbor)) {
							if (isAvailable(neighbor)) {
								setSkillState(neighbor, SkillState.AVAILABLE);
							} else {
								setSkillState(neighbor, SkillState.LOCKED);
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
				.anyMatch(neighbor -> getSkillState(neighbor) == SkillState.UNLOCKED);
	}

	private boolean isAvailable(ClientSkillConfig skill) {
		if (skill.isRoot()) {
			return !config.exclusiveRoot() || config.skills()
					.values()
					.stream()
					.filter(ClientSkillConfig::isRoot)
					.allMatch(other -> getSkillState(other) != SkillState.UNLOCKED);
		}
		var normalNeighborsReversedIds = config.normalNeighborsReversed().get(skill.id());
		if (normalNeighborsReversedIds == null) {
			return false;
		}
		return normalNeighborsReversedIds.stream()
				.map(id -> config.skills().get(id))
				.filter(Objects::nonNull)
				.anyMatch(neighbor -> getSkillState(neighbor) == SkillState.UNLOCKED);
	}

	public boolean hasAvailableSkill() {
		return config.skills()
				.values()
				.stream()
				.anyMatch(skill -> getSkillState(skill) == SkillState.AVAILABLE);
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
