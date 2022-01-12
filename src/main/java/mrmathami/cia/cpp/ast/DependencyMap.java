package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.util.Arrays;

public final class DependencyMap {
	@Nonnull static final int[] DEPENDENCY_ZERO = new int[DependencyType.values.size()];
	@Nonnull static final DependencyMap ZERO = new DependencyMap(DEPENDENCY_ZERO);

	@Nonnull private final int[] dependencies;

	DependencyMap(@Nonnull int[] dependencies) {
		assert dependencies.length == DependencyType.values.size();
		this.dependencies = dependencies;
	}

	public int getCount(@Nonnull DependencyType type) {
		return dependencies[type.ordinal()];
	}

	@Nonnull
	int[] getDependencies() {
		return dependencies;
	}

	@Nonnull
	DependencyMap identity() {
		return this;
	}

	@Override
	public boolean equals(@Nullable Object object) {
		return this == object || object instanceof DependencyMap
				&& Arrays.equals(dependencies, ((DependencyMap) object).dependencies);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(dependencies);
	}
}
