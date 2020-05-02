package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClassNode extends Node implements IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer {
	private static final long serialVersionUID = -9191507582907308880L;

	@Nonnull private transient Set<Node> bases;

	public ClassNode() {
		this.bases = new HashSet<>();
	}

	@Override
	final void internalLock() {
		super.internalLock();
		this.bases = Set.copyOf(bases);
	}

	@Nonnull
	public final Set<Node> getBases() {
		return isReadOnly() ? bases : Collections.unmodifiableSet(bases);
	}

	public final boolean addBases(@Nonnull Set<Node> bases) {
		checkReadOnly();
		return this.bases.addAll(bases);
	}

	public final void removeBases() {
		checkReadOnly();
		bases.clear();
	}

	public final boolean addBase(@Nonnull Node base) {
		checkReadOnly();
		return bases.add(base);
	}

	public final boolean removeBase(@Nonnull Node base) {
		checkReadOnly();
		return bases.remove(base);
	}

	@Nonnull
	@Override
	public final List<ClassNode> getClasses() {
		return getChildrenList(ClassNode.class);
	}

	@Nonnull
	@Override
	public final List<EnumNode> getEnums() {
		return getChildrenList(EnumNode.class);
	}

	@Nonnull
	@Override
	public final List<FunctionNode> getFunctions() {
		return getChildrenList(FunctionNode.class);
	}

	@Nonnull
	@Override
	public final List<VariableNode> getVariables() {
		return getChildrenList(VariableNode.class);
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected final boolean isSimilar(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (!super.isSimilar(node, matcher)) return false;
		final Set<Wrapper> set = new HashSet<>();
		for (final Node base : bases) set.add(new Wrapper(base, MatchLevel.PROTOTYPE_IDENTICAL, matcher));
		for (final Node base : ((ClassNode) node).bases) {
			if (!set.remove(new Wrapper(base, MatchLevel.PROTOTYPE_IDENTICAL, matcher))) return false;
		}
		return set.isEmpty();
	}

	@Override
	protected final int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + bases.size();
		return result;
	}

	@Override
	protected final boolean isIdentical(@Nonnull Node node, @Nonnull Matcher matcher) {
		if (!super.isIdentical(node, matcher)) return false;
		final Set<Wrapper> set = new HashSet<>();
		for (final Node base : bases) set.add(new Wrapper(base, MatchLevel.IDENTICAL, matcher));
		for (final Node base : ((ClassNode) node).bases) {
			if (!set.remove(new Wrapper(base, MatchLevel.IDENTICAL, matcher))) return false;
		}
		return set.isEmpty();
	}

	@Override
	protected final int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + bases.size();
		return result;
	}
	//</editor-fold>

	@Override
	final boolean internalOnTransfer(@Nonnull Node fromNode, @Nullable Node toNode) {
		if (!bases.contains(fromNode)) return false;
		bases.remove(fromNode);
		if (toNode != null) bases.add(toNode);
		return true;
	}

	@Nonnull
	final String partialTreeElementString() {
		return ", bases: " + Utilities.collectionToString(bases);
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
		outputStream.writeObject(Set.copyOf(bases));
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		inputStream.defaultReadObject();
		this.bases = (Set<Node>) inputStream.readObject();
	}
	//</editor-fold>
}
