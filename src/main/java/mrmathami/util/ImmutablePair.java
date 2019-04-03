package mrmathami.util;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>A convenience class to represent name-value pairs.</p>
 */
public final class ImmutablePair<K, V> implements Serializable {
	private static final ImmutablePair EMPTY = new ImmutablePair<>(null, null);

	/**
	 * Key of this <code>ImmutablePair</code>.
	 */
	private final K key;

	/**
	 * Value of this this <code>ImmutablePair</code>.
	 */
	private final V value;

	/**
	 * Creates a new pair
	 *
	 * @param key   The key for this pair
	 * @param value The value to use for this pair
	 */
	public ImmutablePair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Create ImmutablePair
	 *
	 * @param key   key
	 * @param value value
	 * @param <K>   key type
	 * @param <V>   value type
	 * @return new ImmutablePair
	 */
	public static <K, V> ImmutablePair<K, V> of(K key, V value) {
		if (key == null && value == null) {
			//noinspection unchecked
			return (ImmutablePair<K, V>) EMPTY;
		}
		return new ImmutablePair<>(key, value);
	}

	/**
	 * Gets the key for this pair.
	 *
	 * @return key for this pair
	 */
	public final K getKey() {
		return key;
	}

	/**
	 * Gets the value for this pair.
	 *
	 * @return value for this pair
	 */

	public final V getValue() {
		return value;
	}

	/**
	 * <p><code>String</code> representation of this
	 * <code>ImmutablePair</code>.</p>
	 *
	 * <p>The default name/value delimiter '=' is always used.</p>
	 *
	 * @return <code>String</code> representation of this <code>ImmutablePair</code>
	 */
	@Override
	public final String toString() {
		return String.format("%s = %s", key, value);
	}

	/**
	 * <p>Test this <code>ImmutablePair</code> for equality with another
	 * <code>Object</code>.</p>
	 *
	 * <p>If the <code>Object</code> to be tested is not a
	 * <code>ImmutablePair</code> or is <code>null</code>, then this method
	 * returns <code>false</code>.</p>
	 *
	 * <p>Two <code>ImmutablePair</code>s are considered equal if and only if
	 * both the names and values are equal.</p>
	 *
	 * @param obj the <code>Object</code> to test for
	 *            equality with this <code>ImmutablePair</code>
	 * @return <code>true</code> if the given <code>Object</code> is
	 * equal to this <code>ImmutablePair</code> else <code>false</code>
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof ImmutablePair) {
			final ImmutablePair pair = (ImmutablePair) obj;
			return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
		}
		return false;
	}
}
