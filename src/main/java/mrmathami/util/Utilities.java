package mrmathami.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public final class Utilities {
	private Utilities() {
	}

	@Nonnull
	public static String objectIdentifyString(@Nullable Object object) {
		if (object == null) return "null";
		final String simpleName = object.getClass().getSimpleName();
		final int objectHashCode = object.hashCode();
		final int systemHashCode = System.identityHashCode(object);
		return objectHashCode != systemHashCode
				? String.format("%s,0x%08X,0x%08X", simpleName, objectHashCode, systemHashCode)
				: String.format("%s,0x%08X", simpleName, objectHashCode);
	}

	@Nonnull
	public static String exceptionToString(@Nullable Exception exception) {
		if (exception == null) return "null";
		final StringWriter stringWriter = new StringWriter();
		final PrintWriter printWriter = new PrintWriter(stringWriter);
		exception.printStackTrace(printWriter);
		return stringWriter.toString();
	}

	@Nonnull
	public static <K, V> String mapToString(@Nullable Map<K, V> map) {
		return mapToString(map, null, null);
	}

	@Nonnull
	public static <K, V> String mapToString(@Nullable Map<K, V> map, @Nullable Function<K, String> keyToString, @Nullable Function<V, String> valueToString) {
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

	@Nonnull
	public static <E> String collectionToString(@Nullable Collection<E> collection) {
		return collectionToString(collection, null);
	}

	@Nonnull
	public static <E> String collectionToString(@Nullable Collection<E> collection, @Nullable Function<E, String> toString) {
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
