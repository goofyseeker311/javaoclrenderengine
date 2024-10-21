package fi.jkauppa.javaoclrenderengine;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class JavaOCLRenderEngine extends JFrame {
	private static final long serialVersionUID = 1L;
	private Timer redrawtimer = new Timer();
	private long redrawrefreshrate = 60;
	private long redrawperiod = 1000/redrawrefreshrate;
	private DrawPanel graphicspanel = new DrawPanel();
	private Timer ticktimer = new Timer();
	private long tickrefreshrate = 240;
	private long tickperiod = 1000/tickrefreshrate;
	private ComputeLib computelib = new ComputeLib();
	private final String programSource =
			"kernel void range(global float *c) {"
					+ "unsigned int xid = get_global_id(0);"
					+ "c[xid] = (float)xid;"
					+ "}";
	private long device = computelib.devicelist[0];
	private long queue = this.computelib.createQueue(device);
	private long program = this.computelib.compileProgram(device, programSource);
	private long buffer = this.computelib.createBuffer(device, queue, 100);
	private float[] vbuffer = new float[100];

	public JavaOCLRenderEngine() {
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1280, 720);
		this.setVisible(true);
		this.setContentPane(graphicspanel);
		this.addKeyListener(graphicspanel);
		this.addMouseListener(graphicspanel);
		this.addMouseMotionListener(graphicspanel);
		this.addMouseWheelListener(graphicspanel);
		this.redrawtimer.scheduleAtFixedRate(new RedrawTimerTask(), 0, redrawperiod);
		this.ticktimer.scheduleAtFixedRate(new TickTimerTask(), 0, tickperiod);
		this.computelib.runProgram(this.device, this.queue, this.program,"range", buffer, buffer, buffer, 100);
		this.computelib.readBuffer(this.device, this.queue, buffer, vbuffer);
		System.out.print("vbuffer=");
		for (int i=0;i<vbuffer.length;i++) {
			System.out.print(" "+vbuffer[i]);
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		System.out.println("Java OpenCl Render Engine v0.2");
		@SuppressWarnings("unused")
		JavaOCLRenderEngine app = new JavaOCLRenderEngine();
	}
	
	private class RedrawTimerTask extends TimerTask {
		@Override public void run() {
			JavaOCLRenderEngine.this.graphicspanel.paintImmediately(JavaOCLRenderEngine.this.graphicspanel.getBounds());
		}
	}

	private class TickTimerTask extends TimerTask {
		@Override public void run() {
			JavaOCLRenderEngine.this.tick();
		}
	}
	
	private void tick() {
	}
	
	private class DrawPanel extends JComponent implements KeyListener,MouseListener,MouseMotionListener,MouseWheelListener {
		private static final long serialVersionUID = 1L;
		
		@Override public void paint(Graphics g) {
		}

		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {System.out.println("KeyEvent: "+e.getKeyChar());}
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void mouseWheelMoved(MouseWheelEvent e) {System.out.println("MouseWheelEvent: "+e.getWheelRotation());}
		@Override public void mouseDragged(MouseEvent e) {}
		@Override public void mouseMoved(MouseEvent e) {}
		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {System.out.println("MouseEvent: "+e.getX()+","+e.getY()+":"+e.getButton());}
		@Override public void mouseReleased(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
	}
}
