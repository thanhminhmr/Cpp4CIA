package mrmathami.utils;

import mrmathami.annotations.Nonnull;
import org.mozilla.universalchardet.UniversalDetector;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.SequenceInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

public final class AutoEncodingReader extends BufferedReader {

	private static final byte[][] BOM_LIST = new byte[][]{
			new byte[]{(byte) 0xFF, (byte) 0xFE, 0x00, 0x00}, // UTF_32_LE
			new byte[]{0x00, 0x00, (byte) 0xFE, (byte) 0xFF}, // UTF_32_BE
			new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}, // UTF_8
			new byte[]{(byte) 0xFF, (byte) 0xFE}, // UTF_16_LE
			new byte[]{(byte) 0xFE, (byte) 0xFF} // UTF_16_BE
	};

	@Nonnull
	private static Reader autoEncodingReader(@Nonnull InputStream inputStream) throws IOException {
		final UniversalDetector detector = new UniversalDetector(null);
		final byte[] bytes = feedDetector(detector, inputStream);
		final String encoding = detector.getDetectedCharset();
		final Charset charset = encoding != null ? Charset.forName(encoding) : Charset.defaultCharset();
		final InputStream mergedInputStream = new SequenceInputStream(skipBOM(charset, bytes), inputStream);
		return new InputStreamReader(new BufferedInputStream(mergedInputStream), charset);
	}

	@Nonnull
	private static InputStream skipBOM(@Nonnull Charset charset, @Nonnull byte[] prefixBytes) {
		if (charset.name().contains("UTF")) {
			final int prefixLength = prefixBytes.length;
			for (final byte[] bomBytes : BOM_LIST) {
				final int bomLength = bomBytes.length;
				if (prefixLength >= bomLength
						&& Arrays.compare(bomBytes, 0, bomLength, prefixBytes, 0, bomLength) == 0) {
					return prefixLength != bomLength
							? new ByteArrayInputStream(prefixBytes, bomLength, prefixLength - bomLength)
							: InputStream.nullInputStream();
				}
			}
		}
		return new ByteArrayInputStream(prefixBytes);
	}

	@Nonnull
	private static byte[] feedDetector(@Nonnull UniversalDetector detector, @Nonnull InputStream inputStream)
			throws IOException {
		final ByteArrayOutputStream bytesBuilder = new ByteArrayOutputStream();
		{
			// first 4 bytes
			final byte[] bytes = inputStream.readNBytes(4);
			detector.handleData(bytes);
			bytesBuilder.write(bytes);
			final int length = bytes.length;
			if (length < 4 || detector.isDone()) {
				if (length < 4) detector.dataEnd();
				return bytesBuilder.toByteArray();
			}
		}
		final byte[] buffer = new byte[4096];
		do {
			final int length = inputStream.read(buffer);
			if (length <= 0) {
				detector.dataEnd();
				break;
			}
			bytesBuilder.write(buffer, 0, length);
			detector.handleData(buffer, 0, length);
		} while (!detector.isDone());

		return bytesBuilder.toByteArray();
	}

	public AutoEncodingReader(@Nonnull InputStream inputStream) throws IOException {
		super(autoEncodingReader(inputStream));
	}

}
