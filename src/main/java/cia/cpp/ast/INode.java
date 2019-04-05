package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Base node type
 */
public interface INode extends ITreeNode {
	@Nonnull
	static INode getNode(@Nonnull ITreeNode treeNode) {
		if (treeNode instanceof INode) return (INode) treeNode;
		throw new IllegalStateException("Unexpected foreign node in tree.");
	}

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
	 * Replace node dependency with another node dependency
	 *
	 * @param oldNode the old node
	 * @param newNode the new node
	 * @return the dependency info, or null if oldNode not exist
	 */
	@Nullable
	Dependency replaceDependency(@Nonnull INode oldNode, @Nonnull INode newNode);

	/**
	 * Remove dependency info
	 *
	 * @param node the node
	 * @return true if the dependency info exist, false otherwise
	 */
	boolean removeDependency(@Nonnull INode node);

	void addDependencies(Map<INode, Dependency> newDependencyMap);

	@Nonnull
	Map<INode, Dependency> removeDependencies();

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
