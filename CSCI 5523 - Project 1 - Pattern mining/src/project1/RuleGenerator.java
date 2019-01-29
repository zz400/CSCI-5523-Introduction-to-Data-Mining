package project1;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is the class to generate association rules.
 * Assuming all the frequent itemSets and their corresponding frequencies are already known.
 * 		(provided by the class FrequentSetMiner)
 * Each generated rule will be stored in a instance of Rule class.
 * 'allRules' is a list of all the Rule instances.
 */

/**
 * @author Zhengyang Zhao
 * Oct 21, 2018
 */

public class RuleGenerator {
	
	static HashMap<ArrayList<Integer>, Integer> frequencyMap = new HashMap<>();
	static ArrayList<Rule> allRules = new ArrayList<>();
	
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
	
	
	public static void test(String[] args) {
		String filename = "in/small";
		int option = 1;
		int minSup = 5;
		double minConf = 0.4;
		FileLoader.runBatch(filename, option);
		
		FrequentSetMiner.getFrequentSets(FileLoader.dbReordered, minSup);
		
		int index = 5;
		ArrayList<Integer> itemSet = FrequentSetMiner.frequentSets.get(index);
		getFrequencyMap();
		if (itemSet.size() == 1) {
			System.out.println("No rule.");
		} else {
			RuleGenerator rg = new RuleGenerator(itemSet, minConf);
			rg.generateRule();
			System.out.println("ruleNumber: " + allRules.size());
			for (int i = 0; i < allRules.size(); i++) {
				System.out.println(allRules.get(i).getRule());
			}
		}
	}

}
