package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base of nay AST TreeNode. Do not use TreeNode class directly.
 */
public abstract class Node extends TreeNode implements INode {
	@Nonnull
	private final String name;

	@Nonnull
	private final String uniqueName;

	@Nonnull
	private final String content;

	@Nonnull
	private final Map<INode, Dependency> dependencyMap = new HashMap<>();

	protected Node(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String content) {
		this.name = name;
		this.uniqueName = uniqueName;
		this.content = content;
	}

	@Override
	@Nonnull
	public final String getName() {
		return name;
	}

	@Nonnull
	public final String getUniqueName() {
		return uniqueName;
	}

	@Nonnull
	public final String getContent() {
		return content;
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

	@Nonnull
	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") { name = \"" + name + "\", uniqueName = \"" + uniqueName + "\", content = \"" + content + "\" }";
	}

	protected static abstract class NodeBuilder<E extends INode, B extends INodeBuilder> implements INodeBuilder<E, B> {
		@Nullable
		protected String name;

		@Nullable
		protected String uniqueName;

		@Nullable
		protected String content;

		protected NodeBuilder() {
		}

		@Override
		public boolean isValid() {
			return name != null && uniqueName != null && content != null;
		}

		@Override
		@Nonnull
		public abstract E build();

		@Override
		@Nullable
		public final String getName() {
			return name;
		}

		@Override
		@Nonnull
		public final B setName(@Nonnull String name) {
			this.name = name;
			//noinspection unchecked
			return (B) this;
		}

		@Override
		@Nullable
		public final String getUniqueName() {
			return uniqueName;
		}

		@Override
		@Nonnull
		public final B setUniqueName(@Nonnull String uniqueName) {
			this.uniqueName = uniqueName;
			//noinspection unchecked
			return (B) this;
		}

		@Override
		@Nullable
		public final String getContent() {
			return content;
		}

		@Override
		@Nonnull
		public final B setContent(@Nonnull String content) {
			this.content = content;
			//noinspection unchecked
			return (B) this;
		}
	}
}

