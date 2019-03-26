//package cia.cpp.ast;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//public final class ParameterNode extends Node implements IParameter {
//	@Nullable
//	private IType type;
//
//	private ParameterNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nullable IType type) {
//		super(name, simpleName, uniqueName);
//		this.type = type;
//	}
//
//	public static ParameterNodeBuilder builder() {
//		return new ParameterNodeBuilder();
//	}
//
//	@Nullable
//	@Override
//	public final IType getType() {
//		return type;
//	}
//
//	@Override
//	public final void setType(@Nullable IType type) {
//		this.type = type;
//	}
//
//	public static final class ParameterNodeBuilder extends NodeBuilder<ParameterNode, ParameterNodeBuilder> {
//		@Nullable
//		private IType type;
//
//		@Nonnull
//		@Override
//		public final ParameterNode build() {
//			if (isValid()) throw new NullPointerException("Builder element(s) is null.");
//			//noinspection ConstantConditions
//			return new ParameterNode(name, simpleName, uniqueName, type);
//		}
//
//		@Nullable
//		public final IType getType() {
//			return type;
//		}
//
//		@Nonnull
//		public final ParameterNodeBuilder setType(@Nullable IType type) {
//			this.type = type;
//			return this;
//		}
//	}
//}
