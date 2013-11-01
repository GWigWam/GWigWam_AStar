package pack;

public class Block {
	
	int x;
	int y;
	boolean wall;
	boolean start;
	boolean end;
	float[] color;
	int gScore;
		
	
	public Block(int x, int y, boolean wall, float[] color){
		this.x = x;
		this.y = y;
		this.wall = wall;
		this.start = false;
		this.end = false;
		this.color = color;
		this.gScore = 0;
	}

}
