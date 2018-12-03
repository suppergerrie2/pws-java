package pws;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;

import javax.swing.JFrame;

import pws.AI.BaseAI;
import pws.AI.NeuralNetwork.NeuralNetwork;
import pws.AI.pool.Pool;
import pws.games.Game;
import pws.games.XOR.XOR;
import pws.games.flappybird.FlappyBird;
import pws.games.pong.Pong;
import pws.trainingData.InputOutputPair;

public class Main extends Canvas implements Runnable, KeyListener {

	private static final long serialVersionUID = 1L;
	private static JFrame frame;
	private Thread thread;
	private boolean running = false;
	
	public static int width = 800;
	@SuppressWarnings("SuspiciousNameCombination")
	public static int height = width;
	
	private final Game runningGame;
	
	private final BaseAI ai;

    /**
     * Arguments can be:
     * game=one of following values xor, flappy_bird, flappybird and pong (default=pong)
     * width=positive integer values (default=800)
     * height=positive integer values (default=800)
     * ai with=one of following values pool, ga, geneticalgorithm, nn, neuralnetwork (default=nn)
     * ai_type=same as ai
     * layers=an list of numbers in the following format: a,b,c (Numbers with a , as divider)
     * @param args The programs arguments as described above
     */
	public static void main(String[] args) {

		//Read the arguments
		String gameName = "";
		String aiType = "";
		int[] layers = new int[] {5,5};

		for(String s : args) {

		    //Arguments are in a key=value format, so we split to get the key
			String[] argParts = s.split("=");
			
			if(argParts.length>2) {
				System.out.println("Please only use 1 = for each argument!");
				continue;
			}

			//The key determines what we do
			switch(argParts[0]) {
            //gameName is later used to instantiate either Pong, FlappyBird or XOR
			case "game":
				gameName = argParts[1];
				break;

            //Width and height of the window
			case "width":
				width = Integer.parseUnsignedInt(argParts[1]);
				break;
			case "height":
				height = Integer.parseUnsignedInt(argParts[1]);
				break;
            //ai or ai_type can both be used
            //This is used to later determine what ai to instantiate. This can be the neuralnetwork with gradient descent. Or the genetic algorithm
            case "ai":
			case "ai_type":
				aiType = argParts[1];
				break;
            //Change the layers from the neural network
			case "layers":
				String[] layerValues = argParts[1].split(",");
				layers = new int[layerValues.length];
				
				for(int i = 0; i < layerValues.length; i++) {
					layers[i] = Integer.parseUnsignedInt(layerValues[i]);
				}
				
				break;
			}
				
		}

		Game game;
		BaseAI ai;

		switch(gameName.toLowerCase()) {
		case "xor":
			game = new XOR(100);
			break;
		case "flappy_bird":
		case "flappybird":
			game = new FlappyBird(width, height);
			break;
		case "pong":
		default:
			game = new Pong(width, height);
			break;
		}
		
		switch(aiType.toLowerCase()) {
		case "pool":
		case "ga":
		case "geneticalgorithm":
			ai = new Pool(layers, 2000, game);
			break;
		case "nn":
		case "neuralnetwork":
		default:
			ai = new NeuralNetwork(game.getInputSize(), game.getOutputSize(), layers).randomise();
			break;
		}

		//Create the frame and the main instance
		Main main = new Main(game, ai);

		//Make the frame not resizable and set the titel
		frame.setResizable(false);
		frame.setTitle("PWS");

		//Make main be able to draw on the frame
		frame.add(main);
		frame.pack();

		//By default we hide it, this is so we can run some code before the program stops
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		//Center it in the window and set it visible
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

		//Add a listener to call main.stop when the frame is closed
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e)
		    {
				main.stop();
		    }
		});

		//Start!
		main.start();
	}

    /**
     * The main class is the main controller for everything.
     * From here the update and render methods are called.
     *
     * @param game The game to run
     * @param ai The ai type to use
     */
	private Main(Game game, BaseAI ai) {
	    //Make sure the frame is the right size and request focus so we can listen to keyboard input
		setPreferredSize(new Dimension(width,height));
		this.setFocusable(true);
		frame = new JFrame();
		//Both Main and the game have to know when the user presses a key.
		this.addKeyListener(this);
		runningGame = game;
		this.addKeyListener(runningGame);
		
		this.ai = ai;
	}

    /**
     * Starts the game thread
     */
	private synchronized void start() {
		running = true;
		thread = new Thread(this);
		thread.start();
	}

    /**
     * Save the game and makes everything stop.
     */
	private synchronized void stop() {
		running = false;
		runningGame.save();
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	//Updates Per Second. Should be 60 in a normal run but can be higher when superspeed is enabled
	private int ups = 0;
	//Frames Per Second. Can be as high as the cpu/gpu can do
	private int fps = 0;
	//Makes the updates not wait but run whenever possible
	private boolean superSpeed = false;
	
	@Override
	public void run() {
	    //lastTime we did the while loop
		long lastTime = System.nanoTime();
		//How long 1 update should take
		double updateTime = 1000000000.0 / 60;
		//When this is 1 we need another update, if this is 0.5 we need to wait 0.5*updateTime
		double delta = 0;

		//LastTime since the ups and fps update
		long lastTimeMS = System.currentTimeMillis();

		//Count how often we have updated and rendered
		int upsCounter = 0;
		int fpsCounter = 0;
		
		while(running) {
			long now = System.nanoTime();

			//Reset delta to 20 if it gets to big, makes sure we dont forget to render if the update is too slow
			if(delta>20) delta = 20;

			//If we have superspeed enabled we set delta to 1, else we calculate the fraction of updateTime we have waited
			if(!superSpeed) {
				delta += (now - lastTime) / updateTime; 
			} else {
				delta = 1;
			}
			
			lastTime = now;

			//As long as delta is >= to 1 we need to update
			while(delta >= 1) {
				upsCounter++;
				update();
				delta--;
			}
			
			fpsCounter++;
			render();

			//Every second we update the fps and ups.
			if(System.currentTimeMillis()-lastTimeMS>=1000) {
				lastTimeMS = System.currentTimeMillis();
				fps = fpsCounter;
				ups = upsCounter;
				upsCounter = 0;
				fpsCounter = 0;
			}
		}
	}

    /**
     * Update the game and run the train method from the AI
     */
	private void update() {
		runningGame.update(ai);

		for(int i = 0; i < 100; i++) {
			if(runningGame.trainingData.hasData()) {
				InputOutputPair pair = runningGame.trainingData.getRandomDataPoint();
				ai.train(pair.inputs, pair.outputs);
			}
		}
	}

    /**
     * Reset the background and call the draw method from the runningGame
     */
	private void render() {

		BufferStrategy bs = this.getBufferStrategy();
		if(bs == null) {
			this.createBufferStrategy(3);
			return;
		}
		
		Graphics g = bs.getDrawGraphics();

		//Make the background black
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		runningGame.draw(g);

		//Show the fps and ups
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.PLAIN, 20));
		g.drawString("FPS: " + fps, 0, height);
		g.drawString("UPS: " + ups, 0, height-20);
		
		g.dispose();
		bs.show();
	}

	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
        //ctrl+shif+s will toggle superSpeed
		if(e.isControlDown()&&e.isShiftDown()&&e.getKeyCode()==KeyEvent.VK_S) {
			superSpeed = !superSpeed;
		}
	}
}