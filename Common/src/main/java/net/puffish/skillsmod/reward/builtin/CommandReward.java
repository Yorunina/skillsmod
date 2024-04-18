package net.puffish.skillsmod.reward.builtin;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CommandReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("command");

	private final Map<UUID, Integer> counts = new HashMap<>();

	private final String command;
	private final String unlockCommand;
	private final String lockCommand;

	private CommandReward(String command, String unlockCommand, String lockCommand) {
		this.command = command;
		this.unlockCommand = unlockCommand;
		this.lockCommand = lockCommand;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				CommandReward::parse
		);
	}

	private static Result<CommandReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(CommandReward::parse);
	}

	private static Result<CommandReward, Problem> parse(JsonObject rootObject) {
		var command = rootObject.getString("command")
				.getSuccess()
				.orElse("");

		var unlockCommand = rootObject.getString("unlock_command")
				.getSuccess()
				.orElse("");

		var lockCommand = rootObject.getString("lock_command")
				.getSuccess()
				.orElse("");

		return Result.success(new CommandReward(
				command,
				unlockCommand,
				lockCommand
		));
	}

	private void executeCommand(ServerPlayerEntity player, String command) {
		if (command.isBlank()) {
			return;
		}

		var server = Objects.requireNonNull(player.getServer());

		server.getCommandManager().executeWithPrefix(
				player.getCommandSource()
						.withSilent()
						.withLevel(server.getFunctionPermissionLevel()),
				command
		);
	}

	@Override
	public void update(RewardUpdateContext context) {
		var player = context.getPlayer();

		if (context.isAction()) {
			executeCommand(player, command);
		}

		counts.compute(player.getUuid(), (uuid, count) -> {
			if (count == null) {
				count = 0;
			}

			while (context.getCount() > count) {
				executeCommand(player, unlockCommand);
				count++;
			}
			while (context.getCount() < count) {
				executeCommand(player, lockCommand);
				count--;
			}

			return count;
		});
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		for (var entry : counts.entrySet()) {
			var player = context.getServer().getPlayerManager().getPlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			for (var i = 0; i < entry.getValue(); i++) {
				executeCommand(player, lockCommand);
			}
		}
		counts.clear();
	}
}
