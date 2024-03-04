package net.puffish.skillsmod.impl.calculation.prototype;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class PrototypeImpl<T> implements Prototype<T> {

	private final Map<Identifier, Function<OperationConfigContext, Result<PrototypeView<T>, Failure>>> factories = new HashMap<>();

	private final Identifier id;

	public PrototypeImpl(Identifier id) {
		this.id = id;
	}

	@Override
	public <R> void registerOperation(Identifier id, PrototypeView<R> view, OperationFactory<T, R> factory) {
		register(id, context -> factory.apply(context).mapSuccess(o -> new PrototypeViewImpl<>(view, o)));
	}

	private void register(Identifier id, Function<OperationConfigContext, Result<PrototypeView<T>, Failure>> factory) {
		factories.compute(id, (key, old) -> {
			if (old == null) {
				return factory;
			}
			throw new IllegalStateException("Trying to add duplicate key `" + key + "` to registry");
		});
	}

	@Override
	public Identifier getId() {
		return id;
	}

	@Override
	public Optional<Result<PrototypeView<T>, Failure>> getView(Identifier id, OperationConfigContext context) {
		return Optional.ofNullable(factories.get(id)).map(f -> f.apply(context));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> Optional<Operation<T, R>> getOperation(Prototype<R> prototype) {
		if (this == prototype) {
			return Optional.of(t -> Optional.of((R) t));
		}
		return Optional.empty();
	}
}
