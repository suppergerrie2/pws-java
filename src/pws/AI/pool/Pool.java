package pws.AI.pool;

import pws.AI.BaseAI;
import pws.AI.NeuralNetwork.NeuralNetwork;
import pws.games.Game;
import pws.games.flappybird.FlappyBird;
import pws.games.pong.Pong;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The genetic algorithm.
 * This class is thrown together and contains some things that aren't really good but I ran out of time for this class
 */
public class Pool extends BaseAI implements Runnable {

    private final int populationCount;
    private final Random random = new Random();

    private final Game game;

    private final int inputs;
    private final int outputs;
    private final int[] layers;

	private List<AIHolder> aiHolders = new ArrayList<>();
	private AIHolder topAIHolder;
    private int generation = 0;
	private final List<Game> games = new ArrayList<>();

    /**
     * Create a new pool with networks to train.
     * @param layers The amount of neurons in each layer of the neural network
     * @param populationCount The amount of networks in the pool
     * @param game The game to train on (Pong and FlappyBird are supported)
     */
    public Pool(int[] layers, int populationCount, Game game) {
        this.inputs = game.getInputSize();
        this.outputs = game.getOutputSize();

        //Create and randomise neural networks
		for(int i = 0; i < populationCount; i++) {
			aiHolders.add(new AIHolder(new NeuralNetwork(inputs, outputs, layers).randomise()));
		}

		this.layers = layers;
		
		this.populationCount = populationCount;
		this.game = game;

		//Start a new thread to do the generations on, makes sure the game wont crash
		new Thread(this).start();
	}

	@Override
	public void run() {
		//noinspection InfiniteLoopStatement
		while(true) {
		    //Do a generation and output the best fitness
	        doGen();
            if(this.generation%10000==0) {
			    System.out.println(topAIHolder.getFitness());
		    }
        }
    }

    /**
     * Does a generation, the workload is spread over x threads.
     * In ever generation the networks play the game and get a fitness score based on that
     * The fitness score will determine how likely they are to be bred and/or mutated.
     */
	private void doGen() {
	    //Create a ExecutorService to spread the load
        int threadCount = 4;
        ExecutorService es = Executors.newFixedThreadPool(threadCount);

		for(int i = 0; i < this.aiHolders.size(); i++) {
			AIHolder aiHolder = this.aiHolders.get(i);
			//Every AI needs its own game
			if(this.games.size()-1<i) this.games.add(game.newInstance());
			es.execute(new TrainingThread(this.games.get(i), aiHolder));
		}

		//Wait for every AI to finish
		es.shutdown();
		try {
			es.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		//Find the highest fitness from this generation
		float maxFitness = Float.MIN_VALUE;
		for(AIHolder holder : aiHolders) {
		    if(holder.getFitness() >maxFitness) {
		        maxFitness = holder.getFitness();
            }

			if(topAIHolder == null || holder.getFitness() > topAIHolder.getFitness()) {
				topAIHolder = holder;
			}
		}
		
		generation++;

		//Make sure the list is sorted
		aiHolders.sort(Collections.reverseOrder());

		//Normalize (Make every fitness between 0 and 1)
		float totalWeight = 0;
		for(AIHolder holder : aiHolders) {
		    holder.normalizeFitness(maxFitness);
		    totalWeight+= holder.normalizedFitness;
        }

        //This list will contain the new aiholders
		List<AIHolder> newHolders = new ArrayList<>();


		for(int n = 0; n < populationCount; n++) {

		    //1% chance that a totally random network is created
		    if(random.nextFloat()<0.01) {
                aiHolders.add(new AIHolder(new NeuralNetwork(inputs, outputs, layers).randomise()));
            } else {
		        //Get a random parent, the chance it is chosen depends on the fitness
                AIHolder parent = getRandomByWeight(aiHolders, totalWeight);

                //If the staleness > 15 create a new holder
                if(parent.staleness>15) {
                    parent = new AIHolder(new NeuralNetwork(inputs, outputs, layers).randomise());
                }

                //50% chance the parent will mutate
                if (random.nextFloat() < 0.5) {
                    parent = parent.mutate();
                }

                //20% chance the parent will do a crossover
                if (random.nextFloat() < 0.2) {
                    AIHolder parent2 = getRandomByWeight(aiHolders, totalWeight);
                    parent = parent.crossOver(parent2);
                }


                newHolders.add(parent);
            }
        }

		this.aiHolders = newHolders;
	}

    /**
     * Get a random holder based on its fitness
     * @param holders The holders to choose from
     * @param totalWeight The total fitness
     * @return A holder
     */
	private AIHolder getRandomByWeight(List<AIHolder> holders, float totalWeight) {
        double randomNumber = random.nextDouble() * totalWeight;

		for (AIHolder holder : holders) {
			randomNumber -= holder.normalizedFitness;
			if (randomNumber <= 0) {
				return holder;
			}
		}

        return holders.get(0);
    }
			
	@Override
	public float[] evaluate(float[] inputs) {
		//Let the best AI we've had determine what to do
		if(topAIHolder != null) return topAIHolder.ai.evaluate(inputs);

		return new float[outputs];
	}

    //We need to implement these methods because we extend BaseAI
	@Override
	public void train(float[] inputs, float[] outputs) {
	    //Training happens on another thread
	}

	@Override
	public void mutate() {}

	@Override
	public BaseAI crossOver(BaseAI ai) { return this; }

}

/**
 * This class is used to run the game, and will thus run on another thread.
 */
class TrainingThread implements Runnable {

	private final Game game;
	private final AIHolder holder;
	
	TrainingThread(Game game, AIHolder holder) {
		this.game = game;
		this.holder = holder;
	}
	
	@Override
	public void run() {

		float fitness = 0;

		//We run the game 10 times
        for(int run = 0; run < 10; run++) {

            //Make sure the game is reset first
			this.game.reset();

			//Then we start the game
			this.game.isRunning = true;

			//We keep track of the updates so we can kill the game if it runs for too long
			int updates = 0;

			//Keep running this until the game ends or we've done 36000 updates
			do {
			    //Update the game!
				this.game.update(holder.ai);

				//Depending on the game we either just increase the fitness by a default value
				if(this.game instanceof FlappyBird) {
                    fitness+=0.01;
                } else if (this.game instanceof Pong) {
				    //Or we give more score the closer the bat is to the ball
				    Pong pong = (Pong)this.game;

				    float yDelta = pong.player.y - pong.ball.y;
				    float xDelta = pong.player.x - pong.ball.x;

				    float distanceToBall = yDelta*yDelta+xDelta*xDelta;

				    if(distanceToBall<5f) {
				        fitness += 100;
                    } else {
                        fitness += (5 / (distanceToBall * distanceToBall));
                    }
                }

                updates++;

			} while (this.game.isRunning&&updates<36000);

			//FlappyBird gets 100 points for every point it got. Other games get 500 points if they won the game
			if(this.game instanceof FlappyBird) {
                    FlappyBird flappyBird = (FlappyBird)game;
                    fitness+=flappyBird.bird.points*100;
            } else {
                if (this.game.playerWon()) {
                    fitness+=500;
                }
            }
		}

		holder.setFitness( fitness);
	}
	
}