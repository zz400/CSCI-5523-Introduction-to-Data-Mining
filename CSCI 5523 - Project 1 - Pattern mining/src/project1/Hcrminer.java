package project1;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * This is the main class.
 * The main function of Hcrminer class will invoke multiple other classes to give a final output of association rules.
 * 		Classes that are invoked: FileLoader, FrequentSetMiner, RuleGenerator.
 * All the association rules will be written into an output file.
 */

/**
 * @author Zhengyang Zhao
 * Oct 21, 2018
 */

public class Hcrminer {

	// Run command line: java hcrminer minsup minconf inputfile outputfile options
	
	public static void main(String args[]) {
//		int minSup = Integer.parseInt(args[0]);
//		double minConf = Double.parseDouble(args[1]);
//		String inputFile = args[2];
//		String outputFile = args[3];
//		int option = Integer.parseInt(args[4]);
		
		int minSup = 20;
		double minConf = 0.8;
		int option = 3;
		String inputFile = "in/small";
		String outputFile = "out/rules_large_minSup_" + minSup + "_minConf_" +  + minConf + "_minConf_" + option;
		
		
		FileLoader.runBatch(inputFile, option);
		
		long t0 = System.currentTimeMillis(); 
		FrequentSetMiner.getFrequentSets(FileLoader.dbReordered, minSup);
		long t1 = System.currentTimeMillis(); 
		//System.out.println(inputFile + ": minSup = " + minSup + "; " + "minConf = " + minConf + "; " 
		//		+ "option = " + option + "; " + "FreqSetMining: " + (t1 - t0)  + " ms; "
		//		+ "FreqSetNum: " +  FrequentSetMiner.frequentSetsNum );

		long t2 = System.currentTimeMillis(); 
		RuleGenerator.getFrequencyMap();
		for (ArrayList<Integer> set : FrequentSetMiner.frequentSets) {
			if (minSup <= 20 ) {
				int setFreq = RuleGenerator.frequencyMap.get(set);
				ArrayList<Integer> reorderedSet = new ArrayList<Integer>();
				for (int i = 0; i < set.size(); i++) {
					int orderOfItem = set.get(i);
					int itemID = FileLoader.order[0][orderOfItem];
					reorderedSet.add(itemID);
				}
				Rule rule = new Rule(reorderedSet, setFreq);
				RuleGenerator.allRules.add(rule);
			} else {
				if (set.size() == 1) {
					continue;
				} else {
					RuleGenerator rg = new RuleGenerator(set, minConf);
					rg.generateRule();
				}
			}
		}
		long t3 = System.currentTimeMillis(); 
		
		System.out.println(inputFile + ": minSup = " + minSup + "; " + "minConf = " + minConf + "; " 
				+ "option = " + option + "; " + "FreqSetMining: " + (t1 - t0)  + " ms; "
				+ "RuleGeneration: " + (t3 - t2)  + " ms; "+ "FreqSetNum: " 
				+  FrequentSetMiner.frequentSetsNum + "; RuleNum: " 
				+ RuleGenerator.allRules.size() );
		
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(outputFile);
			pw = new PrintWriter(fw);
			for (Rule rule : RuleGenerator.allRules) {
				pw.println(rule.getRule());
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
