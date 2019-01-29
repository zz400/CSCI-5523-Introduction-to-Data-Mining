package project1;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the class to find out all the frequent itemSets. 
 * It invokes the class SparseList and use DFS tree to get all frequent itemSets.
 * 		Each node of the DFS tree corresponds to a SparseList instance.
 * 		do projection on each SparseList instance to get the children nodes of current node.
 */

/**
 * @author Zhengyang Zhao
 * Oct 18, 2018
 */

public class FrequentSetMiner {
	
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
	
	public static void main(String[] args) {
		int option = 2;
		int[] minSupList = {500};
		String filename = "in/large";

		for (int i = 0; i < minSupList.length; i++) {
			FileLoader.runBatch(filename, option);
			long t0 = System.currentTimeMillis(); 
			frequentSetsNum = 0;
			getFrequentSets(FileLoader.dbReordered, minSupList[i]);
			long t1 = System.currentTimeMillis(); 
			System.out.println("minSup = " + minSupList[i] + ": " + (t1 - t0)  + " ms; " + frequentSetsNum  + " frequentSetsNum");
			//System.out.println("frequentSets: " + frequentSets);
			//System.out.println("frequencies: " + frequencies);
			//System.out.println("longestSetLength: " + longestSetLength);
		
		}
	}

}
