package test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Oct 20, 2018
 */

public class TestHash {
	
	public static void main(String[] args) {
		Integer i1 = new Integer(100000);
		Integer i2 = new Integer(100000);
		System.out.println(i1.hashCode());
		System.out.println(i2.hashCode());
		System.out.println(i1.equals(i2));
		System.out.println(i1 == i2);
		ArrayList<Integer> l1 = new ArrayList<>();
		ArrayList<Integer> l2 = new ArrayList<>();
		l1.add(44393);
		l1.add(-24843);
		
		l2.add(44393);
		l2.add(-24843);
		System.out.println(l1.hashCode());
		System.out.println(l2.hashCode());
		System.out.println(l1.equals(l2));
		System.out.println(l1 == l2);

		System.out.println("--------");
		int[] arr1 = new int[] {44393, -24843, 7733};
		int[] arr2 = new int[] {44393, -24843, 7733};
		System.out.println(arr1.hashCode());
		System.out.println(arr2.hashCode());
		System.out.println(arr1.equals(arr2));
		System.out.println(arr1 == arr2);

	}
}
