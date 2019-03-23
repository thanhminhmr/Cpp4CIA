package cia.cpp.ast;

import javax.annotation.Nonnull;

/**
 * Base node type
 */
public interface INode extends ITreeNode {
	@Nonnull
	String getName();
}
