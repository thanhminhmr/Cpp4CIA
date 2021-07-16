package mrmathami.cia.cpp.ast;

import mrmathami.utils.Utilities;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClassNode extends CppNode implements IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -6768126335469290258L;

	@Nonnull private transient Set<CppNode> bases = new HashSet<>();

	public ClassNode() {
	}

	public ClassNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
	}

	@Override
	final void internalLock() {
		super.internalLock();
		this.bases = Set.copyOf(bases);
	}

	@Nonnull
	public final Set<CppNode> getBases() {
		return isWritable() ? Collections.unmodifiableSet(bases) : bases;
	}

	public final boolean addBases(@Nonnull Set<CppNode> bases) {
		checkReadOnly();
		for (final CppNode base : bases) {
			if (base == this || base.getRoot() != getRoot()) return false;
		}
		return this.bases.addAll(bases);
	}

	public final void removeBases() {
		checkReadOnly();
		bases.clear();
	}

	public final boolean addBase(@Nonnull CppNode base) {
		checkReadOnly();
		return base != this && base.getRoot() == getRoot() && bases.add(base);
	}

	public final boolean removeBase(@Nonnull CppNode base) {
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

	@Nonnull
	@Override
	public final List<TypedefNode> getTypedefs() {
		return getChildrenList(TypedefNode.class);
	}

	//<editor-fold desc="Node Comparator">
	@Override
	protected final boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isSimilar(node, matcher)) return false;
		final Set<CppNode> nodeBases = ((ClassNode) node).bases;
		final int basesSize = bases.size();
		if (basesSize != nodeBases.size()) return false;
		final Map<Wrapper, int[]> map = new HashMap<>(basesSize);
		for (final CppNode base : this.bases) {
			final Wrapper wrapper = new Wrapper(base, MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			final int[] countWrapper = map.computeIfAbsent(wrapper, any -> new int[]{0});
			countWrapper[0] += 1;
		}
		for (final CppNode base : nodeBases) {
			final Wrapper wrapper = new Wrapper(base, MatchLevel.PROTOTYPE_IDENTICAL, matcher);
			final int[] countWrapper = map.get(wrapper);
			if (countWrapper == null) return false;
			if (--countWrapper[0] == 0) map.remove(wrapper);
		}
		return map.isEmpty();
	}

	@Override
	protected final int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + bases.size();
		return result;
	}

	@Override
	protected final boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
		if (!super.isIdentical(node, matcher)) return false;
		final Set<CppNode> nodeBases = ((ClassNode) node).bases;
		final int basesSize = bases.size();
		if (basesSize != nodeBases.size()) return false;
		final Map<Wrapper, int[]> map = new HashMap<>(basesSize);
		for (final CppNode base : this.bases) {
			final Wrapper wrapper = new Wrapper(base, MatchLevel.IDENTICAL, matcher);
			final int[] countWrapper = map.computeIfAbsent(wrapper, any -> new int[]{0});
			countWrapper[0] += 1;
		}
		for (final CppNode base : nodeBases) {
			final Wrapper wrapper = new Wrapper(base, MatchLevel.IDENTICAL, matcher);
			final int[] countWrapper = map.get(wrapper);
			if (countWrapper == null) return false;
			if (--countWrapper[0] == 0) map.remove(wrapper);
		}
		return map.isEmpty();
	}

	@Override
	protected final int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + bases.size();
		return result;
	}
	//</editor-fold>

	@Override
	final boolean internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (!bases.contains(fromNode)) return false;
		bases.remove(fromNode);
		if (toNode != null) bases.add(toNode);
		return true;
	}

	@Nonnull
	final String partialElementString() {
		return ", \"bases\": " + Utilities.collectionToString(bases);
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
		this.bases = (Set<CppNode>) inputStream.readObject();
	}
	//</editor-fold>
}
