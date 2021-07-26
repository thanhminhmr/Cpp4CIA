package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;
import mrmathami.utils.AutoEncodingReader;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * This convert a reader to a codepoint reader that support unread, convert all
 * line-separator to LF, do line splicing and line:column position tracker
 * (even when unread). The difference with normal pushback is that you cannot
 * change the content of the reader when unread.
 */
final class LexerReader implements Closeable {
	LexerReader(@Nonnull InputStream inputStream) throws IOException {
		this(new AutoEncodingReader(inputStream));
	}

	LexerReader(@Nonnull Reader reader) throws IOException {
		this.reader = reader instanceof BufferedReader ? reader : new BufferedReader(reader);
	}

	// ====

	@Nullable private Reader reader;

	private int underlyingReadCodePoint() throws IOException, PreprocessorException {
		assert reader != null;
		int u0 = reader.read();
		if (u0 < 0 || !Character.isHighSurrogate((char) u0)) {
			// eof or normal character
			return u0;
		}
		int u1 = reader.read();
		if (u1 >= 0 && Character.isLowSurrogate((char) u1)) {
			// extended character
			return Character.toCodePoint((char) u0, (char) u1);
		}
		// invalid/incomplete pair
		throw new PreprocessorException("Invalid input surrogate pair.");
	}

	// ====

	private static final int MAX_UNREAD_COUNT = 1024;
	private static final int BUFFER_STRIDE = 3;
	private static final int BUFFER_LENGTH = MAX_UNREAD_COUNT * BUFFER_STRIDE;

	@Nonnull private final int[] buffer = new int[BUFFER_LENGTH];
	private int bufferPointer = BUFFER_LENGTH - BUFFER_STRIDE;
	private int bufferValidCount = 0;
	private int bufferUnreadCount = 0;

	private int bufferRereadAvailable() {
		return bufferUnreadCount;
	}

	private int bufferUnreadAvailable() {
		return bufferValidCount - bufferUnreadCount;
	}

	private void bufferAdd(int codepoint, int line, int column) {
		assert bufferUnreadCount == 0 : "Buffer add while reread are available!";
		// increase valid count
		this.bufferValidCount = Math.min(bufferValidCount + 1, BUFFER_LENGTH);
		// increase buffer pointer
		final int uncheckedPointer = this.bufferPointer + BUFFER_STRIDE;
		final int newPointer = this.bufferPointer = uncheckedPointer < BUFFER_LENGTH ? uncheckedPointer : 0;
		// write to buffer
		final int[] buffer = this.buffer;
		buffer[newPointer] = codepoint;
		buffer[newPointer + 1] = line;
		buffer[newPointer + 2] = column;
	}

	private void bufferUnread() {
		assert bufferValidCount > bufferUnreadCount : "Buffer unread too many codepoints!";
		// increase unread count
		this.bufferUnreadCount += 1;
		// decrease buffer pointer
		final int uncheckedPointer = this.bufferPointer - BUFFER_STRIDE;
		this.bufferPointer = uncheckedPointer >= 0 ? uncheckedPointer : BUFFER_LENGTH - BUFFER_STRIDE;

	}

	private void bufferUnreadMany(int count) {
		assert bufferValidCount >= bufferUnreadCount + count : "Buffer unread too many codepoints!";
		// increase unread count
		this.bufferUnreadCount += count;
		// decrease buffer pointer
		final int pointerOffset = count * BUFFER_STRIDE;
		final int uncheckedPointer = bufferPointer - pointerOffset;
		this.bufferPointer = uncheckedPointer >= 0 ? uncheckedPointer : BUFFER_LENGTH - pointerOffset;

	}

	private int bufferReread() {
		assert bufferUnreadCount > 0 : "Buffer reread when empty!";
		// decrease unread count
		this.bufferUnreadCount -= 1;
		// decrease buffer pointer
		final int uncheckedPointer = this.bufferPointer + BUFFER_STRIDE;
		this.bufferPointer = uncheckedPointer < BUFFER_LENGTH ? uncheckedPointer : 0;
		return buffer[bufferPointer];
	}

	private void bufferErase() {
		assert bufferUnreadCount == 0 : "Buffer erase while reread are available!";
		assert bufferValidCount > 0 : "Buffer erase when empty!";
		// decrease valid count
		this.bufferValidCount -= 1;
		// decrease buffer pointer
		final int uncheckedPointer = bufferPointer - BUFFER_STRIDE;
		this.bufferPointer = uncheckedPointer >= 0 ? uncheckedPointer : BUFFER_LENGTH - BUFFER_STRIDE;
	}

	private void bufferEraseMany(int count) {
		assert bufferUnreadCount == 0 : "Buffer erase while reread are available!";
		assert bufferValidCount >= count : "Buffer erase too many codepoints!";
		// decrease valid count
		this.bufferValidCount -= count;
		// decrease buffer pointer
		final int pointerOffset = count * BUFFER_STRIDE;
		final int uncheckedPointer = bufferPointer - pointerOffset;
		this.bufferPointer = uncheckedPointer >= 0 ? uncheckedPointer : BUFFER_LENGTH - pointerOffset;
	}

	private boolean bufferIsValid() {
		return bufferValidCount > 0;
	}

	private int bufferGetCodepoint() {
		return buffer[bufferPointer];
	}

	private int bufferGetLine() {
		return buffer[bufferPointer + 1];
	}

	private int bufferGetColumn() {
		return buffer[bufferPointer + 2];
	}

	// ====

	private boolean previousCR = false;
	private int nextLine = 1;
	private int nextColumn = 0;

	private int readNewRaw() throws IOException, PreprocessorException {
		// read next cp, return if EOF
		int cp = underlyingReadCodePoint();
		if (cp < 0) return -1;
		// skip LF after previous CR
		if (previousCR && cp == '\n') cp = underlyingReadCodePoint();
		// substitute CR with LF
		cp = (this.previousCR = cp == '\r') ? '\n' : cp;
		// buffer add
		bufferAdd(cp, nextLine, nextColumn);
		// change nextLine and nextColumn
		this.nextLine += cp == '\n' ? 1 : 0;
		this.nextColumn = cp == '\n' ? 1 : nextColumn + 1;
		return cp;
	}

	// ====

	private int repeatNewLine = 0;

	private int readWithRepeatNewLine() throws PreprocessorException, IOException {
		// check if current character is a new line
		if (bufferGetCodepoint() == '\n') {
			// check if any repeat new line is necessary
			if (repeatNewLine <= 0) return readNewRaw();
		} else {
			// if the last character of the file not a new line, add a new line there
			final int cp = readNewRaw();
			if (cp >= 0) return cp;
		}
		// repeat the new line
		this.repeatNewLine -= 1;
		bufferAdd('\n', bufferGetLine(), bufferGetColumn());
		return '\n';
	}

	// ====

	/**
	 * Return negative when it detect a UCN starting sequence
	 * Return positive when it doesn't detect that
	 */
	private int processUniversalCharacterName() throws PreprocessorException, IOException {
		// is this an universal character name
		final int currentLine = bufferGetLine(), currentColumn = bufferGetColumn();
		final int nextCP = readNewRaw();
		if (nextCP == 'u' || nextCP == 'U') {
			final int length = nextCP == 'u' ? 4 : 8;
			int ucn = 0;
			for (int i = 0; i < length; i++) {
				final int hexCP = readNewRaw();
				if (hexCP >= '0' && hexCP <= '9') {
					ucn = ucn * 16 + hexCP - '0';
				} else if (hexCP >= 'A' && hexCP <= 'F' || hexCP >= 'a' && hexCP <= 'f') {
					ucn = ucn * 16 + 9 + (hexCP & 7);
				} else {
					// not an UCN, check if current thing is a slash
					if (hexCP == '\\') processUniversalCharacterName();
					// unread and return the backslash
					bufferUnreadMany(i + (hexCP < 0 ? 1 : 2));
					return -'\\';
				}
			}
			if (ucn < 0 || ucn > 0X10FFFF) {
				throw new PreprocessorException("Universal character name form an invalid code point: " + ucn);
			} else if (ucn >= 0xD800 && ucn <= 0xDFFF) {
				throw new PreprocessorException("Universal character name form a surrogate code point: " + ucn);
			}
			// erasing the UCN escape sequence
			// including the backslash and the universal escape character 'u' or 'U'
			bufferEraseMany(length + 2);
			bufferAdd(ucn, currentLine, currentColumn);
			return -ucn;
		}
		return nextCP;
	}

	private int readNewProcessed() throws IOException, PreprocessorException {
		// start reading
		int cp =  readWithRepeatNewLine();
		while (true) {
			if (cp != '\\') return cp;
			// check if the backslash form universal character name
			int nextCP = processUniversalCharacterName();
			if (nextCP < 0) return -nextCP;
			// check if the backslash form line splicing
			int length = 1;
			while (true) {
				// check for valid line splicing
				if (nextCP == '\n' || nextCP == -1) {
					// yes, it is a valid line splicing
					// ... then erase all read-ahead characters and the backslash
					// note that
					bufferEraseMany(nextCP == -1 ? length : length + 1);
					// add a virtual new line
					this.repeatNewLine += 1;
					break;
				}
				// check if it still a whitespace
				if (nextCP != '\t' && nextCP != '\u000B' /* VT */ && nextCP != '\f' && nextCP != ' ') {
					// no, it is not
					// check for valid universal character name
					if (nextCP == '\\') processUniversalCharacterName();
					// invalid line splicing, unread those read-ahead...
					bufferUnreadMany(length);
					return '\\';
				}
				if (++length >= MAX_UNREAD_COUNT) {
					// TODO: buffer extend instead of throwing
					throw new IOException("Line splicing read-ahead too many characters!");
				}
				nextCP = readNewRaw();
			}
			cp = readNewRaw();
		}
	}

	/**
	 * Read back from buffer (redo read).
	 * Note that previousCR never change from unread/reread, it should be unchanged from a readNew to next readNew.
	 */
	private int reread() {
		return bufferReread();
	}

	// ====

	private boolean rawStringMode = false;

	/**
	 * Read a codepoint, throw IOException if underlying reader throw.
	 */
	int read() throws IOException, PreprocessorException {
		if (reader == null) throw new IOException("Already closed.");
		return bufferRereadAvailable() == 0
				? rawStringMode ? readNewRaw() : readNewProcessed()
				: bufferReread();
	}

	/**
	 * Unread previous codepoint, throw IOException when unread too many codepoints.
	 */
	void unread() throws IOException {
		if (reader == null) throw new IOException("Already closed.");
		// is buffer still has room to unread?
		if (bufferUnreadAvailable() == 0) throw new IOException("Unread too many!");
		// do the unread
		bufferUnread();
	}

	/**
	 * Unread previous codepoint, throw IOException when unread too many codepoints.
	 */
	void unreadMany(int count) throws IOException {
		if (reader == null) throw new IOException("Already closed.");
		// is buffer still has room to unread?
		if (bufferUnreadAvailable() < count) throw new IOException("Unread too many!");
		// do the unread
		bufferUnreadMany(count);
	}

	/**
	 * Line number start from 1
	 */
	int getLine() {
		if (bufferIsValid()) return bufferGetLine();
		throw new IllegalStateException("Buffer currently empty!");
	}

	/**
	 * Column number start from 1
	 */
	int getColumn() {
		if (bufferIsValid()) return bufferGetColumn();
		throw new IllegalStateException("Buffer currently empty!");
	}

	/**
	 * Check if raw string mode is currently on
	 * Raw string mode disable line splicing and universal character name
	 */
	boolean isRawStringMode() {
		return rawStringMode;
	}

	/**
	 * Turn raw string mode on or off
	 */
	void setRawStringMode(boolean rawStringMode) {
		this.rawStringMode = rawStringMode;
	}

	@Override
	public synchronized void close() throws IOException {
		if (reader != null) {
			reader.close();
			this.reader = null;
		}
	}


	public static void main(String[] strings) throws IOException, PreprocessorException {
		final StringReader stringReader = new StringReader(
				"hello \\u1234 hi\\\n" +
						"\\\n" +
						"\\\n" +
						"\\\n" +
						"\\\n" +
						"\\u\\u0100\n" +
						"\\\n" +
						"\\\n" +
						"\\\n" +
						"what is this\n" +
						"is this a joke"
		);
		final LexerReader reader = new LexerReader(stringReader);

		while (true) {
			final int cp = reader.read();
			if (cp < 0) break;
			System.out.print(Character.toChars(cp));
		}
		System.out.println();
	}
}
