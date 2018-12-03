package pws.games.flappybird;

class Pipe {

	float x;
	@SuppressWarnings("CanBeFinal")
    float y;
	boolean counted = false;

	public Pipe(FlappyBird instance) {
		x = instance.width + FlappyBird.AVERAGE_DISTANCE + instance.random.nextInt(FlappyBird.MAX_DIFFERENCE)*2-FlappyBird.MAX_DIFFERENCE;
		y = instance.random.nextInt(instance.height - FlappyBird.HOLE_SIZE) + FlappyBird.HOLE_SIZE/2f;
	}
	
	public void update() {
		x-=(3);
	}
	
}
