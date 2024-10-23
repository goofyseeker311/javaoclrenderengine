package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferFloat;
import java.awt.image.WritableRaster;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JFrame;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class JavaOCLRenderEngine extends JFrame {
	private static final long serialVersionUID = 1L;
	private static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	private Timer redrawtimer = new Timer();
	private long redrawrefreshrate = 60;
	private long redrawperiod = 1000/redrawrefreshrate;
	private DrawPanel graphicspanel = new DrawPanel();
	private int graphicswidth = 1280, graphicsheight = 720;
	private int graphicpixels = graphicswidth*graphicsheight;
	private float[] graphicsbuffer = new float[4*graphicpixels];
	private ComponentSampleModel readsamplemodel = new ComponentSampleModel(DataBuffer.TYPE_FLOAT,graphicswidth,graphicsheight,4,4*graphicswidth,new int[]{1,2,3,0});
	private ColorModel readcolormodel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[]{32,32,32,32}, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
	private BufferedImage graphicsimage = gc.createCompatibleImage(graphicswidth, graphicsheight, Transparency.TRANSLUCENT);
	private Graphics2D graphicsimage2d = graphicsimage.createGraphics();
	private Timer ticktimer = new Timer();
	private long tickrefreshrate = 240;
	private long tickperiod = 1000/tickrefreshrate;
	private ComputeLib computelib = new ComputeLib();
	private int selecteddevice;
	private long device, queue, program;
	private Device devicedata;
	private long[] buffer = new long[1];

	public JavaOCLRenderEngine(int vselecteddevice) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(graphicswidth, graphicsheight);
		this.setLocationRelativeTo(null);
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
		this.selecteddevice = vselecteddevice;
		this.device = this.computelib.devicelist[selecteddevice];
		this.devicedata = this.computelib.devicemap.get(device);
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		this.queue = devicedata.queue;
		this.buffer[0] = this.computelib.createBuffer(device, queue, graphicsbuffer.length);
		this.program = this.computelib.compileProgram(device, ProgramLib.programSource);
		Graphics2D g2 = this.graphicsimage.createGraphics();
		g2.setColor(new Color(0.0f,0.0f,1.0f,1.0f));
		g2.fillRect(0, 0, this.graphicsimage.getWidth(), this.graphicsimage.getHeight());
		g2.dispose();
		this.repaint();
	}
	
	public static void main(String[] args) {
		System.out.println("Java OpenCl Render Engine v0.8");
		int argdevice = 0;
		try {argdevice = Integer.parseInt(args[0]);} catch(Exception ex) {}
		@SuppressWarnings("unused")
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice);
	}
	
	private class RedrawTimerTask extends TimerTask {@Override public void run() {graphicspanel.paintImmediately(graphicspanel.getBounds());}}
	private class TickTimerTask extends TimerTask {@Override public void run() {tick();}}
	
	private void tick() {}
	
	private class DrawPanel extends JComponent implements KeyListener,MouseListener,MouseMotionListener,MouseWheelListener {
		private static final long serialVersionUID = 1L;
		
		@Override public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			if (program!=NULL) {
				computelib.runProgram(device, queue, program, "renderrgbapixels", buffer, 0, graphicpixels);
				computelib.readBuffer(device, queue, buffer[0], graphicsbuffer);
				DataBufferFloat readdatabuffer = new DataBufferFloat(graphicsbuffer, graphicsbuffer.length);
				WritableRaster readraster = WritableRaster.createWritableRaster(readsamplemodel, readdatabuffer, null);
				BufferedImage readimage = new BufferedImage(readcolormodel, readraster, false, null);
				graphicsimage2d.drawImage(readimage, 0, 0, null);
			}
			g2.drawImage(graphicsimage, 0, 0, null);
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
