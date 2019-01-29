import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Dec 17, 2018
 */

public class rfmerge {

	public static void main(String[] args) {

		//String writePath = "out/";
		String writePath = args[0];
		
		voting(writePath);
		System.out.println("\nPrediction files merged.");
		String predMajorFile = writePath + "predMajor.csv";
		showconfmatrix resultShower = new showconfmatrix(predMajorFile);
		resultShower.show();
		
	}
	
	public static void voting(String writePath) {
		List<Integer> trueLabels = new ArrayList<>();
		List<Integer> predMajorLabels = new ArrayList<>();
		List<List<Integer>> predLabels = new ArrayList<>();
		
		for (int i = 0; i < 100; i++) {
			String predictionFile = writePath + String.format("pred%03d.csv", i + 1);
			FileReader fr = null;
			BufferedReader br = null;
			try {
				fr = new FileReader(predictionFile);
				br = new BufferedReader(fr);
				String line;
				int recNum = 0;
				while ((line = br.readLine()) != null) {
					
					String[] words = line.split(",");
					int tureLabel = Integer.parseInt(words[0]);
					int predLabel = Integer.parseInt(words[1]);
					if (i == 0) {
						trueLabels.add(tureLabel);
						List<Integer> recPred = new ArrayList<>();
						predLabels.add(recPred);
					}
					predLabels.get(recNum).add(predLabel);
					recNum++;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				close(br);
				close(fr);
			}
		}
		
		for (List<Integer> list : predLabels) {
			int majorLabel = majorityVote(list, 9);
			predMajorLabels.add(majorLabel);
		}
		
		String predMajorFile = writePath + "predMajor.csv";
		
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(predMajorFile);
			pw = new PrintWriter(fw);
			for (int i = 0; i < trueLabels.size(); i++) {
				String line = "";
				line += trueLabels.get(i);
				line += ",";
				line += predMajorLabels.get(i);
				pw.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(pw);
			close(fw);
		}
		
	}
	
	
	public static int majorityVote(List<Integer> list, int upperbound) {
		int[] array = new int[upperbound + 1];
		for (int element : list) {
			array[element] = array[element] + 1;
		}
		
		int majorElement = 0;
		int majorCount = 0;
		for (int i = 0; i <= upperbound; i++) {
			if (majorCount < array[i]) {
				majorCount = array[i];
				majorElement = i;
			}
		}
		return majorElement;
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

}
