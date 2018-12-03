package pws.AI;

public abstract class BaseAI {

	/**
	 * Give the AI an input and return the answer
	 * @param inputs The input to give to the AI
	 * @return the output of the AI
	 */
	public abstract float[] evaluate(float[] inputs);

    /**
     * Give the AI an input array and the outputs that should be returned with those inputs
     * @param inputs The inputs
     * @param outputs The expected outputs
     */
	public abstract void train(float[] inputs, float[] outputs);

    /**
     * Mutate the AI (For Genetic Algorithm)
     */
	public abstract void mutate();

    /**
     * Do a crossover with another AI (For Genetic Algorithm)
     * @param ai The other AI
     * @return A new AI
     */
	public abstract BaseAI crossOver(BaseAI ai);
}
