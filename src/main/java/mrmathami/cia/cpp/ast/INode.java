package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;

/**
 * Base node type
 */
public interface INode extends Iterable<INode>, Serializable {
	int getId();

	@Nonnull
	String getName();

	@Nonnull
	String getUniqueName();

	@Nonnull
	String getSignature();

	float getDirectWeight();

	void setDirectWeight(float directWeight);

	float getIndirectWeight();

	void setIndirectWeight(float indirectWeight);

	/**
	 * @return read-only view of dependency info map
	 */
	@Nonnull
	Dependency getDependency();

	/**
	 * Add multiple dependency info
	 *
	 * @param dependency new dependency info
	 */
	void addDependency(@Nonnull Dependency dependency);

	/**
	 * remove all dependency info
	 *
	 * @return old dependency map
	 */
	@Nonnull
	Dependency removeDependency();

//==============================================================================

	/**
	 * Return the root node.
	 *
	 * @return root node
	 */
	@Nonnull
	INode getRoot();

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
	INode getParent();

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	List<INode> getChildren();

	/**
	 * Add child node to current node.
	 * Return false if child node already have parent node.
	 * Return true otherwise.
	 *
	 * @param child a child node to add
	 * @return whether the operation is success or not
	 */
	boolean addChild(@Nonnull INode child);

	/**
	 * Remove a child node from current node.
	 * Return false if the child node doesn't belong to this node.
	 * Return true otherwise.
	 *
	 * @param child a child node to remove
	 * @return whether the operation is success or not
	 */
	boolean removeChild(@Nonnull INode child);

	/**
	 * Replace a child node by another node from current node.
	 * Return false if the old child node doesn't belong to this node, or new child node already have parent.
	 * Return true otherwise.
	 *
	 * @param oldChild a child node to remove
	 * @param newChild a child node to add
	 * @return whether the operation is success or not
	 */
	boolean replaceChild(@Nonnull INode oldChild, @Nonnull INode newChild);

	/**
	 * Add children nodes to current node.
	 * Return false if one of children nodes already have parent node.
	 * Return true otherwise.
	 *
	 * @param children children nodes to add
	 * @return whether the operation is success or not
	 */
	<E extends INode> boolean addChildren(@Nonnull List<E> children);

	/**
	 * Remove children nodes from current node.
	 * Return children nodes.
	 *
	 * @return children nodes
	 */
	List<INode> removeChildren();

	/**
	 * Add this node to the parent node.
	 * Return false if this node already have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	boolean addToParent(@Nonnull INode parent);

	/**
	 * Remove this node itself from its parent node.
	 * Return false if this node doesn't have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	boolean removeFromParent();

	@Nonnull
	String toString();

	@Nonnull
	String toTreeElementString();

	@Nonnull
	String toTreeString();

//==============================================================================

	/**
	 * @param <E> the node
	 * @param <B> the node builder
	 */
	interface INodeBuilder<E extends INode, B extends INodeBuilder> {
		boolean isValid();

		@Nonnull
		E build();

		@Nullable
		String getName();

		@Nonnull
		B setName(@Nonnull String name);

		@Nullable
		String getUniqueName();

		@Nonnull
		B setUniqueName(@Nonnull String uniqueName);

		@Nullable
		String getSignature();

		@Nonnull
		B setSignature(@Nonnull String content);
	}
}
