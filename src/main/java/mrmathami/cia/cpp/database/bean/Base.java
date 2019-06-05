package mrmathami.cia.cpp.database.bean;

import java.util.List;
import java.util.Objects;

public final class Base extends BeanClass {
	private static final BeanInfo BEAN_INFO = BeanInfo.register(
			"bases",
			List.of("classId", "baseId"),
			List.of(Integer.class, Integer.class)
	);

	public Base() {
		super(BEAN_INFO);
	}

	public final Integer getClassId() {
		return (Integer) get("classId");
	}

	public final Base setClassId(Integer classId) {
		put("classId", classId);
		return this;
	}

	public final Integer getBaseId() {
		return (Integer) get("baseId");
	}

	public final Base setBaseId(Integer baseId) {
		put("baseId", baseId);
		return this;
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Base base = (Base) object;
		return Objects.equals(getClassId(), base.getClassId()) && Objects.equals(getBaseId(), base.getBaseId());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getClassId(), getBaseId());
	}
}
