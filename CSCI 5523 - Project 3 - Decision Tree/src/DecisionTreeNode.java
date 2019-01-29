/**
 * 
 */


import java.util.Set;

/**
 * @author Zhengyang Zhao
 * Dec 14, 2018
 */

public class DecisionTreeNode {
	// variables provided by training constructor:
	Set<Integer> recIDs;
	int recNum;
	int[] recsInClasses;
	int majorClassLabel;
	int nonEmptyClasses;
	double gini;
	double errorRate;
	int errorRecs;
	int nodeLevel;
	
	boolean isLeafNode;
	boolean isPureNode;
	Set<Integer> attrBeenUsed;
	
	// variables not provided by training constructor:
	DecisionTreeNode left;
	DecisionTreeNode right;
	DecisionTreeNode father;
	int splitAttr = -1;
	double splitValue;
	Set<Integer> zeroAttrs;

	
	// Constructor for training:
	public DecisionTreeNode(Set<Integer> recIDs, int[] recsInClasses, double gini,
			Set<Integer> attrBeenUsed, int nodeLevel, int minFreq, int attrNum) {
		this.recIDs = recIDs;
		this.recNum = recIDs.size();
		this.recsInClasses = recsInClasses;
		this.gini = gini;
		this.attrBeenUsed = attrBeenUsed;
		this.nodeLevel = nodeLevel;
		
		int maxRecInClasses = 0;
		for (int i = 0; i < 10; i++) {
			if (recsInClasses[i] != 0) {
				nonEmptyClasses++;
			}
			if (maxRecInClasses < recsInClasses[i]) {
				maxRecInClasses = recsInClasses[i];
				majorClassLabel = i;
			}
		}
		errorRecs = recNum - recsInClasses[majorClassLabel];
		errorRate = (double)errorRecs / (double)recNum;
		
		if (nonEmptyClasses == 1) {
			isPureNode = true;
			isLeafNode = true;
		}
		if (recNum < minFreq) {
			isLeafNode = true;
		}
		if (attrBeenUsed.size() == attrNum) {
			isLeafNode = true;
		}
	}
	
	
	// Constructor for testing (build nodes from treeFile):
	public DecisionTreeNode(String s) {
		String[] words = s.split(",");
		int splitAttr = Integer.parseInt(words[0]);
		double splitValue = Double.parseDouble(words[1]);
		int majorClassLabel = Integer.parseInt(words[2]);
		boolean isLeafNode = false;
		if (splitAttr == -1) {
			isLeafNode = true;
		}
		this.splitAttr = splitAttr;
		this.splitValue = splitValue;
		this.majorClassLabel = majorClassLabel;
		this.isLeafNode = isLeafNode;
	}
	
	
	public String toString() {
		String s = splitAttr + "," + splitValue + "," + majorClassLabel;
		return s;
	}
}
