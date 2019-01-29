import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Dec 15, 2018
 */

public class DecisionTree {

	DecisionTreeNode root;
	int nodeNum;
	int leafNodeNum;
	int maxLevel;
	double trainingErrorRate;
	
	
	public void writeToFile(String fileName) {
		List<String> treeString = new ArrayList<>();
		Queue<DecisionTreeNode> q = new LinkedList<>();
		q.offer(root);
		
		while (!q.isEmpty()) {
			int size = q.size();
			for (int i = 0; i < size; i++) {
				DecisionTreeNode node = q.poll();
				if (node == null) {
					treeString.add("#");
					continue;
				}
				treeString.add(node.toString());
				q.offer(node.left);
				q.offer(node.right);
			}
		}
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(fileName);
			pw = new PrintWriter(fw);
			for (String line : treeString) {
				pw.println(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(pw);
			close(fw);
		}
	}
	
	
	public void readFromFile(String fileName) {
		Queue<String> q = new LinkedList<>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(fileName);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				q.offer(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		
		int level = 0;
		int sizeUpperLevel = 1;
		Queue<DecisionTreeNode> qFatherNodes = null;
		DecisionTreeNode father = null;
		while (!q.isEmpty()) {
			Queue<DecisionTreeNode> qNodes = new LinkedList<>();
			int size = level == 0 ? 1 : 2 * sizeUpperLevel;
			sizeUpperLevel = size;
			for (int i = 0; i < size; i++) {
				if (level > 0 && i % 2 == 0) {
					father = qFatherNodes.poll();
				}
				String nodeString = q.poll();
				if (nodeString.equals("#")) {
					sizeUpperLevel--;
					continue;
				}
				DecisionTreeNode node = new DecisionTreeNode(nodeString);
				node.nodeLevel = level;
				qNodes.offer(node);
				node.father = father;
				if (level == 0) {
					this.root = node;
					continue;
				}
				if (i % 2 == 0) {
					father.left = node;
				} else {
					father.right = node;
				}
			}
			qFatherNodes = qNodes;
			level++;
		}
	}
	
	
	public static void close(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
