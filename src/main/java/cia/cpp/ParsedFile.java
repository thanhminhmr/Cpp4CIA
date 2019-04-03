//package cia.cpp;
//
//import org.eclipse.cdt.core.dom.astbuilder.IASTTranslationUnit;
//import org.eclipse.cdt.core.dom.astbuilder.gnu.cpp.GPPLanguage;
//import org.eclipse.cdt.core.parser.DefaultLogService;
//import org.eclipse.cdt.core.parser.FileContent;
//import org.eclipse.cdt.core.parser.IParserLogService;
//import org.eclipse.cdt.core.parser.IScannerInfo;
//import org.eclipse.cdt.core.parser.IncludeFileContentProvider;
//import org.eclipse.cdt.core.parser.ScannerInfo;
//
//public class ParsedFile {
//	private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
//	private static final IScannerInfo SCANNER_INFO = new ScannerInfo();
//	private static final IParserLogService LOG_SERVICE = new DefaultLogService();
//	private static final IncludeFileContentProvider EMPTY_FILES_PROVIDER = IncludeFileContentProvider.getEmptyFilesProvider();
//
//	private final IASTTranslationUnit unit;
//
//	private ParsedFile(IASTTranslationUnit unit) {
//		this.unit = unit;
//	}
//
//	/**
//	 * To parse C++ as string.
//	 *
//	 * @param fileContent The code C++ in input as FileContent.
//	 * @return A transition unit about the input string.
//	 * @throws Exception exception
//	 */
//	private static ParsedFile createParsedFileFromFileContent(FileContent fileContent) throws Exception {
//		return new ParsedFile(GPP_LANGUAGE.getASTTranslationUnit(fileContent, SCANNER_INFO, EMPTY_FILES_PROVIDER, null, 0, LOG_SERVICE));
//	}
//
//	private void buildAst() {
//		unit.getDeclarations();
//		unit.freeze();
//	}
//}
