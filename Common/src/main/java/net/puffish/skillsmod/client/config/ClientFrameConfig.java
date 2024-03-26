package net.puffish.skillsmod.client.config;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.ArrayList;

public sealed interface ClientFrameConfig permits ClientFrameConfig.AdvancementFrameConfig, ClientFrameConfig.TextureFrameConfig {

	record AdvancementFrameConfig(AdvancementFrame frame) implements ClientFrameConfig {
		public static Result<AdvancementFrameConfig, Problem> parse(JsonElement rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("frame"))
					.andThen(BuiltinJson::parseFrame)
					.mapSuccess(AdvancementFrameConfig::new);
		}
	}

	record TextureFrameConfig(
			Identifier availableTexture,
			Identifier unlockedTexture,
			Identifier lockedTexture,
			Identifier excludedTexture
	) implements ClientFrameConfig {
		public static Result<TextureFrameConfig, Problem> parse(JsonElement rootElement) {
			return rootElement.getAsObject().andThen(TextureFrameConfig::parse);
		}

		private static Result<TextureFrameConfig, Problem> parse(JsonObject rootObject) {
			var problems = new ArrayList<Problem>();

			var optAvailableTexture = rootObject.get("available")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			var optUnlockedTexture = rootObject.get("unlocked")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			var lockedTexture = rootObject.get("locked")
					.andThen(BuiltinJson::parseIdentifier)
					.getSuccess()
					.orElse(null);

			var excludedTexture = rootObject.get("excluded")
					.andThen(BuiltinJson::parseIdentifier)
					.getSuccess()
					.orElse(null);

			if (problems.isEmpty()) {
				return Result.success(new TextureFrameConfig(
						optAvailableTexture.orElseThrow(),
						optUnlockedTexture.orElseThrow(),
						lockedTexture,
						excludedTexture
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}

		public static TextureFrameConfig createMissing() {
			return new TextureFrameConfig(
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER
			);
		}
	}
}
