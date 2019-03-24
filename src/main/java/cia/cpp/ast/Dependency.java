package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Serializable;

public final class Dependency implements Serializable {
	private final int type;
	private int count = 0;

	Dependency(@Nonnull Type type) {
		this.type = type.ordinal();
	}

	public final int getCount() {
		return count;
	}

	public final void setCount(int count) {
		this.count = count;
	}

	public final int increment() {
		this.count += 1;
		return count;
	}

	@Nullable
	public final Type getType() {
		return (type >= 0 && type < Type.values.length) ? Type.values[type] : null;
	}

	public enum Type {
		USE,
		MEMBER,
		INHERITANCE,
		CONTAINMENT,
		INVOCATION,
		OVERRIDE,
		CALLBACK;

		private static final Type[] values = Type.values();
	}
}
