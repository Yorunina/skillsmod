package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Optional;
import java.util.stream.Stream;

public interface Category {
	Identifier getId();

	Optional<Experience> getExperience();

	Optional<Skill> getSkill(String skillId);

	Stream<Skill> streamSkills();

	Stream<Skill> streamUnlockedSkills(ServerPlayerEntity player);

	void openScreen(ServerPlayerEntity player);

	void resetSkills(ServerPlayerEntity player);

	void unlock(ServerPlayerEntity player);

	void lock(ServerPlayerEntity player);

	boolean isUnlocked(ServerPlayerEntity player);

	void erase(ServerPlayerEntity player);

	int getExtraPoints(ServerPlayerEntity player);

	void setExtraPoints(ServerPlayerEntity player, int count);

	void addExtraPoints(ServerPlayerEntity player, int count);

	int getPointsLeft(ServerPlayerEntity player);
}
