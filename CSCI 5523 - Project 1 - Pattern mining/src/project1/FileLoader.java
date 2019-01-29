package project1;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This is the very first class in the whole pipeline. 
 * It's tasks include:
 * 	1. getDBOrigin(filename) -- Load data from input file
 * 	2. getFrequencies() -- Get the frequency for each itemID
 *  3. getNewOrder(option) -- Generate the new order per parameter 'option'. 
 *  	This method invokes class Pair and class FrequencyComparator.
 *  4. getDBReordered();  -- Reorder the items and the corresponding frequencies array.
 */

/**
 * @author Zhengyang Zhao
 * Oct 16, 2018
 * 
 */

public class FileLoader {
	
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
	
	public static void main(String[] args) throws FileNotFoundException {
		String filename = "in/toy";
		int option = 2;
		runBatch(filename, option);
		
		System.out.println("maxTransID: " + maxTransID);
		System.out.println("minTransID: " + minTransID);
		System.out.println("maxItemID: " + maxItemID);
		System.out.println("minItemID: " + minItemID);
		System.out.println("numRec: " + numRec);
		System.out.println("loadingTime: " + loadingTime + " ms");
		
		System.out.println("Freq: " + Arrays.toString(frequencies));
		System.out.println("order[0]: " + Arrays.toString(order[0]));
		System.out.println("order[1]: " + Arrays.toString(order[1]));
		
		System.out.println("dbOrigin[0]: " + dbOrigin.get(0));
		System.out.println("dbOrigin[1]: " + dbOrigin.get(1));
		System.out.println("dbReordered[1]: " + dbReordered.get(1));
		
		// To check answers (file = large):
		//  itemID	frequency
		//      1	 45897
		//    100	  2152
		//   1000		82
		//   5000		79
		//  10000		11
		//
		//  minTransID = 0; maxTransID = 63033
		//  minItemID = 1; maxItemID = 15846
	}

}
