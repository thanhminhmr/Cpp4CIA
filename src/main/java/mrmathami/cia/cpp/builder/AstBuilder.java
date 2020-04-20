package mrmathami.cia.cpp.builder;

import mrmathami.cia.cpp.CppException;
import mrmathami.cia.cpp.ast.ClassNode;
import mrmathami.cia.cpp.ast.DependencyType;
import mrmathami.cia.cpp.ast.EnumNode;
import mrmathami.cia.cpp.ast.FunctionNode;
import mrmathami.cia.cpp.ast.IntegralNode;
import mrmathami.cia.cpp.ast.NamespaceNode;
import mrmathami.cia.cpp.ast.Node;
import mrmathami.cia.cpp.ast.RootNode;
import mrmathami.cia.cpp.ast.UnknownNode;
import mrmathami.cia.cpp.ast.VariableNode;
import mrmathami.util.Pair;
import mrmathami.util.Utilities;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
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
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

final class AstBuilder {
	@Nonnull private final Map<String, Node> integralNodeMap = new HashMap<>();
	@Nonnull private final Map<IBinding, Node> bindingNodeMap = new HashMap<>();
	@Nonnull private final List<UnknownNode> unknownNodeList = new LinkedList<>();
	@Nonnull private final Queue<Pair<Node, IASTNode>> childrenCreationQueue = new LinkedList<>();
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
		for (final Node node : List.copyOf(bindingNodeMap.values())) {
			if (node instanceof UnknownNode) {
				// replace unknown node with integral node
				node.removeChildren();
				node.removeAllDependency();
				final Node newNode = createIntegralNode(firstNonBlank(node.getName(), node.getUniqueName(), node.getSignature()));
				replaceNode((UnknownNode) node, newNode);
			} else if (node instanceof VariableNode) {
				// remove all children
				node.removeChildren();

			} else if (node instanceof FunctionNode) {
				final FunctionNode function = (FunctionNode) node;
				final List<Node> parameters = function.getParameters();
				final List<Node> variables = new LinkedList<>(function.getVariables());
				variables.removeAll(parameters);
				for (final Node variable : variables) {
					variable.transferAllDependencyTo(function);
					function.removeChild(variable);
				}
				for (final Node parameter : parameters) {
					parameter.transferAllDependencyTo(function);
					parameter.removeAllDependency();
				}
			}
		}
		for (final UnknownNode node : List.copyOf(unknownNodeList)) {
			// replace unknown node with integral node
			node.removeChildren();
			node.removeAllDependency();
			final Node newNode = createIntegralNode(firstNonBlank(node.getName(), node.getUniqueName(), node.getSignature()));
			replaceNode(node, newNode);
		}
		for (final Node integralNode : integralNodeMap.values()) {
			integralNode.removeChildren();
			integralNode.removeAllDependency();
			if (integralNode.getParent() == null) {
				rootNode.addChild(integralNode);
			} else if (integralNode.getParent() != rootNode) {
				final Node newIntegralNode = new IntegralNode()
						.setName(integralNode.getName())
						.setUniqueName(integralNode.getUniqueName())
						.setSignature(integralNode.getSignature());
				rootNode.addChild(newIntegralNode);
				integralNode.transfer(newIntegralNode);
				integralNode.remove();
			}
		}
	}

	private void createOverride() {
		final Node.Matcher matcher = new Node.Matcher();
		for (final Node node : rootNode) {
			if (!(node instanceof ClassNode)) continue;
			final ClassNode classNode = (ClassNode) node;
			final Set<Node> classBases = classNode.getBases();
			final List<FunctionNode> classFunctions = classNode.getFunctions();
			if (classBases.isEmpty() || classFunctions.isEmpty()) continue;

			final Map<Node.Wrapper, FunctionNode> classFunctionsMatched = new HashMap<>();
			for (final FunctionNode classFunction : classFunctions) {
				final Node.Wrapper wrapper = new Node.Wrapper(classFunction, Node.MatchLevel.PROTOTYPE_IDENTICAL, matcher);
				classFunctionsMatched.put(wrapper, classFunction);
			}
			for (final Node classBase : classBases) {
				if (!(classBase instanceof ClassNode)) continue;
				for (final FunctionNode baseFunction : ((ClassNode) classBase).getFunctions()) {
					final Node.Wrapper wrapper = new Node.Wrapper(baseFunction, Node.MatchLevel.PROTOTYPE_IDENTICAL, matcher);
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
			final Pair<Node, IASTNode> pair = childrenCreationQueue.poll();
			createChildrenFromAstNode(pair.getA(), pair.getB());
		}

		cleanUp();
		createOverride();
		rootNode.removeAllDependency();

		int nodeId = 0;
		for (final Node node : rootNode) node.setId(++nodeId);
		rootNode.setNodeCount(++nodeId);

		rootNode.lock();
		return rootNode;
	}

	private void replaceNode(@Nonnull UnknownNode oldNode, @Nonnull Node newNode) {
		assert !(newNode instanceof UnknownNode);
		unknownNodeList.remove(oldNode);
		if (oldNode.getParent() != null) {
			if (newNode.getParent() == null) oldNode.getParent().addChild(newNode);
			oldNode.transfer(newNode);
			if (newNode instanceof IntegralNode) {
				newNode.removeChildren();
				newNode.removeAllDependencyTo();
			}
			for (final Map.Entry<IBinding, Node> entry : bindingNodeMap.entrySet()) {
				if (entry.getValue() == oldNode) entry.setValue(newNode);
			}
			for (final Pair<Node, IASTNode> pair : childrenCreationQueue) {
				if (pair.getA() == oldNode) pair.setA(newNode);
			}
		} else {
			bindingNodeMap.entrySet().removeIf(entry -> entry.getValue() == oldNode);
			childrenCreationQueue.removeIf(pair -> pair.getA() == oldNode);
		}
		oldNode.remove();
	}

	@Nonnull
	private Node createIntegralNode(@Nonnull String typeName) {
		final Node existNode = integralNodeMap.get(typeName);
		if (existNode != null) return existNode;

		final Node newNode = new IntegralNode();
		newNode.setName(typeName);
		newNode.setUniqueName(typeName);
		newNode.setSignature(typeName);

		integralNodeMap.put(typeName, newNode);
		return newNode;
	}

	@Nonnull
	private Node createNode(@Nonnull IBinding binding, @Nullable IASTName astName, @Nullable String signature, @Nonnull Node buildingNode) {
		if (binding instanceof ICPPSpecialization) binding = ((ICPPSpecialization) binding).getSpecializedBinding();

		final Node existNode = bindingNodeMap.get(binding);
		if (existNode != null && (!(existNode instanceof UnknownNode) || buildingNode instanceof UnknownNode)) {
			return existNode;
		}

		final String name = firstNonBlank(
				astName != null ? astName.toString() : null,
				binding != null ? binding.getName() : null,
				binding != null ? binding.toString() : null
		);
		final String uniqueName = firstNonBlank(binding instanceof ICPPBinding
				? ASTTypeUtil.getQualifiedName((ICPPBinding) binding).replaceAll("\\{##ROOT##:\\d+}", "{ROOT}")
				: astName != null ? ASTStringUtil.getQualifiedName(astName) : null, name);

		final Node newNode = buildingNode instanceof UnknownNode && binding instanceof IProblemBinding
				? createIntegralNode(uniqueName)
				: uniqueName.isBlank() && signature != null && !signature.isBlank()
				? createIntegralNode(signature)
				: buildingNode.setName(name).setUniqueName(uniqueName).setSignature(signature != null ? signature : uniqueName);

		if (existNode != null) replaceNode((UnknownNode) existNode, newNode);

		if (newNode instanceof UnknownNode) unknownNodeList.add((UnknownNode) newNode);
		if (binding != null) bindingNodeMap.put(binding, newNode);
		return newNode;
	}

	@Nonnull
	private Node createFromDeclarator(@Nonnull Node typeNode, @Nonnull IASTDeclarator declarator) {
		final IASTName declaratorName = declarator.getName();
		final IBinding declaratorBinding = declaratorName.resolveBinding();
		final String signature = ASTStringUtil.getSignatureString(declarator);

		if (declarator instanceof ICPPASTFunctionDeclarator) {
			// region
			final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;

			final Node functionNode = createNode(declaratorBinding, declaratorName, signature,
					new FunctionNode().setType(typeNode));

			if (functionNode instanceof FunctionNode) {
				for (final ICPPASTParameterDeclaration functionParameter : functionDeclarator.getParameters()) {
					final IASTDeclSpecifier parameterSpecifier = functionParameter.getDeclSpecifier();
//					final ICPPASTDeclarator parameterDeclarator = functionParameter.getDeclarator();

					final Node parameterType = createFromDeclSpecifier(typeNode, parameterSpecifier);
//					final Node parameterNode = createFromDeclarator(parameterType, parameterDeclarator);

//					if (parameterNode.getParent() == null) {
//						functionNode.addChild(parameterNode);
////						functionNode.addDependencyTo(parameterNode, DependencyType.MEMBER);
//						((FunctionNode) functionNode).addParameter(parameterNode);
//					}
					((FunctionNode) functionNode).addParameter(parameterType);
					functionNode.addDependencyTo(parameterType, DependencyType.USE);
				}
			}
			// endregion
			return functionNode;
		} else if (declarator instanceof ICPPASTDeclarator) {
			// region
			final Node variableNode = createNode(declaratorBinding, declaratorName, signature, new VariableNode().setType(typeNode));
			final IASTInitializer initializer = declarator.getInitializer();
			if (initializer != null) {
				childrenCreationQueue.add(Pair.mutableOf(variableNode, initializer));
				//createChildrenFromAstNode(node, initializer);
				if (variableNode instanceof VariableNode) {
					((VariableNode) variableNode).setBody(initializer.getRawSignature());
				}
			}
			// endregion
			return variableNode;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromDeclarator(typeNode = (" + Utilities.objectIdentifyString(typeNode)
					+ "), declarator = (" + Utilities.objectIdentifyString(declarator) + "))");
		}
	}

	@Nonnull
	private Node createFromDeclSpecifier(@Nonnull Node parentNode, @Nonnull IASTDeclSpecifier declSpecifier) {
		final String signature = ASTStringUtil.getSignatureString(declSpecifier, null);

		if (declSpecifier instanceof ICPPASTEnumerationSpecifier) {
			// region
			final ICPPASTEnumerationSpecifier enumerationSpecifier = (ICPPASTEnumerationSpecifier) declSpecifier;
			final IASTName enumerationName = enumerationSpecifier.getName();
			final IBinding enumerationBinding = enumerationName.resolveBinding();

			final Node enumNode = createNode(enumerationBinding, enumerationName, signature, new EnumNode());

			final ICPPASTDeclSpecifier enumBaseType = enumerationSpecifier.getBaseType();
			final Node baseType = enumBaseType != null ? createFromDeclSpecifier(enumNode, enumBaseType) : null;

			final Node nodeType = enumerationSpecifier.isScoped() ? enumNode : baseType;
			final StringBuilder bodyBuilder = enumNode.getName().isBlank() ? new StringBuilder() : null;
			for (final IASTEnumerationSpecifier.IASTEnumerator enumerator : enumerationSpecifier.getEnumerators()) {
				final IASTName enumeratorName = enumerator.getName();
				final IBinding enumeratorBinding = enumeratorName.resolveBinding();

				final Node enumeratorNode = createNode(enumeratorBinding, enumeratorName, null, new VariableNode().setType(nodeType));
				if (bodyBuilder != null) {
					bodyBuilder.append(bodyBuilder.length() > 0 ? ',' : "enum{")
							.append(enumeratorNode.getName());
				}

				enumNode.addChild(enumeratorNode);
				enumNode.addDependencyTo(enumeratorNode, DependencyType.MEMBER);
			}
			if (enumNode instanceof EnumNode) {
				((EnumNode) enumNode).setType(baseType);
				if (bodyBuilder != null) enumNode.setName(bodyBuilder.append('}').toString());
			}
			parentNode.addChild(enumNode);
			parentNode.addDependencyTo(enumNode, DependencyType.MEMBER);
			// endregion
			return enumNode;
		} else if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
			// region
			final ICPPASTCompositeTypeSpecifier classSpecifier = (ICPPASTCompositeTypeSpecifier) declSpecifier;
			final IASTName className = classSpecifier.getName();
			final IBinding classBinding = className.resolveBinding();

			final Node classNode = createNode(classBinding, className, signature, new ClassNode());
			final StringBuilder bodyBuilder = classNode.getName().isBlank()
					? new StringBuilder().append(classNode.getSignature()).append('{')
					: null;
			for (final IASTDeclaration classChildDeclaration : classSpecifier.getDeclarations(false)) {
				final List<Node> nodeList = createChildrenFromDeclaration(classNode, classChildDeclaration);
				if (bodyBuilder != null) {
					for (final Node node : nodeList) {
						bodyBuilder.append(node.getName()).append(';');
					}
				}
			}
			if (classNode instanceof ClassNode) {
				if (bodyBuilder!= null) {
					classNode.setName(bodyBuilder.append('}').toString());
				}
				for (final ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier classBaseSpecifier : classSpecifier.getBaseSpecifiers()) {
					final ICPPASTNameSpecifier classBaseNameSpecifier = classBaseSpecifier.getNameSpecifier();
					final IBinding classBaseNameBinding = classBaseNameSpecifier.resolveBinding();

					final Node classBaseNode = createNode(classBaseNameBinding, null, null, new UnknownNode());
					((ClassNode) classNode).addBase(classBaseNode);
					classNode.addDependencyTo(classBaseNode, DependencyType.INHERITANCE);
				}
			}
			if (classNode.getParent() == null) {
				parentNode.addChild(classNode);
				parentNode.addDependencyTo(classNode, DependencyType.MEMBER);
			}
			// endregion
			return classNode;
		} else if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
			// region
			final IASTNamedTypeSpecifier namedSpecifier = (IASTNamedTypeSpecifier) declSpecifier;
			final IASTName namedName = namedSpecifier.getName();
			final IBinding namedBinding = namedName.resolveBinding();

			//noinspection UnnecessaryLocalVariable
			final Node namedNode = createNode(namedBinding, namedName, signature, new UnknownNode());
			// endregion
			return namedNode;
		} else if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier) {
			// region
			final ICPPASTElaboratedTypeSpecifier elaboratedSpecifier = (ICPPASTElaboratedTypeSpecifier) declSpecifier;
			final IASTName elaboratedName = elaboratedSpecifier.getName();
			final IBinding elaboratedBinding = elaboratedName.resolveBinding();

			switch (elaboratedSpecifier.getKind()) {
				case IASTElaboratedTypeSpecifier.k_enum:
					return createNode(elaboratedBinding, elaboratedName, signature, new EnumNode());
				case IASTElaboratedTypeSpecifier.k_struct:
				case IASTElaboratedTypeSpecifier.k_union:
				case ICPPASTElaboratedTypeSpecifier.k_class:
					return createNode(elaboratedBinding, elaboratedName, signature, new ClassNode());
			}

			//noinspection UnnecessaryLocalVariable
			final Node elaboratedNode = createNode(elaboratedBinding, elaboratedName, signature, new UnknownNode());
			// endregion
			return elaboratedNode;
		} else if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier) {
			// region
			//noinspection UnnecessaryLocalVariable
			final Node simpleNode = createIntegralNode(signature);
			// endregion
			return simpleNode;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromDeclSpecifier(declSpecifier = (" + Utilities.objectIdentifyString(declSpecifier) + "))");
		}
	}

	@Nonnull
	private Node createFromTemplateParameter(@Nonnull Node parentNode, @Nonnull ICPPASTTemplateParameter templateParameter) {
		if (templateParameter instanceof ICPPASTParameterDeclaration) {
			final ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration) templateParameter;

			final IASTDeclSpecifier parameterSpecifier = parameterDeclaration.getDeclSpecifier();
			final ICPPASTDeclarator parameterDeclarator = parameterDeclaration.getDeclarator();

			final Node parameterType = createFromDeclSpecifier(parentNode, parameterSpecifier);
			//noinspection UnnecessaryLocalVariable
			final Node parameterNode = createFromDeclarator(parameterType, parameterDeclarator);

			return parameterNode;

		} else if (templateParameter instanceof ICPPASTSimpleTypeTemplateParameter) {
			final ICPPASTSimpleTypeTemplateParameter simpleParameter = (ICPPASTSimpleTypeTemplateParameter) templateParameter;
			final IASTName simpleName = simpleParameter.getName();
			final IBinding simpleBinding = simpleName.resolveBinding();

			//noinspection UnnecessaryLocalVariable
			final Node variableNode = createNode(simpleBinding, simpleName, null, new VariableNode());

			return variableNode;
		} else if (templateParameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
			final ICPPASTTemplatedTypeTemplateParameter nestedTemplateParameter = (ICPPASTTemplatedTypeTemplateParameter) templateParameter;
			final IASTName nestedTemplateName = nestedTemplateParameter.getName();
			final IBinding nestedTemplateBinding = nestedTemplateName.resolveBinding();
			final Node nestedTemplateNode = createNode(nestedTemplateBinding, nestedTemplateName,
					null, new VariableNode());

			for (final ICPPASTTemplateParameter nestedParameter : nestedTemplateParameter.getTemplateParameters()) {
				final Node nestedNode = createFromTemplateParameter(nestedTemplateNode, nestedParameter);
				nestedTemplateNode.addChild(nestedNode);
				nestedTemplateNode.addDependencyTo(nestedNode, DependencyType.MEMBER);
			}

			return nestedTemplateNode;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromTemplateParameter(parentNode = (" + Utilities.objectIdentifyString(parentNode)
					+ "), templateParameter = (" + Utilities.objectIdentifyString(templateParameter) + "))");
		}
	}

	@Nonnull
	private List<Node> createChildrenFromDeclaration(@Nonnull Node parentNode, @Nonnull IASTDeclaration declaration) {
		if (declaration instanceof ICPPASTVisibilityLabel
				|| declaration instanceof ICPPASTUsingDeclaration
				|| declaration instanceof ICPPASTNamespaceAlias
				|| declaration instanceof IASTProblemDeclaration
				|| declaration instanceof ICPPASTStaticAssertDeclaration
				|| declaration instanceof ICPPASTExplicitTemplateInstantiation
		) {
			// skipped
			return List.of();

		} else if (declaration instanceof ICPPASTUsingDirective) {
			final ICPPASTUsingDirective usingDirective = (ICPPASTUsingDirective) declaration;
			final IASTName namespaceName = usingDirective.getQualifiedName();
			final IBinding namespaceBinding = namespaceName.resolveBinding();

			final Node namespaceNode = createNode(namespaceBinding, namespaceName, null, new NamespaceNode());
			parentNode.addDependencyTo(namespaceNode, DependencyType.USE);
			return List.of(namespaceNode);

		} else if (declaration instanceof ICPPASTLinkageSpecification) {
			final ICPPASTLinkageSpecification linkageSpecification = (ICPPASTLinkageSpecification) declaration;
			final List<Node> childrenNode = new ArrayList<>();
			for (final IASTDeclaration linkageDeclaration : linkageSpecification.getDeclarations(false)) {
				childrenNode.addAll(createChildrenFromDeclaration(parentNode, linkageDeclaration));
			}
			return childrenNode;

		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			// region
			final ICPPASTNamespaceDefinition namespaceDefinition = (ICPPASTNamespaceDefinition) declaration;
			final IASTName namespaceName = namespaceDefinition.getName();
			final IBinding namespaceBinding = namespaceName.resolveBinding();

			final Node namespaceNode = createNode(namespaceBinding, namespaceName, null, new NamespaceNode());
			final IASTDeclaration[] namespaceChildDeclarations = namespaceDefinition.getDeclarations(false);
			for (final IASTDeclaration namespaceChildDeclaration : namespaceChildDeclarations) {
				createChildrenFromDeclaration(namespaceNode, namespaceChildDeclaration);
			}
			// endregion
			parentNode.addChild(namespaceNode);
			parentNode.addDependencyTo(namespaceNode, DependencyType.MEMBER);
			return List.of(namespaceNode);

		} else if (declaration instanceof IASTSimpleDeclaration) {
			// region
			final IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;

			final IASTDeclSpecifier simpleSpecifier = simpleDeclaration.getDeclSpecifier();
			final IASTDeclarator[] simpleDeclarators = simpleDeclaration.getDeclarators();

			final Node simpleNodeType = createFromDeclSpecifier(parentNode, simpleSpecifier);
			if (simpleNodeType.getParent() == null) {
				parentNode.addChild(simpleNodeType);
				parentNode.addDependencyTo(simpleNodeType, DependencyType.MEMBER);
			} else if (simpleDeclarators.length > 0) {
				parentNode.addDependencyTo(simpleNodeType, DependencyType.USE);
			}

			final List<Node> simpleNodeList = new ArrayList<>();
			for (final IASTDeclarator simpleDeclarator : simpleDeclarators) {
				final Node simpleNode = createFromDeclarator(simpleNodeType, simpleDeclarator);
				simpleNodeList.add(simpleNode);
				if (simpleNode.getParent() == null) {
					parentNode.addChild(simpleNode);
					parentNode.addDependencyTo(simpleNode, DependencyType.MEMBER);
				}
				simpleNode.addDependencyTo(simpleNodeType, DependencyType.USE);
			}

			return simpleNodeList.size() > 0 ? simpleNodeList : List.of(simpleNodeType);
			// endregion

		} else if (declaration instanceof ICPPASTFunctionDefinition) {
			// region
			final ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;

			final IASTDeclSpecifier functionSpecifier = functionDefinition.getDeclSpecifier();
			final IASTFunctionDeclarator functionDeclarator = functionDefinition.getDeclarator();

			final Node functionReturnType = createFromDeclSpecifier(parentNode, functionSpecifier);
			final Node functionNode = createFromDeclarator(functionReturnType, functionDeclarator);

			functionNode.addDependencyTo(functionReturnType, DependencyType.USE);

			final StringBuilder functionBodyBuilder = new StringBuilder();

			// function dependency
			for (final ICPPASTConstructorChainInitializer memberChainInitializer : functionDefinition.getMemberInitializers()) {
				final IASTName memberName = memberChainInitializer.getMemberInitializerId();
				final IBinding memberBinding = memberName.resolveBinding();

				final Node memberNode = createNode(memberBinding, memberName, null, new UnknownNode());
				if (memberNode.getParent() == null) {
					parentNode.addChild(memberNode);
					parentNode.addDependencyTo(memberNode, DependencyType.MEMBER);
				}
				functionNode.addDependencyTo(memberNode, DependencyType.USE);

				final IASTInitializer memberInitializer = memberChainInitializer.getInitializer();
				childrenCreationQueue.add(Pair.mutableOf(functionNode, memberInitializer));
				//createChildrenFromAstNode(functionNode, memberInitializer);

				functionBodyBuilder.append(memberName.toString()).append('=').append(memberInitializer.getRawSignature()).append(';');
			}

			final IASTStatement functionBody = functionDefinition.getBody();
			if (functionBody != null) {
				childrenCreationQueue.add(Pair.mutableOf(functionNode, functionBody));
				//createChildrenFromAstNode(functionNode, functionBody);
				if (functionNode instanceof FunctionNode) {
					((FunctionNode) functionNode).setBody(functionBodyBuilder.append(functionBody.getRawSignature()).toString());
				}
			}

			if (functionNode.getParent() == null) {
				parentNode.addChild(functionNode);
				parentNode.addDependencyTo(functionNode, DependencyType.MEMBER);
			}
			return List.of(functionNode);
			// endregion

		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			final ICPPASTTemplateDeclaration templateDeclaration = (ICPPASTTemplateDeclaration) declaration;

			for (final ICPPASTTemplateParameter templateParameter : templateDeclaration.getTemplateParameters()) {
				final Node templateNode = createFromTemplateParameter(parentNode, templateParameter);
				parentNode.addChild(templateNode);
			}

			final IASTDeclaration innerDeclaration = templateDeclaration.getDeclaration();
			//noinspection UnnecessaryLocalVariable
			final List<Node> innerNodeList = createChildrenFromDeclaration(parentNode, innerDeclaration);

//			for (final Node innerNode : innerNodeList) {
//				parentNode.addChild(innerNode);
//				parentNode.addDependencyTo(innerNode, DependencyType.MEMBER);
//			}

			return innerNodeList;

		} else if (declaration instanceof ICPPASTAliasDeclaration) {
			// region
			final ICPPASTAliasDeclaration aliasDefinition = (ICPPASTAliasDeclaration) declaration;
			final IASTName aliasName = aliasDefinition.getAlias();
			final IBinding aliasBinding = aliasName.resolveBinding();

			final ICPPASTTypeId aliasTypeId = aliasDefinition.getMappingTypeId();

			final IASTDeclSpecifier aliasDeclSpecifier = aliasTypeId.getDeclSpecifier();
			final Node aliasType = createFromDeclSpecifier(parentNode, aliasDeclSpecifier);

			final IASTDeclarator aliasDeclarator = aliasTypeId.getAbstractDeclarator();
			final Node aliasNodeType = createFromDeclarator(aliasType, aliasDeclarator);

			final Node aliasNode = createNode(aliasBinding, aliasName,
					ASTStringUtil.getSignatureString(aliasDeclSpecifier, aliasDeclarator),
					new VariableNode().setType(aliasNodeType));

			// endregion
			parentNode.addChild(aliasNode);
			parentNode.addDependencyTo(aliasNode, DependencyType.MEMBER);
			parentNode.addDependencyTo(aliasType, DependencyType.USE);
			parentNode.addDependencyTo(aliasNodeType, DependencyType.USE);
			return List.of(aliasNode);

		} else {
			// todo: debug?
			throw new IllegalArgumentException("createChildrenFromDeclaration(parentNode = (" + Utilities.objectIdentifyString(parentNode)
					+ "), declaration = (" + Utilities.objectIdentifyString(declaration) + "))");
		}
	}

	private void createChildrenFromAstNode(@Nonnull Node parentNode, @Nonnull IASTNode astNode) {
		for (final IASTNode astChild : astNode.getChildren()) {
			if (astChild instanceof IASTDeclaration) {
				createChildrenFromDeclaration(parentNode, (IASTDeclaration) astChild);
			} else if (astChild instanceof IASTName) {
				final IASTName astName = (IASTName) astChild;
				final IBinding astBinding = astName.resolveBinding();

				final Node childNode = createNode(astBinding, astName, null, new UnknownNode());
				if (!(childNode instanceof IntegralNode)) {
					parentNode.addDependencyTo(childNode, childNode instanceof FunctionNode
							? DependencyType.INVOCATION : DependencyType.USE);
				}
			} else {
				createChildrenFromAstNode(parentNode, astChild);
			}
		}
	}
}
