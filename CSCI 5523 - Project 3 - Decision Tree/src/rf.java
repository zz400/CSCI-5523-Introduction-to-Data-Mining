import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Random;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Dec 16, 2018
 */

public class rf {

	public static void main(String args[]) {
		/*
		int minFreq = 1;
		String trainFile = "data/rep1/train.csv";
		String writePath = "out/";
		String testFile = "data/rep1/test.csv";
		*/
		
		int minFreq = Integer.parseInt(args[0]);
		String trainFile = args[1];
		String testFile = args[2];
		String writePath = args[3];
		
		
		System.out.println("\n###################################################");
		System.out.println("[-----Training Random Forest-----]");
		
		// Sampling the data file:
		generateSamplingFiles(trainFile, writePath);
		
		// Traing 100 decision trees:
		for (int i = 0; i < 100; i++) {
			String samplingFile = writePath + String.format("train_sample%03d.csv", i + 1);
			String modelFile = writePath + String.format("dt_tree%03d.csv", i + 1);
			
			boolean printTreeDetails = false;
			dtinduce.treeIndex++;
			dtinduce dtTrainer = new dtinduce(minFreq, samplingFile, modelFile);
			dtTrainer.train(printTreeDetails);
		}
		
		// Do testing for the 100 trees:
		for (int i = 0; i < 100; i++) {
			String modelFile = writePath + String.format("dt_tree%03d.csv", i + 1);
			String predictionFile = writePath + String.format("pred%03d.csv", i + 1);
			
			dtclassify.classifierIndex++;
			dtclassify dtClassifier = new dtclassify(modelFile, testFile, predictionFile);
			dtClassifier.classify();
			
			
		}
	}
	
	public static void generateSamplingFiles(String trainFile, String writePath) {
		FileReader fr  = null;
		BufferedReader br  = null;
		String line;
		String[] lines = new String[60000];;
		try {
			fr = new FileReader(trainFile);
			br = new BufferedReader(fr);
			int i = 0;
			while ((line = br.readLine()) != null) {
				lines[i++] = line;
			}
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
		
		int sampleNum = 24000;
		for (int i = 0; i < 100; i++) {
			Random rand = new Random();
			FileWriter fw = null;
			PrintWriter pw = null;
			try {
				String samplingFile = String.format("train_sample%03d.csv", i + 1);
				samplingFile = writePath + samplingFile;
				fw = new FileWriter(samplingFile);
				pw = new PrintWriter(fw);
				for (int j = 0; j < sampleNum; j++) {
					int n = rand.nextInt(60000);
					pw.println(lines[n]);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				close(pw);
				close(fw);
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

