package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public final class TypedefNode extends Node implements ITypeContainer, ITypedefContainer {
	private static final long serialVersionUID = 4653337164369098190L;

	@Nullable private Node type;

	public TypedefNode() {
	}

	public TypedefNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
	}

	@Nullable
	@Override
	public final Node getType() {
		return type;
	}

	@Override
	public final boolean setType(@Nullable Node type) {
		checkReadOnly();
		if (type != null && type.getRoot() != getRoot()) return false;
		this.type = type;
		return true;
	}

	@Nonnull
	@Override
	public final List<TypedefNode> getTypedefs() {
		return getChildrenList(TypedefNode.class);
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected final boolean isPrototypeSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isPrototypeSimilar(node, matcher) && matcher.isNodeMatch(type, ((TypedefNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isPrototypeIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isPrototypeIdentical(node, matcher) && matcher.isNodeMatch(type, ((TypedefNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher) && matcher.isNodeMatch(type, ((TypedefNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected final int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected final boolean isIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		return super.isIdentical(node, matcher) && matcher.isNodeMatch(type, ((TypedefNode) node).type, MatchLevel.IDENTICAL);
	}

	@Override
	protected final int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}
	//</editor-fold>

	@Override
	final boolean internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
		if (type != fromNode) return false;
		this.type = toNode;
		return true;
	}

	@Nonnull
	@Override
	final String partialTreeElementString() {
		return ", type: " + type;
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}
