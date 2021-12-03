package mrmathami.cia.cpp.preprocessor.codepoint;

import mrmathami.annotations.Nonnull;

import java.io.IOException;
import java.io.Reader;

public abstract class CodepointReader {
	CodepointReader() {
	}


	@Nonnull
	public static CodepointReader fromReader(@Nonnull Reader reader) {
		return new PreprocessingCodepointReader(reader);
	}

	@Nonnull
	public static CodepointReader fromCodepoints(@Nonnull int[] codePoints) {
		return new ArrayCodepointReader(codePoints);
	}


	/**
	 * Read a codepoint, throw IOException if underlying reader throw.
	 */
	public abstract int read() throws IOException;

	/**
	 * Unread previous codepoint, throw IOException when unread too many codepoints.
	 */
	public abstract void unread() throws IOException;

	/**
	 * Unread previous codepoint, throw IOException when unread too many codepoints.
	 */
	public abstract void unreadMany(int count) throws IOException;

	/**
	 * Line number, starting from 1.
	 */
	public abstract int getLine();

	/**
	 * Column number, starting from 1.
	 */
	public abstract int getColumn();

	/**
	 * Check if raw string mode is currently on. Raw string mode disable line splicing and universal character name.
	 */
	public abstract boolean isRawStringMode();

	/**
	 * Turn raw string mode on or off
	 */
	public abstract void setRawStringMode(boolean rawStringMode);
}
