package regression;

import java.util.ArrayList;

import org.jblas.DoubleMatrix;

public class GradientDescentRidge extends GradientDescent{

	
	private double lambda = 0.1;
	
	/**
	 * Set the lambda value
	 * @param lamda
	 */
	public void setLambda(double lamda) {
		this.lambda = lamda;
	}
	
	@Override
	protected DoubleMatrix gradient(DoubleMatrix matrixX, DoubleMatrix matrixY, DoubleMatrix weightVector ) {
		// ∂Err(w)/∂w = 2(XTXw – XTY)
		// ∂Err(w)/∂w = −2XT (Y − Xw) + 2λw = 2(XTXw – XTY) + 2λw
		DoubleMatrix matrixXTranspose = matrixX.transpose();
		DoubleMatrix xtx = matrixXTranspose.mmul(matrixX);
		DoubleMatrix xtxw = xtx.mmul(weightVector);
		DoubleMatrix xty = matrixXTranspose.mmul(matrixY);
		DoubleMatrix result = xtxw.sub(xty).mul(2.0);
		result = result.add(weightVector.mul(lambda*2));
		return result;		
	}
	
	@Override
	public DoubleMatrix train(DataSet dataSet) {
		System.out.println("Training with GradientDescent (Ridge) directly...");
		return super.train(dataSet);
	}

	@Override
	public DoubleMatrix train(ArrayList<DataSetPair> dataSetPairs) {
		System.out.println("Training with GradientDescent (Ridge) (kFold)...");
		return super.train(dataSetPairs);
	}
}
