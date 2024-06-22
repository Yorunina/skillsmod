package net.puffish.skillsmod.client.setup;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.network.ClientPacketHandler;
import net.puffish.skillsmod.network.InPacket;

import java.util.function.Function;

public interface ClientRegistrar {
	<T extends InPacket> void registerInPacket(Identifier id, Function<RegistryByteBuf, T> reader, ClientPacketHandler<T> handler);
	void registerOutPacket(Identifier id);
}

