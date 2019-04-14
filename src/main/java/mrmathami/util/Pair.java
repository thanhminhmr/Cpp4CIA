package mrmathami.util;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * <p>A convenience class to represent name-value pairs.</p>
 */
public final class Pair<K, V> implements Map.Entry<K, V>, Serializable {
	private static final long serialVersionUID = -3462418631316367048L;
	
	/**
	 * Key of this <code>Pair</code>.
	 */
	private K key;
	/**
	 * Value of this this <code>Pair</code>.
	 */
	private V value;

	/**
	 * Creates a new pair
	 *
	 * @param key   The key for this pair
	 * @param value The value to use for this pair
	 */
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/**
	 * Creates a new empty pair
	 */
	public Pair() {
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
	 * Set the key for this pair.
	 */
	public final void setKey(K key) {
		this.key = key;
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
	 * Set the value for this pair.
	 */
	public final V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}

	/**
	 * <p><code>String</code> representation of this
	 * <code>Pair</code>.</p>
	 *
	 * <p>The default name/value delimiter '=' is always used.</p>
	 *
	 * @return <code>String</code> representation of this <code>Pair</code>
	 */
	@Override
	public final String toString() {
		return Utilities.objectToString(key) + "=" + Utilities.objectToString(value);
	}

	/**
	 * <p>Generate a hash code for this <code>Pair</code>.</p>
	 *
	 * <p>The hash code is calculated using both the name and
	 * the value of the <code>Pair</code>.</p>
	 *
	 * @return hash code for this <code>Pair</code>
	 */
	@Override
	public final int hashCode() {
		// name's hashCode is multiplied by an arbitrary prime number (13)
		// in order to make sure there is a difference in the hashCode between
		// these two parameters:
		//  name: a  value: aa
		//  name: aa value: a
		return key.hashCode() * 13 + value.hashCode();
	}

	/**
	 * <p>Test this <code>Pair</code> for equality with another
	 * <code>Object</code>.</p>
	 *
	 * <p>If the <code>Object</code> to be tested is not a
	 * <code>Pair</code> or is <code>null</code>, then this method
	 * returns <code>false</code>.</p>
	 *
	 * <p>Two <code>Pair</code>s are considered equal if and only if
	 * both the names and values are equal.</p>
	 *
	 * @param obj the <code>Object</code> to test for
	 *            equality with this <code>Pair</code>
	 * @return <code>true</code> if the given <code>Object</code> is
	 * equal to this <code>Pair</code> else <code>false</code>
	 */
	@Override
	public final boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof Pair) {
			final Pair pair = (Pair) obj;
			return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
		}
		return false;
	}
}
