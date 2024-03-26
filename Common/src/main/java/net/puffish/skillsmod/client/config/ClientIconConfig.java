package net.puffish.skillsmod.client.config;

import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

public sealed interface ClientIconConfig permits ClientIconConfig.EffectIconConfig, ClientIconConfig.ItemIconConfig, ClientIconConfig.TextureIconConfig {

	record ItemIconConfig(ItemStack item) implements ClientIconConfig {
		public static Result<ItemIconConfig, Problem> parse(JsonElement rootElement) {
			return BuiltinJson.parseItemStack(rootElement).mapSuccess(ItemIconConfig::new);
		}
	}

	record EffectIconConfig(StatusEffect effect) implements ClientIconConfig {
		public static Result<EffectIconConfig, Problem> parse(JsonElement rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("effect"))
					.andThen(BuiltinJson::parseEffect)
					.mapSuccess(EffectIconConfig::new);
		}
	}

	record TextureIconConfig(Identifier texture) implements ClientIconConfig {
		public static Result<TextureIconConfig, Problem> parse(JsonElement rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("texture"))
					.andThen(BuiltinJson::parseIdentifier)
					.mapSuccess(TextureIconConfig::new);
		}

		public static TextureIconConfig createMissing() {
			return new TextureIconConfig(TextureManager.MISSING_IDENTIFIER);
		}
	}
}
