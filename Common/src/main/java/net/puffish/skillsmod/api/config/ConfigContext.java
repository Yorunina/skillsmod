package net.puffish.skillsmod.api.config;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;

public interface ConfigContext {
	MinecraftServer getServer();

	void emitWarning(String message);
}
