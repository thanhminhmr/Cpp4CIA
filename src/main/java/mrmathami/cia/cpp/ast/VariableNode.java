package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;

public final class VariableNode extends CppNode implements IBodyContainer, ITypeContainer {
	private static final long serialVersionUID = -1L;

	@Nullable private String body;
	@Nullable private CppNode type;

	public VariableNode() {
	}

	@Nullable
	@Override
	public String getBody() {
		return body;
	}

	@Internal
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

	//region Node Comparator
	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher)
				&& matcher.isNodeMatch(type, ((VariableNode) node).type, MatchLevel.PROTOTYPE_IDENTICAL);
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
	//endregion Node Comparator

	//region TreeNode

	@Override
	void internalLock(@Nonnull Map<String, String> stringPool, @Nonnull Map<DependencyMap, DependencyMap> countsPool) {
		super.internalLock(stringPool, countsPool);
		if (body != null) this.body = stringPool.computeIfAbsent(body, String::toString);
	}

	@Override
	void internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (type == fromNode) this.type = toNode;
	}

	//endregion TreeNode

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

	//region Object Helper

	@Override
	public void write(@Nonnull ObjectOutput output) throws IOException {
		super.write(output);

		output.writeObject(body);
		output.writeObject(type);
	}

	@Override
	public void read(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		super.read(input);

		this.body = castNullable(input.readObject(), String.class);
		this.type = castNullable(input.readObject(), CppNode.class);
	}

	//endregion Object Helper
}
