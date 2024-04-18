package net.puffish.skillsmod.impl.experience.source;

import net.minecraft.server.MinecraftServer;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public record ExperienceSourceConfigContextImpl(
		ConfigContext context,
		Result<JsonElement, Problem> maybeDataElement
) implements ExperienceSourceConfigContext {

	@Override
	public MinecraftServer getServer() {
		return context.getServer();
	}

	@Override
	public void emitWarning(String message) {
		context.emitWarning(message);
	}

	@Override
	public Result<JsonElement, Problem> getData() {
		return maybeDataElement;
	}
}
