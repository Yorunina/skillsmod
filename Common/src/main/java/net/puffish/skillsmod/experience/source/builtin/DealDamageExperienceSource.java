package net.puffish.skillsmod.experience.source.builtin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
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
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.experience.source.builtin.util.AntiFarmingPerEntity;

import java.util.ArrayList;
import java.util.Optional;

public class DealDamageExperienceSource implements ExperienceSource {
	private static final Identifier ID = SkillsMod.createIdentifier("deal_damage");
	private static final Prototype<Data> PROTOTYPE = Prototype.create(ID);

	static {
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("player"),
				BuiltinPrototypes.PLAYER,
				OperationFactory.create(Data::player)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("damaged_living_entity"),
				BuiltinPrototypes.LIVING_ENTITY,
				OperationFactory.create(Data::entity)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("damage_source"),
				BuiltinPrototypes.DAMAGE_SOURCE,
				OperationFactory.create(Data::damageSource)
		);
		PROTOTYPE.registerOperation(
				SkillsMod.createIdentifier("damage"),
				BuiltinPrototypes.NUMBER,
				OperationFactory.create(data -> (double) data.damage())
		);
	}

	private final Calculation<Data> calculation;
	private final Optional<AntiFarmingPerEntity> optAntiFarming;

	private DealDamageExperienceSource(Calculation<Data> calculation, Optional<AntiFarmingPerEntity> optAntiFarming) {
		this.calculation = calculation;
		this.optAntiFarming = optAntiFarming;
	}

	public static void register() {
		SkillsAPI.registerExperienceSource(
				ID,
				DealDamageExperienceSource::parse
		);
	}

	private static Result<DealDamageExperienceSource, Problem> parse(ExperienceSourceConfigContext context) {
		return context.getData()
				.andThen(JsonElement::getAsObject)
				.andThen(rootObject -> parse(rootObject, context));
	}

	private static Result<DealDamageExperienceSource, Problem> parse(JsonObject rootObject, ExperienceSourceConfigContext context) {
		var problems = new ArrayList<Problem>();

		var calculation = rootObject.get("variables")
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
				.ifFailure(problems::add)
				.getSuccess();

		var optAntiFarming = rootObject.get("anti_farming")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(element -> AntiFarmingPerEntity.parse(element)
						.ifFailure(problems::add)
						.getSuccess()
				);

		if (problems.isEmpty()) {
			return Result.success(new DealDamageExperienceSource(
					calculation.orElseThrow(),
					optAntiFarming
			));
		} else {
			return Result.failure(Problem.combine(problems));
		}
	}

	private record Data(ServerPlayerEntity player, LivingEntity entity, float damage, DamageSource damageSource) { }

	public int getValue(ServerPlayerEntity player, LivingEntity entity, float damage, DamageSource damageSource) {
		return (int) Math.round(calculation.evaluate(
				new Data(player, entity, damage, damageSource)
		));
	}

	public Optional<AntiFarmingPerEntity> getAntiFarming() {
		return optAntiFarming;
	}

	@Override
	public void dispose(ExperienceSourceDisposeContext context) {
		// Nothing to do.
	}
}
