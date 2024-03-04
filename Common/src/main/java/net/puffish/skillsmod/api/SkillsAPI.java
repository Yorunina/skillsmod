package net.puffish.skillsmod.api;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.api.experience.ExperienceSourceFactory;
import net.puffish.skillsmod.api.rewards.RewardFactory;
import net.puffish.skillsmod.experience.ExperienceSourceRegistry;
import net.puffish.skillsmod.impl.CategoryImpl;
import net.puffish.skillsmod.reward.RewardRegistry;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SkillsAPI {
	public static final String MOD_ID = "puffish_skills";

	public static void registerReward(Identifier key, RewardFactory factory) {
		RewardRegistry.register(key, factory);
	}

	public static void registerExperienceSource(Identifier key, ExperienceSourceFactory factory) {
		ExperienceSourceRegistry.register(key, factory);
	}

	public static void visitExperienceSources(ServerPlayerEntity player, Function<ExperienceSource, Integer> function) {
		SkillsMod.getInstance().visitExperienceSources(player, function);
	}

	public static void refreshReward(ServerPlayerEntity player, Identifier key) {
		SkillsMod.getInstance().refreshReward(player, key);
	}

	public static void openScreen(ServerPlayerEntity player) {
		SkillsMod.getInstance().openScreen(player, Optional.empty());
	}

	public static Optional<Category> getCategory(Identifier categoryId) {
		if (SkillsMod.getInstance().hasCategory(categoryId)) {
			return Optional.of(new CategoryImpl(categoryId));
		} else {
			return Optional.empty();
		}
	}

	public static List<Category> getCategories() {
		return SkillsMod.getInstance()
				.getCategories(false)
				.stream()
				.map(id -> (Category) new CategoryImpl(id))
				.toList();
	}

	public static List<Category> getUnlockedCategories(ServerPlayerEntity player) {
		return SkillsMod.getInstance()
				.getUnlockedCategories(player)
				.stream()
				.map(id -> (Category) new CategoryImpl(id))
				.toList();
	}
}
