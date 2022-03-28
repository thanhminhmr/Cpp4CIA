package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.utils.Utilities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

public final class FunctionNode extends CppNode implements IBodyContainer, ITypeContainer, IClassContainer, IEnumContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -1L;

	@Nonnull private transient List<CppNode> parameters = new LinkedList<>();
	@Nullable private String body;
	@Nullable private CppNode type;

	public FunctionNode() {
	}

	//region Getter & Setter

	@Nonnull
	public List<CppNode> getParameters() {
		return isWritable() ? Collections.unmodifiableList(parameters) : parameters;
	}

//	@Internal
//	@SuppressWarnings("AssertWithSideEffects")
//	public boolean addParameters(@Nonnull List<CppNode> parameters) {
//		checkReadOnly();
//		assert parameters.stream().noneMatch(this::equals)
//				&& parameters.stream().map(CppNode::getRoot).allMatch(getRoot()::equals);
//		return this.parameters.addAll(parameters);
//	}

//	@Internal
//	public void removeParameters() {
//		checkReadOnly();
//		parameters.clear();
//	}

	@Internal
	@SuppressWarnings("AssertWithSideEffects")
	public void addParameter(@Nonnull CppNode parameter) {
		checkReadOnly();
		assert parameter != this && parameter.getRoot() == getRoot();
		parameters.add(parameter);
		addDependencyTo(parameter, DependencyType.USE);
	}

	@Internal
	public void removeParameter(@Nonnull CppNode parameter) {
		checkReadOnly();
		parameters.remove(parameter);
		removeDependencyTo(parameter, DependencyType.USE);
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

	//endregion Getter & Setter

	//region Containers

	@Nonnull
	@Override
	public List<ClassNode> getClasses() {
		return getChildrenList(ClassNode.class);
	}

	@Nonnull
	@Override
	public List<EnumNode> getEnums() {
		return getChildrenList(EnumNode.class);
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

	//endregion Containers

	//region Node Comparator

	@Override
	protected boolean isPrototypeSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isPrototypeSimilar(node, matcher)
				&& matcher.isNodesMatchOrdered(parameters, ((FunctionNode) node).parameters, MatchLevel.PROTOTYPE_SIMILAR);
	}

	@Override
	protected int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
		result = 31 * result + parameters.size(); // prototype similar
		return result;
	}

	@Override
	protected boolean isPrototypeIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isPrototypeIdentical(node, matcher)
				&& matcher.isNodesMatchOrdered(parameters, ((FunctionNode) node).parameters, MatchLevel.PROTOTYPE_SIMILAR);
	}

	@Override
	protected int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
		result = 31 * result + parameters.size(); // prototype similar
		return result;
	}

	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		return super.isSimilar(node, matcher)
				&& matcher.isNodesMatchOrdered(parameters, ((FunctionNode) node).parameters, MatchLevel.PROTOTYPE_IDENTICAL);
	}

	@Override
	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		result = 31 * result + parameters.size(); // prototype identical
		return result;
	}

	@Override
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isIdentical(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		return Objects.equals(body, function.body)
				&& matcher.isNodeMatch(type, function.type, MatchLevel.PROTOTYPE_IDENTICAL)
				&& matcher.isNodesMatchOrdered(parameters, ((FunctionNode) node).parameters, MatchLevel.PROTOTYPE_IDENTICAL);
	}

	@Override
	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + (body != null ? body.hashCode() : 0);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		result = 31 * result + parameters.size(); // prototype identical
		return result;
	}

	//endregion Node Comparator

	//region TreeNode

	@Override
	void internalLock(@Nonnull Map<String, String> stringPool, @Nonnull Map<DependencyMap, DependencyMap> countsPool) {
		super.internalLock(stringPool, countsPool);
		if (body != null) this.body = stringPool.computeIfAbsent(body, String::toString);
		this.parameters = List.copyOf(parameters);
	}

	@Override
	void internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (type == fromNode) this.type = toNode;
		final ListIterator<CppNode> iterator = parameters.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next() == fromNode) {
				if (toNode != null) {
					iterator.set(toNode);
				} else {
					iterator.remove();
				}
			}
		}
	}

	//endregion TreeNode

	@Nonnull
	String partialElementString() {
		final StringBuilder builder = new StringBuilder()
				.append(", \"type\": ").append(type)
				.append(", \"parameters\": ").append(Utilities.collectionToString(parameters))
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
	void write(@Nonnull ObjectOutput output) throws IOException {
		super.write(output);

		output.writeObject(body);
		output.writeObject(type);

		output.writeInt(parameters.size());
		for (final CppNode parameter : parameters) {
			output.writeObject(parameter);
		}
	}

	@Override
	void read(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		super.read(input);

		this.body = castNullable(input.readObject(), String.class);
		this.type = castNullable(input.readObject(), CppNode.class);

		final int parametersSize = input.readInt();
		final CppNode[] parameters = new CppNode[parametersSize];
		for (int i = 0; i < parametersSize; i++) {
			parameters[i] = castNonnull(input.readObject(), CppNode.class);
		}
		this.parameters = List.of(parameters);
	}

	//endregion Object Helper
}
