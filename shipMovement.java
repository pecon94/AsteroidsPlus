package zGameTST;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.*;
import java.io.File;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

public class zGameTST {

	public static void main(String[] args) {
		JFrame GPHX = new JFrame();
		GPHX.setSize(1024, 768);
		GPHX.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		GPHX.setResizable(false);
		MainScreen ms = new MainScreen();
		ms.setFocusable(true);
		//ms.requestFocusInWindow(); //Used to manage JPanel with KeyListener
		GPHX.add(ms);
		GPHX.setVisible(true);
		ms.setup();
	}

}

class MainScreen extends JPanel implements KeyListener{
	private Graphics rasterGraphics;
	private Image raster, background, playerShip, stars;
	PlayerShip p;
	Graphics2D playerG;
	PlaySound BGM, FX;
	Clip BGMClip, thrust;
	
	public void setup()
	{
		//System.out.println("Test Line");
		int screenW=this.getWidth(), screenH=this.getHeight();
		raster=this.createImage(screenW,screenH);
		rasterGraphics=raster.getGraphics();
		p =new PlayerShip(screenW/2,screenH/2, screenW, screenH);
		BGM=new PlaySound();
		//BGMClip=BGM.playBGM("");//directory string
		addKeyListener(this);
		draw();
	}
	private void draw()
	{// This loop is basically the engine that will be running our code, the cycle speed can be modified with 
		while (true){
			drawBG();
			p.move();
			drawPlayer();
			checkSound();
		getGraphics().drawImage(raster, 0, 0, null);
		try{Thread.sleep(3);}catch(Exception e){} ////////////IMPORTANT, this will manage the speed of the game, if it runs too fast, increase the number.
		}
	}
	private void drawBG()
	{
		rasterGraphics.setColor(Color.blue);
		rasterGraphics.fillRect(0, 0, this.getWidth(), this.getHeight());
		//rasterGraphics.drawImage(background,0,0,this.getWidth(), this.getHeight(),null);//setting BG to window Size.
	}
	
	private void drawPlayer(){
		rasterGraphics.setColor(Color.white);
		rasterGraphics.fillRect(p.getPosX(), p.getPosY(), p.shipW, p.shipH);
		//rasterGraphics.drawImage(playerShip, p.getPosX(), p.getPosY(), p.shipW, p.shipH,null);
	}
	private void checkSound(){
		if (p.up&&!p.thrust){
			//thrust=BGM.playBGM("");//directory string
			p.thrust=true;
		}else if(!p.up && p.thrust){
			p.thrust=false;
			//thrust.stop();
		}
	}
	
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			p.right=true;
		if(e.getKeyCode() == KeyEvent.VK_LEFT)
			p.left=true;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			p.up=true;
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			p.down=true;
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			p.right=false;
		if(e.getKeyCode() == KeyEvent.VK_LEFT)
			p.left=false;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			p.up=false;
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			p.down=false;
	}

	public void keyTyped(KeyEvent e) {}
	
}

class PlayerShip{
	//Point2D location; //Decided to use simple X and Y in to no call location.getX and all every time, could be changed later.
	float posX, posY, preX, preY; //Location points for the ship
	float rotate= 1f, friction= 0.98f, spd=0.1f, rotation=0, sx,sy;
	int shipW=60, shipH=30;//ship dimensions
	boolean left, right, up, down, thrust;
	int screenX, screenY;
	
	
	PlayerShip(int x, int y, int sx, int sy){
		posX= x;
		posY= y;
		screenX= sx;
		screenY= sy;
		//location.setLocation(posX, posY);
	}
	public void move()
	{
		preX= posX;
		preY= posY;
		if(right){
			rotation= (rotation+rotate)%360;
		}
		else if(left){
			rotation=(rotation-rotate)%360;
		}
		if(up){
				sy+=Math.sin(Math.toRadians(rotation))*spd;
				sx+=Math.cos(Math.toRadians(rotation))*spd;
			
		}
		
		posX+=sx;		//Velocity based movement fed by acceleration and slowed down by friction.
		posY-=sy;
		sx*=friction;	//Velocity is limited by a friction factor. This allows the ship to reach a maximum velocity by countering its acceleration.
		sy*=friction;
	
		//Resetting position to keep ship in window bounds
		if (posX>screenX)
			posX=0-shipW;
		if (posX<0-shipW)
			posX=screenX;
		if (posY>screenY)
			posY=0-shipH;
		if (posY<0-shipH)
			posY=screenY;
		System.out.println(Math.sin(Math.toRadians(rotation))+" , "+Math.cos(Math.toRadians(rotation))+" , "+Math.round(sx)+"-"+sx+" , "+Math.round(sy)+"-"+sy+ " , "+ rotation);
		
	}
	public int getPosX()
	{
		return Math.round(posX);
	}
	public int getPosY()
	{
		return Math.round(posY);
	}
}

class PlaySound
{
	public Clip playBGM(String f)
	{
		//plays the sound of the string path that is provided to the file object
		try{
			File file = new File(f);
			Clip clip = AudioSystem.getClip();
			AudioInputStream ais = AudioSystem.getAudioInputStream( file );
			clip.open(ais);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
			return clip;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
		
	}
	public void playFX(String fx)
	{
		try{
			File file = new File(fx);
			Clip clip = AudioSystem.getClip();
			AudioInputStream ais = AudioSystem.
					getAudioInputStream( file );
			clip.open(ais);
			//clip.loop(Clip.LOOP_CONTINUOUSLY); //shouldn't be needed, as sound effects would play and stop once the track is done.
			clip.start();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}