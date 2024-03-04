package net.puffish.skillsmod.api.experience;

import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

public interface ExperienceSourceFactory {
	Result<? extends ExperienceSource, Failure> create(ExperienceSourceConfigContext context);
}
