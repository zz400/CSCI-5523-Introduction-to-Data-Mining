package test;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Test the speed of remove tail of LinkedList
 * 1. remove one by one using iterator;
 * 2. remove one by one using removeLast;
 * 3. using list.subList(int fromIndex, int toIndex).
 */

/**
 * @author Zhengyang Zhao
 * Oct 19, 2018
 */

public class TestListRemoveTail {

	public static void main(String[] args) {
		int num = 1000 * 1000;
		int threshold = 200 * 1000;
		long t0 = 0, t1, t2 = 0, t3, t4, t5 = 0, t6, t7;
		LinkedList<Integer> list1 = new LinkedList<>();
		LinkedList<Integer> list2 = new LinkedList<>();
		LinkedList<Integer> list3 = new LinkedList<>();
		
		t0 = System.currentTimeMillis();  
		for (int i = 0; i < num; i++) {
			list1.add(i);
			list2.add(i);
			list3.add(i);
		}
		
		t1 = System.currentTimeMillis();
		
		// 1. remove one by one using iterator;
		Iterator<Integer> iter = list1.iterator();
		while (iter.hasNext()) {
			Integer i = iter.next();
			if (i == threshold - 1) {
				t2 = System.currentTimeMillis();
				break;
			}
		}
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		t3 = System.currentTimeMillis();
		
		// 2. remove one by one using removeLast;
		while (list2.getLast() >= threshold) {
			list2.removeLast();
		}
		t4 = System.currentTimeMillis();
		
		
		// 3. using list.subList(int fromIndex, int toIndex).
		ListIterator<Integer> liter = list3.listIterator();
		int index = 0;
		while (liter.hasNext()) {
			Integer i = liter.next();
			if (i == threshold) {
				index = liter.nextIndex();
				t5 = System.currentTimeMillis();
			}
		}
		list3.subList(0, index);
		t6 = System.currentTimeMillis();
		
		System.out.println("list1: " + list1.size() + " list2: " + list2.size() + " list3: " + list3.size());
		System.out.println("list1: " + (t3 - t2) + " ms; total: "  + (t3 - t1) + " ms");
		System.out.println("list2: " + (t4 - t3) + " ms");
		System.out.println("list3: " + (t6 - t5) + " ms; total: "  + (t6 - t4) + " ms");
		
	}
}
