package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;

public final class EnumNode extends Node implements ITypeContainer<EnumNode>, IVariableContainer {
	private static final long serialVersionUID = 7234965704737476746L;

	@Nullable private Node type;

	public EnumNode() {
	}

	@Nullable
	@Override
	public final Node getType() {
		return type;
	}

	@Nonnull
	@Override
	public final EnumNode setType(@Nullable Node type) {
		if (readOnly) throwReadOnly();
		this.type = type;
		return this;
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected final boolean isPrototypeSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isPrototypeSimilar(node, matcher) && matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isPrototypeIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isPrototypeIdentical(node, matcher) && matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher) && matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isIdentical(node, matcher)
				&& matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.IDENTICAL);
	}

	@Override
	protected final int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}
	//</editor-fold>

	@Override
	protected final void internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
		if (type == fromNode) this.type = toNode;
	}

	@Nonnull
	@Override
	protected final String partialTreeElementString() {
		return ", type: " + type;
	}

	@Nonnull
	@Override
	public final List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}

