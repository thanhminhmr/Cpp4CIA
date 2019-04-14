package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

abstract class TreeNode implements ITreeNode {
	private static final long serialVersionUID = 478909744504392013L;

	@Nullable
	private ITreeNode parent;

	@Nonnull
	private List<ITreeNode> children = new ArrayList<>();

	protected TreeNode() {
	}

	private static TreeNode getTreeNode(ITreeNode iTreeNode) {
		if (iTreeNode instanceof TreeNode) return (TreeNode) iTreeNode;
		throw new IllegalStateException("Unexpected foreign node in tree.");
	}

	@Nonnull
	@Override
	public abstract String toString();

	@Nonnull
	@Override
	public abstract String toTreeElementString();

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

			getTreeNode(children.get(0)).internalToString(builder, level + 1);
			for (int i = 1; i < children.size(); i++) {
				builder.append(",\n");
				getTreeNode(children.get(i)).internalToString(builder, level + 1);
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
	public final ITreeNode getRoot() {
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
	public final ITreeNode getParent() {
		return parent;
	}

	// Set parent node, or null if there is none.
	// Note: a node without parent is a root node.
	private void internalSetParent(@Nullable ITreeNode parent) {
		this.parent = parent;
	}

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	@Override
	public final List<ITreeNode> getChildren() {
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
	public final boolean addChild(@Nonnull ITreeNode child) {
		// check if child node is root node
		if (!child.isRoot()) return false;

		children.add(child);
		getTreeNode(child).internalSetParent(this);
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
	public final boolean removeChild(@Nonnull ITreeNode child) {
		// check if current node is not parent node
		if (child.getParent() != this) return false;

		children.remove(child);
		getTreeNode(child).internalSetParent(null);
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
	public final boolean replaceChild(@Nonnull ITreeNode oldChild, @Nonnull ITreeNode newChild) {
		// check if current node is not parent node
		if (oldChild.getParent() != this) return false;
		// check if child node is root node
		if (!newChild.isRoot()) return false;

		final int index = children.indexOf(oldChild);
		children.set(index, newChild);
		getTreeNode(oldChild).internalSetParent(null);
		getTreeNode(newChild).internalSetParent(this);
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
	public final <E extends ITreeNode> boolean addChildren(@Nonnull List<E> newChildren) {
		if (newChildren.isEmpty()) return true;

		for (final ITreeNode child : newChildren) {
			if (!child.isRoot()) return false;
		}
		children.addAll(newChildren);
		for (final ITreeNode child : newChildren) {
			getTreeNode(child).internalSetParent(this);
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
	public final List<ITreeNode> removeChildren() {
		if (children.isEmpty()) return List.of();

		final List<ITreeNode> oldChildren = List.copyOf(children);
		for (final ITreeNode child : oldChildren) child.removeFromParent();
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
	public final boolean addToParent(@Nonnull ITreeNode parent) {
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

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final TreeNode node = (TreeNode) object;
		return Objects.equals(parent, node.parent);// && children.equals(node.children);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	/**
	 * Return this tree iterator
	 *
	 * @return the iterator
	 */
	@Nonnull
	@Override
	public final Iterator<ITreeNode> iterator() {
		return new NodeIterator(this);
	}

	/**
	 * The tree iterator
	 */
	private static final class NodeIterator implements Iterator<ITreeNode> {
		private ITreeNode current;
		private Stack<Iterator<ITreeNode>> iterators = new Stack<>();

		private NodeIterator(ITreeNode treeNode) {
			this.iterators.push(getTreeNode(treeNode).children.iterator());
		}

		@Override
		public final boolean hasNext() {
			if (current != null) {
				this.iterators.push(getTreeNode(current).children.iterator());
				this.current = null;
			}
			do {
				if (iterators.peek().hasNext()) return true;
				this.iterators.pop();
			} while (!iterators.isEmpty());
			return false;
		}

		@Override
		public final ITreeNode next() {
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
