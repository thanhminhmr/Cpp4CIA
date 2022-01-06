package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public final class TypedefNode extends CppNode implements ITypeContainer, ITypedefContainer {
	private static final long serialVersionUID = -1L;

	@Nullable private CppNode type;

	public TypedefNode() {
	}

	@Nullable
	@Override
	public CppNode getType() {
		return type;
	}

	@Internal
	@Override
	@SuppressWarnings("AssertWithSideEffects")
	public void setType(@Nullable CppNode type) {
		checkReadOnly();
		assert type == null || (type != this && type.getRoot() == getRoot());
		this.type = type;
	}

	@Nonnull
	@Override
	public List<TypedefNode> getTypedefs() {
		return getChildrenList(TypedefNode.class);
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher) && matcher.isNodeMatch(type, ((TypedefNode) node).type, MatchLevel.PROTOTYPE_IDENTICAL);
	}

	@Override
	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		return result;
	}

	@Override
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isIdentical(node, matcher) && matcher.isNodeMatch(type, ((TypedefNode) node).type, MatchLevel.PROTOTYPE_IDENTICAL);
	}

	@Override
	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		return result;
	}
	//</editor-fold>

	@Override
	void internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (type == fromNode) this.type = toNode;
	}

	@Nonnull
	@Override
	String partialElementString() {
		return ", \"type\": " + type;
	}

	//<editor-fold desc="Object Helper">
	@Override
	public void writeExternal(@Nonnull ObjectOutput output) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		super.writeExternal(output);

		output.writeObject(type);
	}

	@Override
	public void readExternal(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		super.readExternal(input);

		this.type = castNullable(input.readObject(), CppNode.class);
	}
	//</editor-fold>
}
