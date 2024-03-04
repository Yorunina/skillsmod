package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
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

public final class BlockCondition implements Operation<Block, Boolean> {
	private final RegistryEntryList<Block> blockEntries;

	private BlockCondition(RegistryEntryList<Block> blockEntries) {
		this.blockEntries = blockEntries;
	}

	public static void register() {
		BuiltinPrototypes.BLOCK.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				BlockCondition::parse
		);
	}

	public static Result<BlockCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(BlockCondition::parse);
	}

	public static Result<BlockCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optBlock = rootObject.get("block")
				.andThen(JsonParseUtils::parseBlockOrBlockTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new BlockCondition(
					optBlock.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(Block block) {
		return Optional.of(blockEntries.contains(Registries.BLOCK.getEntry(block)));
	}
}
