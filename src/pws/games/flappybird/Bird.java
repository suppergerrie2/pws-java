package pws.games.flappybird;

public class Bird {

	float y;
	float yVell;
	private final FlappyBird instance;
	public int points;
	
	public Bird(FlappyBird instance) {
		this.instance = instance;
		this.y = this.instance.width/2f;
		this.yVell = 0;
	}

	public void update() {
		this.yVell++;
		this.yVell = Math.min(this.yVell, 20);
		
		this.y += this.yVell;
		if(this.y < FlappyBird.BIRD_SIZE/2f) {
			this.y = FlappyBird.BIRD_SIZE/2f;
		} else if(this.y > instance.height - FlappyBird.BIRD_SIZE/2) {
			this.y = instance.height - FlappyBird.BIRD_SIZE/2f;
		}
	}
	
}
