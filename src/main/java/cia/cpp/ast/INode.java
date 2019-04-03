package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Base node type
 */
public interface INode extends ITreeNode {
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
	Map<INode, Dependency> getDependencyMap();

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
	 * @return the dependency info, or null if already exist
	 */
	@Nonnull
	Dependency addDependency(@Nonnull INode node);

	@Nullable
	Dependency replaceDependency(@Nonnull INode oldNode, @Nonnull INode newNode);

	/**
	 * remove dependency info
	 *
	 * @param node the node
	 * @return true if the dependency info exist, false otherwise
	 */
	boolean removeDependency(@Nonnull INode node);

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
