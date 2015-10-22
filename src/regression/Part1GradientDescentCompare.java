package regression;

import org.jblas.DoubleMatrix;

public class Part1GradientDescentCompare {

	/**
	 * The entry point for the main program.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		// Load training set data into matrices.
		// The first two columns are ignored
		DataSet data = DataSet.loadDataSet("../Data/Part1/trainingSet.csv", 2);
		
		GradientDescent trainer = new GradientDescent();
		
		DoubleMatrix initWeightVector = DoubleMatrix.zeros(data.features.columns, 1);
		double alphaValue = 0.0000000000000005;
		double epsilon = 0.0000001;
		trainer.setParameters(alphaValue, epsilon, initWeightVector);
		
		// Solve using the whole data set
		trainer.train(data);
		
		// Normalise the features
		data.normaliseFeatures();
		
		alphaValue = 0.000005;
		epsilon = 5;
		trainer.setParameters(alphaValue, epsilon, initWeightVector);
		
		trainer.train(data);
	}
}