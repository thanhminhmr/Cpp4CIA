package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * Base of nay AST TreeNode. Do not use TreeNode class directly.
 */
abstract class Node extends TreeNode implements INode {
	@Nonnull
	private final String name;

	protected Node(@Nonnull String name) {
		this.name = name;
	}

	protected final <E> List<E> getChildrenList(final Class<E> aClass) {
		final List<ITreeNode> children = super.getChildren();
		final List<E> list = new ArrayList<>(children.size());
		for (final ITreeNode child : children) {
			if (aClass.isInstance(child)) {
				list.add(aClass.cast(child));
			}
		}
		return list;
	}

	@Override
	@Nonnull
	public final String getName() {
		return name;
	}
}

