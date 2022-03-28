package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.utils.IntsWrapper;
import mrmathami.utils.Pair;
import mrmathami.utils.Utilities;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static mrmathami.cia.cpp.ast.DependencyMap.DEPENDENCY_ZERO;

/**
 * Base of AST Tree.
 */
public abstract class CppNode implements Iterable<CppNode>, Externalizable {
	private static final long serialVersionUID = -1L;

	private int id;

	@Nonnull private String name = "";
	@Nonnull private String uniqueName = "";
	@Nonnull private String signature = "";

	@Nullable private CppNode parent;
	@Nonnull private List<CppNode> children = new LinkedList<>();
	@Nonnull private Map<CppNode, int[]> dependencyFrom = new IdentityHashMap<>();
	@Nonnull private Map<CppNode, int[]> dependencyTo = new IdentityHashMap<>();

	private boolean writable = true; // should not access this directly
	@Nullable private CppNode rootNode; // should not access this directly

	CppNode() {
	}

	@Nonnull
	private static String countsToString(@Nonnull int[] counts) {
		final StringBuilder builder = new StringBuilder().append('{');
		for (int type = 0; type < counts.length; type++) {
			int typeCount = counts[type];
			if (typeCount != 0) {
				builder.append(builder.length() > 1 ? ", \"" : " \"")
						.append(DependencyType.values.get(type)).append("\": ").append(typeCount);
			}
		}
		if (builder.length() > 1) builder.append(' ');
		return builder.append('}').toString();
	}

	static void escapeBody(@Nonnull StringBuilder builder, @Nonnull String body) {
		for (final int codePoint : body.codePoints().toArray()) {
			if (codePoint == '\\' || codePoint == '/' || codePoint == '"') {
				builder.append('\\').appendCodePoint(codePoint);
			} else if (codePoint == '\b') {
				builder.append("\\b");
			} else if (codePoint == '\f') {
				builder.append("\\f");
			} else if (codePoint == '\n') {
				builder.append("\\n");
			} else if (codePoint == '\r') {
				builder.append("\\r");
			} else if (codePoint == '\t') {
				builder.append("\\t");
			} else if (codePoint < 32) {
				builder.append("\\u00").append(codePoint <= 0xF ? '0' : '1')
						.append((char) ((codePoint <= 0x9 ? '0' : 49) + (codePoint & 0xF)));
			} else if (codePoint < 127) {
				// yes, the 127th character is a control character
				builder.appendCodePoint(codePoint);
			} else if (codePoint < 65535) {
				builder.append("\\u").append(String.format("%04X", codePoint));
			} else {
				// new unicode characters are more than 2 bytes
				builder.append("\\U").append(String.format("%08X", codePoint));
			}
		}
	}

	//region Node

	public final int getId() {
		return id;
	}

	@Internal
	public final void setId(int id) {
		checkReadOnly();
		this.id = id;
	}

	@Nonnull
	public final String getName() {
		return name;
	}

	@Internal
	public final void setName(@Nonnull String name) {
		checkReadOnly();
		this.name = name;
	}

	@Nonnull
	public final String getUniqueName() {
		return uniqueName;
	}

	@Internal
	public final void setUniqueName(@Nonnull String uniqueName) {
		checkReadOnly();
		this.uniqueName = uniqueName;
	}

	@Nonnull
	public final String getSignature() {
		return signature;
	}

	@Internal
	public final void setSignature(@Nonnull String signature) {
		checkReadOnly();
		this.signature = signature;
	}

	//endregion

	//region Dependency

	//region All Dependency

	@Internal
	public final void transferAllDependency(@Nonnull CppNode node) {
		transferAllDependencyFrom(node);
		transferAllDependencyTo(node);
	}

	@Internal
	public final void removeAllDependency() {
		removeAllDependencyFrom();
		removeAllDependencyTo();
	}

	public final boolean equalsAllDependency(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return equalsAllDependencyFrom(node, matcher) && equalsAllDependencyTo(node, matcher);
	}

	//endregion

	//region All Dependency From

	@Nonnull
	public final Set<CppNode> getAllDependencyFrom() {
		return isWritable() ? Collections.unmodifiableSet(dependencyFrom.keySet()) : dependencyFrom.keySet();
	}

	@Internal
	public final void transferAllDependencyFrom(@Nonnull CppNode node) {
		checkReadOnly();
		for (final Iterator<Map.Entry<CppNode, int[]>> iterator = dependencyFrom.entrySet().iterator(); iterator.hasNext(); ) {
			final Map.Entry<CppNode, int[]> entry = iterator.next();
			final CppNode fromNode = entry.getKey();
			final Map<CppNode, int[]> fromNodeDependencyTo = fromNode.dependencyTo;
			final int[] oldCounts = fromNodeDependencyTo.remove(this);
			assert oldCounts != null && oldCounts == entry.getValue() : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			if (fromNode != node) {
				final int[] newCounts = fromNodeDependencyTo.get(node);
				if (newCounts != null) {
					for (int i = 0; i < newCounts.length; i++) {
						newCounts[i] += oldCounts[i];
					}
					iterator.remove();
				} else {
					fromNodeDependencyTo.put(node, oldCounts);
				}
			} else {
				iterator.remove();
			}
		}
		node.dependencyFrom.putAll(dependencyFrom);
		dependencyFrom.clear();
	}

	@Internal
	public final void removeAllDependencyFrom() {
		checkReadOnly();
		for (final CppNode node : dependencyFrom.keySet()) {
			node.dependencyTo.remove(this);
		}
		dependencyFrom.clear();
	}

	public final boolean equalsAllDependencyFrom(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (dependencyFrom.size() != node.dependencyFrom.size()) return false;
		final HashMap<Wrapper, int[]> nodeDependencyFrom = new HashMap<>();
		for (final Map.Entry<CppNode, int[]> entry : node.dependencyFrom.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			nodeDependencyFrom.put(wrapper, entry.getValue());
		}
		for (final Map.Entry<CppNode, int[]> entry : dependencyFrom.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			final int[] counts = nodeDependencyFrom.get(wrapper);
			if (counts == null || !Arrays.equals(counts, entry.getValue())) return false;
			nodeDependencyFrom.remove(wrapper);
		}
		return nodeDependencyFrom.isEmpty();
	}

	//endregion

	//region All Dependency To

	@Nonnull
	public final Set<CppNode> getAllDependencyTo() {
		return isWritable() ? Collections.unmodifiableSet(dependencyTo.keySet()) : dependencyTo.keySet();
	}

	@Internal
	public final void transferAllDependencyTo(@Nonnull CppNode node) {
		checkReadOnly();
		for (final Iterator<Map.Entry<CppNode, int[]>> iterator = dependencyTo.entrySet().iterator(); iterator.hasNext(); ) {
			final Map.Entry<CppNode, int[]> entry = iterator.next();
			final CppNode toNode = entry.getKey();
			final Map<CppNode, int[]> toNodeDependencyFrom = toNode.dependencyFrom;
			final int[] oldCounts = toNodeDependencyFrom.remove(this);
			assert oldCounts != null && oldCounts == entry.getValue() : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			if (toNode != node) {
				final int[] newCounts = toNodeDependencyFrom.get(node);
				if (newCounts != null) {
					for (int i = 0; i < newCounts.length; i++) {
						newCounts[i] += oldCounts[i];
					}
					iterator.remove();
				} else {
					toNodeDependencyFrom.put(node, oldCounts);
				}
			} else {
				iterator.remove();
			}
		}
		node.dependencyTo.putAll(dependencyTo);
		dependencyTo.clear();
	}

	@Internal
	public final void removeAllDependencyTo() {
		checkReadOnly();
		for (final CppNode node : dependencyTo.keySet()) {
			node.dependencyFrom.remove(this);
		}
		dependencyTo.clear();
	}

	public final boolean equalsAllDependencyTo(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		final int dependencyToSize = dependencyTo.size();
		if (dependencyToSize != node.dependencyTo.size()) return false;
		final Map<Pair<Wrapper, IntsWrapper>, int[]> map = new HashMap<>(dependencyToSize);
		for (final Map.Entry<CppNode, int[]> entry : node.dependencyTo.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			final Pair<Wrapper, IntsWrapper> pair = Pair.immutableOf(wrapper, IntsWrapper.of(entry.getValue()));
			final int[] countWrapper = map.computeIfAbsent(pair, any -> new int[]{0});
			countWrapper[0] += 1;
		}
		for (final Map.Entry<CppNode, int[]> entry : dependencyTo.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			final Pair<Wrapper, IntsWrapper> pair = Pair.immutableOf(wrapper, IntsWrapper.of(entry.getValue()));
			final int[] countWrapper = map.get(pair);
			if (countWrapper == null) return false;
			if (--countWrapper[0] == 0) map.remove(pair);
		}
		return map.isEmpty();
	}

	//endregion

	//region Node Dependency From

	@Nonnull
	public final DependencyMap getNodeDependencyFrom(@Nonnull CppNode node) {
		return node.getNodeDependencyTo(this);
	}

	@Internal
	public final boolean addNodeDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyMap dependencyMap) {
		return node.addNodeDependencyTo(this, dependencyMap);
	}

	@Internal
	public final void removeNodeDependencyFrom(@Nonnull CppNode node) {
		node.removeNodeDependencyTo(this);
	}

	//endregion

	//region Node Dependency To

	@Nonnull
	public final DependencyMap getNodeDependencyTo(@Nonnull CppNode node) {
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts == null) return DependencyMap.ZERO;
		return new DependencyMap(counts);
	}

	@Internal
	public final boolean addNodeDependencyTo(@Nonnull CppNode node, @Nonnull DependencyMap dependencyMap) {
		checkReadOnly();
		if (node == this || getRoot() != node.getRoot()) return false;
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			// add it to old one
			for (final DependencyType type : DependencyType.values) {
				counts[type.ordinal()] += dependencyMap.getCount(type);
			}
		} else {
			// create new one
			final int[] newCounts = new int[DependencyType.values.size()];
			for (final DependencyType type : DependencyType.values) {
				newCounts[type.ordinal()] += dependencyMap.getCount(type);
			}
			if (!Arrays.equals(newCounts, DEPENDENCY_ZERO)) {
				// if not empty, put it in
				dependencyTo.put(node, newCounts);
				node.dependencyFrom.put(this, newCounts);
			}
		}
		return true;
	}

	@Internal
	public final void removeNodeDependencyTo(@Nonnull CppNode node) {
		checkReadOnly();
		dependencyTo.remove(node);
		node.dependencyFrom.remove(this);
	}

	//endregion

	//region Dependency From

	public final int getDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyType type) {
		return node.getDependencyTo(this, type);
	}

	@Internal
	public final boolean addDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyType type) {
		return node.addDependencyTo(this, type);
	}

	@Internal
	public final void removeDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyType type) {
		node.removeDependencyTo(this, type);
	}

	//endregion

	//region Dependency To

	public final int getDependencyTo(@Nonnull CppNode node, @Nonnull DependencyType type) {
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		return counts != null ? counts[type.ordinal()] : 0;
	}

	@Internal
	public final boolean addDependencyTo(@Nonnull CppNode node, @Nonnull DependencyType type) {
		checkReadOnly();
		if (node == this || getRoot() != node.getRoot()) return false;
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			counts[type.ordinal()] += 1;
		} else {
			final int[] newCounts = new int[DependencyType.values.size()];
			newCounts[type.ordinal()] += 1;
			dependencyTo.put(node, newCounts);
			node.dependencyFrom.put(this, newCounts);
		}
		return true;
	}

	@Internal
	public final void removeDependencyTo(@Nonnull CppNode node, @Nonnull DependencyType type) {
		checkReadOnly();
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null && counts[type.ordinal()] > 0) {
			counts[type.ordinal()] -= 1;
			if (Arrays.equals(counts, DEPENDENCY_ZERO)) {
				dependencyTo.remove(node);
				node.dependencyFrom.remove(this);
			}
		}
	}

	//endregion

	//endregion

	//region Object Helper

	void internalLock(@Nonnull Map<String, String> stringPool, @Nonnull Map<DependencyMap, DependencyMap> countsPool) {
		this.name = stringPool.computeIfAbsent(name, String::toString);
		this.uniqueName = stringPool.computeIfAbsent(uniqueName, String::toString);
		this.signature = stringPool.computeIfAbsent(signature, String::toString);
		this.children = List.copyOf(children);

		for (final Map.Entry<CppNode, int[]> entry : dependencyFrom.entrySet()) {
			final DependencyMap map = new DependencyMap(entry.getValue());
			entry.setValue(countsPool.computeIfAbsent(map, DependencyMap::identity).getDependencies());
		}
		this.dependencyFrom = Map.copyOf(dependencyFrom);

		for (final Map.Entry<CppNode, int[]> entry : dependencyTo.entrySet()) {
			final DependencyMap map = new DependencyMap(entry.getValue());
			entry.setValue(countsPool.computeIfAbsent(map, DependencyMap::identity).getDependencies());
		}
		this.dependencyTo = Map.copyOf(dependencyTo);

		this.writable = false;
	}

	final void checkReadOnly() {
		if (!writable) throw new UnsupportedOperationException("Read-only Node!");
	}

	final boolean isWritable() {
		return writable;
	}

	@Nullable
	static <E> E castNullable(@Nullable Object object, @Nonnull Class<E> checkingClass) throws InvalidObjectException {
		if (object != null && !checkingClass.isInstance(object)) {
			throw new InvalidObjectException("Expecting null or object with class " + checkingClass.getSimpleName());
		}
		return checkingClass.cast(object);
	}

	@Nonnull
	static <E> E castNonnull(@Nullable Object object, @Nonnull Class<E> checkingClass) throws InvalidObjectException {
		if (!checkingClass.isInstance(object)) {
			throw new InvalidObjectException("Expecting object with class " + checkingClass.getSimpleName());
		}
		return checkingClass.cast(object);
	}

	@Override
	public void writeExternal(@Nonnull ObjectOutput output) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
	}

	@Override
	public void readExternal(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
	}

	void write(@Nonnull ObjectOutput output) throws IOException {
		output.writeInt(id);
		output.writeObject(name);
		output.writeObject(uniqueName);
		output.writeObject(signature);

		output.writeInt(children.size());
		for (final CppNode childNode : children) {
			output.writeObject(childNode);
		}

		output.writeInt(dependencyFrom.size());
		for (final Map.Entry<CppNode, int[]> entry : dependencyFrom.entrySet()) {
			output.writeObject(entry.getKey());
			output.writeObject(entry.getValue());
//			for (int count : entry.getValue()) output.writeInt(count);
		}
	}

	void read(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		this.writable = false;

		this.id = input.readInt();
		this.name = castNonnull(input.readObject(), String.class);
		this.uniqueName = castNonnull(input.readObject(), String.class);
		this.signature = castNonnull(input.readObject(), String.class);

		final int childrenSize = input.readInt();
		final CppNode[] children = new CppNode[childrenSize];
		for (int i = 0; i < childrenSize; i++) {
			final CppNode child = castNonnull(input.readObject(), CppNode.class);
			children[i] = child;
			child.parent = this;
		}
		this.children = List.of(children);

		final int dependencySize = input.readInt();
		for (int i = 0; i < dependencySize; i++) {
			final CppNode dependingNode = castNonnull(input.readObject(), CppNode.class);
			final int[] dependencyCounts = castNonnull(input.readObject(), int[].class);
//			final int[] dependencyCounts = DEPENDENCY_ZERO.clone();
//			for (int j = 0; j < dependencyCounts.length; j++) {
//				dependencyCounts[j] = input.readInt();
//			}

			dependencyFrom.put(dependingNode, dependencyCounts);
			dependingNode.dependencyTo.put(this, dependencyCounts);
		}
	}

	//endregion Object Helper

	//region Node Comparator

	/**
	 * If two nodes have the similar prototype, aka same type.
	 *
	 * @param node node to compare
	 * @param matcher node matcher
	 * @return result
	 */
	protected boolean isPrototypeSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return this == node || getClass() == node.getClass();
	}

	protected int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		return getClass().hashCode();
	}

	/**
	 * If two nodes have the same prototype, aka same name and same type.
	 *
	 * @param node node to compare
	 * @param matcher node matcher
	 * @return result
	 */
	protected boolean isPrototypeIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return this == node || getClass() == node.getClass()
				&& name.equals(node.name)
				&& signature.equals(node.signature);
	}

	protected int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = getClass().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + signature.hashCode();
		return result;
	}

	/**
	 * If two nodes are similar, aka same name, same prototype, same parent. Only happen when two nodes are in different
	 * trees with the same structure, or they are the same object.
	 *
	 * @param node node to compare
	 * @param matcher node matcher
	 * @return result
	 */
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return this == node || getClass() == node.getClass()
				&& name.equals(node.name)
				&& uniqueName.equals(node.uniqueName)
				&& signature.equals(node.signature)
				&& matcher.isNodeMatch(parent, node.parent, MatchLevel.SIMILAR);
	}

	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = getClass().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + uniqueName.hashCode();
		result = 31 * result + signature.hashCode();
		result = 31 * result + matcher.nodeHashcode(parent, MatchLevel.SIMILAR);
		return result;
	}

	/**
	 * If two nodes are exactly the same, aka same name, same prototype, same parent, same content. Only happen when two
	 * nodes are in different trees with the same structure, or they are the same object.
	 *
	 * @param node node to compare
	 * @param matcher node matcher
	 * @return result
	 */
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return this == node || getClass() == node.getClass()
				&& name.equals(node.name)
				&& uniqueName.equals(node.uniqueName)
				&& signature.equals(node.signature)
				&& matcher.isNodeMatch(parent, node.parent, MatchLevel.SIMILAR)
				&& equalsAllDependencyTo(node, matcher);
	}

	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = getClass().hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + uniqueName.hashCode();
		result = 31 * result + signature.hashCode();
		result = 31 * result + matcher.nodeHashcode(parent, MatchLevel.SIMILAR);
		result = 31 * result + dependencyTo.size();
		return result;
	}

	public enum MatchLevel {
		PROTOTYPE_SIMILAR(CppNode::isPrototypeSimilar, CppNode::prototypeSimilarHashcode),
		PROTOTYPE_IDENTICAL(CppNode::isPrototypeIdentical, CppNode::prototypeIdenticalHashcode),
		SIMILAR(CppNode::isSimilar, CppNode::similarHashcode),
		IDENTICAL(CppNode::isIdentical, CppNode::identicalHashcode);

		static final MatchLevel[] values = values();

		@Nonnull private final InternalMatcher matcher;
		@Nonnull private final InternalHasher hasher;

		MatchLevel(@Nonnull InternalMatcher matcher, @Nonnull InternalHasher hasher) {
			this.matcher = matcher;
			this.hasher = hasher;
		}
	}

	public static final class Matcher {
		@Nonnull private final Map<CppNode, int[]> hashcodeMap = new IdentityHashMap<>();

		@Nonnull private final Deque<Map<MatchLevel, Set<Pair<CppNode, CppNode>>>> positiveCacheStack;
		@Nonnull private final Map<MatchLevel, Set<Pair<CppNode, CppNode>>> negativeCache;

		public Matcher() {
			this.positiveCacheStack = new ArrayDeque<>(List.of(new EnumMap<>(MatchLevel.class)));
			this.negativeCache = new EnumMap<>(MatchLevel.class);
		}

		public boolean isNodeMatch(@Nullable CppNode nodeA, @Nullable CppNode nodeB, @Nonnull MatchLevel level) {
			if (nodeA == nodeB) return true;
			if (nodeA == null || nodeB == null) return false;
			// This key will guarantee to work on when in same graph differ mode
			final Pair<CppNode, CppNode> key = nodeA.hashCode() <= nodeB.hashCode()
					? Pair.immutableOf(nodeA, nodeB)
					: Pair.immutableOf(nodeB, nodeA);

			// === Negative cache ===
			// negative cache hit on same or lower level -> not match
			for (final Map.Entry<MatchLevel, Set<Pair<CppNode, CppNode>>> entry : negativeCache.entrySet()) {
				final MatchLevel matchLevel = entry.getKey();
				final Set<Pair<CppNode, CppNode>> levelCache = entry.getValue();
				if (matchLevel.compareTo(level) <= 0 && levelCache.contains(key)) return false;
			}

			// === Layered positive cache ===
			// positive cache hit on same or higher level -> match
			for (final Map<MatchLevel, Set<Pair<CppNode, CppNode>>> positiveCache : positiveCacheStack) {
				for (Map.Entry<MatchLevel, Set<Pair<CppNode, CppNode>>> entry : positiveCache.entrySet()) {
					final MatchLevel matchLevel = entry.getKey();
					final Set<Pair<CppNode, CppNode>> levelCache = entry.getValue();
					if (matchLevel.compareTo(level) >= 0 && levelCache.contains(key)) return true;
				}
			}

			// ===  Calculate and compare ===
			// create new layer of positive cache
			final Map<MatchLevel, Set<Pair<CppNode, CppNode>>> newPositiveCache = new EnumMap<>(MatchLevel.class);
			positiveCacheStack.push(newPositiveCache);

			// create theory: current key is true
			newPositiveCache.put(level, new HashSet<>(List.of(key)));

			// prove the theory
			final boolean result = level.matcher.isNodeMatch(nodeA, nodeB, this);

			// remove new layer
			positiveCacheStack.pop();

			// check if the theory is incorrect
			if (!result) {
				// put the wrong theory to the negative cache
				negativeCache.computeIfAbsent(level, any -> new HashSet<>());
				for (final Map.Entry<MatchLevel, Set<Pair<CppNode, CppNode>>> entry : negativeCache.entrySet()) {
					final MatchLevel matchLevel = entry.getKey();
					final Set<Pair<CppNode, CppNode>> levelCache = entry.getValue();
					final int compare = matchLevel.compareTo(level);
					if (compare == 0) levelCache.add(key);
					if (compare > 0) levelCache.remove(key); // purely for memory and anti-rehash
				}
				return false;
			}

			// theory is correct! -> combine the positive cache
			final Map<MatchLevel, Set<Pair<CppNode, CppNode>>> positiveCache = positiveCacheStack.peek();
			assert positiveCache != null;
			// for each level of the new positive cache...
			for (final Map.Entry<MatchLevel, Set<Pair<CppNode, CppNode>>> newEntry : newPositiveCache.entrySet()) {
				final MatchLevel newMatchLevel = newEntry.getKey();
				final Set<Pair<CppNode, CppNode>> newLevelCache = newEntry.getValue();
				// ... with each level of the current positive cache...
				for (final Map.Entry<MatchLevel, Set<Pair<CppNode, CppNode>>> entry : positiveCache.entrySet()) {
					final MatchLevel matchLevel = entry.getKey();
					final Set<Pair<CppNode, CppNode>> levelCache = entry.getValue();
					final int compare = matchLevel.compareTo(newMatchLevel);
					if (compare < 0) {
						// if the level of the current positive cache is lower than the level of the new positive cache
						// remove all positive cached key that exist but has lower lever than current positive cache
						levelCache.removeAll(newLevelCache);
					} else if (compare == 0) {
						// if the current positive cache and the new positive cache is at the same level
						// add all positive cached key from the new positive cache to the current positive cache
						levelCache.addAll(newLevelCache);
					}
				}
			}
			return true;
		}

		public int nodeHashcode(@Nullable CppNode node, @Nonnull MatchLevel level) {
			if (node == null) return 0;
			final int[] hashcodes = hashcodeMap.get(node);
			if (hashcodes == null) {
				final int[] newHashcodes = new int[MatchLevel.values.length];
				final int newHashcode = level.hasher.nodeHashcode(node, this);
				newHashcodes[level.ordinal()] = newHashcode;
				hashcodeMap.put(node, newHashcodes);
				return newHashcode;
			} else if (hashcodes[level.ordinal()] == 0) {
				final int newHashcode = level.hasher.nodeHashcode(node, this);
				hashcodes[level.ordinal()] = newHashcode;
				return newHashcode;
			} else {
				return hashcodes[level.ordinal()];
			}
		}

		public boolean isNodesMatchUnordered(@Nonnull Collection<CppNode> nodesA, @Nonnull Collection<CppNode> nodesB,
				@Nonnull MatchLevel level) {
			final int size = nodesA.size();
			if (size != nodesB.size()) return false;
			final Map<Wrapper, int[]> map = new HashMap<>(size);
			for (final CppNode nodeA : nodesA) {
				final Wrapper wrapper = new Wrapper(nodeA, level, this);
				final int[] countWrapper = map.computeIfAbsent(wrapper, any -> new int[]{0});
				countWrapper[0] += 1;
			}
			for (final CppNode nodeB : nodesB) {
				final Wrapper wrapper = new Wrapper(nodeB, level, this);
				final int[] countWrapper = map.get(wrapper);
				if (countWrapper == null) return false;
				if (--countWrapper[0] == 0) map.remove(wrapper);
			}
			return map.isEmpty();
		}

		public boolean isNodesMatchOrdered(@Nonnull Collection<CppNode> nodesA, @Nonnull Collection<CppNode> nodesB,
				@Nonnull MatchLevel level) {
			final Iterator<CppNode> iteratorA = nodesA.iterator();
			final Iterator<CppNode> iteratorB = nodesB.iterator();
			while (iteratorA.hasNext() == iteratorB.hasNext()) {
				if (!iteratorA.hasNext()) return true;
				if (!isNodeMatch(iteratorA.next(), iteratorB.next(), level)) break;
			}
			return false;
		}
	}

	private interface InternalMatcher {
		boolean isNodeMatch(@Nonnull CppNode nodeA, @Nonnull CppNode nodeB, @Nonnull Matcher matcher);
	}

	private interface InternalHasher {
		int nodeHashcode(@Nonnull CppNode node, @Nonnull Matcher matcher);
	}

	public static final class Wrapper {
		@Nonnull private final CppNode node;
		@Nonnull private final MatchLevel level;
		@Nonnull private final Matcher matcher;
		private final int hashcode;

		public Wrapper(@Nonnull CppNode node, @Nonnull MatchLevel level, @Nonnull Matcher matcher) {
			this.node = node;
			this.level = level;
			this.matcher = matcher;
			this.hashcode = matcher.nodeHashcode(node, level);
		}

		@Nonnull
		public CppNode getNode() {
			return node;
		}

		@Nonnull
		public MatchLevel getLevel() {
			return level;
		}

		@Nonnull
		public Matcher getMatcher() {
			return matcher;
		}

		@Override
		public int hashCode() {
			return hashcode;
		}

		@Override
		public boolean equals(Object object) {
			return this == object || object instanceof Wrapper
					&& hashcode == ((Wrapper) object).hashcode
					&& matcher.isNodeMatch(node, ((Wrapper) object).node, level);
		}
	}

	//endregion

	//region TreeNode

	/**
	 * Return the root node.
	 *
	 * @return root node
	 */
	@Nonnull
	public final CppNode getRoot() {
		return rootNode != null ? rootNode : (this.rootNode = parent != null ? parent.getRoot() : this);
	}

	/**
	 * Check if this node is root node. Note: a node without parent is a root node.
	 *
	 * @return true if this node is root node
	 */
	public final boolean isRoot() {
		return parent == null;
	}

	/**
	 * Get parent node, or null if there is none. Note: a node without parent is a root node.
	 *
	 * @return parent node
	 */
	@Nullable
	public final CppNode getParent() {
		return parent;
	}

	/**
	 * @param node target node
	 * @return true if this node is a descendant
	 */
	public final boolean isAncestorOf(@Nonnull CppNode node) {
		return node.parent == this || node.parent != null && isAncestorOf(node.parent);
	}

	@Nonnull
	final <E extends CppNode> List<E> getChildrenList(@Nonnull Class<E> aClass) {
		final List<E> list = new ArrayList<>(children.size());
		for (final CppNode child : children) if (aClass.isInstance(child)) list.add(aClass.cast(child));
		return list;
	}

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	public final List<CppNode> getChildren() {
		return isWritable() ? Collections.unmodifiableList(children) : children;
	}

	/**
	 * Remove children nodes from current node {@link #remove}
	 */
	@Internal
	public final void removeChildren() {
		checkReadOnly();
		if (children.isEmpty()) return;
		// remove children
		for (final Iterator<CppNode> iterator = children.iterator(); iterator.hasNext(); ) {
			final CppNode child = iterator.next();
			assert child.parent == this : "WRONG TREE CONSTRUCTION!";
			// remove child
			child.internalRemoveDependencyRecursive();
			child.setRootRecursive(null);
			child.parent = null;
			iterator.remove();
		}
	}

	private void internalRemoveDependencyRecursive() {
		internalTransferReference(null);
		removeAllDependency();
		for (final CppNode childNode : this) {
			childNode.internalTransferReference(null);
			childNode.removeAllDependency();
		}
	}

	private void setRootRecursive(@Nullable CppNode rootNode) {
		this.rootNode = rootNode;
		for (final CppNode child : children) child.setRootRecursive(rootNode);
	}

	/**
	 * Add child node to current node.
	 *
	 * @param child a child node to add
	 */
	@Internal
	public final void addChild(@Nonnull CppNode child) {
		checkReadOnly();
		// check if child node is root node or adding to itself
		final CppNode root = getRoot();
		assert child.parent == null && root != child;
		children.add(child);
		child.parent = this;
		child.setRootRecursive(root);
	}

	/**
	 * Remove a child node from current node. otherwise.
	 *
	 * @param child a child node to removeFromParent
	 */
	@Internal
	public final void removeChild(@Nonnull CppNode child) {
		checkReadOnly();
		// check if current node is not parent node of child node
		assert child.parent == this;
		assert children.contains(child) : "WRONG TREE CONSTRUCTION!";
		// remove child
		child.internalRemoveDependencyRecursive();
		internalRemoveChild(child);
	}

	/**
	 * Remove this node itself from its parent node
	 */
	@Internal
	public final void remove() {
		checkReadOnly();
		// check if current node is root node
		assert parent != null;
		internalRemoveDependencyRecursive();
		parent.internalRemoveChild(this);
	}

	// Exactly the same as remove child, without checking input
	private void internalRemoveChild(@Nonnull CppNode child) {
		assert child.parent == this && children.contains(child);
		child.parent = null;
		child.setRootRecursive(null);
		children.remove(child);
	}

	/**
	 * Transfer node to another node and remove itself.
	 *
	 * @param node destination node
	 */
	@Internal
	@SuppressWarnings("AssertWithSideEffects")
	public final void transfer(@Nonnull CppNode node) {
		checkReadOnly();
		// check if current node is root node or child node is not root node
		final CppNode root = getRoot();
		assert root == node.getRoot() && !isAncestorOf(node);
		internalTransferReference(node);
		transferAllDependency(node);
		if (!children.isEmpty()) {
			node.children.addAll(children);
			for (final CppNode child : children) child.parent = node;
			children.clear();
		}
		assert parent != null;
		parent.children.remove(this);
		this.parent = null;
	}

	private void internalTransferReference(@Nullable CppNode toNode) {
		for (final CppNode dependencyNode : getAllDependencyFrom()) {
			dependencyNode.internalOnTransfer(this, toNode);
		}
		for (final CppNode dependencyNode : getAllDependencyTo()) {
			dependencyNode.internalOnTransfer(this, toNode);
		}
	}

	void internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
	}

	@Internal
	public final void collapse() {
		checkReadOnly();
		for (final CppNode childNode : this) {
			childNode.internalTransferReference(null);
			childNode.transferAllDependency(this);
		}
		// fast remove child without transfer ref
		for (final Iterator<CppNode> iterator = children.iterator(); iterator.hasNext(); ) {
			final CppNode child = iterator.next();
			assert child.parent == this : "WRONG TREE CONSTRUCTION!";
			// remove child
			child.setRootRecursive(null);
			child.parent = null;
			iterator.remove();
		}
	}

	@Internal
	public final void collapseToParent() {
		checkReadOnly();
		assert parent != null;
		internalTransferReference(null);
		transferAllDependency(parent);
		for (final CppNode childNode : this) {
			childNode.internalTransferReference(null);
			childNode.transferAllDependency(parent);
		}
		// fast remove child without transfer ref
		for (final Iterator<CppNode> iterator = children.iterator(); iterator.hasNext(); ) {
			final CppNode child = iterator.next();
			assert child.parent == this : "WRONG TREE CONSTRUCTION!";
			// remove child
			child.setRootRecursive(null);
			child.parent = null;
			iterator.remove();
		}
		parent.children.remove(this);
		this.parent = null;
	}

	/**
	 * Move this node to a new parent.
	 */
	@Internal
	@SuppressWarnings("AssertWithSideEffects")
	public final void move(@Nonnull CppNode newParent) {
		checkReadOnly();
		assert parent != null && getRoot() == newParent.getRoot() && !isAncestorOf(newParent);
		parent.children.remove(this);
		newParent.children.add(this);
		this.parent = newParent;
	}

	//region toString
	@Nonnull
	private String innerHeaderString() {
		return "\"class\": \"" + getClass().getSimpleName()
				+ "\", \"id\": " + id
				+ ", \"name\": \"" + name
				+ "\", \"uniqueName\": \"" + uniqueName
				+ "\", \"signature\": \"" + signature + "\"";
	}


	@Nonnull
	public final String toString() {
		return "{ " + innerHeaderString() + " }";
	}

	@Nonnull
	String partialElementString() {
		return "";
	}

	@Nonnull
	private String innerElementString() {
		return innerHeaderString()
				+ ", \"dependencyFrom\": " + Utilities.mapToString(dependencyFrom, null, CppNode::countsToString)
				+ ", \"dependencyTo\": " + Utilities.mapToString(dependencyTo, null, CppNode::countsToString)
				+ partialElementString();
	}

	@Nonnull
	public final String toTreeString() {
		final StringBuilder builder = new StringBuilder();
		internalToString(builder, 0);
		return builder.toString();
	}

	// Build tree to string internally by using a StringBuilder.
	private void internalToString(@Nonnull StringBuilder builder, int level) {
		final String alignString = "\t".repeat(level);
		builder.append(alignString).append("{ ")
				.append(innerElementString().replace("\n", "\n" + alignString));
		if (children.isEmpty()) {
			builder.append(" }");
		} else {
			builder.append(", \"children\": [\n");

			children.get(0).internalToString(builder, level + 1);
			for (int i = 1; i < children.size(); i++) {
				builder.append(",\n");
				children.get(i).internalToString(builder, level + 1);
			}

			builder.append('\n').append(alignString).append("]}");
		}
	}
	//endregion

	/**
	 * Return this tree iterator
	 *
	 * @return the iterator
	 */
	@Nonnull
	@Override
	public final Iterator<CppNode> iterator() {
		return new NodeIterator(this, null);
	}

	/**
	 * Return this tree iterator and auto skip the specified node
	 *
	 * @return the iterator
	 */
	@Nonnull
	private Iterator<CppNode> skippedIterator(@Nonnull CppNode skippedNode) {
		if (this == skippedNode) return Collections.emptyIterator();
		return new NodeIterator(this, skippedNode);
	}

	private static final class NodeIterator implements Iterator<CppNode> {
		@Nonnull private final Deque<Iterator<CppNode>> stack = new ArrayDeque<>();
		@Nullable private final CppNode skippedNode;
		@Nonnull private CppNode currentMode;
		private boolean available;

		NodeIterator(@Nonnull CppNode startNode, @Nullable CppNode skippedNode) {
			this.currentMode = startNode;
			this.skippedNode = skippedNode;
		}

		@Override
		public boolean hasNext() {
			if (available) return true;
			stack.push(currentMode.children.iterator());
			while (true) {
				final Iterator<CppNode> iterator = stack.peek();
				if (iterator == null) return false;
				while (iterator.hasNext()) {
					final CppNode node = iterator.next();
					if (node != skippedNode) {
						this.currentMode = node;
						this.available = true;
						return true;
					}
				}
				stack.pop();
			}
		}

		@Nonnull
		@Override
		public CppNode next() {
			if (available || hasNext()) {
				this.available = false;
				return currentMode;
			}
			throw new NoSuchElementException();
		}
	}

	//endregion
}
