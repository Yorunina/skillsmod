package net.puffish.skillsmod.client.network.packets.in;

import net.minecraft.network.PacketByteBuf;
import net.puffish.skillsmod.api.Skill;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.client.config.ClientBackgroundConfig;
import net.puffish.skillsmod.client.config.ClientCategoryConfig;
import net.puffish.skillsmod.client.config.ClientFrameConfig;
import net.puffish.skillsmod.client.config.ClientIconConfig;
import net.puffish.skillsmod.client.config.colors.ClientColorsConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.client.data.ClientCategoryData;
import net.puffish.skillsmod.network.InPacket;

import java.util.Optional;
import java.util.stream.Collectors;

public class ShowCategoryInPacket implements InPacket {
	private final ClientCategoryData category;

	private ShowCategoryInPacket(ClientCategoryData category) {
		this.category = category;
	}

	public static ShowCategoryInPacket read(PacketByteBuf buf) {
		var category = readCategory(buf);

		return new ShowCategoryInPacket(category);
	}

	public static ClientCategoryData readCategory(PacketByteBuf buf) {
		var id = buf.readIdentifier();

		var title = buf.readText();
		var icon = readSkillIcon(buf);
		var background = readBackground(buf);
		var colors = readColors(buf);
		var exclusiveRoot = buf.readBoolean();
		var spentPointsLimit = buf.readInt();

		var definitions = buf.readList(ShowCategoryInPacket::readDefinition)
				.stream()
				.collect(Collectors.toMap(ClientSkillDefinitionConfig::id, definition -> definition));

		var skills = buf.readList(ShowCategoryInPacket::readSkill)
				.stream()
				.collect(Collectors.toMap(ClientSkillConfig::id, skill -> skill));

		var normalConnections = buf.readList(ShowCategoryInPacket::readSkillConnection);
		var exclusiveConnections = buf.readList(ShowCategoryInPacket::readSkillConnection);

		var skillsStates = buf.readMap(
				PacketByteBuf::readString,
				buf1 -> buf1.readEnumConstant(Skill.State.class)
		);

		var spentPoints = buf.readInt();
		var earnedPoints = buf.readInt();

		var currentLevel = Integer.MIN_VALUE;
		var currentExperience = Integer.MIN_VALUE;
		var requiredExperience = Integer.MIN_VALUE;
		if (buf.readBoolean()) {
			currentLevel = buf.readInt();
			currentExperience = buf.readInt();
			requiredExperience = buf.readInt();
		}

		var category = new ClientCategoryConfig(
				id,
				title,
				icon,
				background,
				colors,
				exclusiveRoot,
				spentPointsLimit,
				definitions,
				skills,
				normalConnections,
				exclusiveConnections
		);

		return new ClientCategoryData(
				category,
				skillsStates,
				spentPoints,
				earnedPoints,
				currentLevel,
				currentExperience,
				requiredExperience
		);
	}

	public static ClientSkillDefinitionConfig readDefinition(PacketByteBuf buf) {
		var id = buf.readString();
		var title = buf.readText();
		var description = buf.readText();
		var extraDescription = buf.readText();
		var frame = readFrameIcon(buf);
		var icon = readSkillIcon(buf);
		var size = buf.readFloat();
		var cost = buf.readInt();
		var requiredSkills = buf.readInt();
		var requiredPoints = buf.readInt();
		var requiredSpentPoints = buf.readInt();

		return new ClientSkillDefinitionConfig(
				id,
				title,
				description,
				extraDescription,
				icon,
				frame,
				size,
				cost,
				requiredSkills,
				requiredPoints,
				requiredSpentPoints
		);
	}

	public static ClientIconConfig readSkillIcon(PacketByteBuf buf) {
		var type = buf.readString();
		return buf.readOptional(PacketByteBuf::readString)
				.flatMap(data -> JsonElement.parseString(data, JsonPath.create("Client Skill Icon")).getSuccess())
				.flatMap(rootElement -> switch (type) {
					case "item" -> ClientIconConfig.ItemIconConfig.parse(rootElement).getSuccess();
					case "effect" -> ClientIconConfig.EffectIconConfig.parse(rootElement).getSuccess();
					case "texture" -> ClientIconConfig.TextureIconConfig.parse(rootElement).getSuccess();
					default -> Optional.empty();
				}).orElseGet(ClientIconConfig.TextureIconConfig::createMissing);
	}

	public static ClientFrameConfig readFrameIcon(PacketByteBuf buf) {
		var type = buf.readString();
		return buf.readOptional(PacketByteBuf::readString)
				.flatMap(data -> JsonElement.parseString(data, JsonPath.create("Client Frame Icon")).getSuccess())
				.flatMap(rootElement -> switch (type) {
					case "advancement" -> ClientFrameConfig.AdvancementFrameConfig.parse(rootElement).getSuccess();
					case "texture" -> ClientFrameConfig.TextureFrameConfig.parse(rootElement).getSuccess();
					default -> Optional.empty();
				}).orElseGet(ClientFrameConfig.TextureFrameConfig::createMissing);
	}

	public static ClientBackgroundConfig readBackground(PacketByteBuf buf) {
		return buf.readOptional(PacketByteBuf::readString)
				.flatMap(data -> JsonElement.parseString(data, JsonPath.create("Client Background")).getSuccess())
				.flatMap(rootElement -> ClientBackgroundConfig.parse(rootElement).getSuccess())
				.orElseGet(ClientBackgroundConfig::createMissing);
	}

	public static ClientColorsConfig readColors(PacketByteBuf buf) {
		return buf.readOptional(PacketByteBuf::readString)
				.flatMap(data -> JsonElement.parseString(data, JsonPath.create("Client Colors")).getSuccess())
				.flatMap(rootElement -> ClientColorsConfig.parse(rootElement).getSuccess())
				.orElseGet(ClientColorsConfig::createDefault);
	}

	public static ClientSkillConfig readSkill(PacketByteBuf buf) {
		var id = buf.readString();
		var x = buf.readInt();
		var y = buf.readInt();
		var definition = buf.readString();
		var isRoot = buf.readBoolean();

		return new ClientSkillConfig(id, x, y, definition, isRoot);
	}

	public static ClientSkillConnectionConfig readSkillConnection(PacketByteBuf buf) {
		var skillAId = buf.readString();
		var skillBId = buf.readString();
		var bidirectional = buf.readBoolean();

		return new ClientSkillConnectionConfig(skillAId, skillBId, bidirectional);
	}

	public ClientCategoryData getCategory() {
		return category;
	}
}
