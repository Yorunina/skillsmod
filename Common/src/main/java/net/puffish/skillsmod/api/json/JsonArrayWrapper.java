package net.puffish.skillsmod.api.json;

import com.google.gson.JsonArray;
import net.puffish.skillsmod.api.utils.Result;

import java.util.List;
import java.util.stream.Stream;

public interface JsonArrayWrapper extends JsonWrapper {
	Stream<JsonElementWrapper> stream();

	<S, F> Result<List<S>, List<F>> getAsList(JsonListReader<S, F> reader);

	int getSize();

	JsonArray getJson();
}
