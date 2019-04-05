package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClassNode extends Node implements IClass {
	private static final long serialVersionUID = 6858078087778982493L;

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
	public final boolean addBase(@Nonnull INode base) {
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
		bases.set(index, newBase);
		return true;
	}

	@Nonnull
	@Override
	public final List<INode> getClasses() {
		return getChildrenList(IClass.class);
	}

	@Nonnull
	@Override
	public final List<INode> getEnums() {
		return getChildrenList(IEnum.class);
	}

	@Nonnull
	@Override
	public final List<INode> getFunctions() {
		return getChildrenList(IFunction.class);
	}

	@Nonnull
	@Override
	public final List<INode> getVariables() {
		return getChildrenList(IVariable.class);
	}

	@Nonnull
	@Override
	public String toString() {
		return "(" + objectToString(this)
				+ ") { name: \"" + getName()
				+ "\", uniqueName: \"" + getUniqueName()
				+ "\", signature: \"" + getSignature()
				+ "\" }";
	}

	@Nonnull
	@Override
	public String toTreeElementString() {
		return "(" + objectToString(this)
				+ ") { name: \"" + getName()
				+ "\", uniqueName: \"" + getUniqueName()
				+ "\", signature: \"" + getSignature()
				+ "\", dependencyMap: " + mapToString(getDependencies())
				+ ", bases: " + listToString(bases)
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
