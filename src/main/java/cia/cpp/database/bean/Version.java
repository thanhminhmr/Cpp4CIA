package cia.cpp.database.bean;

import java.util.List;
import java.util.Objects;

public final class Version extends BeanClass {
	private static final BeanInfo BEAN_INFO = BeanInfo.register(
			"versions",
			List.of("id", "name", "files", "paths", "rootId"),
			List.of(Integer.class, String.class, String.class, String.class, Integer.class)
	);

	public Version() {
		super(BEAN_INFO);
	}

	public final Integer getId() {
		return (Integer) get("id");
	}

	public final Version setId(Integer id) {
		put("id", id);
		return this;
	}

	public final String getName() {
		return (String) get("name");
	}

	public final Version setName(String name) {
		put("name", name);
		return this;
	}

	public final String getFiles() {
		return (String) get("files");
	}

	public final Version setFiles(String files) {
		put("files", files);
		return this;
	}

	public final String getPaths() {
		return (String) get("paths");
	}

	public final Version setPaths(String paths) {
		put("paths", paths);
		return this;
	}

	public final Integer getRootId() {
		return (Integer) get("rootId");
	}

	public final Version setRootId(Integer rootId) {
		put("rootId", rootId);
		return this;
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Version version = (Version) object;
		return Objects.equals(getId(), version.getId());
	}

	@Override
	public final int hashCode() {
		return Objects.hash(getId());
	}
}
