package net.puffish.skillsmod.client.data;

import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonObject;
import net.puffish.skillsmod.api.json.BuiltinJson;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.api.util.Problem;

import java.util.ArrayList;

public sealed interface ClientFrameData permits ClientFrameData.AdvancementFrameData, ClientFrameData.TextureFrameData {

	non-sealed class AdvancementFrameData implements ClientFrameData {
		private final AdvancementFrame frame;

		private AdvancementFrameData(AdvancementFrame frame) {
			this.frame = frame;
		}

		public static Result<AdvancementFrameData, Problem> parse(JsonElement rootElement) {
			return rootElement
					.getAsObject()
					.andThen(rootObject -> rootObject.get("frame"))
					.andThen(BuiltinJson::parseFrame)
					.mapSuccess(AdvancementFrameData::new);
		}

		public AdvancementFrame getFrame() {
			return frame;
		}
	}

	non-sealed class TextureFrameData implements ClientFrameData {
		private final Identifier availableTexture;
		private final Identifier unlockedTexture;
		private final Identifier lockedTexture;
		private final Identifier excludedTexture;

		public TextureFrameData(Identifier availableTexture, Identifier unlockedTexture, Identifier lockedTexture, Identifier excludedTexture) {
			this.availableTexture = availableTexture;
			this.unlockedTexture = unlockedTexture;
			this.lockedTexture = lockedTexture;
			this.excludedTexture = excludedTexture;
		}

		public static Result<TextureFrameData, Problem> parse(JsonElement rootElement) {
			return rootElement.getAsObject().andThen(TextureFrameData::parse);
		}

		private static Result<TextureFrameData, Problem> parse(JsonObject rootObject) {
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
				return Result.success(new TextureFrameData(
						optAvailableTexture.orElseThrow(),
						optUnlockedTexture.orElseThrow(),
						lockedTexture,
						excludedTexture
				));
			} else {
				return Result.failure(Problem.combine(problems));
			}
		}

		public static TextureFrameData createMissing() {
			return new TextureFrameData(
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER,
					TextureManager.MISSING_IDENTIFIER
			);
		}

		public Identifier getLockedTexture() {
			return lockedTexture;
		}

		public Identifier getAvailableTexture() {
			return availableTexture;
		}

		public Identifier getUnlockedTexture() {
			return unlockedTexture;
		}

		public Identifier getExcludedTexture() {
			return excludedTexture;
		}
	}
}
