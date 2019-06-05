package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.ast.INode;

interface IUnknown extends INode {
	/**
	 * The unknown builder
	 */
	interface IUnknownBuilder extends INodeBuilder<IUnknown, IUnknownBuilder> {
	}
}
