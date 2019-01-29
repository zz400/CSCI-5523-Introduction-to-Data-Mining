package test;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Oct 16, 2018
 */

public class TestArrayPerformance {

	public static int[][] testArray(String filename, int rowNum){
		int[][] array = new int[rowNum][2];
		FileReader fr = null;
		BufferedReader br = null;
		long t0 = 0, t1 = 0, t2 = 0, t3;
		try {
			t0 = System.currentTimeMillis();
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			String line;
			int row = 0;
			t1 = System.currentTimeMillis();    // t1: start loop
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\\s+");
				array[row][0] = Integer.parseInt(pair[0]);
				array[row][1] = Integer.parseInt(pair[1]); 
				row++;
			}
			t2 = System.currentTimeMillis();    // t2: end loop
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			close(br);
			close(fr);
			t3 = System.currentTimeMillis();
		}
		
		System.out.println("Array:");
		System.out.println("Initialization: " + (t1 - t0)  + " ms  FillingArray: "  + (t2 - t1)  + " ms  Closing: "  + (t3 - t2) + " ms");
		return array;
	}

	public static List<List<Integer>> testArrayListRow(String filename, int rowNum){
		List<List<Integer>> list = new ArrayList<>();
		FileReader fr = null;
		BufferedReader br = null;
		long t0 = 0, t1 = 0, t2 = 0, t3;
		try {
			t0 = System.currentTimeMillis();
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			String line;
			t1 = System.currentTimeMillis();    // t1: start loop
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\\s+");
				List<Integer> rowlist = new ArrayList<>();
				rowlist.add(Integer.parseInt(pair[0]));
				rowlist.add(Integer.parseInt(pair[1]));
				list.add(rowlist);
			}
			t2 = System.currentTimeMillis();    // t2: end loop
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			close(br);
			close(fr);
			t3 = System.currentTimeMillis();
		}
		
		System.out.println("ArrayList Row:");
		System.out.println("Initialization: " + (t1 - t0)  + " ms  FillingArray: "  + (t2 - t1)  + " ms  Closing: "  + (t3 - t2) + " ms");
		return list;
	}
    
	
	public static List<List<Integer>> testArrayListCol(String filename, int rowNum){
		List<List<Integer>> list = new ArrayList<>();
		List<Integer> collist0 = new ArrayList<>();
		List<Integer> collist1 = new ArrayList<>();
		FileReader fr = null;
		BufferedReader br = null;
		long t0 = 0, t1 = 0, t2 = 0, t3;
		try {
			t0 = System.currentTimeMillis();
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			String line;
			t1 = System.currentTimeMillis();    // t1: start loop
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\\s+");
				collist0.add(Integer.parseInt(pair[0]));
				collist1.add(Integer.parseInt(pair[1]));
			}
			list.add(collist0);
			list.add(collist1);
			t2 = System.currentTimeMillis();    // t2: end loop
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			close(br);
			close(fr);
			t3 = System.currentTimeMillis();
		}
		
		System.out.println("ArrayList Col:");
		System.out.println("Initialization: " + (t1 - t0)  + " ms  FillingArray: "  + (t2 - t1)  + " ms  Closing: "  + (t3 - t2) + " ms");
		return list;
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
	
    public static void main(String args[]) {
		String smallfile = "src/small", largefile = "src/large";
		int rowSmall = 100000, rowLarge = 2060719;
		
		int[][] smallArray = testArray(smallfile, rowSmall);
		List<List<Integer>> smallListRow = testArrayListRow(smallfile, rowSmall);
		List<List<Integer>> smallListCol = testArrayListCol(smallfile, rowSmall);
		
		int[][] largeArray = testArray(largefile, rowLarge);
		List<List<Integer>> largeListRow = testArrayListRow(largefile, rowLarge);
		List<List<Integer>> largeListCol = testArrayListCol(largefile, rowLarge);
		
		System.out.println(largeArray[rowLarge - 1][1]);
		System.out.println(largeListRow.get(rowLarge - 1).get(1));
		System.out.println(largeListCol.get(1).get(rowLarge - 1));
		
		
		int count = 0;
		long t4, t5;
		t4 = System.currentTimeMillis(); 
		for (int i = 0; i < rowLarge; i++) {
			if (largeArray[i][1] == 1000) {
				count++;
			}
		}
		t5 = System.currentTimeMillis(); 
		System.out.println(count);
		System.out.println("Array: " + (t5 - t4) );
		
		count = 0;
		t4 = System.currentTimeMillis(); 
		for (int i = 0; i < rowLarge; i++) {
			if (largeListRow.get(i).get(1) == 1000) {
				count++;
			}
		}
		t5 = System.currentTimeMillis(); 
		System.out.println(count);
		System.out.println("ArrayList Row: " + (t5 - t4) );
		
		count = 0;
		t4 = System.currentTimeMillis(); 
		for (int i = 0; i < rowLarge; i++) {
			if (largeListCol.get(1).get(i) == 1000) {
				count++;
			}
		}
		t5 = System.currentTimeMillis(); 
		System.out.println(count);
		System.out.println("ArrayList Col: " + (t5 - t4) );
		
    }

}
