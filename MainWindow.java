package experimentGame;

import java.awt.Color;
import java.awt.DisplayMode;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import java.util.ArrayList;

import javax.sound.sampled.Clip;
import javax.swing.JPanel;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

class MainWindow extends JPanel implements GLEventListener, KeyListener{
	private PlayerShip player;
	private EnemyShip enemy;
	private PlaySound BGM, FX;
	private String bgm1="src/sound/BGM/bgm.wav", fx1="src/sound/FX/thrust.wav", 
			laserFX="src/sound/FX/laser.wav", blowUp="src/sound/FX/blowUp.wav", 
			pblowUp="src/sound/FX/pblowUp.wav", ufoFly="src/sound/FX/saucerBig.wav";
	private Clip BGMClip, thrust, saucer;
	private int metOnScreen=4;
	private ArrayList<Meteors> meteors= new ArrayList<Meteors>();
	private int gameFieldX=1600, gameFieldY=900, zrend=-1000;//Drawing dimension and Z depth
	private Obj3D ship, met3D, title3D, ufo;
	private String shipObj="src/objects/ship_new.obj", meteorObj="src/objects/meteor.obj",
			title="src/objects/title.obj", ufoObj="src/objects/UFO.obj", 
			backGround="src/textures/background.jpg",
			fractalImg="src/textures/neb.png", holeImg="src/textures/spin.jpg";
	FPSAnimator animator;
	public GLCanvas glcanvas;
	private TextRenderer renderer, score, lives, stage;
	private int backgroundID, fractalID, holeID, scene=0, blinkCounter=0, levelCounter=1, highscore=0;
	private float titleRotationAngle=0;
	private boolean render=true, bgmPlaying=false, stageCleared=false, reloaded = true;
	
	private Timer timer;
	boolean started = true;
	private GLU glu = new GLU();
	
	public String meteorTex= "src/textures/meteor2.jpg";

	private File background, meteor;
	private Texture bgTex, metTex;
	private ArrayList<Integer> textureID = new ArrayList<Integer>(); 
	
	public MainWindow(int screenWidth, int screenHeight){
		
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		//The canvas
		glcanvas = new GLCanvas(capabilities);
		glcanvas.addGLEventListener (this); //allows us to use the keyboard with the window.
		glcanvas.setSize(screenWidth,screenHeight);
		glcanvas.addKeyListener(this);
		timer = new Timer();
		
		
		//Creating Title
		title3D = new Obj3D(title);
		//Creating a player
		player = new PlayerShip(0,0,gameFieldX,gameFieldY);
		ship=new Obj3D(shipObj);
		
		//Creating an enemy
		enemy = new EnemyShip(gameFieldX,gameFieldY);
		ufo=new Obj3D(ufoObj);
		
		//creating meteors on screen and storing them into an array
		for(int i=0; i<metOnScreen;i++){
			meteors.add(new Meteors(gameFieldX,gameFieldY));
		}
		met3D=new Obj3D(meteorObj);//importing the 3D model for the Meteors (location defined in string meteorObj).
		
		//Creating sound
		BGM=new PlaySound();
		//Initialize sound FX
		FX= new PlaySound();
		//Creating the animator for this window
		animator = new FPSAnimator(glcanvas, 60, true);
		animator.start();
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		//drawing stuff here~
		final GL2 gl= drawable.getGL().getGL2();
		switch (scene){
		case 0:
			drawIntro(gl);
			break;
		case 1:
			gl.glClear(GL2.GL_COLOR_BUFFER_BIT|GL2.GL_DEPTH_BUFFER_BIT); //Clear the screen and the depth buffer
			//Probably our most important section, here we draw everything on the canvas.
			checkSound(); //Calls to check on SFX.
			drawBackGround(gl);
			drawPlayer(gl);
			drawMeteors(gl);		
			if (started)
			{
				timer.schedule(new spawnUFO(), 6*1000);
				started = false;
			}
			if (enemy.isAlive)
			{
				drawUFO(gl);	
				
			}
			drawScoreLives(gl);
			gl.glFlush();
			if(stageCleared)
				nextLevel(metOnScreen);
			break;
		}
		
	}
	
	public class spawnUFO extends TimerTask {
		 public void run() {
			enemy.isAlive = true;
		 }
	}
	
	public class respawnUFO extends TimerTask {
		public void run() {
			enemy.isAlive = true;
		}
	}
	
	public class enemyFire extends TimerTask {
		public void run() {
			enemy.fireRound(player.posX, player.posY);
			reloaded = true;
	}
	}
	
	private void drawPlayer(GL2 gl){
		gl.glLoadIdentity();				//resets values of gl Matrix
		gl.glTranslatef(player.getPosX(),player.getPosY(),zrend); //places objects at an inner distance Z (calculated to obtain desired logical units).
		gl.glRotatef(player.rotation, 0, 0, 1);					 //makes the ship face in the desired direction.
		gl.glRotatef(player.tilt, 1f, 0, 0);					 //adds tilting effect, calculated within playerShip.
		if(!player.blink) //Adds the blinking effect while invulnerable
			ship.draw(gl, player.scale);							//calls object reader to draw out model.
		
		//This draws the bullets when shot
		if(player.bullets.size()>0){
			for(int i=0; i<player.bullets.size();i++){
				checkBCollision();
				gl.glLoadIdentity();
				gl.glColor3f(0, 1, 0);
				gl.glTranslatef(player.bullets.get(i).posX,player.bullets.get(i).posY,zrend);
				gl.glRotatef(player.bullets.get(i).rotation, 0, 0, 1);
				gl.glBegin(GL2.GL_TRIANGLES);
					gl.glVertex3f(20, 0, 0);
					gl.glVertex3f(0, -5, 0);
					gl.glVertex3f(0, 5, 0);
				gl.glEnd();
			}
		}
		checkPCollision();
		player.move();
	}
	private void nextLevel(int asteroids){
		asteroids+=2*levelCounter; //Adding 2 more asteroids
		player.reset();	//resetting player position
		//creating new array
		for(int i=0; i<asteroids;i++){
			meteors.add(new Meteors(gameFieldX,gameFieldY));
		}
		levelCounter++;
		stageCleared=false;
		
	}
	private void drawMeteors(GL2 gl){
		//This draws our meteors and fragments if a meteor is destroyed.
				for(int i=0; i<meteors.size();i++){	
					gl.glLoadIdentity();
					gl.glTranslatef(meteors.get(i).getPosX(),meteors.get(i).getPosY(),zrend);
					gl.glRotatef(meteors.get(i).getRotation(), 1, 1, 1);
					
					//If it's alive, we draw it!
					if(meteors.get(i).isAlive){
						met3D.draw(gl, meteors.get(i).getRadius(), getTextureID(Textures.METEOR));
						meteors.get(i).move();
					}	
					//if not, then we do its fragments.
					else if(meteors.get(i).hasFragments){
						for(int j=0;j<meteors.get(i).frags.size();j++){
							if(meteors.get(i).frags.get(j).isAlive){
							  gl.glLoadIdentity();
							  gl.glTranslatef(meteors.get(i).frags.get(j).X,meteors.get(i).frags.get(j).Y,zrend);
							  gl.glRotatef(meteors.get(i).frags.get(j).rt, 1, 1, 1);
							  met3D.draw(gl, meteors.get(i).frags.get(j).rad, getTextureID(Textures.METEOR));
							  meteors.get(i).frags.get(j).move();
						  }
							else
								meteors.get(i).frags.remove(j);
						}
						if(meteors.get(i).frags.size()==0)
							meteors.get(i).hasFragments=false;
					}else{
						meteors.remove(i);
					}
				}
				if(meteors.size()==0)
					stageCleared=true;
	} 
	private void drawUFO(GL2 gl){
		gl.glLoadIdentity();				//resets values of gl Matrix
		gl.glTranslatef(enemy.getPosX(),enemy.getPosY(),zrend); //places objects at an inner distance Z (calculated to obtain desired logical units).
		gl.glRotatef(enemy.getRotation(), 0, 1, 0); //makes the ship face in the desired direction.
		if(enemy.isAlive) {
			ufo.draw(gl, enemy.getRadius());
		}
		/*if (reloaded)
		{
			timer.schedule(new enemyFire(), 1*1000);
			reloaded = false;
		}*/
		if(player.isAlive && !player.invulnerable)
		{
	     //This draws the bullets when shot
		if(enemy.bullets2.size()>0){
			for(int i=0; i<enemy.bullets2.size();i++){
				checkPCollision();
				gl.glLoadIdentity();
				gl.glColor3f(0, 0, 1);
				gl.glTranslatef(enemy.bullets2.get(i).posX1,enemy.bullets2.get(i).posY1,zrend);
				gl.glRotatef(enemy.bullets2.get(i).rotation, 0, 0, 1);
				gl.glBegin(GL2.GL_TRIANGLES);
					gl.glVertex3f(20, 0, 0);
					gl.glVertex3f(0, -5, 0);
					gl.glVertex3f(0, 5, 0);
				gl.glEnd();
			}
		}
		}
		checkECollision();
		enemy.move();
	} 
	

	private void drawIntro(GL2 gl){
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT|GL2.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();

		int fit=120;
		gl.glEnable(GL.GL_BLEND); 
		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA); 
		gl.glColor3f(1,1,1);
		gl.glBindTexture(GL2.GL_TEXTURE_2D,holeID);
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTranslatef(0,0,zrend-80);
		gl.glRotatef(titleRotationAngle, 0, 0, 1);
		gl.glBegin(GL2.GL_QUADS);
		gl.glTexCoord2d(0.0, 0.0);
		gl.glVertex2d(-800-fit, -800+fit/2);
		gl.glTexCoord2d(1.0, 0.0);
		gl.glVertex2d(800-fit, -800+fit/2);
		gl.glTexCoord2d(1.0, 1.0);
		gl.glVertex2d(800-fit, 800+fit/2);
		gl.glTexCoord2d(0.0, 1.0);
		gl.glVertex2d(-800-fit,800+fit/2);
		gl.glEnd();
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
		gl.glLoadIdentity();
		gl.glTranslatef(0,0,-40);
		gl.glRotatef(titleRotationAngle, 0, 1, 0);
		title3D.draw(gl, 4, 0, 0, 1);
		titleRotationAngle += 0.42;
		
		renderer.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
		renderer.setColor(Color.WHITE);
		renderer.draw("High Score: "+highscore,
		(int) ((glcanvas.getWidth()/2)-renderer.getBounds("Press 'Space' to Start").getWidth()/2),
		(int) ((glcanvas.getHeight()/2)-(2*renderer.getBounds("Press 'Space' to Start").getHeight())));
		renderer.endRendering();
		
		if(render)
		{
		renderer.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
		renderer.setColor(Color.WHITE);
		renderer.draw("Press 'Space' to Start",
		(int) ((glcanvas.getWidth()/2)-renderer.getBounds("Press 'Space' to Start").getWidth()/2),
		(int) ((glcanvas.getHeight()/2)-(3*renderer.getBounds("Press 'Space' to Start").getHeight())));
		renderer.endRendering();

		blinkCounter++;
		}
		else{
			blinkCounter++;
		}
			if(blinkCounter == 30){
				render = !render;
				blinkCounter =0;
		}

			
	}
	private void drawBackGround(GL2 gl){
		//Start BG render
		int stretch = 50;
		gl.glBindTexture(GL2.GL_TEXTURE_2D,getTextureID(Textures.BACKGROUND));
		gl.glColor3f(1, 1, 1);
		gl.glLoadIdentity();
		gl.glEnable(GL2.GL_TEXTURE_2D);
		gl.glTranslatef(0,0,zrend-100);
		gl.glBegin(GL2.GL_QUADS);
		gl.glNormal3f(1,0,0);
		gl.glTexCoord2d(0.0, 0.0);
		gl.glVertex2d(-gameFieldX/2 -stretch, -gameFieldY/2-stretch);
		gl.glTexCoord2d(1.0, 0.0);
		gl.glVertex2d(gameFieldX/2+stretch, -gameFieldY/2-stretch);
		gl.glTexCoord2d(1.0, 1.0);
		gl.glVertex2d(gameFieldX/2+stretch, gameFieldY/2+stretch);
		gl.glTexCoord2d(0.0, 1.0);
		gl.glVertex2d(-gameFieldX/2-stretch, gameFieldY/2+stretch);
		gl.glEnd();
		gl.glDisable(GL2.GL_TEXTURE_2D);
		
	}
	private void drawScoreLives(GL2 gl){
		score.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
		score.setColor(Color.RED);
		score.draw("Score: "+player.playerScore,80,glcanvas.getHeight()-50);
		score.endRendering();
		lives.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
		lives.setColor(Color.RED);
		lives.draw("X "+player.lives,(int) (glcanvas.getWidth()-lives.getBounds("X 00").getWidth()),glcanvas.getHeight()-50);
		lives.endRendering();
		stage.beginRendering(glcanvas.getWidth(), glcanvas.getHeight());
		stage.setColor(Color.WHITE);
		stage.draw("Stage "+levelCounter,(int) (glcanvas.getWidth()/2-stage.getBounds("Stage 00").getWidth()/2),glcanvas.getHeight()-50);
		stage.endRendering();
	}
	private void checkSound(){
		if(!bgmPlaying){
			BGMClip=BGM.playBGM(bgm1);//---------------------------------------------------------------------------------------------------------------------
			bgmPlaying=true;
		}
		
		//Lets the ship make some noise while accelerating.
		if (player.up&&!player.thrust&&!player.delay){
			thrust=BGM.playBGM(fx1);
			player.thrust=true;
		}else if((!player.up && player.thrust)||player.delay){
			player.thrust=false;
			if(thrust!=null)
				thrust.stop();
		}
		if (enemy.isAlive && !enemy.hasSound)
		{
			saucer=BGM.playBGM(ufoFly); 
			enemy.hasSound = true;
		}
		else if (!enemy.isAlive && enemy.hasSound)
		{
			saucer.stop();
			enemy.hasSound = false;
		}
	}
	private void checkPCollision(){
		float pX=player.posX, pY=player.posY;
		for(int i = 0;i<meteors.size();i++){
			if(meteors.get(i).isAlive){
			 if(!player.invulnerable&&(Math.sqrt((pX-meteors.get(i).getPosX())
					 *(pX-meteors.get(i).getPosX())+(pY-meteors.get(i).getPosY())
					 *(pY-meteors.get(i).getPosY()))<(player.scale*(4)+(meteors.get(i).getRadius())))
					 &&meteors.get(i).isAlive){
				player.blowsUp();
				FX.playFX(pblowUp);
				if(!player.isAlive)
					gameReset();
			 }
			}else if(meteors.get(i).hasFragments){
				for(int j=0; j<meteors.get(i).frags.size();j++){
				    float fX=meteors.get(i).frags.get(j).X;
				    float fY=meteors.get(i).frags.get(j).Y;
				if(!player.invulnerable&&(Math.sqrt(((pX-fX)*(pX-fX))+((pY-fY)*(pY-fY)))<player.scale+meteors.get(i).frags.get(j).rad)){
					player.blowsUp();
					FX.playFX(pblowUp);
					if(!player.isAlive)
						gameReset();
				}
				}
			}
			}
		if(enemy.isAlive)
		{
			if(!player.invulnerable&&(Math.sqrt((pX-enemy.getPosX())
					 *(pX-enemy.getPosX())+(pY-enemy.getPosY())
					 *(pY-enemy.getPosY()))<(player.scale*(4)+(enemy.getRadius())))
					 &&enemy.isAlive){
				player.blowsUp();
				enemy.blowsUp();
				FX.playFX(pblowUp);
				if(!player.isAlive)
					gameReset();
			 }
			}
		
	}
	private void checkBCollision(){
		//Checking collision for regular sized meteors
		for(int i =0; i<player.bullets.size();i++){
			//If there are Meteors in the array
			for(int j=0; j<meteors.size();j++){
				if(meteors.get(j).isAlive){
				 float bX=player.bullets.get(i).posX;
				 float bY=player.bullets.get(i).posY;
				 float mX=meteors.get(j).getPosX();
				 float mY=meteors.get(j).getPosY();
					if((Math.sqrt((bX-mX)*(bX-mX))+((bY-mY)*(bY-mY))<meteors.get(j).getRadius()*2+15) && player.bullets.get(i).isAlive){
						player.bullets.get(i).isAlive=false;
						player.playerScore+=meteors.get(j).points;
						player.extraLife+=meteors.get(j).points;
						meteors.get(j).blowsUp();
						FX.playFX(blowUp);
					}
				}
				//If there are no meteors, check for fragments
				else if(meteors.get(j).hasFragments){
				  for(int k=0; k<meteors.get(j).frags.size();k++){
				    float bX=player.bullets.get(i).posX;
				    float bY=player.bullets.get(i).posY;
				    float fX=meteors.get(j).frags.get(k).X;
				    float fY=meteors.get(j).frags.get(k).Y;
				  	   if((Math.sqrt(((bX-fX)*(bX-fX))+((bY-fY)*(bY-fY)))<meteors.get(j).frags.get(k).rad) && player.bullets.get(i).isAlive){
					       player.bullets.get(i).isAlive=false;
						   player.playerScore+=meteors.get(j).frags.get(k).points;
						   player.extraLife+=meteors.get(j).frags.get(k).points;
						   meteors.get(j).frags.get(k).isAlive=false;
						   FX.playFX(blowUp);
					   }
				  }
				}
			}
		}
	}
	private void checkECollision(){
		//Checking collision for regular sized meteors
		 float eX=enemy.getPosX();
		 float eY=enemy.getPosY();
		for(int i =0; i<player.bullets.size();i++){
			if(enemy.isAlive){
				 float bX=player.bullets.get(i).posX;
				 float bY=player.bullets.get(i).posY;
				
					if((Math.sqrt((bX-eX)*(bX-eX))+((bY-eY)*(bY-eY))<enemy.getRadius()*2+15) && player.bullets.get(i).isAlive){
						player.bullets.get(i).isAlive=false;
						player.playerScore+=enemy.points;
						player.extraLife+=enemy.points;
						enemy.blowsUp();
						FX.playFX(blowUp);
						timer.schedule(new respawnUFO(), 13*1000);
					}
				}
			}
		for(int i = 0;i<meteors.size();i++){
			if(meteors.get(i).isAlive){
			 if((Math.sqrt((eX-meteors.get(i).getPosX())
					 *(eX-meteors.get(i).getPosX())+(eY-meteors.get(i).getPosY())
					 *(eY-meteors.get(i).getPosY()))<(player.scale*(4)+(meteors.get(i).getRadius())))
					 &&meteors.get(i).isAlive){
				enemy.blowsUp();
				FX.playFX(blowUp);
				timer.schedule(new respawnUFO(), 13*1000);
			 }
			}else if(meteors.get(i).hasFragments){
				for(int j=0; j<meteors.get(i).frags.size();j++){
				    float fX=meteors.get(i).frags.get(j).X;
				    float fY=meteors.get(i).frags.get(j).Y;
				if((Math.sqrt(((eX-fX)*(eX-fX))+((eY-fY)*(eY-fY)))< enemy.getRadius() +meteors.get(i).frags.get(j).rad)){
					enemy.blowsUp();
					FX.playFX(blowUp);
					timer.schedule(new respawnUFO(), 13*1000);
				}
				}
			}
			}
		}
		
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		//initializing OpenGL elements for this window~
		final GL2 gl=drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0f, 0f, 1f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
	    gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);	
		
		//Texture~
		renderer = new TextRenderer(new Font("SansSerif", Font.BOLD, 50));
		score = new TextRenderer(new Font("Impact", Font.BOLD, 50));
		lives = new TextRenderer(new Font("Impact", Font.BOLD, 50));
		stage = new TextRenderer(new Font("Impact", Font.BOLD, 50));
		try{
		 File texture = new File(backGround);
		 Texture tx = TextureIO.newTexture(texture,true);
		 backgroundID = tx.getTextureObject(gl);
		 
		 texture = new File(fractalImg);
		 tx = TextureIO.newTexture(texture,true);
		 fractalID = tx.getTextureObject(gl);
		 
		 texture = new File(holeImg);
		 tx = TextureIO.newTexture(texture,true);
		 holeID = tx.getTextureObject(gl);
		}
		catch(IOException e){e.printStackTrace();}
		try {
			setTextures(gl);
		} catch (GLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		gl.glEnd();
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		
		if(height<=0)
			height=1;
		final float h=(float)width/(float)height;	//calculating h= window width/height.
		gl.glViewport(0,0,glcanvas.getWidth(),glcanvas.getHeight());	//position set for view port
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		//Using a 2d Plane with 2d Graphics
		//gl.glOrtho(0, glcanvas.getWidth(), glcanvas.getHeight(), 0, -1, 1);
		//gl.glMatrixMode(GL2.GL_MODELVIEW);
		//gl.glMatrixMode(GL2.GL_MODELVIEW);
		//gl.glLoadIdentity();
		//gl.glFrustum(0, glcanvas.getWidth(), glcanvas.getHeight(), 0, 0.9999f, 10);
		//Implementing 3D with perspective view.
		/**Using a 3D perspective after calculating Z distance for drawing for a fixed amount of logical units on X and Y*/
		glu.gluPerspective(45F, h,1, 2000); //View angle is 45deg, renders from unit 1000 to 2000, aspect ratio "h" calculated above.
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();
	}
	public void gameReset(){
		//Resetting Game values to start all over
		highscore=Math.max(highscore, player.playerScore);
		scene=0;
		levelCounter=1;
		BGMClip.stop();
		bgmPlaying=false;
		thrust.stop();
		scene=0;
		meteors= new ArrayList<Meteors>();
		scene=0;
		blinkCounter=0;
		float titleRotationAngle=0;
		boolean render=true, bgmPlaying=false;
		player = new PlayerShip(0,0,gameFieldX,gameFieldY);
		for(int i=0; i<metOnScreen;i++){
			meteors.add(new Meteors(gameFieldX,gameFieldY));
		}
	}
	//Key listener instructions! We're letting the ship know what keys are being pressed so it can do it's thing!
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			player.right=true;
		if(e.getKeyCode() == KeyEvent.VK_LEFT)
			player.left=true;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			player.up=true;
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			player.down=true;
		if(e.getKeyCode() == KeyEvent.VK_D){
			if(scene==1&&(!player.shooting && (player.bullets.size()!=player.bulletMax)))
				FX.playFX(laserFX);
			player.shooting=true;
		}
		//TESTING, initialize game!
		if(e.getKeyCode() == KeyEvent.VK_SPACE)
			if(scene==0)
				scene=1;
		
	}
	//Lets our ship know we are no longer pressing the keys.
	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			player.right=false;
		if(e.getKeyCode() == KeyEvent.VK_LEFT)
			player.left=false;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			player.up=false;
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			player.down=false;
		if(e.getKeyCode() == KeyEvent.VK_D){
			player.shooting=false;
			player.shoot=true;
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {}
	
	private void setTextures(GL2 gl) throws GLException, IOException
	{
		background = new File(backGround);
		bgTex = TextureIO.newTexture(background,true);
		textureID.add(bgTex.getTextureObject(gl));
		
		meteor = new File(meteorTex);
		metTex = TextureIO.newTexture(meteor, true);
		textureID.add(metTex.getTextureObject(gl));
		
	}
	private int getTextureID(Textures texENUM)
	{
		switch(texENUM)
		{
		case BACKGROUND :
		{
			return textureID.get(0);
		} 
		case METEOR :
		{
			return textureID.get(1);
		}
		}
		return 0;
	}
}
enum Textures
{
	METEOR, BACKGROUND;
}