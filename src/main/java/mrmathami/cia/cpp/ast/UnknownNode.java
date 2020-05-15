package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class UnknownNode extends CppNode {
	private static final long serialVersionUID = 4828860334527211201L;

	public UnknownNode() {
	}

	public UnknownNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		throw new IOException("UnknownNode is not Serializable");
	}

	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
		throw new IOException("UnknownNode is not Serializable");
	}
	//</editor-fold>
}
