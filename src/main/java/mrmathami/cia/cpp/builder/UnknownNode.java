package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.ast.Node;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

final class UnknownNode extends Node implements IUnknown {
	private static final long serialVersionUID = -7116903985998129873L;

	private UnknownNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String content) {
		super(name, uniqueName, content);
	}

	public static IUnknownBuilder builder() {
		return new UnknownBuilder();
	}

	private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
		throw new IOException("UnknownNode is not Serializable");
	}

	private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
		throw new IOException("UnknownNode is not Serializable");
	}

	private static final class UnknownBuilder extends NodeBuilder<IUnknown, IUnknownBuilder> implements IUnknownBuilder {
		private UnknownBuilder() {
		}

		@Nonnull
		@Override
		public IUnknown build() {
			if (!isValid()) throw new NullPointerException("Builder element(s) is null.");
			//noinspection ConstantConditions
			return new UnknownNode(name, uniqueName, signature);
		}
	}
}
