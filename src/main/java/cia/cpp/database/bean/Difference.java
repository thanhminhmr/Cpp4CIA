package cia.cpp.database.bean;

import java.util.List;
import java.util.Objects;

public final class Difference extends BeanClass {
	private static final BeanInfo BEAN_INFO = BeanInfo.register(
			"differences",
			List.of("id", "versionA", "versionB", "nodeA", "nodeB", "typeEnum"),
			List.of(Integer.class, Integer.class, Integer.class, Integer.class, Integer.class, Integer.class)
	);

	public Difference() {
		super(BEAN_INFO);
	}

	public final Integer getId() {
		return (Integer) get("id");
	}

	public final Difference setId(Integer id) {
		put("id", id);
		return this;
	}

	public final Integer getVersionA() {
		return (Integer) get("versionA");
	}

	public final Difference setVersionA(Integer versionA) {
		put("versionA", versionA);
		return this;
	}

	public final Integer getVersionB() {
		return (Integer) get("versionB");
	}

	public final Difference setVersionB(Integer versionB) {
		put("versionB", versionB);
		return this;
	}

	public final Integer getNodeA() {
		return (Integer) get("nodeA");
	}

	public final Difference setNodeA(Integer nodeA) {
		put("nodeA", nodeA);
		return this;
	}

	public final Integer getNodeB() {
		return (Integer) get("nodeB");
	}

	public final Difference setNodeB(Integer nodeB) {
		put("nodeB", nodeB);
		return this;
	}

	public final Integer getTypeEnum() {
		return (Integer) get("typeEnum");
	}

	public final Difference setTypeEnum(Integer typeEnum) {
		put("typeEnum", typeEnum);
		return this;
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Difference difference = (Difference) object;
		return Objects.equals(getId(), difference.getId());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getId());
	}
}
