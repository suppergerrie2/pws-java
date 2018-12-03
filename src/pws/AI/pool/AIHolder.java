package pws.AI.pool;

import pws.AI.BaseAI;

/**
 * This class is holds an ai together with the fitness
 */
public class AIHolder implements Comparable<AIHolder> {

	final BaseAI ai;
	private float fitness;
	float normalizedFitness;

	private float topFitness = 0;

	//If it is stale it will not get bred.
    //Staleness increases every gen unless it got better
	int staleness;

    /**
     * Create a new AIHolder
     * @param ai The ai it should hold
     */
	AIHolder(BaseAI ai) {
		this.ai = ai;
	}


    /**
     * Compare the holder to another holder, this will sort the holders based on fitness
     * @param o The other AIHolder
     * @return -1 if the fitness is smaller than the other holder, 0 if same, 1 if the fitness is bigger than the other holder
     */
	@Override
	public int compareTo(AIHolder o) {
		if(o.fitness>fitness) {
			return -1;
		} else if (o.fitness<fitness) {
			return 1;
		}
		
		return 0;
	}

    /**
     * Set the fitness for this holder, will also update the staleness and topfitness
     * @param fitness The new fitness
     */
    void setFitness(float fitness) {
	    if(fitness>topFitness) {
	        topFitness = fitness;
	        staleness = 0;
        } else {
	        staleness++;
        }

        this.fitness = fitness;
    }

    float getFitness() {
	    return fitness;
    }

    /**
     * Make the fitness between 0 and 1
     * @param maxFitness The highest fitness from this generation
     */
	void normalizeFitness(float maxFitness) {
		normalizedFitness = fitness/maxFitness;
	}

    /**
     * Mutate the ai this holder holds.
     * @return A new AIHolder with the mutated ai.
     */
	AIHolder mutate() {
		this.ai.mutate();
		AIHolder newHolder = new AIHolder(this.ai);
		newHolder.topFitness = topFitness;
		newHolder.staleness = staleness;
		return newHolder;
	}

    /**
     * Makes the ai this holderholds crossover with the other AI.
     * @param parent2 The other parent
     * @return a new aiholder with the result
     */
	public AIHolder crossOver(AIHolder parent2) {
        AIHolder newHolder = new AIHolder(ai.crossOver(parent2.ai));
        newHolder.topFitness = topFitness;
        newHolder.staleness = staleness;
		return newHolder;
	}
	
}
