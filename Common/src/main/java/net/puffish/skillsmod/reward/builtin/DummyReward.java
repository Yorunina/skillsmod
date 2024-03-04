package net.puffish.skillsmod.reward.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.rewards.Reward;
import net.puffish.skillsmod.api.rewards.RewardUpdateContext;

public class DummyReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("dummy");

	public DummyReward() {

	}

	@Override
	public void update(ServerPlayerEntity player, RewardUpdateContext context) {

	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
