package net.puffish.skillsmod.client.config;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.client.config.skill.ClientSkillConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillConnectionConfig;
import net.puffish.skillsmod.client.config.skill.ClientSkillDefinitionConfig;
import net.puffish.skillsmod.util.Bounds2i;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public record ClientCategoryConfig(
		Identifier id,
		Text title,
		ClientIconConfig icon,
		Identifier background,
		boolean exclusiveRoot,
		int spentPointsLimit,
		Map<String, ClientSkillDefinitionConfig> definitions,
		Map<String, ClientSkillConfig> skills,
		Collection<ClientSkillConnectionConfig> normalConnections,
		Map<String, Collection<String>> normalNeighbors,
		Map<String, Collection<String>> exclusiveNeighbors,
		Map<String, Collection<ClientSkillConnectionConfig>> exclusiveConnections,
		Map<String, Collection<String>> normalNeighborsReversed,
		Map<String, Collection<String>> exclusiveNeighborsReversed
) {
	public ClientCategoryConfig(
			Identifier id,
			Text title,
			ClientIconConfig icon,
			Identifier background,
			boolean exclusiveRoot,
			int spentPointsLimit,
			Map<String, ClientSkillDefinitionConfig> definitions,
			Map<String, ClientSkillConfig> skills,
			Collection<ClientSkillConnectionConfig> normalConnections,
			Collection<ClientSkillConnectionConfig> exclusiveConnections
	) {
		this(
			id,
			title,
			icon,
			background,
			exclusiveRoot,
			spentPointsLimit,
			definitions,
			skills,
			normalConnections,
			new HashMap<>(),
			new HashMap<>(),
			new HashMap<>(),
			new HashMap<>(),
			new HashMap<>()
		);

		for (var connection : normalConnections) {
			var a = connection.skillAId();
			var b = connection.skillBId();

			normalNeighbors.computeIfAbsent(a, key -> new HashSet<>()).add(b);
			normalNeighborsReversed.computeIfAbsent(b, key -> new HashSet<>()).add(a);
			if (connection.bidirectional()) {
				normalNeighbors.computeIfAbsent(b, key -> new HashSet<>()).add(a);
				normalNeighborsReversed.computeIfAbsent(a, key -> new HashSet<>()).add(b);
			}
		}

		for (var connection : exclusiveConnections) {
			var a = connection.skillAId();
			var b = connection.skillBId();

			exclusiveNeighbors.computeIfAbsent(a, key -> new HashSet<>()).add(b);
			exclusiveNeighborsReversed.computeIfAbsent(b, key -> new HashSet<>()).add(a);
			if (connection.bidirectional()) {
				exclusiveNeighbors.computeIfAbsent(b, key -> new HashSet<>()).add(a);
				exclusiveNeighborsReversed.computeIfAbsent(a, key -> new HashSet<>()).add(b);
			}

			this.exclusiveConnections.computeIfAbsent(a, key -> new HashSet<>()).add(connection);
			this.exclusiveConnections.computeIfAbsent(b, key -> new HashSet<>()).add(connection);
		}
	}

	public Bounds2i getBounds() {
		Bounds2i bounds = Bounds2i.zero();
		for (var skill : skills.values()) {
			bounds.extend(new Vector2i(skill.x(), skill.y()));
		}
		return bounds;
	}

	public Optional<ClientSkillDefinitionConfig> getDefinitionById(String id) {
		return Optional.ofNullable(definitions.get(id));
	}

}
