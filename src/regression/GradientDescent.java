package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;

public class GradientDescent extends Trainer{
	
	private double learningRateAlpha = 0.05;
	private double epsilon = 0.1;
	private DoubleMatrix initWeights = null;
	
	/**
	 * Set parameters for this specific kind of training approach
	 * @param learningRateAlpha
	 * @param epsilon
	 * @param initWeights
	 */
	public void setParameters(double learningRateAlpha, double epsilon, DoubleMatrix initWeights) {
		this.learningRateAlpha = learningRateAlpha;
		this.epsilon = epsilon;
		this.initWeights = initWeights;
	}
	

	/**
	 * Calculate the gradient given the X, Y matrices and a weightVector
	 * @param matrixX
	 * @param matrixY
	 * @param weightVector
	 * @return
	 */
	protected DoubleMatrix gradient(DoubleMatrix matrixX, DoubleMatrix matrixY, DoubleMatrix weightVector ) {
		// ∂Err(w)/∂w = 2(XTXw – XTY)		
		DoubleMatrix matrixXTranspose = matrixX.transpose();
		DoubleMatrix xtx = matrixXTranspose.mmul(matrixX);
		DoubleMatrix xtxw = xtx.mmul(weightVector);
		DoubleMatrix xty = matrixXTranspose.mmul(matrixY);
		DoubleMatrix result = xtxw.sub(xty).mul(2.0);
		return result;		
	}
	
	@Override
	protected DoubleMatrix solve(DoubleMatrix matrixX, DoubleMatrix matrixY) {
		if (matrixX == null || matrixY == null || initWeights == null) {
			System.err.println("Invalid matrices!");
			return null;
		}
		
		if (epsilon < 0 || learningRateAlpha < 0) {
			System.err.println("Invalid parameters!");
			return null;
		}
		
		DoubleMatrix currentWeights = initWeights;
		DoubleMatrix currentGradient = null;	
		
		int iterationCount = 0;
		
		int numIterationLimit = 10000000;
		
		// Use adaptive learning rate
		double currentLearningRate = learningRateAlpha;
		
		double currentError = leastSquareError(matrixX, matrixY, currentWeights);
		
		// Check if the learning rate is too large
		while( true) {
			currentGradient = gradient(matrixX, matrixY, currentWeights);
			
			DoubleMatrix newWeights = currentWeights.sub( currentGradient.mul(currentLearningRate));
			
			double newError = leastSquareError(matrixX, matrixY, newWeights);
			
			// New error bigger than old error, learning rate too large
			if (newError > currentError) {
				currentLearningRate /= 1.05f;
				//System.out.println("Learning rate reduced to " + currentLearningRate + "!");
			} else {
				break;
			}
		}
		
		while( true) {
			currentGradient = gradient(matrixX, matrixY, currentWeights);
			
			DoubleMatrix newWeights = currentWeights.sub( currentGradient.mul(currentLearningRate));
				
			double weightDiffSquared = currentWeights.squaredDistance(newWeights);
			
			if (weightDiffSquared < epsilon * epsilon) {
				break;
			}			
			
			//currentError = newError;
			currentWeights = newWeights;
					
//			// Check error and weights every 1000 iterations
//			if (iterationCount % 10 == 0) {	
//				currentError = leastSquareError(matrixX, matrixY, newWeights);
//				System.out.println("("+ iterationCount + ", " + currentError / 100000000 + ")" );
//			}
			
			iterationCount++;
			
			if (iterationCount > numIterationLimit) {
				System.out.println("Gradient Descent does not return after " + iterationCount + " iterations.");
				return currentWeights;
			}
		}
		System.out.println("Gradient Descent returns after " + iterationCount + " iterations.");
		return currentWeights;
	}

	@Override
	public DoubleMatrix train(DataSet dataSet) {
		System.out.println("Training with GradientDescent directly...");
		return super.train(dataSet);
	}

	@Override
	public DoubleMatrix train(ArrayList<DataSetPair> dataSetPairs) {
		System.out.println("Training with GradientDescent (kFold)...");
		return super.train(dataSetPairs);
	}
	
}
