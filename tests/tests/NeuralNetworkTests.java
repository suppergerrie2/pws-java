package tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import pws.AI.NeuralNetwork.ExtendedMatrix;
import pws.AI.NeuralNetwork.NeuralNetwork;

class NeuralNetworkTests {

	@Test
	void NeuralNetworkSingleLayer() {
		NeuralNetwork network = new NeuralNetwork(2, 2, new int[] {2});
		
		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.5f, 0.5f}, 0.001f);

		network.weights.set(0, (ExtendedMatrix) network.weights.get(0).plus(new ExtendedMatrix(new double[][] {{1,2},{0,0}})));
		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.5f, 0.5f}, 0.001f);
		
		network.weights.set(1, (ExtendedMatrix) network.weights.get(1).plus(new ExtendedMatrix(new double[][] {{2,1},{0,0}})));
		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.9231980886476312f, 0.5f}, 0.00001f);
	}
	
	@Test
	void NeuralNetworkMultipleLayers() {
		NeuralNetwork network = new NeuralNetwork(2,1, new int[] {3, 2});

		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.5f}, 0.001f);

		network.weights.set(0, (ExtendedMatrix) network.weights.get(0).plus(new ExtendedMatrix(new double[][] {{1,2},{3,4},{5,6}})));
		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.5f}, 0.001f);
	
		network.weights.set(1, (ExtendedMatrix) network.weights.get(1).plus(new ExtendedMatrix(new double[][] {{1,2,3}, { 4, 5, 6}})));
		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.5f}, 0.001f);
		
		network.weights.set(2, (ExtendedMatrix) network.weights.get(2).plus(new ExtendedMatrix(new double[][] {{1,2}})));

		checkArray(network.evaluate(new float[] {1,2}), new float[] {0.9524615147104792f}, 0.001f);
	}
	
	@Test
	void RandomiseWeightsAndBias() {
		NeuralNetwork network = new NeuralNetwork(2, 2, new int[] {2});
		
		network.randomise();
		
		assertTrue(network.weights.get(0).get(0)!=network.weights.get(0).get(2));
		assertTrue(network.bias.get(0).get(0)!=network.bias.get(0).get(1));
	}

	@Test
	void TestExceptions() {
		NeuralNetwork network = new NeuralNetwork(2, 1, new int[] {2});
		assertThrows(IllegalArgumentException.class, ()->network.evaluate(new float[] {2}), "Evaluate should throw error with not enough inputs");
		assertThrows(IllegalArgumentException.class, ()->network.train(new float[] {2}, new float[] {1}), "Input size is 1, train should expect size of 2");
		assertThrows(IllegalArgumentException.class, ()->network.train(new float[] {2,1}, new float[] {1,2}), "Output size is 2, train should expect size of 1");
	}
	
	private static void checkArray(float[] value, float[] expected, float delta) {
		assertEquals(value.length, expected.length, "Array size is " + value.length + " while " + expected.length + " was expected!");
		
		for(int i = 0; i < value.length; i++) {
			assertEquals(value[i], expected[i], delta, "Expected " + expected[i] + " with delta " + delta +  " but got " + value[i]);
		}
	}

}
