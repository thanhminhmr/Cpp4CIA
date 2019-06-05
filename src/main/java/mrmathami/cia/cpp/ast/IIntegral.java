package mrmathami.cia.cpp.ast;

@IAstComponent
public interface IIntegral extends INode {
	/**
	 * The integral builder
	 */
	interface IIntegralBuilder extends INodeBuilder<IIntegral, IIntegralBuilder> {
	}
}
