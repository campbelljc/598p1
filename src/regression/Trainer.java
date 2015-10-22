package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;

public abstract class Trainer {
	
	protected double trainingError = -1;
	protected double validationError = -1;
	protected double accuracy = -1;
	
	protected abstract DoubleMatrix solve(DoubleMatrix matrixX, DoubleMatrix matrixY);
	
	/**
	 * Train on the whole data set
	 * @param dataSet
	 * @return
	 */
	public DoubleMatrix train(DataSet dataSet) {
		DoubleMatrix result = (null == dataSet ? null : solve(dataSet.features, dataSet.target));
		if (null == result)
			System.out.println("Invalid result!!");
		else {
			System.out.println("The weights are : ");
			String[] featureNames = dataSet.getFirstRow();
			featureNames[featureNames.length-1] = "ones";
			for (int i = 0; i < result.rows; i++)
			{
				System.out.println("Weight " + i + " : " + result.get(i, 0) + " (" + featureNames[i] + ")");
			}
	//		result.print();
			trainingError = leastSquareError(dataSet, result);
			accuracy = accuracy(dataSet, result, 100);
			System.out.println("Training Error : " + trainingError);
			System.out.println("Training Accuracy: " + accuracy);
		}
		System.out.println();
		return result;
	}
	
	/**
	 * Train with cross validation
	 * @param dataSetPairs
	 * @return
	 */
	public DoubleMatrix train(ArrayList<DataSetPair> dataSetPairs){
		if (null == dataSetPairs || dataSetPairs.size() < 2) {
			System.out.println("Invalid parameters!!");
			return null;
		}
		
		int kFold = dataSetPairs.size();
		double accumulativeTrainingError = 0;
		double accumulativeValidationError = 0;
		DoubleMatrix accumulativeWeightMatrix = null;
				
		for (int i = 0; i < kFold; i++) {
			DataSetPair pair = dataSetPairs.get(i);
			if (pair == null || pair.trainingSet == null || pair.validationSet == null) {
				System.out.println("Invalid parameters!!");
				return null;
			}
			DoubleMatrix weights = solve(pair.trainingSet.features, pair.trainingSet.target);
			
			if (weights == null) {
				System.out.println("Invalid result!!");
				return null;
			}
			
			if (accumulativeWeightMatrix == null)
				accumulativeWeightMatrix = weights;
			else
				accumulativeWeightMatrix.add(weights);
			
			double currentTrainingError  = leastSquareError(pair.trainingSet, weights);
			double currentValidationError  = leastSquareError(pair.validationSet, weights);
			System.out.print("Weights(" + i + ") : ");
			weights.print();
			System.out.println("Training Error (" + i + ") : " + currentTrainingError);	
			System.out.println("Validation Error (" + i + ") : " + currentValidationError);	
			
			accumulativeTrainingError += currentTrainingError;
			accumulativeValidationError += currentValidationError;
		}
		
		if (accumulativeWeightMatrix != null) {
			accumulativeWeightMatrix.div(kFold);
			accumulativeTrainingError /=  kFold;
			accumulativeValidationError /= kFold;
			
			trainingError = accumulativeTrainingError;
			validationError = accumulativeValidationError;
			
			System.out.println();
			System.out.print("Average Weights(" + kFold + " Fold) : ");
			accumulativeWeightMatrix.print();
			System.out.println("Average Training Error (" + kFold + " Fold) : " + accumulativeTrainingError);	
			System.out.println("Average Validation Error (" + kFold + " Fold) : " + accumulativeValidationError);	
		} else {
			System.out.println("Invalid result!!");
		}
		System.out.println();
		return accumulativeWeightMatrix;
	}
	
	/**
	 * Return the training error (read only, cannot be set externally)
	 * @return
	 */
	public double getTrainingError(){
		return trainingError;
	}
	
	/**
	 * Return the validation error (read only, cannot be set externally)
	 * @return
	 */
	public double getValidationError() {
		return validationError;
	}

	/**
	 * Return the Accuracy (read only, cannot be set externally)
	 * @return
	 */
	public double getAccuracy(){
		return accuracy;
	}

	/**
	 * Calculates least square error given the matrices and the weight vector
	 * @param matrixX 
	 * @param matrixY
	 * @param weightVector
	 * @return
	 */
	public static double leastSquareError(DoubleMatrix matrixX, DoubleMatrix matrixY, DoubleMatrix weightVector) {
		// Err(w) = (Y-Xw)T ( Y-Xw)
		if (null == matrixX || null == matrixY || null == weightVector)
			return -1.0;
		DoubleMatrix xw = matrixX.mmul(weightVector);
		DoubleMatrix yMinusXw = matrixY.sub(xw);
		DoubleMatrix result = yMinusXw.transpose().mul(yMinusXw);
		return result.sum() / result.columns;
	}
	
	/**
	 * Calculates least square error given the data set and the weight vector
	 * @param dataSet
	 * @param weightVector
	 * @return
	 */
	public static double leastSquareError(DataSet dataSet, DoubleMatrix weightVector){
		if (null == dataSet)
			return -1.0;
		return leastSquareError(dataSet.features, dataSet.target, weightVector);
	}

	/**
	 * Calculates the accuracy given the matrices, the weight vector and a precision
	 * @param matrixX 
	 * @param matrixY
	 * @param weightVector
	 * @param precision
	 * @return
	 */
	public static double accuracy(DoubleMatrix matrixX, DoubleMatrix matrixY, DoubleMatrix weightVector, double precision){
		if (null == matrixX || null == matrixY || null == weightVector)
			return -1.0;
		
		double result = 0;
		DoubleMatrix xw = matrixX.mmul(weightVector);
		DoubleMatrix yMinusXw = matrixY.sub(xw);
		for(int i=0; i<yMinusXw.length; i++)
		{
			if(Math.abs(yMinusXw.get(i)) <= precision)
				result++;
		}
		
		
		return result/yMinusXw.length;
	}
	
	/**
	 * Calculates accuracy given the data set, the weight vector and a precision
	 * @param dataSet
	 * @param weightVector
	 * @param precision
	 * @return
	 */
	public static double accuracy(DataSet dataSet, DoubleMatrix weightVector, double precision){
		if (null == dataSet)
			return -1.0;
		return accuracy(dataSet.features, dataSet.target, weightVector, precision);
	}

}
