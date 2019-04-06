package cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Base of nay AST TreeNode. Do not use TreeNode class directly.
 */
public abstract class Node extends TreeNode implements INode {
	private static final long serialVersionUID = 2977623392166713480L;

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

	@Nonnull
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
	public final Map<INode, Dependency> getDependencies() {
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
			newDependency.setCount(newDependency.getCount() + oldDependency.getCount());
			dependencyMap.remove(oldNode);
			return newDependency;
		} else {
			dependencyMap.put(newNode, oldDependency);
			dependencyMap.remove(oldNode);
			return oldDependency;
		}
	}

	@Override
	public final boolean removeDependency(@Nonnull INode node) {
		return dependencyMap.remove(node) != null;
	}

	@Override
	public void addDependencies(Map<INode, Dependency> newDependencyMap) {
		if (newDependencyMap.isEmpty()) return;

		for (final Map.Entry<INode, Dependency> entry : newDependencyMap.entrySet()) {
			final INode node = entry.getKey();
			final Dependency dependency = entry.getValue();
			final Dependency oldDependency = dependencyMap.get(node);
			if (oldDependency != null) {
				oldDependency.setCount(oldDependency.getCount() + dependency.getCount());
			} else {
				final Dependency newDependency = new Dependency(dependency.getType());
				newDependency.setCount(dependency.getCount());
				dependencyMap.put(node, newDependency);
			}
		}
	}

	@Nonnull
	@Override
	public Map<INode, Dependency> removeDependencies() {
		if (dependencyMap.isEmpty()) return Map.of();

		final Map<INode, Dependency> oldDependencyMap = Map.copyOf(dependencyMap);
		dependencyMap.clear();
		return oldDependencyMap;
	}

	@Override
	public boolean equals(Object object) {
		if (!super.equals(object)) return false;
		final Node node = (Node) object;
		return name.equals(node.name) && uniqueName.equals(node.uniqueName)
				&& signature.equals(node.signature);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		//noinspection ConstantConditions
		result = 31 * result + (name != null ? name.hashCode() : 0);
		//noinspection ConstantConditions
		result = 31 * result + (uniqueName != null ? uniqueName.hashCode() : 0);
		//noinspection ConstantConditions
		result = 31 * result + (signature != null ? signature.hashCode() : 0);
		return result;
	}

	@Nonnull
	@Override
	public String toString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\" }";
	}

	@Nonnull
	@Override
	public String toTreeElementString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", dependencyMap: " + Utilities.mapToString(dependencyMap)
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

