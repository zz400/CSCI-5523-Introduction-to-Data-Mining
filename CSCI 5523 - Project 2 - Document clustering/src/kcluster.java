import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;


/**
 * @author Zhengyang Zhao
 * Nov 25, 2018
 */


public class kcluster {
	static ArrayList<ArrayList<Integer>> RandLists; // contains 20 lists (20 trials).
	static ArrayList<Integer> seeds 
		= new ArrayList<>(Arrays.asList(new Integer[] 
				{1,3,5,7,9,11,13,15,17,19,21,23,25,27,29,31,33,35,37,39}));
	
	static int numPoints;
	static ArrayList<Integer> pID2newID;  // store the newID of each point
	static HashMap<Integer, Integer> newID2pID; // store the mapping from newID to pID
	static ArrayList<HashMap<Integer, Double>> vectors;
	
	static HashMap<Integer, String> point2class;
	
	static ArrayList<HashMap<Integer, Double>> compVectors;
	static HashMap<Integer, Double> compVectorSum;  
	static ArrayList<Double> compSquares;
	static ArrayList<Double> compDiD;  // Used int E1 model
	static ArrayList<HashSet<Integer>> clusters;
	static HashMap<Integer, Integer> point2cluster;
	static int iterationNum;
	
	static int bestTrialNum;  // the number of the best trial.
	static double bestTrialCF;  // value of criterionFunction for the best trial.
	static HashMap<Integer, Integer> bestTrailPoint2cluster;
	static HashMap<Integer, HashMap<String, Integer>> cluster2ClassVector;
	static double bestTrialEntropy;
	static double bestTrialPurity;
	static int averageTrialRuntime;
	
	public static void main(String[] args) {
		/*
		String inputfile = "log2freq.csv";
		String criterionFunction = "I2";
		String classFile = "reuters21578.class";
		int numClusters = 10;
		int numTrials = 20;
		String outputFile = "output.csv";
		*/
		
		String inputfile = args[0];
		String criterionFunction = args[1];
		String classFile = args[2];
		int numClusters = Integer.parseInt(args[3]);
		int numTrials = Integer.parseInt(args[4]);
		String outputFile = args[5];
		
		int iterNum = 30;
		
		// Import files:
		importVectors(inputfile);
		importClassfile(classFile);
		//System.out.println("Files imported.  Points #: " + numPoints);
		
		// Generate random lists:
		getRandomLists(numClusters, numPoints);
		
		System.out.println("inputfile: " +  inputfile + ";   Criterion Function: " 
				+  criterionFunction + ";   numClusters: " +  numClusters);
		
		averageTrialRuntime = 0;
		
		if (criterionFunction.equals("I2")) {
			bestTrialCF = 0.0;
			for (int trial = 0; trial < numTrials; trial++) {
				double cf = clusterI2(trial, iterNum);
				/*
				System.out.println("Trial: " + String.format("%02d", trial) 
				+ ";  Iteration #: " + String.format("%02d", iterationNum)
				+ ";  CriterionFunction: " + String.format("%.4f", cf));
				*/
				if (cf > bestTrialCF) {
					bestTrialCF = cf;
					bestTrialNum = trial;
					bestTrailPoint2cluster = new HashMap<>();
					for (int key : point2cluster.keySet()) {
						int value = point2cluster.get(key);
						bestTrailPoint2cluster.put(key, value);
					}
				}
			}
		} else if (criterionFunction.equals("E1")) {
			bestTrialCF = Double.MAX_VALUE;
			for (int trial = 0; trial < numTrials; trial++) {
				double cf = clusterE1(trial, iterNum);
				/*
				System.out.println("Trial: " + String.format("%02d", trial) 
						+ ";  Iteration #: " + String.format("%02d", iterationNum)
						+ ";  CriterionFunction: " + String.format("%.4f", cf));
				*/
				if (cf < bestTrialCF) {
					bestTrialCF = cf;
					bestTrialNum = trial;
					bestTrailPoint2cluster = new HashMap<>();
					for (int key : point2cluster.keySet()) {
						int value = point2cluster.get(key);
						bestTrailPoint2cluster.put(key, value);
					}
				}
			} 
		} else if (criterionFunction.equals("SSE")) {
			bestTrialCF = Double.MAX_VALUE;
			for (int trial = 0; trial < numTrials; trial++) {
				double cf = clusterSSE(trial, iterNum);
				/*
				System.out.println("Trial: " + String.format("%02d", trial) 
						+ ";  Iteration #: " + String.format("%02d", iterationNum)
						+ ";  CriterionFunction: " + String.format("%.4f", cf));
				*/
				if (cf < bestTrialCF) {
					bestTrialCF = cf;
					bestTrialNum = trial;
					bestTrailPoint2cluster = new HashMap<>();
					for (int key : point2cluster.keySet()) {
						int value = point2cluster.get(key);
						bestTrailPoint2cluster.put(key, value);
					}
				}
			}
		}
		averageTrialRuntime = averageTrialRuntime / numTrials / 1000;
		
		getEntropyPurity();
		System.out.println("BestTrial: " + String.format("%02d", bestTrialNum)
				+ ";   CF: " + String.format("%.4f", bestTrialCF)
				+ ";   Entropy: " + String.format("%.4f", bestTrialEntropy) 
				+ ";   Purity: " + String.format("%.4f", bestTrialPurity)
				+ ";   AverageTrailRunTime: " + averageTrialRuntime + " s");
		writeOutput(outputFile);
		
	}
	
	
	public static double clusterI2(int trialNum, int iterNum) {
		long t0 = System.currentTimeMillis(); 
		// Initialize clusters:
		ArrayList<Integer> initalCents = RandLists.get(trialNum);
		HashSet<Integer> initalCentsSet = new HashSet<>(initalCents);
		int clusterNum = initalCents.size();
		//System.out.println("initalCentsSet: " + initalCentsSet);
		
		compVectors = new ArrayList<>();
		compSquares = new ArrayList<>();
		clusters = new ArrayList<>();
		point2cluster = new HashMap<>();
		
		int clusterIndexInit = 0;
		for (int point : initalCents) {
			HashSet<Integer> set = new HashSet<>();
			set.add(point);
			clusters.add(set);
			HashMap<Integer, Double> compVector = new HashMap<>();
			for (int key : vectors.get(point).keySet()) {
				compVector.put(key, vectors.get(point).get(key));
			}
			compVectors.add(compVector);
			double compSquare = dot(compVector, compVector);
			compSquares.add(compSquare);
			point2cluster.put(point, clusterIndexInit);
			clusterIndexInit++;
		}
		
		for (int i = 0; i < numPoints; i++) {
			if (initalCentsSet.contains(i)) {
				continue;
			}
			double maxCos = -1;
			int clusterIndex = -1;
			for (int j = 0; j < clusterNum; j++) {
				double cos = dot(vectors.get(i), compVectors.get(j));
				if (cos > maxCos) {
					maxCos = cos;
					clusterIndex = j;
				}
			}
			point2cluster.put(i, clusterIndex);
		}
		
		for (Integer i : point2cluster.keySet()) {
			if (initalCentsSet.contains(i)) {
				continue;
			}
			int clusterIndex = point2cluster.get(i);
			HashSet<Integer> set = clusters.get(clusterIndex);
			set.add(i);
			HashMap<Integer, Double> compVector = compVectors.get(clusterIndex);
			
			double compSquare = compSquares.get(clusterIndex);
			double compSquareNew = compSquare + 1.0 + 2 * dot(vectors.get(i), compVector);
			sum(compVector, vectors.get(i));
			compSquares.set(clusterIndex, compSquareNew);
		}
		
		// Iteration:
		iterationNum = iterNum;
		for (int iter = 0; iter < iterNum; iter++) {
			int moveNum = 0;
			for (int i = 0; i < numPoints; i++) {
				int clusterIndex = point2cluster.get(i);
				HashSet<Integer> set0 = clusters.get(clusterIndex);
				HashMap<Integer, Double> compVector0 = compVectors.get(clusterIndex);
				HashMap<Integer, Double> vector = vectors.get(i);
				double compSquare0 = compSquares.get(clusterIndex);
				double compSquare0New = compSquare0 + 1.0 - 2 * dot(vector, compVector0);
				double comp0Diff = Math.sqrt(compSquare0) - Math.sqrt(compSquare0New);
				
				double maxCompDiff = Double.NEGATIVE_INFINITY;
				int clusterIndex1 = -1;
				for (int j = 0; j < clusterNum; j++) {
					if (j == clusterIndex) {
						continue;
					}
					HashMap<Integer, Double> compVector1 = compVectors.get(j);
					double compSquare1 = compSquares.get(j);
					double compSquare1New = compSquare1 + 1.0 + 2 * dot(vector, compVector1);
					double comp1Diff = Math.sqrt(compSquare1New) - Math.sqrt(compSquare1);
					if (maxCompDiff < comp1Diff) {
						maxCompDiff = comp1Diff;
						clusterIndex1 = j;
					}
				}
				
				// Update centroid:
				if (maxCompDiff > comp0Diff) {
					moveNum++;
					//System.out.println("Point: " + i + "; old cluster: " + clusterIndex + "; new cluster: " + clusterIndex1);
					HashSet<Integer> set1 = clusters.get(clusterIndex1);
					HashMap<Integer, Double> compVector1 = compVectors.get(clusterIndex1);
					double compSquare1 = compSquares.get(clusterIndex1);
					double compSquare1New = compSquare1 + 1.0 + 2 * dot(vector, compVector1);
					// Update old cluster:
					set0.remove(i);
					minus(compVector0, vector);
					compSquares.set(clusterIndex, compSquare0New);
					// Update new cluster:
					set1.add(i);
					sum(compVector1, vector);
					compSquares.set(clusterIndex1, compSquare1New);
					// Update point2cluster map:
					point2cluster.put(i, clusterIndex1);
				}
			}
			//System.out.println("Iteration: " + iter + "; # of movement: " + moveNum);
			if (moveNum == 0) {
				iterationNum = iter;
				break;
			}
		}
		
		// calculate criterionFunction:
		double cf = 0;
		for (int j = 0; j < clusterNum; j++) {
			cf += Math.sqrt(compSquares.get(j));
		}
		long t1 = System.currentTimeMillis(); 
		int trialRuntime = (int)(t1 - t0);
		averageTrialRuntime += trialRuntime;
		
		return cf;
	}
	
	
	public static double clusterE1(int trialNum, int iterNum) {
		long t0 = System.currentTimeMillis(); 
		// Initialize clusters:
		ArrayList<Integer> initalCents = RandLists.get(trialNum);
		HashSet<Integer> initalCentsSet = new HashSet<>(initalCents);
		int clusterNum = initalCents.size();
		//System.out.println("initalCentsSet: " + initalCentsSet);
		
		compVectors = new ArrayList<>();
		compVectorSum = new HashMap<>();
		compSquares = new ArrayList<>();
		compDiD = new ArrayList<>();
		clusters = new ArrayList<>();
		point2cluster = new HashMap<>();
		
		int clusterIndexInit = 0;
		for (int i = 0; i < numPoints; i++) {
			sum(compVectorSum, vectors.get(i));
		}
		for (int point : initalCents) {
			HashSet<Integer> set = new HashSet<>();
			set.add(point);
			clusters.add(set);
			HashMap<Integer, Double> compVector = new HashMap<>();
			for (int key : vectors.get(point).keySet()) {
				compVector.put(key, vectors.get(point).get(key));
			}
			compVectors.add(compVector);
			double compSquare = dot(compVector, compVector);
			compSquares.add(compSquare);
			double did = dot(compVector, compVectorSum);
			compDiD.add(did);
			point2cluster.put(point, clusterIndexInit);
			clusterIndexInit++;
		}
		
		for (int i = 0; i < numPoints; i++) {
			if (initalCentsSet.contains(i)) {
				continue;
			}
			double maxCos = -1;
			int clusterIndex = -1;
			for (int j = 0; j < clusterNum; j++) {
				double cos = dot(vectors.get(i), compVectors.get(j));
				if (cos > maxCos) {
					maxCos = cos;
					clusterIndex = j;
				}
			}
			point2cluster.put(i, clusterIndex);
		}
		
		for (Integer i : point2cluster.keySet()) {
			if (initalCentsSet.contains(i)) {
				continue;
			}
			int clusterIndex = point2cluster.get(i);
			HashSet<Integer> set = clusters.get(clusterIndex);
			set.add(i);
			HashMap<Integer, Double> compVector = compVectors.get(clusterIndex);
			
			double compSquare = compSquares.get(clusterIndex);
			double compSquareNew = compSquare + 1.0 + 2 * dot(vectors.get(i), compVector);
			compSquares.set(clusterIndex, compSquareNew);
			double did = compDiD.get(clusterIndex);
			double didNew = did + dot(vectors.get(i), compVectorSum);
			
			compDiD.set(clusterIndex, didNew);
			sum(compVector, vectors.get(i));
		}
		
		// Iteration:
		iterationNum = iterNum;
		for (int iter = 0; iter < iterNum; iter++) {
			int moveNum = 0;
			for (int i = 0; i < numPoints; i++) {
				int clusterIndex = point2cluster.get(i);
				HashSet<Integer> set0 = clusters.get(clusterIndex);
				int numPoint0 = set0.size();
				HashMap<Integer, Double> compVector0 = compVectors.get(clusterIndex);
				HashMap<Integer, Double> vector = vectors.get(i);
				
				double compSquare0Old = compSquares.get(clusterIndex);
				double compSquare0New = compSquare0Old + 1.0 - 2 * dot(vector, compVector0);
				double did0Old = compDiD.get(clusterIndex);
				double did0New = did0Old - dot(vector, compVectorSum);
				double cf0Old = (double)numPoint0 * did0Old / Math.sqrt(compSquare0Old);
				double cf0New = (double)(numPoint0 - 1) * did0New / Math.sqrt(compSquare0New);
				double cf0Diff = cf0Old - cf0New;  // cf0Diff the larger the better
				
				double minCompDiff = Double.POSITIVE_INFINITY;
				int clusterIndex1 = -1;
				for (int j = 0; j < clusterNum; j++) {
					if (j == clusterIndex) {
						continue;
					}
					HashSet<Integer> set1 = clusters.get(j);
					int numPoint1 = set1.size();
					HashMap<Integer, Double> compVector1 = compVectors.get(j);
					
					double compSquare1Old = compSquares.get(j);
					double compSquare1New = compSquare1Old + 1.0 + 2 * dot(vector, compVector1);
					double did1Old = compDiD.get(j);
					double did1New = did1Old + dot(vector, compVectorSum);
					double cf1Old = (double)numPoint1 * did1Old / Math.sqrt(compSquare1Old);
					double cf1New = (double)(numPoint1 + 1) * did1New / Math.sqrt(compSquare1New);
					
					double cf1Diff = cf1New - cf1Old;  // cf1Diff the smaller the better
					if (minCompDiff > cf1Diff) {
						minCompDiff = cf1Diff;
						clusterIndex1 = j;
					}
				}
				
				// Update centroid:
				if (minCompDiff < cf0Diff) {
					moveNum++;
					HashSet<Integer> set1 = clusters.get(clusterIndex1);
					HashMap<Integer, Double> compVector1 = compVectors.get(clusterIndex1);
					
					double compSquare1Old = compSquares.get(clusterIndex1);
					double compSquare1New = compSquare1Old + 1.0 + 2 * dot(vector, compVector1);
					double did1Old = compDiD.get(clusterIndex1);
					double did1New = did1Old + dot(vector, compVectorSum);
					
					// Update old cluster:
					set0.remove(i);
					minus(compVector0, vector);
					compSquares.set(clusterIndex, compSquare0New);
					compDiD.set(clusterIndex, did0New);
					// Update new cluster:
					set1.add(i);
					sum(compVector1, vector);
					compSquares.set(clusterIndex1, compSquare1New);
					compDiD.set(clusterIndex1, did1New);
					// Update point2cluster map:
					point2cluster.put(i, clusterIndex1);
				}
			}
			//System.out.println("Iteration: " + iter + "; # of movement: " + moveNum);
			if (moveNum == 0) {
				iterationNum = iter;
				break;
			}
		}
		
		// calculate criterionFunction:
		double cf = 0;
		for (int j = 0; j < clusterNum; j++) {
			HashSet<Integer> set = clusters.get(j);
			int numPoint = set.size();
			double compSquare = compSquares.get(j);
			double did = compDiD.get(j);
			cf += (double)(numPoint) * did / Math.sqrt(compSquare);
		}
		
		long t1 = System.currentTimeMillis(); 
		int trialRuntime = (int)(t1 - t0);
		averageTrialRuntime += trialRuntime;
		
		return cf;
	}
	
	
 	public static void getRandomLists(int numClusters, int numPoints) {
		RandLists = new ArrayList<>();
		for (int i = 0; i < seeds.size(); i++) {
			int seed = seeds.get(i);
			RandomGenerator randGen = new RandomGenerator(seed, numClusters, numPoints);
			ArrayList<Integer> randList = randGen.getRandomList();
			//System.out.println("Seed: " + seed);
			//System.out.println("Rand: " + randList);
			RandLists.add(randList);
		}
	}
 	
	public static double clusterSSE(int trialNum, int iterNum) {
		long t0 = System.currentTimeMillis(); 
		// Initialize clusters:
		ArrayList<Integer> initalCents = RandLists.get(trialNum);
		HashSet<Integer> initalCentsSet = new HashSet<>(initalCents);
		int clusterNum = initalCents.size();
		//System.out.println("initalCentsSet: " + initalCentsSet);
		
		compVectors = new ArrayList<>();
		compSquares = new ArrayList<>();
		clusters = new ArrayList<>();
		point2cluster = new HashMap<>();
		ArrayList<Double> centSquares = new ArrayList<>();
		
		int clusterIndexInit = 0;
		for (int point : initalCents) {
			HashSet<Integer> set = new HashSet<>();
			set.add(point);
			clusters.add(set);
			HashMap<Integer, Double> compVector = new HashMap<>();
			for (int key : vectors.get(point).keySet()) {
				compVector.put(key, vectors.get(point).get(key));
			}
			compVectors.add(compVector);
			double compSquare = dot(compVector, compVector);
			compSquares.add(compSquare);
			point2cluster.put(point, clusterIndexInit);
			clusterIndexInit++;
		}
		
		for (int i = 0; i < numPoints; i++) {
			if (initalCentsSet.contains(i)) {
				continue;
			}
			HashMap<Integer, Double> vector = vectors.get(i);
			double minDistSq = Double.MAX_VALUE;
			int clusterIndex = -1;
			for (int j = 0; j < clusterNum; j++) {
				double distSq = 2 - 2 * dot(vector, compVectors.get(j));
				if (distSq < minDistSq) {
					minDistSq = distSq;
					clusterIndex = j;
				}
			}
			point2cluster.put(i, clusterIndex);
		}
		
		for (int i = 0; i < numPoints; i++) {
			if (initalCentsSet.contains(i)) {
				continue;
			}
			HashMap<Integer, Double> vector = vectors.get(i);
			int clusterIndex = point2cluster.get(i);
			HashSet<Integer> set = clusters.get(clusterIndex);
			set.add(i);
			HashMap<Integer, Double> compVector = compVectors.get(clusterIndex);
			
			double compSquareOld = compSquares.get(clusterIndex);
			double compSquareNew = compSquareOld + 1.0 + 2 * dot(vector, compVector);
			compSquares.set(clusterIndex, compSquareNew);
			sum(compVector, vector);
		}
		
		for (int j = 0; j < clusterNum; j++) {
			double compSquare = compSquares.get(j);
			int clusterSize = clusters.get(j).size();
			double centSquare = compSquare / (double)clusterSize / (double)clusterSize;
			centSquares.add(centSquare);
		}
		
		// Iteration:
		iterationNum = iterNum;
		for (int iter = 0; iter < iterNum; iter++) {
			int moveNum = 0;
			for (int i = 0; i < numPoints; i++) {
				int clusterIndex = point2cluster.get(i);
				HashSet<Integer> set0 = clusters.get(clusterIndex);
				HashMap<Integer, Double> compVector0 = compVectors.get(clusterIndex);
				HashMap<Integer, Double> vector = vectors.get(i);
				double compSquare0 = compSquares.get(clusterIndex);
				double centSquare0 = centSquares.get(clusterIndex);
				double distSqr0 = 1 + centSquare0 - 2.0 * dot(vector, compVector0, (double)set0.size());
				
				double minDistSqr1 = Double.MAX_VALUE;
				int clusterIndex1 = -1;
				for (int j = 0; j < clusterNum; j++) {
					if (j == clusterIndex) {
						continue;
					}
					HashMap<Integer, Double> compVector1 = compVectors.get(j);
					double centSquare1 = centSquares.get(j);
					double distSqr1 = 1 + centSquare1 - 2.0 * dot(vector, compVector1, (double)clusters.get(j).size());
					if (minDistSqr1 > distSqr1) {
						minDistSqr1 = distSqr1;
						clusterIndex1 = j;
					}
				}
				
				// Update centroid:
				if (minDistSqr1 < distSqr0) {
					moveNum++;
					//System.out.println("Point: " + i + "; old cluster: " + clusterIndex + "; new cluster: " + clusterIndex1);
					HashSet<Integer> set1 = clusters.get(clusterIndex1);
					HashMap<Integer, Double> compVector1 = compVectors.get(clusterIndex1);
					double compSquare1 = compSquares.get(clusterIndex1);
					
					set0.remove(i);
					set1.add(i);
					double compSquare0New = compSquare0 + 1.0 - 2 * dot(vector, compVector0);
					double compSquare1New = compSquare1 + 1.0 + 2 * dot(vector, compVector1);
					double centSquare0New = compSquare0New / (double)set0.size() / (double)set0.size();
					double centSquare1New = compSquare1New / (double)set1.size() / (double)set1.size();
					compSquares.set(clusterIndex, compSquare0New);
					compSquares.set(clusterIndex1, compSquare1New);
					centSquares.set(clusterIndex, centSquare0New);
					centSquares.set(clusterIndex1, centSquare1New);
					minus(compVector0, vector);
					sum(compVector1, vector);
					point2cluster.put(i, clusterIndex1);
				}
			}
			//System.out.println("Iteration: " + iter + "; # of movement: " + moveNum);
			if (moveNum == 0) {
				iterationNum = iter;
				break;
			}
		}
		
		// calculate criterionFunction:
		double cf = 0;
		for (int j = 0; j < clusterNum; j++) {
			for (int i : clusters.get(j)) {
				HashMap<Integer, Double> compVector = compVectors.get(j);
				HashMap<Integer, Double> vector = vectors.get(i);
				double centSquare = centSquares.get(j);
				cf += 1 + centSquare - 2 * dot(vector, compVector, (double)clusters.get(j).size());
			}
		}
		
		long t1 = System.currentTimeMillis(); 
		int trialRuntime = (int)(t1 - t0);
		averageTrialRuntime += trialRuntime;
		
		return cf;
	}

	
	public static void getEntropyPurity() {
		// build the cluster2ClassVector map:
		cluster2ClassVector = new HashMap<>();
		for (Integer point : bestTrailPoint2cluster.keySet()) {
			int clusterIndex = bestTrailPoint2cluster.get(point);
			if (!cluster2ClassVector.containsKey(clusterIndex)) {
				HashMap<String, Integer> classVector = new HashMap<>();
				cluster2ClassVector.put(clusterIndex, classVector);
			}
			String classStr = point2class.get(point);
			HashMap<String, Integer> classVector = cluster2ClassVector.get(clusterIndex);
			int classSize = classVector.getOrDefault(classStr, 0);
			classVector.put(classStr, classSize + 1);
		}
		
		// calculate entropy and purity:
		bestTrialEntropy = 0.0;
		bestTrialPurity = 0.0;
		for (int clusterIndex : cluster2ClassVector.keySet()) {
			int mJ = 0;
			double enJ = 0.0;
			double puJ = 0.0;
			HashMap<String, Integer> classVector = cluster2ClassVector.get(clusterIndex);
			for (String classStr : classVector.keySet()) {
				int mIJ = classVector.get(classStr);
				mJ += mIJ;
			}
			
			for (String classStr : classVector.keySet()) {
				int mIJ = classVector.get(classStr);
				double pIJ = (double)mIJ / (double)mJ;
				enJ += -1.0 * pIJ * Math.log(pIJ) / Math.log(2.0);
				puJ = Math.max(puJ, pIJ);
			}
			bestTrialEntropy += (double)mJ / (double)numPoints * enJ;
			bestTrialPurity += (double)mJ / (double)numPoints * puJ;
		}
	}
	
	
	public static void importVectors(String inputfile) {
		pID2newID = new ArrayList<>();
		newID2pID = new HashMap<>();
		vectors = new ArrayList<>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(inputfile);
			br = new BufferedReader(fr);
			String line;
			int lastNewID = -1;
			while ((line = br.readLine()) != null) {
				String[] triple = line.split(",");
				int newID = Integer.parseInt(triple[0]);  
				int dimension = Integer.parseInt(triple[1]); 
				double value = Double.parseDouble(triple[2]); 
				if (newID != lastNewID) {
					numPoints++;
					// System.out.println("numPoints: " + numPoints);
					pID2newID.add(newID);
					HashMap<Integer, Double> vector = new HashMap<>();
					vector.put(dimension, value);
					vectors.add(vector);
				} else {
					HashMap<Integer, Double> vector = vectors.get(vectors.size() - 1);
					vector.put(dimension, value);
				}
				lastNewID = newID;
			}
			for (int i = 0; i < pID2newID.size(); i++) {
				newID2pID.put(pID2newID.get(i), i);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
	}
	
	public static void importClassfile(String classFile) {
		point2class = new HashMap<>();
		FileReader fr = null;
		BufferedReader br = null;
		try {
			fr = new FileReader(classFile);
			br = new BufferedReader(fr);
			String line;
			while ((line = br.readLine()) != null) {
				String[] string = line.split(",");
				int newID = Integer.parseInt(string[0]);  
				int point = newID2pID.get(newID);
				String classStr = string[1]; 
				point2class.put(point, classStr);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			close(br);
			close(fr);
		}
	}
	
	public static void writeOutput(String outputFile) {
		FileWriter fw = null;
		PrintWriter pw = null;
		try {
			fw = new FileWriter(outputFile);
			pw = new PrintWriter(fw);
			String[] outputLines = new String[numPoints];
			for (int point : bestTrailPoint2cluster.keySet()) {
				int clusterIndex = bestTrailPoint2cluster.get(point);
				int newID = pID2newID.get(point);
				String line = newID + "," + clusterIndex;
				outputLines[point] = line;
			}
			for (int i = 0; i < numPoints; i++) {
				pw.println(outputLines[i]);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close(pw);
			close(fw);
		}
	}
	
	
	public static double dot(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2) {
		double res = 0;
		if (v1 == v2) {
			for (Integer key : v1.keySet()) {
				double value = v1.get(key);
				res += value * value;
			}
			return res;
		}
		
		if (v1.size() > v2.size()) {
			return dot(v2, v1);
		}
		
		// v2 is longer, v1 is shorter and more sparse;
		for (Integer key : v1.keySet()) {
			if (v2.containsKey(key)) {
				double value1 = v1.get(key);
				double value2 = v2.get(key);
				res += value1 * value2;
			}
		}
		return res;
	}
	
	public static double dot(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2, double dd) {
		// @return: dot(v1, v2) / dd.
		double res = 0;
		
		// Usually v2 is longer, v1 is shorter and more sparse;
		for (Integer key : v1.keySet()) {
			if (v2.containsKey(key)) {
				double value1 = v1.get(key);
				double value2 = v2.get(key);
				res += value1 * value2 / dd;
			}
		}
		return res;
	}
	
	public static void sum(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2) {
		// add v2 onto v1.
		for (Integer key : v2.keySet()) {
			double value2 = v2.get(key);
			double value1 = v1.getOrDefault(key, 0.0);
			v1.put(key, value1 + value2);
		}
	}
	
	public static void minus(HashMap<Integer, Double> v1, HashMap<Integer, Double> v2) {
		// minus v2 from v1.
		for (Integer key : v2.keySet()) {
			double value2 = v2.get(key);
			double value1 = v1.getOrDefault(key, 0.0);
			v1.put(key, value1 - value2);
		}
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


class RandomGenerator {
	long seed;
	int numClusters;
	int upperBound; // inclusive
	
	RandomGenerator(int seed, int numClusters, int upperBound) {
		this.seed = (long)seed;
		this.numClusters = numClusters;
		this.upperBound = upperBound; // inclusive
	}
	
	public ArrayList<Integer> getRandomList() {
		Random rand = new Random(this.seed);
		Set<Integer> randSet = new HashSet<>();
		ArrayList<Integer> randomList = new ArrayList<>();
		while (randomList.size() < this.numClusters) {
			int randInt = rand.nextInt(this.upperBound);
			if (!randSet.contains(randInt)) {
				randSet.add(randInt);
				randomList.add(randInt);
			}
		}
		Collections.sort(randomList);
		return randomList;
	}
}

