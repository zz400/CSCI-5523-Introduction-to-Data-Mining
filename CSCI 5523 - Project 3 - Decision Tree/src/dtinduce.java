import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;


/**
 * @author Zhengyang Zhao
 * Dec 13, 2018
 */

public class dtinduce {

	int minFreq;
	String trainFile;
	String modelFile;
	
	int attrNum = 0;
	List<Integer> dbIDs = new ArrayList<>();
	List<Integer> dbLabels = new ArrayList<>();
	List<List<Double>> db = new ArrayList<>();  // for double-attr data set only.
	
	List<HashMap<Integer, Integer>> dbAttrMap = new ArrayList<>();  // for int-attr data set only.
		// dbAttrMap.get(k): for the k'th data point, map of attrIndex -> attrValue
	List<int[]> dbAttrZeroInClasses = new ArrayList<>();  // for int-attr data set only.
		// dbAttrZeroInClasses.get(k): for the k'th attribute, 0-value data numbers with in each class.
	List<Integer> dbAttrZeroNum = new ArrayList<>();  // for int-attr data set only.
	
	static int treeIndex;
	
	
	public dtinduce(int minFreq, String trainFile, String modelFile) {
		this.minFreq = minFreq;
		this.trainFile = trainFile;
		this.modelFile = modelFile;
	}
	
	
	public static void main(String[] args) {
		/*
		int minFreq = 20;
		String trainFile = "data/rep2/train.csv";
		String modelFile = "/Users/zhao/Desktop/code3/modelfile.txt";
		*/
		int minFreq = Integer.parseInt(args[1]);
		String trainFile = args[0];
		String modelFile = args[2];
		
		boolean printTreeDetails = true;
		dtinduce dtTrainer = new dtinduce(minFreq, trainFile, modelFile);
		dtTrainer.train(printTreeDetails);
	}
	
	
	public void train(boolean printTreeDetails) {
		long t1 = System.currentTimeMillis(); 
		boolean isAttrDouble = isDataAttrDouble(trainFile);
		if (isAttrDouble) {
			loadDataDouble(trainFile);
		} else {
			loadDataInt(trainFile);
		}
		
		System.out.println("\n********************************************");
		System.out.println("[-----Training decision tree "
				+ String.format("%03d", treeIndex) + " -----]");
		System.out.println("TrainingFile: " + trainFile + ";  "
				+ "AttributesNumber: " + attrNum + ";  minFreq = " + minFreq);
		
		DecisionTree dt = induceDecisonTree(minFreq, isAttrDouble, printTreeDetails);
		
		System.out.println("TrainingErrorRate: " + dt.trainingErrorRate);
		System.out.println("TreeHeight: " + dt.maxLevel);
		System.out.println("TotalNodeNum: " + dt.nodeNum);
		System.out.println("LeafNodeNum: " + dt.leafNodeNum);
		
		long t2 = System.currentTimeMillis(); 
		int trainingRunTime  = (int)(t2 - t1) / 1000;
		System.out.println("TrainingRunTime: " + trainingRunTime + " s");
		
		dt.writeToFile(modelFile);
	}
	
	
	public boolean isDataAttrDouble(String trainFile) {
		boolean ans = false;
		FileReader fr  = null;
		BufferedReader br  = null;
		try {
			fr = new FileReader(trainFile);
			br = new BufferedReader(fr);
			String line = br.readLine();
			String[] words = line.split(",");
			attrNum = words.length - 1;
			if (line.contains(".")) {
				ans = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		return ans;
	}
	
	
	public void loadDataDouble(String trainFile) {
		FileReader fr  = null;
		BufferedReader br  = null;
		String line;
		try {
			fr = new FileReader(trainFile);
			br = new BufferedReader(fr);
			int id = 0;
			while ((line = br.readLine()) != null) {
				dbIDs.add(id++);
				String[] words = line.split(",");
				int label = Integer.parseInt(words[0]);
				dbLabels.add(label);
				List<Double> record = new ArrayList<>();
				for (int i = 1; i < words.length; i++) {
					Double num = Double.parseDouble(words[i]);
					record.add(num);
				}
				db.add(record);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
	}
	
	
	public void loadDataInt(String trainFile) {
		for (int i = 0; i < attrNum; i++) {
			//Set<Integer> zeroSet = new HashSet<>();
			//dbAttrZero.add(zeroSet);
			int[] zeroInClasses = new int[10];
			dbAttrZeroInClasses.add(zeroInClasses);
		}
		
		FileReader fr  = null;
		BufferedReader br  = null;
		String line;
		try {
			fr = new FileReader(trainFile);
			br = new BufferedReader(fr);
			int id = 0;
			while ((line = br.readLine()) != null) {
				HashMap<Integer, Integer> attrMap = new HashMap<>();
				String[] words = line.split(",");
				int label = Integer.parseInt(words[0]);
				dbLabels.add(label);
				for (int i = 1; i < words.length; i++) {
					int num = Integer.parseInt(words[i]);
					if (num != 0) {
						attrMap.put(i - 1, num);
					} 
				}
				dbAttrMap.add(attrMap);
				dbIDs.add(id++);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		
		for (int i = 0; i < attrNum; i++) {
			//Set<Integer> zeroSet = dbAttrZero.get(i);
			int[] zeroInClasses = dbAttrZeroInClasses.get(i);
			int zeroNum = 0;
			for (int j = 0; j < dbIDs.size(); j++) {
				if (dbAttrMap.get(j).containsKey(i)) {
					continue;
				}
				//zeroSet.add(j);
				zeroNum++;
				int label = dbLabels.get(j);
				zeroInClasses[label] = zeroInClasses[label] + 1;
			}
			dbAttrZeroNum.add(zeroNum);
		}
	}
	
	
	public DecisionTree induceDecisonTree(int minFreq, boolean isAttrDouble, 
			boolean printTreeDetails) {
		DecisionTree dt = new DecisionTree();
		DecisionTreeNode root = generateRootNode(minFreq, isAttrDouble);
		
		int nodeNum = 0;
		int leafNodeNum = 0;
		int maxLevel = -1;
		int errorRecs = 0;
		double errorRate = 0;
		
		Queue<DecisionTreeNode> q = new LinkedList<>();
		q.offer(root);
		while (!q.isEmpty()) {
			int size = q.size();
			nodeNum += size;
			maxLevel++;
			int leafNodeNumCurrLevel = 0;
			for (int i = 0; i < size; i++) {
				DecisionTreeNode node = q.poll();
				//System.out.println("recNum in Node: " + node.recNum);
				if (node.isLeafNode) {
					leafNodeNumCurrLevel++;
					leafNodeNum++;
					errorRecs += node.errorRecs;
					continue;
				}
				if (isAttrDouble) {
					generateChildrenNodeAttrDouble(node, minFreq);
				} else {
					generateChildrenNodeAttrInt(node, minFreq);
				}
				
				q.offer(node.left);
				q.offer(node.right);
			}
			if (printTreeDetails) {
				System.out.println("Level " + maxLevel + ": " + " TotalNodeNum = " + size 
					+ "; LeafNodeNum = " + leafNodeNumCurrLevel);
			}
		}
		errorRate = (double)errorRecs / (double)dbIDs.size();
		
		dt.root = root;
		dt.nodeNum = nodeNum;
		dt.leafNodeNum = leafNodeNum;
		dt.maxLevel = maxLevel;
		dt.trainingErrorRate = errorRate;
		return dt;
	}
	
	
	public DecisionTreeNode generateRootNode(int minFreq, boolean isAttrDouble) {
		int recNum = dbLabels.size();
		int[] recsInClasses = new int[10];
		Set<Integer> setRec = new HashSet<>();
		for (int i = 0; i < recNum; i++) {
			setRec.add(i);
			int label = dbLabels.get(i);
			recsInClasses[label] = recsInClasses[label] + 1;
		}
		double gini = 1;
		for (int label = 0; label < 10; label++) {
			double p = (double)recsInClasses[label] / (double)recNum;
			gini -= p * p;
		}
		Set<Integer> attrBeenUsed = new HashSet<>();
		DecisionTreeNode root = new DecisionTreeNode(setRec, recsInClasses, gini,
				attrBeenUsed, 0, minFreq, attrNum);
		root.zeroAttrs = new HashSet<>();
		if (!isAttrDouble) {
			for (int attr = 0; attr < attrNum; attr++) {
				if (dbAttrZeroNum.get(attr) == dbIDs.size()) {
					root.zeroAttrs.add(attr);
				}
			}
		}
		return root;
	}
	
	
	public void generateChildrenNodeAttrInt(DecisionTreeNode father, int minFreq) {
		if (father.isLeafNode) {
			return;
		}
		
		int splitAttr = 0;
		double splitValue = 0;
		double leftMinGini = 1;
		double rightMinGini = 1;
		double minGini = 1;
		Set<Integer> leftRecIDs = null;
		Set<Integer> rightRecIDs = null;
		int[] leftRecsInClasses = null;
		int[] rightRecsInClasses = null;
		
		Set<Integer> attrBeenUsed = new HashSet<>();
		Set<Integer> leftZeroAttrs = new HashSet<>();
		Set<Integer> rightZeroAttrs = new HashSet<>();
		
		List<PairDouble> pairListChosen = null;
		Set<Integer> zeroSetChosen = null;
		int[] leftClassSumChosen = null;
		int[] rightClassSumChosen = null;
		for (int attr = 0; attr < attrNum; attr++) {
			if (father.zeroAttrs.contains(attr)) {
				continue; 
			}
			
			Set<Integer> zeroSet = new HashSet<>();
			int[] leftClassSum = new int[10];
			int[] rightClassSum = new int[10];
			int leftNum = 0, rightNum = father.recNum;
			double leftGini = 1, rightGini = 1;
			boolean giniBeenUpdated = false;
			List<PairDouble> pairList = new ArrayList<>();
			for (Integer recID : father.recIDs) {
				int label = dbLabels.get(recID);
				if (dbAttrMap.get(recID).containsKey(attr)) {
					int attrValue = dbAttrMap.get(recID).get(attr);
					PairDouble pair = new PairDouble(recID, (double)attrValue);
					pairList.add(pair);
				} else {
					zeroSet.add(recID);
					leftClassSum[label] = leftClassSum[label] + 1;
					leftNum++;
				}
			}
			
			if (pairList.size() == 0 || (leftNum == 0 && 
					pairList.get(0).attrValue == pairList.get(pairList.size() - 1).attrValue) ) {
				father.zeroAttrs.add(attr);
				continue;  //////////88888888
			}
			PairDoubleComparator comparator = new PairDoubleComparator();
			Collections.sort(pairList, comparator);
			
			rightNum = father.recNum - leftNum;
			int[] leftClassSumBuffer = new int[10];
			int[] rightClassSumBuffer = new int[10];
			for (int i = 0; i < 10; i++) {
				rightClassSum[i] = father.recsInClasses[i] - leftClassSum[i];
				leftClassSumBuffer[i] = leftClassSum[i];
				rightClassSumBuffer[i] = rightClassSum[i];
				double pl = (double)leftClassSum[i] / (double)leftNum;
				double pr = (double)rightClassSum[i] / (double)rightNum;
				leftGini -= pl * pl;
				rightGini -= pr * pr;
			}
			double giniWeighted = leftGini * (double)leftNum / (double)father.recNum 
					+ rightGini * (double)rightNum / (double)father.recNum;
			if (giniWeighted < minGini) {
				giniBeenUpdated = true;
				splitAttr = attr;
				splitValue = 0;
				minGini = giniWeighted;
				leftMinGini = leftGini;
				rightMinGini = rightGini;
			}
			
			for (int i = 0; i < pairList.size(); i++) {
				int recID = pairList.get(i).recID;
				int recLabel = dbLabels.get(recID);
				double attrValue = pairList.get(i).attrValue;
				leftClassSum[recLabel] = leftClassSum[recLabel] + 1;
				rightClassSum[recLabel] = rightClassSum[recLabel] - 1;
				leftNum++;
				rightNum--;
				
				// calculate Gini at current split:
				if (i == pairList.size() - 1 || attrValue != pairList.get(i + 1).attrValue) {
					leftGini = 1;
					rightGini = 1;
					for (int j = 0; j < 10; j++) {
						double pl = (double)leftClassSum[j] / (double)leftNum;
						double pr = (double)rightClassSum[j] / (double)rightNum;
						leftGini -= pl * pl;
						rightGini -= pr * pr;
					}
					giniWeighted = leftGini * (double)leftNum / (double)father.recNum 
										+ rightGini * (double)rightNum / (double)father.recNum;
					if (giniWeighted < minGini) {
						giniBeenUpdated = true;
						splitAttr = attr;
						splitValue = attrValue;
						minGini = giniWeighted;
						leftMinGini = leftGini;
						rightMinGini = rightGini;
					}
				}
			}
			
			if (giniBeenUpdated) {
				pairListChosen = pairList;
				zeroSetChosen = zeroSet;
				leftClassSumChosen = leftClassSumBuffer;
				rightClassSumChosen = rightClassSumBuffer;
			}
		}
		
		leftRecIDs = new HashSet<>();
		rightRecIDs = new HashSet<>();
		leftRecsInClasses = leftClassSumChosen;
		rightRecsInClasses = rightClassSumChosen;
		leftRecIDs.addAll(zeroSetChosen);
		for (int i = 0; !zeroSetChosen.contains(i) && pairListChosen.get(i).attrValue <= splitValue; i++) {
			int recID = pairListChosen.get(i).recID;
			leftRecIDs.add(recID);
			int recLabel = dbLabels.get(recID);
			leftRecsInClasses[recLabel] = leftRecsInClasses[recLabel] + 1;
			rightRecsInClasses[recLabel] = rightRecsInClasses[recLabel] - 1;
		}
		for (int recID : father.recIDs) {
			if (!leftRecIDs.contains(recID)) {
				rightRecIDs.add(recID);
			}
		}
		
		leftZeroAttrs.addAll(father.zeroAttrs);
		rightZeroAttrs.addAll(father.zeroAttrs);
		attrBeenUsed.addAll(father.attrBeenUsed);
		attrBeenUsed.add(splitAttr);
		int nodeLevel = father.nodeLevel + 1;
		DecisionTreeNode leftNode = new DecisionTreeNode(leftRecIDs, leftRecsInClasses, 
				leftMinGini, attrBeenUsed, nodeLevel, minFreq, attrNum);
		DecisionTreeNode rightNode = new DecisionTreeNode(rightRecIDs, rightRecsInClasses, 
				rightMinGini, attrBeenUsed, nodeLevel, minFreq, attrNum);
		leftNode.zeroAttrs = leftZeroAttrs;
		rightNode.zeroAttrs = rightZeroAttrs;
		leftNode.father = father;
		rightNode.father = father;
		father.left = leftNode;
		father.right = rightNode;
		father.splitAttr = splitAttr;
		father.splitValue = splitValue;
	}
	
	
	public void generateChildrenNodeAttrDouble(DecisionTreeNode father, int minFreq) {
		if (father.isLeafNode) {
			return;
		}
		
		int splitAttr = 0;
		double splitValue = 0;
		double leftMinGini = 1;
		double rightMinGini = 1;
		double minGini = 1;
		Set<Integer> leftRecIDs = null;
		Set<Integer> rightRecIDs = null;
		int[] leftRecsInClasses = null;
		int[] rightRecsInClasses = null;
		Set<Integer> attrBeenUsed = new HashSet<>();
		attrBeenUsed.addAll(father.attrBeenUsed);
		
		List<PairDouble> pairListChosen = null;
		for (int attr = 0; attr < attrNum; attr++) {
			List<PairDouble> pairList = new ArrayList<>();
			for (Integer recID : father.recIDs) {
				double attrValue = db.get(recID).get(attr);
				PairDouble pair = new PairDouble(recID, attrValue);
				pairList.add(pair);
			}
			PairDoubleComparator comparator = new PairDoubleComparator();
			Collections.sort(pairList, comparator);
			int[] leftClassSum = new int[10];
			int[] rightClassSum = new int[10];
			for (int i = 0; i < 10; i++) {
				rightClassSum[i] = father.recsInClasses[i];
			}
			int leftNum = 0, rightNum = father.recNum;
			boolean giniBeenUpdated = false;
			
			for (int i = 0; i < pairList.size(); i++) {
				int recID = pairList.get(i).recID;
				int recLabel = dbLabels.get(recID);
				double attrValue = pairList.get(i).attrValue;
				leftClassSum[recLabel] = leftClassSum[recLabel] + 1;
				rightClassSum[recLabel] = rightClassSum[recLabel] - 1;
				leftNum++;
				rightNum--;
				
				// calculate Gini at current split:
				if (i == pairList.size() - 1 || attrValue != pairList.get(i + 1).attrValue) {
					double leftGini = 1, rightGini = 1;
					for (int j = 0; j < 10; j++) {
						double pl = (double)leftClassSum[j] / (double)leftNum;
						double pr = (double)rightClassSum[j] / (double)rightNum;
						leftGini -= pl * pl;
						rightGini -= pr * pr;
					}
					double giniWeighted = leftGini * (double)leftNum / (double)father.recNum 
										+ rightGini * (double)rightNum / (double)father.recNum;
					if (giniWeighted < minGini) {
						giniBeenUpdated = true;
						splitAttr = attr;
						splitValue = attrValue;
						minGini = giniWeighted;
						leftMinGini = leftGini;
						rightMinGini = rightGini;
					}
				}
			}
			if (giniBeenUpdated) {
				pairListChosen = pairList;
			}
		}
		
		leftRecIDs = new HashSet<>();
		rightRecIDs = new HashSet<>();
		leftRecsInClasses = new int[10];
		rightRecsInClasses = new int[10];
		for (int i = 0; i < 10; i++) {
			rightRecsInClasses[i] = father.recsInClasses[i];
		}
		for (int i = 0; pairListChosen.get(i).attrValue <= splitValue; i++) {
			int recID = pairListChosen.get(i).recID;
			leftRecIDs.add(recID);
			int recLabel = dbLabels.get(recID);
			leftRecsInClasses[recLabel] = leftRecsInClasses[recLabel] + 1;
			rightRecsInClasses[recLabel] = rightRecsInClasses[recLabel] - 1;
		}
		for (int recID : father.recIDs) {
			if (!leftRecIDs.contains(recID)) {
				rightRecIDs.add(recID);
			}
		}
		
		attrBeenUsed.add(splitAttr);
		int nodeLevel = father.nodeLevel + 1;
		DecisionTreeNode leftNode = new DecisionTreeNode(leftRecIDs, leftRecsInClasses, 
				leftMinGini, attrBeenUsed, nodeLevel, minFreq, attrNum);
		DecisionTreeNode rightNode = new DecisionTreeNode(rightRecIDs, rightRecsInClasses, 
				rightMinGini, attrBeenUsed, nodeLevel, minFreq, attrNum);
		leftNode.father = father;
		rightNode.father = father;
		father.left = leftNode;
		father.right = rightNode;
		father.splitAttr = splitAttr;
		father.splitValue = splitValue;
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