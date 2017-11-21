package experimentGame;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;

public class Render{

	
	public static void main(String[] args) {
		//setup open GL version 2
		
		MainWindow MAIN = new MainWindow();
		final JFrame frame = new JFrame ("Ateroids+!");
		frame.getContentPane().add(MAIN.glcanvas);
		
		//Shutdown
		frame.addWindowListener(new WindowAdapter(){public void windowClosing(WindowEvent e){
			if(MAIN.animator.isStarted())
				MAIN.animator.stop();
		System.exit(0);
		}}
			);
		
		frame.setSize(frame.getContentPane().getPreferredSize());
		
		/**This part centers the screen on start up*/
		
		
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = graphicsEnvironment.getScreenDevices();
		DisplayMode dm_old = devices[0].getDisplayMode();
		DisplayMode dm=dm_old;
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int windowX = Math.max(0, (screenSize.width - frame.getWidth())/2);
		int windowY = Math.max(0,  (screenSize.height - frame.getHeight())/2);
		frame.setLocation(windowX,  windowY);
		frame.setVisible(true);
		
		//Adding button control
		JPanel p = new JPanel();
		p.setPreferredSize(new Dimension(0,0));
		frame.add(p,BorderLayout.SOUTH);
		
		/*
		//keyBindings(p, frame, MAIN);
		
		private void keyBindings(JPanel p, final JFrame frame, MainWindow m) {
			ActionMap actionMap = p.getActionMap();
			InputMap inputMap = p.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
			
			inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0),  "F1");
			actionMap.put("F1",  new AbstractAction(){

				@Override
				public void actionPerformed(ActionEvent e) {
					MAIN.fullScreen(frame);
				}});
		}
		
		*/
		
		
	}
	

}
class MainWindow extends JPanel implements GLEventListener, KeyListener{
	GraphicsEnvironment graphicsEnvironment;
	public boolean isFullScreen = false;
	public  DisplayMode dm, dm_old;
	private  Dimension xgraphic;
	private  Point point = new Point(0, 0);
	private  PlayerShip player;
	FPSAnimator animator;
	public GLCanvas glcanvas;
	public int windowX, windowY;
	//Render r;
	
	private float rquad=15.0f, rtri=10.0f;
	
	private GLU glu = new GLU();
	
	public MainWindow(){
		
		final GLProfile profile = GLProfile.get(GLProfile.GL2);
		GLCapabilities capabilities = new GLCapabilities(profile);
		//The canvas
		glcanvas = new GLCanvas(capabilities);
		//r = new Render();
		glcanvas.addGLEventListener (this);
		glcanvas.setSize(1024,768);
		
		/*
		
		*/
		
		glcanvas.addKeyListener(this);
		
		//Testing player in
		player = new PlayerShip(glcanvas.getWidth()/2,glcanvas.getHeight()/2,glcanvas.getWidth(),glcanvas.getHeight());
		
		animator = new FPSAnimator(glcanvas, 60, true);
		animator.start();
	}
	
	protected void fullScreen(final JFrame frame){
		if(!isFullScreen){
			frame.dispose();
			frame.setUndecorated(true);
			frame.setVisible(true);
			frame.setResizable(false);
			xgraphic = frame.getSize();
			point = frame.getLocation();
			frame.setLocation(0, 0);
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.setSize((int)screenSize.getWidth(), (int)screenSize.getHeight());
			isFullScreen = true;
		}else{
			frame.dispose();
			frame.setUndecorated(false);
			frame.setResizable(true);
			frame.setLocation(point);
			frame.setSize(xgraphic);
			frame.setVisible(true);
			
			isFullScreen = false;
		}
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		//drawing stuff here~
		final GL2 gl= drawable.getGL().getGL2();
		gl.glClear(GL2.GL_COLOR_BUFFER_BIT|GL2.GL_DEPTH_BUFFER_BIT); //Clear the screen and the depth buffer
		
		//Figure 1
		gl.glLoadIdentity();				//resets values of gl Matrix
		gl.glTranslatef(player.getPosX(),player.getPosY(),0);				//initial position to draw (moved from center) TESSTTTIIINGGGGGGGGGGGG BOUNDS
		gl.glRotatef(player.rotation, 0, 0, 1);
		//gl.glRotated(0, 1.0f, 0, 0);	//set position for quad
		
		gl.glColor3f(0.75f, 0.75f, 0.75f);
		gl.glBegin(GL2.GL_TRIANGLES);					//Draw a Triangle
		gl.glVertex3f(0-player.shipW/2, 0-player.shipH/2, 0);							//ship back left
		gl.glVertex3f(player.shipW-player.shipW/2, player.shipH/2-player.shipH/2, 0);	//ship front
		gl.glVertex3f(0-player.shipW/2, player.shipH-player.shipH/2, 0);				//bottom right
		gl.glEnd();										//finished drawing ship
		
		
		gl.glFlush();
		
		/*rtri=(rtri+1f)%360;		//increasing rotation
		rquad=(rquad+1f)%360;		//increasing rotation  */
		player.move();
		//System.out.println(player.left+" , "+player.right);
	}
		
	@Override
	public void dispose(GLAutoDrawable drawable) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(GLAutoDrawable drawable) {
		//initializing stuff~
		final GL2 gl=drawable.getGL().getGL2();
		gl.glShadeModel(GL2.GL_SMOOTH);
		gl.glClearColor(0f, 0.5f, 0.5f, 0f);
		gl.glClearDepth(1.0f);
		gl.glEnable(GL2.GL_DEPTH_TEST);
		gl.glDepthFunc(GL2.GL_LEQUAL);
		gl.glHint(GL2.GL_PERSPECTIVE_CORRECTION_HINT, GL2.GL_NICEST);
		
	}

	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		final GL2 gl = drawable.getGL().getGL2();
		
		if(height<=0)
			height=1;
		final float h=(float) width/(float)height;
		gl.glViewport(0,0,width,height);
		gl.glMatrixMode(GL2.GL_PROJECTION);
		gl.glLoadIdentity();
		
		gl.glOrtho(0, glcanvas.getWidth(), glcanvas.getHeight(), 0, -1, 1);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		/*glu.gluPerspective(45.0f, h, 0.0001, 100.0);
		gl.glMatrixMode(GL2.GL_MODELVIEW);
		gl.glLoadIdentity();*/
		
	}
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
	}

	public void keyReleased(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_RIGHT)
			player.right=false;
		if(e.getKeyCode() == KeyEvent.VK_LEFT)
			player.left=false;
		if(e.getKeyCode() == KeyEvent.VK_UP)
			player.up=false;
		if(e.getKeyCode() == KeyEvent.VK_DOWN)
			player.down=false;
	}

	@Override
	public void keyTyped(KeyEvent e) {}
}
class PlayerShip{
	//Point2D location; //Decided to use simple X and Y in to no call location.getX and all every time, could be changed later.
	float posX, posY, preX, preY;
	float rotate= 2f, friction= 0.97f, spd=0.3f, rotation=0, sx,sy;
	int shipW=60, shipH=30;//ship dimensions
	int move= 1, maxSpeed=10;
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
		
		posX+=sx;
		posY+=sy;
		sx*=friction;
		sy*=friction;
	
		//Resetting position
		if (posX>screenX)
			posX=0-shipW;
		if (posX<0-shipW)
			posX=screenX;
		if (posY>screenY)
			posY=0-shipH;
		if (posY<0-shipH)
			posY=screenY;
		//System.out.println(Math.sin(Math.toRadians(rotation))+" , "+Math.cos(Math.toRadians(rotation))+" , "+Math.round(sx)+"-"+sx+" , "+Math.round(sy)+"-"+sy+ " , "+ rotation);
		System.out.println("Ship at point: "+(int)posX+" , "+(int)posY);
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
