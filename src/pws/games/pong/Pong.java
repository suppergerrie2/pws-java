package pws.games.pong;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import pws.Utils;
import pws.AI.BaseAI;
import pws.games.Game;
import pws.trainingData.InputOutputPair;
import pws.trainingData.TrainingData;

public class Pong extends Game {

	enum Direction { UP, STILL, DOWN }

	static final int PADDLE_WIDTH = 10;
	static final int PADDLE_HEIGHT = 50;
	static final int BALL_SIZE = 10;

	public final Paddle player;
	final Paddle enemy;
	public final Ball ball;

	final int width;
	final int height;

	private Direction playerMove = Direction.STILL;

	private boolean useAI = false;
	private boolean isRecording = false;

	private final TrainingData recordedData = new TrainingData();

	private final List<InputOutputPair> currentRecording = new ArrayList<>();

	private boolean lastPlayerWin = false;
	
	public Pong(int width, int height) {
		this(width, height, true);
	}
	
	private Pong(int width, int height, boolean loadData) {
		this.width = width;
		this.height = height;

		if(loadData) this.trainingData.readFromFile(Paths.get("saves/"), "pong");

		enemy = new Enemy(this);
		player = new Paddle(20, height/2, this);
		ball = new Ball(this);
	}

	@Override
	public void update(BaseAI ai) {
		if(useAI) {
			isRunning = false;
			isRecording = false;
		}

		if(!isRunning&&!useAI) return;
		
		if(useAI) {
			float[] outputs = ai.evaluate(new float[] {
					//					player.getX() / (float) width,
					player.getY() / (float) height,
					ball.getX() / (float) width,
					ball.getY() / (float) height,
					ball.getXVell() / 6f,
					ball.getYVell() / 6f
			});

			int move = Utils.oneHot(outputs);

			switch(move) {
			case 0:
				player.move(Direction.UP);
				break;
			case 1:
				player.move(Direction.STILL);
				break;
			case 2:
				player.move(Direction.DOWN);
				break;
			}

		} else {
			if(isRecording) {
				currentRecording.add(new InputOutputPair(new float[] {
						player.getY() / (float) height,
						ball.getX() / (float) width,
						ball.getY() / (float) height,
						ball.getXVell() / 6f,
						ball.getYVell() / 6f
				}, Utils.convertToOneHot(playerMove.ordinal(), 3)));
			}

			player.move(playerMove);
		}
		player.update();
		enemy.update();
		ball.update();
	}

	@Override
	public void draw(Graphics g) {
		g.setColor(isRecording?Color.RED:Color.WHITE);
		g.drawRect(player.getX() - PADDLE_WIDTH/2, player.getY() - PADDLE_HEIGHT/2, PADDLE_WIDTH, PADDLE_HEIGHT);
		g.drawRect(enemy.getX() - PADDLE_WIDTH/2, enemy.getY() - PADDLE_HEIGHT/2, PADDLE_WIDTH, PADDLE_HEIGHT);
		g.drawOval((int)ball.getX() - BALL_SIZE/2, (int)ball.getY() - BALL_SIZE/2, BALL_SIZE, BALL_SIZE);

		g.drawLine((int)ball.getX(), (int)ball.getY(), (int)(ball.getX() + ball.getXVell()*200), (int)(ball.getY() + ball.getYVell()*200));

		Utils.drawHorizontallyCenteredString(player.points + " - " + enemy.points, width, 30, g);
		
		if(useAI) Utils.drawHorizontallyCenteredString("AI MODE", width, 50, g); 
		if(!isRunning) Utils.drawCenteredString("Press up or down to start playing!", width, height, g);
	}

	public void reset(boolean playerWon) {
		if(playerWon) {
			player.points++;
			this.recordedData.addData(this.currentRecording);
			this.currentRecording.clear();
		} else {
			enemy.points++;
		}
		
		lastPlayerWin = playerWon;

		isRunning = false;

		player.reset();
		enemy.reset();
		ball.reset();
	}

	@Override
	public void reset() {
		player.reset();
		enemy.reset();
		ball.reset();
		player.points = enemy.points = 0;
		lastPlayerWin = false;
		currentRecording.clear();
		
		isRunning = false;
		
		playerMove = Direction.STILL;
	}
	
	//Did the player win in the last game?
	@Override
	public boolean playerWon() {
		return lastPlayerWin;
	}

	@Override
	public void save() {
		this.recordedData.writeToFile(Paths.get("saves/"), "pong");
	}

	@Override
	public int getInputSize() {
		return 5;
	}

	@Override
	public int getOutputSize() {
		return 3;
	}
	

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP:
		case KeyEvent.VK_W:
			playerMove = Direction.UP;
			isRunning = true;
			break;
		case KeyEvent.VK_DOWN:
		case KeyEvent.VK_S:
			playerMove = Direction.DOWN;
			isRunning = true;
			break;
		}
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_R) {
			isRecording = !isRecording;
		} else if (e.getKeyCode() == KeyEvent.VK_T) {
			useAI = !useAI;
		} else {		
			playerMove = Direction.STILL;
		}
	}

	@Override
	public Game newInstance() {
		return new Pong(this.width, this.height, false);
	}
}