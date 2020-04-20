package mrmathami.cia.cpp.ast;

public enum DependencyType {
	USE(4.0),
	MEMBER(3.0),
	INHERITANCE(4.0),
	INVOCATION(3.5),
	OVERRIDE(3.3);

	static final DependencyType[] values = DependencyType.values();

	private final double weight;

	DependencyType(double weight) {
		this.weight = weight;
	}

	public double getWeight() {
		return weight;
	}
}
