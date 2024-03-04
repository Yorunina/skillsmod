package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.entry.RegistryEntryList;
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

public final class LegacyBlockTagCondition implements Operation<BlockState, Boolean> {
	private final RegistryEntryList<Block> entries;

	private LegacyBlockTagCondition(RegistryEntryList<Block> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK_STATE.registerOperation(
				SkillsMod.createIdentifier("legacy_block_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyBlockTagCondition::parse
		);
	}

	public static Result<LegacyBlockTagCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(LegacyBlockTagCondition::parse);
	}

	public static Result<LegacyBlockTagCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseBlockTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new LegacyBlockTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(BlockState blockState) {
		return Optional.of(blockState.isIn(entries));
	}
}
