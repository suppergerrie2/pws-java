package pws.AI.NeuralNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pws.AI.BaseAI;

@SuppressWarnings("ConstantConditions")
public class NeuralNetwork extends BaseAI {

    //How many there are neurons in each layer
	private final int[] layers;

	//How many input and output neurons this network has
	private final int inputCount;
	private final int outputCount;

	//The matrices with the weight and biases
	public final List<ExtendedMatrix> weights = new ArrayList<>();
	public final List<ExtendedMatrix> bias = new ArrayList<>();

	//Random object for mutation and crossover
	private final Random random = new Random();

    /**
     * Create a new neural network. The weights and biases are all 0
     * @param inputCount The number of input neurons
     * @param outputCount The number of output neurons
     * @param hiddenLayers An array with an index i saying how many neurons are at layer i+1 (Input is layer 0)
     */
	public NeuralNetwork(int inputCount, int outputCount, int[] hiddenLayers) {
		this.inputCount = inputCount;
		this.outputCount = outputCount;
		this.layers = hiddenLayers;

		//Each matrix has the weights going from one layer to the next
		this.weights.add(new ExtendedMatrix(hiddenLayers[0], inputCount));
		this.bias.add(new ExtendedMatrix(hiddenLayers[0]));

		//Create all of the weights for the hidden layers
        for(int i = 1; i < hiddenLayers.length; i++) {
            this.weights.add(new ExtendedMatrix(hiddenLayers[i], hiddenLayers[i-1]));
            this.bias.add(new ExtendedMatrix(hiddenLayers[i]));
        }

        //Create the last weights for the last layer
        this.weights.add(new ExtendedMatrix(outputCount, hiddenLayers[hiddenLayers.length-1]));
        this.bias.add(new ExtendedMatrix(outputCount));
	}

    /**
     * Randomize the weights and biases
     * @return this
     */
	public NeuralNetwork randomise() {
		for(ExtendedMatrix matrix : weights) {
			matrix.randomise();
		}
		
		for(ExtendedMatrix matrix : bias) {
			matrix.randomise();
		}
		
		return this;
	}

    /**
     * Get the answer the network produces for a given input
     * @param input An array with the data for the network to use. Array should be the same length as the input cout
     * @return An array with the same size as the outputCount and the values the network produced.
     */
	public float[] evaluate(float[] input) {
		if(input.length != this.inputCount) {
			throw new IllegalArgumentException(String.format("Invalid input! Size expected: %d. Got size %d", this.inputCount, input.length));
		}

		//Create a matrix from the input array
		ExtendedMatrix inputMatrix = ExtendedMatrix.fromArray(input);

		ExtendedMatrix output = null;
		for(int i = 0; i < this.weights.size(); i++) {
		    //Multiply the input/outputs from the previous layer with the weights
			ExtendedMatrix weightInput = (ExtendedMatrix) this.weights.get(i).mult(inputMatrix);

			//No we add the bias
			ExtendedMatrix preActivation = (ExtendedMatrix) weightInput.plus(this.bias.get(i));

			//And finally we apply the activation function
			output = preActivation.apply(NeuralNetwork::activationFunction);

			//This could be written as activation(weights*input + bias)
            //Where weights is the weight matrix for this layer
            //Input is for the first layer the input for this method, else it is the output of the last layer.
            //The bias is the bias matrix for this layer

            //Set the inputMatrix to the output of this layer so we can use it in the next layer
			inputMatrix = output;
		}

		//Convert the matrix to an array
		float[] outputs = new float[output.getNumElements()];
		
		for(int i = 0; i < output.getNumElements(); i++) {
			outputs[i] = (float) output.get(i);
		}
 		
		return outputs;
	}

    /**
     * Train the network using gradient descent and backpropagation
     * @param input The input
     * @param target The target output for the given input
     */
	public void train(float[] input, float[] target) {
		if(input.length!=this.inputCount) {
			throw new IllegalArgumentException(String.format("Invalid input! Size expected: %d. Got size %d", this.inputCount, input.length));
		}
		
		if(target.length!=this.outputCount) {
			throw new IllegalArgumentException(String.format("Invalid target! Size expected: %d. Got size %d", this.outputCount, target.length));
		}

		//Convert the input and targets to a matrix
		ExtendedMatrix inputMatrix = ExtendedMatrix.fromArray(input);
		ExtendedMatrix targetMatrix = ExtendedMatrix.fromArray(target);

		//We need to keep track of the weightedSums (The value just before the activation function) and the neuronOutputs
		List<ExtendedMatrix> weightedSums = new ArrayList<>();
		List<ExtendedMatrix> neuronOutputs = new ArrayList<>();

		//Same algorithm as evaluate
		ExtendedMatrix output;
		for(int i = 0; i < this.weights.size(); i++) {
			ExtendedMatrix weightInput = (ExtendedMatrix) this.weights.get(i).mult(inputMatrix);

			ExtendedMatrix preActivation = (ExtendedMatrix) weightInput.plus(this.bias.get(i));

			//We need to save this for Gradient descent
			weightedSums.add(preActivation);

			output = preActivation.apply(NeuralNetwork::activationFunction);

			//this also needs to be saved
			neuronOutputs.add(output);
			
			inputMatrix = output;
		}

		//The errors calculated which will be used to determine how much to change the weight
		ExtendedMatrix[] layerErrors = new ExtendedMatrix[this.weights.size()];

		//From here we will run the gradient descent algorithm with help from backpropagation (http://neuralnetworksanddeeplearning.com/chap2.html#warm_up_a_fast_matrix-based_approach_to_computing_the_output_from_a_neural_network)
		for(int i = this.weights.size()-1; i>=0; i--) {

		    //The output layer uses a different calculation
			if(i==this.weights.size()-1) {
			    //We calculate how close the value was to the target
				ExtendedMatrix a = (ExtendedMatrix) neuronOutputs.get(i).minus(targetMatrix);

				//Here we calculate how fast the activation would change for the given value, which will be used to determine how much to change the weight
				ExtendedMatrix b = weightedSums.get(i).apply(NeuralNetwork::dActivationFunction);

				//Calculate the error
				layerErrors[i] = (ExtendedMatrix) a.elementMult(b);
			} else {

			    //Here we backpropagate the error.
				ExtendedMatrix transposed = (ExtendedMatrix) this.weights.get(i+1).transpose();
				ExtendedMatrix multipliedErrors = (ExtendedMatrix) transposed.mult(layerErrors[i+1]);

				ExtendedMatrix d = weightedSums.get(i).apply(NeuralNetwork::dActivationFunction);

				layerErrors[i] = (ExtendedMatrix) multipliedErrors.elementMult(d);
			}
		}

		//Apply the errors to the current weights
		for(int l = 0; l < this.weights.size(); l++) {
			for(int row = 0; row < this.weights.get(l).numRows(); row++) {
				for(int col = 0; col < this.weights.get(l).numCols(); col++) {
					float activation;

					//First layer uses the input, the other layers use the outputs from the previous layers
					if (l == 0) {
                        activation = (float) input[col];
                    } else {
                        activation = (float) neuronOutputs.get(l - 1).get(col);
                    }

                    float error = (float) layerErrors[l].get(row);
					float gradient = activation * error;

					float currentValue = (float) this.weights.get(l).get(row, col);
					//We substract the gradient because the gradient says how to go up, but we want to go down
					this.weights.get(l).set(row, col, currentValue-gradient);
                }
			}
		}

		//Apply the errors to the biases
		for(int i = 0; i < this.bias.size(); i++) {
			ExtendedMatrix biasGradient = layerErrors[i];
			this.bias.set(i, (ExtendedMatrix) this.bias.get(i).minus(biasGradient));
		}
	}

    /**
     * The activation function, currently the sigmoid function
     * @param f The input to the activation function
     * @return The output from the activation function
     */
	private static float activationFunction(float f) {
		return (float) (1/(1+Math.exp(-f)));
	}

    /**
     * The derivative of the activation function
     * @param f The input of the function
     * @return The output of the function
     */
	private static float dActivationFunction(float f) {
		return activationFunction(f)*(1-activationFunction(f));
	}

    /**
     * Mutate the network, 70% chance for each weight that it will mutate
     *     - When a weight mutates it has a 1% chance to be set to a value between -1 and 1
     *     - When it doesn't get reset it will increase by a value between -0.1 to 0.1
     * Each bias also has a 40# chance to mutate, when it mutates the same thing can happen as the weight
     */
	@Override
	public void mutate() {
		for(ExtendedMatrix m : this.weights) {
			m.apply((v)->{

			    //70% chance to mutate
				if ( random.nextFloat()<0.7) {

				    //And a 10%
					if(random.nextFloat()<0.1) {
						return random.nextFloat() *2 - 1;
					}

					return v + (random.nextFloat()*0.2f-0.1f);
				} else {
					return v;
				}
			});
		}
		
		for(ExtendedMatrix m : this.bias) {
			m.apply((v)->{
				if ( random.nextFloat()<0.4) {
					if(random.nextFloat()<0.1) {
						return random.nextFloat() *2 - 1;
					}
					
					return v + (random.nextFloat()*0.2f-0.1f);
				} else {
					return v;
				}
			});
		}
	}

    /**
     * Do a crossover with another ai. Will randomly choose from which AI the child will get the weight or bias
     * @param ai The other AI to do a crossover with
     * @return a new NeuralNetwork
     */
	@Override
	public BaseAI crossOver(BaseAI ai) {
	    //Make sure it is a neuralnetwork
		if(ai instanceof NeuralNetwork) {

			NeuralNetwork other = (NeuralNetwork) ai;
			NeuralNetwork network = new NeuralNetwork(this.inputCount, this.outputCount, this.layers);
			for(int i = 0; i < this.weights.size(); i++) {

			    //50% chance to choose a weight from this network, and 50% chance to get the weight from the other network
				if(random.nextBoolean()) {
					network.weights.set(i, this.weights.get(i));
				} else {
					network.weights.set(i, other.weights.get(i));
				}
			}
			
			for(int i = 0; i < this.bias.size(); i++) {
				if(random.nextBoolean()) {
					network.bias.set(i, this.bias.get(i));
				} else {
                    network.bias.set(i, other.bias.get(i));
                }
			}
		
			return network;
		} else {
			throw new IllegalArgumentException("NeuralNetwork can only breed with a neuralnetwork!");
		}
	}
}
