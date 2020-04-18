package mrmathami.cia.cpp.ast;

import java.io.IOException;
import java.io.ObjectOutputStream;

public final class IntegralNode extends Node {
	private static final long serialVersionUID = -4501278124054642537L;

	public IntegralNode() {
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) throw new IOException("Only RootNode is directly Serializable!");
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}
