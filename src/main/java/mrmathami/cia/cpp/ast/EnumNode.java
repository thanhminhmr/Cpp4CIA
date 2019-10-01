package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.List;

public final class EnumNode extends Node implements IVariableContainer {
	private static final long serialVersionUID = -6608005197635338383L;

	public EnumNode() {
	}

	@Nonnull
	@Override
	public final List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}
}

