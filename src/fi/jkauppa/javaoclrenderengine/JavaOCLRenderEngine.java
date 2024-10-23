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

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

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
	private int selecteddevice = 0;

	public JavaOCLRenderEngine(int vselecteddevice) {
		this.selecteddevice = vselecteddevice;
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(1280, 720);
		this.setVisible(true);
		this.setFocusTraversalKeysEnabled(false);
		this.setContentPane(graphicspanel);
		this.requestFocus();
		this.addKeyListener(graphicspanel);
		this.addMouseListener(graphicspanel);
		this.addMouseMotionListener(graphicspanel);
		this.addMouseWheelListener(graphicspanel);
		this.redrawtimer.scheduleAtFixedRate(new RedrawTimerTask(), 0, redrawperiod);
		this.ticktimer.scheduleAtFixedRate(new TickTimerTask(), 0, tickperiod);
		int buffersize = 10;
		float[] vbuffer = new float[buffersize];
		long device = this.computelib.devicelist[this.selecteddevice];
		Device devicedata = this.computelib.devicemap.get(device);
		System.out.println("Using device["+this.selecteddevice+"]: "+devicedata.devicename);
		long queue = devicedata.queue;
		long[] buffer = {this.computelib.createBuffer(device, queue, buffersize)};
		String programSource = "kernel void range(global float *c) { unsigned int xid=get_global_id(0); c[xid]=((float)xid)+13.5f; }";
		long program = this.computelib.compileProgram(device, programSource);
		this.computelib.runProgram(device, queue, program,"range", buffer, 0, buffersize);
		this.computelib.readBuffer(device, queue, buffer[0], vbuffer);
		System.out.print("vbuffer=");
		for (int i=0;i<vbuffer.length;i++) {
			System.out.print(" "+vbuffer[i]);
		}
		System.out.println();
	}
	
	public static void main(String[] args) {
		System.out.println("Java OpenCl Render Engine v0.5");
		int argdevice = 0;
		try {
			argdevice = Integer.parseInt(args[0]);
		} catch(Exception ex) {}
		@SuppressWarnings("unused")
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice);
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
		@Override public void keyPressed(KeyEvent e) {System.out.println("KeyEvent: '"+e.getKeyChar()+"'");}
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
