//package cia.cpp;
//
//import org.eclipse.cdt.core.dom.astbuilder.IASTNode;
//import org.eclipse.cdt.core.dom.astbuilder.IASTTranslationUnit;
//
//import java.util.function.Function;
//
//public abstract class EclipseAstTreeWalker {
//	public static void walk(IASTTranslationUnit translationUnit, Function<IASTNode, Boolean> nodePredicate) {
//		new EclipseAstTreeWalker() {
//			@Override
//			protected boolean onNode(IASTNode node) {
//				return nodePredicate.apply(node);
//			}
//		}.walk(translationUnit);
//	}
//
//	protected abstract boolean onNode(IASTNode node);
//
//	public void walk(IASTTranslationUnit translationUnit) {
//		walkRecursive(translationUnit);
//	}
//
//	private void walkRecursive(IASTNode node) {
//		if (onNode(node)) {
//			final IASTNode[] children = node.getChildren();
//			if (children == null) return;
//			for (IASTNode child : children) {
//				walkRecursive(child);
//			}
//		}
//	}
//}
