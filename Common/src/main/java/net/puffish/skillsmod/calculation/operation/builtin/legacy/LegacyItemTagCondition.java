package net.puffish.skillsmod.calculation.operation.builtin.legacy;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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

public final class LegacyItemTagCondition implements Operation<ItemStack, Boolean> {
	private final RegistryEntryList<Item> entries;

	private LegacyItemTagCondition(RegistryEntryList<Item> entries) {
		this.entries = entries;
	}

	public static void register() {
		BuiltinPrototypes.ITEM_STACK.registerOperation(
				SkillsMod.createIdentifier("legacy_item_tag"),
				BuiltinPrototypes.BOOLEAN,
				LegacyItemTagCondition::parse
		);
	}

	public static Result<LegacyItemTagCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(LegacyItemTagCondition::parse);
	}

	public static Result<LegacyItemTagCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optTag = rootObject.get("tag")
				.andThen(JsonParseUtils::parseItemTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new LegacyItemTagCondition(
					optTag.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(ItemStack itemStack) {
		return Optional.of(itemStack.itemMatches(entries));
	}
}
