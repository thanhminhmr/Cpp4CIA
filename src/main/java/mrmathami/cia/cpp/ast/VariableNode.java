package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

public final class VariableNode extends Node implements IBodyContainer<VariableNode>, ITypeContainer<VariableNode> {
	private static final long serialVersionUID = -4695243239191843968L;

	@Nullable private String body;
	@Nullable private Node type;

	public VariableNode() {
	}

	@Nullable
	@Override
	public String getBody() {
		return body;
	}

	@Nonnull
	@Override
	public VariableNode setBody(@Nullable String body) {
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
	public final VariableNode setType(@Nullable Node type) {
		if (readOnly) throwReadOnly();
		this.type = type;
		return this;
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected final boolean isPrototypeSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isPrototypeSimilar(node, matcher) && matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isPrototypeIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isPrototypeIdentical(node, matcher) && matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher) && matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.SIMILAR);
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
				&& Objects.equals(body, ((VariableNode) node).body)
				&& matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.IDENTICAL);
	}

	@Override
	protected final int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + (body != null ? body.hashCode() : 0);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}
	//</editor-fold>

	@Override
	protected final boolean internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
		if (type != fromNode) return false;
		this.type = toNode;
		return true;
	}

	@Nonnull
	@Override
	protected final String partialTreeElementString() {
		return ", type: " + type
				+ ", body: " + (body != null ? "\"" + body.replaceAll("\"", "\\\\\"") + "\"" : null);
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}
