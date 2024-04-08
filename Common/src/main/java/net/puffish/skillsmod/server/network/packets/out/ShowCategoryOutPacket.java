package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.config.BackgroundConfig;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.server.data.CategoryData;
import net.puffish.skillsmod.skill.SkillConnection;

public class ShowCategoryOutPacket extends OutPacket {
	public static ShowCategoryOutPacket write(CategoryConfig category, CategoryData categoryData) {
		var packet = new ShowCategoryOutPacket();
		write(packet.buf, category, categoryData);
		return packet;
	}

	public static void write(PacketByteBuf buf, CategoryConfig category, CategoryData categoryData) {
		buf.writeIdentifier(category.getId());
		write(buf, category.getGeneral());
		write(buf, category.getDefinitions());
		write(buf, category.getSkills());
		write(buf, category.getConnections());
		buf.writeMap(
				category.getSkills().getMap(),
				PacketByteBuf::writeString,
				(buf1, skill) -> buf1.writeEnumConstant(
						categoryData.getSkillState(
								category,
								skill,
								category.getDefinitions().getById(skill.getDefinitionId()).orElseThrow()
						)
				)
		);
		buf.writeInt(categoryData.getSpentPoints(category));
		buf.writeInt(categoryData.getEarnedPoints(category));
		if (category.getExperience().isPresent()) {
			var level = categoryData.getCurrentLevel(category);
			buf.writeBoolean(true);
			buf.writeInt(level);
			buf.writeInt(categoryData.getCurrentExperience(category));
			buf.writeInt(categoryData.getRequiredExperience(category, level));
		} else {
			buf.writeBoolean(false);
		}
	}

	public static void write(PacketByteBuf buf, SkillDefinitionsConfig definitions) {
		buf.writeCollection(definitions.getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, GeneralConfig general) {
		buf.writeText(general.getTitle());
		write(buf, general.getIcon());
		write(buf, general.getBackground());
		buf.writeNullable(general.getColors(), (buf1, element) -> buf1.writeString(element.toString()));
		buf.writeBoolean(general.isExclusiveRoot());
		buf.writeInt(general.getSpentPointsLimit());
	}

	public static void write(PacketByteBuf buf, SkillDefinitionConfig definition) {
		buf.writeString(definition.getId());
		buf.writeText(definition.getTitle());
		buf.writeText(definition.getDescription());
		buf.writeText(definition.getExtraDescription());
		write(buf, definition.getFrame());
		write(buf, definition.getIcon());
		buf.writeFloat(definition.getSize());
		buf.writeInt(definition.getCost());
		buf.writeInt(definition.getRequiredSkills());
		buf.writeInt(definition.getRequiredPoints());
		buf.writeInt(definition.getRequiredSpentPoints());
	}

	public static void write(PacketByteBuf buf, SkillsConfig skills) {
		buf.writeCollection(skills.getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, SkillConnectionsConfig connections) {
		buf.writeCollection(connections.getNormal().getAll(), ShowCategoryOutPacket::write);
		buf.writeCollection(connections.getExclusive().getAll(), ShowCategoryOutPacket::write);
	}

	public static void write(PacketByteBuf buf, SkillConfig skill) {
		buf.writeString(skill.getId());
		buf.writeInt(skill.getX());
		buf.writeInt(skill.getY());
		buf.writeString(skill.getDefinitionId());
		buf.writeBoolean(skill.isRoot());
	}

	public static void write(PacketByteBuf buf, SkillConnection skill) {
		buf.writeString(skill.skillAId());
		buf.writeString(skill.skillBId());
		buf.writeBoolean(skill.bidirectional());
	}

	public static void write(PacketByteBuf buf, IconConfig icon) {
		buf.writeString(icon.getType());
		buf.writeNullable(icon.getData(), (buf1, element) -> buf1.writeString(element.toString()));
	}

	public static void write(PacketByteBuf buf, FrameConfig frame) {
		buf.writeString(frame.getType());
		buf.writeNullable(frame.getData(), (buf1, element) -> buf1.writeString(element.toString()));
	}

	public static void write(PacketByteBuf buf, BackgroundConfig background) {
		buf.writeNullable(background.getData(), (buf1, element) -> buf1.writeString(element.toString()));
	}

	@Override
	public Identifier getIdentifier() {
		return Packets.SHOW_CATEGORY;
	}
}
