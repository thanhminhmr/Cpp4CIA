package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Hold a type
 */
@IAstFragment
public interface ITypeContainer extends INode {
	@Nullable
	INode getType();

	void setType(@Nullable INode type);

	/**
	 * The type builder
	 */
	interface ITypeContainerBuilder<E extends ITypeContainer, B extends ITypeContainerBuilder> extends INodeBuilder<E, B> {
		@Nullable
		INode getType();

		@Nonnull
		B setType(@Nullable INode type);
	}
}
