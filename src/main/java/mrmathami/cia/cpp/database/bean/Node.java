package mrmathami.cia.cpp.database.bean;

import java.util.List;
import java.util.Objects;

public final class Node extends BeanClass {
	private static final BeanInfo BEAN_INFO = BeanInfo.register(
			"nodes",
			List.of("id", "typeEnum", "parentId", "name", "uniqueName", "signature", "typeId", "versionId"),
			List.of(Integer.class, Integer.class, Integer.class, String.class, String.class, String.class, Integer.class, Integer.class)
	);

	public Node() {
		super(BEAN_INFO);
	}

	public final Integer getId() {
		return (Integer) get("id");
	}

	public final Node setId(Integer id) {
		put("id", id);
		return this;
	}

	public final Integer getTypeEnum() {
		return (Integer) get("typeEnum");
	}

	public final Node setTypeEnum(Integer typeEnum) {
		put("typeEnum", typeEnum);
		return this;
	}

	public final Integer getParentId() {
		return (Integer) get("parentId");
	}

	public final Node setParentId(Integer parentId) {
		put("parentId", parentId);
		return this;
	}

	public final String getName() {
		return (String) get("name");
	}

	public final Node setName(String name) {
		put("name", name);
		return this;
	}

	public final String getUniqueName() {
		return (String) get("uniqueName");
	}

	public final Node setUniqueName(String uniqueName) {
		put("uniqueName", uniqueName);
		return this;
	}

	public final String getSignature() {
		return (String) get("signature");
	}

	public final Node setSignature(String signature) {
		put("signature", signature);
		return this;
	}

	public final Integer getTypeId() {
		return (Integer) get("typeId");
	}

	public final Node setTypeId(Integer typeId) {
		put("typeId", typeId);
		return this;
	}

	public final Integer getVersionId() {
		return (Integer) get("versionId");
	}

	public final Node setVersionId(Integer versionId) {
		put("versionId", versionId);
		return this;
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Node node = (Node) object;
		return Objects.equals(getId(), node.getId());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getId());
	}
}
