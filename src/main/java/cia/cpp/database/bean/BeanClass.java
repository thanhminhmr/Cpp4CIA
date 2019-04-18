package cia.cpp.database.bean;

import java.util.*;

public abstract class BeanClass extends AbstractMap<String, Object> implements Map<String, Object> {
	private final BeanInfo beanInfo;
	private final List<Object> fieldValues;

	protected BeanClass(BeanInfo beanInfo) {
		this.beanInfo = beanInfo;
		this.fieldValues = Arrays.asList(new Object[beanInfo.numOfField]);
	}

	public final String getTableName() {
		return beanInfo.tableName;
	}

	public final List<String> getFieldNames() {
		return beanInfo.fieldNames;
	}

	public final List<Class> getFieldTypes() {
		return beanInfo.fieldTypes;
	}

	public final Class getFieldType(String key) {
		final int index = key != null ? beanInfo.fieldNames.indexOf(key) : -1;
		if (index < 0) throw new UnsupportedOperationException("Invalid key! key = " + key);

		return beanInfo.fieldTypes.get(index);
	}

	@Override
	public Object put(String key, Object value) {
		final int index = key != null ? beanInfo.fieldNames.indexOf(key) : -1;
		if (index < 0) throw new UnsupportedOperationException("Invalid key! key = " + key);

		return fieldValues.set(index, beanInfo.fieldTypes.get(index).cast(value));
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet() {
		return new EntrySet(this);
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		BeanClass beanClass = (BeanClass) object;
		return beanInfo.equals(beanClass.beanInfo) && fieldValues.equals(beanClass.fieldValues);
	}

	@Override
	public int hashCode() {
		return Objects.hash(beanInfo, fieldValues);
	}

	protected static final class BeanInfo {
		private final int numOfField;
		public final String tableName;
		private final List<String> fieldNames;
		private final List<Class> fieldTypes;

		private BeanInfo(int numOfField, String tableName, List<String> fieldNames, List<Class> fieldTypes) {
			this.numOfField = numOfField;
			this.tableName = tableName;
			this.fieldNames = List.copyOf(fieldNames);
			this.fieldTypes = List.copyOf(fieldTypes);
		}

		protected static BeanInfo register(String tableName, List<String> fieldNames, List<Class> fieldTypes) {
			final int numOfField = fieldNames.size();

			if (numOfField != fieldTypes.size() || numOfField != Set.copyOf(fieldNames).size()) {
				throw new IllegalArgumentException("Invalid parameters.");
			}

			return new BeanInfo(numOfField, tableName, fieldNames, fieldTypes);
		}
	}

	private static final class EntrySet extends AbstractSet<Map.Entry<String, Object>> implements Set<Map.Entry<String, Object>> {
		private final BeanClass beanClass;

		public EntrySet(BeanClass beanClass) {
			this.beanClass = beanClass;
		}

		@Override
		public Iterator<Map.Entry<String, Object>> iterator() {
			return new EntryIterator(beanClass);
		}

		@Override
		public int size() {
			int fieldCount = 0;
			for (final Object value : beanClass.fieldValues) {
				if (value != null) fieldCount++;
			}
			return fieldCount;
		}
	}

	private static class EntryIterator implements Iterator<Map.Entry<String, Object>> {
		private final BeanClass beanClass;
		private int lastIndex = -1;
		private int index = 0;

		private EntryIterator(BeanClass beanClass) {
			this.beanClass = beanClass;
		}

		@Override
		public boolean hasNext() {
			while (index < beanClass.beanInfo.numOfField && beanClass.fieldValues.get(index) == null) {
				index += 1;
			}
			return index < beanClass.beanInfo.numOfField;
		}

		@Override
		public Map.Entry<String, Object> next() {
			while (index < beanClass.beanInfo.numOfField && beanClass.fieldValues.get(index) == null) {
				index += 1;
			}
			if (index >= beanClass.beanInfo.numOfField) throw new NoSuchElementException();
			lastIndex = index;
			return new Entry(beanClass, index++);
		}

		@Override
		public void remove() {
			if (lastIndex < 0) throw new IllegalStateException();
			beanClass.fieldValues.set(lastIndex, null);
			lastIndex = -1;
		}
	}

	private static class Entry implements Map.Entry<String, Object> {
		private final BeanClass beanClass;
		private final int index;

		public Entry(BeanClass beanClass, int index) {
			this.beanClass = beanClass;
			this.index = index;
		}

		@Override
		public String getKey() {
			return beanClass.beanInfo.fieldNames.get(index);
		}

		@Override
		public Object getValue() {
			return beanClass.fieldValues.get(index);
		}

		@Override
		public Object setValue(Object value) {
			return beanClass.fieldValues.set(index, beanClass.beanInfo.fieldTypes.get(index).cast(value));
		}
	}
}
