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
	private final String signature;

	@Nonnull
	private final Map<INode, Dependency> dependencyMap = new HashMap<>();

	protected Node(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		this.name = name;
		this.uniqueName = uniqueName;
		this.signature = signature;
	}

	protected static String objectToString(Object object) {
		return object != null ? String.format("%s@0x%08X", object.getClass().getSimpleName(), object.hashCode()) : "null";
	}

	protected static String mapToString(Map<?, ?> map) {
		final StringBuilder builder = new StringBuilder().append("[");
		for (final Map.Entry<?, ?> entry : map.entrySet()) {
			if (builder.length() > 1) builder.append(',');
			builder.append("\n\t").append(entry.getKey()).append(" = ").append(entry.getValue());
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
	}

	protected static String listToString(List<?> list) {
		final StringBuilder builder = new StringBuilder().append("[");
		for (final Object element : list) {
			if (builder.length() > 1) builder.append(',');
			builder.append("\n\t").append(element);
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
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
	public final String getSignature() {
		return signature;
	}

	protected final <E> List<INode> getChildrenList(final Class<E> aClass) {
		final List<ITreeNode> children = super.getChildren();
		final List<INode> list = new ArrayList<>(children.size());
		for (final ITreeNode child : children) {
			if (aClass.isInstance(child)) {
				list.add((INode) child);
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

	@Nonnull
	public final Dependency addDependency(@Nonnull INode node) {
		final Dependency oldDependency = dependencyMap.get(node);
		if (oldDependency != null) return oldDependency.incrementCount();
		final Dependency dependency = new Dependency();


		dependencyMap.put(node, dependency);
		return dependency;
	}

	@Nullable
	@Override
	public final Dependency replaceDependency(@Nonnull INode oldNode, @Nonnull INode newNode) {
		final Dependency oldDependency = dependencyMap.get(oldNode);
		if (oldDependency == null) return null;
		final Dependency newDependency = dependencyMap.get(newNode);
		if (newDependency != null) {
			oldDependency.setCount(newDependency.getCount() + oldDependency.getCount());
			dependencyMap.remove(newNode);
		} else {
			dependencyMap.put(newNode, oldDependency);
			dependencyMap.remove(oldNode);
		}
		return oldDependency;
	}

	@Override
	public final boolean removeDependency(@Nonnull INode node) {
		return dependencyMap.remove(node) != null;
	}

	@Nonnull
	@Override
	public String toString() {
		return "(" + objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\" }";
	}

	@Nonnull
	@Override
	public String toTreeElementString() {
		return "(" + objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", dependencyMap: " + mapToString(dependencyMap)
				+ " }";
	}

	protected static abstract class NodeBuilder<E extends INode, B extends INodeBuilder> implements INodeBuilder<E, B> {
		@Nullable
		protected String name;

		@Nullable
		protected String uniqueName;

		@Nullable
		protected String signature;

		protected NodeBuilder() {
		}

		@Override
		public boolean isValid() {
			return name != null && uniqueName != null && signature != null;
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
		public final String getSignature() {
			return signature;
		}

		@Override
		@Nonnull
		public final B setSignature(@Nonnull String content) {
			this.signature = content;
			//noinspection unchecked
			return (B) this;
		}
	}
}

