package cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Base of AST Tree.
 */
public abstract class Node implements INode {
	private static final long serialVersionUID = -6090578442390659268L;

	@Nonnull
	private final String name;

	@Nonnull
	private final String uniqueName;

	@Nonnull
	private final String signature;

	@Nonnull
	private final Map<INode, Dependency> dependencyMap = new HashMap<>();

	@Nullable
	private INode parent;

	@Nonnull
	private List<INode> children = new ArrayList<>();

	protected Node(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		this.name = name;
		this.uniqueName = uniqueName;
		this.signature = signature;
	}

	private static Node getNode(INode iNode) {
		if (iNode instanceof Node) return (Node) iNode;
		throw new IllegalStateException("Unexpected foreign node in tree.");
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
	protected final <E extends INode> List<E> getChildrenList(final Class<E> aClass) {
		final List<INode> children = getChildren();
		final List<E> list = new ArrayList<>(children.size());
		for (final INode child : children) {
			if (aClass.isInstance(child)) {
				list.add(aClass.cast(child));
			}
		}
		return list;
	}

	@Nonnull
	@Override
	public final Map<INode, Dependency> getDependencies() {
		return Collections.unmodifiableMap(dependencyMap);
	}

	@Override
	public final void addDependencies(@Nonnull Map<INode, Dependency> dependencyMap) {
		if (dependencyMap.isEmpty()) return;
		if (this.dependencyMap.isEmpty()) {
			this.dependencyMap.putAll(dependencyMap);
			return;
		}

		for (final Map.Entry<INode, Dependency> entry : dependencyMap.entrySet()) {
			final INode node = entry.getKey();
			final Dependency dependency = entry.getValue();
			final Dependency oldDependency = this.dependencyMap.get(node);
			if (oldDependency != null) {
				oldDependency.setCount(oldDependency.getCount() + dependency.getCount());
			} else {
				final Dependency newDependency = new Dependency(dependency.getType());
				newDependency.setCount(dependency.getCount());
				this.dependencyMap.put(node, newDependency);
			}
		}
	}

	@Nonnull
	@Override
	public final Map<INode, Dependency> removeDependencies() {
		if (dependencyMap.isEmpty()) return Map.of();

		final Map<INode, Dependency> oldDependencyMap = Map.copyOf(dependencyMap);
		dependencyMap.clear();
		return oldDependencyMap;
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

	@Override
	public final boolean removeDependency(@Nonnull INode node) {
		return dependencyMap.remove(node) != null;
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
	public final boolean equalsDependencies(@Nonnull INode node) {
		return dependencyMap.equals(node.getDependencies());
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Node node = (Node) object;
		return name.equals(node.name) && uniqueName.equals(node.uniqueName)
				&& signature.equals(node.signature) && Objects.equals(parent, node.parent);
	}

	@Override
	public int hashCode() {
		int result = getClass().hashCode();
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

	private void writeObject(ObjectOutputStream out) throws IOException {
		if (this.getParent() == null && !(this instanceof IRoot)) {
			throw new IOException("Null parent!");
		}
		out.defaultWriteObject();
	}

	@Nonnull
	@Override
	public final String toTreeString() {
		final StringBuilder builder = new StringBuilder();
		internalToString(builder, 0);
		return builder.toString();
	}

	// Build tree to string internally by using a StringBuilder.
	private void internalToString(@Nonnull StringBuilder builder, int level) {
		final String alignString = "\t".repeat(level);
		if (children.isEmpty()) {
			builder.append(alignString).append("{ value: ")
					.append(this.toTreeElementString().replace("\n", "\n" + alignString)).append(" }");
		} else {
			builder.append(alignString).append("{ value: ")
					.append(this.toTreeElementString().replace("\n", "\n" + alignString)).append(", children: [\n");

			getNode(children.get(0)).internalToString(builder, level + 1);
			for (int i = 1; i < children.size(); i++) {
				builder.append(",\n");
				getNode(children.get(i)).internalToString(builder, level + 1);
			}

			builder.append('\n').append(alignString).append("]}");
		}
	}

	/**
	 * Return the root node.
	 *
	 * @return root node
	 */
	@Nonnull
	@Override
	public final INode getRoot() {
		return parent != null ? parent.getRoot() : this;
	}

	/**
	 * Check if this node is root node.
	 * Note: a node without parent is a root node.
	 *
	 * @return true if this node is root node
	 */
	@Override
	public final boolean isRoot() {
		return parent == null;
	}

	/**
	 * Get parent node, or null if there is none.
	 * Note: a node without parent is a root node.
	 *
	 * @return parent node
	 */
	@Nullable
	@Override
	public final INode getParent() {
		return parent;
	}

	// Set parent node, or null if there is none.
	// Note: a node without parent is a root node.
	private void internalSetParent(@Nullable INode parent) {
		this.parent = parent;
	}

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	@Override
	public final List<INode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Add child node to current node.
	 * Return false if child node already have parent node.
	 * Return true otherwise.
	 *
	 * @param child a child node to add
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean addChild(@Nonnull INode child) {
		// check if child node is root node
		if (!child.isRoot()) return false;

		children.add(child);
		getNode(child).internalSetParent(this);
		return true;
	}

	/**
	 * Remove a child node from current node.
	 * Return false if the child node doesn't belong to this node.
	 * Return true otherwise.
	 *
	 * @param child a child node to removeFromParent
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean removeChild(@Nonnull INode child) {
		// check if current node is not parent node
		if (child.getParent() != this) return false;

		children.remove(child);
		getNode(child).internalSetParent(null);
		return true;
	}

	/**
	 * Replace a child node by another node from current node.
	 * Return false if the old child node doesn't belong to this node, or new child node already have parent.
	 * Return true otherwise.
	 *
	 * @param oldChild a child node to remove
	 * @param newChild a child node to add
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean replaceChild(@Nonnull INode oldChild, @Nonnull INode newChild) {
		// check if current node is not parent node
		if (oldChild.getParent() != this) return false;
		// check if child node is root node
		if (!newChild.isRoot()) return false;

		final int index = children.indexOf(oldChild);
		children.set(index, newChild);
		getNode(oldChild).internalSetParent(null);
		getNode(newChild).internalSetParent(this);
		return true;
	}

	/**
	 * Add children nodes to current node.
	 * Return false if one of children nodes already have parent node.
	 * Return true otherwise.
	 *
	 * @param newChildren children nodes to add
	 * @return whether the operation is success or not
	 */
	@Override
	public final <E extends INode> boolean addChildren(@Nonnull List<E> newChildren) {
		if (newChildren.isEmpty()) return true;

		for (final INode child : newChildren) {
			if (!child.isRoot()) return false;
		}
		children.addAll(newChildren);
		for (final INode child : newChildren) {
			getNode(child).internalSetParent(this);
		}
		return true;
	}

	/**
	 * Remove children nodes from current node.
	 * Return children nodes.
	 *
	 * @return children nodes
	 */
	@Override
	public final List<INode> removeChildren() {
		if (children.isEmpty()) return List.of();

		final List<INode> oldChildren = List.copyOf(children);
		for (final INode child : oldChildren) child.removeFromParent();
		return oldChildren;
	}

	/**
	 * Add this node to the parent node.
	 * Return false if this node already have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean addToParent(@Nonnull INode parent) {
		return parent.addChild(this);
	}

	/**
	 * Remove this node itself from its parent node.
	 * Return false if this node doesn't have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean removeFromParent() {
		// if current node is root node
		if (this.parent == null) return false;

		return parent.removeChild(this);
	}

	/**
	 * Return this tree iterator
	 *
	 * @return the iterator
	 */
	@Nonnull
	@Override
	public final Iterator<INode> iterator() {
		return new Node.NodeIterator(this);
	}

	/**
	 * The tree iterator
	 */
	private static final class NodeIterator implements Iterator<INode> {
		private INode current;
		private Stack<Iterator<INode>> iterators = new Stack<>();

		private NodeIterator(INode treeNode) {
			this.iterators.push(getNode(treeNode).children.iterator());
		}

		@Override
		public final boolean hasNext() {
			if (current != null) {
				this.iterators.push(getNode(current).children.iterator());
				this.current = null;
			}
			do {
				if (iterators.peek().hasNext()) return true;
				this.iterators.pop();
			} while (!iterators.isEmpty());
			return false;
		}

		@Override
		public final INode next() {
			this.current = iterators.peek().next();
			return current;
		}

		@Override
		public final void remove() {
			if (current == null) throw new IllegalStateException();
			iterators.peek().remove();
			this.current = null;
		}
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

