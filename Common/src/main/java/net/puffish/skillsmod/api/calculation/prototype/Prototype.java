package net.puffish.skillsmod.api.calculation.prototype;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.calculation.operation.OperationFactory;
import net.puffish.skillsmod.impl.calculation.prototype.PrototypeImpl;

public interface Prototype<T> extends PrototypeView<T> {

	static <T> Prototype<T> create(Identifier id) {
		return new PrototypeImpl<>(id);
	}

	<R> void registerOperation(Identifier id, PrototypeView<R> view, OperationFactory<T, R> factory);
}
