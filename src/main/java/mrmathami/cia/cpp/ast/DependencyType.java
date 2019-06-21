package mrmathami.cia.cpp.ast;

public enum DependencyType {
	USE(4.0f, 1.0f),
	MEMBER(3.0f, 0.5f),
	INHERITANCE(4.0f, 1.0f),
	INVOCATION(3.5f, 0.875f),
	OVERRIDE(3.3f, 0.825f);

	static final DependencyType[] values = DependencyType.values();

	private final float forwardWeight;

	private final float backwardWeight;

	DependencyType(float forwardWeight, float backwardWeight) {
		this.forwardWeight = forwardWeight;
		this.backwardWeight = backwardWeight;
	}

	public float getForwardWeight() {
		return forwardWeight;
	}

	public float getBackwardWeight() {
		return backwardWeight;
	}
}
