package cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public final class Dependency implements Serializable {
	private static final long serialVersionUID = -6916548911262334697L;

	private int type = 0;
	private int count = 1;

	Dependency(@Nonnull Type type) {
		this.type = type.ordinal();
	}

	Dependency() {
	}

	public final int getCount() {
		return count;
	}

	@Nonnull
	public final Dependency setCount(int count) {
		this.count = count;
		return this;
	}

	@Nonnull
	public final Dependency incrementCount() {
		this.count += 1;
		return this;
	}

	@Nonnull
	public final Type getType() {
		if (type < 0 || type >= Type.values.length) throw new IndexOutOfBoundsException("Invalid type!");
		return Type.values[type];
	}

	@Nonnull
	public final Dependency setType(@Nonnull Type type) {
		this.type = type.ordinal();
		return this;
	}

	@Override
	public final String toString() {
		return "(" + Utilities.objectToString(this)
				+ ") { type: " + getType()
				+ ", count: " + count
				+ " }";
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Dependency dependency = (Dependency) object;
		return type == dependency.type && count == dependency.count;

	}

	@Override
	public final int hashCode() {
		int result = type;
		result = 31 * result + count;
		return result;
	}

	public enum Type {
		UNKNOWN,
		USE,
		MEMBER,
		INHERITANCE,
		//CONTAINMENT,
		INVOCATION,
		OVERRIDE;

		private static final Type[] values = Type.values();
	}
}
