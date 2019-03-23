package mrmathami.util;

import javax.annotation.Nonnull;

public interface ShallowClonable<E> {
	@Nonnull
	E shallowClone();
}
