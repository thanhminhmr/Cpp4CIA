package cia.cpp;

import cia.cpp.ast.*;
import mrmathami.util.Pair;
import mrmathami.util.tree.TreeNode;
import org.anarres.cpp.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.parser.*;
import org.eclipse.cdt.internal.core.parser.IMacroDictionary;
import org.eclipse.cdt.internal.core.parser.SavedFilesProvider;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContent;
import org.eclipse.cdt.internal.core.parser.scanner.InternalFileContentProvider;
import org.eclipse.core.runtime.CoreException;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
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

	private static String getCanonicalAbsolutePath(String path) {
		return getCanonicalAbsolutePath(new File(path));
	}

	private static File getCanonicalAbsoluteFile(File file) {
		try {
			return file.getCanonicalFile();
		} catch (IOException ignored) {
			return file.getAbsoluteFile();
		}
	}

	private static File getCanonicalAbsoluteFile(String path) {
		return getCanonicalAbsoluteFile(new File(path));
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
				Set.of(
						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\main.cpp"),
						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.cpp"),
						new File("D:\\Research\\SourceCodeComparator\\test\\TinyEXIF-1.0.1\\TinyEXIF.h")
				)
//				Set.of(
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\zpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.cpp"),
//						new File("D:\\Research\\SourceCodeComparator\\test\\zpaq715\\libzpaq.h")
//				)
//				readConfigFile(new File("D:\\Research\\SourceCodeComparator\\test\\tesseract-4.0.0\\src\\a.txt"))
		);
//		astBuilder.build(new File("D:\\Research\\SourceCodeComparator\\test\\tiny_but_decent\\Test\\Source.cpp"));
		long start_time = System.nanoTime();
		astBuilder.build();
		System.out.println((System.nanoTime() - start_time) / 1000000.0);
	}

	private static void printer(PrintStream printStream, int level, IASTNode node, IASTTranslationUnit translationUnit) {
		char[] tabLevel = new char[level];
		for (int i = 0; i < tabLevel.length; i++) tabLevel[i] = '\t';

		final String raw = node.getRawSignature();
		final int cr = raw.indexOf('\r');
		final int lf = raw.indexOf('\n');
		final int line = (cr < 0 || lf < 0) ? Integer.max(cr, lf) : Integer.min(cr, lf);
		final String rawSub = (line < 0 || line > 60) ? (raw.length() > 60 ? raw.substring(0, 60) : raw) : raw.substring(0, line);


		if (node instanceof IASTName) {
			IBinding iBinding = ((IASTName) node).resolveBinding();
			IASTName[] names = iBinding != null ? translationUnit.getDeclarationsInAST(iBinding) : null;

			printStream.printf("%s%-" + (100 - level * 4 - 13) + "s (0x%08X) + %-60s + %-30s | %-50s | %-50s | %s\n",
					String.valueOf(tabLevel),
					node.getClass().getSimpleName(), node.hashCode(),
					rawSub,
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
			printStream.printf("%s%-" + (100 - level * 4 - 13) + "s (0x%08X) | %-60s | %s\n",
					String.valueOf(tabLevel),
					node.getClass().getSimpleName(), node.hashCode(),
					rawSub,
					node.getFileLocation()
			);

		}

		for (IASTNode child : node.getChildren()) {
			printer(printStream, level + 1, child, translationUnit);
		}
	}

	private static void createBindingMap(Map<IBinding, TreeNode<Pair<IBinding, INode>>> bindingMap, IASTNode node) {
//		final Map<IType, IBinding> TypeBindingMap = new HashMap<>();
		if (node instanceof IASTName) {
			IBinding binding = ((IASTName) node).resolveBinding();
			while (binding != null && !(binding instanceof ISemanticProblem) && !bindingMap.containsKey(binding)) {
				bindingMap.put(binding, new TreeNode<>(new Pair<>(binding, null)));
//				if (binding instanceof)
				binding = binding.getOwner();
			}
		}
		final IASTNode[] children = node.getChildren();
		if (children == null) return;
		for (final IASTNode child : children) {
			createBindingMap(bindingMap, child);
		}
	}

	private static void createBindingTree(TreeNode<Pair<IBinding, INode>> treeRoot, Map<IBinding, TreeNode<Pair<IBinding, INode>>> bindingMap) {
		// TODO: tree walk ?
		for (final Map.Entry<IBinding, TreeNode<Pair<IBinding, INode>>> entry : bindingMap.entrySet()) {
			final IBinding binding = entry.getKey();
			final TreeNode<Pair<IBinding, INode>> node = entry.getValue();
			final IBinding parentBinding = binding.getOwner();
			if (parentBinding == null) {
				treeRoot.addChild(node);
			} else if (!(parentBinding instanceof ISemanticProblem)) {
				final TreeNode<Pair<IBinding, INode>> parentNode = bindingMap.get(parentBinding);
				parentNode.addChild(node);
			}
		}
	}

//	private static IMember.Visibility createVisibilityFromMember(ICPPMember member) throws IllegalStateException {
//		switch (member.getVisibility()) {
//			case ICPPMember.v_private:
//				return IMember.Visibility.PRIVATE;
//			case ICPPMember.v_protected:
//				return IMember.Visibility.PROTECTED;
//			case ICPPMember.v_public:
//				return IMember.Visibility.PUBLIC;
//		}
//
//		throw new IllegalStateException("Member have illegal visibility value of " + member.getVisibility());
//	}

	private static String createQualifiedNameFromCPPBinding(ICPPBinding binding) throws DOMException {
//		return String.join("::", binding.getQualifiedName());
		return ASTTypeUtil.getQualifiedName(binding);
	}

	private static String createUniqueNameFromCPPBinding(ICPPBinding binding, IASTTranslationUnit translationUnit) {
		if (binding instanceof ICPPFunction) {
			ICPPFunction function = (ICPPFunction) binding;
			function.getType();
			function.getDeclaredType();
		} else if (binding instanceof ICPPVariable) {
			ICPPVariable variable = (ICPPVariable) binding;
			variable.getType();
			IASTName[] declarationsInAST = translationUnit.getDeclarationsInAST(binding);
			for (IASTName iastName : declarationsInAST) {
			}
		}
		return null;
	}

//
//	private static IMember.Visibility createDefaultVisibilityFromClass(ICPPClassType classType) throws IllegalStateException {
//		switch (classType.getKey()) {
//			case ICPPClassType.k_class:
//				return IMember.Visibility.PRIVATE;
//			case ICompositeType.k_struct:
//			case ICompositeType.k_union:
//				return IMember.Visibility.PUBLIC;
//		}
//
//		throw new IllegalStateException("Class have illegal type value of " + classType.getKey());
//	}

	private static void createAstTree(TreeNode<Pair<IBinding, INode>> parentTreeNode) throws DOMException {
		final Pair<IBinding, INode> parentBindingAstPair = parentTreeNode.getValue();
		assert parentBindingAstPair != null;
		final INode parentAstNode = parentBindingAstPair.getValue();
		for (TreeNode<Pair<IBinding, INode>> childTreeNode : parentTreeNode.getChildren()) {
			final Pair<IBinding, INode> childBindingAstPair = childTreeNode.getValue();
			assert childBindingAstPair != null;
			final IBinding binding = childBindingAstPair.getKey();
			if (binding instanceof ICPPSpecialization) continue;

			INode childAstNode = null;

			if (binding instanceof ICPPMember) {
				if (parentAstNode instanceof IClass) {
//					final IMember.Visibility visibility = createVisibilityFromMember((ICPPMember) binding);
					if (binding instanceof ICPPMethod) {
						final String uniqueName =
								createQualifiedNameFromCPPBinding((ICPPBinding) binding) + ";"
										+ ((ICPPFunction) binding).getDeclaredType();
						childAstNode = FunctionNode.builder()
								.setName(binding.toString())
								.setUniqueName(binding.getName())
								.setContent(uniqueName)
//								.setVisibility(visibility)
								//((ICPPFunction) binding).getType().getReturnType()
								.build();
					} else if (binding instanceof ICPPField) {
						final String uniqueName =
								createQualifiedNameFromCPPBinding((ICPPBinding) binding) + ";"
										+ ((ICPPVariable) binding).getType();
						childAstNode = VariableNode.builder()
								.setName(binding.toString())
								.setUniqueName(binding.getName())
								.setContent(uniqueName)
//								.setVisibility(visibility)
								.build();
					}
				}
			} else if (binding instanceof ICPPFunction) {
				final String uniqueName =
						createQualifiedNameFromCPPBinding((ICPPBinding) binding) + ";"
								+ ((ICPPFunction) binding).getDeclaredType();
				childAstNode = FunctionNode.builder()
						.setName(binding.toString())
						.setUniqueName(binding.getName())
						.setContent(uniqueName)
//						.setVisibility((parentAstNode instanceof IClass) ? ((IClass) parentAstNode).getChildDefaultVisibility() : IMember.Visibility.PUBLIC)
						.build();
			} else if (binding instanceof ICPPVariable) {
				final String uniqueName =
						createQualifiedNameFromCPPBinding((ICPPBinding) binding) + ";"
								+ ((ICPPVariable) binding).getType();
				childAstNode = VariableNode.builder()
						.setName(binding.toString())
						.setUniqueName(binding.getName())
						.setContent(uniqueName)
//						.setVisibility((parentAstNode instanceof IClass) ? ((IClass) parentAstNode).getChildDefaultVisibility() : IMember.Visibility.PUBLIC)
						.build();
			} else if (binding instanceof ICPPClassType) {
				childAstNode = ClassNode.builder()
						.setName(binding.toString())
						.setUniqueName(binding.getName())
						.setContent(createQualifiedNameFromCPPBinding((ICPPBinding) binding))
//						.setVisibility((parentAstNode instanceof IClass) ? ((IClass) parentAstNode).getChildDefaultVisibility() : IMember.Visibility.PUBLIC)
//						.setDefaultVisibility(createDefaultVisibilityFromClass((ICPPClassType) binding))
						.build();
			} else if (binding instanceof ICPPNamespace) {
				childAstNode = NamespaceNode.builder()
						.setName(binding.toString())
						.setUniqueName(binding.getName())
						.setContent(createQualifiedNameFromCPPBinding((ICPPBinding) binding))
						.build();
			} else if (binding instanceof ICPPEnumeration) {
				childAstNode = EnumNode.builder()
						.setName(binding.toString())
						.setUniqueName(binding.getName())
						.setContent(createQualifiedNameFromCPPBinding((ICPPBinding) binding))
//						.setVisibility((parentAstNode instanceof IClass) ? ((IClass) parentAstNode).getChildDefaultVisibility() : IMember.Visibility.PUBLIC)
						.build();
			}
			if (childAstNode != null) {
				parentAstNode.addChild(childAstNode);
				childBindingAstPair.setValue(childAstNode);
				createAstTree(childTreeNode);
			}
		}
	}

	private void build() throws IOException, DOMException {
		final Set<File> includePaths = TranslationUnitBuilder.createInternalIncludePaths(projectFiles);
//		final Map<File, Set<File>> includeMap = TranslationUnitBuilder.createIncludeMap(projectFiles, includePaths);
//		System.out.println(includeMap);

		final IASTTranslationUnit translationUnit = TranslationUnitBuilder.build(projectFiles, includePaths);

		assert translationUnit != null;
		IASTDeclaration[] declarations = translationUnit.getDeclarations();
		try (PrintStream stream = new PrintStream("R:\\declaration.log")) {
			for (IASTDeclaration declaration : declarations) {
				stream.printf("%-50s%s\n",
						String.format("%s@%08x", declaration.getClass().getSimpleName(), System.identityHashCode(declaration)),
						declaration.getRawSignature()
				);
			}
		}

		{
			final File logFile = new File("R:\\preprocessed.log");
			try (final FileOutputStream fileOutputStream = new FileOutputStream(logFile)) {
				try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
					try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
						printer(printStream, 0, translationUnit, translationUnit);
					}
				}
			}
		}

		final Map<IBinding, TreeNode<Pair<IBinding, INode>>> bindingMap = new HashMap<>(4096);
		createBindingMap(bindingMap, translationUnit);

		final RootNode rootNode = new RootNode();
		final TreeNode<Pair<IBinding, INode>> treeRoot = new TreeNode<>(new Pair<>(null, rootNode));
		createBindingTree(treeRoot, bindingMap);
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

		createAstTree(treeRoot);
		{
			final File logFile = new File("R:\\tree.log");
			try (final FileOutputStream fileOutputStream = new FileOutputStream(logFile)) {
				try (final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream, 65536)) {
					try (final PrintStream printStream = new PrintStream(bufferedOutputStream, false)) {
						printStream.println(rootNode.toTreeString());
					}
				}
			}
		}
	}

	private static final class AstTreeBuilder {
		private final Map<IBinding, INode> bindingNodeMap = new HashMap<>();
		private final Map<INode, IBinding> nodeTypeMap = new HashMap<>();
		private final Map<INode, List<IBinding>> classBasesMap = new HashMap<>();

		private <E extends INode, B extends INode.INodeBuilder<E, B>> INode createNode(IBinding binding, String content, B builder) {
			// todo: support internal node
			final INode existChildNode = bindingNodeMap.get(binding);
			if (existChildNode != null) return existChildNode;

			final E childNode = builder
					.setName(binding.getName())
					.setUniqueName(binding instanceof ICPPBinding
							? ASTTypeUtil.getQualifiedName((ICPPBinding) binding)
							: binding.getName())
					.setContent(content)
					.build();

			bindingNodeMap.put(binding, childNode);
			return childNode;
		}

		private INode createFromDeclarator(INode typeNode, IASTDeclarator declarator) {
			final IASTName declaratorName = declarator.getName();
			final IBinding declaratorBinding = declaratorName.resolveBinding();

			if (declarator instanceof ICPPASTFunctionDeclarator) {
				// region
				final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;

				final List<INode> parameterList = new ArrayList<>();

				final ICPPASTParameterDeclaration[] functionParameters = functionDeclarator.getParameters();
				for (final ICPPASTParameterDeclaration functionParameter : functionParameters) {
					final IASTDeclSpecifier parameterSpecifier = functionParameter.getDeclSpecifier();
					final INode parameterType = createFromDeclSpecifier(parameterSpecifier);

					final ICPPASTDeclarator parameterDeclarator = functionParameter.getDeclarator();
					final INode parameterNode = createFromDeclarator(parameterType, parameterDeclarator);
					parameterList.add(parameterNode);
				}

				final INode functionNode = createNode(declaratorBinding, functionDeclarator.getRawSignature(),
						FunctionNode.builder().setType(typeNode).setParameters(parameterList));

				for (final INode parameterNode : parameterList) {
					functionNode.addChild(parameterNode);
				}
				// endregion
				return functionNode;
			} else {
				// todo: variable, etc...
				return createNode(declaratorBinding, declarator.getRawSignature(), VariableNode.builder());
			}
		}

		private INode createFromDeclSpecifier(IASTDeclSpecifier declSpecifier) {
			if (declSpecifier instanceof ICPPASTEnumerationSpecifier) {
				// region
				final ICPPASTEnumerationSpecifier enumerationSpecifier = (ICPPASTEnumerationSpecifier) declSpecifier;
				final IASTName enumerationName = enumerationSpecifier.getName();
				final IBinding enumerationBinding = enumerationName.resolveBinding();

				final INode enumNode = createNode(enumerationBinding, enumerationSpecifier.getRawSignature(), EnumNode.builder());

				final INode nodeType = enumerationSpecifier.isScoped() ? enumNode : null;
				final IASTEnumerationSpecifier.IASTEnumerator[] enumerators = enumerationSpecifier.getEnumerators();
				for (final IASTEnumerationSpecifier.IASTEnumerator enumerator : enumerators) {
					final IASTName enumeratorName = enumerator.getName();
					final IBinding enumeratorBinding = enumeratorName.resolveBinding();

					final INode enumeratorNode = createNode(enumeratorBinding, enumerator.getRawSignature(),
							VariableNode.builder().setType(nodeType));

					enumNode.addChild(enumeratorNode);
				}
				// endregion
				return enumNode;
			} else if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
				// region
				final ICPPASTCompositeTypeSpecifier classSpecifier = (ICPPASTCompositeTypeSpecifier) declSpecifier;
				final IASTName className = classSpecifier.getName();
				final IBinding classBinding = className.resolveBinding();

				final INode classNode = createNode(classBinding, classSpecifier.getRawSignature(), ClassNode.builder());

				final List<IBinding> classBases = new ArrayList<>();

				final ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier[] classBaseSpecifiers = classSpecifier.getBaseSpecifiers();
				for (final ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier classBaseSpecifier : classBaseSpecifiers) {
					final ICPPASTNameSpecifier classBaseNameSpecifier = classBaseSpecifier.getNameSpecifier();
					final IBinding classBaseNameBinding = classBaseNameSpecifier.resolveBinding();

					classBases.add(classBaseNameBinding);
				}

				classBasesMap.put(classNode, classBases);

				final IASTDeclaration[] classChildDeclarations = classSpecifier.getDeclarations(false);
				for (final IASTDeclaration classChildDeclaration : classChildDeclarations) {
					createChildrenFromDeclaration(classNode, classChildDeclaration);
				}
				// endregion
				return classNode;
			} else if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
				final IASTNamedTypeSpecifier typeSpecifier = (IASTNamedTypeSpecifier) declSpecifier;
				final IASTName typeName = typeSpecifier.getName();
				final IBinding typeBinding = typeName.resolveBinding();

				// todo: <<<<<<<<<<<<<<<<<<<<<<

			}
		}

		private void createChildrenFromDeclaration(INode parentNode, IASTDeclaration declaration) {
			if (declaration instanceof ICPPASTNamespaceDefinition) {
				// region
				final ICPPASTNamespaceDefinition namespaceDefinition = (ICPPASTNamespaceDefinition) declaration;
				final IASTName namespaceName = namespaceDefinition.getName();
				final IBinding namespaceBinding = namespaceName.resolveBinding();

				final INode namespaceNode = createNode(namespaceBinding, namespaceDefinition.getRawSignature(), NamespaceNode.builder());
				final IASTDeclaration[] namespaceChildDeclarations = namespaceDefinition.getDeclarations(false);
				for (final IASTDeclaration namespaceChildDeclaration : namespaceChildDeclarations) {
					createChildrenFromDeclaration(namespaceNode, namespaceChildDeclaration);
				}
				// endregion
				parentNode.addChild(namespaceNode);
			} else if (declaration instanceof IASTSimpleDeclaration) {
				// region
				final IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;

				final IASTDeclSpecifier simpleSpecifier = simpleDeclaration.getDeclSpecifier();
				final INode simpleNodeType = createFromDeclSpecifier(simpleSpecifier);

				final IASTDeclarator[] declarators = simpleDeclaration.getDeclarators();
				for (final IASTDeclarator declarator : declarators) {
					final INode simpleNode = createFromDeclarator(simpleNodeType, declarator);
					parentNode.addChild(simpleNode);
				}
				// endregion
			} else if (declaration instanceof ICPPASTFunctionDefinition) {
				// region
				final ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;

				final IASTDeclSpecifier functionSpecifier = functionDefinition.getDeclSpecifier();
				final INode functionReturnType = createFromDeclSpecifier(functionSpecifier);

				final IASTFunctionDeclarator functionDeclarator = functionDefinition.getDeclarator();
				final INode functionNode = createFromDeclarator(functionReturnType, functionDeclarator);

				functionDefinition.getMemberInitializers();
				functionDefinition.getBody();
				// endregion
				parentNode.addChild(functionNode);

			} else {
				// todo: debug?
			}
		}


		private static final class InternalNode extends Node {
			private InternalNode(@Nonnull String name, @Nonnull String uniqueName, @Nonnull String content) {
				super(name, uniqueName, content);
			}
		}
	}

	private static final class TranslationUnitBuilder {
		private static final IncludeFileContentProvider EMPTY_PROVIDER = /*new MyIncludeFileContentProvider();*/IncludeFileContentProvider.getEmptyFilesProvider();
		private static final IParserLogService LOG_SERVICE = new DefaultLogService();
		private static final GPPLanguage GPP_LANGUAGE = GPPLanguage.getDefault();
		private static final IScannerInfo SCANNER_INFO = new ScannerInfo();

		private TranslationUnitBuilder() {
		}

		private static Set<File> createInternalIncludePaths(Set<File> projectFiles) {
			final Set<File> includePaths = new HashSet<>();
			for (final File projectFile : projectFiles) {
				includePaths.add(getCanonicalAbsoluteFile(projectFile.getParentFile()));
			}
			return includePaths;
		}

		private static Set<File> createFileIncludes(IASTTranslationUnit translationUnit, Set<File> projectFiles, Set<File> internalIncludePaths, File currentFolder) {
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
					for (final File includePath : internalIncludePaths) {
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

		private static Map<File, Set<File>> createIncludeMap(Set<File> projectFiles, Set<File> internalIncludePaths) {
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

					includeMap.put(currentFile, createFileIncludes(translationUnit, projectFiles, internalIncludePaths, currentFile.getParentFile()));
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
			return includeMap;
		}

		private static IASTTranslationUnit build(Set<File> projectFiles, Set<File> internalIncludePaths) {
			final char[] fileContentChars = PreprocessorBuilder.build(projectFiles, internalIncludePaths);
			final FileContent fileContent = FileContent.create("preprocessed.cpp", fileContentChars);
			try {
				return GPP_LANGUAGE.getASTTranslationUnit(
						fileContent, SCANNER_INFO, EMPTY_PROVIDER, null,
						ILanguage.OPTION_NO_IMAGE_LOCATIONS
								| ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS, LOG_SERVICE);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			return null;
		}
//
//		private static IASTTranslationUnit build2(Set<File> projectFiles, Set<File> internalIncludePaths) {
////			final char[] fileContentChars = PreprocessorBuilder.build(projectFiles, internalIncludePaths);
//			final FileContent fileContent = FileContent.create("preprocessed.cpp", fileContentChars);
//			try {
//				return GPP_LANGUAGE.getASTTranslationUnit(
//						fileContent, SCANNER_INFO, EMPTY_PROVIDER, null,
//						ILanguage.OPTION_SKIP_TRIVIAL_EXPRESSIONS_IN_AGGREGATE_INITIALIZERS
//								| ILanguage.OPTION_NO_IMAGE_LOCATIONS,
//						LOG_SERVICE);
//			} catch (CoreException e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
//		private static Map<File, IASTTranslationUnit> buildMultiple() {
//			final Map<File, IASTTranslationUnit> map = new HashMap<>();
//		}

		private static final class MyIncludeFileContentProvider extends SavedFilesProvider {
			//			private final Set<File> projectFiles;
			@Override
			public InternalFileContent getContentForInclusion(String path, IMacroDictionary macroDictionary) {
//				final File file = getCanonicalAbsoluteFile(path);
				System.out.println(path);
				return ((InternalFileContentProvider) IncludeFileContentProvider.getEmptyFilesProvider()).getContentForInclusion(path, macroDictionary);
			}

			@Override
			public InternalFileContent getContentForInclusion(IIndexFileLocation ifl, String astPath) {
				System.out.println(astPath);
				return ((InternalFileContentProvider) IncludeFileContentProvider.getEmptyFilesProvider()).getContentForInclusion(ifl, astPath);
			}
		}
	}

	private static final class PreprocessorBuilder implements PreprocessorListener {
		private static final PreprocessorBuilder EMPTY_PREPROCESSOR_LISTENER = new PreprocessorBuilder();
		private static final Pattern ALPHANUMERIC = Pattern.compile("[0-9A-Z_a-z]*");

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

			final List<String> includePathStrings = new ArrayList<>();
			for (final File file : includePaths) includePathStrings.add(file.getPath());
			preprocessor.setQuoteIncludePath(includePathStrings);
			preprocessor.setSystemIncludePath(includePathStrings);

			final List<File> projectFileList = new ArrayList<>(projectFiles);
			projectFileList.sort(PreprocessorBuilder::fileCompare);
			for (final File sourceFile : projectFiles) preprocessor.addInput(sourceFile);

			return preprocessor;
		}

		private static boolean isValidChar(char c) {
			return c == '"' || c == '\'' || c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c == '_' || c >= 'a' && c <= 'z';
		}

		private static StringBuilder runPreprocessor(Set<File> projectFiles, Set<File> includePaths) throws IOException, LexerException {
			final Preprocessor preprocessor = createPreprocessor(projectFiles, includePaths);
			final StringBuilder fileContent = new StringBuilder();

			/*
			int emptyLine = 1;
			final StringBuilder emptyLineBuilder = new StringBuilder();
			while (true) {
				final Token tok = preprocessor.token();
				if (tok.getType() == Token.EOF) break;

//				DON'T PANIC. THERE IS NOTHING HERE USEFUL. SKIP IT.
//				BASICALLY IT TRIM THE EMPTY LINES, ONLY KEEP AT MOST 2 SEQUENCE EMPTY LINES
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
			/*/
			boolean haveEndSpace = true;
//			boolean needSpace = false;
//			boolean specialNoNamePointer = false;
			while (true) {
				final Token token = preprocessor.token();
				if (token.getType() == Token.EOF) break;

				if (token.getType() == Token.CCOMMENT
						|| token.getType() == Token.CPPCOMMENT
						|| token.getType() == Token.P_LINE) {
					continue;
				}

				if (token.getType() == Token.NL
						|| token.getType() == Token.WHITESPACE) {
					haveEndSpace = true;
					continue;
				}

//				final String tokenText = token.getText();
//				if (tokenText.isBlank()) continue;
//				final String tokenTextTrim = tokenText.trim();
//				if (specialNoNamePointer && token.getType() == '=' || needSpace && haveEndSpace && isValidChar(tokenTextTrim.charAt(0))) {
//					fileContent.append(' ');
//				}
//				fileContent.append(tokenTextTrim);
//				needSpace = isValidChar(tokenTextTrim.charAt(tokenTextTrim.length() - 1));
//				haveEndSpace = false;
//				specialNoNamePointer = token.getType() == '*';

				final String tokenText = token.getText().trim();
				if (tokenText.isBlank()) {
					haveEndSpace = true;
					continue;
				}
				if (haveEndSpace) fileContent.append(' ');
				fileContent.append(tokenText);
				haveEndSpace = false;
			}
			//*/

			//todo: dbg
			try (FileWriter writer = new FileWriter("R:\\output.cpp")) {
				writer.write(fileContent.toString());
			} catch (IOException ignored) {
			}
			return fileContent.append('\n');
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
