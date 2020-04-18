package mrmathami.cia.cpp.ast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class UnknownNode extends Node {
	private static final long serialVersionUID = 5253244210630760970L;

	public UnknownNode() {
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
