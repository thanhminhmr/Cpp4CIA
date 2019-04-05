package cia.cpp.ast;

@IAstComponent
public interface INamespace extends IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	/**
	 * The namespace builder
	 */
	interface INamespaceBuilder extends INodeBuilder<INamespace, INamespaceBuilder> {
	}
}
