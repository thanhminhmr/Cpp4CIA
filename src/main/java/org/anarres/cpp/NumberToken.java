/*
 * Anarres C Preprocessor
 * Copyright (c) 2007-2015, Shevek
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.anarres.cpp;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

final class NumberToken {
	public static final int F_UNSIGNED = 1;
	public static final int F_INT = 2;
	public static final int F_LONG = 4;
	public static final int F_LONG_LONG = 8;
	public static final int F_FLOAT = 16;
	public static final int F_DOUBLE = 32;

	public static final int FF_SIZE = F_INT | F_LONG | F_LONG_LONG | F_FLOAT | F_DOUBLE;

	private final int base;
	@Nonnull private final String integer;

	@Nullable private String fraction;
	private boolean binaryExponent;
	@Nullable private String exponent;

	private int flags;

	public NumberToken(int base, @Nonnull String integer) {
		this.base = base;
		this.integer = integer;
	}

	void setFractionalPart(@Nonnull String fraction) {
		this.fraction = fraction;
	}

	void setExponent(boolean binaryExponent, @Nonnull String exponent) {
		this.binaryExponent = binaryExponent;
		this.exponent = exponent;
	}

	void setFlags(int flags) {
		this.flags = flags;
	}

	@Nonnull
	public Value value() {
		if ((fraction == null || fraction.isEmpty()) && exponent == null) {
			return Value.of(Long.parseUnsignedLong(integer, base));
		}
		double value;
		if (fraction != null && !fraction.isEmpty()) {
			final StringBuilder withFraction = new StringBuilder(32).append(integer).append(fraction);
			if (withFraction.length() > 16) withFraction.setLength(16);
			value = (double) Long.parseUnsignedLong(withFraction.toString(), base)
					* Math.pow(base, integer.length() - withFraction.length());
		} else {
			value = (double) Long.parseUnsignedLong(integer, base);
		}
		if (exponent != null) {
			final long exponentValue = Long.parseLong(exponent, 10);
			return Value.of(value * Math.pow(binaryExponent ? 2 : 10, exponentValue));
		}
		return Value.of(value);
	}

	private boolean appendFlags(StringBuilder buf, String suffix, int flag) {
		if ((flags & flag) != flag) return false;
		buf.append(suffix);
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		if (base == 8) {
			builder.append('0');
		} else if (base == 16) {
			builder.append("0x");
		} else if (base == 2) {
			builder.append('b');
		} else if (base != 10) {
			builder.insert(0, "[base " + base + " ??]");
		}
		builder.append(integer);
		if (fraction != null) builder.append('.').append(fraction);
		if (exponent != null) builder.append(base == 16 ? 'p' : 'e').append(exponent);
		final boolean hasFlags = appendFlags(builder, "ui", F_UNSIGNED | F_INT)
				|| appendFlags(builder, "ul", F_UNSIGNED | F_LONG)
				|| appendFlags(builder, "ull", F_UNSIGNED | F_LONG_LONG)
				|| appendFlags(builder, "i", F_INT)
				|| appendFlags(builder, "l", F_LONG)
				|| appendFlags(builder, "ll", F_LONG_LONG)
				|| appendFlags(builder, "f", F_FLOAT)
				|| appendFlags(builder, "d", F_DOUBLE);
		return builder.toString();
	}
}
