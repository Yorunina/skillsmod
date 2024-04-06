package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.utils.ToastType;

public class ShowToastOutPacket extends OutPacket {
	public static ShowToastOutPacket write(ToastType type) {
		var packet = new ShowToastOutPacket();
		packet.buf.writeEnumConstant(type);
		return packet;
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SHOW_TOAST;
	}
}
