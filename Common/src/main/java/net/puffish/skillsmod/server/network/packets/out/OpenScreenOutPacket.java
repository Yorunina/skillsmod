package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

import java.util.Optional;

public class OpenScreenOutPacket extends OutPacket {
	public static OpenScreenOutPacket write(Optional<Identifier> category) {
		var packet = new OpenScreenOutPacket();
		packet.buf.writeOptional(category, PacketByteBuf::writeIdentifier);
		return packet;
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.OPEN_SCREEN;
	}
}
