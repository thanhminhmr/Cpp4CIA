package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

public interface ITreeNode extends Iterable<ITreeNode>, Serializable {
	/**
	 * Return the root node.
	 *
	 * @return root node
	 */
	@Nonnull
	ITreeNode getRoot();

	/**
	 * Check if this node is root node.
	 * Note: a node without parent is a root node.
	 *
	 * @return true if this node is root node
	 */
	boolean isRoot();

	/**
	 * Get parent node, or null if there is none.
	 * Note: a node without parent is a root node.
	 *
	 * @return parent node
	 */
	@Nullable
	ITreeNode getParent();

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	List<ITreeNode> getChildren();

	/**
	 * Add child node to current node.
	 * Return false if child node already have parent node.
	 * Return true otherwise.
	 *
	 * @param child a child node to add
	 * @return whether the operation is success or not
	 */
	boolean addChild(@Nonnull ITreeNode child);

	/**
	 * Remove a child node from current node.
	 * Return false if the child node doesn't belong to this node.
	 * Return true otherwise.
	 *
	 * @param child a child node to remove
	 * @return whether the operation is success or not
	 */
	boolean removeChild(@Nonnull ITreeNode child);

	/**
	 * Replace a child node by another node from current node.
	 * Return false if the old child node doesn't belong to this node, or new child node already have parent.
	 * Return true otherwise.
	 *
	 * @param oldChild a child node to remove
	 * @param newChild a child node to add
	 * @return whether the operation is success or not
	 */
	boolean replaceChild(@Nonnull ITreeNode oldChild, @Nonnull ITreeNode newChild);

	/**
	 * Add children nodes to current node.
	 * Return false if one of children nodes already have parent node.
	 * Return true otherwise.
	 *
	 * @param children children nodes to add
	 * @return whether the operation is success or not
	 */
	<E extends ITreeNode> boolean addChildren(@Nonnull List<E> children);

	/**
	 * Remove children nodes from current node.
	 * Return children nodes.
	 *
	 * @return children nodes
	 */
	List<ITreeNode> removeChildren();

	/**
	 * Add this node to the parent node.
	 * Return false if this node already have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	boolean addToParent(@Nonnull ITreeNode parent);

	/**
	 * Remove this node itself from its parent node.
	 * Return false if this node doesn't have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	boolean removeFromParent();

	boolean equals(Object object);

	int hashCode();

	@Nonnull
	String toString();

	@Nonnull
	String toTreeElementString();

	@Nonnull
	String toTreeString();
}
