//package cia.cpp.ast;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//import java.io.Serializable;
//import java.util.List;
//
//public final class TypedefNode extends Node implements ITypedef, Serializable {
//	@Nullable
//	private INode type;
//
//	private TypedefNode(@Nonnull String name, @Nonnull String simpleName, @Nonnull String uniqueName, @Nullable INode type) {
//		super(name, simpleName, uniqueName);
//		this.type = type;
//	}
//
//	@Nonnull
//	public static ITypedefBuilder builder() {
//		return new TypedefNodeBuilder();
//	}
//
//	@Nullable
//	@Override
//	public final INode getType() {
//		return type;
//	}
//
//	@Override
//	public final void setType(@Nullable INode type) {
//		this.type = type;
//	}
//
//	@Nonnull
//	@Override
//	public String toString() {
//		return "(" + objectToString(this)
//				+ ") { name: \"" + getName()
//				+ "\", uniqueName: \"" + getUniqueName()
//				+ "\", signature: \"" + getSignature()
//				+ "\", type: " + type
//				+ " }";
//	}
//
//	@Nonnull
//	@Override
//	public String toTreeElementString() {
//		return "(" + objectToString(this)
//				+ ") { name: \"" + getName()
//				+ "\", uniqueName: \"" + getUniqueName()
//				+ "\", signature: \"" + getSignature()
//				+ "\", dependencyMap: " + mapToString(getDependencyMap())
//				+ ", type: " + type
//				+ " }";
//	}
//
//	public static final class TypedefNodeBuilder extends NodeBuilder<ITypedef, ITypedefBuilder> implements ITypedefBuilder {
//		@Nullable
//		private INode type;
//
//		private TypedefNodeBuilder() {
//		}
//
//		@Nonnull
//		@Override
//		public final ITypedef build() {
//			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
//			//noinspection ConstantConditions
//			return new TypedefNode(name, uniqueName, signature, type);
//		}
//
//		@Override
//		@Nullable
//		public final INode getType() {
//			return type;
//		}
//
//		@Override
//		@Nonnull
//		public final ITypedefBuilder setType(@Nullable INode type) {
//			this.type = type;
//			return this;
//		}
//	}
//}
