package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

public final class EnumNode extends CppNode implements ITypeContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -1L;

	@Nullable private CppNode type;

	public EnumNode() {
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
		assert this.type == null; // no overwrite
		assert type == null || (type != this && type.getRoot() == getRoot());
		this.type = type;
		if (type != null) addDependencyTo(type, DependencyType.USE);
	}

	//region Containers

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

	//endregion Containers

	//region Node Comparator

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

	//endregion Node Comparator

	@Override
	void internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (type == fromNode) this.type = toNode;
	}

	@Nonnull
	@Override
	String partialElementString() {
		return ", \"type\": " + type;
	}

	//region Object Helper

	@Override
	void write(@Nonnull ObjectOutput output) throws IOException {
		super.write(output);

		output.writeObject(type);
	}

	@Override
	void read(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		super.read(input);

		this.type = castNullable(input.readObject(), CppNode.class);
	}

	//endregion Object Helper
}

