package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.NbtPredicate;
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

public final class ItemStackCondition implements Operation<ItemStack, Boolean> {
	private final Optional<RegistryEntryList<Item>> optItemEntries;
	private final Optional<NbtPredicate> optNbt;

	private ItemStackCondition(Optional<RegistryEntryList<Item>> optItemEntries, Optional<NbtPredicate> optNbt) {
		this.optItemEntries = optItemEntries;
		this.optNbt = optNbt;
	}

	public static void register() {
		BuiltinPrototypes.ITEM_STACK.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				ItemStackCondition::parse
		);
	}

	public static Result<ItemStackCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(ItemStackCondition::parse);
	}

	public static Result<ItemStackCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optItem = rootObject.get("item")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(itemElement -> JsonParseUtils.parseItemOrItemTag(itemElement)
						.ifFailure(failures::add)
						.getSuccess()
				);

		var optNbt = rootObject.get("nbt")
				.getSuccess() // ignore failure because this property is optional
				.flatMap(stateElement -> JsonParseUtils.parseNbtPredicate(stateElement)
						.ifFailure(failures::add)
						.getSuccess()
				);

		if (failures.isEmpty()) {
			return Result.success(new ItemStackCondition(
					optItem,
					optNbt
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(ItemStack itemStack) {
		return Optional.of(
				optItemEntries.map(itemStack::itemMatches).orElse(true)
						&& optNbt.map(nbt -> nbt.test(itemStack)).orElse(true)
		);
	}
}
