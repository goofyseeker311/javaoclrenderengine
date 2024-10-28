package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.awt.image.WritableRaster;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JFrame;
import javax.swing.JPanel;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class JavaOCLRenderEngine extends JFrame {
	private static final long serialVersionUID = 1L;
	private static String programtitle = "Java OpenCL Render Engine v0.9.2";
	private static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	private int[] pixelabgrbitmask = {0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000};
	private DrawPanel graphicspanel = null;
	private int graphicswidth = 1280, graphicsheight = 720;
	private Dimension graphicdimensions = new Dimension(graphicswidth, graphicsheight);
	private SinglePixelPackedSampleModel newgraphicssamplemodel = null;
	private DirectColorModel newgraphicscolormodel = null;
	private int[] graphicsbuffer = null;
	private long[] graphicspointerbuffer = new long[4];
	private BufferedImage graphicsimage = null;
	private float computetime = 0.0f;
	private float computetimeavg = 0.0f;
	private float frametime = 0.0f;
	private float frametimeavg = 0.0f;
	private Timer ticktimer = new Timer();
	private long tickrefreshrate = 60;
	private long tickperiod = 1000/tickrefreshrate;
	private ComputeLib computelib = new ComputeLib();
	private int selecteddevice;
	private long device, queue, program;
	private Device devicedata;
	private String usingdevice;
	private float[] cameraposdirrotfovres = {0.0f,0.0f,0.0f,1.0f,0.0f,0.0f,0.0f,70.0f,39.375f,graphicswidth,graphicsheight};
	private float[] trianglelistpos3rgba = {1.0f,-1.0f,0.0f,1.0f,1.0f,0.0f,1.0f,0.0f,1.0f,1.0f,0.0f,0.0f,1.0f};
	private int[] trianglelistlength = {1};

	public JavaOCLRenderEngine(int vselecteddevice) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setLocationRelativeTo(null);
		this.setFocusTraversalKeysEnabled(false);
		this.setResizable(false);
		this.setTitle(programtitle);
		this.setVisible(true);
		this.graphicspanel = new DrawPanel();
		this.graphicspanel.setSize(graphicdimensions);
		this.graphicspanel.setPreferredSize(graphicdimensions);
		this.newgraphicssamplemodel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, graphicswidth, graphicsheight, pixelabgrbitmask);
		this.newgraphicscolormodel = new DirectColorModel(32, pixelabgrbitmask[0], pixelabgrbitmask[1], pixelabgrbitmask[2], pixelabgrbitmask[3]);
		this.graphicsimage = gc.createCompatibleImage(graphicswidth, graphicsheight, Transparency.TRANSLUCENT);
		this.setContentPane(graphicspanel);
		this.requestFocus();
		this.addComponentListener(graphicspanel);
		this.addKeyListener(graphicspanel);
		this.addMouseListener(graphicspanel);
		this.addMouseMotionListener(graphicspanel);
		this.addMouseWheelListener(graphicspanel);
		this.pack();
		this.ticktimer.scheduleAtFixedRate(new TickTimerTask(), 0, tickperiod);
		this.selecteddevice = vselecteddevice;
		this.device = this.computelib.devicelist[selecteddevice];
		this.devicedata = this.computelib.devicemap.get(device);
		this.usingdevice = devicedata.devicename;
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		this.queue = devicedata.queue;
		this.graphicsbuffer = new int[graphicswidth*graphicsheight];
		this.graphicspointerbuffer[0] = computelib.createBuffer(device, graphicsbuffer.length);
		this.graphicspointerbuffer[1] = computelib.createBuffer(device, cameraposdirrotfovres.length);
		this.graphicspointerbuffer[2] = computelib.createBuffer(device, trianglelistpos3rgba.length);
		this.graphicspointerbuffer[3] = computelib.createBuffer(device, trianglelistlength.length);
		String programSource = this.computelib.loadProgram("res/clprograms/programlib.cl", true);
		this.program = this.computelib.compileProgram(device, programSource);
		Graphics2D g2 = this.graphicsimage.createGraphics();
		g2.setColor(Color.WHITE);
		g2.fillRect(0, 0, this.graphicsimage.getWidth(), this.graphicsimage.getHeight());
		g2.dispose();
		this.repaint();
	}
	
	public static void main(String[] args) {
		System.out.println(programtitle);
		int argdevice = 0;
		try {argdevice = Integer.parseInt(args[0]);} catch(Exception ex) {}
		@SuppressWarnings("unused")
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice);
	}
	
	private class TickTimerTask extends TimerTask {@Override public void run() {tick();}}
	
	private void tick() {
		this.setTitle(programtitle+": "+String.format("%.0f",1000.0f/frametimeavg).replace(',', '.')+"fps, computetime: "+String.format("%.3f",computetimeavg).replace(',', '.')+"ms ["+usingdevice+"]");
		trianglelistpos3rgba[9] += 0.001f; if (trianglelistpos3rgba[9]>1.0f) {trianglelistpos3rgba[9]=0.0f;}
		trianglelistpos3rgba[10] += 0.0015f; if (trianglelistpos3rgba[10]>1.0f) {trianglelistpos3rgba[10]=0.0f;}
		trianglelistpos3rgba[11] += 0.00175f; if (trianglelistpos3rgba[11]>1.0f) {trianglelistpos3rgba[11]=0.0f;}
		graphicspanel.paintImmediately(graphicspanel.getBounds());
		(new RenderThread()).start();
	}
	
	private class DrawPanel extends JPanel implements KeyListener,MouseListener,MouseMotionListener,MouseWheelListener,ComponentListener {
		private static final long serialVersionUID = 1L;
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			g2.setComposite(AlphaComposite.Src);
			g2.drawImage(graphicsimage, 0, 0, null);
		}

		@Override public void keyTyped(KeyEvent e) {}
		@Override public void keyPressed(KeyEvent e) {System.out.println("KeyPressed: '"+e.getKeyChar()+"'");}
		@Override public void keyReleased(KeyEvent e) {}
		@Override public void mouseWheelMoved(MouseWheelEvent e) {System.out.println("MouseWheelMoved: "+e.getWheelRotation());}
		@Override public void mouseDragged(MouseEvent e) {System.out.println("MouseDragged: "+e.getX()+","+e.getY()+":"+e.getButton());}
		@Override public void mouseMoved(MouseEvent e) {System.out.println("MouseMoved: "+e.getX()+","+e.getY()+":"+e.getButton());}
		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mousePressed(MouseEvent e) {System.out.println("MousePressed: "+e.getX()+","+e.getY()+":"+e.getButton());}
		@Override public void mouseReleased(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void componentMoved(ComponentEvent e) {}
		@Override public void componentShown(ComponentEvent e) {}
		@Override public void componentHidden(ComponentEvent e) {}
		@Override public void componentResized(ComponentEvent e) {}
	}
	
	private class RenderThread extends Thread {
		private static boolean threadrunning = false;
		public void run() {
			if ((!threadrunning)&&(program!=NULL)) {
				threadrunning = true;
				long framestarttime = System.nanoTime();
				computelib.fillBufferi(graphicspointerbuffer[0], queue, 0x00000000, graphicsbuffer.length);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[1], cameraposdirrotfovres);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[2], trianglelistpos3rgba);
				computelib.writeBufferi(device, queue, graphicspointerbuffer[3], trianglelistlength);
				computetime = computelib.runProgram(device, queue, program, "renderview", graphicspointerbuffer, 0, graphicswidth, true);
				computetimeavg = computetimeavg*0.9f+computetime*0.1f;
				computelib.readBufferi(device, queue, graphicspointerbuffer[0], graphicsbuffer);
				DataBufferInt newgraphicsbuffer = new DataBufferInt(graphicsbuffer, graphicsbuffer.length);
				WritableRaster newgraphicsraster = WritableRaster.createWritableRaster(newgraphicssamplemodel, newgraphicsbuffer, null);
				BufferedImage newgraphicsimage = new BufferedImage(newgraphicscolormodel, newgraphicsraster, false, null);
				BufferedImage convertimage = gc.createCompatibleImage(graphicswidth, graphicsheight, Transparency.TRANSLUCENT);
				Graphics2D convertgfx = convertimage.createGraphics();
				convertgfx.setComposite(AlphaComposite.Src);
				convertgfx.drawImage(newgraphicsimage, 0, 0, null);
				convertgfx.dispose();
				graphicsimage = convertimage;
				long frameendtime = System.nanoTime();
				frametime = (frameendtime-framestarttime)/1000000.0f;
				frametimeavg = frametimeavg*0.9f+frametime*0.1f;
				threadrunning = false;
			}
		}
	}
	
}
