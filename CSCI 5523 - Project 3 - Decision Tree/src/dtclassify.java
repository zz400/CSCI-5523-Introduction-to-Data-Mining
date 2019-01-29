import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhengyang Zhao
 * Dec 15, 2018
 */

public class dtclassify {
	
	String modelFile;
	String testFile;
	String predictionFile;
	static int classifierIndex;
	
	
	public dtclassify(String modelFile, String testFile, String predictionFile) {
		this.modelFile = modelFile;
		this.testFile = testFile;
		this.predictionFile = predictionFile;
	}
	

	public static void main(String[] args) {

		/*
		String modelFile = "treeFile";
		String testFile = "data/rep2/test.csv";;
		String predictionFile = "predictionFile";
		*/
		String modelFile = args[0];
		String testFile = args[1];
		String predictionFile = args[2];
		
		dtclassify dtClassifier = new dtclassify(modelFile, testFile, predictionFile);
		dtClassifier.classify();
	}
	
	
	public void classify(){
		
		List<Integer> trueLabels = new ArrayList<>();
		List<Integer> predLabels = new ArrayList<>();
		List<List<Double>> testDB = new ArrayList<>();
		
		// Build the decision tree from modelFile:
		DecisionTree dt = new DecisionTree();
		dt.readFromFile(modelFile);

		// Read testFile:
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(testFile);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split(",");
				int label = Integer.parseInt(words[0]);
				trueLabels.add(label);
				List<Double> list = new ArrayList<>();
				for (int i = 1; i < words.length; i++) {
					double attrValue = Double.parseDouble(words[i]);
					list.add(attrValue);
				}
				testDB.add(list);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		
		// Testing:
		for (List<Double> list : testDB) {
			DecisionTreeNode node = dt.root;
			while (!node.isLeafNode) {
				int splitAttr = node.splitAttr;
				double splitValue = node.splitValue;
				if (list.get(splitAttr) <= splitValue) {
					node = node.left;
				} else {
					node = node.right;
				}
			}
			predLabels.add(node.majorClassLabel);
		}
		
		int error = 0;
		for (int i = 0; i < trueLabels.size(); i++) {
			if (trueLabels.get(i) != predLabels.get(i)) {
				error++;
			}
		}
		double errorRate = (double)error / (double)trueLabels.size();
		System.out.println("\n[-----Testing "
				+ String.format("%03d", classifierIndex) + " -----]");
		System.out.println("TesingErrorRate: " + errorRate);
		
		// Write prediction file:
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(predictionFile);
			pw = new PrintWriter(fw);
			for (int i = 0; i < trueLabels.size(); i++) {
				String line = "";
				line += trueLabels.get(i);
				line += ",";
				line += predLabels.get(i);
				pw.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(pw);
			close(fw);
		}
	}
	
	
	public static void close(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
