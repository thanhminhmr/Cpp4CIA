package mrmathami.cia.cpp.preprocessor.codepoint;

import mrmathami.annotations.Nonnull;

import java.io.IOException;

final class ArrayCodepointReader extends CodepointReader {
	private static final int BUFFER_STRIDE = 3;
	@Nonnull private final int[] buffer;

	private int index = 0;

	ArrayCodepointReader(@Nonnull int[] codePoints) {
		int length = codePoints.length;
		final int[] buffer = new int[length * BUFFER_STRIDE];
		for (int codepointIndex = 0, bufferIndex = 0, line = 1, column = 1;
				codepointIndex < length; codepointIndex++, bufferIndex += 3) {
			int codePoint = codePoints[codepointIndex];
			buffer[bufferIndex] = codePoint;
			buffer[bufferIndex + 1] = line;
			buffer[bufferIndex + 2] = column;
			if (codePoint == '\n') {
				line = 1;
				column += 1;
			} else {
				line += 1;
			}
		}
		this.buffer = buffer;
	}

	@Override
	public int read() throws IOException {
		return index < buffer.length ? buffer[this.index += BUFFER_STRIDE] : -1;
	}

	@Override
	public void unread() throws IOException {
		if (index <= 0) throw new IOException("Unread too many!");
		this.index -= BUFFER_STRIDE;
	}

	@Override
	public void unreadMany(int count) throws IOException {
		final int delta = count * BUFFER_STRIDE;
		if (index < delta) throw new IOException("Unread too many!");
		this.index -= delta;
	}

	@Override
	public int getLine() {
		if (index > 0) return buffer[index - 2];
		throw new IllegalStateException("Buffer currently empty!");
	}

	@Override
	public int getColumn() {
		if (index > 0) return buffer[index - 1];
		throw new IllegalStateException("Buffer currently empty!");
	}

	@Override
	public boolean isRawStringMode() {
		return true;
	}

	@Override
	public void setRawStringMode(boolean rawStringMode) {
		// do nothing :)
	}
}
