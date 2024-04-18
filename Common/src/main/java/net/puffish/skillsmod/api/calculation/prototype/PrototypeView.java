package net.puffish.skillsmod.api.calculation.prototype;

import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.calculation.operation.Operation;
import net.puffish.skillsmod.api.calculation.operation.OperationConfigContext;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;

import java.util.Optional;

public interface PrototypeView<T> {
	Identifier getId();
	Optional<Result<PrototypeView<T>, Problem>> getView(Identifier id, OperationConfigContext context);
	<R> Optional<Operation<T, R>> getOperation(Prototype<R> prototype);
}
