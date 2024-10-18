package net.puffish.skillsmod.util;

import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.util.Result;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class LegacyUtils {
	public static boolean isRemoved(int removalVersion, ConfigContext context) {
		return context instanceof VersionContext versionContext && versionContext.getVersion() >= removalVersion;
	}

	public static <S, F> Optional<S> deprecated(Supplier<? extends Result<S, F>> supplier, int removalVersion, ConfigContext context) {
		if (isRemoved(removalVersion, context)) {
			return Optional.empty();
		}
		return supplier.get().getSuccess();
	}

	public static <S, F> Function<F, Result<S, F>> wrapDeprecated(Supplier<? extends Result<S, F>> supplier, int removalVersion, ConfigContext context) {
		if (isRemoved(removalVersion, context)) {
			return Result::failure;
		}
		return f -> supplier.get().mapFailure(f2 -> f);
	}
}
