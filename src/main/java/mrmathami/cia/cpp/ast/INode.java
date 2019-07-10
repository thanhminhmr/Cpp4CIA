package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

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

	float getWeight();

	void setWeight(float weight);

	float getImpact();

	void setImpact(float impact);

	//<editor-fold desc="Dependency">

	//<editor-fold desc="All Dependency">

	/**
	 * transfer all dependency info to another node
	 *
	 * @param node the node to transfer all dependencies to
	 */
	void transferAllDependency(@Nonnull INode node);

	/**
	 * remove all dependency info
	 */
	void removeAllDependency();

	/**
	 * compare two nodes dependency
	 *
	 * @return result
	 */
	boolean equalsAllDependency(@Nonnull INode node);
	//</editor-fold>

	//<editor-fold desc="All Dependency From">

	@Nonnull
	List<INode> getAllDependencyFrom();

	/**
	 * transfer all dependency info to another node
	 *
	 * @param node the node to transfer all dependencies to
	 */
	void transferAllDependencyFrom(@Nonnull INode node);

	/**
	 * remove all dependency info
	 */
	void removeAllDependencyFrom();

	/**
	 * compare two nodes dependency
	 *
	 * @return result
	 */
	boolean equalsAllDependencyFrom(@Nonnull INode node);
	//</editor-fold>

	//<editor-fold desc="All Dependency To">

	@Nonnull
	List<INode> getAllDependencyTo();

	/**
	 * transfer all dependency info to another node
	 *
	 * @param node the node to transfer all dependencies to
	 */
	void transferAllDependencyTo(@Nonnull INode node);

	/**
	 * remove all dependency info
	 */
	void removeAllDependencyTo();

	/**
	 * compare two nodes dependency
	 *
	 * @return result
	 */
	boolean equalsAllDependencyTo(@Nonnull INode node);
	//</editor-fold>

	//<editor-fold desc="Node Dependency From">

	/**
	 * Get node dependency
	 *
	 * @param node the node
	 * @return the read-only dependency map
	 */
	@Nonnull
	Map<DependencyType, Integer> getNodeDependencyFrom(@Nonnull INode node);

	/**
	 * Add node dependency by dependency map
	 *
	 * @param node          the node
	 * @param dependencyMap the dependency map
	 */
	void addNodeDependencyFrom(@Nonnull INode node, @Nonnull Map<DependencyType, Integer> dependencyMap);

	/**
	 * Remove node dependency
	 *
	 * @param node the node
	 */
	void removeNodeDependencyFrom(@Nonnull INode node);
	//</editor-fold>

	//<editor-fold desc="Node Dependency To">

	/**
	 * Get node dependency
	 *
	 * @param node the node
	 * @return the read-only dependency map
	 */
	@Nonnull
	Map<DependencyType, Integer> getNodeDependencyTo(@Nonnull INode node);

	/**
	 * Add node dependency by dependency map
	 *
	 * @param node          the node
	 * @param dependencyMap the dependency map
	 */
	void addNodeDependencyTo(@Nonnull INode node, @Nonnull Map<DependencyType, Integer> dependencyMap);

	/**
	 * Remove node dependency
	 *
	 * @param node the node
	 */
	void removeNodeDependencyTo(@Nonnull INode node);
	//</editor-fold>

	//<editor-fold desc="Dependency From">

	/**
	 * Get dependency count
	 *
	 * @param node the node
	 * @param type the type
	 * @return the count of the dependency
	 */
	int getDependencyFrom(@Nonnull INode node, @Nonnull DependencyType type);

	/**
	 * Add dependency count
	 *
	 * @param node the node
	 * @param type the type
	 */
	void addDependencyFrom(@Nonnull INode node, @Nonnull DependencyType type);

	/**
	 * Remove dependency
	 *
	 * @param node the node
	 * @param type the type
	 */
	void removeDependencyFrom(@Nonnull INode node, @Nonnull DependencyType type);
	//</editor-fold>

	//<editor-fold desc="Dependency To">

	/**
	 * Get dependency count
	 *
	 * @param node the node
	 * @param type the type
	 * @return the count of the dependency
	 */
	int getDependencyTo(@Nonnull INode node, @Nonnull DependencyType type);

	/**
	 * Add dependency count
	 *
	 * @param node the node
	 * @param type the type
	 */
	void addDependencyTo(@Nonnull INode node, @Nonnull DependencyType type);

	/**
	 * Remove dependency
	 *
	 * @param node the node
	 * @param type the type
	 */
	void removeDependencyTo(@Nonnull INode node, @Nonnull DependencyType type);
	//</editor-fold>

	//</editor-fold>

	//<editor-fold desc="Object Helper">
	@Override
	boolean equals(Object object);

	@Override
	int hashCode();
	//</editor-fold>

	boolean matches(Object node);

	//<editor-fold desc="TreeNode">

	/**
	 * Move information of this node to the new node
	 *
	 * @param node new node
	 * @return false if new node is not root node
	 */
	boolean transfer(@Nonnull INode node);

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
	 * Remove children nodes from current node.
	 * Return children nodes.
	 */
	void removeChildren();

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

	//</editor-fold>

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
