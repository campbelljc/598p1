package preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class Splitter {
	
	/**
	 * The entry point for the splitter that splits the input into a training set & a test set
	 * @param args
	 */
	public static void main(String args[]) {
//		List<String> inputSet = readLinesFromFile("../Data/Part1/OnlineNewsPopularity.csv");
		List<String> inputSet = readLinesFromFile("../Data/Part2/cbc_sentiment.csv");
		int totalNumItems = inputSet.size();
		System.out.println(totalNumItems + " entries loaded.");
		shuffleInput(inputSet);
		
		double trainingDataPercentage = 0.8;
		
		int numTrainingData = (int)(trainingDataPercentage * totalNumItems);		
		List<String> trainingSet = inputSet.subList(0, numTrainingData);
		List<String> testSet = inputSet.subList(numTrainingData, totalNumItems);
		
		System.out.println(trainingSet.size() + " entries as training data." );
		System.out.println(testSet.size() + " entries as test data." );

		writeLinesToFile("../Data/Part2/trainingSet.csv", trainingSet);
		writeLinesToFile("../Data/Part2/testSet.csv", testSet);
	}
	
	/**
	 * Read lines and save them in to a List<String>
	 * @param inputFile : path to the file to read
	 * @return
	 */
	public static List<String> readLinesFromFile(String inputFile) {
		List<String> lines = new ArrayList<>(65535);
		try (BufferedReader bufferedReader = new BufferedReader(new FileReader(inputFile))) {
			// Discard the first line
			bufferedReader.readLine();
			
		    String line;
		    while ((line = bufferedReader.readLine()) != null) {
		    	if (line.length() > 0)
		    		lines.add(line);
		    }
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error reading file : " + inputFile);
		}
		return lines;
	}
	
	/**
	 * Write a List<String> to a text file
	 * @param outputFileName
	 * @param outputList
	 */
	public static void writeLinesToFile(String outputFileName, List<String> outputList ) {

		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFileName))) {
			for (String str : outputList) {
				bufferedWriter.write(str);
				bufferedWriter.write("\n");
			}
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Error writing file : " + outputFileName);
		}
	}
	
	/**
	 * Shuffle the input list
	 * @param inputSet
	 */
	public static void shuffleInput(List<?> inputSet) {
		if (null == inputSet) {
			System.err.println("No valid input set to shuffle!");
			return;
		}
		
		long seed = System.nanoTime();
		Collections.shuffle(inputSet, new Random(seed));
	}
}
