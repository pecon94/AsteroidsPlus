package AsteroidsPlus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.*;

public class asteroids extends JPanel implements ActionListener, KeyListener {
	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	Timer t = new Timer(5, this);
	int x = 0, y = 0, velx = 0, vely = 0;

	public asteroids() {
		t.start();
		addKeyListener(this);
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.red);

		g.fillRect(x, y, 10, 10);
	}

	public void actionPerformed(ActionEvent e) {
		if (x < 0) {
			velx = 0;
			x = 0;
		}

		if (x > 530) {
			velx = 0;
			x = 530;
		}

		if (y < 0) {
			vely = 0;
			y = 0;
		}

		if (y > 330) {
			vely = 0;
			y = 330;
		}

		x += velx;
		y += vely;
		repaint();
	}

	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();

		if (code == KeyEvent.VK_DOWN) {
			vely = 1;
			velx = 0;
		}
		if (code == KeyEvent.VK_UP) {
			vely = -1;
			velx = 0;
		}
		if (code == KeyEvent.VK_LEFT) {
			vely = 0;
			velx = -1;
		}
		if (code == KeyEvent.VK_RIGHT) {
			vely = 0;
			velx = 1;

		}
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		velx = 0;
		vely = 0;
	}

	public static void main(String arge[]) {

		JFrame jframe = new JFrame("Asteroids+");
		jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		jframe.getContentPane().setBackground(Color.black);
		jframe.add(new asteroids());
		jframe.setPreferredSize(new Dimension(500, 400));
		jframe.pack();
		jframe.setLocationRelativeTo(null);
		jframe.setVisible(true);
		

		/*
		 * 
		 * setTitle("Background Color for JFrame"); setSize(400,400);
		 * setLocationRelativeTo(null); setDefaultCloseOperation(EXIT_ON_CLOSE);
		 * setVisible(true); /*
		 * 
		 * setLayout(new BorderLayout()); JLabel background=new JLabel(new
		 * ImageIcon("/Users/sandeepshahi/desktop/image.jpeg")); add(background);
		 * background.setLayout(new FlowLayout()); l1=new JLabel("Here is a button");
		 * b1=new JButton("I am a button"); background.add(l1); background.add(b1);
		 */

	}

}