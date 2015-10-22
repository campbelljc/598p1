package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;
import org.jblas.MatrixFunctions;
import org.jblas.Solve;

public class IterativeLasso extends Trainer{
	
	private double lambda = 0.1;
	private double epsilon = 1;
	
	/**
	 * Set the lambda value
	 * @param lamda
	 */
	public void setLambda(double lamda) {
		this.lambda = lamda;
	}
	
	/**
	 * Set the epsilon value
	 * @param epsilon
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
		
	@Override
	public DoubleMatrix train(DataSet dataSet) {
		System.out.println("Training with IterativeLasso directly...");
		return super.train(dataSet);
	}

	@Override
	public DoubleMatrix train(ArrayList<DataSetPair> dataSetPairs) {
		System.out.println("Training with IterativeLasso (kFold)...");
		return super.train(dataSetPairs);
	}
	
	/**
	 * Find the initial weight vector using Ridge Regression
	 * @param matrixX
	 * @param matrixY
	 * @return
	 */
	private DoubleMatrix findInitVector(DoubleMatrix matrixX, DoubleMatrix matrixY) {
		// Without Regularization : w = (XT X)−1 X T Y
		// With    Regularization : w = (XT X + λI)−1 XT Y
		if (null == matrixX || null == matrixY)
			return null;
		DoubleMatrix xt = matrixX.transpose();
		DoubleMatrix xtx = xt.mmul(matrixX);
		DoubleMatrix identityMatrix = DoubleMatrix.eye(xtx.rows);
		DoubleMatrix xtxLambdaI = xtx.add(identityMatrix.mul(lambda));
		DoubleMatrix xtxLambdaIInverse = Solve.pinv(xtxLambdaI);
		DoubleMatrix xty = xt.mmul(matrixY);
		DoubleMatrix weightVector = xtxLambdaIInverse.mmul(xty);
		return weightVector;
	}
	
	private DoubleMatrix getNewWeights(DoubleMatrix xtx, DoubleMatrix xty, DoubleMatrix weights) {
		//w new = (XTX + λdiag(|w|)−1 )−1  XT y
		DoubleMatrix diagWeights = MatrixFunctions.abs(DoubleMatrix.diag(weights));
		DoubleMatrix diagWeightsInverse = Solve.pinv(diagWeights);
		
		DoubleMatrix result = xtx.add(diagWeightsInverse.mul(lambda));
		result = Solve.pinv(result);
		result = result.mmul(xty);
		return result;
	}

	@Override
	protected DoubleMatrix solve(DoubleMatrix matrixX, DoubleMatrix matrixY) {
		DoubleMatrix currentWeights = findInitVector(matrixX, matrixY);
		
		DoubleMatrix xt = matrixX.transpose();
		DoubleMatrix xtx = xt.mmul(matrixX);
		DoubleMatrix xty = xt.mmul(matrixY);
		
		int iterationCount = 0;
		int numIterationLimit = 100000;
		
		while( true) {

			DoubleMatrix newWeights = getNewWeights(xtx, xty, currentWeights);
				
			double weightDiffSquared = currentWeights.squaredDistance(newWeights);
			
			if (weightDiffSquared < epsilon * epsilon) {
				break;
			}			
			
			//currentError = newError;
			currentWeights = newWeights;
					
//			// Check error and weights every 1000 iterations
//			if (iterationCount % 1000 == 0) {	
//				//double errorDiff = currentError - newError;
//				System.out.print("Current Weights : ");
//				currentWeights.print();
//				double currentError = leastSquareError(matrixX, matrixY, newWeights);
//				System.out.println("Current Error : " + currentError);
//				System.out.println("Change in Weight (Squared) : " + weightDiffSquared);
//			}
			
			iterationCount++;
			
			if (iterationCount > numIterationLimit) {
				System.out.println("Iterative Lasso does not return after " + iterationCount + " iterations.");
				return currentWeights;
			}
		}
		System.out.println("Iterative Lasso returns after " + iterationCount + " iterations.");
		return currentWeights;
	}
}
