package cia.cpp;

import mrmathami.util.tree.TreeNode;
import org.anarres.cpp.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.core.runtime.CoreException;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public final class AstBuilder {
	private static final char[] EMPTY_CHARS = new char[0];

	private final Set<File> projectFiles;


	public AstBuilder(Set<File> projectFiles) {
		this.projectFiles = projectFiles.stream().map(AstBuilder::getCanonicalAbsoluteFile).collect(Collectors.toUnmodifiableSet());
	}

	private static String getCanonicalAbsolutePath(File file) {
		try {
			return file.getCanonicalPath();
		} catch (IOException ignored) {
			return file.getAbsolutePath();
		}
	}

	private static File getCanonicalAbsoluteFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ignored) {
			return file.getAbsoluteFile();
		}
	}

	private static char[] readFile(File file) {
		final StringBuilder content = new StringBuilder();
		final char[] fileBuffer = new char[65536]; // 64k at a time, fast
		try (final FileReader fileReader = new FileReader(file)) {
			int length = fileReader.read(fileBuffer);
			while (length != -1) {
				content.append(fileBuffer, 0, length);
				length = fileReader.read(fileBuffer);
			}
		} catch (IOException e) {
			return EMPTY_CHARS;
		}
		final char[] fileContent = new char[content.length()];
		content.getChars(0, content.length(), fileContent, 0);
		return fileContent;
	}

	private static Set<File> readConfigFile(File file) throws IOException {
		final StringBuilder content = new StringBuilder();
		final char[] fileBuffer = new char[65536]; // 64k at a time, fast
		try (final FileReader fileReader = new FileReader(file)) {
			int length = fileReader.read(fileBuffer);
			while (length != -1) {
				content.append(fileBuffer, 0, length);
				length = fileReader.read(fileBuffer);
			}
		}
		final Set<File> fileSet = new HashSet<>();
		for (final String filePath : content.toString().split("[\r\n]+")) {
			if (filePath != null && !filePath.isBlank()) {
				final File fileInSet = new File(filePath);
				if (fileInSet.exists()) {
					fileSet.add(fileInSet);
				}
			}
		}
		return fileSet;
	}

	public static void main(String[] args) throws Exception {


		AstBuilder astBuilder = new AstBuilder(
//				List.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\main.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.h")
//				),
				readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"))
		);
//		astBuilder.build(new File("D:\\Research\\SourceCodeComparator\\test\\tiny_but_decent\\Test\\Source.cpp"));
		long start_time = System.nanoTime();
		astBuilder.build();
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}

	private static void printer(PrintStream printStream, int level, IASTNode node, IASTTranslationUnit translationUnit) {
		char[] tabLevel = new char[level];
		for (int i = 0; i < tabLevel.length; i++) tabLevel[i] = '\t';


		if (node instanceof IASTName) {
			IBinding iBinding = ((IASTName) node).resolveBinding();
			IASTName[] names = iBinding != null ? translationUnit.getDeclarationsInAST(iBinding) : null;

			printStream.printf("%s%-" + (100 - level * 4) + "s + %-100s + %-50s | %-50s | %-50s | %s\n",
					String.valueOf(tabLevel),
					String.format("%-" + (100 - level * 4 - 13) + "s (0x%08X)", node.getClass().getSimpleName(), node.hashCode()),
					node,
					node.getFileLocation(),
					iBinding != null ? String.format("(0x%08X) %s", iBinding.hashCode(), iBinding.getClass().getSimpleName()) : null,
					iBinding != null ? iBinding.getName() : null,
					iBinding != null ? Arrays.toString(Arrays.stream(names).map(iastName -> {
						if (iastName == null) return "{ null } ";
						IASTImageLocation location = iastName.getImageLocation();
						if (location == null) return "{ " + iastName.toString() + " } ";
						return "{ " + iastName.toString() + ", " + location.getFileName() + "["
								+ location.getNodeOffset() + ", " + (location.getNodeOffset()
								+ location.getNodeLength()) + "] } ";
					}).toArray()) : null
			);
		} else {
			String raw = node.getRawSignature();
			int cr = raw.indexOf('\r');
			int lf = raw.indexOf('\n');
			int line = (cr < 0 || lf < 0) ? Integer.max(cr, lf) : Integer.min(cr, lf);
			String rawSub = (line < 0) ? raw : raw.substring(0, line);

			printStream.printf("%s%-" + (100 - level * 4) + "s | %-100s | %s\n",
					String.valueOf(tabLevel),
					String.format("%-" + (100 - level * 4 - 13) + "s (0x%08X)", node.getClass().getSimpleName(), node.hashCode()),
					rawSub,
					node.getFileLocation()
			);

		}

		for (IASTNode child : node.getChildren()) {
			printer(printStream, level + 1, child, translationUnit);
		}
	}

	private static void createBindings(IASTNode node, Map<IBinding, TreeNode<IBinding>> bindingMap) {
		if (node instanceof IASTName) {
			IBinding binding = ((IASTName) node).resolveBinding();
			while (binding != null && !(binding instanceof ISemanticProblem) && !bindingMap.containsKey(binding)) {
				bindingMap.put(binding, new TreeNode<>(binding));
				binding = binding.getOwner();
			}
		}
		final IASTNode[] children = node.getChildren();
		if (children == null) return;
		for (IASTNode child : children) {
			createBindings(child, bindingMap);
		}
	}

	private static void createBindingTree(TreeNode<IBinding> treeRoot, IASTTranslationUnit translationUnit) {
		final Map<IBinding, TreeNode<IBinding>> bindingMap = new HashMap<>(16384);
		createBindings(translationUnit, bindingMap);
		for (final Map.Entry<IBinding, TreeNode<IBinding>> entry : bindingMap.entrySet()) {
			final IBinding binding = entry.getKey();
			final TreeNode<IBinding> node = entry.getValue();
			final IBinding parentBinding = binding.getOwner();
			if (parentBinding == null) {
				treeRoot.addChild(node);
			} else if (!(parentBinding instanceof ISemanticProblem)) {
				final TreeNode<IBinding> parentNode = bindingMap.get(parentBinding);
				parentNode.addChild(node);
			}
		}
	}

	private void build() throws IOException {
		final Set<File> includePaths = TranslationUnitBuilder.createIncludePaths(projectFiles);
//		final Map<File, Set<File>> includeMap = TranslationUnitBuilder.createIncludeMap(projectFiles, includePaths);

		final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(projectFiles, includePaths);

//		{
//			final File logFile = new File("R:\\preprocessed.log");
//			try (final FileOutputStream fileOutputStream = new FileOutputStream(logFile)) {
//				try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
//					try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
//						printer(printStream, 0, translationUnit, translationUnit);
//					}
//				}
//			}
//		}

		final TreeNode<IBinding> treeRoot = new TreeNode<>(null);
		createBindingTree(treeRoot, translationUnit);
		{
			final File logFile = new File("R:\\treeRoot.log");
			try (final FileOutputStream fileOutputStream = new FileOutputStream(logFile)) {
				try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
					try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
						printStream.println(treeRoot);
					}
				}
			}
		}
	}


	private static final class TranslationUnitBuilder {
		private static final IncludeFileContentProvider EMPTY_PROVIDER = IncludeFileContentProvider.getEmptyFilesProvider();
		private static final IParserLogService LOG_SERVICE = new DefaultLogService();
		private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
		private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

		private TranslationUnitBuilder() {
		}

		private static Set<File> createIncludePaths(Set<File> projectFiles) {
			final Set<File> includePaths = new HashSet<>();
			for (final File projectFile : projectFiles) {
				includePaths.add(getCanonicalAbsoluteFile(projectFile.getParentFile()));
			}
			return includePaths;
		}

		private static Set<File> createFileIncludes(IASTTranslationUnit translationUnit, Set<File> projectFiles, Set<File> includePaths, File currentFolder) {
			final IASTPreprocessorIncludeStatement[] includeDirectives = translationUnit.getIncludeDirectives();
			final Set<File> includeList = new HashSet<>();
			for (final IASTPreprocessorIncludeStatement includeDirective : includeDirectives) {
				if (includeDirective.isActive()) {
					final String includeFileName = includeDirective.getName().toString();
					final File includeFile = getCanonicalAbsoluteFile(new File(currentFolder, includeFileName));
					if (projectFiles.contains(includeFile)) {
						includeList.add(includeFile);
						continue;
					}
					for (final File includePath : includePaths) {
						final File file = getCanonicalAbsoluteFile(new File(includePath, includeFileName));
						if (projectFiles.contains(file)) {
							includeList.add(file);
							break;
						}
					}
				}
			}
			return includeList;
		}

		private static Map<File, Set<File>> createIncludeMap(Set<File> projectFiles, Set<File> includePaths) {
			final Map<File, Set<File>> includeMap = new HashMap<>();
			for (final File currentFile : projectFiles) {
				try {
					final IASTTranslationUnit translationUnit = GPP_LANGUAGE.getASTTranslationUnit(
							FileContent.create(currentFile.getName(), readFile(currentFile)),
							SCANNER_INFO, EMPTY_PROVIDER, null,
							ILanguage.OPTION_NO_IMAGE_LOCATIONS
									| ILanguage.OPTION_SKIP_FUNCTION_BODIES
									| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS,
							LOG_SERVICE);

					includeMap.put(currentFile, createFileIncludes(translationUnit, projectFiles, includePaths, currentFile.getParentFile()));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return includeMap;
		}

		private static IASTTranslationUnit build(Set<File> projectFiles, Set<File> includePaths) {
			final char[] fileContentChars = PreprocessorBuilder.build(projectFiles, includePaths);
			final FileContent fileContent = FileContent.create("preprocessed.cpp", fileContentChars);
			try {
				return GPP_LANGUAGE.getASTTranslationUnit(
						fileContent, SCANNER_INFO, EMPTY_PROVIDER, null,
						ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
								| ILanguage.OPTION_NO_IMAGE_LOCATIONS,
						LOG_SERVICE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

	private static final class PreprocessorBuilder implements PreprocessorListener {
		private static final PreprocessorBuilder EMPTY_PREPROCESSOR_LISTENER = new PreprocessorBuilder();

		private PreprocessorBuilder() {
		}

		private static String getFileExtension(File file) {
			final String filename = file.getName();
			final int dot = filename.lastIndexOf('.');
			return dot >= 0 ? filename.substring(dot) : "";
		}

		private static int fileCompare(File fileA, File fileB) {
			final int compare = getFileExtension(fileA).compareToIgnoreCase(getFileExtension(fileB));
			if (compare != 0) return compare;
			return getCanonicalAbsolutePath(fileA).compareToIgnoreCase(getCanonicalAbsolutePath(fileB));
		}

		private static Preprocessor createPreprocessor(Set<File> projectFiles, Set<File> includePaths) throws LexerException, IOException {
			final Preprocessor preprocessor = new Preprocessor();
			preprocessor.setListener(EMPTY_PREPROCESSOR_LISTENER);
			preprocessor.addFeature(Feature.DIGRAPHS);
			preprocessor.addFeature(Feature.TRIGRAPHS);
			preprocessor.addFeature(Feature.LINEMARKERS);
			preprocessor.addMacro("__JCPP__");
			preprocessor.addMacro("__cplusplus");

			final List<String> includePathStrings = new ArrayList<>();
			for (final File file : includePaths) includePathStrings.add(file.getPath());
			preprocessor.setQuoteIncludePath(includePathStrings);
			preprocessor.setSystemIncludePath(includePathStrings);

			final List<File> projectFileList = new ArrayList<>(projectFiles);
			projectFileList.sort(PreprocessorBuilder::fileCompare);
			for (final File sourceFile : projectFiles) preprocessor.addInput(sourceFile);

			return preprocessor;
		}

		private static StringBuilder runPreprocessor(Set<File> projectFiles, Set<File> includePaths) throws IOException, LexerException {
			final Preprocessor preprocessor = createPreprocessor(projectFiles, includePaths);
			final StringBuilder fileContent = new StringBuilder();

			int emptyLine = 1;
			final StringBuilder emptyLineBuilder = new StringBuilder();
			while (true) {
				final Token tok = preprocessor.token();
				if (tok.getType() == Token.EOF) break;

				/*
				DON'T PANIC. THERE IS NOTHING HERE USEFUL. SKIP IT.
				BASICALLY IT TRIM THE EMPTY LINES, ONLY KEEP AT MOST 2 SEQUENCE EMPTY LINES
				 */

				if (tok.getType() != Token.CCOMMENT && tok.getType() != Token.CPPCOMMENT) {
					final String tokText = tok.getText().replace("\r\n", "\n").replace("\r", "\n");
					if (tok.getType() != Token.WHITESPACE && !tokText.isBlank()) {
						if (tok.getType() != Token.P_LINE && emptyLine > 0) {
							fileContent.append(emptyLineBuilder);
						}
						fileContent.append(tokText);
						emptyLineBuilder.setLength(0);
						emptyLine = 0;
					} else {
						if (!tokText.contains("\n")) {
							if (emptyLine == 0) {
								fileContent.append(' ');
							} else {
								emptyLineBuilder.append(tokText);
							}
						} else if (emptyLine < 2) {
							fileContent.append('\n');
							emptyLineBuilder.setLength(0);
							emptyLine += 1;
						} else {
							emptyLineBuilder.setLength(0);
						}
					}
				}
			}

			return fileContent;
		}

		private static char[] build(Set<File> projectFiles, Set<File> includePaths) {
			try {
				final StringBuilder fileContent = runPreprocessor(projectFiles, includePaths);
				char[] content = new char[fileContent.length()];
				fileContent.getChars(0, content.length, content, 0);
				return content;
			} catch (LexerException | IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void handleWarning(@Nonnull Source source, int line, int column, @Nonnull String msg) {
			System.out.println(source.getName() + ":" + line + ":" + column + ": warning: " + msg);
		}

		@Override
		public void handleError(@Nonnull Source source, int line, int column, @Nonnull String msg) {
			System.out.println(source.getName() + ":" + line + ":" + column + ": error: " + msg);
		}

		@Override
		public void handleSourceChange(@Nonnull Source source, @Nonnull SourceChangeEvent event) {
		}
	}
}
