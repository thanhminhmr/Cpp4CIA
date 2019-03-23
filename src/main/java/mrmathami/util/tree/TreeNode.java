package mrmathami.util.tree;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;

public final class TreeNode<Value> implements Iterable<TreeNode<Value>>, Serializable {
	@Nullable
	private final Value value;
	@Nullable
	private TreeNode<Value> parent;
	@Nonnull
	private List<TreeNode<Value>> children = new LinkedList<>();

	public TreeNode(@Nullable Value value) {
		this.value = value;
	}

	@Nonnull
	private static <Value> String internalValueToString(Value value) {
		if (value == null) return "null";
		return String.format("(%s@%08X) \"%s\"",
				value.getClass().getName(),
				System.identityHashCode(value),
				value.toString()
		);
	}

	/**
	 * Return the value.
	 *
	 * @return value
	 */
	@Nullable
	public final Value getValue() {
		return value;
	}

	/**
	 * Return the root node.
	 *
	 * @return root node
	 */
	@Nonnull
	public final TreeNode<Value> getRoot() {
		return parent != null ? parent.getRoot() : this;
	}

	/**
	 * Check if this node is root node.
	 * Note: a node without parent is a root node.
	 *
	 * @return true if this node is root node
	 */
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
	public final TreeNode<Value> getParent() {
		return parent;
	}

	// Set parent node, or null if there is none.
	// Note: a node without parent is a root node.
	private void internalSetParent(@Nullable TreeNode<Value> parent) {
		this.parent = parent;
	}

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	public final List<TreeNode<Value>> getChildren() {
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
	public final boolean addChild(@Nonnull TreeNode<Value> child) {
		// check if child node is root node
		if (!child.isRoot()) return false;

		children.add(child);
		child.internalSetParent(this);
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
	public final boolean removeChild(@Nonnull TreeNode<Value> child) {
		// check if current node is not parent node
		if (child.getParent() != this) return false;
		// none of above
		children.remove(child);
		child.internalSetParent(null);
		return true;
	}

	/**
	 * Add this node to the parent node.
	 * Return false if this node already have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	public final boolean addToParent(@Nonnull TreeNode<Value> parent) {
		return parent.addChild(this);
	}

	/**
	 * Remove this node itself from its parent node.
	 * Return false if this node doesn't have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 * @throws IllegalStateException if the encapsulated value is not yet set.
	 */
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
	public Iterator<TreeNode<Value>> iterator() {
		return new TreeIterator<>(this);
	}

	@Override
	public final String toString() {
		if (children.isEmpty()) return "{ value: " + value + " }";

		StringBuilder builder = new StringBuilder()
				.append("{ value: ").append(internalValueToString(value)).append(", children: [\n");

		children.get(0).internalToString(builder, 1);
		for (int i = 1; i < children.size(); i++) {
			builder.append(",\n");
			children.get(i).internalToString(builder, 1);
		}

		return builder.append("\n]}").toString();
	}

	// Build tree to string internally by using a StringBuilder.
	private void internalToString(StringBuilder builder, int level) {
		final String alignString = "\t".repeat(level);

		if (children.isEmpty()) {
			builder.append(alignString).append("{ value: ").append(internalValueToString(value)).append(" }");
		} else {
			builder.append(alignString).append("{ value: ").append(internalValueToString(value)).append(", children: [\n");

			children.get(0).internalToString(builder, level + 1);
			for (int i = 1; i < children.size(); i++) {
				builder.append(",\n");
				children.get(i).internalToString(builder, level + 1);
			}

			builder.append('\n')
					.append(alignString).append("]}");
		}
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final TreeNode<?> node = (TreeNode<?>) object;
		if (!Objects.equals(value, node.value)) return false;
		if (!Objects.equals(parent, node.parent)) return false;
		return children.equals(node.children);
	}

	/**
	 * The tree iterator
	 *
	 * @param <E>
	 */
	private static final class TreeIterator<E> implements Iterator<TreeNode<E>> {
		private TreeNode<E> current;
		private Stack<Iterator<TreeNode<E>>> iterators = new Stack<>();

		private TreeIterator(TreeNode<E> treeNode) {
			this.iterators.push(treeNode.children.iterator());
		}

		@Override
		public final boolean hasNext() {
			if (current != null) {
				this.iterators.push(current.children.iterator());
				this.current = null;
			}
			do {
				if (iterators.peek().hasNext()) return true;
				this.iterators.pop();
			} while (!iterators.isEmpty());
			return false;
		}

		@Override
		public final TreeNode<E> next() {
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
}
