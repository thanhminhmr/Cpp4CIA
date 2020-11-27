package mrmathami.utils;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import java.io.Serializable;
import java.util.Arrays;

public interface IntsWrapper extends Comparable<IntsWrapper>, Serializable, Cloneable {
	static IntsWrapper of(@Nonnull int... array) {
		return new IntsWrapperImpl(array);
	}

	@Nonnull
	int[] getArray();
}

final class IntsWrapperImpl implements IntsWrapper {
	private static final long serialVersionUID = 5183650041772367023L;

	@Nonnull private int[] array;

	IntsWrapperImpl(@Nonnull int[] array) {
		this.array = array;
	}

	@Nonnull
	@Override
	public int[] getArray() {
		return array;
	}

	@Override
	public int compareTo(@Nonnull IntsWrapper wrapper) {
		return Arrays.compare(array, wrapper.getArray());
	}

	@Override
	public boolean equals(@Nullable Object object) {
		return this == object || object instanceof IntsWrapper
				&& Arrays.equals(array, ((IntsWrapper) object).getArray());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(array);
	}

	@Nonnull
	@Override
	public IntsWrapperImpl clone() throws CloneNotSupportedException {
		final IntsWrapperImpl wrapper = (IntsWrapperImpl) super.clone();
		wrapper.array = array.clone();
		return wrapper;
	}

	@Override
	public String toString() {
		return Arrays.toString(array);
	}
}