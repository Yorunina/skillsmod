package net.puffish.skillsmod.reward.builtin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.reward.Reward;
import net.puffish.skillsmod.api.reward.RewardConfigContext;
import net.puffish.skillsmod.api.reward.RewardDisposeContext;
import net.puffish.skillsmod.api.reward.RewardUpdateContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttributeReward implements Reward {
	public static final Identifier ID = SkillsMod.createIdentifier("attribute");

	private final List<Identifier> ids = new ArrayList<>();

	private final RegistryEntry<EntityAttribute> attribute;
	private final float value;
	private final EntityAttributeModifier.Operation operation;

	private AttributeReward(RegistryEntry<EntityAttribute> attribute, float value, EntityAttributeModifier.Operation operation) {
		this.attribute = attribute;
		this.value = value;
		this.operation = operation;
	}

	public static void register() {
		SkillsAPI.registerReward(
				ID,
				AttributeReward::parse
		);
	}

	private static Result<AttributeReward, Problem> parse(RewardConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(AttributeReward::parse);
	}

	private static Result<AttributeReward, Problem> parse(JsonObject rootObject) {
		var problems = new ArrayList<Problem>();

		var optAttribute = rootObject.get("attribute")
				.andThen(attributeElement -> BuiltinJson.parseAttribute(attributeElement)
						.andThen(attribute -> {
							var attributeEntry = Registries.ATTRIBUTE.getEntry(attribute);
							if (DefaultAttributeRegistry.get(EntityType.PLAYER).has(attributeEntry)) {
								return Result.success(attributeEntry);
							} else {
								return Result.failure(attributeElement.getPath().createProblem("Expected a valid player attribute"));
							}
						})
				)
				.ifFailure(problems::add)
				.getSuccess();

		var optValue = rootObject.getFloat("value")
				.ifFailure(problems::add)
				.getSuccess();

		var optOperation = rootObject.get("operation")
				.andThen(BuiltinJson::parseAttributeOperation)
				.ifFailure(problems::add)
				.getSuccess();

		if (problems.isEmpty()) {
			return Result.success(new AttributeReward(
					optAttribute.orElseThrow(),
					optValue.orElseThrow(),
					optOperation.orElseThrow()
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	@Override
	public void update(RewardUpdateContext context) {
		var count = context.getCount();
		var instance = Objects.requireNonNull(context.getPlayer().getAttributeInstance(attribute));

		while (ids.size() < count) {
			ids.add(SkillsMod.createIdentifier(
					RandomStringUtils.random(16, "abcdefghijklmnopqrstuvwxyz0123456789")
			));
		}

		for (var i = 0; i < ids.size(); i++) {
			var id = ids.get(i);
			if (instance.getModifier(id) == null) {
				if (i < count) {
					instance.addTemporaryModifier(new EntityAttributeModifier(
							id,
							value,
							operation
					));
				}
			} else {
				if (i >= count) {
					instance.removeModifier(id);
				}
			}
		}
	}

	@Override
	public void dispose(RewardDisposeContext context) {
		for (var player : context.getServer().getPlayerManager().getPlayerList()) {
			var instance = Objects.requireNonNull(player.getAttributeInstance(attribute));
			for (var id : ids) {
				instance.removeModifier(id);
			}
		}
		ids.clear();
	}
}
