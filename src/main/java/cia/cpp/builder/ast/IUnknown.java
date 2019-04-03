package cia.cpp.builder.ast;

import cia.cpp.ast.INode;

interface IUnknown extends INode {
	/**
	 * The unknown builder
	 */
	interface IUnknownBuilder extends INodeBuilder<IUnknown, IUnknownBuilder> {
	}
}
