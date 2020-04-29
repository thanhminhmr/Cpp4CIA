package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public final class FunctionNode extends Node implements IBodyContainer<FunctionNode>, ITypeContainer<FunctionNode>, IClassContainer, IEnumContainer, IVariableContainer {
	private static final long serialVersionUID = 8686443038824913051L;

	@Nonnull private transient List<Node> parameters;
	@Nullable private String body;
	@Nullable private Node type;

	public FunctionNode() {
		this.parameters = new LinkedList<>();
	}

	@Override
	protected final void internalLock() {
		super.internalLock();
		this.parameters = List.copyOf(parameters);
	}

	@Nonnull
	public final List<Node> getParameters() {
		return readOnly ? parameters : Collections.unmodifiableList(parameters);
	}

	public final boolean addParameters(@Nonnull List<Node> parameters) {
		if (readOnly) throwReadOnly();
		return this.parameters.addAll(parameters);
	}

	public final void removeParameters() {
		if (readOnly) throwReadOnly();
		parameters.clear();
	}

	public final boolean addParameter(@Nonnull Node parameter) {
		if (readOnly) throwReadOnly();
		parameters.add(parameter);
		return true;
	}

	public final boolean removeParameter(@Nonnull Node parameter) {
		if (readOnly) throwReadOnly();
		return parameters.remove(parameter);
	}

	@Nullable
	@Override
	public final String getBody() {
		return body;
	}

	@Nonnull
	@Override
	public final FunctionNode setBody(@Nullable String body) {
		if (readOnly) throwReadOnly();
		this.body = body;
		return this;
	}

	@Nullable
	@Override
	public final Node getType() {
		return type;
	}

	@Nonnull
	@Override
	public final FunctionNode setType(@Nullable Node type) {
		if (readOnly) throwReadOnly();
		this.type = type;
		return this;
	}

	@Nonnull
	@Override
	public final List<ClassNode> getClasses() {
		return getChildrenList(ClassNode.class);
	}

	@Nonnull
	@Override
	public final List<EnumNode> getEnums() {
		return getChildrenList(EnumNode.class);
	}

	@Nonnull
	@Override
	public final List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected final boolean isPrototypeSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (!super.isPrototypeSimilar(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		if (!matcher.isNodeMatch(type, function.type, MatchLevel.SIMILAR)) return false;
		final Iterator<Node> iteratorA = parameters.iterator();
		final Iterator<Node> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.PROTOTYPE_SIMILAR)) break;
		}
		return false;
	}

	@Override
	protected final int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		result = 31 * result + parameters.size(); // prototype similar
		return result;
	}

	@Override
	protected final boolean isPrototypeIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (!super.isPrototypeIdentical(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		if (!matcher.isNodeMatch(type, function.type, MatchLevel.SIMILAR)) return false;
		final Iterator<Node> iteratorA = parameters.iterator();
		final Iterator<Node> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.PROTOTYPE_SIMILAR)) break;
		}
		return false;
	}

	@Override
	protected final int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		result = 31 * result + parameters.size(); // prototype similar
		return result;
	}

	@Override
	protected final boolean isSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (!super.isSimilar(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		if (!matcher.isNodeMatch(type, function.type, MatchLevel.SIMILAR)) return false;
		final Iterator<Node> iteratorA = parameters.iterator();
		final Iterator<Node> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.SIMILAR)) break;
		}
		return false;
	}

	@Override
	protected final int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		result = 31 * result + parameters.size(); // similar
		return result;
	}

	@Override
	protected final boolean isIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (!super.isIdentical(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		if (!Objects.equals(body, function.body) || !matcher.isNodeMatch(type, function.type, MatchLevel.IDENTICAL))
			return false;
		final Iterator<Node> iteratorA = parameters.iterator();
		final Iterator<Node> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.IDENTICAL)) break;
		}
		return false;
	}

	@Override
	protected final int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + (body != null ? body.hashCode() : 0);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.IDENTICAL);
		result = 31 * result + parameters.size(); // identical
		return result;
	}
	//</editor-fold>

	@Override
	protected final boolean internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
		boolean value = false;
		if (type == fromNode) {
			this.type = toNode;
			value = true;
		}
		final ListIterator<Node> iterator = parameters.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next() == fromNode) {
				value = true;
				if (toNode != null) {
					iterator.set(toNode);
				} else {
					iterator.remove();
				}
			}
		}
		return value;
	}

	@Nonnull
	protected final String partialTreeElementString() {
		return ", type: " + type
				+ ", parameters: " + Utilities.collectionToString(parameters)
				+ ", body: " + (body != null ? "\"" + body.replaceAll("\"", "\\\\\"") + "\"" : null);
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
		outputStream.writeObject(List.copyOf(parameters));
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.parameters = (List<Node>) inputStream.readObject();
	}
	//</editor-fold>
}
