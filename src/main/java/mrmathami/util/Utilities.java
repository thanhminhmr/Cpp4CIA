package mrmathami.util;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.function.Function;

public final class Utilities {
	private Utilities() {
	}

	public static String objectIdentifyString(Object object) {
		if (object == null) return "null";
		final String simpleName = object.getClass().getSimpleName();
		final int objectHashCode = object.hashCode();
		final int systemHashCode = System.identityHashCode(object);
		return objectHashCode != systemHashCode
				? String.format("%s,0x%08X,0x%08X", simpleName, objectHashCode, systemHashCode)
				: String.format("%s,0x%08X", simpleName, objectHashCode);
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
			final String append = toString != null ? toString.apply(element) : element.toString();
			builder.append("\n\t").append(append.replace("\n", "\n\t"));
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
	}
}
