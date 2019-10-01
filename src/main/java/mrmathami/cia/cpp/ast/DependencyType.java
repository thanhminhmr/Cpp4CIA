package mrmathami.cia.cpp.ast;

public enum DependencyType {
	USE(4.0f),
	MEMBER(3.0f),
	INHERITANCE(4.0f),
	INVOCATION(3.5f),
	OVERRIDE(3.3f);

	static final DependencyType[] values = DependencyType.values();

	private final float weight;

	DependencyType(float weight) {
		this.weight = weight;
	}

	public float getWeight() {
		return weight;
	}
}
