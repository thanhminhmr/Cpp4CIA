package mrmathami.cia.cpp.ast;

public enum DependencyType {
	USE, MEMBER, INHERITANCE, INVOCATION, OVERRIDE;

	static final DependencyType[] values = DependencyType.values();
}
