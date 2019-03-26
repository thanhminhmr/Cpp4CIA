package cia.cpp.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public final class ClassNode extends Node implements IClass {
	@Nonnull
	private final Visibility visibility;
	@Nonnull
	private final List<IClass> bases;
	@Nonnull
	private final Visibility childDefaultVisibility;

	private ClassNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nonnull Visibility visibility, @Nonnull List<IClass> bases, @Nonnull Visibility childDefaultVisibility) {
		super(name, simpleName, uniqueName);
		this.visibility = visibility;
		this.bases = List.copyOf(bases);
		this.childDefaultVisibility = childDefaultVisibility;
	}

	public static ClassNodeBuilder builder() {
		return new ClassNodeBuilder();
	}

	@Override
	@Nonnull
	public final Visibility getChildDefaultVisibility() {
		return childDefaultVisibility;
	}

	@Nonnull
	@Override
	public final List<IClass> getBases() {
		return bases;
	}

	@Nonnull
	@Override
	public final Visibility getVisibility() {
		return visibility;
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

	@Override
	public String toString() {
		return "(" + getClass().getSimpleName() + ") { name = \"" + getName()
				+ "\", visibility = " + visibility
				+ ", bases = " + bases
				+ ", childDefaultVisibility = " + childDefaultVisibility + " }";
	}

	public static final class ClassNodeBuilder extends NodeBuilder<ClassNode, ClassNodeBuilder> {
		@Nullable
		private Visibility visibility;
		@Nullable
		private Visibility defaultVisibility;
		@Nonnull
		private List<IClass> bases = List.of();

		private ClassNodeBuilder() {
		}

		@Override
		public final ClassNode build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new ClassNode(name, simpleName, uniqueName, visibility, bases, defaultVisibility);
		}

		@Override
		boolean isValid() {
			return super.isValid() && visibility != null && defaultVisibility != null;
		}

		@Nullable
		public final Visibility getVisibility() {
			return visibility;
		}

		@Nonnull
		public final ClassNodeBuilder setVisibility(@Nonnull Visibility visibility) {
			this.visibility = visibility;
			return this;
		}

		@Nullable
		public final Visibility getDefaultVisibility() {
			return defaultVisibility;
		}

		@Nonnull
		public final ClassNodeBuilder setDefaultVisibility(@Nonnull Visibility defaultVisibility) {
			this.defaultVisibility = defaultVisibility;
			return this;
		}

		@Nonnull
		public final List<IClass> getBases() {
			return bases;
		}

		@Nonnull
		public final ClassNodeBuilder setBases(@Nonnull List<IClass> bases) {
			this.bases = bases;
			return this;
		}
	}
}
