package mrmathami.cia.cpp.ast;

import mrmathami.annotations.Internal;
import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.utils.Utilities;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ClassNode extends CppNode implements IClassContainer, IEnumContainer, IFunctionContainer, IVariableContainer, ITypedefContainer {
	private static final long serialVersionUID = -1L;

	@Nonnull private transient Set<CppNode> bases = new HashSet<>();

	public ClassNode() {
	}

	//region Getter & Setter

	@Nonnull
	public Set<CppNode> getBases() {
		return isWritable() ? Collections.unmodifiableSet(bases) : bases;
	}

	@Internal
	@SuppressWarnings("AssertWithSideEffects")
	public void addBases(@Nonnull Set<CppNode> bases) {
		checkReadOnly();
		assert bases.stream().noneMatch(this::equals)
				&& bases.stream().map(CppNode::getRoot).allMatch(getRoot()::equals);
		this.bases.addAll(bases);
	}

	@Internal
	public void removeBases() {
		checkReadOnly();
		bases.clear();
	}

	@Internal
	@SuppressWarnings("AssertWithSideEffects")
	public void addBase(@Nonnull CppNode base) {
		checkReadOnly();
		assert base != this && base.getRoot() == getRoot();
		bases.add(base);
	}

	@Internal
	public void removeBase(@Nonnull CppNode base) {
		checkReadOnly();
		bases.remove(base);
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
	public List<FunctionNode> getFunctions() {
		return getChildrenList(FunctionNode.class);
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

	//<editor-fold desc="Node Comparator">
	@Override
	protected boolean isSimilar(@Nonnull CppNode node, @Nonnull Matcher matcher) {
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
	protected int similarHashcode(@Nonnull Matcher matcher) {
		int result = super.similarHashcode(matcher);
		result = 31 * result + bases.size();
		return result;
	}

	@Override
	protected boolean isIdentical(@Nonnull CppNode node, @Nonnull Matcher matcher) {
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
	protected int identicalHashcode(@Nonnull Matcher matcher) {
		int result = super.identicalHashcode(matcher);
		result = 31 * result + bases.size();
		return result;
	}
	//</editor-fold>

	//region TreeNode

	@Override
	void internalLock(@Nonnull Map<String, String> stringPool, @Nonnull Map<DependencyMap, DependencyMap> countsPool) {
		super.internalLock(stringPool, countsPool);
		this.bases = Set.copyOf(bases);
	}

	@Override
	void internalOnTransfer(@Nonnull CppNode fromNode, @Nullable CppNode toNode) {
		if (!bases.contains(fromNode)) return;
		bases.remove(fromNode);
		if (toNode != null) bases.add(toNode);
	}

	//endregion TreeNode

	@Nonnull
	String partialElementString() {
		return ", \"bases\": " + Utilities.collectionToString(bases);
	}

	//region Object Helper

	@Override
	void write(@Nonnull ObjectOutput output) throws IOException {
		super.write(output);

		output.writeInt(bases.size());
		for (final CppNode base : bases) {
			output.writeObject(base);
		}
	}

	@Override
	void read(@Nonnull ObjectInput input) throws IOException, ClassNotFoundException {
		super.read(input);

		final int basesSize = input.readInt();
		for (int i = 0; i < basesSize; i++) {
			bases.add(castNonnull(input.readObject(), CppNode.class));
		}
	}

	//endregion Object Helper
}
