package pws.AI.NeuralNetwork;

import java.util.Random;
import java.util.function.Function;

import org.ejml.data.MatrixType;
import org.ejml.simple.SimpleMatrix;

/**
 * SimpleMatrix with some helpful methods
 */
public class ExtendedMatrix extends SimpleMatrix {

	private static final long serialVersionUID = -2676717466637594577L;

	ExtendedMatrix(int r, int c) {
		super(r, c);
	}

	ExtendedMatrix(int i) {
		this(i, 1);
	}

	public ExtendedMatrix(double[][] ds) {
		super(ds);
	}
	
	private ExtendedMatrix(int numRows, int numCols, MatrixType type) {
		super(numRows, numCols, type);
	}

	@Override
	protected ExtendedMatrix createMatrix(int numRows, int numCols, MatrixType type) {
	    return new ExtendedMatrix(numRows, numCols, type);
	}

    /**
     * Randomise the matrix with values between -1 and 1
     * @param random The random object to use
     * @return this
     */
	private ExtendedMatrix randomise(Random random) {
		for(int i = 0; i < this.getNumElements(); i++) {
			this.set(i, random.nextDouble()*2-1);
		}
		
		return this;
	}

    /**
     * Randomize the matrix with values between -1 and 1.
     * A new random object is created without any arguments.
     * @return this
     */
	@SuppressWarnings("UnusedReturnValue")
    ExtendedMatrix randomise() {
		return this.randomise(new Random());
	}

    /**
     * Run a function for every value in the matrix.
     * @param func The funtion to run for every value.
     * @return A new matrix with the function applied to every value of this matrix
     */
	ExtendedMatrix apply(Function<Float, Float> func) {
		ExtendedMatrix matrix = new ExtendedMatrix(this.numRows(), this.numCols());
		
		for(int i = 0; i < this.getNumElements(); i++) {
			matrix.set(i, func.apply((float) this.get(i)));
		}
		
		
		return matrix;
	}

    /**
     * Create a matrix from an array.
     * @param input The array to convert to a matrix
     * @return The matrix created from the array
     */
	static ExtendedMatrix fromArray(float[] input) {
		ExtendedMatrix matrix = new ExtendedMatrix(input.length);
		
		for(int i = 0; i < input.length; i++) {
			matrix.set(i, input[i]);
		}
		
		return matrix;
	}

}
