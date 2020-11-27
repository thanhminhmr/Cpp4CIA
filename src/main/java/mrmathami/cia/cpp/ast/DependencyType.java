package mrmathami.cia.cpp.ast;

import java.util.List;

public enum DependencyType {
	USE, MEMBER, INHERITANCE, INVOCATION, OVERRIDE;

	public static final List<DependencyType> values = List.of(DependencyType.values());
}
