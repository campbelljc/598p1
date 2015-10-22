package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;

public class Tester {

	/**
	 * The main function (entry point) for the Tester
	 * @param args
	 */
	public static void main(String args[]) {

		// Set up the trainers
		ArrayList<Trainer> listOfTrainers = new ArrayList<Trainer>();		
		ClosedForm closeFormTrainer = new ClosedForm();
		CloseFormRidge closeFormRidgeTrainer = new CloseFormRidge();
		GradientDescent gradientDescentTrainer = new GradientDescent();
		GradientDescentRidge gradientDescentRidgeTrainer = new GradientDescentRidge();
		IterativeLasso iterativeLassoTrainer = new IterativeLasso();
		
		DataSet data = DataSet.loadDataSet("../Data/Part1/testSet.csv", 2);
		
	//	double[] initialWeightVectorData = {0, 0};
	//	DoubleMatrix initialWeightVector = new DoubleMatrix(initialWeightVectorData);
		DoubleMatrix initialWeightVector = DoubleMatrix.zeros(data.features.columns, 1);
		double alphaValue = 0.01;
		double epsilon = 0.0001;
		gradientDescentTrainer.setParameters(alphaValue, epsilon, initialWeightVector);
		gradientDescentRidgeTrainer.setParameters(alphaValue, epsilon, initialWeightVector);
		iterativeLassoTrainer.setEpsilon(epsilon);
		
		listOfTrainers.add(closeFormTrainer);
		listOfTrainers.add(closeFormRidgeTrainer);
		listOfTrainers.add(gradientDescentTrainer);
		listOfTrainers.add(gradientDescentRidgeTrainer);
		listOfTrainers.add(iterativeLassoTrainer);
		
		// Test data from the slides
	//	double[][] matrixXData = { {0.86, 0.09, -0.85, 0.87, -0.44, -0.43, -1.1, 0.4, -0.96, 0.17}, {1, 1, 1, 1, 1, 1, 1, 1, 1, 1} };
	//	double[] matrixYData = { 2.49, 0.83, -0.25, 3.1, 0.87, 0.02, -0.12, 1.81, -0.83, 0.43 };
		
	//	DoubleMatrix matrixX = new DoubleMatrix(matrixXData).transpose();
	//	DoubleMatrix matrixY = new DoubleMatrix(matrixYData);
		
	//	DataSet data = new DataSet(matrixX, matrixY);
		
		// Solve directly
		for (int i = 0; i < listOfTrainers.size(); i++) {
			listOfTrainers.get(i).train(data);
		}
		
		// Split the data into k fold
		int kFold = 10;
		
		ArrayList<DataSetPair> splitResult = data.splitTrainingValidation(kFold);
		
		// Use kFold
		for (int i = 0; i < listOfTrainers.size(); i++) {
			listOfTrainers.get(i).train(splitResult);
		}
		
		// The expected solution
	//	System.out.println("And the weight vector given in the slides is : [1.60; 1.05].");
	}

}
