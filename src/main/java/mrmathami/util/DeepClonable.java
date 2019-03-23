package mrmathami.util;

import javax.annotation.Nonnull;

public interface DeepClonable<E> {
	@Nonnull
	E deepClone();
}
