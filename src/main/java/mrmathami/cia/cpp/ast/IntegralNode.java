package mrmathami.cia.cpp.ast;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.ObjectOutputStream;

public final class IntegralNode extends CppNode {
	private static final long serialVersionUID = 5082730452633391643L;

	public IntegralNode() {
	}

	public IntegralNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String signature) {
		super(name, uniqueName, signature);
	}

	//<editor-fold desc="Object Helper">
	private void writeObject(ObjectOutputStream outputStream) throws IOException {
		if (getParent() == null) {
			throw new IOException("Only RootNode is directly Serializable!");
		}
		outputStream.defaultWriteObject();
	}
	//</editor-fold>
}
