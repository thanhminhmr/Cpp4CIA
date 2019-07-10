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
	private static final long serialVersionUID = 5882036380636640285L;

	@Nonnull
	private static final int[] DEPENDENCY_ZERO = new int[DependencyType.values.length];

	@Nonnull
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

	private float weight;

	private transient float impact = Float.POSITIVE_INFINITY;

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

	@Nonnull
	private static Node getNode(@Nonnull INode iNode) {
		if (iNode instanceof Node) return (Node) iNode;
		throw new IllegalStateException("Unexpected foreign node in tree.");
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
	protected final <E extends INode> List<E> getChildrenList(final Class<E> aClass) {
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
	public final float getWeight() {
		return weight;
	}

	@Override
	public final void setWeight(float weight) {
		this.weight = weight;
	}

	@Override
	public final float getImpact() {
		return impact;
	}

	@Override
	public final void setImpact(float impact) {
		this.impact = impact;
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
		return Objects.equals(name, node.name) && Objects.equals(uniqueName, node.uniqueName)
				&& Objects.equals(signature, node.signature) && Objects.equals(parent, node.parent);
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

	@Override
	public boolean matches(Object node) {
		return equals(node) && equalsAllDependencyTo((INode) node);
	}
	//</editor-fold>

	//<editor-fold desc="TreeNode">

	@Override
	public boolean transfer(@Nonnull INode node) {
		// check if child node is root node
		if (!node.isRoot()) return false;
		// check if current node doesn't have children
		if (!children.isEmpty()) {
			for (final Iterator<INode> iterator = children.iterator(); iterator.hasNext(); ) {
				final INode child = iterator.next();
				iterator.remove();
				getNode(node).children.add(child);
				getNode(child).internalSetParent(node);
			}
		}
		transferAllDependency(node);
		return true;
	}

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
	 * Remove children nodes from current node.
	 * Return children nodes.
	 */
	@Override
	public final void removeChildren() {
		if (!children.isEmpty()) {
			// remove children
			for (final INode child : this) {
				child.removeAllDependency();
			}
			children.clear();
		}
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
		// remove all grand-children and lower dependency
		for (final INode node : child) {
			node.removeAllDependency();
		}
		// remove the child
		getNode(child).internalSetParent(null);
		child.removeAllDependency();
		children.remove(child);
		return true;
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
	@Override
	public final String toString() {
		return "(" + Utilities.objectToString(this)
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
	@Override
	public final String toTreeElementString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + name
				+ "\", uniqueName: \"" + uniqueName
				+ "\", signature: \"" + signature
				+ "\", directWeight: " + weight
				+ ", indirectWeight: " + impact
				+ ", dependencyFrom " + Utilities.mapToString(dependencyFrom, null, Node::countsToString)
				+ ", dependencyTo: " + Utilities.mapToString(dependencyTo, null, Node::countsToString)
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
