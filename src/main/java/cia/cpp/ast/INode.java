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
	String getContent();

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
	 * Create new dependency info
	 *
	 * @param node the node
	 * @return the dependency info, or null if already exist
	 */
	@Nullable
	Dependency createDependency(@Nonnull INode node, @Nonnull Dependency.Type type);

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
		String getContent();

		@Nonnull
		B setContent(@Nonnull String content);
	}
}
