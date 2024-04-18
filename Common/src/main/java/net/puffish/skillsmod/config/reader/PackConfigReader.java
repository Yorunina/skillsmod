package net.puffish.skillsmod.config.reader;

import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.SkillsAPI;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.util.PathUtils;
import net.puffish.skillsmod.api.util.Result;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class PackConfigReader extends ConfigReader {
	private final ResourceManager resourceManager;
	private final String namespace;

	public PackConfigReader(ResourceManager resourceManager, String namespace) {
		this.resourceManager = resourceManager;
		this.namespace = namespace;
	}

	public Result<JsonElement, Problem> readResource(Identifier id, Resource resource) {
		try (var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			return JsonElement.parseReader(reader, JsonPath.create(id.toString()));
		} catch (Exception e) {
			return Result.failure(Problem.message("Failed to read resource `" + id + "`"));
		}
	}

	@Override
	public Result<JsonElement, Problem> read(Path path) {
		var id = new Identifier(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		try {
			return readResource(id, resourceManager.getResource(id));
		} catch (FileNotFoundException e) {
			return Result.failure(Problem.message("Resource `" + id + "` does not exist"));
		} catch (IOException e) {
			return Result.failure(Problem.message("Failed to read resource `" + id + "`"));
		}
	}

	@Override
	public boolean exists(Path path) {
		var id = new Identifier(namespace, PathUtils.pathToString(Path.of(SkillsAPI.MOD_ID).resolve(path)));

		try {
			resourceManager.getResource(id);
			return true;
		} catch (IOException e) {
			return false;
		}
	}
}
