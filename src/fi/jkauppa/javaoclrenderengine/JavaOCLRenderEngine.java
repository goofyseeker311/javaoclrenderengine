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

import javax.swing.JComponent;
import javax.swing.JFrame;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class JavaOCLRenderEngine extends JFrame {
	private static final long serialVersionUID = 1L;
	private static String programtitle = "Java OpenCl Render Engine v0.8.5";
	private static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	private int[] pixelabgrbitmask = {0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000};
	private DrawPanel graphicspanel = null;
	private int graphicswidth = 1280, graphicsheight = 720;
	private Dimension graphicdimensions = new Dimension(graphicswidth, graphicsheight);
	private int[] graphicsbuffer = null;
	private SinglePixelPackedSampleModel readsamplemodel = null;
	private DirectColorModel readcolormodel = null;
	private BufferedImage graphicsimage = null;
	private Graphics2D graphicsimage2d = null;
	private float frametime = 0.0f;
	private float frametimeavg = 0.0f;
	private Timer ticktimer = new Timer();
	private long tickrefreshrate = 240;
	private long tickperiod = 1000/tickrefreshrate;
	private ComputeLib computelib = new ComputeLib();
	private int selecteddevice;
	private long device, queue, program;
	private Device devicedata;
	private long[] gfxbuffer = new long[1];

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
		this.graphicsbuffer = new int[graphicswidth*graphicsheight];
		this.readsamplemodel = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, graphicswidth, graphicsheight, pixelabgrbitmask);
		this.readcolormodel = new DirectColorModel(32, pixelabgrbitmask[0], pixelabgrbitmask[1], pixelabgrbitmask[2], pixelabgrbitmask[3]);
		this.graphicsimage = gc.createCompatibleImage(graphicswidth, graphicsheight, Transparency.TRANSLUCENT);
		this.graphicsimage2d = graphicsimage.createGraphics();
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
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		this.queue = devicedata.queue;
		this.gfxbuffer[0] = this.computelib.createBuffer(device, queue, graphicsbuffer.length);
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
		this.setTitle(programtitle+": "+(1000.0f/frametimeavg)+"fps");
		graphicspanel.paintImmediately(graphicspanel.getBounds());
		RenderThread renderthread = new RenderThread();
		renderthread.start();
	}
	
	private class DrawPanel extends JComponent implements KeyListener,MouseListener,MouseMotionListener,MouseWheelListener,ComponentListener {
		private static final long serialVersionUID = 1L;
		
		@Override public void paintComponent(Graphics g) {
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
		private static boolean running = false;
		public void run() {
			if ((!running)&&(program!=NULL)) {
				running = true;
				long frametimestart = System.nanoTime();
				computelib.runProgram(device, queue, program, "renderview", gfxbuffer, 0, graphicsbuffer.length);
				computelib.readBufferi(device, queue, gfxbuffer[0], graphicsbuffer);
				DataBufferInt readdatabuffer = new DataBufferInt(graphicsbuffer, graphicsbuffer.length);
				WritableRaster readraster = WritableRaster.createWritableRaster(readsamplemodel, readdatabuffer, null);
				BufferedImage readimage = new BufferedImage(readcolormodel, readraster, false, null);
				graphicsimage2d.setComposite(AlphaComposite.Src);
				graphicsimage2d.drawImage(readimage, 0, 0, null);
				long frametimeend = System.nanoTime();
				frametime = (frametimeend-frametimestart)/1000000.0f;
				frametimeavg = frametimeavg*0.9f+frametime*0.1f;
				running = false;
			}
		}
	}
}
