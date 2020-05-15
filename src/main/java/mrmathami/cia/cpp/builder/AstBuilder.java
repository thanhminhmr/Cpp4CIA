package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.ClassNode;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.EnumNode;
import mrmathami.cia.cpp.ast.FunctionNode;
import mrmathami.cia.cpp.ast.IntegralNode;
import mrmathami.cia.cpp.ast.NamespaceNode;
import mrmathami.cia.cpp.ast.CppNode;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.cia.cpp.ast.TypedefNode;
import mrmathami.cia.cpp.ast.UnknownNode;
import mrmathami.cia.cpp.ast.VariableNode;
import mrmathami.util.Pair;
import mrmathami.util.Utilities;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTAliasDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclarator;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

final class AstBuilder {
	@Nonnull private static final Pattern PATTERN = Pattern.compile("\\{\\Q" + TranslationUnitBuilder.VIRTUAL_FILENAME + "\\E:\\d+}");

	@Nonnull private final Map<String, CppNode> integralNodeMap = new HashMap<>();
	@Nonnull private final Map<IBinding, CppNode> bindingNodeMap = new HashMap<>();
	@Nonnull private final List<UnknownNode> unknownNodeList = new LinkedList<>();
	@Nonnull private final Queue<Pair<CppNode, IASTNode>> childrenCreationQueue = new LinkedList<>();
	@Nonnull private final RootNode rootNode = new RootNode();

	private AstBuilder() {
	}

	@Nonnull
	public static RootNode build(@Nonnull IASTTranslationUnit translationUnit) throws CppException {
		try {
			return new AstBuilder().internalBuild(translationUnit);
		} catch (IllegalArgumentException e) {
			throw new CppException("Cannot build component tree from TranslationUnit!", e);
		}
	}

	@Nonnull
	private static String firstNonBlank(@Nonnull String... strings) {
		for (final String string : strings) {
			if (string != null && !string.isBlank()) return string;
		}
		return "";
	}

	private void cleanUp() {
		// remove all children of variable and function node
		for (final CppNode node : List.copyOf(bindingNodeMap.values())) {
			if (node instanceof VariableNode) {
				final VariableNode variableNode = (VariableNode) node;
				for (final CppNode childNode : variableNode) {
					childNode.transferAllDependency(variableNode);
					if (childNode instanceof UnknownNode) unknownNodeList.remove(childNode);
				}
				node.removeChildren();

			} else if (node instanceof FunctionNode) {
				final FunctionNode functionNode = (FunctionNode) node;
				for (final CppNode childNode : List.copyOf(functionNode.getChildren())) {
					if (childNode instanceof VariableNode
							|| childNode instanceof TypedefNode
							|| childNode instanceof UnknownNode) {
						childNode.transferAllDependency(functionNode);
						childNode.remove();
						if (childNode instanceof UnknownNode) unknownNodeList.remove(childNode);
					}
				}
			}
		}

		// replace unknown node with integral node
		for (final UnknownNode unknownNode : List.copyOf(unknownNodeList)) {
			assert unknownNode.getParent() != null;
			unknownNode.transferAllDependency(unknownNode.getParent());
			final CppNode integralNode = createIntegralNode(firstNonBlank(unknownNode.getName(), unknownNode.getUniqueName(), unknownNode.getSignature()));
			if (!replaceNode(unknownNode, integralNode)) integralNode.remove();
		}

		// clean up nodes
		for (final CppNode node : integralNodeMap.values()) {
			node.removeChildren();
			node.removeAllDependency();
		}
	}

	private void createOverride() {
		final CppNode.Matcher matcher = new CppNode.Matcher();
		for (final CppNode node : rootNode) {
			if (!(node instanceof ClassNode)) continue;
			final ClassNode classNode = (ClassNode) node;
			final Set<CppNode> classBases = classNode.getBases();
			final List<FunctionNode> classFunctions = classNode.getFunctions();
			if (classBases.isEmpty() || classFunctions.isEmpty()) continue;

			final Map<CppNode.Wrapper, FunctionNode> classFunctionsMatched = new HashMap<>();
			for (final FunctionNode classFunction : classFunctions) {
				final CppNode.Wrapper wrapper = new CppNode.Wrapper(classFunction, CppNode.MatchLevel.PROTOTYPE_IDENTICAL, matcher);
				classFunctionsMatched.put(wrapper, classFunction);
			}
			for (final CppNode classBase : classBases) {
				if (!(classBase instanceof ClassNode)) continue;
				for (final FunctionNode baseFunction : ((ClassNode) classBase).getFunctions()) {
					final CppNode.Wrapper wrapper = new CppNode.Wrapper(baseFunction, CppNode.MatchLevel.PROTOTYPE_IDENTICAL, matcher);
					final FunctionNode functionNode = classFunctionsMatched.get(wrapper);
					if (functionNode != null) {
						functionNode.addDependencyTo(baseFunction, DependencyType.OVERRIDE);
					}
				}
			}
		}
	}

	@Nonnull
	private RootNode internalBuild(@Nonnull IASTTranslationUnit translationUnit) {
		//final RootNode rootNode = new RootNode().build();
		for (final IASTDeclaration declaration : translationUnit.getDeclarations()) {
			createChildrenFromDeclaration(rootNode, declaration);
		}

		while (!childrenCreationQueue.isEmpty()) {
			final Pair<CppNode, IASTNode> pair = childrenCreationQueue.poll();
			createChildrenFromAstNode(pair.getA(), pair.getB());
		}

		cleanUp();
		createOverride();
		rootNode.removeAllDependency();

		int nodeId = 0;
		for (final CppNode node : rootNode) node.setId(++nodeId);
		rootNode.setNodeCount(++nodeId);

		rootNode.lock();

		for (final CppNode node : rootNode) {
			for (final CppNode toNode : node.getAllDependencyTo()) {
				if (toNode.getRoot() != rootNode) {
					System.out.println("DIE DIE DIE");
				}
			}
			for (final CppNode fromNode : node.getAllDependencyFrom()) {
				if (fromNode.getRoot() != rootNode) {
					System.out.println("DIE DIE DIE");
				}
			}
		}

		return rootNode;
	}

	private boolean replaceNode(@Nonnull CppNode oldNode, @Nonnull CppNode newNode) {
		assert oldNode.getParent() != null && newNode.getParent() != null;
		if (oldNode instanceof UnknownNode) unknownNodeList.remove(oldNode);

		for (final Map.Entry<IBinding, CppNode> entry : bindingNodeMap.entrySet()) {
			if (entry.getValue() == oldNode) entry.setValue(newNode);
		}
		for (final Pair<CppNode, IASTNode> pair : childrenCreationQueue) {
			if (pair.getA() == oldNode) pair.setA(newNode);
		}

		final boolean isChanged = oldNode.transfer(newNode);
		oldNode.remove();
		return isChanged;
	}

	@Nonnull
	private CppNode createIntegralNode(@Nonnull String typeName) {
		final CppNode existNode = integralNodeMap.get(typeName);
		if (existNode != null) return existNode;

		final CppNode newNode = new IntegralNode(typeName, typeName, typeName);
		rootNode.addChild(newNode);
		integralNodeMap.put(typeName, newNode);
		return newNode;
	}

	@Nonnull
	private CppNode createUnknownNode(@Nonnull CppNode parentNode, @Nonnull IBinding binding, @Nonnull String name, boolean createUseDependency) {
		if (binding instanceof IProblemBinding) return createIntegralNode(name);

		final IBinding topBinding = binding instanceof ICPPSpecialization
				? Objects.requireNonNullElse(((ICPPSpecialization) binding).getSpecializedBinding(), binding)
				: binding;

		final CppNode existNode = bindingNodeMap.get(topBinding);
		if (existNode != null) {
			if (createUseDependency) parentNode.addDependencyTo(existNode, DependencyType.USE);
			return existNode;
		}

		final UnknownNode newNode = new UnknownNode(name, name, name);
		parentNode.addChild(newNode);
		if (createUseDependency) parentNode.addDependencyTo(newNode, DependencyType.USE);
		bindingNodeMap.put(topBinding, newNode);
		unknownNodeList.add(newNode);
		return newNode;
	}

	@Nonnull
	private CppNode createNode(@Nullable IBinding binding, @Nullable IASTName astName, @Nullable String signature,
			@Nonnull CppNode buildingNode, @Nonnull CppNode parentNode) {
		assert !(buildingNode instanceof UnknownNode) && !(buildingNode instanceof IntegralNode);
		if (binding == null)
			return createIntegralNode(astName != null ? astName.toString() : signature != null ? signature : "");

		final IBinding topBinding = binding instanceof ICPPSpecialization
				? Objects.requireNonNullElse(((ICPPSpecialization) binding).getSpecializedBinding(), binding)
				: binding;

		final CppNode existNode = bindingNodeMap.get(topBinding);
		if (existNode != null && !(existNode instanceof UnknownNode)) return existNode;

		final String name = firstNonBlank(astName != null ? astName.toString() : null, topBinding.getName());
		final String uniqueName = firstNonBlank(topBinding instanceof ICPPBinding
				? PATTERN.matcher(ASTTypeUtil.getQualifiedName((ICPPBinding) binding)).replaceAll("{ROOT}")
				: astName != null ? ASTStringUtil.getQualifiedName(astName) : null, name);

		final CppNode newNode = buildingNode.setName(name).setUniqueName(uniqueName).setSignature(signature != null ? signature : uniqueName);
		parentNode.addChild(newNode);
		parentNode.addDependencyTo(newNode, DependencyType.MEMBER);

		bindingNodeMap.put(topBinding, newNode);
		if (existNode != null) replaceNode(existNode, newNode);
		return newNode;
	}

	@Nonnull
	private CppNode createFromDeclarator(@Nonnull CppNode parentNode, @Nonnull CppNode typeNode, @Nonnull IASTDeclarator declarator, boolean isTypedef) {
		final IASTName declaratorName = declarator.getName();
		final IBinding declaratorBinding = declaratorName.resolveBinding();
		final String signature = ASTStringUtil.getSignatureString(declarator);

		if (declarator instanceof IASTAmbiguousDeclarator) {
			return createUnknownNode(parentNode, declaratorBinding, declaratorName.toString(), isTypedef);

		} else if (declarator instanceof ICPPASTFunctionDeclarator) {
			// region Function
			final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;
			final CppNode functionNode = createNode(declaratorBinding, declaratorName, signature,
					new FunctionNode(), parentNode);

			if (functionNode instanceof FunctionNode) {
				((FunctionNode) functionNode).setType(typeNode);
				functionNode.addDependencyTo(typeNode, DependencyType.USE);

				for (final ICPPASTParameterDeclaration functionParameter : functionDeclarator.getParameters()) {
					final CppNode parameterType = createFromDeclSpecifier(functionNode, functionParameter.getDeclSpecifier());
					createFromDeclarator(functionNode, parameterType, functionParameter.getDeclarator(), true);
					((FunctionNode) functionNode).addParameter(parameterType);
				}

				final IASTInitializer initializer = declarator.getInitializer();
				if (initializer != null) {
					((FunctionNode) functionNode).setBody(initializer.getRawSignature());
					childrenCreationQueue.add(Pair.mutableOf(functionNode, initializer));
				}
			}
			// endregion
			return functionNode;
		} else if (declarator instanceof ICPPASTDeclarator) {
			if (isTypedef) {
				// region Typedef
				final CppNode typedefNode = createNode(declaratorBinding, declaratorName, signature,
						new TypedefNode(), parentNode);
				if (typedefNode instanceof TypedefNode) {
					((TypedefNode) typedefNode).setType(typeNode);
					typedefNode.addDependencyTo(typeNode, DependencyType.USE);
				}
				// endregion
				return typedefNode;
			} else {
				// region Variable
				final CppNode variableNode = createNode(declaratorBinding, declaratorName, signature,
						new VariableNode(), parentNode);
				if (variableNode instanceof VariableNode) {
					((VariableNode) variableNode).setType(typeNode);
					variableNode.addDependencyTo(typeNode, DependencyType.USE);

					final IASTInitializer initializer = declarator.getInitializer();
					if (initializer != null) {
						((VariableNode) variableNode).setBody(initializer.getRawSignature());
						childrenCreationQueue.add(Pair.mutableOf(variableNode, initializer));
					}
				}
				// endregion
				return variableNode;
			}
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromDeclarator(declarator = (" + Utilities.objectIdentifyString(declarator) + "))");
		}
	}

	@Nonnull
	private CppNode createFromDeclSpecifier(@Nonnull CppNode parentNode, @Nonnull IASTDeclSpecifier declSpecifier) {
		final String signature = ASTStringUtil.getSignatureString(declSpecifier, null);

		if (declSpecifier instanceof ICPPASTEnumerationSpecifier) {
			// region Enumeration
			final ICPPASTEnumerationSpecifier enumerationSpecifier = (ICPPASTEnumerationSpecifier) declSpecifier;
			final IASTName enumerationName = enumerationSpecifier.getName();
			final IBinding enumerationBinding = enumerationName.resolveBinding();

			final CppNode enumNode = createNode(enumerationBinding, enumerationName, signature,
					new EnumNode(), parentNode);
			if (enumNode instanceof EnumNode) {
				final ICPPASTDeclSpecifier enumBaseType = enumerationSpecifier.getBaseType();
				final CppNode baseType = enumBaseType != null ? createFromDeclSpecifier(parentNode, enumBaseType) : null;
				final CppNode nodeType = enumerationSpecifier.isScoped() ? enumNode : baseType;

				if (baseType != null) {
					((EnumNode) enumNode).setType(baseType);
					enumNode.addDependencyTo(baseType, DependencyType.USE);
				}

				final StringBuilder bodyBuilder = enumNode.getName().isBlank() ? new StringBuilder() : null;
				for (final IASTEnumerationSpecifier.IASTEnumerator enumerator : enumerationSpecifier.getEnumerators()) {
					final IASTName enumeratorName = enumerator.getName();
					final IBinding enumeratorBinding = enumeratorName.resolveBinding();
					final CppNode enumeratorNode = createNode(enumeratorBinding, enumeratorName, null,
							new VariableNode(), enumNode);
					if (enumeratorNode.getParent() == null) {
						enumNode.addChild(enumeratorNode);
						enumNode.addDependencyTo(enumeratorNode, DependencyType.MEMBER);
					} else {
						parentNode.addDependencyTo(enumNode, DependencyType.USE);
					}
					if (enumeratorNode instanceof VariableNode) {
						if (nodeType != null) {
							((VariableNode) enumeratorNode).setType(nodeType);
							enumeratorNode.addDependencyTo(nodeType, DependencyType.USE);
						}

						final IASTExpression expression = enumerator.getValue();
						if (expression != null) {
							((VariableNode) enumeratorNode).setBody(expression.getRawSignature());
							childrenCreationQueue.add(Pair.mutableOf(enumeratorNode, expression));
						}
					}
					if (bodyBuilder != null) {
						bodyBuilder.append(bodyBuilder.length() > 0 ? ',' : "enum{").append(enumeratorNode.getName());
					}
				}
				if (bodyBuilder != null) {
					enumNode.setName(bodyBuilder.append('}').toString());
				}
			}
			// endregion
			return enumNode;

		} else if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
			// region Class, Struct, Union
			final ICPPASTCompositeTypeSpecifier classSpecifier = (ICPPASTCompositeTypeSpecifier) declSpecifier;
			final IASTName className = classSpecifier.getName();

			final CppNode classNode = createNode(className.resolveBinding(), className, signature,
					new ClassNode(), parentNode);
			if (classNode instanceof ClassNode) {
				for (final ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier classBaseSpecifier : classSpecifier.getBaseSpecifiers()) {
					final ICPPASTNameSpecifier classBaseNameSpecifier = classBaseSpecifier.getNameSpecifier();
					final IBinding classBaseNameBinding = classBaseNameSpecifier.resolveBinding();

					final CppNode classBaseNode = createUnknownNode(parentNode, classBaseNameBinding, classBaseNameBinding.getName(), true);
					((ClassNode) classNode).addBase(classBaseNode);
					classNode.addDependencyTo(classBaseNode, DependencyType.INHERITANCE);
				}

				final StringBuilder bodyBuilder = classNode.getName().isBlank()
						? new StringBuilder().append(classNode.getSignature()).append('{')
						: null;
				for (final IASTDeclaration classChildDeclaration : classSpecifier.getDeclarations(false)) {
					final List<CppNode> nodeList = createChildrenFromDeclaration(classNode, classChildDeclaration);
					if (bodyBuilder != null) {
						for (final CppNode node : nodeList) bodyBuilder.append(node.getName()).append(';');
					}
				}
				if (bodyBuilder != null) classNode.setName(bodyBuilder.append('}').toString());
			}
			// endregion
			return classNode;

		} else if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
			// region Typename Type
			final IASTName namedName = ((IASTNamedTypeSpecifier) declSpecifier).getName();
			return createUnknownNode(parentNode, namedName.resolveBinding(), namedName.toString(), true);
			// endregion

		} else if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier) {
			// region Forward Declaration
			final IASTName elaboratedName = ((ICPPASTElaboratedTypeSpecifier) declSpecifier).getName();
			return createUnknownNode(parentNode, elaboratedName.resolveBinding(), elaboratedName.toString(), true);
			// endregion

		} else if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier) {
			// region Integral Type
			return createIntegralNode(signature);
			// endregion

		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromDeclSpecifier(declSpecifier = (" + Utilities.objectIdentifyString(declSpecifier) + "))");
		}
	}

	@Nonnull
	private CppNode createFromTemplateParameter(@Nonnull CppNode parentNode, @Nonnull ICPPASTTemplateParameter templateParameter) {
		if (templateParameter instanceof ICPPASTParameterDeclaration) {
			// region Template Variable
			final ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration) templateParameter;
			final CppNode parameterType = createFromDeclSpecifier(parentNode, parameterDeclaration.getDeclSpecifier());
			return createFromDeclarator(parentNode, parameterType, parameterDeclaration.getDeclarator(), false);
			// endregion

		} else if (templateParameter instanceof ICPPASTSimpleTypeTemplateParameter) {
			// region Template Typename
			final ICPPASTSimpleTypeTemplateParameter simpleParameter = (ICPPASTSimpleTypeTemplateParameter) templateParameter;
			final IASTName simpleName = simpleParameter.getName();
			final IBinding simpleBinding = simpleName.resolveBinding();

			final CppNode typedefNode = createNode(simpleBinding, simpleName, null,
					new TypedefNode(), parentNode);

			if (typedefNode instanceof TypedefNode) {
				final IASTTypeId defaultType = simpleParameter.getDefaultType();
				if (defaultType != null) {
					final IASTDeclSpecifier elementSpecifier = defaultType.getDeclSpecifier();
					final IASTDeclarator elementDeclarator = defaultType.getAbstractDeclarator();
					final CppNode elementType = elementSpecifier != null
							? createFromDeclSpecifier(typedefNode, elementSpecifier) : null;
					final CppNode element = elementDeclarator != null && elementType != null
							? createFromDeclarator(typedefNode, elementType, elementDeclarator, false) : null;
					if (element != null || elementType != null) {
						((TypedefNode) typedefNode).setType(element != null ? element : elementType);
					}
				}
			}
			// endregion
			return typedefNode;

		} else if (templateParameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
			// region Nested Template
			final ICPPASTTemplatedTypeTemplateParameter nestedParameter = (ICPPASTTemplatedTypeTemplateParameter) templateParameter;
			final IASTName nestedName = nestedParameter.getName();
			final CppNode nestedNode = createNode(nestedName.resolveBinding(), nestedName, null,
					new TypedefNode(), parentNode);
			for (final ICPPASTTemplateParameter innerParameter : nestedParameter.getTemplateParameters()) {
				createFromTemplateParameter(nestedNode, innerParameter);
			}
			// endregion
			return nestedNode;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromTemplateParameter(parentNode = (" + Utilities.objectIdentifyString(parentNode)
					+ "), templateParameter = (" + Utilities.objectIdentifyString(templateParameter) + "))");
		}
	}

	@Nonnull
	private List<CppNode> createChildrenFromDeclaration(@Nonnull CppNode parentNode, @Nonnull IASTDeclaration declaration) {
		if (declaration instanceof ICPPASTVisibilityLabel
				|| declaration instanceof IASTASMDeclaration
				|| declaration instanceof IASTProblemDeclaration
				|| declaration instanceof ICPPASTStaticAssertDeclaration
				|| declaration instanceof ICPPASTExplicitTemplateInstantiation) {
			// skipped
			return List.of();

		} else if (declaration instanceof ICPPASTUsingDeclaration) {
			// region Using Declaration / Directive
			final IASTName declarationName = ((ICPPASTUsingDeclaration) declaration).getName();
			final IBinding declarationBinding = declarationName.resolveBinding();
			final CppNode declarationNode = createNode(declarationBinding, declarationName, null, new TypedefNode(), parentNode);
			if (declarationNode instanceof TypedefNode && declarationBinding instanceof ICPPUsingDeclaration) {
				for (final IBinding delegateBinding : ((ICPPUsingDeclaration) declarationBinding).getDelegates()) {
					createUnknownNode(declarationNode, delegateBinding, declarationName.toString(), true);
				}
			}
			// endregion
			return List.of(declarationNode);

		} else if (declaration instanceof ICPPASTUsingDirective) {
			// region Using Declaration / Directive
			final IASTName usingName = ((ICPPASTUsingDirective) declaration).getQualifiedName();
			final CppNode usingNode = createUnknownNode(parentNode, usingName.resolveBinding(), usingName.toString(), true);
			// endregion
			return List.of(usingNode);

		} else {
			if (declaration instanceof ICPPASTLinkageSpecification) {
				final ICPPASTLinkageSpecification linkageSpecification = (ICPPASTLinkageSpecification) declaration;
				// region extern "C"
				final List<CppNode> childrenNode = new ArrayList<>();
				for (final IASTDeclaration linkageDeclaration : linkageSpecification.getDeclarations(false)) {
					childrenNode.addAll(createChildrenFromDeclaration(parentNode, linkageDeclaration));
				}
				// endregion
				return childrenNode;

			} else if (declaration instanceof ICPPASTNamespaceDefinition) {
				final ICPPASTNamespaceDefinition namespaceDefinition = (ICPPASTNamespaceDefinition) declaration;
				// region Namespace
				final IASTName namespaceName = namespaceDefinition.getName();
				final CppNode namespaceNode = createNode(namespaceName.resolveBinding(), namespaceName, null,
						new NamespaceNode(), parentNode);
				for (final IASTDeclaration namespaceDeclaration : namespaceDefinition.getDeclarations(false)) {
					createChildrenFromDeclaration(namespaceNode, namespaceDeclaration);
				}
				// endregion
				return List.of(namespaceNode);

			} else if (declaration instanceof IASTSimpleDeclaration) {
				final IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;
				// region Simple Declaration
				final IASTDeclSpecifier simpleSpecifier = simpleDeclaration.getDeclSpecifier();
				final CppNode simpleNodeType = createFromDeclSpecifier(parentNode, simpleSpecifier);

				final List<CppNode> simpleNodeList = new ArrayList<>();
				final boolean isTypedef = simpleSpecifier.getStorageClass() == IASTDeclSpecifier.sc_typedef;
				for (final IASTDeclarator simpleDeclarator : simpleDeclaration.getDeclarators()) {
					simpleNodeList.add(createFromDeclarator(parentNode, simpleNodeType, simpleDeclarator, isTypedef));
				}
				// endregion
				return simpleNodeList.size() > 0 ? simpleNodeList : List.of(simpleNodeType);

			} else if (declaration instanceof ICPPASTFunctionDefinition) {
				final ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;
				// region Function
				final CppNode functionReturnType = createFromDeclSpecifier(parentNode, functionDefinition.getDeclSpecifier());
				final CppNode functionNode = createFromDeclarator(parentNode, functionReturnType, functionDefinition.getDeclarator(), false);
				final StringBuilder functionBodyBuilder = new StringBuilder();
				// function with constructor
				for (final ICPPASTConstructorChainInitializer memberChainInitializer : functionDefinition.getMemberInitializers()) {
					final IASTName memberName = memberChainInitializer.getMemberInitializerId();
					createUnknownNode(functionNode, memberName.resolveBinding(), memberName.toString(), true);
					functionBodyBuilder.append(memberName.toString()).append('(');
					final IASTInitializer memberInitializer = memberChainInitializer.getInitializer();
					if (memberInitializer != null) {
						childrenCreationQueue.add(Pair.mutableOf(functionNode, memberInitializer));
						functionBodyBuilder.append(memberInitializer.getRawSignature());
					}
					functionBodyBuilder.append(");");
				}
				// function with body
				final IASTStatement functionBody = functionDefinition.getBody();
				if (functionBody != null) {
					childrenCreationQueue.add(Pair.mutableOf(functionNode, functionBody));
					if (functionNode instanceof FunctionNode) {
						((FunctionNode) functionNode).setBody(functionBodyBuilder.append(functionBody.getRawSignature()).toString());
					}
				}
				// endregion
				return List.of(functionNode);

			} else if (declaration instanceof ICPPASTTemplateDeclaration) {
				final ICPPASTTemplateDeclaration templateDeclaration = (ICPPASTTemplateDeclaration) declaration;
				// region Template
				final List<CppNode> innerNodeList = createChildrenFromDeclaration(parentNode, templateDeclaration.getDeclaration());
				if (!innerNodeList.isEmpty()) {
					final CppNode innerNode = innerNodeList.get(0);
					for (final ICPPASTTemplateParameter templateParameter : templateDeclaration.getTemplateParameters()) {
						createFromTemplateParameter(innerNode, templateParameter);
					}
				}
				// endregion
				return innerNodeList;

			} else if (declaration instanceof ICPPASTNamespaceAlias) {
				final ICPPASTNamespaceAlias namespaceAlias = (ICPPASTNamespaceAlias) declaration;
				// region Namespace Alias
				final IASTName aliasName = namespaceAlias.getAlias();
				final CppNode aliasNode = createNode(aliasName.resolveBinding(), aliasName, null,
						new TypedefNode(), parentNode);
				final IASTName mappingName = namespaceAlias.getMappingName();
				createUnknownNode(aliasNode, mappingName.resolveBinding(), mappingName.toString(), true);
				// endregion
				return List.of(aliasNode);

			} else if (declaration instanceof ICPPASTAliasDeclaration) {
				final ICPPASTAliasDeclaration aliasDefinition = (ICPPASTAliasDeclaration) declaration;
				// region Alias
				final IASTName aliasName = aliasDefinition.getAlias();
				final ICPPASTTypeId aliasTypeId = aliasDefinition.getMappingTypeId();
				final IASTDeclSpecifier aliasDeclSpecifier = aliasTypeId.getDeclSpecifier();
				final IASTDeclarator aliasDeclarator = aliasTypeId.getAbstractDeclarator();
				final CppNode aliasNode = createNode(aliasName.resolveBinding(), aliasName,
						ASTStringUtil.getSignatureString(aliasDeclSpecifier, aliasDeclarator),
						new TypedefNode(), parentNode);
				final CppNode aliasType = createFromDeclSpecifier(aliasNode, aliasDeclSpecifier);
				createFromDeclarator(aliasNode, aliasType, aliasDeclarator, false);
				// endregion
				return List.of(aliasNode);

			} else {
				// todo: debug?
				throw new IllegalArgumentException("createChildrenFromDeclaration(parentNode = (" + Utilities.objectIdentifyString(parentNode)
						+ "), declaration = (" + Utilities.objectIdentifyString(declaration) + "))");
			}
		}
	}

	private void createChildrenFromAstNode(@Nonnull CppNode parentNode, @Nonnull IASTNode astNode) {
		for (final IASTNode astChild : astNode.getChildren()) {
			if (astChild instanceof IASTDeclaration) {
				createChildrenFromDeclaration(parentNode, (IASTDeclaration) astChild);
			} else if (astChild instanceof IASTName) {
				final IASTName astName = (IASTName) astChild;
				final CppNode childNode = createUnknownNode(parentNode, astName.resolveBinding(), astName.toString(), false);
				parentNode.addDependencyTo(childNode,
						childNode instanceof FunctionNode ? DependencyType.INVOCATION : DependencyType.USE);
			} else {
				createChildrenFromAstNode(parentNode, astChild);
			}
		}
	}
}
