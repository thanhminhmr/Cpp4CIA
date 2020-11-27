package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;

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

}
