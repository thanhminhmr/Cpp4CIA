package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.utils.Utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;

public final class FunctionNode extends CppNode implements IBodyContainer, ITypeContainer, IClassContainer, IEnumContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -2779864800917566027L;

	@Nonnull private transient List<CppNode> parameters = new LinkedList<>();
	@Nullable private String body;
	@Nullable private CppNode type;

	public FunctionNode() {
	}

	public FunctionNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
	}

	@Override
	void internalLock() {
		super.internalLock();
		if (body != null) this.body = body.intern();
		this.parameters = List.copyOf(parameters);
	}

	@Nonnull
	public List<CppNode> getParameters() {
		return isWritable() ? Collections.unmodifiableList(parameters) : parameters;
	}

	public boolean addParameters(@Nonnull List<CppNode> parameters) {
		checkReadOnly();
		for (final CppNode parameter : parameters) {
			if (parameter == this || parameter.getRoot() != getRoot()) return false;
		}
		return this.parameters.addAll(parameters);
	}

	public void removeParameters() {
		checkReadOnly();
		parameters.clear();
	}

	public boolean addParameter(@Nonnull CppNode parameter) {
		checkReadOnly();
		if (parameter == this || parameter.getRoot() != getRoot()) return false;
		parameters.add(parameter);
		return true;
	}

	public boolean removeParameter(@Nonnull CppNode parameter) {
		checkReadOnly();
		return parameters.remove(parameter);
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

	//<editor-fold desc="Node Comparator">
	@Override
	protected boolean isPrototypeSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isPrototypeSimilar(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
//		if (!matcher.isNodeMatch(type, function.type, MatchLevel.SIMILAR)) return false;
		final Iterator<CppNode> iteratorA = parameters.iterator();
		final Iterator<CppNode> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.PROTOTYPE_SIMILAR)) break;
		}
		return false;
	}

	@Override
	protected int prototypeSimilarHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeSimilarHashcode(matcher);
//		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		result = 31 * result + parameters.size(); // prototype similar
		return result;
	}

	@Override
	protected boolean isPrototypeIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isPrototypeIdentical(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
//		if (!matcher.isNodeMatch(type, function.type, MatchLevel.SIMILAR)) return false;
		final Iterator<CppNode> iteratorA = parameters.iterator();
		final Iterator<CppNode> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.PROTOTYPE_SIMILAR)) break;
		}
		return false;
	}

	@Override
	protected int prototypeIdenticalHashcode(@Nonnull Matcher matcher) {
		int result = super.prototypeIdenticalHashcode(matcher);
//		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.SIMILAR);
		result = 31 * result + parameters.size(); // prototype similar
		return result;
	}

	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isSimilar(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		if (!matcher.isNodeMatch(type, function.type, MatchLevel.PROTOTYPE_IDENTICAL)) return false;
		final Iterator<CppNode> iteratorA = parameters.iterator();
		final Iterator<CppNode> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.PROTOTYPE_IDENTICAL)) break;
		}
		return false;
	}

	@Override
	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		result = 31 * result + parameters.size(); // similar
		return result;
	}

	@Override
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isIdentical(node, matcher)) return false;
		final FunctionNode function = (FunctionNode) node;
		if (!Objects.equals(body, function.body)
				|| !matcher.isNodeMatch(type, function.type, MatchLevel.PROTOTYPE_IDENTICAL)) {
			return false;
		}
		final Iterator<CppNode> iteratorA = parameters.iterator();
		final Iterator<CppNode> iteratorB = function.parameters.iterator();
		while (iteratorA.hasNext() == iteratorB.hasNext()) {
			if (!iteratorA.hasNext()) return true;
			if (!matcher.isNodeMatch(iteratorA.next(), iteratorB.next(), MatchLevel.PROTOTYPE_IDENTICAL)) break;
		}
		return false;
	}

	@Override
	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + (body != null ? body.hashCode() : 0);
		result = 31 * result + matcher.nodeHashcode(type, MatchLevel.PROTOTYPE_IDENTICAL);
		result = 31 * result + parameters.size(); // identical
		return result;
	}
	//</editor-fold>

	@Override
	boolean internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		boolean isChanged = false;
		if (type == fromNode) {
			this.type = toNode;
			isChanged = true;
		}
		final ListIterator<CppNode> iterator = parameters.listIterator();
		while (iterator.hasNext()) {
			if (iterator.next() == fromNode) {
				isChanged = true;
				if (toNode != null) {
					iterator.set(toNode);
				} else {
					iterator.remove();
				}
			}
		}
		return isChanged;
	}

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

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
		outputStream.writeObject(List.copyOf(parameters));
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.parameters = (List<CppNode>) inputStream.readObject();
	}
	//</editor-fold>
}
