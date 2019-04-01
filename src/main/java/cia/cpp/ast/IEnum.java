package cia.cpp.ast;

@IAstComponent
public interface IEnum extends IVariableContainer {
	/**
	 * The enum builder
	 */
	interface IEnumBuilder extends INodeBuilder<IEnum, IEnumBuilder> {
	}
}
