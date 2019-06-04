package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Base node type
 */
public interface INode extends Iterable<INode>, Serializable {
	@Nonnull
	String getName();

	@Nonnull
	String getUniqueName();

	@Nonnull
	String getSignature();

	/**
	 * @return read-only view of dependency info map
	 */
	@Nonnull
	Map<INode, Dependency> getDependencies();

	/**
	 * Add multiple dependency info
	 *
	 * @param dependencyMap new dependency map
	 */
	void addDependencies(@Nonnull Map<INode, Dependency> dependencyMap);

	/**
	 * remove all dependency info
	 *
	 * @return old dependency map
	 */
	@Nonnull
	Map<INode, Dependency> removeDependencies();

	/**
	 * Get dependency info
	 *
	 * @param node the node
	 * @return the dependency info, or null if not exist
	 */
	@Nullable
	Dependency getDependency(@Nonnull INode node);

	/**
	 * Get dependency info, create new if not exist
	 *
	 * @param node the node
	 * @return the dependency info
	 */
	@Nonnull
	Dependency addDependency(@Nonnull INode node);

	/**
	 * Remove dependency info
	 *
	 * @param node the node
	 * @return true if the dependency info exist, false otherwise
	 */
	boolean removeDependency(@Nonnull INode node);

	/**
	 * Replace node dependency with another node dependency
	 *
	 * @param oldNode the old node
	 * @param newNode the new node
	 * @return the dependency info, or null if oldNode not exist
	 */
	@Nullable
	Dependency replaceDependency(@Nonnull INode oldNode, @Nonnull INode newNode);

	/**
	 * compare two dependency map
	 *
	 * @return result
	 */
	boolean equalsDependencies(@Nonnull INode node);
	
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
