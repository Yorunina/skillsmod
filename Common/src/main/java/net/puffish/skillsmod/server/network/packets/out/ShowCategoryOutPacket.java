package net.puffish.skillsmod.server.network.packets.out;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.common.FrameType;
import net.puffish.skillsmod.common.IconType;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.config.BackgroundConfig;
import net.puffish.skillsmod.config.CategoryConfig;
import net.puffish.skillsmod.config.FrameConfig;
import net.puffish.skillsmod.config.GeneralConfig;
import net.puffish.skillsmod.config.IconConfig;
import net.puffish.skillsmod.config.colors.ColorConfig;
import net.puffish.skillsmod.config.colors.ColorsConfig;
import net.puffish.skillsmod.config.colors.ConnectionsColorsConfig;
import net.puffish.skillsmod.config.colors.FillStrokeColorsConfig;
import net.puffish.skillsmod.config.skill.SkillConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionConfig;
import net.puffish.skillsmod.config.skill.SkillDefinitionsConfig;
import net.puffish.skillsmod.config.skill.SkillsConfig;
import net.puffish.skillsmod.network.OutPacket;
import net.puffish.skillsmod.network.Packets;
import net.puffish.skillsmod.server.data.CategoryData;

public record ShowCategoryOutPacket(CategoryConfig category, CategoryData categoryData) implements OutPacket {

	@Override
	public void write(RegistryByteBuf buf) {
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

	public void write(RegistryByteBuf buf, SkillDefinitionsConfig definitions) {
		buf.writeCollection(definitions.getAll(), (buf1, definition) -> write(buf, definition));
	}

	public void write(RegistryByteBuf buf, GeneralConfig general) {
		TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, general.getTitle());
		write(buf, general.getIcon());
		write(buf, general.getBackground());
		write(buf, general.getColors());
		buf.writeBoolean(general.isExclusiveRoot());
		buf.writeInt(general.getSpentPointsLimit());
	}

	public void write(RegistryByteBuf buf, SkillDefinitionConfig definition) {
		buf.writeString(definition.getId());
		TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, definition.getTitle());
		TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, definition.getDescription());
		TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC.encode(buf, definition.getExtraDescription());
		write(buf, definition.getFrame());
		write(buf, definition.getIcon());
		buf.writeFloat(definition.getSize());
		buf.writeInt(definition.getCost());
		buf.writeInt(definition.getRequiredSkills());
		buf.writeInt(definition.getRequiredPoints());
		buf.writeInt(definition.getRequiredSpentPoints());
	}

	public void write(PacketByteBuf buf, SkillsConfig skills) {
		buf.writeCollection(skills.getAll(), ShowCategoryOutPacket::write);
	}

	public void write(PacketByteBuf buf, SkillConnectionsConfig connections) {
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

	public static void write(RegistryByteBuf buf, IconConfig icon) {
		if (icon instanceof IconConfig.EffectIconConfig effectIcon) {
			buf.writeEnumConstant(IconType.EFFECT);
			StatusEffect.ENTRY_PACKET_CODEC.encode(buf, Registries.STATUS_EFFECT.getEntry(effectIcon.effect()));
		} else if (icon instanceof IconConfig.ItemIconConfig itemIcon) {
			buf.writeEnumConstant(IconType.ITEM);
			ItemStack.PACKET_CODEC.encode(buf, itemIcon.item());
		} else if (icon instanceof IconConfig.TextureIconConfig textureIcon) {
			buf.writeEnumConstant(IconType.TEXTURE);
			buf.writeIdentifier(textureIcon.texture());
		}
	}

	public static void write(PacketByteBuf buf, FrameConfig frame) {
		if (frame instanceof FrameConfig.AdvancementFrameConfig advancementFrame) {
			buf.writeEnumConstant(FrameType.ADVANCEMENT);
			buf.writeEnumConstant(advancementFrame.frame());
		} else if (frame instanceof FrameConfig.TextureFrameConfig textureFrame) {
			buf.writeEnumConstant(FrameType.TEXTURE);
			buf.writeOptional(textureFrame.lockedTexture(), PacketByteBuf::writeIdentifier);
			buf.writeIdentifier(textureFrame.availableTexture());
			buf.writeOptional(textureFrame.affordableTexture(), PacketByteBuf::writeIdentifier);
			buf.writeIdentifier(textureFrame.unlockedTexture());
			buf.writeOptional(textureFrame.excludedTexture(), PacketByteBuf::writeIdentifier);
		}
	}

	public static void write(PacketByteBuf buf, BackgroundConfig background) {
		buf.writeIdentifier(background.texture());
		buf.writeInt(background.width());
		buf.writeInt(background.height());
		buf.writeEnumConstant(background.position());
	}

	public static void write(PacketByteBuf buf, ColorsConfig colors) {
		write(buf, colors.connections());
		write(buf, colors.points());
	}

	public static void write(PacketByteBuf buf, ConnectionsColorsConfig connectionsColors) {
		write(buf, connectionsColors.locked());
		write(buf, connectionsColors.available());
		write(buf, connectionsColors.affordable());
		write(buf, connectionsColors.unlocked());
		write(buf, connectionsColors.excluded());
	}

	public static void write(PacketByteBuf buf, FillStrokeColorsConfig fillStrokeColors) {
		write(buf, fillStrokeColors.fill());
		write(buf, fillStrokeColors.stroke());
	}

	public static void write(PacketByteBuf buf, ColorConfig color) {
		buf.writeInt(color.argb());
	}

	@Override
	public Identifier getId() {
		return Packets.SHOW_CATEGORY;
	}
}
