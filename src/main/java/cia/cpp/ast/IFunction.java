package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

@IAstComponent
public interface IFunction extends ITypeContainer, IClassContainer, IEnumContainer, IVariableContainer {
	@Nonnull
	List<INode> getParameters();

	boolean addParameters(@Nonnull List<INode> parameters);

	List<INode> removeParameters();

	boolean addParameter(@Nonnull INode parameter);

	boolean removeParameter(@Nonnull INode parameter);

	boolean replaceParameter(@Nonnull INode oldParameter, @Nonnull INode newParameter);

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
