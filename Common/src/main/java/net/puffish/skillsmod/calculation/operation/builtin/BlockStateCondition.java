package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.entry.RegistryEntryList;
import net.puffish.skillsmod.SkillsMod;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.BuiltinPrototypes;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonObjectWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.JsonParseUtils;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.Optional;

public final class BlockStateCondition implements Operation<BlockState, Boolean> {
	private final Optional<RegistryEntryList<Block>> optBlockEntries;
	private final Optional<StatePredicate> optState;

	private BlockStateCondition(Optional<RegistryEntryList<Block>> optBlockEntries, Optional<StatePredicate> optState) {
		this.optBlockEntries = optBlockEntries;
		this.optState = optState;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK_STATE.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				BlockStateCondition::parse
		);
	}

	public static Result<BlockStateCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(BlockStateCondition::parse);
	}

	public static Result<BlockStateCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optBlock = rootObject.get("block")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(idElement -> JsonParseUtils.parseBlockOrBlockTag(idElement)
						.ifFailure(failures::add)
						.getSuccess()
				);

		var optState = rootObject.get("state")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(stateElement -> JsonParseUtils.parseStatePredicate(stateElement)
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return Result.success(new BlockStateCondition(
					optBlock,
					optState
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(BlockState blockState) {
		return Optional.of(
				optBlockEntries.map(blockState::isIn).orElse(true)
						&& optState.map(state -> state.test(blockState)).orElse(true)
		);
	}
}
