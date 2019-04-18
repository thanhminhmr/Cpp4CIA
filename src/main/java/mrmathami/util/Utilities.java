package mrmathami.util;

import java.util.Collection;
import java.util.Map;

public final class Utilities {
	private Utilities() {
	}

	public static String objectToString(Object object) {
		return object != null ? String.format("%s,0x%08X",
				object.getClass().getSimpleName(),
				object.hashCode()) : "null";
	}

	public static String mapToString(Map<?, ?> map) {
		final StringBuilder builder = new StringBuilder().append("[");
		for (final Map.Entry<?, ?> entry : map.entrySet()) {
			if (builder.length() > 1) builder.append(',');
			builder.append("\n\t").append(entry.getKey()).append(" = ").append(entry.getValue());
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
	}

	public static String collectionToString(Collection<?> collection) {
		final StringBuilder builder = new StringBuilder().append("[");
		for (final Object element : collection) {
			if (builder.length() > 1) builder.append(',');
			builder.append("\n\t").append(element);
		}
		if (builder.length() > 1) builder.append('\n');
		return builder.append(']').toString();
	}
}
