package project1;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This is the class to manipulate a sparse matrix dataset (extraction, pruning, etc.)
 * version 4.2
 * Using Compressed Sparse Column (CSC) for storing and managing entries;
 * Using iteration of two ArrayList for searching and pruning entries.
 *   (version 4.3 uses a HashSet of entries for searching and pruning entries ).
 */

/**
 * @author Zhengyang Zhao
 * Oct 18, 2018
 */

public class SparseList {
	
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
	
	
	public static void main(String[] args) {
		int option = 1;
		int minSup = 5;
		String filename = "in/toy";
		FileLoader.runBatch(filename, option);
		
		SparseList sList = new SparseList(FileLoader.dbReordered, minSup);
		sList.doProjection(0, minSup);
		System.out.println("nonZeroCols: " + sList.nonZeroCols);
		System.out.println("colMap: " + sList.colMap);
		System.out.println("colList: " + sList.colList);
	}

}
	
