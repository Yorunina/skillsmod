package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;

public record PointsUpdateOutPacket(Identifier categoryId, int spentPoints, int earnedPoints, boolean announceNewPoints) implements OutPacket {
	@Override
	public void write(RegistryByteBuf buf) {
		buf.writeIdentifier(categoryId);
		buf.writeInt(spentPoints);
		buf.writeInt(earnedPoints);
		buf.writeBoolean(announceNewPoints);
	}

	@Override
	public Identifier getId() {
		return Packets.POINTS_UPDATE;
	}
}
