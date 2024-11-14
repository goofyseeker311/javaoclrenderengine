package fi.jkauppa.javaoclrenderengine;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class JavaOCLRenderEngine {
	private Random rnd = new Random();
	private static String programtitle = "Java OpenCL Render Engine v1.0.3.6";
	private int screenwidth = 0, screenheight = 0, graphicswidth = 0, graphicsheight = 0, graphicslength = 0;
	private float graphicshfov = 70.0f, graphicsvfov = 39.375f;
	private long window = MemoryUtil.NULL;
	@SuppressWarnings("unused")
	private GLCapabilities caps = null;
	@SuppressWarnings("unused")
	private Callback debugProc = null;
	private int vao = 0;
	private int tex = 0;
	private int buf = 0;
	private int quadProgram = 0;
	private int quadProgram_inputPosition = 0;
	private int quadProgram_inputTextureCoords = 0;
	private float frametime = 0.0f;
	private float frametimeavg = 0.0f;
	private ComputeLib computelib = null;
	private int selecteddevice = 0;
	@SuppressWarnings("unused")
	private boolean isfullscreen = false;
	private boolean glinterop = true;
	private long device = MemoryUtil.NULL, queue = MemoryUtil.NULL, program = MemoryUtil.NULL;
	private Device devicedata = null;
	private String usingdevice = null;
	private long[] graphicspointerbuffer = new long[7];
	private float[] graphicsbuffer = null;
	@SuppressWarnings("unused")
	private float[] graphicszbuffer = null;
	private int[] graphicshbuffer = null;
	private float[] cameraposrot3fovres = null;
	private float[] trianglelistpos3iduv3 = null;
	private int trianglelistlength = 0;
	private int[] triangletexturelist = null;
	private float[] objectlistpos3sca3rot3 = null;
	private int objectlistlength = 0;
	private Clip[] cannonsound = null;
	private int cannonsoundind = 0;
	private boolean cannonfiring = false;
	private float cannonfiringlast = 0;
	private float cannonfiringdelta = 0.02f;
	private boolean keyfwd = false;
	private boolean keyback = false;
	private boolean keyleft = false;
	private boolean keyright = false;
	private boolean keyup = false;
	private boolean keydown = false;
	private long nanolasttimetick = System.nanoTime();
	private double[] lastmousex = {0}, lastmousey = {0};
	private float lasttimedeltaseconds = 0.0f;
	private long monitor = MemoryUtil.NULL;
	private GLFWVidMode videomode = null;
	private KeyProcessor keyprocessor = new KeyProcessor();
	private MousePositionProcessor mouseposprocessor = new MousePositionProcessor();
	private MouseButtonProcessor mousebuttonprocessor = new MouseButtonProcessor();
	private MouseWheelProcessor mousewheelprocessor = new MouseWheelProcessor();

	public JavaOCLRenderEngine(int vselecteddevice, int vfullscreen, int vglinterop) {
		GLFWErrorCallback.createPrint(System.err).set();
		if (!GLFW.glfwInit()) {System.out.println("GLFW init failed."); System.exit(1);}
		this.monitor = GLFW.glfwGetPrimaryMonitor();
		this.videomode = GLFW.glfwGetVideoMode(this.monitor);
		this.screenwidth = 1280;
		this.screenheight = 720;
		long fullscreenmonitor = MemoryUtil.NULL;
		if (vfullscreen!=0) {
			this.isfullscreen = true;
			fullscreenmonitor = monitor;
			this.screenwidth = videomode.width();
			this.screenheight = videomode.height();
		}
		this.graphicswidth = screenwidth;
		this.graphicsheight = screenheight;
		if (vglinterop==0) {
			this.glinterop = false;
		}
		this.graphicshfov = (float)(Math.toDegrees(2.0f*Math.atan((((double)this.graphicswidth)/((double)this.graphicsheight))*Math.tan(Math.toRadians((double)(this.graphicsvfov/2.0f))))));
		this.graphicslength = this.graphicswidth*this.graphicsheight;
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, videomode.redBits());
		GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, videomode.greenBits());
		GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, videomode.blueBits());
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		if ((window=GLFW.glfwCreateWindow(screenwidth, screenheight, programtitle, fullscreenmonitor, MemoryUtil.NULL))==MemoryUtil.NULL) {System.out.println("GLFW create window failed."); System.exit(2);}
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		GLFW.glfwSetKeyCallback(window, keyprocessor);
		GLFW.glfwSetCursorPosCallback(window, mouseposprocessor);
		GLFW.glfwSetMouseButtonCallback(window, mousebuttonprocessor);
		GLFW.glfwSetScrollCallback(window, mousewheelprocessor);
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		GLFW.glfwShowWindow(window);
		caps = GL.createCapabilities();
		debugProc = GLUtil.setupDebugMessageCallback();
		createQuadProgram();
		createFullScreenQuad();
		tex = createTexture(this.graphicswidth,this.graphicsheight);
		buf = createBuffer(graphicslength*4);
		GL31.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GL31.glClear(GL31.GL_COLOR_BUFFER_BIT);
		GLFW.glfwSwapBuffers(window);
		GLFW.glfwGetCursorPos(window, lastmousex, lastmousey);
		this.cameraposrot3fovres = new float[]{0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, graphicshfov,graphicsvfov, graphicswidth,graphicsheight};
		this.trianglelistpos3iduv3 = new float[]{
				 1.0f,-1.0f,-1.0f,   1.0f, 1.0f,-1.0f,   1.0f, 1.0f, 1.0f,  0.0f,  0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,
				 1.0f,-1.0f,-1.0f,   1.0f,-1.0f, 1.0f,   1.0f, 1.0f, 1.0f,  0.0f,  0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,
				-1.0f,-1.0f,-1.0f,  -1.0f, 1.0f,-1.0f,  -1.0f, 1.0f, 1.0f,  0.0f,  1.0f,1.0f,1.0f,0.0f,0.0f,0.0f,
				-1.0f,-1.0f,-1.0f,  -1.0f,-1.0f, 1.0f,  -1.0f, 1.0f, 1.0f,  0.0f,  1.0f,1.0f,0.0f,1.0f,0.0f,0.0f,
				
				-1.0f,-1.0f,-1.0f,   1.0f,-1.0f,-1.0f,   1.0f,-1.0f, 1.0f,  0.0f,  0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,
				-1.0f,-1.0f,-1.0f,  -1.0f,-1.0f, 1.0f,   1.0f,-1.0f, 1.0f,  0.0f,  0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,
				-1.0f, 1.0f,-1.0f,   1.0f, 1.0f,-1.0f,   1.0f, 1.0f, 1.0f,  0.0f,  1.0f,1.0f,1.0f,0.0f,0.0f,0.0f,
				-1.0f, 1.0f,-1.0f,  -1.0f, 1.0f, 1.0f,   1.0f, 1.0f, 1.0f,  0.0f,  1.0f,1.0f,0.0f,1.0f,0.0f,0.0f,

				 1.0f,-1.0f, 1.0f,   1.0f, 1.0f, 1.0f,  -1.0f, 1.0f, 1.0f,  0.0f,  0.0f,1.0f,0.0f,0.0f,1.0f,0.0f,
				 1.0f,-1.0f, 1.0f,  -1.0f,-1.0f, 1.0f,  -1.0f, 1.0f, 1.0f,  0.0f,  0.0f,1.0f,1.0f,1.0f,1.0f,0.0f,
				 1.0f,-1.0f,-1.0f,   1.0f, 1.0f,-1.0f,  -1.0f, 1.0f,-1.0f,  0.0f,  1.0f,1.0f,1.0f,0.0f,0.0f,0.0f,
				 1.0f,-1.0f,-1.0f,  -1.0f,-1.0f,-1.0f,  -1.0f, 1.0f,-1.0f,  0.0f,  1.0f,1.0f,0.0f,1.0f,0.0f,0.0f,
		};
		this.trianglelistlength = this.trianglelistpos3iduv3.length/16;
		cannonsound = loadSound("res/sounds/firecannon.wav", 50, true);
		BufferedImage textureimage = loadImage("res/images/texturetest.png", true);
		DataBufferInt textureimagedataint = (DataBufferInt)textureimage.getRaster().getDataBuffer();
		this.triangletexturelist = textureimagedataint.getData();
		this.objectlistlength = 100;
		float objectradius = 20.0f;
		this.objectlistpos3sca3rot3 = new float[objectlistlength*9];
		for (int i=0;i<objectlistlength;i++) {
			this.objectlistpos3sca3rot3[9*i+0] = rnd.nextFloat(-1.0f, 1.0f)*objectradius;
			this.objectlistpos3sca3rot3[9*i+1] = rnd.nextFloat(-1.0f, 1.0f)*objectradius;
			this.objectlistpos3sca3rot3[9*i+2] = rnd.nextFloat(-1.0f, 1.0f)*objectradius;
			this.objectlistpos3sca3rot3[9*i+3] = 1.0f;
			this.objectlistpos3sca3rot3[9*i+4] = 1.0f;
			this.objectlistpos3sca3rot3[9*i+5] = 1.0f;
			this.objectlistpos3sca3rot3[9*i+6] = rnd.nextFloat(0.0f, 1.0f)*360.0f;
			this.objectlistpos3sca3rot3[9*i+7] = rnd.nextFloat(0.0f, 1.0f)*360.0f;
			this.objectlistpos3sca3rot3[9*i+8] = rnd.nextFloat(0.0f, 1.0f)*360.0f;
		}
		this.selecteddevice = vselecteddevice;
		this.computelib = new ComputeLib(window);
		this.device = this.computelib.devicelist[selecteddevice];
		this.devicedata = this.computelib.devicemap.get(device);
		this.usingdevice = devicedata.devicename;
		if (!devicedata.platformcontextsharing) {
			this.glinterop = false;
		}
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		this.queue = devicedata.queue;
		if (this.glinterop) {
			this.graphicspointerbuffer[0] = computelib.createSharedGLBuffer(device, buf);
		} else {
			this.graphicsbuffer = new float[graphicslength*4];
			this.graphicspointerbuffer[0] = computelib.createBuffer(device, graphicslength*4);
		}
		this.graphicspointerbuffer[1] = computelib.createBuffer(device, graphicslength);
		this.graphicszbuffer = new float[graphicslength];
		this.graphicspointerbuffer[2] = computelib.createBuffer(device, 1);
		this.graphicshbuffer = new int[1];
		this.graphicspointerbuffer[3] = computelib.createBuffer(device, cameraposrot3fovres.length);
		this.graphicspointerbuffer[4] = computelib.createBuffer(device, trianglelistpos3iduv3.length);
		computelib.writeBufferf(device, queue, graphicspointerbuffer[4], trianglelistpos3iduv3);
		this.graphicspointerbuffer[5] = computelib.createBuffer(device, triangletexturelist.length);
		computelib.writeBufferi(device, queue, graphicspointerbuffer[5], triangletexturelist);
		this.graphicspointerbuffer[6] = computelib.createBuffer(device, objectlistpos3sca3rot3.length);
		computelib.writeBufferf(device, queue, graphicspointerbuffer[6], objectlistpos3sca3rot3);
		String programSource = ComputeLib.loadProgram("res/clprograms/programlib.cl", true);
		this.program = this.computelib.compileProgram(device, programSource);
	}

	public void run() {
		while(!GLFW.glfwWindowShouldClose(window)) {
			long nanonewtimetick = System.nanoTime();
			lasttimedeltaseconds = (nanonewtimetick - nanolasttimetick)/1000000000.0f;
			nanolasttimetick = nanonewtimetick;
			tick(lasttimedeltaseconds);
			if (this.glinterop) {computelib.acquireSharedGLBuffer(queue, graphicspointerbuffer[0]);}
			render();
			if (this.glinterop) {computelib.releaseSharedGLBuffer(queue, graphicspointerbuffer[0]);}
			if (!this.glinterop) {transferBuffer(buf, graphicsbuffer);}
			updateTexture(tex, buf, graphicswidth, graphicsheight);
			GL31.glClear(GL31.GL_COLOR_BUFFER_BIT | GL31.GL_DEPTH_BUFFER_BIT);
			GL31.glBindTexture(GL31.GL_TEXTURE_2D, tex);
			GL31.glViewport(0, 0, screenwidth, screenheight);
			GL31.glUseProgram(quadProgram);
			GL31.glBindVertexArray(vao);
			GL31.glDrawArrays(GL31.GL_TRIANGLES, 0, 6);
			GL31.glBindVertexArray(0);
			GL31.glUseProgram(0);
			GL31.glFlush();
			GL31.glFinish();
			GL31.glBindTexture(GL31.GL_TEXTURE_2D, 0);
			GLFW.glfwSwapBuffers(window);
			GLFW.glfwPollEvents();
		}

		Callbacks.glfwFreeCallbacks(window);
		GLFW.glfwDestroyWindow(window);
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
		System.exit(0);
	}

	public static void main(String[] args) {
		System.out.println(programtitle);
		int argdevice = 0;
		int argfullscreen = 1;
		int argglinterop = 1;
		try {argdevice = Integer.parseInt(args[0]);} catch(Exception ex) {}
		try {argfullscreen = Integer.parseInt(args[1]);} catch(Exception ex) {}
		try {argglinterop = Integer.parseInt(args[2]);} catch(Exception ex) {}
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice, argfullscreen, argglinterop);
		app.run();
	}

	private void tick(float deltatimeseconds) {
		float ds = deltatimeseconds;
		GLFW.glfwSetWindowTitle(window, programtitle+": "+String.format("%.0f",1000.0f/frametimeavg).replace(',', '.')+
				"fps, computetime: "+String.format("%.3f",frametimeavg).replace(',', '.')+"ms ["+usingdevice+"] ("
				+screenwidth+"x"+screenheight+") tickdeltatime: "+String.format("%.0f",deltatimeseconds*1000.0f)+"ms"
				+" ["+(this.glinterop?"GLINTEROP":"COPYBUFFER")+"]"
				);
		cannonfiringlast += ds;
		if ((cannonfiring)&&(cannonfiringlast>cannonfiringdelta)) {
			cannonfiringlast = 0.0f;
			cannonsound[cannonsoundind].stop();
			cannonsound[cannonsoundind].setFramePosition(0);
			cannonsound[cannonsoundind].start();
			if (++cannonsoundind>=cannonsound.length) {cannonsoundind = 0;}
			int hitobjind = graphicshbuffer[0];
			int hitobjindstep = hitobjind * 9;
			if (hitobjind!=-1) {
				float[] newobjectlistpos3sca3rot3 = new float[objectlistpos3sca3rot3.length-9];
				for (int i=0;i<hitobjindstep;i++) {
					newobjectlistpos3sca3rot3[i] = objectlistpos3sca3rot3[i];
				}
				for (int i=hitobjindstep+9;i<objectlistpos3sca3rot3.length;i++) {
					newobjectlistpos3sca3rot3[i-9] = objectlistpos3sca3rot3[i];
				}
				objectlistlength--;
				objectlistpos3sca3rot3 = newobjectlistpos3sca3rot3;
			}
		}
		for (int i=0;i<objectlistlength;i++) {
			objectlistpos3sca3rot3[9*i+6] += 15.0f*ds;
			objectlistpos3sca3rot3[9*i+7] += 17.0f*ds;
			objectlistpos3sca3rot3[9*i+8] += 19.0f*ds;
		}
		if (this.keyfwd) {cameraposrot3fovres[0] += ds;}
		if (this.keyback) {cameraposrot3fovres[0] -= ds;}
		if (this.keyleft) {cameraposrot3fovres[1] -= ds;}
		if (this.keyright) {cameraposrot3fovres[1] += ds;}
		if (this.keyup) {cameraposrot3fovres[2] += ds;}
		if (this.keydown) {cameraposrot3fovres[2] -= ds;}
	}

	public void render() {
		long framestarttime = System.nanoTime();
		computelib.writeBufferi(device, queue, graphicspointerbuffer[2], new int[]{-1});
		computelib.writeBufferf(device, queue, graphicspointerbuffer[3], cameraposrot3fovres);
		computelib.writeBufferf(device, queue, graphicspointerbuffer[6], objectlistpos3sca3rot3);
		computelib.runProgram(device, queue, program, "clearview", graphicspointerbuffer, new int[]{0}, new int[]{graphicswidth});
		computelib.runProgram(device, queue, program, "renderview", graphicspointerbuffer, new int[]{0,0,0}, new int[]{graphicswidth,trianglelistlength,objectlistlength});
		computelib.insertBarrier(queue);
		computelib.runProgram(device, queue, program, "rendercross", graphicspointerbuffer, new int[]{0}, new int[]{1});
		computelib.waitForQueue(queue);
		computelib.readBufferi(device, queue, graphicspointerbuffer[2], graphicshbuffer);
		if (!this.glinterop) {
			float[] newgraphicsbuffer = new float[graphicslength*4];
			computelib.readBufferf(device, queue, graphicspointerbuffer[0], newgraphicsbuffer);
			graphicsbuffer = newgraphicsbuffer;
		}
		long frameendtime = System.nanoTime();
		frametime = (frameendtime-framestarttime)/1000000.0f;
		frametimeavg = frametimeavg*0.9f+frametime*0.1f;
	}

	private void createQuadProgram() {
		int program = GL31.glCreateProgram();
		String quadvertexshader = ComputeLib.loadProgram("res/glshaders/texturedquad.vs", true);
		String quadfragmentshader = ComputeLib.loadProgram("res/glshaders/texturedquad.fs", true);
		int vshader = createShader(quadvertexshader, GL31.GL_VERTEX_SHADER);
		int fshader = createShader(quadfragmentshader, GL31.GL_FRAGMENT_SHADER);
		GL31.glAttachShader(program, vshader);
		GL31.glAttachShader(program, fshader);
		GL31.glLinkProgram(program);
		int linked = GL31.glGetProgrami(program, GL31.GL_LINK_STATUS);
		String programLog = GL31.glGetProgramInfoLog(program);
		if (programLog.trim().length() > 0) {System.err.println(programLog);}
		if (linked == 0) {throw new AssertionError("Could not link program");}
		GL31.glUseProgram(program);
		int texLocation = GL31.glGetUniformLocation(program, "tex");
		GL31.glUniform1i(texLocation, 0);
		quadProgram_inputPosition = GL31.glGetAttribLocation(program, "position");
		quadProgram_inputTextureCoords = GL31.glGetAttribLocation(program, "texCoords");
		GL31.glUseProgram(0);
		this.quadProgram = program;
	}

	private void createFullScreenQuad() {
		vao = GL31.glGenVertexArrays();
		GL31.glBindVertexArray(vao);
		int positionVbo = GL31.glGenBuffers();
		FloatBuffer fb = BufferUtils.createFloatBuffer(2 * 6);
		fb.put(-1.0f).put(-1.0f);
		fb.put(1.0f).put(-1.0f);
		fb.put(1.0f).put(1.0f);
		fb.put(1.0f).put(1.0f);
		fb.put(-1.0f).put(1.0f);
		fb.put(-1.0f).put(-1.0f);
		fb.flip();
		GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, positionVbo);
		GL31.glBufferData(GL31.GL_ARRAY_BUFFER, fb, GL31.GL_STATIC_DRAW);
		GL31.glVertexAttribPointer(quadProgram_inputPosition, 2, GL31.GL_FLOAT, false, 0, 0L);
		GL31.glEnableVertexAttribArray(quadProgram_inputPosition);
		int texCoordsVbo = GL31.glGenBuffers();
		fb = BufferUtils.createFloatBuffer(2 * 6);
		fb.put(0.0f).put(1.0f);
		fb.put(1.0f).put(1.0f);
		fb.put(1.0f).put(0.0f);
		fb.put(1.0f).put(0.0f);
		fb.put(0.0f).put(0.0f);
		fb.put(0.0f).put(1.0f);
		fb.flip();
		GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, texCoordsVbo);
		GL31.glBufferData(GL31.GL_ARRAY_BUFFER, fb, GL31.GL_STATIC_DRAW);
		GL31.glVertexAttribPointer(quadProgram_inputTextureCoords, 2, GL31.GL_FLOAT, true, 0, 0L);
		GL31.glEnableVertexAttribArray(quadProgram_inputTextureCoords);
		GL31.glBindBuffer(GL31.GL_ARRAY_BUFFER, 0);
		GL31.glBindVertexArray(0);
	}

	private int createTexture(int texturewidth, int textureheight) {
		int id = GL31.glGenTextures();
		GL31.glBindTexture(GL31.GL_TEXTURE_2D, id);
		GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MIN_FILTER, GL31.GL_LINEAR);
		GL31.glTexParameteri(GL31.GL_TEXTURE_2D, GL31.GL_TEXTURE_MAG_FILTER, GL31.GL_LINEAR);
		GL31.glTexImage2D(GL31.GL_TEXTURE_2D, 0, GL31.GL_RGB32F, texturewidth, textureheight, 0, GL31.GL_RGBA, GL31.GL_FLOAT, MemoryUtil.NULL);
		GL31.glBindTexture(GL31.GL_TEXTURE_2D, 0);
		return id;
	}

	private int createBuffer(int len) {
		int id = GL31.glGenBuffers();
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, id);
		GL31.glBufferData(GL31.GL_PIXEL_UNPACK_BUFFER, len*4, GL31.GL_STREAM_DRAW);
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, 0);
		return id;
	}

	private void updateTexture(int tid, int bid, int texturewidth, int textureheight) {
		GL31.glBindTexture(GL31.GL_TEXTURE_2D, tid);
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, bid);
		GL31.glTexSubImage2D(GL31.GL_TEXTURE_2D, 0, 0, 0, texturewidth, textureheight, GL31.GL_RGBA, GL31.GL_FLOAT, 0);
		GL31.glBindTexture(GL31.GL_TEXTURE_2D, 0);
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, 0);
	}

    private void transferBuffer(int id, float[] texturebuffer) {
    	GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, id);
    	GL31.glBufferSubData(GL31.GL_PIXEL_UNPACK_BUFFER, 0, texturebuffer);
    	GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, 0);
    }
	
	private int createShader(String sourceShader, int type) {
		int shader = GL31.glCreateShader(type);
		ByteBuffer source = BufferUtils.createByteBuffer(8192);
		source.put(sourceShader.getBytes()).rewind();
		PointerBuffer strings = BufferUtils.createPointerBuffer(1);
		IntBuffer lengths = BufferUtils.createIntBuffer(1);
		strings.put(0, source);
		lengths.put(0, source.remaining());
		GL31.glShaderSource(shader, strings, lengths);
		GL31.glCompileShader(shader);
		int compiled = GL31.glGetShaderi(shader, GL31.GL_COMPILE_STATUS);
		String shaderLog = GL31.glGetShaderInfoLog(shader);
		if (shaderLog.trim().length() > 0) {System.err.println(shaderLog);}
		if (compiled == 0) {throw new AssertionError("Could not compile shader");}
		return shader;
	}    

	public static BufferedImage loadImage(String filename, boolean loadresourcefromjar) {
		BufferedImage k = null;
		if (filename!=null) {
			try {
				File imagefile = new File(filename);
				BufferedInputStream imagefilestream = null;
				if (loadresourcefromjar) {
					imagefilestream = new BufferedInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(imagefile.getPath().replace(File.separatorChar, '/')));
				} else {
					imagefilestream = new BufferedInputStream(new FileInputStream(imagefile));
				}
				BufferedImage loadimage = ImageIO.read(imagefilestream);
				if (loadimage!=null) {
					BufferedImage argbimage = new BufferedImage(loadimage.getWidth(), loadimage.getHeight(), BufferedImage.TYPE_INT_ARGB_PRE);
					Graphics2D argbimagegfx = argbimage.createGraphics();
					argbimagegfx.setComposite(AlphaComposite.Src);
					argbimagegfx.drawImage(loadimage, 0, 0, null);
					argbimagegfx.dispose();
					k = argbimage;
				}
				imagefilestream.close();
			} catch (Exception ex) {ex.printStackTrace();}
		}
		return k;
	}

	public static Clip[] loadSound(String filename, int copies, boolean loadresourcefromjar) {
		Clip[] k = null;
		if (filename!=null) {
			try {
				File soundfile = new File(filename);
				BufferedInputStream soundfilestream = null;
				if (loadresourcefromjar) {
					soundfilestream = new BufferedInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(soundfile.getPath().replace(File.separatorChar, '/')));
				} else {
					soundfilestream = new BufferedInputStream(new FileInputStream(soundfile));
				}
				AudioInputStream soundfileaudiostream = AudioSystem.getAudioInputStream(soundfilestream);
				AudioFormat soundfileaudioformat = soundfileaudiostream.getFormat();
				byte[] soundbytes = soundfileaudiostream.readAllBytes();
				Clip[] loadsounds = new Clip[copies];
				for (int i=0;i<copies;i++) {
					loadsounds[i] = AudioSystem.getClip();
					loadsounds[i].open(soundfileaudioformat, soundbytes, 0, soundbytes.length);
				}
				k = loadsounds;
				soundfilestream.close();
			} catch (Exception ex) {ex.printStackTrace();}
		}
		return k;
	}
	
	private class KeyProcessor implements GLFWKeyCallbackI {
		@Override public void invoke(long window, int key, int scancode, int action, int mods) {
			if (action==GLFW.GLFW_PRESS) {
				if (key==GLFW.GLFW_KEY_W) {keyfwd = true;}
				if (key==GLFW.GLFW_KEY_S) {keyback = true;}
				if (key==GLFW.GLFW_KEY_A) {keyleft = true;}
				if (key==GLFW.GLFW_KEY_D) {keyright = true;}
				if (key==GLFW.GLFW_KEY_SPACE) {keyup = true;}
				if (key==GLFW.GLFW_KEY_LEFT_SHIFT) {keydown = true;}
			}
			if (action==GLFW.GLFW_RELEASE) {
				if (key==GLFW.GLFW_KEY_W) {keyfwd = false;}
				if (key==GLFW.GLFW_KEY_S) {keyback = false;}
				if (key==GLFW.GLFW_KEY_A) {keyleft = false;}
				if (key==GLFW.GLFW_KEY_D) {keyright = false;}
				if (key==GLFW.GLFW_KEY_SPACE) {keyup = false;}
				if (key==GLFW.GLFW_KEY_LEFT_SHIFT) {keydown = false;}
			}
		}
	}
	private class MousePositionProcessor implements GLFWCursorPosCallbackI {
		@Override public void invoke(long window, double xpos, double ypos) {
			double mousedeltax = xpos-lastmousex[0];
			double mousedeltay = ypos-lastmousey[0];
			cameraposrot3fovres[5] += 0.1f*mousedeltax;
			cameraposrot3fovres[4] += 0.1f*mousedeltay;
			if (cameraposrot3fovres[5]>360.0f) {cameraposrot3fovres[5] -= 360.0f;}
			if (cameraposrot3fovres[5]<0.0f) {cameraposrot3fovres[5] += 360.0f;}
			if (cameraposrot3fovres[4]>90.0f) {cameraposrot3fovres[4] = 90.0f;}
			if (cameraposrot3fovres[4]<-90.0f) {cameraposrot3fovres[4] = -90.0f;}
			lastmousex[0] = xpos;
			lastmousey[0] = ypos;
		}
	}
	private class MouseButtonProcessor implements GLFWMouseButtonCallbackI {
		@Override public void invoke(long window, int button, int action, int mods) {
			if ((button==0)&&(action==1)) {
				cannonfiring = true;
			}
			if ((button==0)&&(action==0)) {
				cannonfiring = false;
			}
		}
	}
	private class MouseWheelProcessor implements GLFWScrollCallbackI {
		@Override public void invoke(long window, double xoffset, double yoffset) {
			System.out.println("xoffset: "+xoffset+" yoffset: "+yoffset);
		}
	}


}
