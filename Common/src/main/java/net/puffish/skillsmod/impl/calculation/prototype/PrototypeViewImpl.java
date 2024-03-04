package net.puffish.skillsmod.impl.calculation.prototype;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.calculation.prototype.Prototype;
import net.puffish.skillsmod.api.calculation.prototype.PrototypeView;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

import java.util.Optional;

public class PrototypeViewImpl<T, R> implements PrototypeView<R> {
	private final PrototypeView<T> parent;
	private final Operation<R, T> operation;

	public PrototypeViewImpl(PrototypeView<T> parent, Operation<R, T> operation) {
		this.parent = parent;
		this.operation = operation;
	}

	@Override
	public Identifier getId() {
		return parent.getId();
	}

	@Override
	public Optional<Result<PrototypeView<R>, Failure>> getView(Identifier id, OperationConfigContext context) {
		return parent.getView(id, context).map(r -> r.mapSuccess(v -> new PrototypeViewImpl<>(v, operation)));
	}

	@Override
	public <V> Optional<Operation<R, V>> getOperation(Prototype<V> prototype) {
		return parent.getOperation(prototype).map(f -> r -> operation.apply(r).flatMap(f));
	}
}
