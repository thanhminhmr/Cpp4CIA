package org.anarres.cpp;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.util.Objects;

final class Value implements Comparable<Value> {

	static final Value INTEGER_ZERO = new Value(0);
	static final Value INTEGER_POSITIVE_ONE = new Value(1);
	static final Value INTEGER_NEGATIVE_ONE = new Value(-1);

	static final Value DECIMAL_NAN = new Value(Double.NaN);
	static final Value DECIMAL_ZERO = new Value(0.0);
	static final Value DECIMAL_POSITIVE_ONE = new Value(1.0);
	static final Value DECIMAL_POSITIVE_INFINITY = new Value(Double.POSITIVE_INFINITY);
	static final Value DECIMAL_NEGATIVE_ONE = new Value(-1.0);
	static final Value DECIMAL_NEGATIVE_INFINITY = new Value(Double.NEGATIVE_INFINITY);

	@Nonnull
	static Value of(double decimal) {
		if (Double.isNaN(decimal)) return DECIMAL_NAN;
		if (decimal == 0.0) return DECIMAL_ZERO;
		if (decimal > 0.0) {
			if (decimal == 1.0) return DECIMAL_POSITIVE_ONE;
			if (Double.isInfinite(decimal)) return DECIMAL_POSITIVE_INFINITY;
		} else {
			if (decimal == -1.0) return DECIMAL_NEGATIVE_ONE;
			if (Double.isInfinite(decimal)) return DECIMAL_NEGATIVE_INFINITY;
		}
		return new Value(decimal);
	}

	@Nonnull
	static Value of(long integer) {
		if (integer == 0) return INTEGER_ZERO;
		if (integer == 1) return INTEGER_POSITIVE_ONE;
		if (integer == -1) return INTEGER_NEGATIVE_ONE;
		return new Value(integer);
	}

	private final double decimal;
	private final long integer;
	private final boolean mode;

	private Value(double decimal) {
		this.decimal = decimal;
		this.integer = 0;
		this.mode = true;
	}

	private Value(long integer) {
		this.decimal = 0;
		this.integer = integer;
		this.mode = false;
	}

	@Nonnull
	private Value newValue(double decimal) {
		if (mode && Double.compare(decimal, this.decimal) == 0) return this;
		return of(decimal);
	}

	@Nonnull
	private Value newValue(long integer) {
		if (!mode && integer == this.integer) return this;
		return of(integer);
	}

	@Nonnull
	public Value negate() {
		return mode ? newValue(-decimal) : newValue(-integer);
	}

	@Nonnull
	public Value add(@Nonnull Value value) {
		return mode == value.mode
				? mode
				? newValue(decimal + value.decimal)
				: newValue(integer + value.integer)
				: newValue(mode ? decimal + value.integer : integer + value.decimal);
	}

	@Nonnull
	public Value subtract(@Nonnull Value value) {
		return mode == value.mode
				? mode
				? newValue(decimal - value.decimal)
				: newValue(integer - value.integer)
				: newValue(mode ? decimal - value.integer : integer - value.decimal);
	}

	@Nonnull
	public Value multiply(@Nonnull Value value) {
		return mode == value.mode
				? mode
				? newValue(decimal * value.decimal)
				: newValue(integer * value.integer)
				: newValue(mode ? decimal * value.integer : integer * value.decimal);
	}

	@Nonnull
	public Value divide(@Nonnull Value value) {
		return mode == value.mode
				? mode
				? newValue(decimal / value.decimal)
				: newValue(integer / value.integer)
				: newValue(mode ? decimal / value.integer : integer / value.decimal);
	}

	@Nonnull
	public Value remainder(@Nonnull Value value) {
		// C++ doesn't support modulus on double
		if (mode || value.mode) throw new UnsupportedOperationException();
		return newValue(integer % value.integer);
	}

	@Nonnull
	public Value shiftLeft(int n) {
		if (mode) throw new UnsupportedOperationException();
		return newValue(integer << n);
	}

	@Nonnull
	public Value shiftRight(int n) {
		if (mode) throw new UnsupportedOperationException();
		return newValue(integer >> n);
	}

	private static int shiftValue(@Nonnull Value value) {
		if (value.mode || value.integer > Integer.MAX_VALUE || value.integer < Integer.MIN_VALUE) {
			throw new UnsupportedOperationException();
		}
		return (int) value.integer;
	}

	@Nonnull
	public Value shiftLeft(@Nonnull Value value) {
		if (mode) throw new UnsupportedOperationException();
		return newValue(integer << shiftValue(value));
	}

	@Nonnull
	public Value shiftRight(@Nonnull Value value) {
		if (mode) throw new UnsupportedOperationException();
		return newValue(integer >> shiftValue(value));
	}

	@Nonnull
	public Value and(@Nonnull Value value) {
		if (mode || value.mode) throw new UnsupportedOperationException();
		return newValue(integer & value.integer);
	}

	@Nonnull
	public Value or(@Nonnull Value value) {
		if (mode || value.mode) throw new UnsupportedOperationException();
		return newValue(integer | value.integer);
	}

	@Nonnull
	public Value xor(@Nonnull Value value) {
		if (mode || value.mode) throw new UnsupportedOperationException();
		return newValue(integer ^ value.integer);
	}

	@Nonnull
	public Value not() {
		if (mode) throw new UnsupportedOperationException();
		return newValue(~integer);
	}

	@Override
	public int hashCode() {
		return Objects.hash(decimal, integer, mode);
	}

	@Override
	public String toString() {
		return mode ? Double.toString(decimal) : Long.toString(integer);
	}

	public boolean equals(long integer) {
		return mode ? Double.compare(decimal, integer) == 0 : integer == this.integer;
	}

	public boolean equals(double decimal) {
		return Double.compare(decimal, mode ? this.decimal : this.integer) == 0;
	}

	@Override
	public boolean equals(@Nullable Object object) {
		if (this == object) return true;
		if (!(object instanceof Value)) return false;
		final Value value = (Value) object;
		return value.mode ? equals(value.decimal) : equals(value.integer);
	}

	public int compareTo(long integer) {
		return mode ? Double.compare(decimal, integer) : Long.compare(integer, this.integer);
	}

	public int compareTo(double decimal) {
		return Double.compare(decimal, mode ? this.decimal : this.integer);
	}

	@Override
	public int compareTo(@Nonnull Value value) {
		return value.mode ? compareTo(value.decimal) : compareTo(value.integer);
	}
}
