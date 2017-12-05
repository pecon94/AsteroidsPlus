package experimentGame;

import java.util.ArrayList;

public class EnemyShip {
	public boolean isAlive= false, hasSound= false, spawned = false;
	private float radius = 17, posX, posY, speedX, speedY, rotation = 0, rotInc;
	public int screenX, screenY, points=200;
	
	ArrayList<Bullets2> bullets2= new ArrayList<Bullets2>();
	
	EnemyShip(){
		//Explicit empty super constructor
	}
	//Creates "Alien Ship"
	//Selecting a random plane to spawn
	EnemyShip(int x, int y){
		init(x, y);
	}
	
	public void init (int x, int y){
		int ranX= (int) ((Math.random()*10)-5);
		if (ranX<0)
			ranX=-1;
		else
			ranX=1;
		int ranY= (int) ((Math.random()*10)-5);
		if (ranY<0)
			ranY=-1;
		else
			ranY=1;
		screenX=x;
		screenY=y;
		speedX= 0; 
		speedY= 0;
		while (speedX >= -0.3 && speedX <= 0.3)
		{
			speedX=(float)(Math.random()-0.5)*4; //generates random values so each meteor behaves differently.
		}
		while (speedY >= -0.3 && speedY <= 0.3)
		{
			speedY=(float)(Math.random()-0.5)*4;
		}
		rotInc=(float)(0.5F)*30;
		posX=screenX*ranX;
		posY=screenY*ranY;
	}
	public void move(){
		posX+=speedX;
		posY+=speedY;
		rotation=(rotation+rotInc)%360;
		
	//Keeping meteors in frame
		if (posX>(screenX/2))
			posX=(-screenX/2);
		if (posX<(-screenX/2))
			posX=(screenX/2);
		if (posY>(screenY/2))
			posY=(-screenY/2);
		if (posY<(-screenY/2))
			posY=screenY/2;
	}
	public void blowsUp(){
		//Play FX here (haven't assigned a sound effect yet, but it will be played when this function is called).
		//Create fragment Array (4 fragments for any meteor broken.
		isAlive=false;
		init(screenX, screenY);
	}
	public float getPosX(){
		return posX;
	}
	public float getPosY(){
		return posY;
	}
	public float getRadius() {
		return radius;
	}
	public void setRadius(float radius) {
		this.radius = radius;
	}
	public float getRotation() {
		return rotation;
	}
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	
	public void fireRound (float posX2, float posY2){
	{
		bullets2.add(new Bullets2(posX, posY, posX2, posY2));
	}
	
	for(int j=0; j<bullets2.size();j++){
		if(bullets2.get(j).isAlive)
			bullets2.get(j).move();
		else{
			bullets2.remove(j);
		}
	}
	}



	class Bullets2{
		float posX1, posY1, posX2, posY2, distX=0, distY=0, rotation, speed=18f, maxDistance=1200;
		double angle;
		boolean isAlive=true;
		Bullets2(float posX1, float posY1, float posX2, float posY2){
			angle =  Math.atan((posY2 - posY1) / (posX2 - posX1));
			this.posX1=(float) (posX+Math.cos(angle)*5);
			this.posY1=(float) (posY+Math.sin(angle)*5);
			this.rotation= (float) angle;
		}
		public void move(){
			if(Math.sqrt((distX*distX)+(distY*distY))<maxDistance){
			  float temp;
			  temp=(float) Math.cos(angle)*speed;
			  posX+=temp;
			  distX+=temp;
			  temp=(float) Math.sin(angle)*speed;
			  posY+=temp;
			  distY+=temp;
		  }
			else
				isAlive=false;
		}
	}
	
}
