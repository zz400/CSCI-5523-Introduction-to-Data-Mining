package test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Test iteration speed of List.
 */

/**
 * @author Zhengyang Zhao
 * Oct 17, 2018
 */

public class TestListIteration {
	
	public static void main(String[] args) {
		int num = 1000 * 1000;
		List<List<Integer>> list1 = new ArrayList<>();
		List<List<Integer>> list2 = new ArrayList<>();
		List<List<Integer>> list3 = new ArrayList<>();
		
		long t0 = System.currentTimeMillis();  
		for (int i = 0; i < num; i++) {
			list1.add(null);
		}
		
		long t1 = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			list2.add(new ArrayList<>());
		}
		
		long t2 = System.currentTimeMillis();
		for (int i = 0; i < num; i++) {
			List<Integer> col = new ArrayList<>();
			for (int j = 0; j < 100; j++) {
				col.add(j);
			}
			list3.add(col);
		}
		long t3 = System.currentTimeMillis();
		
		System.out.println("list1: " + (t1 - t0) + " ms");
		System.out.println("list2: " + (t2 - t1) + " ms");
		System.out.println("list3: " + (t3 - t2) + " ms");
		
		System.out.println(list1.size());
		System.out.println(list2.size());
		System.out.println(list3.size());
		
		long tt0 = System.currentTimeMillis();
		HashMap<Integer, List<Integer>> map = new HashMap<>();
		for (int i = 0; i < num; i++) {
			map.put(i, list3.get(i));
		}
		long tt1 = System.currentTimeMillis();
		System.out.println("HashMap: " + (tt1 - tt0) + " ms");
		
		
		long t4 = System.currentTimeMillis();
		int count1 = 0;
		for (List l : list1) {
			if (l == null) {
				count1++;
			}
			
		}

		long t5 = System.currentTimeMillis();
		int count2 = 0;
		for (List l : list1) {
			if (l == null) {
				count2++;
			}
		}
		
		long t6 = System.currentTimeMillis();
		int count3 = 0;
		for (List l : list3) {
			if (l == null) {
				count3++;
			}
		}
		long t7 = System.currentTimeMillis();
		
		System.out.println("list1: " + (t5 - t4) + " ms");
		System.out.println("list2: " + (t6 - t5) + " ms");
		System.out.println("list3: " + (t7 - t6) + " ms");

	}
	

}
