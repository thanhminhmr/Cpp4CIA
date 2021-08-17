package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Objects;

public final class VariableNode extends CppNode implements IBodyContainer, ITypeContainer {
	private static final long serialVersionUID = -7716874065327722815L;

	@Nullable private String body;
	@Nullable private CppNode type;

	public VariableNode() {
	}

	public VariableNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
	}

	@Override
	void internalLock() {
		super.internalLock();
		if (body != null) this.body = body.intern();
	}

	@Nullable
	@Override
	public String getBody() {
		return body;
	}

	@Override
	public void setBody(@Nullable String body) {
		checkReadOnly();
		this.body = body;
	}

	@Nullable
	@Override
	public CppNode getType() {
		return type;
	}

	@Override
	public boolean setType(@Nullable CppNode type) {
		checkReadOnly();
		if (type != null && (type == this || type.getRoot() != getRoot())) return false;
		this.type = type;
		return true;
	}

	//<editor-fold desc="Node Comparator">
//	@Override
//	protected final boolean isPrototypeSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
//		return super.isPrototypeSimilar(node, matcher) && matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.SIMILAR);
//	}

//	@Override
//	protected final int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
//		int result = super.prototypeSimilarHashcode(matcher);
//		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
//		return result;
//	}

//	@Override
//	protected final boolean isPrototypeIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
//		return super.isPrototypeIdentical(node, matcher) && matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.SIMILAR);
//	}

//	@Override
//	protected final int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
//		int result = super.prototypeIdenticalHashcode(matcher);
//		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
//		return result;
//	}

	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher) && matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.PROTOTYPE_IDENTICAL);
	}

	@Override
	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		return result;
	}

	@Override
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isIdentical(node, matcher)
				&& Objects.equals(body, ((VariableNode) node).body)
				&& matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.PROTOTYPE_IDENTICAL);
	}

	@Override
	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + (body != null ? body.hashCode() : 0);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		return result;
	}
	//</editor-fold>

	@Override
	boolean internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (type != fromNode) return false;
		this.type = toNode;
		return true;
	}

	@Nonnull
	@Override
	String partialElementString() {
		final StringBuilder builder = new StringBuilder();
		builder.append(", \"type\": ").append(type)
				.append(", \"body\": ");
		if (body != null) {
			builder.append("\"");
			escapeBody(builder, body);
			builder.append("\"");
		} else {
			builder.append("null");
		}
		return builder.toString();
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}
