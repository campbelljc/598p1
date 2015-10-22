package preprocessing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class SentimentAnalysis {

	private Properties props;
	private StanfordCoreNLP pipeline;
	
	public static void main(String args[]) {
		List<String[]> dataSet = readFromFile("../Data/Part2/cbc.csv");
		
		SentimentAnalysis analysis = new SentimentAnalysis();
		
		int titleIndex = 1;
		
		for(int i = 0; i < dataSet.size(); ++ i) {
			String[] item = dataSet.get(i);
			int sentimentValue = analysis.getSentimentValue(item[titleIndex]);
			ArrayList<String> newItem = new ArrayList<String>(Arrays.asList(item));
			int lastIndex = newItem.size()-1;
			String lastItem = newItem.get(lastIndex);
			newItem.remove(lastIndex);
			newItem.add(sentimentValue+"");
			newItem.add(lastItem);
			String[] newArray = new String[newItem.size()];
			dataSet.set(i, newItem.toArray(newArray));
		}
		
		writeToFile("cbc_sentiment.csv", dataSet);
		
	}
	
	/**
	 * Constructor
	 */
	public SentimentAnalysis() {
		props = new Properties();
        props.setProperty("annotators","tokenize, ssplit, pos, lemma, parse, sentiment");
        pipeline = new StanfordCoreNLP(props);
	}
	
	/**
	 * Get numeric value to represent sentiment from text
	 * @param text
	 * @return
	 */
	public int getSentimentValue(String text) {
        Annotation annotation = pipeline.process(text);
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
    	return getSentimentValue(sentences);
	}
	
    /**
     * Get numeric value to represent sentiment. 
     * @param sentences
     * @return
     */
    private static int getSentimentValue(List<CoreMap> sentences){
    	int currentValue = 0;
        for (CoreMap sentence : sentences) {
        	String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
        	if (sentiment.equalsIgnoreCase("Positive"))
        		currentValue += 1;
        	else if(sentiment.equalsIgnoreCase("Very Positive"))
        		currentValue += 2;
        	else if(sentiment.equalsIgnoreCase("Negative"))
        		currentValue += -1;
        	else if(sentiment.equalsIgnoreCase("Very Negative"))
        		currentValue += -2;
        }
        return currentValue;
    }
    
	public static List<String[]> readFromFile(String inputFile) {
		
		List<String[]> inputSet = null;
		CSVReader reader;
		
		try {
			reader = new CSVReader(new FileReader(inputFile));
			inputSet = reader.readAll();
			
		} catch (IOException e) {
			e.printStackTrace();

			reader = null;
			inputSet = null;
			
			System.err.println("Error reading file : " + inputFile);
		}
		return inputSet;
	}
	
	public static void writeToFile(String outputFileName, List<String[]> outputList ) {

		CSVWriter writer;
		
		try {
			writer = new CSVWriter(new FileWriter(outputFileName));
			writer.writeAll(outputList);
			
		} catch (IOException e) {
			e.printStackTrace();
			
			System.err.println("Error writing file : " + outputFileName);
		}
	}
}