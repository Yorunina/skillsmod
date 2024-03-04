package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.stat.Stat;
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
import java.util.Objects;
import java.util.Optional;

public class StatCondition implements Operation<Stat<?>, Boolean> {

	private final Stat<?> stat;

	private StatCondition(Stat<?> stat) {
		this.stat = stat;
	}

	public static void register() {
		BuiltinPrototypes.STAT.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				StatCondition::parse
		);
	}

	public static Result<StatCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(StatCondition::parse);
	}

	public static Result<StatCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optStat = rootObject.get("stat")
				.andThen(JsonParseUtils::parseStat)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new StatCondition(
					optStat.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(Stat<?> stat) {
		return Optional.of(Objects.equals(this.stat, stat));
	}
}
