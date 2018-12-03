package pws.games.XOR;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;

import pws.Main;
import pws.AI.BaseAI;
import pws.games.Game;

public class XOR extends Game {

	private final int resolution;

	private final float[][] values;

	public XOR(int resolution) {
		super();

		this.resolution = resolution;
		values = new float[resolution][resolution];

		for(int i = 0; i <= 1; i++) {
			for(int j = 0; j <= 1; j++) {
				trainingData.addData(new float[] { i,j }, new float[] { i^j });
			}	
		}
	}

	@Override
	public void update(BaseAI ai) {
		for(int x = 0; x < resolution; x++) {
			for(int y = 0; y < resolution; y++) {
				values[x][y] = ai.evaluate(new float[] {x/(float)resolution, y/(float)resolution})[0]; 
			}
		}
		isRunning = false;
	}

	@Override
	public void draw(Graphics g) {
		int width = Main.width/resolution;
		int height = Main.height/resolution;

		for(int x = 0; x < resolution; x++) {
			for(int y = 0; y < resolution; y++) {
				g.setColor(new Color(values[x][y], 0, 0));
				g.fillRect(x*width, y*width, width, height);
			}
		}
	}

	@Override
	public int getInputSize() {
		return 2;
	}

	@Override
	public int getOutputSize() {
		return 1;
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public Game newInstance() {
		return new XOR(this.resolution);
	}
}
