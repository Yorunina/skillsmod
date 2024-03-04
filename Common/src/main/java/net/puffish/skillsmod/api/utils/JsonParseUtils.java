package net.puffish.skillsmod.api.utils;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.JsonElementWrapper;

import java.util.ArrayList;
import java.util.function.Supplier;

public class JsonParseUtils {
	public static Result<Identifier, Failure> parseIdentifier(JsonElementWrapper element) {
		try {
			return Result.success(new Identifier(element.getJson().getAsString()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid identifier"));
		}
	}

	public static Result<String, Failure> parseIdentifierPath(JsonElementWrapper element) {
		try {
			return Result.success(new Identifier(Identifier.DEFAULT_NAMESPACE, element.getJson().getAsString()).getPath());
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid identifier path"));
		}
	}

	public static Result<StatusEffect, Failure> parseEffect(JsonElementWrapper element) {
		return parseSomething(Registries.STATUS_EFFECT, element, () -> "Expected valid effect");
	}

	public static Result<RegistryEntryList<StatusEffect>, Failure> parseEffectTag(JsonElementWrapper element) {
		return parseSomethingTag(Registries.STATUS_EFFECT, element, () -> "Expected valid effect tag");
	}

	public static Result<RegistryEntryList<StatusEffect>, Failure> parseEffectOrEffectTag(JsonElementWrapper element) {
		return parseSomethingOrSomethingTag(Registries.STATUS_EFFECT, element, () -> "Expected valid effect or effect tag");
	}

	public static Result<Block, Failure> parseBlock(JsonElementWrapper element) {
		return parseSomething(Registries.BLOCK, element, () -> "Expected valid block");
	}

	public static Result<RegistryEntryList<Block>, Failure> parseBlockTag(JsonElementWrapper element) {
		return parseSomethingTag(Registries.BLOCK, element, () -> "Expected valid block tag");
	}

	public static Result<RegistryEntryList<Block>, Failure> parseBlockOrBlockTag(JsonElementWrapper element) {
		return parseSomethingOrSomethingTag(Registries.BLOCK, element, () -> "Expected valid block or block tag");
	}

	public static Result<EntityType<?>, Failure> parseEntityType(JsonElementWrapper element) {
		return parseSomething(Registries.ENTITY_TYPE, element, () -> "Expected valid entity type");
	}

	public static Result<RegistryEntryList<EntityType<?>>, Failure> parseEntityTypeTag(JsonElementWrapper element) {
		return parseSomethingTag(Registries.ENTITY_TYPE, element, () -> "Expected valid entity type tag");
	}

	public static Result<RegistryEntryList<EntityType<?>>, Failure> parseEntityTypeOrEntityTypeTag(JsonElementWrapper element) {
		return parseSomethingOrSomethingTag(Registries.ENTITY_TYPE, element, () -> "Expected valid entity type or entity type tag");
	}

	public static Result<Item, Failure> parseItem(JsonElementWrapper element) {
		return parseSomething(Registries.ITEM, element, () -> "Expected valid item");
	}

	public static Result<RegistryEntryList<Item>, Failure> parseItemTag(JsonElementWrapper element) {
		return parseSomethingTag(Registries.ITEM, element, () -> "Expected valid item tag");
	}

	public static Result<RegistryEntryList<Item>, Failure> parseItemOrItemTag(JsonElementWrapper element) {
		return parseSomethingOrSomethingTag(Registries.ITEM, element, () -> "Expected valid item or item tag");
	}

	public static Result<StatType<?>, Failure> parseStatType(JsonElementWrapper element) {
		return parseSomething(Registries.STAT_TYPE, element, () -> "Expected valid stat type");
	}

	public static Result<RegistryEntryList<StatType<?>>, Failure> parseStatTypeTag(JsonElementWrapper element) {
		return parseSomethingTag(Registries.STAT_TYPE, element, () -> "Expected valid stat type tag");
	}

	public static Result<RegistryEntryList<StatType<?>>, Failure> parseStatTypeOrStatTypeTag(JsonElementWrapper element) {
		return parseSomethingOrSomethingTag(Registries.STAT_TYPE, element, () -> "Expected valid stat type or stat type tag");
	}

	public static Result<StatePredicate, Failure> parseStatePredicate(JsonElementWrapper element) {
		try {
			return Result.success(StatePredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid state predicate"));
		}
	}

	public static Result<NbtPredicate, Failure> parseNbtPredicate(JsonElementWrapper element) {
		try {
			return Result.success(NbtPredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid state predicate"));
		}
	}

	public static Result<Stat<?>, Failure> parseStat(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> getOrCreateStat(Registries.STAT_TYPE.getOrEmpty(
					Identifier.splitOn(id.getNamespace(), '.')
			).orElseThrow(), Identifier.splitOn(id.getPath(), '.')));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid stat"));
		}
	}

	private static <T> Stat<T> getOrCreateStat(StatType<T> statType, Identifier id) {
		return statType.getOrCreateStat(statType.getRegistry().getOrEmpty(id).orElseThrow());
	}

	public static Result<NbtCompound, Failure> parseNbt(JsonElementWrapper element) {
		try {
			return Result.success(StringNbtReader.parse(element.getJson().getAsString()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid nbt"));
		}
	}

	public static Result<ItemStack, Failure> parseItemStack(JsonElementWrapper element) {
		try {
			return element.getAsObject().andThen(object -> {
				var failures = new ArrayList<Failure>();

				var item = object.get("item")
						.andThen(JsonParseUtils::parseItem)
						.ifFailure(failures::add)
						.getSuccess();

				var nbt = object.get("nbt")
						.getSuccess()
						.flatMap(nbtElement -> JsonParseUtils.parseNbt(nbtElement)
								.ifFailure(failures::add)
								.getSuccess()
						);

				if (failures.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					nbt.ifPresent(itemStack::setNbt);
					return Result.success(itemStack);
				} else {
					return Result.failure(Failure.fromMany(failures));
				}
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid item stack"));
		}
	}

	public static Result<AdvancementFrame, Failure> parseFrame(JsonElementWrapper element) {
		try {
			return element.getAsString().andThen(name -> Result.success(AdvancementFrame.forName(name)));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid frame"));
		}
	}

	public static Result<Text, Failure> parseText(JsonElementWrapper element) {
		try {
			return Result.success(Text.Serializer.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid text"));
		}
	}

	public static Result<EntityAttribute, Failure> parseAttribute(JsonElementWrapper element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> {
				// Backwards compatibility.
				if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
					id = new Identifier("puffish_attributes", id.getPath());
				}
				return Registries.ATTRIBUTE.getOrEmpty(id).orElseThrow();
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().createFailure("Expected valid attribute"));
		}
	}

	public static Result<EntityAttributeModifier.Operation, Failure> parseAttributeOperation(JsonElementWrapper element) {
		return element.getAsString().andThen(string -> switch (string) {
			case "addition" -> Result.success(EntityAttributeModifier.Operation.ADDITION);
			case "multiply_base" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_BASE);
			case "multiply_total" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
			default -> Result.failure(element.getPath().createFailure("Expected valid attribute operation"));
		});
	}

	private static <T> Result<T, Failure> parseSomething(Registry<T> registry, JsonElementWrapper element, Supplier<String> message) {
		try {
			var string = element.getJson().getAsString();
			return Result.success(registry.getOrEmpty(new Identifier(string)).orElseThrow());
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createFailure(message.get()));
		}
	}

	private static <T> Result<RegistryEntryList<T>, Failure> parseSomethingTag(Registry<T> registry, JsonElementWrapper element, Supplier<String> message) {
		try {
			var string = element.getJson().getAsString();
			if (string.startsWith("#")) {
				string = string.substring(1);
			}
			return Result.success(registry.getReadOnlyWrapper().getOptional(TagKey.of(registry.getKey(), new Identifier(string))).orElseThrow());
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createFailure(message.get()));
		}
	}

	private static <T> Result<RegistryEntryList<T>, Failure> parseSomethingOrSomethingTag(Registry<T> registry, JsonElementWrapper element, Supplier<String> message) {
		try {
			var string = element.getJson().getAsString();
			if (string.startsWith("#")) {
				return Result.success(registry.getReadOnlyWrapper().getOptional(TagKey.of(registry.getKey(), new Identifier(string.substring(1)))).orElseThrow());
			} else {
				return Result.success(RegistryEntryList.of(registry.getEntry(RegistryKey.of(registry.getKey(), new Identifier(string))).orElseThrow()));
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createFailure(message.get()));
		}
	}
}
