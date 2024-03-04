package net.puffish.skillsmod.api.calculation.operation;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonElementWrapper;
import net.puffish.skillsmod.api.utils.Failure;
import net.puffish.skillsmod.api.utils.Result;

public interface OperationConfigContext extends ConfigContext {
	Result<JsonElementWrapper, Failure> getData();
}
