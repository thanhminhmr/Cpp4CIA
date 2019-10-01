package mrmathami.cia.cpp.ast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public final class UnknownNode extends Node {
	private static final long serialVersionUID = 5253244210630760970L;

	public UnknownNode() {
	}

	private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
		throw new IOException("UnknownNode is not Serializable");
	}

	private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
		throw new IOException("UnknownNode is not Serializable");
	}
}
