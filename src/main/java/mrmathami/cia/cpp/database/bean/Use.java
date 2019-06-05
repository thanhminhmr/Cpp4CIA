package mrmathami.cia.cpp.database.bean;

import java.util.List;
import java.util.Objects;

public final class Use extends BeanClass {
	private static final BeanInfo BEAN_INFO = BeanInfo.register(
			"uses",
			List.of("versionId", "nodeA", "nodeB", "typeEnum", "count"),
			List.of(Integer.class, Integer.class, Integer.class, Integer.class, Integer.class)
	);

	public Use() {
		super(BEAN_INFO);
	}

	public final Integer getVersionId() {
		return (Integer) get("versionId");
	}

	public final Use setVersionId(Integer id) {
		put("versionId", id);
		return this;
	}

	public final Integer getNodeA() {
		return (Integer) get("nodeA");
	}

	public final Use setNodeA(Integer nodeA) {
		put("nodeA", nodeA);
		return this;
	}

	public final Integer getNodeB() {
		return (Integer) get("nodeB");
	}

	public final Use setNodeB(Integer nodeB) {
		put("nodeB", nodeB);
		return this;
	}

	public final Integer getTypeEnum() {
		return (Integer) get("typeEnum");
	}

	public final Use setTypeEnum(Integer typeEnum) {
		put("typeEnum", typeEnum);
		return this;
	}

	public final Integer getCount() {
		return (Integer) get("count");
	}

	public final Use setCount(Integer count) {
		put("count", count);
		return this;
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Use use = (Use) object;
		return Objects.equals(getVersionId(), use.getVersionId())
				&& Objects.equals(getNodeA(), use.getNodeA())
				&& Objects.equals(getNodeB(), use.getNodeB());
	}

	@Override
	public int hashCode() {
		return Objects.hash(getVersionId(), getNodeA(), getNodeB());
	}
}
