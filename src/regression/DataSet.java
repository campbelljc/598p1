package regression;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jblas.DoubleMatrix;

import com.opencsv.CSVReader;

public class DataSet {

	public DoubleMatrix features;
	public DoubleMatrix target;
	private double targetMean;
	private double targetStd;
	public String filePath;
		
	/**
	 * Private constructor to prevent instantiating a DataSet object without calling "loadDataSet"
	 */
	private DataSet(){}
	
	/**
	 * Initiate DataSet with the two matrices
	 * @param matrixX
	 * @param matrixY
	 */
	public DataSet(DoubleMatrix matrixX, DoubleMatrix matrixY) {
		features = matrixX;
		target = matrixY;
	}
	
	public DoubleMatrix normaliseFeatures(){
		if (features == null || target == null)
			return null;
		DoubleMatrix scaleVector = DoubleMatrix.ones(features.columns, 1);
		
		for(int i = 0; i < features.columns; ++i) {
			DoubleMatrix column = features.getColumn(i);
			double max = column.max();
			double min = column.min();
			
			double scaleValue = Math.max(Math.abs(max), Math.abs(min));
			if (scaleValue <= 0.00000001)
				continue;
			
			column = column.div(scaleValue);
			features.putColumn(i, column);
			scaleVector.put(i, scaleValue);
		}
		System.out.println("Data set scaled by : ");
		scaleVector.print();
		System.out.println();
		return scaleVector;
	}
	
	public void normaliseFeatures(DoubleMatrix scaleVector){
		if (features == null || target == null)
			return;
		
		for(int i = 0; i < features.columns; ++i) {
			DoubleMatrix column = features.getColumn(i);

			double scaleValue = scaleVector.get(i);
			if (scaleValue <= 0.00000001)
				continue;
			column = column.div(scaleValue);
			features.putColumn(i, column);
			scaleVector.put(i, scaleValue);
		}
		System.out.println("Data set scaled by : ");
		scaleVector.print();
		System.out.println();
	}
		
	/**
	 * Split the data with k fold and into a training set and a validation set
	 * @param kFold
	 * @return
	 */
	public ArrayList<DataSetPair> splitTrainingValidation(int kFold){
		if (features == null || target == null) {
			System.out.println("Invalid data!");
			return null;			
		}
		
		if (kFold < 2) {
			System.out.println("Invalid number for k!");
			return null;
		}
		
		ArrayList<DataSetPair> result = new ArrayList<DataSetPair>(kFold);
		
		int numEntries = features.rows;
		int numFeatures = features.columns;
		int numEntriesPerSection = numEntries / kFold;
		
		int numEntriesValidationSet = numEntriesPerSection;
		int numEntriesTrainingSet = numEntries - numEntriesValidationSet;
		
		for (int i = 0; i < kFold; i++) {
			
			DoubleMatrix trainingSetFeatures = new DoubleMatrix(numEntriesTrainingSet, numFeatures);
			DoubleMatrix trainingSetTarget = new DoubleMatrix(numEntriesTrainingSet, 1);
		
			DoubleMatrix validationSetFeatures = new DoubleMatrix(numEntriesValidationSet, numFeatures);
			DoubleMatrix validationSetTarget = new DoubleMatrix(numEntriesValidationSet, 1);
			
			// Start and end index for the validation set
			int startIndex = i * numEntriesPerSection;
			int endIndex = startIndex + numEntriesPerSection;
			
			// Set the validation data sets
			for (int j = 0; j < numEntriesValidationSet; j ++) {
				validationSetFeatures.putRow(j, features.getRow(startIndex + j));
				validationSetTarget.putRow(j, target.getRow(startIndex + j));
			}
			
			
			// Set the training set data
			int currentIndex = 0;
			for (int j = 0; j < numEntries; j ++) { // for each example that we want to put in each training set...
				
				// If the index in in the range of the validation set
				if (j >= startIndex && j < endIndex  ) {
					continue;
				}

				trainingSetFeatures.putRow(currentIndex, features.getRow(j));
				trainingSetTarget.putRow(currentIndex, target.getRow(j));
				currentIndex++;
			}
			
			DataSetPair newPair = new DataSetPair();
			newPair.trainingSet = new DataSet();
			newPair.validationSet = new DataSet();


			newPair.trainingSet.features = trainingSetFeatures;
			newPair.trainingSet.target = trainingSetTarget;
			
			newPair.validationSet.features = validationSetFeatures;
			newPair.validationSet.target = validationSetTarget;
			
			result.add(newPair);		
		}		
		return result;		
	}
	
	/**
	 * @param filename: Path of CSV file to load (Relative to Eclipse project directory.)
	 * @param col: Skip the first 'col' number of columns.
	 */
	public static DataSet loadDataSet(String filename, int columnsToSkip) {		
		// CSVReader gives List<String>
		// DoubleMatrix takes double[][] or can concat double[]

		// Open file
		CSVReader reader;
		try {
			reader = new CSVReader(new FileReader(filename));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Can't find the CSV file.");
			return null;
		}

		// Read file into a list of Double[]
		List<Double[]> doubleArrayList = new ArrayList<Double[]>();
		try {
			String[] nextLine;
			int currentIndex = 0;
			while ((nextLine = reader.readNext()) != null) {
				++currentIndex;
				Double[] temp = new Double[nextLine.length - columnsToSkip];
				for (int i = columnsToSkip; i < nextLine.length; i++) {
					try{
						temp[i - columnsToSkip] = Double.parseDouble(nextLine[i]);
					} catch(java.lang.NumberFormatException e) {
						//System.out.println(e + " on line " + currentIndex);
					}
				}
				doubleArrayList.add(temp);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Close file.
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Convert Double[] to double[] and add to data matrix.
		// Length-1 because we omit the last column (target).
		DoubleMatrix data = new DoubleMatrix(doubleArrayList.size(), doubleArrayList.get(0).length - 1);
		DoubleMatrix target = new DoubleMatrix(doubleArrayList.size(), 1);
		for (int i = 0; i < doubleArrayList.size(); i++) {
			double[] tempArray = new double[doubleArrayList.get(i).length - 1];
			for (int j = 0; j < doubleArrayList.get(i).length - 1; j++) {
				tempArray[j] = doubleArrayList.get(i)[j];
			}

			// Add target value to matrix.
			target.put(i, 0, doubleArrayList.get(i)[doubleArrayList.get(i).length - 1]);

			// Turn the double[] into a one-row DoubleMatrix and add that row to
			// the data matrix.
			DoubleMatrix rowMatrix = new DoubleMatrix(tempArray);
			data.putRow(i, rowMatrix);
//			System.out.println(data.getRow(i).toString());
		}
		
		DoubleMatrix columnOne = DoubleMatrix.ones(data.getRows());
		data = DoubleMatrix.concatHorizontally(data, columnOne);
	//	for (int i = 0; i < data.rows; i++)
	//		System.out.println(data.getRow(i).toString());

		
		System.out.println("Done loading.");
		
		DataSet result = new DataSet();
		result.features = data;
		result.target = target;
		result.average();
		result.standardDeviation();
		result.filePath = filename;
		return result;
	}

	public String[] getFirstRow()
	{
		// Open file
		CSVReader reader;
		try {
			String temp = filePath.substring(0, filePath.lastIndexOf('/'));
			String path = temp + "/complete.csv";
			reader = new CSVReader(new FileReader(path));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Can't find the CSV file.");
			return new String[] { };
		}
		
		String[] nextLine;
		try {
			nextLine = reader.readNext();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		// Close file.
		try {
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String[] features = new String[nextLine.length-2];
		for (int i = 2; i < nextLine.length; i++)
		{
			features[i-2] = nextLine[i];
		}
		
		return features;
	}
	
	
	public double getAverage(){
		return targetMean;
	}

	public double getStandardDeviation(){
		return targetStd;
	}
	
	public double getLength(){
		return target.getLength();
	
	}
	
	public double getMax(){
		return target.max();
	}
	
	public double getMin(){
		return target.min();
	}
	
	public void average(){
		int length = target.getLength();
		double sum = 0;
		for(int i = 0; i < length; i++)
		{
			sum += target.get(i);
		}
		
		targetMean = sum/length;
	}
	
	public void standardDeviation(){
		int length = target.getLength();
		double sum = 0;
		for(int i = 0; i < length; i++)
		{
			sum += Math.pow((target.get(i)- targetMean), 2);
		}
		
		targetStd =  Math.sqrt(sum/length);
	}
	
	
}




class DataSetPair{
	public DataSet trainingSet = null;
	public DataSet validationSet = null;
}
