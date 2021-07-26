package mrmathami.cia.cpp.preprocessor;

import mrmathami.annotations.Nonnull;
import mrmathami.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public interface TokenSource {
	@Nonnull
	String getName();

	@Nullable
	Path getPath();

	@Nonnull
	PreprocessingToken nextToken() throws IOException, PreprocessorException;
}
