package cia.cpp.ast;

@IAstComponent
public interface IVariable extends ITypeContainer {
	/**
	 * The variable builder
	 */
	interface IVariableBuilder extends INodeBuilder<IVariable, IVariableBuilder>, ITypeContainerBuilder<IVariable, IVariableBuilder> {
	}
}
