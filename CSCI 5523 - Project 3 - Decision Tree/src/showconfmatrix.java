import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Zhengyang Zhao
 * Dec 15, 2018
 */

public class showconfmatrix {

	String predictionFile;
	
	
	public showconfmatrix(String predictionFile) {
		this.predictionFile = predictionFile;
	}
	
	
	public static void main(String[] args) {

		//String predictionFile = "predictionFile";
		String predictionFile = args[0];
		showconfmatrix resultShower = new showconfmatrix(predictionFile);
		resultShower.show();
		
	}
	
	
	public void show() {
		double accuracy = 0;
		int recNum = 0;
		List<List<Integer>> confusionMatrix = new ArrayList<>();
		//Definition: confusionMatrix.get(i) : trueLabel = i
		
		for (int i = 0; i < 10; i++) {
			List<Integer> list = new ArrayList<>();
			for (int j = 0; j < 10; j++) {
				list.add(0);
			}
			confusionMatrix.add(list);
		}
		
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(predictionFile);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				recNum++;
				String[] words = line.split(",");
				int tureLabel = Integer.parseInt(words[0]);
				int predLabel = Integer.parseInt(words[1]);
				List<Integer> list = confusionMatrix.get(tureLabel);
				list.set(predLabel, list.get(predLabel) + 1);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		
		int correct = 0;
		System.out.println("\n[-----Evaluation-----]");
		System.out.println("Confusion Matrix: ");
		for (int i = 0; i < 10; i++) {
			StringBuilder s = new StringBuilder();
			s.append("Actual label = " + i + ": ");
			List<Integer> list = confusionMatrix.get(i);
			for (int j = 0; j < 10; j++) {
				s.append(String.format("%5d ", list.get(j)));
			}
			System.out.println(s);
			correct += list.get(i);
		}
		accuracy = (double)correct / (double)recNum;
		System.out.println("\nAccuracy = " + accuracy + "\n");
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
