package net.puffish.skillsmod.calculation.operation.builtin;

import net.minecraft.item.Item;
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

public final class ItemCondition implements Operation<Item, Boolean> {
	private final RegistryEntryList<Item> itemEntries;

	private ItemCondition(RegistryEntryList<Item> itemEntries) {
		this.itemEntries = itemEntries;
	}

	public static void register() {
		BuiltinPrototypes.ITEM.registerOperation(
				SkillsMod.createIdentifier("test"),
				BuiltinPrototypes.BOOLEAN,
				ItemCondition::parse
		);
	}

	public static Result<ItemCondition, Failure> parse(OperationConfigContext context) {
		return context.getData()
				.andThen(JsonElementWrapper::getAsObject)
				.andThen(ItemCondition::parse);
	}

	public static Result<ItemCondition, Failure> parse(JsonObjectWrapper rootObject) {
		var failures = new ArrayList<Failure>();

		var optItem = rootObject.get("item")
				.andThen(JsonParseUtils::parseItemOrItemTag)
				.ifFailure(failures::add)
				.getSuccess();

		if (failures.isEmpty()) {
			return Result.success(new ItemCondition(
					optItem.orElseThrow()
			));
		} else {
			return Result.failure(Failure.fromMany(failures));
		}
	}

	@Override
	public Optional<Boolean> apply(Item item) {
		return Optional.of(itemEntries.contains(Registries.ITEM.getEntry(item)));
	}
}
