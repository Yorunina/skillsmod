package net.puffish.skillsmod.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;

public interface OutPacket {
	Identifier getId();

	void write(RegistryByteBuf buf);
}
