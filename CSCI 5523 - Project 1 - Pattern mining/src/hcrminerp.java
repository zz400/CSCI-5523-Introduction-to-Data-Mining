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

public class hcrminerp {
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

	}
	
}





