package pws.games.flappybird;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import pws.Utils;
import pws.AI.BaseAI;
import pws.games.Game;
import pws.trainingData.InputOutputPair;
import pws.trainingData.TrainingData;

@SuppressWarnings("ConstantConditions")
public class FlappyBird extends Game {

	static final int BIRD_SIZE = 25;
	static final int HOLE_SIZE = 300;
	private static final int PIPE_WIDTH = 50;
	static final int AVERAGE_DISTANCE = 500;
	static final int MAX_DIFFERENCE = 100;

	final Random random = new Random(0);
	final int width;
	final int height;

	public final Bird bird;
	private boolean dead = false;

	private final List<Pipe> pipes = new ArrayList<>();

	private boolean isRecording = false;
	private final TrainingData recordedData = new TrainingData();
	private final List<InputOutputPair> currentRecording = new ArrayList<>();

	private boolean isPressingSpace = false;
	private int jumpDelay = 0;

	private boolean useAI = true;

	public FlappyBird(int width, int height) {
		this(width, height, true);
	}

	private FlappyBird(int width, int height, boolean loadData) {

		this.width = width;
		this.height = height;

		bird = new Bird(this);

		if(loadData) this.trainingData.readFromFile(Paths.get("saves/"), "flappyBird");
	}

	private Pipe debugPipe;

	@Override
	public void update(BaseAI ai) {

		if(this.isRunning||useAI) {
			if(pipes.size()==0||pipes.get(pipes.size()-1).x < width) {
				pipes.add(new Pipe(this));
			}
			Pipe closestPipe = null;
			float minDist = Float.MAX_VALUE;
			for(Pipe pipe : pipes) {
				if(pipe.x<width/2-BIRD_SIZE/2) continue;

				float dist = pipe.x - width/2f-BIRD_SIZE;
				if(dist < minDist) {
					minDist = dist;
					closestPipe = pipe;
				}
			}

			debugPipe = closestPipe;
			
			if(isRecording&&!useAI) {


				this.currentRecording.add(new InputOutputPair(new float[] {
						bird.y,
						bird.yVell,
						closestPipe.y
				}, Utils.convertToOneHot(isPressingSpace?1:0, 2)));
			}

			if(useAI) {
				float[] output = ai.evaluate(new float[] {
						bird.y,
						bird.yVell,
						closestPipe.y
				});

				this.isPressingSpace = Utils.oneHot(output) == 1;
			}

			if(!dead) {
				for(Pipe pipe : pipes) {
					pipe.update();


					//We are entering the pipe section
					if(pipe.x-PIPE_WIDTH/2 < width/2 + BIRD_SIZE/2 && pipe.x+PIPE_WIDTH/2 > width/2 - BIRD_SIZE/2) {
						if(bird.y > pipe.y + HOLE_SIZE/2 || bird.y < pipe.y - HOLE_SIZE/2) {
							dead = true;
							this.currentRecording.clear();
							isPressingSpace = false;
						} else if(!pipe.counted) {
							pipe.counted = true;
							bird.points++;

							this.recordedData.addData(this.currentRecording);
							this.currentRecording.clear();
						}
					}
				}

				if(isPressingSpace&&jumpDelay<=0) {
					jumpDelay = 15;
					bird.yVell = -15;
				} else if (jumpDelay>0) {
					jumpDelay--;
				}

				bird.update();

				if(dead) {
					reset();
				}
			}

			pipes.removeIf((pipe)->pipe.x<-PIPE_WIDTH);
		}
	}

	@Override
	public void reset() {
		dead = false;
		bird.y = height/2f;
		pipes.clear();
		bird.points = 0;
		this.currentRecording.clear();
		this.isRunning = false;
	}

	@Override
	public void draw(Graphics g) {

		g.setColor(Color.blue);
		g.fillRect(0, 0, width, height);

		for(Pipe pipe : pipes) {
			g.setColor(Color.green);
			g.fillRect((int)(pipe.x-PIPE_WIDTH/2), 0, PIPE_WIDTH, (int)(pipe.y-HOLE_SIZE/2));
			g.fillRect((int)(pipe.x-PIPE_WIDTH/2), (int)(pipe.y+HOLE_SIZE/2), PIPE_WIDTH, height);
			g.setColor(Color.RED);
			//noinspection SuspiciousNameCombination
			g.fillOval((int)(pipe.x-PIPE_WIDTH/2), (int)(pipe.y-PIPE_WIDTH/2), PIPE_WIDTH, PIPE_WIDTH);
		}

		g.setColor(dead?Color.RED:this.isRecording?Color.MAGENTA:Color.YELLOW);
		g.fillOval(width/2 - BIRD_SIZE/2, (int) (bird.y - BIRD_SIZE/2), BIRD_SIZE, BIRD_SIZE);

		if(debugPipe!=null) {
			g.drawLine(width/2, (int)bird.y, (int)debugPipe.x, (int)debugPipe.y);
		}

		g.setColor(Color.white);
		Utils.drawHorizontallyCenteredString("Points: " + bird.points, width, 30, g);
		if(useAI) Utils.drawHorizontallyCenteredString("AI MODE", width, 50, g); 
	}

	@Override
	public void save() {
		this.recordedData.writeToFile(Paths.get("saves/"), "flappyBird");
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {

		if(this.useAI) return;
		if(e.getKeyCode()==KeyEvent.VK_SPACE) {
			isPressingSpace = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {

		if(e.getKeyCode()==KeyEvent.VK_R) {
			isRecording = !isRecording;
		} else if (e.getKeyCode() == KeyEvent.VK_T) { 
			this.useAI = !this.useAI;
		} else if (!this.useAI) {
			if(e.getKeyCode()==KeyEvent.VK_SPACE) {
				isPressingSpace = false;
				this.isRunning = true;
			}
		}
	}

	@Override
	public int getInputSize() {
		return 3;
	}

	@Override
	public int getOutputSize() {
		return 2;
	}

	@Override
	public Game newInstance() {
		return new FlappyBird(this.width, this.height, false);
	}

}
