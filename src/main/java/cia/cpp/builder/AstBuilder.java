package cia.cpp.builder;

import cia.cpp.ast.IFunction;
import cia.cpp.ast.IVariable;
import cia.cpp.ast.*;
import org.eclipse.cdt.core.dom.ast.*;
import org.eclipse.cdt.core.dom.ast.cpp.*;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;

import java.util.*;

final class AstBuilder {
	private final Map<String, INode> integralNodeMap = new HashMap<>();
	private final Map<IBinding, INode> bindingNodeMap = new HashMap<>();
	private final List<INode> unknownNodeList = new LinkedList<>();

	private AstBuilder() {
	}

	private static String objectToString(Object object) {
		return object != null ? String.format("(0x%08X) %s", object.hashCode(), object.getClass().getSimpleName()) : "null";
	}

	public static IRoot build(IASTTranslationUnit translationUnit) {
		return new AstBuilder().internalBuild(translationUnit);
	}

	private IRoot internalBuild(IASTTranslationUnit translationUnit) {
		final IRoot rootNode = RootNode.builder().build();
		for (final IASTDeclaration declaration : translationUnit.getDeclarations()) {
			createChildrenFromDeclaration(rootNode, declaration);
		}
		for (final INode node : bindingNodeMap.values()) {
			if (node instanceof IUnknown || node instanceof IIntegral) {
				// replace unknown node with integral node
				node.removeFromParent();
				node.removeChildren();
				node.removeDependencies();
				if (node instanceof IUnknown) {
					final INode newNode = createIntegralNode(node.getName(), IntegralNode.builder());
					replaceNode(node, newNode);
				}
			} else {
				// remove all dependency to unknown node and integral node
				final Set<INode> keySet = Set.copyOf(node.getDependencies().keySet());
				for (final INode dependencyNode : keySet) {
					if (dependencyNode instanceof IUnknown || dependencyNode instanceof IIntegral) {
						node.removeDependency(dependencyNode);
					}
				}
				if (node instanceof IVariable) {
					// remove all children
					node.removeChildren();
				} else if (node instanceof IFunction) {
					final IFunction function = (IFunction) node;
					final List<INode> parameters = List.copyOf(function.getParameters());
					final List<INode> variables = new ArrayList<>(function.getVariables());
					variables.removeAll(parameters);
					for (final INode variable : variables) function.removeChild(variable);
				}
			}
		}
		for (final INode node : List.copyOf(unknownNodeList)) {
			// replace unknown node with integral node
			node.removeChildren();
			node.removeDependencies();
			final INode newNode = createIntegralNode(node.getName(), IntegralNode.builder());
			replaceNode(node, newNode);
		}
		rootNode.addIntegrals(List.copyOf(integralNodeMap.values()));
		return rootNode;
	}

	private <E extends INode, B extends INode.INodeBuilder<E, B>>
	INode createIntegralNode(String typeName, B builder) {
		if (typeName.isBlank()) return null;

		final INode existNode = integralNodeMap.get(typeName);
		if (existNode != null) return existNode;

		final INode newNode = builder
				.setName(typeName)
				.setUniqueName(typeName)
				.setSignature(typeName)
				.build();

		integralNodeMap.put(typeName, newNode);
		return newNode;
	}

	private void replaceNode(INode oldNode, INode newNode) {
		for (final Map.Entry<IBinding, INode> entry : bindingNodeMap.entrySet()) {
			final INode node = entry.getValue();
			if (node instanceof ITypeContainer) {
				final ITypeContainer typeContainerNode = (ITypeContainer) node;
				if (typeContainerNode.getType() == oldNode) {
					typeContainerNode.setType(newNode);
				}
			}
			if (node instanceof IClass) {
				final IClass classNode = (IClass) node;
				classNode.replaceBase(oldNode, newNode);
			}
			if (node instanceof IFunction) {
				final IFunction functionNode = (IFunction) node;
				functionNode.replaceParameter(oldNode, newNode);
			}
			node.replaceChild(oldNode, newNode);
			node.replaceDependency(oldNode, newNode);
			if (node == oldNode) entry.setValue(newNode);
		}
		for (final ListIterator<INode> iterator = unknownNodeList.listIterator(); iterator.hasNext(); ) {
			final INode node = iterator.next();
			if (node != oldNode) {
				if (node instanceof ITypeContainer) {
					final ITypeContainer typeContainerNode = (ITypeContainer) node;
					if (typeContainerNode.getType() == oldNode) {
						typeContainerNode.setType(newNode);
					}
				}
				if (node instanceof IClass) {
					final IClass classNode = (IClass) node;
					classNode.replaceBase(oldNode, newNode);
				}
				if (node instanceof IFunction) {
					final IFunction functionNode = (IFunction) node;
					functionNode.replaceParameter(oldNode, newNode);
				}
				node.replaceChild(oldNode, newNode);
				node.replaceDependency(oldNode, newNode);
			} else {
				iterator.remove();
			}
		}
		newNode.addChildren(oldNode.removeChildren());
		newNode.addDependencies(oldNode.removeDependencies());
	}

	private <E extends INode, B extends INode.INodeBuilder<E, B>>
	INode createNode(IBinding binding, IASTName astName, String signature, B builder) {
		if (binding instanceof ICPPSpecialization) {
			binding = ((ICPPSpecialization) binding).getSpecializedBinding();
		}

		final INode existNode = bindingNodeMap.get(binding);
		if (existNode != null && (!(existNode instanceof IUnknown) || builder instanceof IUnknown.IUnknownBuilder)) {
			return existNode;
		}

		final String name = astName != null ? astName.toString() : binding != null ? binding.getName() : "";
		final String uniqueName = binding instanceof ICPPBinding
				? ASTTypeUtil.getQualifiedName((ICPPBinding) binding).replaceAll("^\\{ROOT:\\d+}", "{ROOT}")
				: astName != null ? ASTStringUtil.getQualifiedName(astName) : name;

		final INode newNode = builder instanceof IUnknown.IUnknownBuilder && binding instanceof IProblemBinding
				? createIntegralNode(uniqueName, IntegralNode.builder())
				: uniqueName.isBlank() && signature != null && !signature.isBlank()
				? createIntegralNode(signature, IntegralNode.builder())
				: builder.setName(name).setUniqueName(uniqueName).setSignature(signature != null ? signature : uniqueName).build();

		if (existNode != null) replaceNode(existNode, newNode);

		if (newNode instanceof IUnknown) unknownNodeList.add(newNode);
		if (binding != null) bindingNodeMap.put(binding, newNode);
		return newNode;
	}

	private INode createFromDeclarator(INode typeNode, IASTDeclarator declarator) {
		final IASTName declaratorName = declarator.getName();
		final IBinding declaratorBinding = declaratorName.resolveBinding();
		final String signature = ASTStringUtil.getSignatureString(declarator);

		if (declarator instanceof ICPPASTFunctionDeclarator) {
			// region
			final ICPPASTFunctionDeclarator functionDeclarator = (ICPPASTFunctionDeclarator) declarator;

			final INode functionNode = createNode(declaratorBinding, declaratorName, signature,
					FunctionNode.builder().setType(typeNode));

			//if (functionNode instanceof IFunction) {
			for (final ICPPASTParameterDeclaration functionParameter : functionDeclarator.getParameters()) {
				final IASTDeclSpecifier parameterSpecifier = functionParameter.getDeclSpecifier();
				final ICPPASTDeclarator parameterDeclarator = functionParameter.getDeclarator();

				final INode parameterType = createFromDeclSpecifier(typeNode, parameterSpecifier);
				final INode parameterNode = createFromDeclarator(parameterType, parameterDeclarator);

				((IFunction) functionNode).addParameter(parameterNode);
				functionNode.addDependency(parameterNode).setType(Dependency.Type.MEMBER);
				functionNode.addDependency(parameterType).setType(Dependency.Type.USE);
				//}
			}
			// endregion
			return functionNode;
		} else if (declarator instanceof ICPPASTDeclarator) {
			// region
			// todo: array, field, typedef
			//noinspection UnnecessaryLocalVariable
			final INode node = createNode(declaratorBinding, declaratorName, signature, VariableNode.builder().setType(typeNode));
			// endregion
			return node;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromDeclarator(typeNode = " + objectToString(typeNode)
					+ ", declarator = " + objectToString(declarator) + ")");
		}
	}

	private INode createFromDeclSpecifier(INode parentNode, IASTDeclSpecifier declSpecifier) {
		final String signature = ASTStringUtil.getSignatureString(declSpecifier, null);

		if (declSpecifier instanceof ICPPASTEnumerationSpecifier) {
			// region
			final ICPPASTEnumerationSpecifier enumerationSpecifier = (ICPPASTEnumerationSpecifier) declSpecifier;
			final IASTName enumerationName = enumerationSpecifier.getName();
			final IBinding enumerationBinding = enumerationName.resolveBinding();

			final INode enumNode = createNode(enumerationBinding, enumerationName, signature, EnumNode.builder());

			final INode nodeType = enumerationSpecifier.isScoped() ? enumNode : null;
			for (final IASTEnumerationSpecifier.IASTEnumerator enumerator : enumerationSpecifier.getEnumerators()) {
				final IASTName enumeratorName = enumerator.getName();
				final IBinding enumeratorBinding = enumeratorName.resolveBinding();

				final INode enumeratorNode = createNode(enumeratorBinding, enumeratorName, null, VariableNode.builder().setType(nodeType));

				enumNode.addChild(enumeratorNode);
				enumNode.addDependency(enumeratorNode).setType(Dependency.Type.MEMBER);
			}
			// endregion
			parentNode.addChild(enumNode);
			parentNode.addDependency(enumNode).setType(Dependency.Type.MEMBER);
			return enumNode;
		} else if (declSpecifier instanceof ICPPASTCompositeTypeSpecifier) {
			// region
			final ICPPASTCompositeTypeSpecifier classSpecifier = (ICPPASTCompositeTypeSpecifier) declSpecifier;
			final IASTName className = classSpecifier.getName();
			final IBinding classBinding = className.resolveBinding();

			final INode classNode = createNode(classBinding, className, signature, ClassNode.builder());

			//if (classNode instanceof IClass) {
			for (final ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier classBaseSpecifier : classSpecifier.getBaseSpecifiers()) {
				final ICPPASTNameSpecifier classBaseNameSpecifier = classBaseSpecifier.getNameSpecifier();
				final IBinding classBaseNameBinding = classBaseNameSpecifier.resolveBinding();

				final INode classBaseNode = createNode(classBaseNameBinding, null, null, UnknownNode.builder());
				((IClass) classNode).addBase(classBaseNode);
				classNode.addDependency(classBaseNode).setType(Dependency.Type.INHERITANCE);
			}
			//}

			for (final IASTDeclaration classChildDeclaration : classSpecifier.getDeclarations(false)) {
				createChildrenFromDeclaration(classNode, classChildDeclaration);
			}
			// endregion
			parentNode.addChild(classNode);
			parentNode.addDependency(classNode).setType(Dependency.Type.MEMBER);
			return classNode;
		} else if (declSpecifier instanceof ICPPASTNamedTypeSpecifier) {
			// region
			final IASTNamedTypeSpecifier namedSpecifier = (IASTNamedTypeSpecifier) declSpecifier;
			final IASTName namedName = namedSpecifier.getName();
			final IBinding namedBinding = namedName.resolveBinding();

			//noinspection UnnecessaryLocalVariable
			final INode namedNode = createNode(namedBinding, namedName, signature, UnknownNode.builder());
			// endregion
			return namedNode;
		} else if (declSpecifier instanceof ICPPASTElaboratedTypeSpecifier) {
			// region
			final ICPPASTElaboratedTypeSpecifier elaboratedSpecifier = (ICPPASTElaboratedTypeSpecifier) declSpecifier;
			final IASTName elaboratedName = elaboratedSpecifier.getName();
			final IBinding elaboratedBinding = elaboratedName.resolveBinding();

			switch (elaboratedSpecifier.getKind()) {
				case IASTElaboratedTypeSpecifier.k_enum:
					return createNode(elaboratedBinding, elaboratedName, signature, EnumNode.builder());
				case IASTElaboratedTypeSpecifier.k_struct:
				case IASTElaboratedTypeSpecifier.k_union:
				case ICPPASTElaboratedTypeSpecifier.k_class:
					return createNode(elaboratedBinding, elaboratedName, signature, ClassNode.builder());
			}

			//noinspection UnnecessaryLocalVariable
			final INode elaboratedNode = createNode(elaboratedBinding, elaboratedName, signature, UnknownNode.builder());
			// endregion
			return elaboratedNode;
		} else if (declSpecifier instanceof ICPPASTSimpleDeclSpecifier) {
			// region
			//noinspection UnnecessaryLocalVariable
			final INode simpleNode = createIntegralNode(signature, IntegralNode.builder());
			// endregion
			return simpleNode;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromDeclSpecifier(declSpecifier = " + objectToString(declSpecifier) + ")");
		}
	}

	private INode createFromTemplateParameter(INode parentNode, ICPPASTTemplateParameter templateParameter) {
		if (templateParameter instanceof ICPPASTParameterDeclaration) {
			final ICPPASTParameterDeclaration parameterDeclaration = (ICPPASTParameterDeclaration) templateParameter;

			final IASTDeclSpecifier parameterSpecifier = parameterDeclaration.getDeclSpecifier();
			final ICPPASTDeclarator parameterDeclarator = parameterDeclaration.getDeclarator();

			final INode parameterType = createFromDeclSpecifier(parentNode, parameterSpecifier);
			//noinspection UnnecessaryLocalVariable
			final INode parameterNode = createFromDeclarator(parameterType, parameterDeclarator);

			return parameterNode;

		} else if (templateParameter instanceof ICPPASTSimpleTypeTemplateParameter) {
			final ICPPASTSimpleTypeTemplateParameter simpleParameter = (ICPPASTSimpleTypeTemplateParameter) templateParameter;
			final IASTName simpleName = simpleParameter.getName();
			final IBinding simpleBinding = simpleName.resolveBinding();

			//noinspection UnnecessaryLocalVariable
			final INode simpleNode = createNode(simpleBinding, simpleName, null, VariableNode.builder());

			return simpleNode;
		} else if (templateParameter instanceof ICPPASTTemplatedTypeTemplateParameter) {
			final ICPPASTTemplatedTypeTemplateParameter nestedTemplateParameter = (ICPPASTTemplatedTypeTemplateParameter) templateParameter;
			final IASTName nestedTemplateName = nestedTemplateParameter.getName();
			final IBinding nestedTemplateBinding = nestedTemplateName.resolveBinding();
			final INode nestedTemplateNode = createNode(nestedTemplateBinding, nestedTemplateName,
					null, VariableNode.builder());

			for (final ICPPASTTemplateParameter nestedParameter : nestedTemplateParameter.getTemplateParameters()) {
				final INode nestedNode = createFromTemplateParameter(nestedTemplateNode, nestedParameter);
				nestedTemplateNode.addChild(nestedNode);
				nestedTemplateNode.addDependency(nestedNode).setType(Dependency.Type.MEMBER);
			}

			return nestedTemplateNode;
		} else {
			// todo: debug?
			throw new IllegalArgumentException("createFromTemplateParameter(parentNode = " + objectToString(parentNode)
					+ ", templateParameter = " + objectToString(templateParameter) + ")");
		}
	}

	private List<INode> createChildrenFromDeclaration(INode parentNode, IASTDeclaration declaration) {
		if (declaration instanceof ICPPASTVisibilityLabel
				|| declaration instanceof ICPPASTUsingDeclaration
				|| declaration instanceof IASTProblemDeclaration
				|| declaration instanceof ICPPASTStaticAssertDeclaration) {
			// skipped
			return List.of();

		} else if (declaration instanceof ICPPASTNamespaceDefinition) {
			// region
			final ICPPASTNamespaceDefinition namespaceDefinition = (ICPPASTNamespaceDefinition) declaration;
			final IASTName namespaceName = namespaceDefinition.getName();
			final IBinding namespaceBinding = namespaceName.resolveBinding();

			final INode namespaceNode = createNode(namespaceBinding, namespaceName, null, NamespaceNode.builder());
			final IASTDeclaration[] namespaceChildDeclarations = namespaceDefinition.getDeclarations(false);
			for (final IASTDeclaration namespaceChildDeclaration : namespaceChildDeclarations) {
				createChildrenFromDeclaration(namespaceNode, namespaceChildDeclaration);
			}
			// endregion
			parentNode.addChild(namespaceNode);
			parentNode.addDependency(namespaceNode).setType(Dependency.Type.MEMBER);
			return List.of(namespaceNode);

		} else if (declaration instanceof IASTSimpleDeclaration) {
			// region
			final IASTSimpleDeclaration simpleDeclaration = (IASTSimpleDeclaration) declaration;

			final IASTDeclSpecifier simpleSpecifier = simpleDeclaration.getDeclSpecifier();
			final INode simpleNodeType = createFromDeclSpecifier(parentNode, simpleSpecifier);

			if (simpleNodeType != null) parentNode.addDependency(simpleNodeType).setType(Dependency.Type.USE);

			final List<INode> simpleNodeList = new ArrayList<>();
			for (final IASTDeclarator simpleDeclarator : simpleDeclaration.getDeclarators()) {
				final INode simpleNode = createFromDeclarator(simpleNodeType, simpleDeclarator);
				simpleNodeList.add(simpleNode);
				parentNode.addChild(simpleNode);
				parentNode.addDependency(simpleNode).setType(Dependency.Type.MEMBER);
			}
			// endregion
			return simpleNodeList.size() > 0 ? simpleNodeList : simpleNodeType != null ? List.of(simpleNodeType) : List.of();

		} else if (declaration instanceof ICPPASTFunctionDefinition) {
			// region
			final ICPPASTFunctionDefinition functionDefinition = (ICPPASTFunctionDefinition) declaration;

			final IASTDeclSpecifier functionSpecifier = functionDefinition.getDeclSpecifier();
			final IASTFunctionDeclarator functionDeclarator = functionDefinition.getDeclarator();

			final INode functionReturnType = createFromDeclSpecifier(parentNode, functionSpecifier);
			final INode functionNode = createFromDeclarator(functionReturnType, functionDeclarator);

			if (functionReturnType != null) functionNode.addDependency(functionReturnType).setType(Dependency.Type.USE);

			// todo: function dependency
			for (final ICPPASTConstructorChainInitializer memberChainInitializer : functionDefinition.getMemberInitializers()) {
				final IASTName memberName = memberChainInitializer.getMemberInitializerId();
				final IBinding memberBinding = memberName.resolveBinding();

				final INode memberNode = createNode(memberBinding, memberName, null, UnknownNode.builder());
				functionNode.addDependency(memberNode).setType(Dependency.Type.MEMBER);

				final IASTInitializer memberInitializer = memberChainInitializer.getInitializer();
				createChildrenFromAstNode(functionNode, memberInitializer);
			}

			final IASTStatement functionBody = functionDefinition.getBody();
			if (functionBody != null) createChildrenFromAstNode(functionNode, functionBody);

			parentNode.addChild(functionNode);
			parentNode.addDependency(functionNode).setType(Dependency.Type.MEMBER);
			return List.of(functionNode);
			// endregion

		} else if (declaration instanceof ICPPASTTemplateDeclaration) {
			final ICPPASTTemplateDeclaration templateDeclaration = (ICPPASTTemplateDeclaration) declaration;

			final IASTDeclaration innerDeclaration = templateDeclaration.getDeclaration();
			final List<INode> innerNodeList = createChildrenFromDeclaration(parentNode, innerDeclaration);

			for (final ICPPASTTemplateParameter templateParameter : templateDeclaration.getTemplateParameters()) {
				final INode templateNode = createFromTemplateParameter(parentNode, templateParameter);
				for (final INode innerNode : innerNodeList) {
					innerNode.addChild(templateNode);
					innerNode.addDependency(templateNode).setType(Dependency.Type.MEMBER);
				}
			}
			return innerNodeList;

		} else if (declaration instanceof ICPPASTAliasDeclaration) {
			// region
			final ICPPASTAliasDeclaration aliasDefinition = (ICPPASTAliasDeclaration) declaration;
			final IASTName aliasName = aliasDefinition.getAlias();
			final IBinding aliasBinding = aliasName.resolveBinding();

			final ICPPASTTypeId aliasTypeId = aliasDefinition.getMappingTypeId();

			final IASTDeclSpecifier aliasDeclSpecifier = aliasTypeId.getDeclSpecifier();
			final INode aliasType = createFromDeclSpecifier(parentNode, aliasDeclSpecifier);

			final IASTDeclarator aliasDeclarator = aliasTypeId.getAbstractDeclarator();
			final INode aliasNodeType = createFromDeclarator(aliasType, aliasDeclarator);

			final INode aliasNode = createNode(aliasBinding, aliasName,
					ASTStringUtil.getSignatureString(aliasDeclSpecifier, aliasDeclarator),
					VariableNode.builder().setType(aliasNodeType));

			// endregion
			parentNode.addChild(aliasNode);
			parentNode.addDependency(aliasNode).setType(Dependency.Type.MEMBER);
			parentNode.addDependency(aliasType).setType(Dependency.Type.USE);
			parentNode.addDependency(aliasNodeType).setType(Dependency.Type.USE);
			return List.of(aliasNode);

		} else {
			// todo: debug?
			throw new IllegalArgumentException("createChildrenFromDeclaration(parentNode = " + objectToString(parentNode)
					+ ", declaration = " + objectToString(declaration) + ")");
		}
	}

	private void createChildrenFromAstNode(INode parentNode, IASTNode astNode) {
		for (final IASTNode astChild : astNode.getChildren()) {
			if (astChild instanceof IASTDeclaration) {
				createChildrenFromDeclaration(parentNode, (IASTDeclaration) astChild);
			} else if (astChild instanceof IASTName) {
				final IASTName astName = (IASTName) astChild;
				final IBinding astBinding = astName.resolveBinding();

				final INode childNode = createNode(astBinding, astName, null, UnknownNode.builder());
				if (!(childNode instanceof IIntegral)) {
					parentNode.addDependency(childNode).setType(childNode instanceof IFunction
							? Dependency.Type.INVOCATION : Dependency.Type.USE);
				}
			} else {
				createChildrenFromAstNode(parentNode, astChild);
			}
		}
	}
}

