package net.puffish.skillsmod.api.json;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;
import java.util.function.Supplier;

public final class BuiltinJson {
	private BuiltinJson() { }

	public static Result<Identifier, Problem> parseIdentifier(JsonElement element) {
		try {
			return Result.success(new Identifier(element.getJson().getAsString()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid identifier"));
		}
	}

	public static Result<String, Problem> parseIdentifierPath(JsonElement element) {
		try {
			return Result.success(new Identifier(Identifier.DEFAULT_NAMESPACE, element.getJson().getAsString()).getPath());
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid identifier path"));
		}
	}

	public static Result<StatusEffect, Problem> parseEffect(JsonElement element) {
		return parseSomething(Registries.STATUS_EFFECT, element, () -> "Expected valid effect");
	}

	public static Result<RegistryEntryList<StatusEffect>, Problem> parseEffectTag(JsonElement element) {
		return parseSomethingTag(Registries.STATUS_EFFECT, element, () -> "Expected valid effect tag");
	}

	public static Result<RegistryEntryList<StatusEffect>, Problem> parseEffectOrEffectTag(JsonElement element) {
		return parseSomethingOrSomethingTag(Registries.STATUS_EFFECT, element, () -> "Expected valid effect or effect tag");
	}

	public static Result<Block, Problem> parseBlock(JsonElement element) {
		return parseSomething(Registries.BLOCK, element, () -> "Expected valid block");
	}

	public static Result<RegistryEntryList<Block>, Problem> parseBlockTag(JsonElement element) {
		return parseSomethingTag(Registries.BLOCK, element, () -> "Expected valid block tag");
	}

	public static Result<RegistryEntryList<Block>, Problem> parseBlockOrBlockTag(JsonElement element) {
		return parseSomethingOrSomethingTag(Registries.BLOCK, element, () -> "Expected valid block or block tag");
	}

	public static Result<DamageType, Problem> parseDamageType(JsonElement element, DynamicRegistryManager manager) {
		return parseSomething(manager.get(RegistryKeys.DAMAGE_TYPE), element, () -> "Expected valid damage type");
	}

	public static Result<RegistryEntryList<DamageType>, Problem> parseDamageTypeTag(JsonElement element, DynamicRegistryManager manager) {
		return parseSomethingTag(manager.get(RegistryKeys.DAMAGE_TYPE), element, () -> "Expected valid damage type tag");
	}

	public static Result<RegistryEntryList<DamageType>, Problem> parseDamageTypeOrDamageTypeTag(JsonElement element, DynamicRegistryManager manager) {
		return parseSomethingOrSomethingTag(manager.get(RegistryKeys.DAMAGE_TYPE), element, () -> "Expected valid damage type or damage type tag");
	}

	public static Result<EntityType<?>, Problem> parseEntityType(JsonElement element) {
		return parseSomething(Registries.ENTITY_TYPE, element, () -> "Expected valid entity type");
	}

	public static Result<RegistryEntryList<EntityType<?>>, Problem> parseEntityTypeTag(JsonElement element) {
		return parseSomethingTag(Registries.ENTITY_TYPE, element, () -> "Expected valid entity type tag");
	}

	public static Result<RegistryEntryList<EntityType<?>>, Problem> parseEntityTypeOrEntityTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(Registries.ENTITY_TYPE, element, () -> "Expected valid entity type or entity type tag");
	}

	public static Result<Item, Problem> parseItem(JsonElement element) {
		return parseSomething(Registries.ITEM, element, () -> "Expected valid item");
	}

	public static Result<RegistryEntryList<Item>, Problem> parseItemTag(JsonElement element) {
		return parseSomethingTag(Registries.ITEM, element, () -> "Expected valid item tag");
	}

	public static Result<RegistryEntryList<Item>, Problem> parseItemOrItemTag(JsonElement element) {
		return parseSomethingOrSomethingTag(Registries.ITEM, element, () -> "Expected valid item or item tag");
	}

	public static Result<StatType<?>, Problem> parseStatType(JsonElement element) {
		return parseSomething(Registries.STAT_TYPE, element, () -> "Expected valid stat type");
	}

	public static Result<RegistryEntryList<StatType<?>>, Problem> parseStatTypeTag(JsonElement element) {
		return parseSomethingTag(Registries.STAT_TYPE, element, () -> "Expected valid stat type tag");
	}

	public static Result<RegistryEntryList<StatType<?>>, Problem> parseStatTypeOrStatTypeTag(JsonElement element) {
		return parseSomethingOrSomethingTag(Registries.STAT_TYPE, element, () -> "Expected valid stat type or stat type tag");
	}

	public static Result<StatePredicate, Problem> parseStatePredicate(JsonElement element) {
		try {
			return Result.success(StatePredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid state predicate"));
		}
	}

	public static Result<NbtPredicate, Problem> parseNbtPredicate(JsonElement element) {
		try {
			return Result.success(NbtPredicate.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid state predicate"));
		}
	}

	public static Result<Stat<?>, Problem> parseStat(JsonElement element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> getOrCreateStat(Registries.STAT_TYPE.getOrEmpty(
					Identifier.splitOn(id.getNamespace(), '.')
			).orElseThrow(), Identifier.splitOn(id.getPath(), '.')));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid stat"));
		}
	}

	private static <T> Stat<T> getOrCreateStat(StatType<T> statType, Identifier id) {
		return statType.getOrCreateStat(statType.getRegistry().getOrEmpty(id).orElseThrow());
	}

	public static Result<NbtCompound, Problem> parseNbt(JsonElement element) {
		try {
			return Result.success(StringNbtReader.parse(element.getJson().getAsString()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid nbt"));
		}
	}

	public static Result<ItemStack, Problem> parseItemStack(JsonElement element) {
		try {
			return element.getAsObject().andThen(object -> {
				var problems = new ArrayList<Problem>();

				var item = object.get("item")
						.andThen(BuiltinJson::parseItem)
						.ifFailure(problems::add)
						.getSuccess();

				var nbt = object.get("nbt")
						.getSuccess()
						.flatMap(nbtElement -> BuiltinJson.parseNbt(nbtElement)
								.ifFailure(problems::add)
								.getSuccess()
						);

				if (problems.isEmpty()) {
					var itemStack = new ItemStack(item.orElseThrow());
					nbt.ifPresent(itemStack::setNbt);
					return Result.success(itemStack);
				} else {
					return Result.failure(Problem.combine(problems));
				}
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid item stack"));
		}
	}

	public static Result<AdvancementFrame, Problem> parseFrame(JsonElement element) {
		try {
			return element.getAsString().andThen(name -> Result.success(AdvancementFrame.forName(name)));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid frame"));
		}
	}

	public static Result<Text, Problem> parseText(JsonElement element) {
		try {
			return Result.success(Text.Serializer.fromJson(element.getJson()));
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid text"));
		}
	}

	public static Result<EntityAttribute, Problem> parseAttribute(JsonElement element) {
		try {
			return parseIdentifier(element).mapSuccess(id -> {
				// Backwards compatibility.
				if (id.getNamespace().equals(SkillsAPI.MOD_ID)) {
					id = new Identifier("puffish_attributes", id.getPath());
				}
				return Registries.ATTRIBUTE.getOrEmpty(id).orElseThrow();
			});
		} catch (Exception e) {
			return Result.failure(element.getPath().createProblem("Expected valid attribute"));
		}
	}

	public static Result<EntityAttributeModifier.Operation, Problem> parseAttributeOperation(JsonElement element) {
		return element.getAsString().andThen(string -> switch (string) {
			case "addition" -> Result.success(EntityAttributeModifier.Operation.ADDITION);
			case "multiply_base" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_BASE);
			case "multiply_total" -> Result.success(EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
			default -> Result.failure(element.getPath().createProblem("Expected valid attribute operation"));
		});
	}

	private static <T> Result<T, Problem> parseSomething(Registry<T> registry, JsonElement element, Supplier<String> message) {
		try {
			var string = element.getJson().getAsString();
			return Result.success(registry.getOrEmpty(new Identifier(string)).orElseThrow());
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem(message.get()));
		}
	}

	private static <T> Result<RegistryEntryList<T>, Problem> parseSomethingTag(Registry<T> registry, JsonElement element, Supplier<String> message) {
		try {
			var string = element.getJson().getAsString();
			if (string.startsWith("#")) {
				string = string.substring(1);
			}
			return Result.success(registry.getReadOnlyWrapper().getOptional(TagKey.of(registry.getKey(), new Identifier(string))).orElseThrow());
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem(message.get()));
		}
	}

	private static <T> Result<RegistryEntryList<T>, Problem> parseSomethingOrSomethingTag(Registry<T> registry, JsonElement element, Supplier<String> message) {
		try {
			var string = element.getJson().getAsString();
			if (string.startsWith("#")) {
				return Result.success(registry.getReadOnlyWrapper().getOptional(TagKey.of(registry.getKey(), new Identifier(string.substring(1)))).orElseThrow());
			} else {
				return Result.success(RegistryEntryList.of(registry.getEntry(RegistryKey.of(registry.getKey(), new Identifier(string))).orElseThrow()));
			}
		} catch (Exception ignored) {
			return Result.failure(element.getPath().createProblem(message.get()));
		}
	}
}
