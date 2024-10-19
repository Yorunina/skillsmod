package net.puffish.skillsmod.client.config;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.common.BackgroundPosition;

public record ClientBackgroundConfig(
		Identifier texture,
		int width,
		int height,
		BackgroundPosition position
) { }
