package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class CloseFormRidge extends Trainer {
	
	private double lambda = 0.1;
	
	/**
	 * Set the lambda value
	 * @param lamda
	 */
	public void setLambda(double lamda) {
		this.lambda = lamda;
	}
	
	@Override
	protected DoubleMatrix solve(DoubleMatrix matrixX, DoubleMatrix matrixY) {
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

	@Override
	public DoubleMatrix train(DataSet dataSet) {
		System.out.println("Training with ClosedForm (Ridge) directly...");
		return super.train(dataSet);
	}

	@Override
	public DoubleMatrix train(ArrayList<DataSetPair> dataSetPairs) {
		System.out.println("Training with ClosedForm (Ridge) (kFold)...");
		return super.train(dataSetPairs);
	}
}
