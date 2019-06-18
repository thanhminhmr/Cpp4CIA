package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base of AST Tree.
 */
public abstract class Node implements INode {
	private static final long serialVersionUID = -7829002077653496113L;
	private static final int[] DEPENDENCY_ZERO = new int[DependencyType.values.length];

	private static final AtomicInteger ID_COUNTER = new AtomicInteger();

	private final int id;

	@Nonnull
	private final String name;

	@Nonnull
	private final String uniqueName;

	@Nonnull
	private final String signature;
	@Nonnull
	private final Map<INode, int[]> dependencyFrom = new HashMap<>();
	@Nonnull
	private final Map<INode, int[]> dependencyTo = new HashMap<>();
	private float directWeight;
	private float indirectWeight;
	@Nullable
	private INode parent;

	@Nonnull
	private List<INode> children = new ArrayList<>();

	protected Node(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		this.id = ID_COUNTER.incrementAndGet();
		this.name = name;
		this.uniqueName = uniqueName;
		this.signature = signature;
	}

	private static Node getNode(INode iNode) {
		if (iNode instanceof Node) return (Node) iNode;
		throw new IllegalStateException("Unexpected foreign node in tree.");
	}

	@Nonnull
	private static String countsToString(int[] counts) {
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

	//<editor-fold desc="Dependency Helper">
	private static void addDependency(@Nonnull Map<INode, int[]> dependency, @Nonnull INode node, @Nonnull DependencyType type) {
		final int[] counts = dependency.get(node);
		if (counts != null) {
			counts[type.ordinal()] += 1;
		} else {
			final int[] newCounts = new int[DependencyType.values.length];
			newCounts[type.ordinal()] += 1;
			dependency.put(node, newCounts);
		}
	}

	private static void removeDependency(@Nonnull Map<INode, int[]> dependency, @Nonnull INode node, @Nonnull DependencyType type) {
		final int[] counts = dependency.get(node);
		if (counts != null) {
			counts[type.ordinal()] = 0;
			if (Arrays.equals(counts, DEPENDENCY_ZERO)) {
				dependency.remove(node);
			}
		}
	}

	private static void replaceDependency(@Nonnull Map<INode, int[]> oldDependency, @Nonnull INode oldNode,
			@Nonnull Map<INode, int[]> newDependency, @Nonnull INode newNode) {
		final int[] oldCounts = oldDependency.get(oldNode);
		if (oldCounts == null) return;
		final int[] newCounts = newDependency.get(newNode);
		if (newCounts != null) {
			for (int type = 0; type < oldCounts.length; type++) {
				newCounts[type] += oldCounts[type];
			}
		} else {
			newDependency.put(newNode, oldCounts);
		}
		oldDependency.remove(oldNode);
	}
	//</editor-fold>

	//<editor-fold desc="Node">
	@Nonnull
	protected final <E extends INode> List<E> getChildrenList(final Class<E> aClass) {
		final List<INode> children = getChildren();
		final List<E> list = new ArrayList<>(children.size());
		for (final INode child : children) {
			if (aClass.isInstance(child)) {
				list.add(aClass.cast(child));
			}
		}
		return list;
	}

	@Override
	public final int getId() {
		return id;
	}

	@Override
	@Nonnull
	public final String getName() {
		return name;
	}

	@Override
	@Nonnull
	public final String getUniqueName() {
		return uniqueName;
	}

	@Override
	@Nonnull
	public final String getSignature() {
		return signature;
	}

	@Override
	public final float getDirectWeight() {
		return directWeight;
	}

	@Override
	public final void setDirectWeight(float directWeight) {
		this.directWeight = directWeight;
	}

	@Override
	public final float getIndirectWeight() {
		return indirectWeight;
	}

	@Override
	public final void setIndirectWeight(float indirectWeight) {
		this.indirectWeight = indirectWeight;
	}
	//</editor-fold>

	//<editor-fold desc="Dependency">

	//<editor-fold desc="All Dependency">

	@Override
	public final void transferAllDependency(@Nonnull final INode node) {
		transferAllDependencyFrom(node);
		transferAllDependencyTo(node);
	}

	@Override
	public final void removeAllDependency() {
		removeAllDependencyFrom();
		removeAllDependencyTo();
	}

	@Override
	public final boolean equalsAllDependency(@Nonnull final INode node) {
		return equalsAllDependencyFrom(node) && equalsAllDependencyTo(node);
	}
	//</editor-fold>

	//<editor-fold desc="All Dependency From">

	@Nonnull
	@Override
	public List<INode> getAllDependencyFrom() {
		return List.copyOf(dependencyFrom.keySet());
	}

	public final void transferAllDependencyFrom(@Nonnull INode node) {
		for (final INode toNode : dependencyFrom.keySet()) {
			final Map<INode, int[]> dependencyTo = getNode(toNode).dependencyTo;
			final int[] oldCounts = dependencyTo.remove(this);
			assert oldCounts != null : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			final int[] newCounts = dependencyTo.get(node);
			if (newCounts != null) {
				for (int i = 0; i < newCounts.length; i++) {
					newCounts[i] += oldCounts[i];
				}
			} else {
				dependencyTo.put(node, oldCounts);
			}
		}
		getNode(node).dependencyFrom.putAll(dependencyFrom);
		dependencyFrom.clear();
	}

	public final void removeAllDependencyFrom() {
		for (final INode node : dependencyFrom.keySet()) {
			getNode(node).dependencyTo.remove(this);
		}
		dependencyFrom.clear();
	}

	public final boolean equalsAllDependencyFrom(@Nonnull INode node) {
		return dependencyFrom.equals(getNode(node).dependencyFrom);
	}
	//</editor-fold>

	//<editor-fold desc="All Dependency To">

	@Nonnull
	@Override
	public List<INode> getAllDependencyTo() {
		return List.copyOf(dependencyTo.keySet());
	}

	@Override
	public final void transferAllDependencyTo(@Nonnull INode node) {
		for (final INode toNode : dependencyTo.keySet()) {
			final Map<INode, int[]> dependencyFrom = getNode(toNode).dependencyFrom;
			final int[] oldCounts = dependencyFrom.remove(this);
			assert oldCounts != null : "WRONG TREE DEPENDENCY CONSTRUCTION!";
			final int[] newCounts = dependencyFrom.get(node);
			if (newCounts != null) {
				for (int i = 0; i < newCounts.length; i++) {
					newCounts[i] += oldCounts[i];
				}
			} else {
				dependencyFrom.put(node, oldCounts);
			}
		}
		getNode(node).dependencyTo.putAll(dependencyTo);
		dependencyTo.clear();
	}

	@Override
	public final void removeAllDependencyTo() {
		for (final INode node : dependencyTo.keySet()) {
			getNode(node).dependencyFrom.remove(this);
		}
		dependencyTo.clear();
	}

	@Override
	public final boolean equalsAllDependencyTo(@Nonnull INode node) {
		return dependencyTo.equals(getNode(node).dependencyTo);
	}
	//</editor-fold>

	//<editor-fold desc="Node Dependency From">
	@Nonnull
	@Override
	public final Map<DependencyType, Integer> getNodeDependencyFrom(@Nonnull final INode node) {
		return getNode(node).getNodeDependencyTo(this);
	}

	@Override
	public final void addNodeDependencyFrom(@Nonnull final INode node, @Nonnull final Map<DependencyType, Integer> dependencyMap) {
		getNode(node).addNodeDependencyTo(this, dependencyMap);
	}

	@Override
	public final void removeNodeDependencyFrom(@Nonnull final INode node) {
		getNode(node).removeNodeDependencyTo(this);
	}
	//</editor-fold>

	//<editor-fold desc="Node Dependency To">
	@Nonnull
	@Override
	public final Map<DependencyType, Integer> getNodeDependencyTo(@Nonnull final INode node) {
		final int[] counts = dependencyTo.get(node);
		if (counts == null) return Map.of();

		final Map<DependencyType, Integer> map = new EnumMap<>(DependencyType.class);
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] != 0) map.put(DependencyType.values[i], counts[i]);
		}
		return map;
	}

	@Override
	public final void addNodeDependencyTo(@Nonnull final INode node, @Nonnull final Map<DependencyType, Integer> dependencyMap) {
		final int[] counts = dependencyTo.get(node);
		assert counts == getNode(node).dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
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
				getNode(node).dependencyFrom.put(this, newCounts);
			}
		}
	}

	@Override
	public final void removeNodeDependencyTo(@Nonnull final INode node) {
		dependencyTo.remove(node);
		getNode(node).dependencyFrom.remove(this);
	}
	//</editor-fold>

	//<editor-fold desc="Dependency From">
	@Override
	public final int getDependencyFrom(@Nonnull final INode node, @Nonnull final DependencyType type) {
		return getNode(node).getDependencyTo(this, type);
	}

	@Override
	public final void addDependencyFrom(@Nonnull final INode node, @Nonnull final DependencyType type) {
		getNode(node).addDependencyTo(this, type);
	}

	@Override
	public final void removeDependencyFrom(@Nonnull final INode node, @Nonnull final DependencyType type) {
		getNode(node).removeDependencyTo(this, type);
	}
	//</editor-fold>

	//<editor-fold desc="Dependency To">
	@Override
	public final int getDependencyTo(@Nonnull final INode node, @Nonnull final DependencyType type) {
		final int[] counts = dependencyTo.get(node);
		return counts != null ? counts[type.ordinal()] : 0;
	}

	@Override
	public final void addDependencyTo(@Nonnull final INode node, @Nonnull final DependencyType type) {
		final int[] counts = dependencyTo.get(node);
		assert counts == getNode(node).dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			counts[type.ordinal()] += 1;
		} else {
			final int[] newCounts = new int[DependencyType.values.length];
			newCounts[type.ordinal()] += 1;
			dependencyTo.put(node, newCounts);
			getNode(node).dependencyFrom.put(this, newCounts);
		}
	}

	@Override
	public final void removeDependencyTo(@Nonnull final INode node, @Nonnull final DependencyType type) {
		final int[] counts = dependencyTo.get(node);
		assert counts == getNode(node).dependencyFrom.get(this) : "WRONG TREE DEPENDENCY CONSTRUCTION!";
		if (counts != null) {
			counts[type.ordinal()] = 0;
			if (Arrays.equals(counts, DEPENDENCY_ZERO)) {
				removeNodeDependencyTo(node);
			}
		}
	}
	//</editor-fold>

	//</editor-fold>

	//<editor-fold desc="Object Helper">
	// Prevent serialize empty object
	private void writeObject(ObjectOutputStream out) throws IOException {
		if (this.getParent() == null && !(this instanceof IRoot)) {
			throw new IOException("Null parent!");
		}
		out.defaultWriteObject();
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;
		final Node node = (Node) object;
		return name.equals(node.name) && uniqueName.equals(node.uniqueName)
				&& signature.equals(node.signature) && Objects.equals(parent, node.parent);
	}

	@Override
	public int hashCode() {
		int result = getClass().hashCode();
		//noinspection ConstantConditions
		result = 31 * result + (name != null ? name.hashCode() : 0);
		//noinspection ConstantConditions
		result = 31 * result + (uniqueName != null ? uniqueName.hashCode() : 0);
		//noinspection ConstantConditions
		result = 31 * result + (signature != null ? signature.hashCode() : 0);
		return result;
	}
	//</editor-fold>

	//<editor-fold desc="TreeNode">

	/**
	 * Return the root node.
	 *
	 * @return root node
	 */
	@Nonnull
	@Override
	public final INode getRoot() {
		INode node = this;
		while (node.getParent() != null) node = node.getParent();
		return node;
	}

	/**
	 * Check if this node is root node.
	 * Note: a node without parent is a root node.
	 *
	 * @return true if this node is root node
	 */
	@Override
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
	@Override
	public final INode getParent() {
		return parent;
	}

	// Set parent node, or null if there is none.
	// Note: a node without parent is a root node.
	private void internalSetParent(@Nullable INode parent) {
		this.parent = parent;
	}

	/**
	 * Get list of children nodes, or empty list if there is none
	 *
	 * @return read-only list of children nodes
	 */
	@Nonnull
	@Override
	public final List<INode> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * Add child node to current node.
	 * Return false if child node already have parent node.
	 * Return true otherwise.
	 *
	 * @param child a child node to add
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean addChild(@Nonnull INode child) {
		// check if child node is root node
		if (!child.isRoot()) return false;

		children.add(child);
		getNode(child).internalSetParent(this);
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
	@Override
	public final boolean removeChild(@Nonnull INode child) {
		// check if current node is not parent node
		if (child.getParent() != this) return false;

		children.remove(child);
		getNode(child).internalSetParent(null);
		return true;
	}

	/**
	 * Replace a child node by another node from current node.
	 * Return false if the old child node doesn't belong to this node, or new child node already have parent.
	 * Return true otherwise.
	 *
	 * @param oldChild a child node to remove
	 * @param newChild a child node to add
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean replaceChild(@Nonnull INode oldChild, @Nonnull INode newChild) {
		// check if current node is not parent node
		if (oldChild.getParent() != this) return false;
		// check if child node is root node
		if (!newChild.isRoot()) return false;

		final int index = children.indexOf(oldChild);
		children.set(index, newChild);
		getNode(oldChild).internalSetParent(null);
		getNode(newChild).internalSetParent(this);
		return true;
	}

	/**
	 * Add children nodes to current node.
	 * Return false if one of children nodes already have parent node.
	 * Return true otherwise.
	 *
	 * @param newChildren children nodes to add
	 * @return whether the operation is success or not
	 */
	@Override
	public final <E extends INode> boolean addChildren(@Nonnull List<E> newChildren) {
		if (newChildren.isEmpty()) return true;

		for (final INode child : newChildren) {
			if (!child.isRoot()) return false;
		}
		children.addAll(newChildren);
		for (final INode child : newChildren) {
			getNode(child).internalSetParent(this);
		}
		return true;
	}

	/**
	 * Remove children nodes from current node.
	 * Return children nodes.
	 *
	 * @return children nodes
	 */
	@Override
	public final List<INode> removeChildren() {
		if (children.isEmpty()) return List.of();

		final List<INode> oldChildren = List.copyOf(children);
		for (final INode child : oldChildren) child.removeFromParent();
		return oldChildren;
	}

	/**
	 * Add this node to the parent node.
	 * Return false if this node already have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean addToParent(@Nonnull INode parent) {
		return parent.addChild(this);
	}

	/**
	 * Remove this node itself from its parent node.
	 * Return false if this node doesn't have parent node.
	 * Return true otherwise.
	 *
	 * @return whether the operation is success or not
	 */
	@Override
	public final boolean removeFromParent() {
		// if current node is root node
		if (this.parent == null) return false;

		return parent.removeChild(this);
	}

	@Nonnull
	protected String partialToString() {
		return "";
	}

	@Nonnull
	@Override
	public final String toString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", directWeight: " + directWeight
				+ ", indirectWeight: " + indirectWeight
				+ partialToString()
				+ " }";
	}

	@Nonnull
	protected String partialTreeElementString() {
		return "";
	}

	@Nonnull
	@Override
	public final String toTreeElementString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", directWeight: " + directWeight
				+ ", indirectWeight: " + indirectWeight
				+ ", dependencyFrom " + Utilities.mapToString(dependencyFrom, null, Node::countsToString)
				+ ", dependencyTo: " + Utilities.mapToString(dependencyFrom, null, Node::countsToString)
				+ partialTreeElementString()
				+ " }";
	}

	@Nonnull
	@Override
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

			getNode(children.get(0)).internalToString(builder, level + 1);
			for (int i = 1; i < children.size(); i++) {
				builder.append(",\n");
				getNode(children.get(i)).internalToString(builder, level + 1);
			}

			builder.append('\n').append(alignString).append("]}");
		}
	}

	/**
	 * Return this tree iterator
	 *
	 * @return the iterator
	 */
	@Nonnull
	@Override
	public final Iterator<INode> iterator() {
		return new NodeIterator(this);
	}

	/**
	 * The tree iterator
	 */
	private static final class NodeIterator implements Iterator<INode> {
		private INode current;

		private Stack<Iterator<INode>> iterators = new Stack<>();

		private NodeIterator(INode node) {
			this.iterators.push(getNode(node).children.iterator());
		}

		@Override
		public final boolean hasNext() {
			if (current != null) {
				this.iterators.push(getNode(current).children.iterator());
				this.current = null;
			}
			do {
				if (iterators.peek().hasNext()) return true;
				this.iterators.pop();
			} while (!iterators.isEmpty());
			return false;
		}

		@Override
		public final INode next() {
			this.current = iterators.peek().next();
			return current;
		}

		@Override
		public final void remove() {
			if (current == null) throw new IllegalStateException();
			iterators.peek().remove();
			this.current = null;
		}
	}
	//</editor-fold>

	protected static abstract class NodeBuilder<E extends INode, B extends INodeBuilder> implements INodeBuilder<E, B> {
		@Nullable
		protected String name;

		@Nullable
		protected String uniqueName;

		@Nullable
		protected String signature;

		protected NodeBuilder() {
		}

		@Override
		public boolean isValid() {
			return name != null && uniqueName != null && signature != null;
		}

		@Override
		@Nonnull
		public abstract E build();

		@Override
		@Nullable
		public final String getName() {
			return name;
		}

		@Override
		@Nonnull
		public final B setName(@Nonnull String name) {
			this.name = name;
			//noinspection unchecked
			return (B) this;
		}

		@Override
		@Nullable
		public final String getUniqueName() {
			return uniqueName;
		}

		@Override
		@Nonnull
		public final B setUniqueName(@Nonnull String uniqueName) {
			this.uniqueName = uniqueName;
			//noinspection unchecked
			return (B) this;
		}

		@Override
		@Nullable
		public final String getSignature() {
			return signature;
		}

		@Override
		@Nonnull
		public final B setSignature(@Nonnull String content) {
			this.signature = content;
			//noinspection unchecked
			return (B) this;
		}
	}
}

