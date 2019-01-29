package backup;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Dec 13, 2018
 */

public class DataLoaderInt {

	public static void main(String[] args) {
		String filename = "data/rep1/train.csv";
		FileReader fr  = null;
		BufferedReader br  = null;
		List<List<Integer>> db = new ArrayList<>();
		List<Integer> labels = new ArrayList<>();
		String line;
		
		try {
			fr = new FileReader(filename);
			br = new BufferedReader(fr);
			while ((line = br.readLine()) != null) {
				String[] words = line.split(",");
				int label = Integer.parseInt(words[0]);
				labels.add(label);
				List<Integer> record = new ArrayList<>();
				for (int i = 1; i < words.length; i++) {
					int num = Integer.parseInt(words[i]);
					record.add(num);
				}
				db.add(record);
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		
		for (int d = 0; d < db.get(0).size(); d++) {
			System.out.println("\n\nd = " + d);
			TreeMap<Integer, Integer> map = new TreeMap<>();
			for (int i = 0; i < db.size(); i++) {
				int level = db.get(i).get(d);
				int count = map.getOrDefault(level, 0);
				map.put(level, count + 1);
			}
			for (int key : map.keySet()) {
				System.out.println(key + " : " + map.get(key));
			}
		}
	}
	
	public static void close(Closeable c) {
		if (c == null) {
			return;
		}
		try {
			c.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
