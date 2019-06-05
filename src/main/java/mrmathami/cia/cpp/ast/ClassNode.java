package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ClassNode extends Node implements IClass {
	private static final long serialVersionUID = -5538726025004995091L;

	@Nonnull
	private final List<INode> bases;

	private ClassNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull List<INode> bases) {
		super(name, simpleName, uniqueName);
		this.bases = bases;
	}

	@Nonnull
	public static IClassBuilder builder() {
		return new ClassNodeBuilder();
	}

	@Nonnull
	@Override
	public final List<INode> getBases() {
		return Collections.unmodifiableList(bases);
	}

	@Override
	public final boolean addBases(@Nonnull List<INode> bases) {
		if (bases.isEmpty()) return true;
		if (this.bases.isEmpty()) {
			this.bases.addAll(bases);
			return true;
		}

		for (final INode base : bases) {
			if (this.bases.contains(base)) return false;
		}

		return this.bases.addAll(bases);
	}

	@Override
	public final List<INode> removeBases() {
		if (bases.isEmpty()) return List.of();

		final List<INode> oldBases = List.copyOf(bases);
		bases.clear();
		return oldBases;
	}

	@Override
	public final boolean addBase(@Nonnull INode base) {
		if (bases.contains(base)) return false;
		return bases.add(base);
	}

	@Override
	public final boolean removeBase(@Nonnull INode base) {
		return bases.remove(base);
	}

	@Override
	public final boolean replaceBase(@Nonnull INode oldBase, @Nonnull INode newBase) {
		final int index = bases.indexOf(oldBase);
		if (index < 0) return false;
		if (bases.contains(newBase)) {
			bases.remove(index);
		} else {
			bases.set(index, newBase);
		}
		return true;
	}

	@Override
	public final boolean equalsBase(@Nonnull INode node) {
		if (node instanceof IClass) {
			final Set<INode> baseA = Set.copyOf(bases);
			final Set<INode> baseB = Set.copyOf(((IClass) node).getBases());
			return baseA.equals(baseB);
		}
		return false;
	}

	@Nonnull
	@Override
	public final List<IClass> getClasses() {
		return getChildrenList(IClass.class);
	}

	@Nonnull
	@Override
	public final List<IEnum> getEnums() {
		return getChildrenList(IEnum.class);
	}

	@Nonnull
	@Override
	public final List<IFunction> getFunctions() {
		return getChildrenList(IFunction.class);
	}

	@Nonnull
	@Override
	public final List<IVariable> getVariables() {
		return getChildrenList(IVariable.class);
	}

	@Nonnull
	@Override
	public final String toString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + getName()
				+ "\", uniqueName: \"" + getUniqueName()
				+ "\", signature: \"" + getSignature()
				+ "\", directWeight: " + getDirectWeight()
//				+ ", indirectWeight: " + getIndirectWeight()
				+ " }";
	}

	@Nonnull
	@Override
	public final String toTreeElementString() {
		return "(" + Utilities.objectToString(this)
				+ ") { name: \"" + getName()
				+ "\", uniqueName: \"" + getUniqueName()
				+ "\", signature: \"" + getSignature()
				+ "\", directWeight: " + getDirectWeight()
//				+ ", indirectWeight: " + getIndirectWeight()
				+ ", dependencyMap: " + Utilities.mapToString(getDependencies())
				+ ", bases: " + Utilities.collectionToString(bases)
				+ " }";
	}

	public static final class ClassNodeBuilder extends NodeBuilder<IClass, IClassBuilder> implements IClassBuilder {
		@Nonnull
		private List<INode> bases = new ArrayList<>();

		private ClassNodeBuilder() {
		}

		@Nonnull
		@Override
		public final IClass build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new ClassNode(name, uniqueName, signature, bases);
		}

		@Nonnull
		public final List<INode> getBases() {
			return bases;
		}

		@Nonnull
		public final IClassBuilder setBases(@Nonnull List<INode> bases) {
			this.bases = new ArrayList<>(bases);
			return this;
		}
	}
}
