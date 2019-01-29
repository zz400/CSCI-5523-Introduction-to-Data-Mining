package project1;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Oct 16, 2018
 */

public class SparseMatrixGenerator {
	
	static final Integer Zero = 0;
	static final Integer One = 1;
	static int generationTime = 0;  // time taken for sparse matrix generation
	static int[] orderItemID = null;  // orderItemID[i]: the itemID for the i-th column in the sparse matrix
	static HashMap<Integer, Integer> itemID2Col = new HashMap<>();   // itemID --> column number
	static HashMap<Integer, Integer> itemID2Freq = new HashMap<>();  // itemID --> frequency
	
	public static void getFrequency(List<List<Integer>> dbOrigin) {
		// run FileLoader.getDBOrigin() first to initialize minItemID and maxItemID.
		
		orderItemID = new int[FileLoader.maxItemID + 1];
		orderItemID[0] = 0;
		itemID2Col.put(0, 0);
		for (int i = 1; i < orderItemID.length; i++) {
			orderItemID[i] = i;
			itemID2Col.put(i, i);
		}
		
		itemID2Freq.put(0, 0);
		for (int i = 0; i < dbOrigin.get(1).size(); i++) {
			int itemID = dbOrigin.get(1).get(i);
			itemID2Freq.put(itemID, itemID2Freq.getOrDefault(itemID, 0) + 1);
		}
	}
	
	public static List<List<Integer>> getDBMatrix(List<List<Integer>> dbOrigin) {
		// 1. run FileLoader.getDBOrigin() first to initialize minTransID and maxTransID, minItemID and maxItemID.
		// 2. run getFrequency() to initialize the HashMap itemID2Col
		
		long t0 = System.currentTimeMillis();
		
		int colNum = FileLoader.maxItemID + 1;
		List<List<Integer>> matrix = new ArrayList<>();
		for (int i = 0; i < colNum; i++) {
			List<Integer> colList = new ArrayList<Integer>();
			for (int j = 0; j <= FileLoader.maxTransID; j++) {
				colList.add(Zero);
			}
			matrix.add(colList);
		}
		
		for (int i = 0; i < dbOrigin.get(0).size(); i++) {
			int transID = dbOrigin.get(0).get(i);
			int itemID = dbOrigin.get(1).get(i);
			int col = itemID2Col.get(itemID);
			matrix.get(col).set(transID, 1);
		}
		
		long t1 = System.currentTimeMillis();
		generationTime = (int) (t1 - t0);
		return matrix;
	}
	
	public static void main(String[] args) {
		
		String filename = "src/toy";
		List<List<Integer>> dbOrigin = null;
		getFrequency(dbOrigin);
		
		System.out.println("orderItemID: " + orderItemID.length);
		System.out.println("itemID2Freq: " + itemID2Freq.size());
		
		List<List<Integer>> dbMatrix = getDBMatrix(dbOrigin);
		for (int itemID = 0; itemID <= FileLoader.maxItemID; itemID++) {
			int col = itemID2Col.get(itemID);
			int count = 0;
			List<Integer> list = dbMatrix.get(col);
			for (int i : list) {
				count += i;
			}
			System.out.println("itemID = " + itemID + ": " + count);
		}
		System.out.println(generationTime + " ms");
		
		for (int transID = 0; transID <= FileLoader.maxTransID; transID++) {
			System.out.print("\n" + transID + ":\t");
			for (int ItemID = 0; ItemID <= FileLoader.maxItemID; ItemID++) {
				System.out.print(dbMatrix.get(ItemID).get(transID) + "\t");
			}
		}
		
	}
}
