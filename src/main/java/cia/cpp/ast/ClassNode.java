package cia.cpp.ast;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ClassNode extends Node implements IClass, Serializable {
	@Nonnull
	private final List<IClass> bases;

	private ClassNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull List<IClass> bases) {
		super(name, simpleName, uniqueName);
		this.bases = bases;
	}

	@Nonnull
	public static IClassBuilder builder() {
		return new ClassNodeBuilder();
	}

	@Nonnull
	@Override
	public final List<IClass> getBases() {
		return Collections.unmodifiableList(bases);
	}

	@Override
	public final boolean addBase(@Nonnull IClass base) {
		return bases.add(base);
	}

	@Override
	public final boolean removeBase(@Nonnull IClass base) {
		return bases.remove(base);
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
	public final List<ITypedef> getTypedefs() {
		return getChildrenList(ITypedef.class);
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
	public String toString() {
		return "(" + getClass().getSimpleName() + ") { name = \"" + getName()
				+ ", bases = " + bases
				+ " }";
	}

	public static final class ClassNodeBuilder extends NodeBuilder<IClass, IClassBuilder> implements IClassBuilder {
		@Nonnull
		private List<IClass> bases = new ArrayList<>();

		private ClassNodeBuilder() {
		}

		@Nonnull
		@Override
		public final IClass build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new ClassNode(name, uniqueName, content, bases);
		}

		@Nonnull
		public final List<IClass> getBases() {
			return bases;
		}

		@Nonnull
		public final IClassBuilder setBases(@Nonnull List<IClass> bases) {
			this.bases = new ArrayList<>(bases);
			return this;
		}
	}
}
