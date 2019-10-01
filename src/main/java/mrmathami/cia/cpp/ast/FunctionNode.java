package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public final class FunctionNode extends Node implements
		IBodyContainer<FunctionNode>, ITypeContainer<FunctionNode>,
		IClassContainer, IEnumContainer, IVariableContainer {
	private static final long serialVersionUID = 4794995746418921391L;

	@Nonnull
	private List<Node> parameters = new LinkedList<>();

	@Nullable
	private String body;

	@Nullable
	private Node type;

	public FunctionNode() {
	}

	@Nonnull
	public final List<Node> getParameters() {
		return Collections.unmodifiableList(parameters);
	}

	public final boolean addParameters(@Nonnull List<Node> parameters) {
		for (final Node parameter : parameters) {
			if (parameter.getParent() != this) return false;
		}
		return this.parameters.addAll(parameters);
	}

	public final void removeParameters() {
		parameters.clear();
	}

	public final boolean addParameter(@Nonnull Node parameter) {
		if (parameter.getParent() != this) return false;
		parameters.add(parameter);
		return true;
	}

	public final boolean removeParameter(@Nonnull Node parameter) {
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
	protected final void internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
		if (type == fromNode) this.type = toNode;
		final ListIterator<Node> iterator = parameters.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next() == fromNode) {
				if (toNode != null) {
					iterator.set(toNode);
				} else {
					iterator.remove();
				}
				break;
			}
		}
	}

	@Nonnull
	protected final String partialTreeElementString() {
		return ", type: " + type
				+ ", parameters: " + Utilities.collectionToString(parameters)
				+ ", body: " + (body != null ? "\"" + body.replaceAll("\"", "\"\"") + "\"" : null);
	}
}
