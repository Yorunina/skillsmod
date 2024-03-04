package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.stat.StatType;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public class StatTypeCondition implements Operation<StatType<?>, Boolean> {

	private final RegistryEntryList<StatType<?>> statTypeEntries;

	private StatTypeCondition(RegistryEntryList<StatType<?>> statTypeEntries) {
		this.statTypeEntries = statTypeEntries;
	}

	public static void register() {
		BuiltinPrototypes.STAT_TYPE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				StatTypeCondition::parse
		);
	}

	public static Result<StatTypeCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(StatTypeCondition::parse);
	}

	public static Result<StatTypeCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optStatType = rootObject.get("stat")
				.andThen(JsonParseUtils::parseStatTypeOrStatTypeTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new StatTypeCondition(
					optStatType.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(StatType<?> statType) {
		return Optional.of(statTypeEntries.contains(Registries.STAT_TYPE.getEntry(statType)));
	}
}
