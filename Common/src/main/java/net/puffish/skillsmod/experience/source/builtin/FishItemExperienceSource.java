package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.calculation.Calculation;
import net.puffish.skillsmod.api.calculation.Variables;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.experience.source.ExperienceSource;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceConfigContext;
import net.puffish.skillsmod.api.experience.source.ExperienceSourceDisposeContext;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public class FishItemExperienceSource implements ExperienceSource {
	private static final Identifier ID = SkillsMod.createIdentifier("fish_item");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_rod_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::rod)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("get_fished_item_stack"),
				BuiltinPrototypes.ITEM_STACK,
				OperationFactory.create(Data::fished)
		);
	}

	private final Calculation<Data> calculation;

	private FishItemExperienceSource(Calculation<Data> calculation) {
		this.calculation = calculation;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				FishItemExperienceSource::parse
		);
	}

	private static Result<FishItemExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> rootObject.get("variables")
						.andThen(variablesElement -> Variables.parse(
								variablesElement,
								PROTOTYPE,
								context
						))
						.andThen(variables -> rootObject.get("experience")
								.andThen(experienceElement -> Calculation.parse(
										experienceElement,
										variables,
										context
								))
						)
						.mapSuccess(FishItemExperienceSource::new)
				);
	}

	private record Data(ServerPlayerEntity player, ItemStack rod, ItemStack fished) { }

	public int getValue(ServerPlayerEntity player, ItemStack rod, ItemStack fished) {
		return (int) Math.round(calculation.evaluate(
				new Data(player, rod, fished)
		));
	}

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}
