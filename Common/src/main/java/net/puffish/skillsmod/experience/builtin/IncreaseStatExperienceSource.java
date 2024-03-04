package net.puffish.skillsmod.experience.builtin;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stat;
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
import net.puffish.skillsmod.calculation.operation.builtin.StatCondition;

public class IncreaseStatExperienceSource implements ExperienceSource {
	private static final Identifier ID = SkillsMod.createIdentifier("increase_stat");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("stat"),
				BuiltinPrototypes.STAT,
				OperationFactory.create(Data::stat)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("amount"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(data -> (double) data.amount())
		);

		// Backwards compatibility.
		var legacy = new LegacyOperationRegistry<>(PROTOTYPE);
		legacy.registerBooleanFunction(
				"stat",
				StatCondition::parse,
				Data::stat
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
		legacy.registerNumberFunction(
				"amount",
				data -> (double) data.amount()
		);
	}

	private final Calculation<Data> calculation;

	private IncreaseStatExperienceSource(Calculation<Data> calculation) {
		this.calculation = calculation;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				IncreaseStatExperienceSource::parse
		);
	}

	private static Result<IncreaseStatExperienceSource, Failure> parse(ExperienceSourceConfigContext context) {
		return context.getData().andThen(rootElement ->
				LegacyCalculation.parse(rootElement, PROTOTYPE, context)
						.mapSuccess(IncreaseStatExperienceSource::new)
		);
	}

	private record Data(ServerPlayerEntity player, Stat<?> stat, int amount) { }

	public int getValue(ServerPlayerEntity player, Stat<?> stat, int amount) {
		return (int) Math.round(calculation.evaluate(
				new Data(player, stat, amount)
		));
	}

	@Override
	public void dispose(MinecraftServer server) {

	}
}
