package mrmathami.util;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public final class Utilities {
	private Utilities() {
	}

	public static String objectIdentifyString(Object object) {
		return object != null ? String.format("%s,0x%08X,0x%08X",
				object.getClass().getSimpleName(),
				object.hashCode(),
				System.identityHashCode(object)) : "null";
	}

	public static <K, V> String mapToString(Map<K, V> map) {
		return mapToString(map, null, null);
	}

	public static <K, V> String mapToString(Map<K, V> map, Function<K, String> keyToString, Function<V, String> valueToString) {
		if (map == null) return "null";
		final StringBuilder builder = new StringBuilder().append("[");
		for (final Map.Entry<K, V> entry : map.entrySet()) {
			if (builder.length() > 1) builder.append(',');
			builder.append("\n\t").append(keyToString != null ? keyToString.apply(entry.getKey()) : entry.getKey())
					.append(" = ").append(valueToString != null ? valueToString.apply(entry.getValue()) : entry.getValue());
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
	}

	public static <E> String collectionToString(Collection<E> collection) {
		return collectionToString(collection, null);
	}

	public static <E> String collectionToString(Collection<E> collection, Function<E, String> toString) {
		if (collection == null) return "null";
		final StringBuilder builder = new StringBuilder().append("[");
		for (final E element : collection) {
			if (builder.length() > 1) builder.append(',');
			builder.append("\n\t").append(toString != null ? toString.apply(element) : element);
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
	}
}
