package net.puffish.skillsmod.experience.builtin;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.ExperienceSource;
import net.puffish.skillsmod.api.experience.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;
import net.puffish.skillsmod.calculation.LegacyCalculation;
import net.puffish.skillsmod.calculation.operation.LegacyOperationRegistry;
import net.puffish.skillsmod.calculation.operation.builtin.AttributeOperation;
import net.puffish.skillsmod.calculation.operation.builtin.EffectOperation;
import net.puffish.skillsmod.calculation.operation.builtin.ItemStackCondition;
import net.puffish.skillsmod.calculation.operation.builtin.legacy.LegacyItemTagCondition;

public class CraftItemExperienceSource implements ExperienceSource {
	private static final Identifier ID = SkillsMod.createIdentifier("craft_item");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("crafted_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::itemStack)
		);

		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"item",
				ItemStackCondition::parse,
				Data::itemStack
		);
		legacy.registerBooleanFunction(
				"item_nbt",
				ItemStackCondition::parse,
				Data::itemStack
		);
		legacy.registerBooleanFunction(
				"item_tag",
				LegacyItemTagCondition::parse,
				Data::itemStack
		);
		legacy.registerNumberFunction(
				"player_effect",
				effect -> (double) (effect.getAmplifier() + 1),
				EffectOperation::parse,
				Data::player
		);
		legacy.registerNumberFunction(
				"player_attribute",
				EntityAttributeInstance::getValue,
				AttributeOperation::parse,
				Data::player
		);
	}

	private final Calculation<Data> calculation;

	private CraftItemExperienceSource(Calculation<Data> calculation) {
		this.calculation = calculation;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				CraftItemExperienceSource::parse
		);
	}

	private static Result<CraftItemExperienceSource, Failure> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(CraftItemExperienceSource::new)
		);
	}

	private record Data(ServerPlayerEntity player, ItemStack itemStack) { }

	public int getValue(ServerPlayerEntity player, ItemStack itemStack) {
		return (int) Math.round(calculation.evaluate(
				new Data(player, itemStack)
		));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
