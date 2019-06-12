package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;

public final class Dependency implements Serializable {
	private static final long serialVersionUID = -1915308422101126082L;
	private static final int[] ZERO = new int[Type.values.length];

	@Nonnull
	private final Map<INode, int[]> map = new HashMap<>();

	Dependency() {
	}

	@Nonnull
	private static String countsToString(int[] counts) {
		final StringBuilder builder = new StringBuilder().append('{');
		for (int type = 0; type < counts.length; type++) {
			int typeCount = counts[type];
			if (typeCount != 0) {
				if (builder.length() > 1) builder.append(',');
				builder.append(' ').append(Type.values[type]).append(": ").append(typeCount);
			}
		}
		if (builder.length() > 1) builder.append(' ');
		return builder.append('}').toString();
	}

	public final boolean isEmpty() {
		return map.isEmpty();
	}

	@Nonnull
	public final Dependency add(@Nonnull INode node, @Nonnull Type type) {
		final int[] counts = map.get(node);
		if (counts != null) {
			counts[type.ordinal()] += 1;
		} else {
			final int[] newCounts = new int[Type.values.length];
			newCounts[type.ordinal()] += 1;
			map.put(node, newCounts);
		}
		return this;
	}

	@Nonnull
	public final Dependency remove(@Nonnull INode node, @Nonnull Type type) {
		final int[] counts = map.get(node);
		if (counts != null) {
			counts[type.ordinal()] = 0;
			if (Arrays.equals(counts, ZERO)) {
				map.remove(node);
			}
		}
		return this;
	}

	@Nonnull
	public Dependency remove(@Nonnull INode node) {
		map.remove(node);
		return this;
	}

	@Nonnull
	public final Dependency replace(@Nonnull INode oldNode, @Nonnull INode newNode) {
		final int[] oldCounts = map.get(oldNode);
		if (oldCounts == null) return this;
		final int[] newCounts = map.get(newNode);
		if (newCounts != null) {
			for (int type = 0; type < oldCounts.length; type++) {
				newCounts[type] += oldCounts[type];
			}
		} else {
			map.put(newNode, oldCounts);
		}
		map.remove(oldNode);
		return this;
	}

	public final Set<INode> getNodes() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public final int getCount(@Nonnull INode node, @Nonnull Type type) {
		final int[] counts = map.get(node);
		return counts != null ? counts[type.ordinal()] : 0;
	}

	@Nonnull
	public final Dependency setCount(@Nonnull INode node, @Nonnull Type type, int count) {
		final int[] counts = map.get(node);
		if (counts != null) {
			counts[type.ordinal()] = count;
		} else {
			final int[] newCounts = new int[Type.values.length];
			map.put(node, newCounts);
		}
		return this;
	}

	final void merge(@Nonnull Dependency dependency) {
		if (map.isEmpty()) {
			map.putAll(dependency.map);
			return;
		}
		if (dependency.map.isEmpty()) return;

		for (final Map.Entry<INode, int[]> entry : dependency.map.entrySet()) {
			final INode node = entry.getKey();
			final int[] oldCounts = map.get(node);
			final int[] newCounts = entry.getValue();
			if (oldCounts != null) {
				for (int type = 0; type < oldCounts.length; type++) {
					oldCounts[type] += newCounts[type];
				}
			} else {
				map.put(node, newCounts);
			}
		}
	}

	final void clear() {
		map.clear();
	}

	@Override
	public final String toString() {
		return "(" + Utilities.objectToString(this) + ") "
				+ Utilities.mapToString(map, null, Dependency::countsToString);
	}

	@Override
	public final boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Dependency dependency = (Dependency) object;
		return map.equals(dependency.map);

	}

	@Override
	public int hashCode() {
		return map.hashCode();
	}

	public enum Type {
		UNKNOWN(0.0f),
		USE(4.0f),
		MEMBER(3.0f),
		INHERITANCE(4.0f),
		INVOCATION(3.5f),
		OVERRIDE(3.3f);

		private static final Type[] values = Type.values();

		private final float weight;

		Type(float weight) {
			this.weight = weight;
		}

		public float getWeight() {
			return weight;
		}
	}
}
