package cia.cpp.ast;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base of nay AST TreeNode. Do not use TreeNode class directly.
 */
abstract class Node extends TreeNode implements INode {
	@Nonnull
	private final String name;

	@Nonnull
	private final Map<INode, Dependency> dependencyMap = new HashMap<>();

	protected Node(@Nonnull String name) {
		this.name = name;
	}

	@Override
	@Nonnull
	public final String getName() {
		return name;
	}

	protected final <E> List<E> getChildrenList(final Class<E> aClass) {
		final List<ITreeNode> children = super.getChildren();
		final List<E> list = new ArrayList<>(children.size());
		for (final ITreeNode child : children) {
			if (aClass.isInstance(child)) {
				list.add(aClass.cast(child));
			}
		}
		return list;
	}

	@Nonnull
	@Override
	public final Map<INode, Dependency> getDependencyMap() {
		return Collections.unmodifiableMap(dependencyMap);
	}

	@Nullable
	@Override
	public final Dependency getDependency(@Nonnull INode node) {
		return dependencyMap.get(node);
	}

	@Nullable
	@Override
	public final Dependency createDependency(@Nonnull INode node, @Nonnull Dependency.Type type) {
		if (dependencyMap.containsKey(node)) return null;
		final Dependency dependency = new Dependency(type);
		dependencyMap.put(node, dependency);
		return dependency;
	}

	@Override
	public final boolean removeDependency(@Nonnull INode node) {
		return dependencyMap.remove(node) != null;
	}

	protected static abstract class NodeBuilder<E extends Node, B extends NodeBuilder> {
		@Nullable
		protected String name;

		protected NodeBuilder() {
		}

		@Nonnull
		public abstract E build();

		@Nullable
		public final String getName() {
			return name;
		}

		@Nonnull
		public final B setName(@Nonnull String name) {
			this.name = name;
			//noinspection unchecked
			return (B) this;
		}
	}
}

