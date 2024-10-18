package net.puffish.skillsmod.impl.calculation.prototype;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.util.LegacyUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class PrototypeImpl<T> implements Prototype<T> {

	private final Map<Identifier, Function<OperationConfigContext, Result<PrototypeView<T>, Problem>>> factories = new HashMap<>();

	private final Identifier id;

	public PrototypeImpl(Identifier id) {
		this.id = id;
	}

	private <R> Function<OperationConfigContext, Result<PrototypeView<T>, Problem>> createFunction(Identifier id, PrototypeView<R> view, OperationFactory<T, R> factory) {
		return context -> factory.apply(context).mapSuccess(o -> new PrototypeViewImpl<>(view, o));
	}

	@Override
	public <R> void registerOperation(Identifier id, PrototypeView<R> view, OperationFactory<T, R> factory) {
		register(id, createFunction(id, view, factory));
	}

	public <R> void registerLegacyOperation(Identifier id, PrototypeView<R> view, OperationFactory<T, R> factory) {
		register(id, new LegacyFunction<>(createFunction(id, view, factory)));
	}

	public void registerAlias(Identifier id, Identifier existingId) {
		register(id, new LegacyFunction<>(Objects.requireNonNull(factories.get(existingId))));
	}

	private void register(Identifier id, Function<OperationConfigContext, Result<PrototypeView<T>, Problem>> factory) {
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
	public Optional<Result<PrototypeView<T>, Problem>> getView(Identifier id, OperationConfigContext context) {
		var function = factories.get(id);
		if (function instanceof PrototypeImpl.LegacyFunction<T> && LegacyUtils.isRemoved(3, context)) {
			return Optional.empty();
		}
		return Optional.ofNullable(function).map(f -> f.apply(context));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <R> Optional<Operation<T, R>> getOperation(Prototype<R> prototype) {
		if (this == prototype) {
			return Optional.of(t -> Optional.of((R) t));
		}
		return Optional.empty();
	}

	private record LegacyFunction<T>(
			Function<OperationConfigContext, Result<PrototypeView<T>, Problem>> parent
	) implements Function<OperationConfigContext, Result<PrototypeView<T>, Problem>> {

		@Override
		public Result<PrototypeView<T>, Problem> apply(OperationConfigContext operationConfigContext) {
			return parent.apply(operationConfigContext);
		}

	}

}
