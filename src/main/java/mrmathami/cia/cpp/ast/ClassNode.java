package mrmathami.cia.cpp.ast;

import mrmathami.util.Utilities;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ClassNode extends Node implements IClass {
	private static final long serialVersionUID = -1141866194079166813L;

	@Nonnull
	private final Set<INode> bases;

	private ClassNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull Set<INode> bases) {
		super(name, simpleName, uniqueName);
		this.bases = bases;
	}

	@Nonnull
	public static IClassBuilder builder() {
		return new ClassNodeBuilder();
	}

	@Nonnull
	@Override
	public final Set<INode> getBases() {
		return Collections.unmodifiableSet(bases);
	}

	@Override
	public final void removeBases() {
		bases.clear();
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
		if (!bases.contains(oldBase)) return false;

		bases.remove(oldBase);
		bases.add(newBase);
		return true;
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

	@Override
	public final boolean matches(Object node) {
		return super.matches(node) && ((IClass) node).getBases().equals(bases);
	}

	@Nonnull
	@Override
	protected String partialTreeElementString() {
		return ", bases: " + Utilities.collectionToString(bases);
	}

	public static final class ClassNodeBuilder extends NodeBuilder<IClass, IClassBuilder> implements IClassBuilder {
		@Nonnull
		private Set<INode> bases = new HashSet<>();

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
		public final Set<INode> getBases() {
			return bases;
		}

		@Nonnull
		public final IClassBuilder setBases(@Nonnull Set<INode> bases) {
			this.bases = new HashSet<>(bases);
			return this;
		}
	}
}
