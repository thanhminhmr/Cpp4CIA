package mrmathami.cia.cpp.ast;

import mrmathami.util.Pair;
import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
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
import java.util.Stack;

/**
 * Base of AST Tree.
 */
public abstract class Node implements Serializable, Iterable<Node> {
	private static final long serialVersionUID = 5538494159157921442L;

	@Nonnull
	private static final int[] DEPENDENCY_ZERO = new int[DependencyType.values.length];

	@Nonnull
	private String name = "";
	@Nonnull
	private String uniqueName = "";
	@Nonnull
	private String signature = "";

	private float weight;

	@Nonnull
	private Map<Node, int[]> dependencyFrom = new IdentityHashMap<>();
	@Nonnull
	private Map<Node, int[]> dependencyTo = new IdentityHashMap<>();

	@Nullable
	private Node parent;
	@Nonnull
	private List<Node> children = new LinkedList<>();

	protected Node() {
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
	@Nonnull
	final <E extends Node> List<E> getChildrenList(@Nonnull Class<E> aClass) {
		final List<E> list = new ArrayList<>(children.size());
		for (final Node child : children) {
			if (aClass.isInstance(child)) {
				list.add(aClass.cast(child));
			}
		}
		return list;
	}

	public final int getId() {
		return System.identityHashCode(this);
	}

	@Nonnull
	public final String getName() {
		return name;
	}

	@Nonnull
	public final Node setName(@Nonnull String name) {
		this.name = name;
		return this;
	}

	@Nonnull
	public final String getUniqueName() {
		return uniqueName;
	}

	@Nonnull
	public final Node setUniqueName(@Nonnull String uniqueName) {
		this.uniqueName = uniqueName;
		return this;
	}

	@Nonnull
	public final String getSignature() {
		return signature;
	}

	@Nonnull
	public final Node setSignature(@Nonnull String signature) {
		this.signature = signature;
		return this;
	}

	public final float getWeight() {
		return weight;
	}

	@Nonnull
	public final Node setWeight(float weight) {
		this.weight = weight;
		return this;
	}
	//</editor-fold>

	//<editor-fold desc="Dependency">

	//<editor-fold desc="All Dependency">

	public final void transferAllDependency(@Nonnull Node node) {
		transferAllDependencyFrom(node);
		transferAllDependencyTo(node);
	}

	public final void removeAllDependency() {
		removeAllDependencyFrom();
		removeAllDependencyTo();
	}

	public final boolean equalsAllDependency(@Nonnull Node node, @Nonnull Matcher matcher) {
		return equalsAllDependencyFrom(node, matcher) && equalsAllDependencyTo(node, matcher);
	}
	//</editor-fold>

	//<editor-fold desc="All Dependency From">

	@Nonnull
	public final List<Node> getAllDependencyFrom() {
		return List.copyOf(dependencyFrom.keySet());
	}

	public final void transferAllDependencyFrom(@Nonnull Node node) {
		for (final Iterator<Map.Entry<Node, int[]>> iterator = dependencyFrom.entrySet().iterator(); iterator.hasNext(); ) {
			final Map.Entry<Node, int[]> entry = iterator.next();
			final Node fromNode = entry.getKey();
			final Map<Node, int[]> fromNodeDependencyTo = fromNode.dependencyTo;
			final int[] oldCounts = fromNodeDependencyTo.remove(this);
			assert oldCounts != null : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			assert oldCounts == entry.getValue() : "WRONG TREE DEPENDENCY CONSTRUCTION!";
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

	public final void removeAllDependencyFrom() {
		for (final Node node : dependencyFrom.keySet()) {
			node.dependencyTo.remove(this);
		}
		dependencyFrom.clear();
	}

	public final boolean equalsAllDependencyFrom(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (dependencyFrom.size() != node.dependencyFrom.size()) return false;
		final HashMap<Wrapper, int[]> nodeDependencyFrom = new HashMap<>();
		for (final Map.Entry<Node, int[]> entry : node.dependencyFrom.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			nodeDependencyFrom.put(wrapper, entry.getValue());
		}
		for (final Map.Entry<Node, int[]> entry : dependencyFrom.entrySet()) {
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
	public final List<Node> getAllDependencyTo() {
		return List.copyOf(dependencyTo.keySet());
	}

	public final void transferAllDependencyTo(@Nonnull Node node) {
		for (final Iterator<Map.Entry<Node, int[]>> iterator = dependencyTo.entrySet().iterator(); iterator.hasNext(); ) {
			final Map.Entry<Node, int[]> entry = iterator.next();
			final Node toNode = entry.getKey();
			final Map<Node, int[]> toNodeDependencyFrom = toNode.dependencyFrom;
			final int[] oldCounts = toNodeDependencyFrom.remove(this);
			assert oldCounts != null : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			assert oldCounts == entry.getValue() : "WRONG TREE DEPENDENCY CONSTRUCTION!";
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

	public final void removeAllDependencyTo() {
		for (final Node node : dependencyTo.keySet()) {
			node.dependencyFrom.remove(this);
		}
		dependencyTo.clear();
	}

	public final boolean equalsAllDependencyTo(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (dependencyTo.size() != node.dependencyTo.size()) return false;
		final HashMap<Wrapper, int[]> nodeDependencyTo = new HashMap<>();
		for (final Map.Entry<Node, int[]> entry : node.dependencyTo.entrySet()) {
			final Wrapper wrapper = new Wrapper(entry.getKey(), MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			nodeDependencyTo.put(wrapper, entry.getValue());
		}
		for (final Map.Entry<Node, int[]> entry : dependencyTo.entrySet()) {
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
	public final Map<DependencyType, Integer> getNodeDependencyFrom(@Nonnull Node node) {
		return node.getNodeDependencyTo(this);
	}

	public final void addNodeDependencyFrom(@Nonnull Node node, @Nonnull Map<DependencyType, Integer> dependencyMap) {
		node.addNodeDependencyTo(this, dependencyMap);
	}

	public final void removeNodeDependencyFrom(@Nonnull Node node) {
		node.removeNodeDependencyTo(this);
	}
	//</editor-fold>

	//<editor-fold desc="Node Dependency To">
	@Nonnull
	public final Map<DependencyType, Integer> getNodeDependencyTo(@Nonnull Node node) {
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts == null) return Map.of();

		final Map<DependencyType, Integer> map = new EnumMap<>(DependencyType.class);
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] != 0) map.put(DependencyType.values[i], counts[i]);
		}
		return map;
	}

	public final void addNodeDependencyTo(@Nonnull Node node, @Nonnull Map<DependencyType, Integer> dependencyMap) {
		if (node == this) return;
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
	}

	public final void removeNodeDependencyTo(@Nonnull Node node) {
		dependencyTo.remove(node);
		node.dependencyFrom.remove(this);
	}
	//</editor-fold>

	//<editor-fold desc="Dependency From">
	public final int getDependencyFrom(@Nonnull Node node, @Nonnull DependencyType type) {
		return node.getDependencyTo(this, type);
	}

	public final void addDependencyFrom(@Nonnull Node node, @Nonnull DependencyType type) {
		node.addDependencyTo(this, type);
	}

	public final void removeDependencyFrom(@Nonnull Node node, @Nonnull DependencyType type) {
		node.removeDependencyTo(this, type);
	}
	//</editor-fold>

	//<editor-fold desc="Dependency To">
	public final int getDependencyTo(@Nonnull Node node, @Nonnull DependencyType type) {
		final int[] counts = dependencyTo.get(node);
		assert counts == node.dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		return counts != null ? counts[type.ordinal()] : 0;
	}

	public final void addDependencyTo(@Nonnull Node node, @Nonnull DependencyType type) {
		if (node == this) return;
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
	}

	public final void removeDependencyTo(@Nonnull Node node, @Nonnull DependencyType type) {
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
	// Prevent serialize empty object
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (parent == null && !(this instanceof RootNode)) {
			throw new IOException("Null parent!");
		}
		outputStream.defaultWriteObject();
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
	protected boolean isPrototypeSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
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
	protected boolean isPrototypeIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
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
	protected boolean isSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
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
	protected boolean isIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
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
		PROTOTYPE_SIMILAR(Node::isPrototypeSimilar, Node::prototypeSimilarHashcode),
		PROTOTYPE_IDENTICAL(Node::isPrototypeIdentical, Node::prototypeIdenticalHashcode),
		SIMILAR(Node::isSimilar, Node::similarHashcode),
		IDENTICAL(Node::isIdentical, Node::identicalHashcode);

		static final MatchLevel[] values = values();

		final InternalMatcher matcher;
		final InternalHasher hasher;

		MatchLevel(InternalMatcher matcher, InternalHasher hasher) {
			this.matcher = matcher;
			this.hasher = hasher;
		}
	}

	public static final class Matcher {
		private final Map<Pair<Node, Node>, MatchLevel> map = new HashMap<>();
		private final Map<Node, int[]> hashcodeMap = new IdentityHashMap<>();

		public Matcher() {
		}

		public final boolean isNodeMatch(@Nullable Node nodeA, @Nullable Node nodeB, @Nonnull MatchLevel level) {
			if (nodeA == nodeB) return true;
			if (nodeA == null || nodeB == null) return false;
			final Pair<Node, Node> pair = Pair.immutableOf(nodeA, nodeB);
			final MatchLevel oldLevel = map.get(pair);
			if (oldLevel == null || oldLevel.compareTo(level) < 0) {
				map.put(pair, level);
				return level.matcher.isNodeMatch(nodeA, nodeB, this);
			}
			return true;
		}

		public final int nodeHashcode(@Nullable Node node, @Nonnull MatchLevel level) {
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
		boolean isNodeMatch(@Nonnull Node nodeA, @Nonnull Node nodeB, @Nonnull Matcher matcher);
	}

	private interface InternalHasher {
		int nodeHashcode(@Nonnull Node node, @Nonnull Matcher matcher);
	}

	public static final class Wrapper {
		@Nonnull
		private final Node node;

		@Nonnull
		private final MatchLevel level;

		@Nonnull
		private final Matcher matcher;

		private final int hashcode;

		public Wrapper(@Nonnull Node node, @Nonnull MatchLevel level, @Nonnull Matcher matcher) {
			this.node = node;
			this.level = level;
			this.matcher = matcher;
			this.hashcode = matcher.nodeHashcode(node, level);
		}

		@Nonnull
		public final Node getNode() {
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
	public final Node getRoot() {
		Node parentNode, node = this;
		while ((parentNode = node.getParent()) != null) node = parentNode;
		return node;
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
	public final Node getParent() {
		return parent;
	}

	/**
	 * Set parent node, or null if there is none.
	 * Note: a node without parent is a root node.
	 *
	 * @param parent parent
	 */
	private void internalSetParent(@Nullable Node parent) {
		this.parent = parent;
	}

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	public final List<Node> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Remove children nodes from current node
	 * {@link #remove}
	 */
	public final void removeChildren() {
		if (children.isEmpty()) return;
		// remove children
		for (final Node child : children) {
			child.internalRemove();
		}
		children.clear();
	}

	/**
	 * Add child node to current node.
	 * Return false if child node already have parent node.
	 * Return true otherwise.
	 *
	 * @param child a child node to add
	 * @return whether the operation is success or not
	 */
	public final boolean addChild(@Nonnull Node child) {
		// check if child node is root node
		if (child.parent != null) return false;
		internalAddChild(child);
		return true;
	}

	private void internalAddChild(@Nonnull Node child) {
		assert child.parent == null;
		children.add(child);
		child.internalSetParent(this);
	}

	/**
	 * Remove a child node from current node.
	 * Return false if the child node doesn't belong to this node.
	 * Return true otherwise.
	 *
	 * @param child a child node to removeFromParent
	 * @return whether the operation is success or not
	 */
	public final boolean removeChild(@Nonnull Node child) {
		// check if current node is not parent node of child node
		if (child.parent != this) return false;
		assert children.contains(child) : "WRONG TREE CONSTRUCTION!";
		getRoot().internalTransferRecursive(child, null);
		internalRemoveChild(child);
		return true;
	}

	// Exactly the same as remove child, without checking input
	private void internalRemoveChild(@Nonnull Node child) {
		assert child.parent == this && children.contains(child);
		child.internalRemove();
		this.children.remove(child);
	}

	/**
	 * Remove this node itself from its parent node
	 */
	public final void remove() {
		// check if current node is root node
		if (parent == null) return;
		parent.getRoot().internalTransferRecursive(this, null);
		parent.internalRemoveChild(this);
	}

	// Without remove children from parent node!!
	private void internalRemove() {
		assert parent != null;
		this.removeChildren();
		this.removeAllDependency();
		this.internalSetParent(null);
	}

	/**
	 * Transfer node to another node
	 *
	 * @param node destination node
	 * @return false if current node is root or destination par
	 */
	public boolean transfer(@Nonnull Node node) {
		// check if current node is root node or child node is not root node
		if (getRoot() == node.getRoot()) return false;
		getRoot().internalTransferRecursive(this, node);
		transferAllDependency(node);
		if (!children.isEmpty()) {
			node.children.addAll(children);
			for (final Node child : children) {
				child.internalSetParent(node);
			}
			children.clear();
		}
		return true;
	}

	private void internalTransferRecursive(@Nonnull Node fromNode, @Nullable Node toNode) {
		// todo: link
		this.internalOnTransfer(fromNode, toNode);
		for (final Node child : children) {
			child.internalTransferRecursive(fromNode, toNode);
		}
	}

	protected void internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
	}

	//<editor-fold desc="toString">
	@Nonnull
	public final String toString() {
		return "(" + Utilities.objectIdentifyString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\" }";
	}

	@Nonnull
	protected String partialTreeElementString() {
		return "";
	}

	@Nonnull
	public final String toTreeElementString() {
		return "(" + Utilities.objectIdentifyString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", weight: " + weight
				+ ", dependencyFrom " + Utilities.mapToString(dependencyFrom, null, Node::countsToString)
				+ ", dependencyTo: " + Utilities.mapToString(dependencyTo, null, Node::countsToString)
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
	public final Iterator<Node> iterator() {
		return new NodeIterator(this);
	}

	/**
	 * The tree iterator
	 */
	private static final class NodeIterator implements Iterator<Node> {
		private Node current;

		private Stack<Iterator<Node>> iterators = new Stack<>();

		private NodeIterator(Node node) {
			this.iterators.push(node.children.iterator());
		}

		public final boolean hasNext() {
			if (current != null) {
				this.iterators.push(current.children.iterator());
				this.current = null;
			}
			do {
				if (iterators.peek().hasNext()) return true;
				this.iterators.pop();
			} while (!iterators.isEmpty());
			return false;
		}

		public final Node next() {
			this.current = iterators.peek().next();
			return current;
		}

		public final void remove() {
			if (current == null) throw new IllegalStateException();
			iterators.peek().remove();
			this.current = null;
		}
	}
	//</editor-fold>
}
