import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;


/**
 * version 4.2
 * This is the combined file that includes all the classes (for the convenience of submission).
 * db-projection based on SparseList version 4.2. 
 */

/**
 * @author Zhengyang Zhao
 * Oct 22, 2018
 */

public class hcrminer {
	/**
	 * This is the class to generate association rules.
	 * Assuming all the frequent itemSets and their corresponding frequencies are already known.
	 * 		(provided by the class FrequentSetMiner)
	 * Each generated rule will be stored in a instance of Rule class.
	 * 'allRules' is a list of all the Rule instances.
	 */
	
	// Run command line: java hcrminer minsup minconf inputfile outputfile options
	
	public static void main(String args[]) {
		int minSup = Integer.parseInt(args[0]);
		double minConf = Double.parseDouble(args[1]);
		String inputFile = args[2];
		String outputFile = args[3];
		int option = Integer.parseInt(args[4]);
		
//		int minSup = 30;
//		double minConf = 0.6;
//		int option = 2;
//		String inputFile = "in/small";
//		String outputFile = "out/rules_large_minSup_" + minSup + "_minConf_" +  + minConf + "_minConf_" + option;
		
		
		FileLoader.runBatch(inputFile, option);
		
		long t0 = System.currentTimeMillis(); 
		FrequentSetMiner.getFrequentSets(FileLoader.dbReordered, minSup);
		long t1 = System.currentTimeMillis(); 
		System.out.println(inputFile + ": minSup = " + minSup + "; " + "minConf = " + minConf + "; " 
				+ "option = " + option + "; " + "FreqSetMining: " + (t1 - t0)  + " ms; FreqSetNum: " 
				+  FrequentSetMiner.frequentSetsNum);

		FileWriter fw = null;
		PrintWriter pw = null;
		long t2 = System.currentTimeMillis(); 
		RuleGenerator.getFrequencyMap();
		
		if (minSup <= 20 ) {
			try {
				fw = new FileWriter(outputFile, true);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pw = new PrintWriter(fw);
			
			for (ArrayList<Integer> set : FrequentSetMiner.frequentSets) {
				int setFreq = RuleGenerator.frequencyMap.get(set);
				ArrayList<Integer> reorderedSet = new ArrayList<Integer>();
				for (int i = 0; i < set.size(); i++) {
					int orderOfItem = set.get(i);
					int itemID = FileLoader.order[0][orderOfItem];
					reorderedSet.add(itemID);
				}
				RuleGenerator.ruleCount++;
				pw.println(new Rule(reorderedSet, setFreq).getRule());
			}
			close(pw);
			close(fw);
			
		} else {
			try {
				fw = new FileWriter(outputFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
			pw = new PrintWriter(fw);
			
			for (ArrayList<Integer> set : FrequentSetMiner.frequentSets) {
				if (set.size() == 1) {
					continue;
				} else {
					RuleGenerator rg = new RuleGenerator(set, minConf);
					rg.generateRule();
				}
			}
			for (Rule rule : RuleGenerator.allRules) {
				pw.println(rule.getRule());
			}
			close(pw);
			close(fw);
		}
		
		
		long t3 = System.currentTimeMillis(); 
		System.out.println(inputFile + ": minSup = " + minSup + "; " + "minConf = " + minConf + "; " 
				+ "option = " + option + "; " + "FreqSetMining: " + (t1 - t0)  + " ms; FreqSetNum: " 
				+ FrequentSetMiner.frequentSetsNum +  "; RuleGeneration: " + (t3 - t2)  + " ms; RuleNum: "+ 
				+ RuleGenerator.ruleCount );
		
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





class FileLoader {
	/**
	 * This is the very first class in the whole pipeline. 
	 * It's tasks include:
	 * 	1. getDBOrigin(filename) -- Load data from input file
	 * 	2. getFrequencies() -- Get the frequency for each itemID
	 *  3. getNewOrder(option) -- Generate the new order per parameter 'option'. 
	 *  	This method invokes class Pair and class FrequencyComparator.
	 *  4. getDBReordered();  -- Reorder the items and the corresponding frequencies array.
	 */
	
	static int minTransID = Integer.MAX_VALUE;  
	static int maxTransID = 0; 
	static int minItemID = Integer.MAX_VALUE;
	static int maxItemID = 0;   
	static int numRec = 0;    // number of records (rows in the file)
	static int loadingTime;  // time taken for file loading
	
	static int[] frequencies = null;
	static int[] frequenciesReordered = null;
	static int[][] order = null;
	static List<List<Integer>> dbOrigin = null;
	static List<List<Integer>> dbReordered = null;
	
	
	public static void runBatch(String filename, int option) {
		getDBOrigin(filename);
		getFrequencies();
		getNewOrder(option);
		getDBReordered();
	}
	
	
	// load file, and transform data into 2 x n matrix. Also count the frequency of each item.
	public static void getDBOrigin(String filename)  {
		FileReader fr = null;
		BufferedReader br = null;
		dbOrigin = new ArrayList<>();
		List<Integer> list0 = new ArrayList<>();
		List<Integer> list1 = new ArrayList<>();

		long t1 = 0, t2 = 0;
		
		try {
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			String line;
			
			t1 = System.currentTimeMillis();    
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\\s+");
				int tid = Integer.parseInt(pair[0]);  // transaction id
				int iid = Integer.parseInt(pair[1]);  // item id
				list0.add(tid);
				list1.add(iid);
				maxTransID = Math.max(maxTransID, tid);
				minTransID = Math.min(minTransID, tid);
				maxItemID = Math.max(maxItemID, iid);
				minItemID = Math.min(minItemID, iid);
				numRec++;
			}
			dbOrigin.add(list0);
			dbOrigin.add(list1);
			t2 = System.currentTimeMillis(); 
			
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			close(br);
			close(fr);
			loadingTime = (int)(t2 - t1);
		}
	}
	
	public static void getFrequencies() {
		frequencies = new int[maxItemID + 1];
		for (int i = 0; i < dbOrigin.get(0).size(); i++) {
			int itemID = dbOrigin.get(1).get(i);
			frequencies[itemID]++;
		}
	}
	
	public static void getNewOrder(int option) {
		// options = 1: order the items using their itemID;
		// options = 2: sorting the items in increasing frequency order;
		// options = 3: sorting the items in decreasing frequency order;
		// return int[][] order, where:
		// 		order[0][i] stores i-th item's itemID; 
		// 		order[1][i] stores itemID = i 's order; 
		// For example, when option == 1:
		// 		ItemID   = [0,  1,  2,  3,  4,  5,  6,  7,  8,  9, 10]
		// 		Freq     = [0, 10,  3,  5,  4,  4,  6,  8,  5,  5,  6]
		// 		order[0] = [0,  2,  4,  5,  3,  8,  9,  6, 10,  7,  1] --- stores itemID
		// 		order[1] = [0, 10,  1,  4,  2,  3,  7,  9,  5,  6,  8] --- stores order
		// Note: always keep itemID = 0 at the first position.
		
		order = new int[2][maxItemID + 1];
		Pair[] items = new Pair[maxItemID + 1];
		int[] id = new int[maxItemID + 1];
		
		for (int i = 0; i <= maxItemID; i++) {
			id[i] = i;
			Pair item = new Pair(i, frequencies[i]);
			items[i] = item;
		}
		
		if (option == 1) {
			order[0] = id;
			order[1] = id;
			return;
		}
		
		Arrays.sort(items, new FrequencyComparator(option));
		for (int i = 0; i <= maxItemID; i++) {
			int itemID;
			itemID = items[i].itemID;
			order[0][i] = itemID;
			order[1][itemID] = i;
		}
	} 
	
	public static void getDBReordered() {
		
		dbReordered = new ArrayList<>();
		dbReordered.add(dbOrigin.get(0));
		dbReordered.add(new ArrayList<Integer>());
		frequenciesReordered = new int[maxItemID + 1];
		
		for (int i = 0; i < dbOrigin.get(1).size(); i++) {
			int itemID = dbOrigin.get(1).get(i);
			int orderOfItem = order[1][itemID];
			dbReordered.get(1).add(orderOfItem);
			frequenciesReordered[orderOfItem] = frequencies[itemID];
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




class FrequentSetMiner {
	/**
	 * This is the class to find out all the frequent itemSets. 
	 * It invokes the class SparseList and use DFS tree to get all frequent itemSets.
	 * 		Each node of the DFS tree corresponds to a SparseList instance.
	 * 		do projection on each SparseList instance to get the children nodes of current node.
	 */
	
	static ArrayList<ArrayList<Integer>> frequentSets = new ArrayList<>();
	static ArrayList<Integer> frequencies = new ArrayList<>();
	static int frequentSetsNum = 0;
	static int longestSetLength = 0;
	
	static void getFrequentSets(List<List<Integer>> db, int minSup) {
		SparseList originList = new SparseList(db, minSup);
		ArrayList<Integer> freqSet = new ArrayList<>();
		doDFS(originList, minSup, freqSet);
	}
	
	static void doDFS(SparseList fatherList, int minSup, ArrayList<Integer> freqSetFather) {
		if (fatherList.nonZeroCols.size() == 0) {
			return;
		}

		for (int i = 0; i < fatherList.nonZeroCols.size(); i++) {
			int colIndex = fatherList.nonZeroCols.get(i);
			ArrayList<Integer> freqSetSon = new ArrayList<>();
			for (int j = 0; j < freqSetFather.size(); j++) {
				freqSetSon.add(freqSetFather.get(j));
			}
			freqSetSon.add(colIndex);
			int frequency = fatherList.colMap.get(i + 1) - fatherList.colMap.get(i);
			frequentSets.add(freqSetSon);
			frequencies.add(frequency);
			frequentSetsNum++;
			//longestSetLength = Math.max(longestSetLength, freqSetSon.size());
			
			SparseList sonList = new SparseList(fatherList);
			sonList.doProjection(i, minSup);
			doDFS(sonList, minSup, freqSetSon);
		}
	}
}




class RuleGenerator {
	/**
	 * This is the class to generate association rules.
	 * Assuming all the frequent itemSets and their corresponding frequencies are already known.
	 * 		(provided by the class FrequentSetMiner)
	 * Each generated rule will be stored in a instance of Rule class.
	 * 'allRules' is a list of all the Rule instances.
	 */
	
	static HashMap<ArrayList<Integer>, Integer> frequencyMap = new HashMap<>();
	static ArrayList<Rule> allRules = new ArrayList<>();
	static int ruleCount = 0;
	
	ArrayList<Integer> itemSet = null;
	int itemSetFrequency = 0;
	double minConf = 0;
	
	
	public static void getFrequencyMap() {
		for (int i = 0; i < FrequentSetMiner.frequentSets.size(); i++) {
			frequencyMap.put(FrequentSetMiner.frequentSets.get(i), 
					FrequentSetMiner.frequencies.get(i));
		}
	}
	
	
	public RuleGenerator(ArrayList<Integer> itemSet, double minConf) {
		// Important: itemSet.size() has to be larger than 1.
		this.itemSet = itemSet;
		this.itemSetFrequency = frequencyMap.get(itemSet);
		this.minConf = minConf;
	}
	
	
	public void generateRule() {
		
		ArrayList<ArrayList<Integer>> firstLevel = new ArrayList<>();
		for (int i = 0; i < itemSet.size(); i++) {
			ArrayList<Integer> right = new ArrayList<>();
			right.add(itemSet.get(i));
			ArrayList<Integer> leftSet = getLeftSet(right, itemSet);
			int leftSetFrequency = frequencyMap.get(leftSet);
			double conf = (double)itemSetFrequency / (double)leftSetFrequency;
			if (conf < minConf) {
				continue;
			}
			
			ArrayList<Integer> reorderedleftSet = reorderSet(leftSet);
			ArrayList<Integer> reorderedRightSet = reorderSet(right);
			Rule rule = new Rule(reorderedleftSet, reorderedRightSet, itemSetFrequency, leftSetFrequency, conf);
			allRules.add(rule);
			ruleCount++;
			firstLevel.add(right);
		}
		getNextLevel(firstLevel);
	}
	
	
	public void getNextLevel(ArrayList<ArrayList<Integer>> upperLever) {
		
		if (upperLever == null || upperLever.size() == 0) {
			return;
		}
		
		int setLength = upperLever.get(0).size();
		ArrayList<ArrayList<Integer>> lowerLevel = new ArrayList<>();
		
		if (setLength + 1 == itemSet.size()) {
			return;
		}
		
		for (int i = 0; i < upperLever.size() - 1; i++) {
			for (int j = i + 1; j < upperLever.size(); j++) {
				ArrayList<Integer> set1 = upperLever.get(i);
				ArrayList<Integer> set2 = upperLever.get(j);
				ArrayList<Integer> set = new ArrayList<Integer>();
				for (int k = 0; k < setLength - 1; k++) {
					if (set1.get(k) != set2.get(k)) {
						break;
					} else {
						set.add(set1.get(k));
					}
				}
				if (set.size() < setLength - 1) {
					break;
				}
				set.add(set1.get(setLength - 1));
				set.add(set2.get(setLength - 1));
				ArrayList<Integer> leftSet = getLeftSet(set, itemSet);
				int leftSetFrequency = frequencyMap.get(leftSet);
				double conf = (double)itemSetFrequency / (double)leftSetFrequency;
				if (conf < minConf) {
					continue;
				}
				
				ArrayList<Integer> reorderedleftSet = reorderSet(leftSet);
				ArrayList<Integer> reorderedRightSet = reorderSet(set);
				Rule rule = new Rule(reorderedleftSet, reorderedRightSet, itemSetFrequency, leftSetFrequency, conf);
				allRules.add(rule);
				ruleCount++;
				lowerLevel.add(set);
			}
		}
		
		getNextLevel(lowerLevel);
	}
	
	
	public ArrayList<Integer> getLeftSet(ArrayList<Integer> rightSet, ArrayList<Integer> fullSet) {
		ArrayList<Integer> leftSet = new ArrayList<Integer>();
		int i = 0;
		for (int j = 0; j < fullSet.size(); j++) {
			if (i >= rightSet.size() || fullSet.get(j) != rightSet.get(i)) {
				leftSet.add(fullSet.get(j));
				continue;
			}
			i++;
		}
		return leftSet;
	}
	
	public ArrayList<Integer> reorderSet(ArrayList<Integer> set) {
		ArrayList<Integer> reorderedSet = new ArrayList<Integer>();
		for (int i = 0; i < set.size(); i++) {
			int orderOfItem = set.get(i);
			int itemID = FileLoader.order[0][orderOfItem];
			reorderedSet.add(itemID);
		}
		return reorderedSet;
	}
}




class SparseList {
	/**
	 * This is the class to manipulate a sparse matrix dataset (extraction, pruning, etc.)
	 * version 4.2
	 * Using Compressed Sparse Column (CSC) for storing and managing entries;
	 * Using iteration of two ArrayList for searching and pruning entries.
	 *   (version 4.3 uses a HashSet of entries for searching and pruning entries ).
	 */
	
	static int countInstance = 0;
	static int nnz = FileLoader.numRec;  // number of non-zero entries in the matrix
	static int rowNum = FileLoader.maxTransID + 1;
	static int colNum = FileLoader.maxItemID + 1;
	
	ArrayList<Integer> nonZeroCols = new ArrayList<>();
	ArrayList<Integer> colList = new ArrayList<>();  // Store the non-zero entries' rowIndexes using CSC 
	ArrayList<Integer> colMap = new ArrayList<>(); 
	// from colList[colMap[i]] to colList[ colMap[i+1] - 1] are the rowIndexes of entries in Column i.
	
	SparseList(List<List<Integer>> db, int minSup) {
		// db: two-column arrayList listing the cells with value "1".
		// 		col0 lists row indexes of non-zero entries; 
		// 		col1 lists column indexes of non-zero entries.
		
		countInstance++;
		nnz = FileLoader.numRec;  // number of non-zero entries in the matrix
		rowNum = FileLoader.maxTransID + 1;
		colNum = FileLoader.maxItemID + 1;
		
		// Initialize nonZeroCols and colMap:
		HashMap<Integer, Integer> map = new HashMap<>();
		colMap.add(0); 
		for (int i = 0; i < colNum; i++) {
			if (FileLoader.frequenciesReordered[i] < minSup) {
				continue;
			}
			nonZeroCols.add(i);
			map.put(i, colMap.get(colMap.size() - 1));
			colMap.add(colMap.get(colMap.size() - 1) + FileLoader.frequenciesReordered[i]);
		}
		
		// Filling colList:
		int[] fillingCount = new int[colNum];
		for (int i = 0; i < db.get(0).size(); i++) {
			colList.add(null);
		}
		for (int i = 0; i < db.get(0).size(); i++) {
			int rowIndex = db.get(0).get(i);
			int colIndex = db.get(1).get(i);
			if (!map.containsKey(colIndex)) {
				continue;
			}
			int fillingIndex = map.get(colIndex) + fillingCount[colIndex];
			colList.set(fillingIndex, rowIndex);
			fillingCount[colIndex]++;
		}
	}
	
	
	SparseList(SparseList another) {
		countInstance++;
		nonZeroCols = another.nonZeroCols;
		colList = another.colList;
		colMap = another.colMap;
	}
	
	
	public void doProjection(int index, int minSup) {
		// do projection about the Column: nonZeroCols[index].
		if (nonZeroCols.size() == 0) {
			return;
		}
		
		ArrayList<Integer> colListFather = colList;
		ArrayList<Integer> colMapFather = colMap;
		ArrayList<Integer> nonZeroColsFather = nonZeroCols;
		colList = new ArrayList<Integer>();
		colMap = new ArrayList<Integer>();
		nonZeroCols = new ArrayList<Integer>();
		
		if (index == nonZeroColsFather.size() - 1) {
			return;
		}
		
		int writeTo = 0;
		colMap.add(writeTo);
		int refStart = colMapFather.get(index);
		int refTo = colMapFather.get(index + 1);
		for (int i = index + 1; i < nonZeroColsFather.size(); i++) {
			int colStart = colMapFather.get(i);
			int colTo = colMapFather.get(i + 1);
			int count = pruneList(colListFather, refStart, refTo, colStart, colTo, writeTo);
			if (count >= minSup) {
				writeTo += count;
				colMap.add(writeTo);
				nonZeroCols.add(nonZeroColsFather.get(i));
			}
		}
	}
	
	
	public int pruneList(ArrayList<Integer> colListFather, int refStart, int refTo, 
			int colStart, int colTo, int writeTo ) {
		// Extract common elements of originList and refList. Add them into colList.
		// refList: subList of colListFather, from refStart (inclusive) to refTo (exclusive);
		// originList: subList of colListFather, from colStart (inclusive) to colTo (exclusive);
		// Add the common elements to colList, the first element be added at index writeTo.
		// Return the number of common elements.
		
		int i = colStart, j = refStart;
		int count = 0;
		while (i < colTo && j < refTo) {
			if (colListFather.get(i).equals(colListFather.get(j))) {
				colList.add(writeTo, colListFather.get(i));
				i++;
				j++;
				writeTo++;
				count++;
			} else if (colListFather.get(i) < colListFather.get(j)) {
				int increment = 1;
				while (i < colTo && colListFather.get(i) < colListFather.get(j)) {
					i += increment;
					increment = increment * 2;
				}
				i -= increment / 2;
				i++;
			} else {
				int increment = 1;
				while (j < refTo && colListFather.get(j) < colListFather.get(i)) {
					j += increment;
					increment = increment * 2;
				}
				j -= increment / 2;
				j++;
			}
		}
		return count;
	}
}



class Pair {
	Integer itemID;
	Integer freq;
	
	Pair(int itemID, int freq) {
		this.itemID = itemID;
		this.freq = freq;
	}
}




class FrequencyComparator implements Comparator<Pair> {

	int option;
	
	FrequencyComparator(int option) {
		this.option = option;
	}
	
	@Override
	public int compare(Pair o1, Pair o2) {
		
		if (option == 2) {
			// increasing order, with itemID = 0 at head.
			if (o1.itemID == 0) {
				return -1;
			} 
			if (o2.itemID == 0) {
				return 1;
			}
			if (o1.freq == o2.freq) {
				return o1.itemID.compareTo(o2.itemID);
			}
			return o1.freq.compareTo(o2.freq);
			
		} else {
			// decreasing order, with itemID = 0 at head.
			if (o1.itemID == 0) {
				return -1;
			} 
			if (o2.itemID == 0) {
				return 1;
			}
			if (o1.freq == o2.freq) {
				return o2.itemID.compareTo(o1.itemID);
			}
			return o2.freq.compareTo(o1.freq);
		}
	}
}




class Rule {
	/**
	 * This is the class to store association rules.
	 */

	ArrayList<Integer> leftSet = null;
	ArrayList<Integer> rightSet = null;
	int supFullSet = 0;
	int supLeftSet = 0;
	double conf = -1.0;
	String rule = null;
	
	public Rule (ArrayList<Integer> leftSet, ArrayList<Integer> rightSet,
			int supFullSet, int supLeftSet, double conf) {
		this.leftSet = leftSet;
		this.rightSet = rightSet;
		this.supFullSet = supFullSet;
		this.supLeftSet = supLeftSet;
		this.conf = conf;
		// this.conf = (double) this.supFullSet / (double) this.supLeftSet;
	}
	
	public Rule (ArrayList<Integer> leftSet,  int supFullSet) {
		this.leftSet = leftSet;
		this.supFullSet = supFullSet;
	}
	
	public String getRule() {
		
		if (conf == -1.0) {
			// "LHS|{}|SUPPORT|-1"
			String lhs = leftSet.toString();
			String rhs = "{}";
			rule = lhs + " | " + rhs + " | " + supFullSet + " | " + "-1";
		} else {
			// "LHS|RHS|SUPPORT|CONFIDENCE";
			String lhs = leftSet.toString();
			String rhs = rightSet.toString();
			rule = lhs + " | " + rhs + " | " + supFullSet + " | " + conf;
		}
		return rule;
	}
}