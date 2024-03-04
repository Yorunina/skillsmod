package net.puffish.skillsmod.api.rewards;

import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

public interface RewardFactory {
	Result<? extends Reward, Failure> create(RewardConfigContext context);
}
