package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.Timer;
import java.util.TimerTask;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL46;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class JavaOCLRenderEngine {
	private static String programtitle = "Java OpenCL Render Engine v0.9.9";
	private int graphicswidth = 0, graphicsheight = 0;
	private long window = NULL;
	private int[] graphicsbuffer = null;
	private long[] graphicspointerbuffer = new long[4];
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
	private float[] cameraposrot3fovres = null;
	private float[] trianglelistpos3rgba = null;
	private int[] trianglelistlength = {0};
	private long monitor = NULL;
	private GLFWVidMode videomode = null;
	private KeyProcessor keyprocessor = new KeyProcessor();
	private MousePositionProcessor mouseposprocessor = new MousePositionProcessor();
	private MouseButtonProcessor mousebuttonprocessor = new MouseButtonProcessor();
	private MouseWheelProcessor mousewheelprocessor = new MouseWheelProcessor();

	public JavaOCLRenderEngine(int vselecteddevice) {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {System.out.println("GLFW init failed."); System.exit(1);}
		this.monitor = GLFW.glfwGetPrimaryMonitor();
		this.videomode = GLFW.glfwGetVideoMode(this.monitor);
		this.graphicswidth = this.videomode.width();
		this.graphicsheight = this.videomode.height();
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		if ((window=GLFW.glfwCreateWindow(graphicswidth, graphicsheight, programtitle, monitor, NULL))==NULL) {System.out.println("GLFW create window failed."); System.exit(2);}
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		GLFW.glfwSetKeyCallback(window, keyprocessor);
		GLFW.glfwSetCursorPosCallback(window, mouseposprocessor);
		GLFW.glfwSetMouseButtonCallback(window, mousebuttonprocessor);
		GLFW.glfwSetScrollCallback(window, mousewheelprocessor);
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		GLFW.glfwShowWindow(window);
		GL.createCapabilities();
		GL46.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GL46.glClear(GL46.GL_COLOR_BUFFER_BIT);
		GLFW.glfwSwapBuffers(window);
		float[] initcameraposrot3fovres = {0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 70.0f,39.375f, graphicswidth,graphicsheight};
		float[] inittrianglelistpos3rgba = {
				1.0f,-1.0f,0.0f,  1.0f, 1.0f,0.0f,  1.0f, 0.0f,1.0f,  1.0f,0.0f,0.0f,1.0f,
				1.0f,-3.0f,0.0f,  1.0f,-1.0f,0.0f,  1.0f,-2.0f,1.0f,  0.0f,1.0f,0.0f,1.0f,
				1.0f, 1.0f,0.0f,  1.0f, 3.0f,0.0f,  1.0f, 2.0f,1.0f,  0.0f,0.0f,1.0f,1.0f,
				1.0f,-1.0f,2.0f,  1.0f, 1.0f,2.0f,  1.0f, 0.0f,3.0f,  1.0f,0.0f,1.0f,1.0f
		};
		this.cameraposrot3fovres = initcameraposrot3fovres;
		this.trianglelistpos3rgba = inittrianglelistpos3rgba;
		this.trianglelistlength[0] = trianglelistpos3rgba.length/13;
		this.ticktimer.scheduleAtFixedRate(new TickTimerTask(), 0, tickperiod);
		this.selecteddevice = vselecteddevice;
		this.device = this.computelib.devicelist[selecteddevice];
		this.devicedata = this.computelib.devicemap.get(device);
		this.usingdevice = devicedata.devicename;
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		this.queue = devicedata.queue;
		this.graphicsbuffer = new int[graphicswidth*graphicsheight];
		this.graphicspointerbuffer[0] = computelib.createBuffer(device, graphicsbuffer.length);
		this.graphicspointerbuffer[1] = computelib.createBuffer(device, cameraposrot3fovres.length);
		this.graphicspointerbuffer[2] = computelib.createBuffer(device, trianglelistpos3rgba.length);
		this.graphicspointerbuffer[3] = computelib.createBuffer(device, trianglelistlength.length);
		String programSource = this.computelib.loadProgram("res/clprograms/programlib.cl", true);
		this.program = this.computelib.compileProgram(device, programSource);
	}

	public void run() {
		while(!GLFW.glfwWindowShouldClose(window)) {
			GL46.glClear(GL46.GL_COLOR_BUFFER_BIT | GL46.GL_DEPTH_BUFFER_BIT);
			GL46.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
			GL46.glEnable(GL46.GL_TEXTURE_RECTANGLE);
			GL46.glTexImage2D(GL46.GL_TEXTURE_RECTANGLE, 0, GL46.GL_RGBA8, graphicswidth, graphicsheight, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_INT_8_8_8_8, graphicsbuffer);
			GL46.glBindTexture(GL46.GL_TEXTURE_2D, 0);
			GL46.glBegin(GL46.GL_TRIANGLE_STRIP);
			GL46.glTexCoord2f(1.0f, 0.0f); GL46.glVertex2f(1.0f, -1.0f);
			GL46.glTexCoord2f(1.0f, 1.0f); GL46.glVertex2f(1.0f, 1.0f);
			GL46.glTexCoord2f(0.0f, 0.0f); GL46.glVertex2f(-1.0f, -1.0f);
			GL46.glTexCoord2f(0.0f, 1.0f); GL46.glVertex2f(-1.0f, 1.0f);
			GL46.glEnd();
			GL46.glFlush();
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		System.exit(0);
	}

	public static void main(String[] args) {
		System.out.println(programtitle);
		int argdevice = 0;
		try {argdevice = Integer.parseInt(args[0]);} catch(Exception ex) {}
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice);
		app.run();
	}

	private class TickTimerTask extends TimerTask {@Override public void run() {tick();}}

	private void tick() {
		GLFW.glfwSetWindowTitle(window, programtitle+": "+String.format("%.0f",1000.0f/frametimeavg).replace(',', '.')+
				"fps, computetime: "+String.format("%.3f",computetimeavg).replace(',', '.')+"ms ["+usingdevice+"] ("
				+graphicswidth+"x"+graphicsheight+")"
				);
		int len = trianglelistlength[0]-1;
		trianglelistpos3rgba[13*len+9] += 0.001f; if (trianglelistpos3rgba[13*len+9]>1.0f) {trianglelistpos3rgba[13*len+9]=0.0f;}
		trianglelistpos3rgba[13*len+10] += 0.0015f; if (trianglelistpos3rgba[13*len+10]>1.0f) {trianglelistpos3rgba[13*len+10]=0.0f;}
		trianglelistpos3rgba[13*len+11] += 0.00175f; if (trianglelistpos3rgba[13*len+11]>1.0f) {trianglelistpos3rgba[13*len+11]=0.0f;}
		(new RenderThread()).start();
	}

	private class RenderThread extends Thread {
		private static boolean threadrunning = false;
		public void run() {
			if ((!threadrunning)&&(program!=NULL)) {
				threadrunning = true;
				long framestarttime = System.nanoTime();
				computelib.fillBufferi(graphicspointerbuffer[0], queue, 0x00000000, graphicsbuffer.length);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[1], cameraposrot3fovres);
				computelib.writeBufferf(device, queue, graphicspointerbuffer[2], trianglelistpos3rgba);
				computelib.writeBufferi(device, queue, graphicspointerbuffer[3], trianglelistlength);
				computetime = computelib.runProgram(device, queue, program, "renderview", graphicspointerbuffer, 0, graphicswidth, true);
				computetimeavg = computetimeavg*0.9f+computetime*0.1f;
				int[] newgraphicsbuffer = new int[graphicswidth*graphicsheight];
				computelib.readBufferi(device, queue, graphicspointerbuffer[0], newgraphicsbuffer);
				graphicsbuffer = newgraphicsbuffer;
				long frameendtime = System.nanoTime();
				frametime = (frameendtime-framestarttime)/1000000.0f;
				frametimeavg = frametimeavg*0.9f+frametime*0.1f;
				threadrunning = false;
			}
		}
	}

	private class KeyProcessor implements GLFWKeyCallbackI {
		@Override public void invoke(long window, int key, int scancode, int action, int mods) {
			System.out.println("key: "+key+" scancode: "+scancode+" action: "+action+" mods: "+mods);
		}
	}
	private class MousePositionProcessor implements GLFWCursorPosCallbackI {
		@Override public void invoke(long window, double xpos, double ypos) {
			System.out.println("xpos: "+xpos+" ypos: "+ypos);
		}
	}
	private class MouseButtonProcessor implements GLFWMouseButtonCallbackI {
		@Override public void invoke(long window, int button, int action, int mods) {
			System.out.println("button: "+button+" action: "+action+" mods: "+mods);
		}
	}
	private class MouseWheelProcessor implements GLFWScrollCallbackI {
		@Override public void invoke(long window, double xoffset, double yoffset) {
			System.out.println("xoffset: "+xoffset+" yoffset: "+yoffset);
		}
	}
	
}
