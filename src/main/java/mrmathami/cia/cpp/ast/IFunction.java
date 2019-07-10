package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@IAstComponent
public interface IFunction extends ITypeContainer, IClassContainer, IEnumContainer, IVariableContainer {
	@Nonnull
	List<INode> getParameters();

	void removeParameters();

	boolean addParameter(@Nonnull INode parameter);

	boolean removeParameter(@Nonnull INode parameter);

	boolean replaceParameter(@Nonnull INode oldParameter, @Nonnull INode newParameter);

	@Nullable
	String getBody();

	void setBody(@Nullable String body);

	/**
	 * The function builder
	 */
	interface IFunctionBuilder extends INodeBuilder<IFunction, IFunctionBuilder>, ITypeContainerBuilder<IFunction, IFunctionBuilder> {
		@Nonnull
		List<INode> getParameters();

		@Nonnull
		IFunctionBuilder setParameters(@Nonnull List<INode> parameters);
	}
}
