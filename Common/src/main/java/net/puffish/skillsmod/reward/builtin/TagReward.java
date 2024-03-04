package net.puffish.skillsmod.reward.builtin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.rewards.Reward;
import net.puffish.skillsmod.api.rewards.RewardConfigContext;
import net.puffish.skillsmod.api.rewards.RewardUpdateContext;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;

public class TagReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("tag");

	private final String tag;

	private TagReward(String tag) {
		this.tag = tag;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				TagReward::parse
		);
	}

	private static Result<TagReward, Failure> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(TagReward::parse);
	}

	private static Result<TagReward, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.getString("tag")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new TagReward(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public void update(ServerPlayerEntity player, RewardUpdateContext context) {
		if (context.getCount() > 0) {
			player.addScoreboardTag(tag);
		} else {
			player.removeScoreboardTag(tag);
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
