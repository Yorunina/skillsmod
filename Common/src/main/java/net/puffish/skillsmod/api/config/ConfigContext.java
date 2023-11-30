package net.puffish.skillsmod.api.config;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.DynamicRegistryManager;

public interface ConfigContext {

	MinecraftServer getServer();
	DynamicRegistryManager getDynamicRegistryManager();
	ResourceManager getResourceManager();

	void addWarning(String message);
}
