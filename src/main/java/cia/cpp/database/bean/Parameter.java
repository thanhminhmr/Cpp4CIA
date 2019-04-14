package cia.cpp.database.bean;

import java.util.List;
import java.util.Objects;

public final class Parameter extends BeanClass {
	private static final BeanInfo BEAN_INFO = BeanInfo.register(
			"parameters",
			List.of("functionId", "parameterId"),
			List.of(Integer.class, Integer.class)
	);

	public Parameter() {
		super(BEAN_INFO);
	}

	public final Integer getFunctionId() {
		return (Integer) get("functionId");
	}

	public final Parameter setFunctionId(Integer functionId) {
		put("functionId", functionId);
		return this;
	}

	public final Integer getParameterId() {
		return (Integer) get("parameterId");
	}

	public final Parameter setParameterId(Integer parameterId) {
		put("parameterId", parameterId);
		return this;
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Parameter parameter = (Parameter) object;
		return Objects.equals(getFunctionId(), parameter.getFunctionId()) && Objects.equals(getParameterId(), parameter.getParameterId());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getFunctionId(), getParameterId());
	}
}
