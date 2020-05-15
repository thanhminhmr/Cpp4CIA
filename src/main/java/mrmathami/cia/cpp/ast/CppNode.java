package mrmathami.cia.cpp.ast;

import mrmathami.util.Pair;
import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 * Base of AST Tree.
 */
public abstract class CppNode implements Serializable, Iterable<CppNode> {
	private static final long serialVersionUID = -7556411197265241247L;

	@Nonnull private static final int[] DEPENDENCY_ZERO = new int[DependencyType.values.length];

	private int id;

	@Nonnull private String name = "";
	@Nonnull private String uniqueName = "";
	@Nonnull private String signature = "";

	@Nullable private CppNode parent;
	@Nonnull private transient List<CppNode> children = new LinkedList<>();
	@Nonnull private transient Map<CppNode, int[]> dependencyFrom = new IdentityHashMap<>();
	@Nonnull private transient Map<CppNode, int[]> dependencyTo = new IdentityHashMap<>();

	private transient boolean writable = true; // should not access this directly
	@Nullable private transient CppNode rootNode; // should not access this directly

	CppNode() {
	}

	CppNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		this.name = name;
		this.uniqueName = uniqueName;
		this.signature = signature;
	}

	@Nonnull
	private static String countsToString(@Nonnull int[] counts) {
		final StringBuilder builder = new StringBuilder().append('{');
		for (int type = 0; type < counts.length; type++) {
			int typeCount = counts[type];
			if (typeCount != 0) {
				if (builder.length() > 1) builder.append(',');
				builder.append(' ').append(DependencyType.values[type]).append(": ").append(typeCount);
			}
		}
		if (builder.length() > 1) builder.append(' ');
		return builder.append('}').toString();
	}

	//<editor-fold desc="Node">

	public final int getId() {
		return id;
	}

	@Nonnull
	public final CppNode setId(int id) {
		checkReadOnly();
		this.id = id;
		return this;
	}

	@Nonnull
	public final String getName() {
		return name;
	}

	@Nonnull
	public final CppNode setName(@Nonnull String name) {
		checkReadOnly();
		this.name = name;
		return this;
	}

	@Nonnull
	public final String getUniqueName() {
		return uniqueName;
	}

	@Nonnull
	public final CppNode setUniqueName(@Nonnull String uniqueName) {
		checkReadOnly();
		this.uniqueName = uniqueName;
		return this;
	}

	@Nonnull
	public final String getSignature() {
		return signature;
	}

	@Nonnull
	public final CppNode setSignature(@Nonnull String signature) {
		checkReadOnly();
		this.signature = signature;
		return this;
	}

	//</editor-fold>

	//<editor-fold desc="Dependency">

	//<editor-fold desc="All Dependency">

	public final boolean transferAllDependency(@Nonnull CppNode node) {
		return transferAllDependencyFrom(node) && transferAllDependencyTo(node);
	}

	public final void removeAllDependency() {
		removeAllDependencyFrom();
		removeAllDependencyTo();
	}

	public final boolean equalsAllDependency(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return equalsAllDependencyFrom(node, matcher) && equalsAllDependencyTo(node, matcher);
	}

	//</editor-fold>

	//<editor-fold desc="All Dependency From">

	@Nonnull
	public final Set<CppNode> getAllDependencyFrom() {
		return isWritable() ? Collections.unmodifiableSet(dependencyFrom.keySet()) : dependencyFrom.keySet();
	}

	public final boolean transferAllDependencyFrom(@Nonnull CppNode node) {
		checkReadOnly();
		boolean isChanged = false;
		for (final Iterator<Map.Entry<CppNode, int[]>> iterator = dependencyFrom.entrySet().iterator(); iterator.hasNext(); ) {
			final Map.Entry<CppNode, int[]> entry = iterator.next();
			final CppNode fromNode = entry.getKey();
			final Map<CppNode, int[]> fromNodeDependencyTo = fromNode.dependencyTo;
			final int[] oldCounts = fromNodeDependencyTo.remove(this);
			assert oldCounts != null && oldCounts == entry.getValue() : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			if (fromNode != node) {
				isChanged = true;
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
		return isChanged;
	}

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

	//</editor-fold>

	//<editor-fold desc="All Dependency To">

	@Nonnull
	public final Set<CppNode> getAllDependencyTo() {
		return isWritable() ? Collections.unmodifiableSet(dependencyTo.keySet()) : dependencyTo.keySet();
	}

	public final boolean transferAllDependencyTo(@Nonnull CppNode node) {
		checkReadOnly();
		boolean isChanged = false;
		for (final Iterator<Map.Entry<CppNode, int[]>> iterator = dependencyTo.entrySet().iterator(); iterator.hasNext(); ) {
			final Map.Entry<CppNode, int[]> entry = iterator.next();
			final CppNode toNode = entry.getKey();
			final Map<CppNode, int[]> toNodeDependencyFrom = toNode.dependencyFrom;
			final int[] oldCounts = toNodeDependencyFrom.remove(this);
			assert oldCounts != null && oldCounts == entry.getValue() : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			if (toNode != node) {
				isChanged = true;
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
		return isChanged;
	}

	public final void removeAllDependencyTo() {
		checkReadOnly();
		for (final CppNode node : dependencyTo.keySet()) {
			node.dependencyFrom.remove(this);
		}
		dependencyTo.clear();
	}

	public final boolean equalsAllDependencyTo(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (dependencyTo.size() != node.dependencyTo.size()) return false;
		final HashMap<Wrapper, int[]> nodeDependencyTo = new HashMap<>();
		for (final Map.Entry<CppNode, int[]> entry : node.dependencyTo.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			nodeDependencyTo.put(wrapper, entry.getValue());
		}
		for (final Map.Entry<CppNode, int[]> entry : dependencyTo.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			final int[] counts = nodeDependencyTo.get(wrapper);
			if (counts == null || !Arrays.equals(counts, entry.getValue())) return false;
			nodeDependencyTo.remove(wrapper);
		}
		return nodeDependencyTo.isEmpty();
	}

	//</editor-fold>

	//<editor-fold desc="Node Dependency From">

	@Nonnull
	public final Map<DependencyType, Integer> getNodeDependencyFrom(@Nonnull CppNode node) {
		return node.getNodeDependencyTo(this);
	}

	public final boolean addNodeDependencyFrom(@Nonnull CppNode node, @Nonnull Map<DependencyType, Integer> dependencyMap) {
		return node.addNodeDependencyTo(this, dependencyMap);
	}

	public final void removeNodeDependencyFrom(@Nonnull CppNode node) {
		node.removeNodeDependencyTo(this);
	}

	//</editor-fold>

	//<editor-fold desc="Node Dependency To">

	@Nonnull
	public final Map<DependencyType, Integer> getNodeDependencyTo(@Nonnull CppNode node) {
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts == null) return Map.of();

		final Map<DependencyType, Integer> map = new EnumMap<>(DependencyType.class);
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] != 0) map.put(DependencyType.values[i], counts[i]);
		}
		return map;
	}

	public final boolean addNodeDependencyTo(@Nonnull CppNode node, @Nonnull Map<DependencyType, Integer> dependencyMap) {
		checkReadOnly();
		if (node == this || getRoot() != node.getRoot()) return false;
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			// add it to old one
			for (final Map.Entry<DependencyType, Integer> entry : dependencyMap.entrySet()) {
				counts[entry.getKey().ordinal()] += entry.getValue();
			}
		} else {
			// create new one
			final int[] newCounts = new int[DependencyType.values.length];
			for (final Map.Entry<DependencyType, Integer> entry : dependencyMap.entrySet()) {
				newCounts[entry.getKey().ordinal()] = entry.getValue();
			}
			if (!Arrays.equals(newCounts, DEPENDENCY_ZERO)) {
				// if not empty, put it in
				dependencyTo.put(node, newCounts);
				node.dependencyFrom.put(this, newCounts);
			}
		}
		return true;
	}

	public final void removeNodeDependencyTo(@Nonnull CppNode node) {
		checkReadOnly();
		dependencyTo.remove(node);
		node.dependencyFrom.remove(this);
	}

	//</editor-fold>

	//<editor-fold desc="Dependency From">

	public final int getDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyType type) {
		return node.getDependencyTo(this, type);
	}

	public final boolean addDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyType type) {
		return node.addDependencyTo(this, type);
	}

	public final void removeDependencyFrom(@Nonnull CppNode node, @Nonnull DependencyType type) {
		node.removeDependencyTo(this, type);
	}

	//</editor-fold>

	//<editor-fold desc="Dependency To">

	public final int getDependencyTo(@Nonnull CppNode node, @Nonnull DependencyType type) {
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		return counts != null ? counts[type.ordinal()] : 0;
	}

	public final boolean addDependencyTo(@Nonnull CppNode node, @Nonnull DependencyType type) {
		checkReadOnly();
		if (node == this || getRoot() != node.getRoot()) return false;
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			counts[type.ordinal()] += 1;
		} else {
			final int[] newCounts = new int[DependencyType.values.length];
			newCounts[type.ordinal()] += 1;
			dependencyTo.put(node, newCounts);
			node.dependencyFrom.put(this, newCounts);
		}
		return true;
	}

	public final void removeDependencyTo(@Nonnull CppNode node, @Nonnull DependencyType type) {
		checkReadOnly();
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			counts[type.ordinal()] = 0;
			if (Arrays.equals(counts, DEPENDENCY_ZERO)) {
				dependencyTo.remove(node);
				node.dependencyFrom.remove(this);
			}
		}
	}

	//</editor-fold>

	//</editor-fold>

	//<editor-fold desc="Object Helper">

	void internalLock() {
		this.name = name.intern();
		this.uniqueName = uniqueName.intern();
		this.signature = signature.intern();
		this.children = List.copyOf(children);
		this.dependencyFrom = Map.copyOf(dependencyFrom);
		this.dependencyTo = Map.copyOf(dependencyTo);
		this.writable = false;
	}

	final void checkReadOnly() {
		if (!writable) throw new UnsupportedOperationException("Read-only Node!");
	}

	final boolean isWritable() {
		return writable;
	}

	private void writeObject(@Nonnull ObjectOutputStream outputStream) throws IOException {
		outputStream.defaultWriteObject();
		if (writable) {
			outputStream.writeObject(List.copyOf(children));
			outputStream.writeObject(Map.copyOf(dependencyFrom));
			outputStream.writeObject(Map.copyOf(dependencyTo));
		} else {
			outputStream.writeObject(children);
			outputStream.writeObject(dependencyFrom);
			outputStream.writeObject(dependencyTo);
		}
	}

	@SuppressWarnings("unchecked")
	private void readObject(@Nonnull ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.children = (List<CppNode>) inputStream.readObject();
		this.dependencyFrom = (Map<CppNode, int[]>) inputStream.readObject();
		this.dependencyTo = (Map<CppNode, int[]>) inputStream.readObject();
		this.writable = false;
	}

	//</editor-fold>

	//<editor-fold desc="Node Comparator">

	/**
	 * If two nodes have the similar prototype, aka same type.
	 *
	 * @param node    node to compare
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
	 * @param node    node to compare
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
	 * If two nodes are similar, aka same name, same prototype, same parent.
	 * Only happen when two nodes are in different trees with
	 * the same structure, or they are the same object.
	 *
	 * @param node    node to compare
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
	 * If two nodes are exactly the same, aka same name, same prototype, same parent, same content.
	 * Only happen when two nodes are in different trees with
	 * the same structure, or they are the same object.
	 *
	 * @param node    node to compare
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
		@Nonnull private final Map<Pair<CppNode, CppNode>, MatchLevel> map = new HashMap<>();
		@Nonnull private final Map<CppNode, int[]> hashcodeMap = new IdentityHashMap<>();

		public Matcher() {
		}

		public final boolean isNodeMatch(@Nullable CppNode nodeA, @Nullable CppNode nodeB, @Nonnull MatchLevel level) {
			if (nodeA == nodeB) return true;
			if (nodeA == null || nodeB == null) return false;
			final Pair<CppNode, CppNode> pair = Pair.immutableOf(nodeA, nodeB);
			final MatchLevel oldLevel = map.get(pair);
			if (oldLevel == null || oldLevel.compareTo(level) < 0) {
				map.put(pair, level);
				return level.matcher.isNodeMatch(nodeA, nodeB, this);
			}
			return true;
		}

		public final int nodeHashcode(@Nullable CppNode node, @Nonnull MatchLevel level) {
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
		public final CppNode getNode() {
			return node;
		}

		@Nonnull
		public final MatchLevel getLevel() {
			return level;
		}

		@Nonnull
		public final Matcher getMatcher() {
			return matcher;
		}

		@Override
		public final int hashCode() {
			return hashcode;
		}

		@Override
		public final boolean equals(Object object) {
			return this == object || object instanceof Wrapper
					&& hashcode == ((Wrapper) object).hashcode
					&& matcher.isNodeMatch(node, ((Wrapper) object).node, level);
		}
	}

	//</editor-fold>

	//<editor-fold desc="TreeNode">

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
	 * Check if this node is root node.
	 * Note: a node without parent is a root node.
	 *
	 * @return true if this node is root node
	 */
	public final boolean isRoot() {
		return parent == null;
	}

	/**
	 * Get parent node, or null if there is none.
	 * Note: a node without parent is a root node.
	 *
	 * @return parent node
	 */
	@Nullable
	public final CppNode getParent() {
		return parent;
	}

	/**
	 * @param node target node
	 * @return true if this node is a decendant
	 */
	public final boolean isAncestorOf(@Nonnull CppNode node) {
		CppNode parent = node;
		do {
			parent = parent.parent;
			if (parent == this) return true;
		} while (parent != null);
		return false;
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

	private void internalRemoveDependencyRecursive() {
		removeAllDependency();
		getRoot().internalTransferRecursive(this, null);
		for (final CppNode child : children) child.internalRemoveDependencyRecursive();
	}

	/**
	 * Remove children nodes from current node
	 * {@link #remove}
	 */
	public final void removeChildren() {
		checkReadOnly();
		if (children.isEmpty()) return;
		// remove children
		for (final Iterator<CppNode> iterator = children.iterator(); iterator.hasNext(); ) {
			final CppNode child = iterator.next();
			assert child.parent == this : "WRONG TREE CONSTRUCTION!";
			// remove child
			child.internalRemoveDependencyRecursive();
			child.parent = null;
			child.rootNode = null;
			iterator.remove();
		}
	}

	private void setRootRecursive(@Nonnull CppNode rootNode) {
		this.rootNode = rootNode;
		for (final CppNode child : children) child.setRootRecursive(rootNode);
	}

	/**
	 * Add child node to current node.
	 * Return false if child node already have parent node.
	 * Return true otherwise.
	 *
	 * @param child a child node to add
	 * @return whether the operation is success or not
	 */
	public final boolean addChild(@Nonnull CppNode child) {
		checkReadOnly();
		// check if child node is root node or adding to itself
		if (child.parent != null || getRoot() == child) return false;
		children.add(child);
		child.parent = this;
		child.setRootRecursive(getRoot());
		return true;
	}

	/**
	 * Remove a child node from current node.
	 * Return false if the child node doesn't belong to this node.
	 * Return true otherwise.
	 *
	 * @param child a child node to removeFromParent
	 * @return whether the operation is success or not
	 */
	public final boolean removeChild(@Nonnull CppNode child) {
		checkReadOnly();
		// check if current node is not parent node of child node
		if (child.parent != this) return false;
		assert children.contains(child) : "WRONG TREE CONSTRUCTION!";
		// remove child
		internalRemoveChild(child);
		return true;
	}

	/**
	 * Remove this node itself from its parent node
	 */
	public final void remove() {
		checkReadOnly();
		// check if current node is root node
		if (parent == null) return;
		parent.internalRemoveChild(this);
	}

	// Exactly the same as remove child, without checking input
	private void internalRemoveChild(@Nonnull CppNode child) {
		assert child.parent == this && children.contains(child);
		child.internalRemoveDependencyRecursive();
		child.parent = null;
		child.rootNode = null;
		children.remove(child);
	}

	/**
	 * Transfer node to another node
	 *
	 * @param node destination node
	 * @return false if current node is root or destination par
	 */
	public final boolean transfer(@Nonnull CppNode node) {
		checkReadOnly();
		// check if current node is root node or child node is not root node
		if (getRoot() != node.getRoot() || isAncestorOf(node)) return false;
		boolean isChanged = transferAllDependency(node)
				|| getRoot().internalTransferRecursive(this, node);
		if (!children.isEmpty()) {
			isChanged = true;
			node.children.addAll(children);
			for (final CppNode child : children) child.parent = node;
			children.clear();
		}
		return isChanged;
	}

	private boolean internalTransferRecursive(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		boolean isChanged = internalOnTransfer(fromNode, toNode);
		for (final CppNode child : children) isChanged |= child.internalTransferRecursive(fromNode, toNode);
		return isChanged;
	}

	boolean internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		return false;
	}

	//<editor-fold desc="toString">
	@Nonnull
	public final String toString() {
		return "(" + Utilities.objectIdentifyString(this)
				+ ") { id: " + id
				+ ", name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\" }";
	}

	@Nonnull
	String partialTreeElementString() {
		return "";
	}

	@Nonnull
	public final String toTreeElementString() {
		return "(" + Utilities.objectIdentifyString(this)
				+ ") { id: " + id
				+ ", name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", dependencyFrom: " + Utilities.mapToString(dependencyFrom, null, CppNode::countsToString)
				+ ", dependencyTo: " + Utilities.mapToString(dependencyTo, null, CppNode::countsToString)
				+ partialTreeElementString()
				+ " }";
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
		if (children.isEmpty()) {
			builder.append(alignString).append("{ value: ")
					.append(this.toTreeElementString().replace("\n", "\n" + alignString)).append(" }");
		} else {
			builder.append(alignString).append("{ value: ")
					.append(this.toTreeElementString().replace("\n", "\n" + alignString)).append(", children: [\n");

			children.get(0).internalToString(builder, level + 1);
			for (int i = 1; i < children.size(); i++) {
				builder.append(",\n");
				children.get(i).internalToString(builder, level + 1);
			}

			builder.append('\n').append(alignString).append("]}");
		}
	}
	//</editor-fold>

	/**
	 * Return this tree iterator
	 *
	 * @return the iterator
	 */
	@Nonnull
	public final Iterator<CppNode> iterator() {
		final Stack<Iterator<CppNode>> stack = new Stack<>();
		stack.push(children.iterator());
		return new Iterator<>() {
			@Nullable private CppNode current;

			public final boolean hasNext() {
				if (current != null) {
					stack.push(current.children.iterator());
					this.current = null;
				}
				do {
					if (stack.peek().hasNext()) return true;
					stack.pop();
				} while (!stack.isEmpty());
				return false;
			}

			@Nonnull
			public final CppNode next() {
				return this.current = stack.peek().next();
			}
		};
	}

	//</editor-fold>
}
