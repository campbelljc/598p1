package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

public class ClosedForm extends Trainer {

	
	@Override
	protected DoubleMatrix solve(DoubleMatrix matrixX, DoubleMatrix matrixY) {
		// w = (X T X) −1 X T Y
		if (null == matrixX || null == matrixY)
			return null;
		DoubleMatrix matrixXTranspose = matrixX.transpose();
		DoubleMatrix xtx = matrixXTranspose.mmul(matrixX);
		DoubleMatrix xtxInverse = Solve.pinv(xtx);
		DoubleMatrix xty = matrixXTranspose.mmul(matrixY);
		DoubleMatrix weightVector = xtxInverse.mmul(xty);
		return weightVector;
	}

	@Override
	public DoubleMatrix train(DataSet dataSet) {
		System.out.println("Training with ClosedForm directly...");
		return super.train(dataSet);
	}

	@Override
	public DoubleMatrix train(ArrayList<DataSetPair> dataSetPairs) {
		System.out.println("Training with ClosedForm (kFold)...");
		return super.train(dataSetPairs);
	}
	
}
