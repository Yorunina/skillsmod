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

public class ScoreboardReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("scoreboard");

	private final String objectiveName;

	private ScoreboardReward(String objectiveName) {
		this.objectiveName = objectiveName;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				ScoreboardReward::parse
		);
	}

	private static Result<ScoreboardReward, Failure> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(ScoreboardReward::parse);
	}

	private static Result<ScoreboardReward, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optScoreboard = rootObject.getString("scoreboard")
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new ScoreboardReward(
					optScoreboard.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public void update(ServerPlayerEntity player, RewardUpdateContext context) {
		var scoreboard = player.getScoreboard();
		var objective = scoreboard.getObjective(objectiveName);
		if (objective != null) {
			scoreboard.getPlayerScore(player.getEntityName(), objective).setScore(context.getCount());
		}
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
