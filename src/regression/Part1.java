package regression;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import org.jblas.DoubleMatrix;

public class Part1 {

	/**
	 * The entry point for the main program.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {

		// Load training set data into matrices.
		// The first two columns are ignored
		DataSet data = DataSet.loadDataSet("../Data/Part1/trainingSet.csv", 2);
		DataSet testData = DataSet.loadDataSet("../Data/Part1/testSet.csv", 2);
		DoubleMatrix scaleVector = data.normaliseFeatures();
		testData.normaliseFeatures(scaleVector);

		// Initialise the trainers
		Trainer trainer;
		// ArrayList<Trainer> listOfTrainers = new ArrayList<Trainer>();
		ClosedForm closeFormTrainer = new ClosedForm();
		CloseFormRidge closeFormRidgeTrainer = new CloseFormRidge();

		GradientDescent gradientDescentTrainer = new GradientDescent();
		GradientDescentRidge gradientDescentRidgeTrainer = new GradientDescentRidge();
		IterativeLasso iterativeLassoTrainer = new IterativeLasso();
		double alphaValue = 0.000005;
		double epsilon = 1;
		DoubleMatrix initWeightVector = DoubleMatrix.zeros(data.features.columns, 1);
		gradientDescentTrainer.setParameters(alphaValue, epsilon, initWeightVector);
		gradientDescentRidgeTrainer.setParameters(alphaValue, epsilon, initWeightVector);
		iterativeLassoTrainer.setEpsilon(epsilon / 1000.0f);

		int trainerType = -1;
		Scanner scan = new Scanner(System.in);

		while (trainerType == -1) {

			System.out.println("Please choose a trainer: ");
			System.out.println("1- Closed-Form trainer");
			System.out.println("2- Closed-Form Ridge regularization trainer");
			System.out.println("3- Gradient Descent trainer");
			System.out.println("4- Gradient Descent Ridge Regularization trainer");
			System.out.println("5- Iterative Lasso trainer");

			trainerType = scan.nextInt();
			if (trainerType < 1 || trainerType > 5) {
				trainerType = -1;
				System.out.println("invalid trainer Type, please enter a valid integer number");
			}
		}

		/*
		 * listOfTrainers.add(closeFormTrainer);
		 * listOfTrainers.add(closeFormRidgeTrainer);
		 * listOfTrainers.add(gradientDescentTrainer);
		 * listOfTrainers.add(gradientDescentRidgeTrainer);
		 * listOfTrainers.add(iterativeLassoTrainer);
		 */
		String trainerUsed = null;

		switch (trainerType) {
		case 1:
			trainer = closeFormTrainer;
			trainerUsed = "Closed-Form Trainer";
			break;

		case 2:
			trainer = closeFormRidgeTrainer;
			trainerUsed = "Closed-Form Ridge regularization trainer";
			break;

		case 3:
			trainer = gradientDescentTrainer;
			trainerUsed = "Gradient Descent trainer";
			break;

		case 4:
			trainer = gradientDescentRidgeTrainer;
			trainerUsed = "Gradient Descent Ridge Regularization trainer";
			break;

		case 5:
			trainer = iterativeLassoTrainer;
			trainerUsed = "Iterative Lasso trainer";
			break;

		default:
			trainer = null;
			break;

		}

		/*
		 * // Train with the whole set of data for (int i = 0; i <
		 * listOfTrainers.size(); i++) { listOfTrainers.get(i).train(data); }
		 */
		// Split the data into k fold
		int kFold = 10;
		ArrayList<DataSetPair> splitResult = data.splitTrainingValidation(kFold);

		/*
		 * // Solve it with kFold for (int i = 0; i < listOfTrainers.size();
		 * i++) { listOfTrainers.get(i).train(splitResult); }
		 */
		// Solve with kFold
		trainer.train(splitResult);

		// Solve using the whole data set
		
		DoubleMatrix weights = trainer.train(data);
		double errorTrainingError = Trainer.leastSquareError(data, weights);
		System.out.println("The training error is : " + errorTrainingError);
		double error = Trainer.leastSquareError(testData, weights);
		System.out.println("The test error is : " + error);

		int toOutput = -1;

		while (toOutput == -1) {

			System.out.println("Would you like to output the result: ");
			System.out.println("2- Output Everything");
			System.out.println("1- Only Critical information");
			System.out.println("0- No");

			toOutput = scan.nextInt();
			if (toOutput < 0 || toOutput > 2) {
				toOutput = -1;
				System.out.println("invalid Answer, please enter a valid integer number");
			}
		}

		if (toOutput == 1 || toOutput == 2) {
			String filename = "results.txt";
			Path filepath = Paths.get(filename);

			try {
				if (!Files.exists(filepath))
					Files.createFile(filepath);

			} catch (IOException e) {
				System.err.println(e);
			}
			try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filepath.toString(), true)))) {

				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
				writer.println(timeStamp);
				writer.println("Trainer Used: " + trainerUsed);
				writer.println("Training Error: " + trainer.getTrainingError());
				writer.println("Validation Error: " + trainer.getValidationError());
				if (toOutput == 2) {
					writer.println("Number of Instances in training set: " + data.getLength());
					writer.println("Target Variable average: " + data.getAverage());
					writer.println("Target Variable Standard Deviation: " + data.getStandardDeviation());
					writer.println("Target Maximum is: " + data.getMax());
					writer.println("Target Minimum is: " + data.getMin());
				}

				writer.println();

			} catch (IOException e) {
				System.err.println(e);
			}

			System.out.println("Done");
		}

	}
}
