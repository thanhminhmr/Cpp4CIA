package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public final class EnumNode extends CppNode implements ITypeContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = 6958602690011907254L;

	@Nullable private CppNode type;

	public EnumNode() {
	}

	public EnumNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
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

	@Nonnull
	@Override
	public List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}

	@Nonnull
	@Override
	public List<TypedefNode> getTypedefs() {
		return getChildrenList(TypedefNode.class);
	}

	//<editor-fold desc="Node Comparator">

	@Override
	protected boolean isPrototypeSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isPrototypeSimilar(node, matcher) && matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected boolean isPrototypeIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isPrototypeIdentical(node, matcher) && matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher) && matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.SIMILAR);
	}

	@Override
	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		return result;
	}

	@Override
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isIdentical(node, matcher)
				&& matcher.isNodeMatch(type, ((EnumNode) node).type, MatchLevel.IDENTICAL);
	}

	@Override
	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
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
		return ", \"type\": " + type;
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}

