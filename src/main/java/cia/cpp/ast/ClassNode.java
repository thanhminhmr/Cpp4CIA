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

	private ClassNode(@Nonnull String name, @Nonnull Visibility visibility, @Nonnull List<IClass> bases, @Nonnull Visibility childDefaultVisibility) {
		super(name);
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

	public static final class ClassNodeBuilder extends NodeBuilder<ClassNode, ClassNodeBuilder> {
		@Nullable
		private Visibility visibility;
		@Nullable
		private Visibility defaultVisibility;
		@Nullable
		private List<IClass> bases;

		private ClassNodeBuilder() {
		}

		@Override
		public final ClassNode build() {
			if (name == null || visibility == null || defaultVisibility == null || bases == null) {
				throw new NullPointerException("Builder element(s) is null.");
			}
			return new ClassNode(name, visibility, bases, defaultVisibility);
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

		@Nullable
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
