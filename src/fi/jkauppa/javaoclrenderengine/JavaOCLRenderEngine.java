package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.awt.AlphaComposite;
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
	private static String programtitle = "Java OpenCL Render Engine v1.0.0.0";
	private static GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
	private int[] pixelabgrbitmask = {0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000};
	private DrawPanel graphicspanel = null;
	private int graphicswidth = 0, graphicsheight = 0;
	private Dimension graphicdimensions = new Dimension(graphicswidth, graphicsheight);
	private SinglePixelPackedSampleModel newgraphicssamplemodel = null;
	private DirectColorModel newgraphicscolormodel = null;
	private float computetime = 0.0f;
	private float computetimeavg = 0.0f;
	private float frametime = 0.0f;
	private float frametimeavg = 0.0f;
	private Timer ticktimer = new Timer();
	private long tickrefreshrate = 60;
	private long tickperiod = 1000/tickrefreshrate;
	private ComputeLib computelib = new ComputeLib();
	private int selecteddevice = 0;
	private long device = NULL, queue = NULL, program = NULL;
	private Device devicedata = null;
	private String usingdevice = null;
	private BufferedImage graphicsimage = null;
	private long[] graphicspointerbuffer = new long[9];
	private int[] graphicsbuffer = null;
	private float[] graphicszbuffer = null;
	private float[] cameraposrot3fovres = null;
	private float[] trianglelistpos3rgba = null;
	private int[] trianglelistlength = null;
	private float[] triangletexturelist = null;
	private int[] triangletexturelength = null;
	private float[] trianglesphbvhlist = null;
	private int[] trianglesphbvhlength = null;
	private boolean keyfwd = false;
	private boolean keyback = false;
	private boolean keyleft = false;
	private boolean keyright = false;
	private boolean keyup = false;
	private boolean keydown = false;
	private long nanolasttimetick = System.nanoTime();
	private float lasttimedeltaseconds = 1.0f;

	public JavaOCLRenderEngine(int vselecteddevice) {
		JFrame.setDefaultLookAndFeelDecorated(true);
		this.graphicswidth = 1280;
		this.graphicsheight = 720;
		graphicdimensions = new Dimension(graphicswidth, graphicsheight);
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
		this.graphicsbuffer = new int[graphicswidth*graphicsheight];
		this.graphicszbuffer = new float[graphicswidth*graphicsheight];
		this.cameraposrot3fovres = new float[]{0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 70.0f,39.375f, graphicswidth,graphicsheight};
		this.trianglelistpos3rgba = new float[]{
				4.0f,-1.0f, 0.0f,  4.0f, 1.0f, 0.0f,  4.0f, 0.0f, 1.0f,  1.0f,0.0f,0.0f,1.0f,
				3.0f,-1.5f, 0.0f,  3.0f, 0.5f, 0.0f,  3.0f,-0.5f, 1.0f,  0.0f,1.0f,0.0f,1.0f,
				2.0f,-0.5f, 0.0f,  2.0f, 1.5f, 0.0f,  2.0f, 0.5f, 1.0f,  0.0f,0.0f,1.0f,1.0f,
				1.0f,-1.0f,-0.8f,  1.0f, 1.0f,-0.8f,  1.0f, 0.0f, 0.2f,  1.0f,0.0f,1.0f,1.0f,
		};
		this.trianglelistlength = new int[]{this.trianglelistpos3rgba.length/13};
		this.triangletexturelist = new float[]{1.0f};
		this.triangletexturelength = new int[]{1};
		this.trianglesphbvhlist = new float[]{2.0f,3.0f};
		this.trianglesphbvhlength = new int[]{2};
		this.selecteddevice = vselecteddevice;
		this.device = this.computelib.devicelist[selecteddevice];
		this.devicedata = this.computelib.devicemap.get(device);
		this.usingdevice = devicedata.devicename;
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		this.queue = devicedata.queue;
		this.graphicspointerbuffer[0] = computelib.createBuffer(device, graphicsbuffer.length);
		this.graphicspointerbuffer[1] = computelib.createBuffer(device, graphicszbuffer.length);
		this.graphicspointerbuffer[2] = computelib.createBuffer(device, cameraposrot3fovres.length);
		this.graphicspointerbuffer[3] = computelib.createBuffer(device, trianglelistpos3rgba.length);
		this.graphicspointerbuffer[4] = computelib.createBuffer(device, trianglelistlength.length);
		this.graphicspointerbuffer[5] = computelib.createBuffer(device, triangletexturelist.length);
		this.graphicspointerbuffer[6] = computelib.createBuffer(device, triangletexturelength.length);
		this.graphicspointerbuffer[7] = computelib.createBuffer(device, trianglesphbvhlist.length);
		this.graphicspointerbuffer[8] = computelib.createBuffer(device, trianglesphbvhlength.length);
		computelib.fillBufferi(graphicspointerbuffer[0], queue, 0x00000000, graphicsbuffer.length);
		String programSource = this.computelib.loadProgram("res/clprograms/programlib.cl", true);
		this.program = this.computelib.compileProgram(device, programSource);
		this.ticktimer.scheduleAtFixedRate(new TickTimerTask(), 0, tickperiod);
	}

	public static void main(String[] args) {
		System.out.println(programtitle);
		int argdevice = 0;
		try {argdevice = Integer.parseInt(args[0]);} catch(Exception ex) {}
		@SuppressWarnings("unused")
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice);
	}

	private class TickTimerTask extends TimerTask {@Override public void run() {
		long nanonewtimetick = System.nanoTime();
		lasttimedeltaseconds = (nanonewtimetick - nanolasttimetick)/1000000.0f;
		nanolasttimetick = nanonewtimetick;
		tick(lasttimedeltaseconds);}
	}

	private void tick(float deltatimeseconds) {
		float ds = deltatimeseconds;
		this.setTitle(programtitle+": "+String.format("%.0f",1000.0f/frametimeavg).replace(',', '.')+
				"fps, computetime: "+String.format("%.3f",computetimeavg).replace(',', '.')+"ms ["+usingdevice+"] ("
				+graphicswidth+"x"+graphicsheight+")"
				);
		graphicspanel.paintImmediately(graphicspanel.getBounds());
		int len = trianglelistlength[0]-1;
		trianglelistpos3rgba[13*len+9] += 0.0001f*ds; if (trianglelistpos3rgba[13*len+9]>1.0f) {trianglelistpos3rgba[13*len+9]=0.0f;}
		trianglelistpos3rgba[13*len+10] += 0.00015f*ds; if (trianglelistpos3rgba[13*len+10]>1.0f) {trianglelistpos3rgba[13*len+10]=0.0f;}
		trianglelistpos3rgba[13*len+11] += 0.000175f*ds; if (trianglelistpos3rgba[13*len+11]>1.0f) {trianglelistpos3rgba[13*len+11]=0.0f;}
		if (this.keyfwd) {cameraposrot3fovres[0] += 0.001*ds;}
		if (this.keyback) {cameraposrot3fovres[0] -= 0.001*ds;}
		if (this.keyleft) {cameraposrot3fovres[1] -= 0.001*ds;}
		if (this.keyright) {cameraposrot3fovres[1] += 0.001*ds;}
		if (this.keyup) {cameraposrot3fovres[2] += 0.001*ds;}
		if (this.keydown) {cameraposrot3fovres[2] -= 0.001*ds;}
		(new RenderThread()).start();
	}

	private class RenderThread extends Thread {
		private static boolean threadrunning = false;
		public void run() {
			if ((!threadrunning)&&(program!=NULL)) {
				threadrunning = true;
				long framestarttime = System.nanoTime();
				computelib.fillBufferi(graphicspointerbuffer[0], queue, 0x00000000, graphicsbuffer.length);
				computelib.fillBufferf(graphicspointerbuffer[1], queue, Float.POSITIVE_INFINITY, graphicszbuffer.length);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[2], cameraposrot3fovres);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[3], trianglelistpos3rgba);
				computelib.writeBufferi(device, queue, graphicspointerbuffer[4], trianglelistlength);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[5], triangletexturelist);
				computelib.writeBufferi(device, queue, graphicspointerbuffer[6], triangletexturelength);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[7], trianglesphbvhlist);
				computelib.writeBufferi(device, queue, graphicspointerbuffer[8], trianglesphbvhlength);
				computetime = computelib.runProgram(device, queue, program, "renderview", graphicspointerbuffer, new int[]{0}, new int[]{graphicswidth}, true);
				computetimeavg = computetimeavg*0.9f+computetime*0.1f;
				computelib.readBufferi(device, queue, graphicspointerbuffer[0], graphicsbuffer);
				DataBufferInt newgraphicsbuffer = new DataBufferInt(graphicsbuffer, graphicsbuffer.length);
				WritableRaster newgraphicsraster = WritableRaster.createWritableRaster(newgraphicssamplemodel, newgraphicsbuffer, null);
				BufferedImage newgraphicsimage = new BufferedImage(newgraphicscolormodel, newgraphicsraster, false, null);
				BufferedImage convertimage = gc.createCompatibleImage(graphicswidth, graphicsheight, Transparency.TRANSLUCENT);
				Graphics2D convertgfx = convertimage.createGraphics();
				convertgfx.setComposite(AlphaComposite.Src);
				convertgfx.scale(1, -1);
				convertgfx.translate(0, -newgraphicsimage.getHeight());
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
	
	private class DrawPanel extends JPanel implements KeyListener,MouseListener,MouseMotionListener,MouseWheelListener,ComponentListener {
		private static final long serialVersionUID = 1L;
		
		@Override public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D)g;
			g2.setComposite(AlphaComposite.Src);
			g2.drawImage(graphicsimage, 0, 0, null);
		}

		@Override public void keyTyped(KeyEvent e) {}
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
		
		@Override public void keyPressed(KeyEvent e) {
			System.out.println("KeyPressed: '"+e.getKeyChar()+"'");
			if (e.getKeyCode()==KeyEvent.VK_W) {keyfwd = true;}
			if (e.getKeyCode()==KeyEvent.VK_S) {keyback = true;}
			if (e.getKeyCode()==KeyEvent.VK_A) {keyleft = true;}
			if (e.getKeyCode()==KeyEvent.VK_D) {keyright = true;}
			if (e.getKeyCode()==KeyEvent.VK_SPACE) {keyup = true;}
			if (e.getKeyCode()==KeyEvent.VK_SHIFT) {keydown = true;}
		}
		@Override public void keyReleased(KeyEvent e) {
			System.out.println("KeyReleased: '"+e.getKeyChar()+"'");
			if (e.getKeyCode()==KeyEvent.VK_W) {keyfwd = false;}
			if (e.getKeyCode()==KeyEvent.VK_S) {keyback = false;}
			if (e.getKeyCode()==KeyEvent.VK_A) {keyleft = false;}
			if (e.getKeyCode()==KeyEvent.VK_D) {keyright = false;}
			if (e.getKeyCode()==KeyEvent.VK_SPACE) {keyup = false;}
			if (e.getKeyCode()==KeyEvent.VK_SHIFT) {keydown = false;}
		}
	}

}
