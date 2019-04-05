package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public final class Dependency implements Serializable {
	private static final long serialVersionUID = 7276599684911562797L;

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
	public String toString() {
		return "(" + Node.objectToString(this)
				+ ") { type: " + getType()
				+ ", count: " + count
				+ " }";
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
