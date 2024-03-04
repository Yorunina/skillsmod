package net.puffish.skillsmod.impl.rewards;

import net.puffish.skillsmod.api.rewards.RewardUpdateContext;

public record RewardUpdateContextImpl(int count, boolean recent) implements RewardUpdateContext {

	@Override
	public int getCount() {
		return count;
	}

	@Override
	public boolean isRecent() {
		return recent;
	}
}
