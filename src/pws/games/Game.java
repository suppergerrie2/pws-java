package pws.games;

import java.awt.Graphics;
import java.awt.event.KeyListener;

import pws.AI.BaseAI;
import pws.trainingData.TrainingData;

public abstract class Game implements KeyListener {
	
	public boolean isRunning = false;
	
	public final TrainingData trainingData = new TrainingData();
	
	public abstract void update(BaseAI ai);
	
	public abstract void draw(Graphics g);

	public abstract int getInputSize();
	
	public abstract int getOutputSize();
	
	public void save() {
		//Not every game needs to save stuff (XOR for example)
	}

	public void reset() {
		//Not every game has to reset stuff (XOR for example)
	}

	public boolean playerWon() {
		return true;
	}

	public abstract Game newInstance();
}
