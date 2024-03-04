package net.puffish.skillsmod.impl.json;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import net.puffish.skillsmod.api.json.JsonArrayWrapper;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.json.JsonListReader;
import net.puffish.skillsmod.api.json.JsonPath;
import net.puffish.skillsmod.api.utils.Result;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JsonArrayWrapperImpl extends JsonWrapperImpl implements JsonArrayWrapper {
	private final JsonArray json;

	public JsonArrayWrapperImpl(JsonArray json, JsonPath path) {
		super(path);
		this.json = json;
	}

	@Override
	public Stream<JsonElementWrapper> stream() {
		return Streams.mapWithIndex(
				json.asList().stream(),
				(jsonElement, i) -> new JsonElementWrapperImpl(jsonElement, path.thenArray(i))
		);
	}

	@Override
	public <S, F> Result<List<S>, List<F>> getAsList(JsonListReader<S, F> reader) {
		var exceptions = new ArrayList<F>();
		var list = new ArrayList<S>();

		var tmp = json.asList();
		for (int i = 0; i < tmp.size(); i++) {
			reader.apply(
					i,
					new JsonElementWrapperImpl(tmp.get(i), path.thenArray(i))
			).peek(
					list::add,
					exceptions::add
			);
		}

		if (exceptions.isEmpty()) {
			return Result.success(list);
		} else {
			return Result.failure(exceptions);
		}
	}

	@Override
	public int getSize() {
		return json.size();
	}

	@Override
	public JsonArray getJson() {
		return json;
	}
}
