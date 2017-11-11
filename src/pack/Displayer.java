package pack;

//le impor

import java.util.Random;

import org.lwjgl.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;

public class Displayer {
	
	final int screenWidth = 600;
	final int screenHeight = 600;
	final int blockSize = 4;
	final int calcSpeed = 10; //0 = complete path every frame, 1 = once per frame, 2 = every other frame etc: 0 IS SPECIAL! 0 solves it instanly, while other numbers solve it in VISIBLE steps
	
	boolean computedStartBlock = false;
	
		
	// Create vars DON'T YET DEFINE VALUE
	int frameNumber;
	int FPS;
	long time;
	long thinkTime;
	boolean drawNRS;
	boolean pathFound;
	boolean allDone;
		
	Block startBlock = null;
	Block endBlock = null;
	
	Block[] openList = new Block[(screenHeight / blockSize) + (screenWidth / blockSize) * 10];
	Block[] closedList = new Block[(screenHeight / blockSize) * (screenWidth / blockSize)];
	Block[] pathList = new Block[(screenHeight / blockSize) + (screenWidth / blockSize) * 10];
	
	Block[] blokken = new Block[(screenHeight / blockSize) * (screenWidth / blockSize)];
	
	Displayer(){ // CONSTRUCTOR
		//System.setProperty("org.lwjgl.librarypath",System.getProperty("user.dir") + "\\lwjgl-2.8.5\\native\\windows");
		initGL(screenWidth, screenHeight);
		load();
		startDisplayLoop();
	}
	
	private void initGL(int width, int height){
			System.out.println("initGL started");
		try {
    		Display.setDisplayMode(new DisplayMode(width,height));
    		Display.create();
    		System.out.println("initGL 1th part succes!");
		} catch (LWJGLException e){
			System.out.println("Error in the try in initGL");
			e.printStackTrace();
			System.exit(0);
		}
 
		// init OpenGL
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0, width, 0, height, 1, -1);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		
		/*GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);*/
    }	
	
	private void load(){
		frameNumber = 0;
		FPS = 0;
		thinkTime = 0;
		pathFound = true; // becomes false when world is loaded
		drawNRS = false;
		allDone = false;
		
		int c = 0;
		
		for(int i = 0; i < (screenWidth/blockSize); i++){
			for(int j = 0; j < (screenHeight/blockSize); j++){
				
				Block block = new Block(i*blockSize, j*blockSize, false, randomColorArray());
				//System.out.println("Created block NR: "+ c+" @ ("+block.x+", "+block.y+") ");
				
				while(block.color[0] + 0.1 < block.color[1]){
					block.color[0]+=0.1;
					block.color[1]-=0.1;
				}
				
				while(block.color[1] + 0.1 < block.color[0]){
					block.color[1]+=0.1;
					block.color[0]-=0.1;
				}
				
				while(block.color[0] + block.color[1] + block.color[2] > 2.2){
					block.color[0]-= 0.05;
					block.color[1]-= 0.05;
					block.color[2]-= 0.1;
				}
				
				while(block.color[0] + block.color[1] + block.color[2] < 1.8){
					block.color[0]+= 0.05;
					block.color[1]+= 0.05;
					block.color[2]+= 0.01;
				}	
				
				block.color[2] = 0;
				
				blokken[c] = block;
				
				c++;
			}
		}
	}
	
	private Block getBlockAtCoords(int x, int y){
		try{
			for(int i = 0; i < blokken.length; i++){
				if(x >= blokken[i].x && x < blokken[i].x + blockSize && y >= blokken[i].y && y < blokken[i].y + blockSize){
					return blokken[i];
				}
			}
			//System.out.println("Block at ("+x+", "+y+") not found!");
			return null;
		}catch(Exception e){
			return null;
		}
	}
	
	private float[] randomColorArray(){
		float[] array = new float[3];
		
		array[0] = (float) Math.random();
		array[1] = (float) Math.random();
		array[2] = (float) Math.random();
		
		return array;
	}

	
	private void startDisplayLoop(){
		while(!Display.isCloseRequested()){
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			aStar();
			
			if(calcSpeed == 0){
				while(!pathFound){
					aStar();
				}
			}
			
			checkInput();
			drawStuff();
			
			endThisLoop();
		}
		Display.destroy();
	}
	

	private void endThisLoop(){
		//FPS meter
		if (time <= System.currentTimeMillis() - 1000){
			Display.setTitle("Finding paths @ " + frameNumber + " FPS"); //Normal
			FPS = frameNumber;
			frameNumber = 0;
			time = System.currentTimeMillis();
		} else {
			frameNumber++;
		}
		if(calcSpeed != 0){
			for(int c = 0; c < calcSpeed; c++){
				aStar();
			}
		}
		
		Display.update();
		Display.sync(60);
	}
	

	private void checkInput(){
		if(Mouse.isButtonDown(0)){
			leftHold(Mouse.getX(), Mouse.getY());
		}
		while(Mouse.next()){
			if(Mouse.getEventButtonState() && Mouse.getEventButton() == 0){
				leftClick(Mouse.getX(), Mouse.getY());
			}
			
			if(Mouse.getEventButtonState() && Mouse.getEventButton() == 1){
				rightClick(Mouse.getX(), Mouse.getY());
			}			
		}
		while(Keyboard.next()){
			if(Keyboard.getEventKey() == Keyboard.KEY_ESCAPE){
				System.exit(0);
			}
			
			if(Keyboard.getEventKey() == Keyboard.KEY_SPACE){
				if(drawNRS){
					drawNRS = false;
				}else{
					drawNRS = true;
				}
			}
			
			if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Keyboard.KEY_R){
				if(startBlock == null){
					double tmp = ((screenWidth/blockSize)*(screenHeight/blockSize));
					drawRandomWalls(tmp / 100);
				}
			}			
		}
	}
	
	private void drawRandomWalls(double max) {
		System.out.println(max);
		for(int i = 0; i < max; i++){
			Random rand = new Random();
			int tmp = rand.nextInt(((screenWidth/blockSize)*(screenHeight/blockSize)));

			blokken[tmp].wall = true;
			blokken[tmp].color = new float[] {0.3f, 0.3f, 1f};
		}
	}

	private void leftHold(int x, int y){
		if(startBlock == null){
			if(x > 0 && x < screenWidth && y > 0 && y < screenHeight){
				if(!getBlockAtCoords(x, y).start && !getBlockAtCoords(x, y).end){
			
					getBlockAtCoords(x, y).wall = true;
					getBlockAtCoords(x, y).color = new float[] {0.3f, 0.3f, 1f};
				}
			}
		}
	}
	
	private void leftClick(int x, int y){
	
	}
	
	private void rightClick(int x, int y){
		boolean foundStart = false;
		boolean foundEnd = false;
		for(int i = 0; i < blokken.length; i++){
			if(blokken[i] != null){
				if(blokken[i].start){
					foundStart = true;
				}
				if(blokken[i].end){
					foundEnd = true;
				}
			}
		}
		
		if(!foundStart && !foundEnd){
			if(!getBlockAtCoords(x, y).wall){
				getBlockAtCoords(x, y).start = true;
				getBlockAtCoords(x, y).gScore = 1;
			}
		}
		if(foundStart && !foundEnd){
			if(!getBlockAtCoords(x, y).start){
				if(!getBlockAtCoords(x, y).wall){
					getBlockAtCoords(x, y).end = true;
					pathFound = false;
					thinkTime = System.currentTimeMillis();
				}
			}
		}
	}
	

	private void makeWallsRed() {
		for(int i = 0; i < blokken.length; i++){
			if(blokken[i].wall){
				blokken[i].color = new float[] {1f, 0f, 0f};
			}
		}
	}
	
	private void aStar(){
		boolean foundStart = false;
		boolean foundEnd = false;
		
		for(int i = 0; i < blokken.length; i++){
			if(blokken[i] != null){
				if(blokken[i].start){
					foundStart = true;
					startBlock = blokken[i];
				}
				if(blokken[i].end){
					foundEnd = true;
					endBlock = blokken[i];
					if(!computedStartBlock){
						computeBlock(startBlock);
						computedStartBlock = true;
						System.out.println("Computed startblock");
					}
				}
			}			
		}
		
		if(foundStart && foundEnd){
			if(!isOnClosedList(endBlock)){
				if(!isOpenListEmpty()){
					int lowestScore = Integer.MAX_VALUE;
					Block lowestBlock = null;
						for(int i = 0; i < openList.length; i++){ //Find block with lowest score on the open list 
							if(openList[i] != null){
								if(calculateScore(openList[i]) < lowestScore && openList[i] != startBlock && !isOnClosedList(openList[i])){
									lowestScore = calculateScore(openList[i]);
									lowestBlock = openList[i];
								}
							}
						}
						computeBlock(lowestBlock);
				}else{
					makeWallsRed();
					pathFound = true;
				}
			}else{
				//System.out.println("The path has been found");
				pathFound = true;
				if(!allDone){
					for(int i = 0; i < pathList.length; i++){
					
						if(pathList[i] == null){
							if(i != 0){
								backTrack(pathList[i-1]);
	
							}else{
								backTrack(endBlock);
							}
						}
						allDone = true;
					}
				}
			}
		}
	}

	private boolean isOpenListEmpty(){
		for(int i = 0; i < openList.length; i++){
			if(openList[i] != null){
				return false;
			}
		}
		return true;
	}
	
	private int calculateScore(Block block){
		int G = block.gScore;
		int H = 0;
		
		if(block.x > endBlock.x){
			H += (block.x/blockSize) - (endBlock.x/blockSize);
		}else{
			H += (endBlock.x/blockSize) - (block.x/blockSize);
		}
		if(block.y > endBlock.y){
			H += (block.y/blockSize) - (endBlock.y/blockSize);
		}else{
			H += (endBlock.y/blockSize) - (block.y/blockSize);
		}
				
		int score = G + H;
		
		return score;
	}
	
	private void computeBlock(Block block){
		if(block != null){
			//System.out.println("Computing a block at ("+block.x+", "+block.y+")");
			addToClosedList(block);
			
			//Gaaat dit werken??????????????????????????????? ??? ?? they will never???????
			
			for(int i = 0; i < 4; i++){
				switch(i){
				case 0 :{
					if(getBlockAtCoords(block.x, block.y + blockSize) != null){
						suroundingBlock(getBlockAtCoords(block.x, block.y + blockSize), block.gScore+1); // TOP
					}
					break;
				}
				case 1 :{
					if(getBlockAtCoords(block.x + blockSize, block.y) != null){
						suroundingBlock(getBlockAtCoords(block.x + blockSize, block.y), block.gScore+1); //RIGHT
					}
					break;
				}
				case 2 :{
					if(getBlockAtCoords(block.x, block.y - blockSize) != null){
						suroundingBlock(getBlockAtCoords(block.x, block.y - blockSize), block.gScore+1); //BOT
					}
					break;
				}
				case 3 :{
					if(getBlockAtCoords(block.x - blockSize, block.y) != null){
						suroundingBlock(getBlockAtCoords(block.x - blockSize, block.y), block.gScore+1); //LEFT
					}
					break;
				}
				/*case 4 :{
					if(getBlockAtCoords(block.x - blockSize, block.y + blockSize) != null){
						suroundingBlock(getBlockAtCoords(block.x - blockSize, block.y + blockSize), block.gScore+1); //LEFT & TOP
					}
					break;
				}
				case 5 :{
					if(getBlockAtCoords(block.x + blockSize, block.y + blockSize) != null){
						suroundingBlock(getBlockAtCoords(block.x + blockSize, block.y + blockSize), block.gScore+1); //RIGHT & TOP
					}
					break;
				}
				case 6 :{
					if(getBlockAtCoords(block.x + blockSize, block.y - blockSize) != null){
						suroundingBlock(getBlockAtCoords(block.x + blockSize, block.y - blockSize), block.gScore+1); //RIGHT & BOT
					}
					break;
				}
				case 7 :{
					if(getBlockAtCoords(block.x - blockSize, block.y - blockSize) != null){
						suroundingBlock(getBlockAtCoords(block.x - blockSize, block.y - blockSize), block.gScore+1); //LEFT & BOT
					}
					break;
				}*/
				}
			}
		}
	}
	
	private void suroundingBlock(Block block, int gScore){
		if(!isOnClosedList(block)){
			if(!isOnOpenList(block) && !block.wall){
				addToOpenList(block, gScore);
			}
		}
	}

	private void addToOpenList(Block block, int gScore){
		if(!isOnOpenList(block)){
			for(int j = 0; j < openList.length; j++){
				if(openList[j] == null){
					block.gScore = gScore;
					openList[j] = block;
					break;
				}
			}
		}
		
		if(block == endBlock){
			addToClosedList(endBlock);
			System.out.println("Found it! time in MS: "+ (System.currentTimeMillis() - thinkTime));
		}
	}
	
	private void addToClosedList(Block block){
		if(!isOnClosedList(block)){
			for(int j = 0; j < closedList.length; j++){
				if(closedList[j] == null){
					closedList[j] = block;
					break;
				}
			}
		}
		removeFromOpenList(block);
	}

	private boolean isOnClosedList(Block block) {
		boolean InList = false;
		for(int i = 0; i < closedList.length; i++){
			if(block == closedList[i]){
				InList = true;
				break;
			}
		}
		return InList;
	}
	
	private boolean isOnOpenList(Block block) {
		boolean InList = false;
		for(int i = 0; i < openList.length; i++){
			if(block == openList[i]){
				InList = true;
				break;
			}
		}
		return InList;
	}
	
	private void removeFromOpenList(Block block) {
		if(isOnOpenList(block)){
			for(int i = 0; i < openList.length; i++){
				if(openList[i] != null){
					if(openList[i] == block){
						openList[i] = null;
						break;
					}
				}
			}
		}
	}
	
	private void backTrack(Block block) {
		boolean finished = false;
		int bestBlockScore = Integer.MAX_VALUE;
		Block bestBlock = null;
		
		for(int i = 0; i < 4; i++){
			switch(i){
			case 0 :{
				if(getBlockAtCoords(block.x, block.y + blockSize) != null){ // TOP
					if(getBlockAtCoords(block.x, block.y + blockSize).gScore < bestBlockScore && getBlockAtCoords(block.x, block.y + blockSize).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x, block.y + blockSize).gScore;
						bestBlock = getBlockAtCoords(block.x, block.y + blockSize);
					}
				}
				break;
			}
			case 1 :{
				if(getBlockAtCoords(block.x + blockSize, block.y) != null){ //RIGHT
					if(getBlockAtCoords(block.x + blockSize, block.y).gScore < bestBlockScore && getBlockAtCoords(block.x + blockSize, block.y).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x + blockSize, block.y).gScore;
						bestBlock = getBlockAtCoords(block.x + blockSize, block.y);
					}
				}
				break;
			}
			case 2 :{
				if(getBlockAtCoords(block.x, block.y - blockSize) != null){ //BOT
					if(getBlockAtCoords(block.x, block.y - blockSize).gScore < bestBlockScore && getBlockAtCoords(block.x, block.y - blockSize).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x, block.y - blockSize).gScore;
						bestBlock = getBlockAtCoords(block.x, block.y - blockSize);
					}
				}
				break;
			}
			case 3 :{
				if(getBlockAtCoords(block.x - blockSize, block.y) != null){ //LEFT
					if(getBlockAtCoords(block.x - blockSize, block.y).gScore < bestBlockScore && getBlockAtCoords(block.x - blockSize, block.y).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x - blockSize, block.y).gScore;
						bestBlock = getBlockAtCoords(block.x - blockSize, block.y);
					}
				}
				break;
			}
			/*case 4 :{
				if(getBlockAtCoords(block.x - blockSize, block.y + blockSize) != null){ //LEFT & TOP
					if(getBlockAtCoords(block.x - blockSize, block.y + blockSize).gScore < bestBlockScore && getBlockAtCoords(block.x - blockSize, block.y + blockSize).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x - blockSize, block.y + blockSize).gScore;
						bestBlock = getBlockAtCoords(block.x - blockSize, block.y + blockSize);
					}
				}
				break;
			}
			case 5 :{
				if(getBlockAtCoords(block.x + blockSize, block.y + blockSize) != null){ //RIGHT & TOP
					if(getBlockAtCoords(block.x + blockSize, block.y + blockSize).gScore < bestBlockScore && getBlockAtCoords(block.x + blockSize, block.y + blockSize).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x + blockSize, block.y + blockSize).gScore;
						bestBlock = getBlockAtCoords(block.x + blockSize, block.y + blockSize);
					}
				}
				break;
			}
			case 6 :{
				if(getBlockAtCoords(block.x + blockSize, block.y - blockSize) != null){ //RIGHT & BOT
					if(getBlockAtCoords(block.x + blockSize, block.y - blockSize).gScore < bestBlockScore && getBlockAtCoords(block.x + blockSize, block.y - blockSize).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x + blockSize, block.y - blockSize).gScore;
						bestBlock = getBlockAtCoords(block.x + blockSize, block.y - blockSize);
					}
				}
				break;
			}
			case 7 :{
				if(getBlockAtCoords(block.x - blockSize, block.y - blockSize) != null){ //LEFT & BOT
					if(getBlockAtCoords(block.x - blockSize, block.y - blockSize).gScore < bestBlockScore && getBlockAtCoords(block.x - blockSize, block.y - blockSize).gScore != 0){
						bestBlockScore = getBlockAtCoords(block.x - blockSize, block.y - blockSize).gScore;
						bestBlock = getBlockAtCoords(block.x - blockSize, block.y - blockSize);
					}
				}
				break;
			}*/
			}
		}
		
		for(int i = 0; i < pathList.length; i++){
			if(pathList[i] == null){
				pathList[i] = bestBlock;
				break;
			}
		}
		
	}

	private void drawStuff(){
		drawBlokken();
		if(drawNRS){
			drawOpenList();
			drawClosedList();
			//drawScore();
		}
		drawPathList();
	}

	private void drawBlokken(){
		for(int i = 0; i < blokken.length; i++){
			if(blokken[i] != null){
				Block b = blokken[i];
				
				if(b.start){
					b.color = new float[] {0, 1f, 0};
				}
				if(b.end){
					b.color = new float[] {1f, 0, 0};
				}
				
				drawQuad(b.x, b.y, blockSize, blockSize, b.color);
			}
		}
	}
	
	private void drawOpenList(){
		for(int i = 0; i < openList.length; i++){
			if(openList[i] != null){
				drawCircle(openList[i].x + (blockSize/2), openList[i].y + (blockSize/2), (blockSize/3), new float[] {0, 1f, 0});
			}
		}
	}
	
	private void drawClosedList(){
		for(int i = 0; i < closedList.length; i++){
			if(closedList[i] != null){
				drawLine(closedList[i].x, closedList[i].y, closedList[i].x + blockSize, closedList[i].y + blockSize, new float[] {1f, 0, 0});
				drawLine(closedList[i].x + blockSize, closedList[i].y, closedList[i].x, closedList[i].y + blockSize, new float[] {1f, 0, 0});
			}
		}
	}
	
	private void drawPathList(){
		for(int i = 0; i < pathList.length; i++){
			if(pathList[i] != null && i < pathList.length -1){
				if(pathList[i].x == startBlock.x && pathList[i].y == startBlock.y){
					break;
				}else{
					drawLine(pathList[i].x + blockSize/2, pathList[i].y + blockSize/2, pathList[i+1].x + blockSize/2, pathList[i+1].y + blockSize/2, new float[] {1f, 1f, 1f});
				}
			}else{
				break;
			}
		}
	}
	
	private void drawScore(){
		try{
			for(int i = 0; i < blokken.length; i++){
				if(blokken[i] != null){
					GL11.glColor3f(1f, 0, 0);
					SimpleText.drawString("" + calculateScore(blokken[i]), blokken[i].x+2, blokken[i].y+blockSize/2);
					SimpleText.drawString("" + blokken[i].gScore, blokken[i].x+2, blokken[i].y);
				}
			}
		}catch(Exception e){
		
		}
	}
	
	private void drawQuad(int x, int y, int h, int w, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);

		// draw quad
		GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(x, y);
		    GL11.glVertex2f(x+w,y);
		    GL11.glVertex2f(x+w,y+h);
		    GL11.glVertex2f(x,y+h);
		GL11.glEnd();
	}
	
	private void drawQuad(int x, int y, int h, int w, float color[], float alpha){
		GL11.glColor4f(color[0], color[1], color[2], alpha);

		// draw quad
		GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex2f(x, y);
		    GL11.glVertex2f(x+w,y);
		    GL11.glVertex2f(x+w,y+h);
		    GL11.glVertex2f(x,y+h);
		GL11.glEnd();
	}
	
	private void drawHalfQuad(int x, int y, int n, int size, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);
		
		switch(n){
			case 0:
			GL11.glBegin(GL11.GL_TRIANGLES);
				GL11.glVertex2f( x, y+size);
				GL11.glVertex2f( x, y);
				GL11.glVertex2f( x+size, y);
			GL11.glEnd();
			break;
			
			case 1:
			GL11.glBegin(GL11.GL_TRIANGLES);
				GL11.glVertex2f( x, y+size);
				GL11.glVertex2f( x, y);
				GL11.glVertex2f( x+size, y+size);
			GL11.glEnd();
			break;
			
			case 2:
			GL11.glBegin(GL11.GL_TRIANGLES);
				GL11.glVertex2f( x+size, y+size);
				GL11.glVertex2f( x, y+size);
				GL11.glVertex2f( x+size, y);
			GL11.glEnd();
			break;
			
			case 3:
			GL11.glBegin(GL11.GL_TRIANGLES);
				GL11.glVertex2f( x+size, y+size);
				GL11.glVertex2f( x, y);
				GL11.glVertex2f( x+size, y);
			GL11.glEnd();
			break;
			
			default:
				System.out.println("Invalid 3th parameter in drawHalfQuad()");
				System.exit(-1);
			break;
				
		}
	}
	
	private void drawLine(int X1, int Y1, int X2, int Y2, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);
		
		GL11.glBegin(GL11.GL_LINES);
			GL11.glVertex2f(X1, Y1);
			GL11.glVertex2f(X2, Y2);
		GL11.glEnd();
	}
	
	private void drawCircle(double x, double y, int radius, float color[]){
		GL11.glColor3f(color[0], color[1], color[2]);
		
		int slices = 35;
		float incr = (float) (2 * Math.PI / slices);
		/*xCoord = xCoord + radius;
		yCoord = yCoord + radius;*/
		
    GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        for(int i = 0; i < slices; i++)
        {
              float angle = incr * i;

              float Xc = (float) (x +  Math.cos(angle) * radius);
              float Yc = (float) (y +  Math.sin(angle) * radius);

              GL11.glVertex2f(Xc, Yc);
        }
     GL11.glEnd();	
	}


	private void drawCircle(double x, double y, int radius, float color[], float alpha){		
		GL11.glColor4f(color[0], color[1], color[2], alpha);
	
		int slices = 35;
		float incr = (float) (2 * Math.PI / slices);
		/*xCoord = xCoord + radius;
		yCoord = yCoord + radius;*/
	
		GL11.glBegin(GL11.GL_TRIANGLE_FAN);
		for(int i = 0; i < slices; i++){
			float angle = incr * i;

			float Xc = (float) (x +  Math.cos(angle) * radius);
			float Yc = (float) (y +  Math.sin(angle) * radius);

          GL11.glVertex2f(Xc, Yc);
		}
		GL11.glEnd();	
	}
}







