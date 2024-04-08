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
import java.util.Optional;

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
			Optional<Identifier> lockedTexture,
			Identifier availableTexture,
			Optional<Identifier> affordableTexture,
			Identifier unlockedTexture,
			Optional<Identifier> excludedTexture
	) implements ClientFrameConfig {
		public static Result<TextureFrameConfig, Problem> parse(JsonElement rootElement) {
			return rootElement.getAsObject().andThen(TextureFrameConfig::parse);
		}

		private static Result<TextureFrameConfig, Problem> parse(JsonObject rootObject) {
			var problems = new ArrayList<Problem>();

			var optAffordableTexture = rootObject.get("affordable")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(element -> BuiltinJson.parseIdentifier(element)
							.ifFailure(problems::add)
							.getSuccess()
					);

			var optAvailableTexture = rootObject.get("available")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			var optLockedTexture = rootObject.get("locked")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(element -> BuiltinJson.parseIdentifier(element)
							.ifFailure(problems::add)
							.getSuccess()
					);

			var optUnlockedTexture = rootObject.get("unlocked")
					.andThen(BuiltinJson::parseIdentifier)
					.ifFailure(problems::add)
					.getSuccess();

			var optExcludedTexture = rootObject.get("excluded")
					.getSuccess() // ignore failure because this property is optional
					.flatMap(element -> BuiltinJson.parseIdentifier(element)
							.ifFailure(problems::add)
							.getSuccess()
					);

			if (problems.isEmpty()) {
				return Result.success(new TextureFrameConfig(
						optLockedTexture,
						optAvailableTexture.orElseThrow(),
						optAffordableTexture,
						optUnlockedTexture.orElseThrow(),
						optExcludedTexture
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}

		public static TextureFrameConfig createMissing() {
			return new TextureFrameConfig(
					Optional.of(TextureManager.MISSING_IDENTIFIER),
					TextureManager.MISSING_IDENTIFIER,
					Optional.of(TextureManager.MISSING_IDENTIFIER),
					TextureManager.MISSING_IDENTIFIER,
					Optional.of(TextureManager.MISSING_IDENTIFIER)
			);
		}
	}
}
