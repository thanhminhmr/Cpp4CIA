package cia.cpp.ast;

@IAstComponent
public interface ITypedef extends ITypeContainer {
	/**
	 * The typedef builder
	 */
	interface ITypedefBuilder extends INodeBuilder<ITypedef, ITypedefBuilder>, ITypeContainerBuilder<ITypedef, ITypedefBuilder> {
	}
}
