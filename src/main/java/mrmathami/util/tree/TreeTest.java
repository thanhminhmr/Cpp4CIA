package mrmathami.util.tree;

import java.util.Random;

public class TreeTest {
	private static final Random RANDOM = new Random();

	public static void main(String[] args) {
		TreeNode<String> tree = new TreeNode<>("Hello");
		generate(0, tree);
		System.out.println(tree);
	}

	private static void generate(int level, TreeNode<String> node) {
		if (level >= 4) return;
		for (int count = 0; count < 8; count++) {
			if (RANDOM.nextInt(8) < count) break;
			TreeNode<String> treeNode = new TreeNode<>(level + "-" + count);
			node.addChild(treeNode);
			generate(level + 1, treeNode);
		}
	}
}
