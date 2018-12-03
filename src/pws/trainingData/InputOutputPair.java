package pws.trainingData;

/**
 * Class contains the inputs and expected outputs
 */
public class InputOutputPair {
	
	public final float[] inputs;
	public final float[] outputs;

	public InputOutputPair(float[] inputs, float[] outputs) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

    /**
     * Converts the inputOutputPair into a string, the numbers have a space to seperate them and a | is used to seperate the inputs and outputs
     * @return The string representing the input output pair
     */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for(float f : inputs) {
			builder.append(f);
			builder.append(" ");
		}
		
		builder.append(" | ");
		
		for(float f : outputs) {
			builder.append(f);
			builder.append(" ");
		}
		
		return builder.toString();
	}

    /**
     * Read an inputOutputPair from a string
     * @param string The string which represents the inputoutputpair
     * @return A new inputOutpuPair
     */
	static InputOutputPair fromString(String string) {
		String[] inputs = string.split(" \\| ")[0].split(" ");
		String[] outputs = string.split(" \\| ")[1].split(" ");
		
		float[] inputArray = new float[inputs.length];
		for(int i = 0; i < inputs.length; i++) {
			inputArray[i] = Float.parseFloat(inputs[i]);
		}
		
		float[] outputArray = new float[outputs.length];
		for(int i = 0; i < outputs.length; i++) {
			outputArray[i] = Float.parseFloat(outputs[i]);
		}
		
		return new InputOutputPair(inputArray, outputArray);
	}
	
}
