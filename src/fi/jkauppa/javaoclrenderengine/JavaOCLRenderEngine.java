package fi.jkauppa.javaoclrenderengine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.openal.EXTThreadLocalContext;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.system.MemoryUtil.NULL;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;
import fi.jkauppa.javarenderengine.ModelLib;
import fi.jkauppa.javarenderengine.ModelLib.Entity;
import fi.jkauppa.javarenderengine.ModelLib.Triangle;
import fi.jkauppa.javarenderengine.UtilLib;

public class JavaOCLRenderEngine {
	private static String programtitle = "Java OpenCL Render Engine v1.1.0.9";
	private int screenwidth = 0, screenheight = 0, graphicswidth = 0, graphicsheight = 0, graphicslength = 0;
	@SuppressWarnings("unused")
	private int litgraphicswidth = 0, litgraphicsheight = 0;
	private float graphicshfov = 70.0f, graphicsvfov = 39.375f;
	private long window = NULL;
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
	private int soundbuf = 0;
	private int sourcebuf = 0;
	private float frametime = 0.0f;
	private float frametimeavg = 0.0f;
	private ComputeLib computelib = null;
	private int selecteddevice = 0;
	@SuppressWarnings("unused")
	private boolean isfullscreen = false;
	private boolean glinterop = true;
	private long opencldevice = NULL, queue = NULL, program = NULL;
	private Device opencldevicedata = null;
	private String usingopencldevice = null;
	private long audiodevice = NULL;
	private long graphicsbufferptr = NULL, graphicszbufferptr = NULL, graphicsibufferptr = NULL, graphicshbufferptr = NULL, camposbufferptr = NULL, cammovbufferptr = NULL;
	private long entitiesptr = NULL, entitieslenptr = NULL, objectsptr = NULL, objectslenptr = NULL, trianglesptr = NULL, triangleslenptr = NULL, texturesptr = NULL, textureslenptr = NULL;
	private long triangleslitptr = NULL;
	private long litptr = NULL;
	private long norptr = NULL;
	private float[] graphicsbuffer = null;
	@SuppressWarnings("unused")
	private float[] graphicszbuffer = null;
	private int[] graphicshbuffer = null;
	private float[] camerapos3fov2res2rotmat16 = null;
	private float[] cameramov3rot3 = null;
	private float[] triangleslist = null;
	private int[] triangleslistlength = {0};
	private int[] textureslist = null;
	private int[] textureslistlength = {0};
	private float[] objectslist = null;
	private int[] objectslistlength = {0};
	private float[] entitylist = null;
	private int[] entitylistlength = {0};
	private int[] renderlit = {1};
	private int[] rendersphnorm = {0};
	private boolean keyfwd = false;
	private boolean keyback = false;
	private boolean keyleft = false;
	private boolean keyright = false;
	private boolean keyup = false;
	private boolean keydown = false;
	private boolean keyrleft = false;
	private boolean keyrright = false;
	private boolean keyspeed = false;
	private long nanolasttimetick = System.nanoTime();
	private double[] mousex = {0}, mousey = {0};
	private double lastmousex = 0, lastmousey = 0;
	private float lasttimedeltaseconds = 0.0f;
	private long monitor = NULL;
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
		this.screenwidth = 1280; this.screenheight = 720;
		long fullscreenmonitor = NULL;
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
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 1);
		GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, videomode.redBits());
		GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, videomode.greenBits());
		GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, videomode.blueBits());
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		if ((window=GLFW.glfwCreateWindow(screenwidth, screenheight, programtitle, fullscreenmonitor, NULL))==NULL) {System.out.println("GLFW create window failed."); System.exit(2);}
		GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
		GLFW.glfwSetKeyCallback(window, keyprocessor);
		GLFW.glfwSetCursorPosCallback(window, mouseposprocessor);
		GLFW.glfwSetMouseButtonCallback(window, mousebuttonprocessor);
		GLFW.glfwSetScrollCallback(window, mousewheelprocessor);
		GLFW.glfwMakeContextCurrent(window);
		GLFW.glfwSwapInterval(1);
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
		GLFW.glfwGetCursorPos(window, mousex, mousey);
		lastmousex = mousex[0]; lastmousey = mousey[0];
		BufferedImage iconimage = UtilLib.loadImage("res/images/icon.png", true);
		this.setIcon(iconimage);

		this.selecteddevice = vselecteddevice;
		this.computelib = new ComputeLib(window);
		this.opencldevice = this.computelib.devicelist[selecteddevice];
		this.opencldevicedata = this.computelib.devicemap.get(opencldevice);
		this.usingopencldevice = opencldevicedata.devicename;
		if (!opencldevicedata.platformcontextsharing) {
			this.glinterop = false;
		}
		System.out.println("Using device["+selecteddevice+"]: "+opencldevicedata.devicename);
		this.queue = opencldevicedata.queue;

		this.audiodevice = ALC11.alcOpenDevice((String)null);
		if (this.audiodevice == NULL) {
			throw new IllegalStateException("Failed to open default OpenAL device.");
		}
		ALCCapabilities audiodeviceCaps = ALC.createCapabilities(this.audiodevice);
		if (!audiodeviceCaps.OpenALC10) {
			throw new IllegalStateException();
		}
		long audiocontext = ALC11.alcCreateContext(this.audiodevice, (IntBuffer)null);
		boolean useTLC = audiodeviceCaps.ALC_EXT_thread_local_context && EXTThreadLocalContext.alcSetThreadContext(audiocontext);
		if (!useTLC) {
			if (!ALC11.alcMakeContextCurrent(audiocontext)) {
				throw new IllegalStateException();
			}
		}
		@SuppressWarnings("unused")
		ALCapabilities caps = AL.createCapabilities(audiodeviceCaps, MemoryUtil::memCallocPointer);
		this.soundbuf = AL10.alGenBuffers();
		this.sourcebuf = AL10.alGenSources();

		byte[] soundbytes = UtilLib.loadSound("res/sounds/firecannon.wav", 1, true);
		ByteBuffer soundbytesbuffer = MemoryUtil.memAlloc(soundbytes.length);
		soundbytesbuffer.put(soundbytes).rewind();
		AL10.alBufferData(this.soundbuf, AL10.AL_FORMAT_STEREO16, soundbytesbuffer, 44100);
		AL10.alSourcei(this.sourcebuf, AL10.AL_BUFFER, this.soundbuf);

		this.camerapos3fov2res2rotmat16 = new float[]{0.0f,0.0f,0.0f, graphicshfov,graphicsvfov, graphicswidth,graphicsheight, 1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
		this.cameramov3rot3 = new float[]{0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f};

		Entity loadmodel = ModelLib.loadOBJFileEntity("res/models/pasteroid2.obj", true);
		Entity loadmodel2 = ModelLib.loadOBJFileEntity("res/models/spaceboxgreen.obj", true);
		TriangleObjectEntity triobjent = getEntityObjectTriangles(loadmodel, 1.0f);
		TriangleObjectEntity triobjent2 = getEntityObjectTriangles(loadmodel2, 1.0f);
		TriangleObjectEntity alltriobjents = mergeEntityObjectTriangles(new TriangleObjectEntity[]{triobjent, triobjent2});
		
		this.entitylist = alltriobjents.entities;
		this.entitylistlength[0] = alltriobjents.entities.length/15;
		this.objectslist = alltriobjents.objects;
		this.objectslistlength[0] = alltriobjents.objects.length/15;
		this.triangleslist = alltriobjents.triangles;
		this.triangleslistlength[0] = alltriobjents.triangles.length/35;
		this.textureslist = alltriobjents.images;
		this.textureslistlength[0] = alltriobjents.imagesize;
		
		if (this.glinterop) {
			this.graphicsbufferptr = computelib.createSharedGLBuffer(opencldevice, buf);
		} else {
			this.graphicsbuffer = new float[graphicslength*4];
			this.graphicsbufferptr = computelib.createBuffer(opencldevice, graphicslength*4);
		}
		this.graphicszbufferptr = computelib.createBuffer(opencldevice, graphicslength);
		this.graphicszbuffer = new float[graphicslength];
		this.graphicsibufferptr = computelib.createBuffer(opencldevice, graphicslength*4);
		this.graphicshbufferptr = computelib.createBuffer(opencldevice, 1);
		this.graphicshbuffer = new int[1];
		this.camposbufferptr = computelib.createBuffer(opencldevice, camerapos3fov2res2rotmat16.length);
		computelib.writeBufferf(opencldevice, queue, camposbufferptr, camerapos3fov2res2rotmat16);
		this.cammovbufferptr = computelib.createBuffer(opencldevice, cameramov3rot3.length);
		computelib.writeBufferf(opencldevice, queue, cammovbufferptr, cameramov3rot3);

		this.entitiesptr = computelib.createBuffer(opencldevice, this.entitylistlength[0]*15);
		computelib.writeBufferf(opencldevice, queue, entitiesptr, this.entitylist);
		this.entitieslenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, entitieslenptr, this.entitylistlength);
		
		this.objectsptr = computelib.createBuffer(opencldevice, this.objectslistlength[0]*15);
		computelib.writeBufferf(opencldevice, queue, objectsptr, this.objectslist);
		this.objectslenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, objectslenptr, this.objectslistlength);
		
		this.trianglesptr = computelib.createBuffer(opencldevice, this.triangleslistlength[0]*35);
		computelib.writeBufferf(opencldevice, queue, trianglesptr, this.triangleslist);
		this.triangleslenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, triangleslenptr, this.triangleslistlength);
		
		this.triangleslitptr = computelib.createBuffer(opencldevice, this.triangleslistlength[0]*35);
		
		this.texturesptr = computelib.createBuffer(opencldevice, textureslist.length);
		computelib.writeBufferi(opencldevice, queue, texturesptr, textureslist);
		this.textureslenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, textureslenptr, textureslistlength);
		
		this.litptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, litptr, renderlit);
		this.norptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, norptr, rendersphnorm);
		
		String programSource = UtilLib.loadText("res/clprograms/programlib.cl", true);
		this.program = this.computelib.compileProgram(opencldevice, programSource);
		computelib.runProgram(opencldevice, queue, program, "transformentity", new long[]{triangleslitptr,trianglesptr,objectsptr,entitiesptr}, new int[]{0}, new int[]{entitylistlength[0]});
		computelib.insertBarrier(queue);
		System.out.println("init.");
	}

	public void run() {
		while(!GLFW.glfwWindowShouldClose(window)) {
			long nanonewtimetick = System.nanoTime();
			lasttimedeltaseconds = (nanonewtimetick - nanolasttimetick)/1000000000.0f;
			nanolasttimetick = nanonewtimetick;
			tick(lasttimedeltaseconds);
			if (this.glinterop) {computelib.acquireSharedGLBuffer(queue, graphicsbufferptr);}
			render();
			if (this.glinterop) {computelib.releaseSharedGLBuffer(queue, graphicsbufferptr);}
			if (!this.glinterop) {transferBufferf(buf, graphicsbuffer);}
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
		System.out.println("exit.");
		System.exit(0);
	}

	private void tick(float deltatimeseconds) {
		float ds = deltatimeseconds;
		GLFW.glfwSetWindowTitle(window, programtitle+": "+String.format("%.0f",1000.0f/frametimeavg).replace(',', '.')+
				"fps, computetime: "+String.format("%.3f",frametimeavg).replace(',', '.')+"ms ["+usingopencldevice+"] ("
				+screenwidth+"x"+screenheight+") tickdeltatime: "+String.format("%.0f",deltatimeseconds*1000.0f)+"ms"
				+" ["+(this.glinterop?"GLINTEROP":"COPYBUFFER")+"]"
				);
		cameramov3rot3[0] = 0.0f;
		cameramov3rot3[1] = 0.0f;
		cameramov3rot3[2] = 0.0f;
		cameramov3rot3[3] = 0.0f;
		cameramov3rot3[4] = 0.0f;
		cameramov3rot3[5] = 0.0f;
		float sp = this.keyspeed?100.0f:1.0f;
		if (this.keyfwd) {cameramov3rot3[0] = ds*sp;}
		if (this.keyback) {cameramov3rot3[0] = -ds*sp;}
		if (this.keyleft) {cameramov3rot3[1] = -ds*sp;}
		if (this.keyright) {cameramov3rot3[1] = ds*sp;}
		if (this.keyup) {cameramov3rot3[2] = ds*sp;}
		if (this.keydown) {cameramov3rot3[2] = -ds*sp;}
		if (this.keyrleft) {cameramov3rot3[5] = -ds;}
		if (this.keyrright) {cameramov3rot3[5] = ds;}
		cameramov3rot3[4] = -(float)(0.001f*(mousex[0]-lastmousex));
		cameramov3rot3[3] = (float)(0.001f*(mousey[0]-lastmousey));
		lastmousex = mousex[0];
		lastmousey = mousey[0];
	}

	public void render() {
		long framestarttime = System.nanoTime();
		computelib.writeBufferf(opencldevice, queue, cammovbufferptr, cameramov3rot3);
		computelib.runProgram(opencldevice, queue, program, "movecamera", new long[]{camposbufferptr,cammovbufferptr}, new int[]{0}, new int[]{1});
		computelib.insertBarrier(queue);
		computelib.readBufferf(opencldevice, queue, camposbufferptr, camerapos3fov2res2rotmat16);
		int spaceboxind = this.objectslist.length/15-1;
		this.objectslist[spaceboxind*15+0] = camerapos3fov2res2rotmat16[0];
		this.objectslist[spaceboxind*15+1] = camerapos3fov2res2rotmat16[1];
		this.objectslist[spaceboxind*15+2] = camerapos3fov2res2rotmat16[2];
		computelib.writeBufferf(opencldevice, queue, objectsptr, this.objectslist);
		computelib.runProgram(opencldevice, queue, program, "clearview", new long[]{graphicsibufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr}, new int[]{0,0}, new int[]{graphicswidth,8});
		//computelib.runProgram(opencldevice, queue, program, "transformentity", new long[]{triangleslitptr,trianglesptr,objectsptr,entitiesptr}, new int[]{0}, new int[]{entitylistlength[0]});
		//computelib.insertBarrier(queue);
		//computelib.runProgram(opencldevice, queue, program, "lightobject", new long[]{,,,triangleslitptr,triangleslitptr,triangleslenptr,texturesptr,textureslenptr}, new int[]{0,0,0}, new int[]{triangleslistlen[0],1,triangleslistlen[0]});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "renderplaneview", new long[]{graphicsibufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr,triangleslitptr,objectsptr,entitiesptr,entitieslenptr,texturesptr,textureslenptr,litptr,norptr}, new int[]{0,0}, new int[]{graphicswidth,8});
		//computelib.runProgram(opencldevice, queue, program, "renderrayview", new long[]{graphicsibufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr,triangleslitptr,objectsptr,entitiesptr,entitieslenptr,texturesptr,textureslenptr,litptr,norptr}, new int[]{0,0}, new int[]{graphicswidth,graphicsheight});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "viewfilter", new long[]{graphicsbufferptr,graphicsibufferptr,camposbufferptr}, new int[]{0,0}, new int[]{graphicswidth,graphicsheight});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "rendercross", new long[]{graphicsbufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr}, new int[]{0}, new int[]{1});
		computelib.waitForQueue(queue);
		computelib.readBufferi(opencldevice, queue, graphicshbufferptr, graphicshbuffer);
		if (!this.glinterop) {
			float[] newgraphicsbuffer = new float[graphicslength*4];
			computelib.readBufferf(opencldevice, queue, graphicsbufferptr, newgraphicsbuffer);
			graphicsbuffer = newgraphicsbuffer;
		}
		long frameendtime = System.nanoTime();
		frametime = (frameendtime-framestarttime)/1000000.0f;
		frametimeavg = frametimeavg*0.9f+frametime*0.1f;
	}
	
	private void createQuadProgram() {
		int program = GL31.glCreateProgram();
		int vshader = createShader("res/glshaders/texturedquad.vs", GL31.GL_VERTEX_SHADER);
		int fshader = createShader("res/glshaders/texturedquad.fs", GL31.GL_FRAGMENT_SHADER);
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
		GL31.glTexImage2D(GL31.GL_TEXTURE_2D, 0, GL31.GL_RGBA32F, texturewidth, textureheight, 0, GL31.GL_RGBA, GL31.GL_FLOAT, NULL);
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

	@SuppressWarnings("unused")
	private void transferBufferi(int id, int[] texturebuffer) {
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, id);
		GL31.glBufferSubData(GL31.GL_PIXEL_UNPACK_BUFFER, 0, texturebuffer);
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, 0);
	}

	private void transferBufferf(int id, float[] texturebuffer) {
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, id);
		GL31.glBufferSubData(GL31.GL_PIXEL_UNPACK_BUFFER, 0, texturebuffer);
		GL31.glBindBuffer(GL31.GL_PIXEL_UNPACK_BUFFER, 0);
	}
	
	private int createShader(String sourceShaderFile, int type) {
		byte[] sourcebytes = UtilLib.loadBinary(sourceShaderFile, true);
		int shader = GL31.glCreateShader(type);
		ByteBuffer source = BufferUtils.createByteBuffer(sourcebytes.length);
		source.put(sourcebytes).rewind();
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

	private void setIcon(BufferedImage iconimage) {
		DataBufferInt iconimagedataint = (DataBufferInt)iconimage.getRaster().getDataBuffer();
		int[] iconimageints = iconimagedataint.getData();
		IntBuffer iconimageintbuffer = IntBuffer.wrap(iconimageints);
		ByteBuffer iconimagebytebuffer = MemoryUtil.memAlloc(iconimageints.length*4);
		iconimagebytebuffer.asIntBuffer().put(iconimageintbuffer);
		for (int i=0;i<iconimageints.length;i++) {
			byte cr = iconimagebytebuffer.get(i*4+2);
			byte cg = iconimagebytebuffer.get(i*4+1);
			byte cb = iconimagebytebuffer.get(i*4+0);
			byte ca = iconimagebytebuffer.get(i*4+3);
			iconimagebytebuffer.put(i*4+0, cr);
			iconimagebytebuffer.put(i*4+1, cg);
			iconimagebytebuffer.put(i*4+2, cb);
			iconimagebytebuffer.put(i*4+3, ca);
		}
		Buffer iconimagebuffer = GLFWImage.create(1);
		GLFWImage iconglfwimage = GLFWImage.create().set(iconimage.getWidth(), iconimage.getHeight(), iconimagebytebuffer);
		iconimagebuffer.put(0, iconglfwimage);
		GLFW.glfwSetWindowIcon(window, iconimagebuffer);
		MemoryUtil.memFree(iconimagebytebuffer);
	}
	
	private class TriangleObjectEntity {
		public float[] triangles = null;
		public float[] objects = null;
		public float[] entities = null;
		public int[] images = null;
		public int imagesize = 0;
	}
	
	private TriangleObjectEntity mergeEntityObjectTriangles(TriangleObjectEntity[] triobjent) {
		TriangleObjectEntity mergedtriobjent = new TriangleObjectEntity();
		mergedtriobjent.entities = triobjent[0].entities;
		mergedtriobjent.objects = triobjent[0].objects;
		mergedtriobjent.triangles = triobjent[0].triangles;
		mergedtriobjent.images = triobjent[0].images;
		mergedtriobjent.imagesize = triobjent[0].imagesize;
		for (int j=1;j<triobjent.length;j++) {
			if (mergedtriobjent.imagesize==0) {mergedtriobjent.imagesize = triobjent[j].imagesize;}

			if (mergedtriobjent.entities==null) {
				mergedtriobjent.entities = triobjent[j].entities;
			} else if (triobjent[j].entities==null) {
			} else {
				float[] newentities = Arrays.copyOf(mergedtriobjent.entities, mergedtriobjent.entities.length+triobjent[j].entities.length);
				int mergedobjectcount = 0;
				if (mergedtriobjent.objects!=null) {
					mergedobjectcount = mergedtriobjent.objects.length/15;
				}
				for (int i=0;i<triobjent[j].entities.length;i++) {
					int trind = i+mergedtriobjent.entities.length;
					newentities[trind] = triobjent[j].entities[i];
					if (i%15==13) {
						newentities[trind] += mergedobjectcount;
					}
				}
				mergedtriobjent.entities = newentities;
			}
			
			if (mergedtriobjent.objects==null) {
				mergedtriobjent.objects = triobjent[j].objects;
			} else if (triobjent[j].objects==null) {
			} else {
				float[] newobjects = Arrays.copyOf(mergedtriobjent.objects, mergedtriobjent.objects.length+triobjent[j].objects.length);
				int mergedtrianglecount = 0;
				if (mergedtriobjent.triangles!=null) {
					mergedtrianglecount = mergedtriobjent.triangles.length/35;
				}
				for (int i=0;i<triobjent[j].objects.length;i++) {
					int trind = i+mergedtriobjent.objects.length;
					newobjects[trind] = triobjent[j].objects[i];
					if (i%15==13) {
						newobjects[trind] += mergedtrianglecount;
					}
				}
				mergedtriobjent.objects = newobjects;
			}
			
			if (mergedtriobjent.triangles==null) {
				mergedtriobjent.triangles = triobjent[j].triangles;
			} else if (triobjent[j].triangles==null) {
			} else {
				float[] newtriangles = Arrays.copyOf(mergedtriobjent.triangles, mergedtriobjent.triangles.length+triobjent[j].triangles.length);
				int mergedimagecount = 0;
				if (mergedtriobjent.images!=null) {
					mergedimagecount = mergedtriobjent.images.length/(mergedtriobjent.imagesize*mergedtriobjent.imagesize);
				}
				for (int i=0;i<triobjent[j].triangles.length;i++) {
					int trind = i+mergedtriobjent.triangles.length;
					newtriangles[trind] = triobjent[j].triangles[i];
					if ((i%35==18)&&(newtriangles[trind]>=0)) {
						newtriangles[trind] += mergedimagecount;
					}
				}
				mergedtriobjent.triangles = newtriangles;
			}
			
			if (mergedtriobjent.images==null) {
				mergedtriobjent.images = triobjent[j].images;
			} else if (triobjent[j].images==null) {
			} else {
				int[] newimages = Arrays.copyOf(mergedtriobjent.images, mergedtriobjent.images.length+triobjent[j].images.length);
				for (int i=0;i<triobjent[j].images.length;i++) {
					int imind = i+mergedtriobjent.images.length;
					newimages[imind] = triobjent[j].images[i];
				}
				mergedtriobjent.images = newimages;
			}
		}
		return mergedtriobjent;
	}
	
	private TriangleObjectEntity getEntityObjectTriangles(Entity loadmodel, float scale) {
		TriangleObjectEntity triobjent = new TriangleObjectEntity();
		ArrayList<Float> trianglearraylist = new ArrayList<Float>();
		ArrayList<Float> objectarraylist = new ArrayList<Float>();
		ArrayList<Float> entityarraylist = new ArrayList<Float>();

		if (loadmodel.imagelist.length>0) {
			BufferedImage textureimage = loadmodel.imagelist[0];
			triobjent.imagesize = textureimage.getWidth();
			int texturelength = triobjent.imagesize*triobjent.imagesize;
			triobjent.images = new int[loadmodel.imagelist.length*texturelength];
			for (int j=0;j<loadmodel.imagelist.length;j++) {
				textureimage = loadmodel.imagelist[j];
				DataBufferInt textureimagedataint = (DataBufferInt)textureimage.getRaster().getDataBuffer();
				int[] texturedata = textureimagedataint.getData();
				for (int i=0;i<texturelength;i++) {
					triobjent.images[j*texturelength+i] = texturedata[i];
				}
			}
		}
		
		entityarraylist.add(0.0f);
		entityarraylist.add(0.0f);
		entityarraylist.add(0.0f);
		entityarraylist.add(1.0f);
		entityarraylist.add(1.0f);
		entityarraylist.add(1.0f);
		entityarraylist.add(0.0f);
		entityarraylist.add(0.0f);
		entityarraylist.add(0.0f);
		entityarraylist.add(-scale*(float)loadmodel.sphereboundaryvolume.x);
		entityarraylist.add(scale*(float)loadmodel.sphereboundaryvolume.y);
		entityarraylist.add(scale*(float)loadmodel.sphereboundaryvolume.z);
		entityarraylist.add(scale*(float)loadmodel.sphereboundaryvolume.r);
		entityarraylist.add(0.0f);
		entityarraylist.add((float)loadmodel.childlist.length);
		
		for (int j=0;j<loadmodel.childlist.length;j++) {
			Entity object = loadmodel.childlist[j];
			
			objectarraylist.add(0.0f);
			objectarraylist.add(0.0f);
			objectarraylist.add(0.0f);
			objectarraylist.add(1.0f);
			objectarraylist.add(1.0f);
			objectarraylist.add(1.0f);
			objectarraylist.add(0.0f);
			objectarraylist.add(0.0f);
			objectarraylist.add(0.0f);
			objectarraylist.add(-scale*(float)object.sphereboundaryvolume.x);
			objectarraylist.add(scale*(float)object.sphereboundaryvolume.y);
			objectarraylist.add(scale*(float)object.sphereboundaryvolume.z);
			objectarraylist.add(scale*(float)object.sphereboundaryvolume.r);
			objectarraylist.add((float)trianglearraylist.size()/35);
			objectarraylist.add((float)object.trianglelist.length);
			
			for (int i=0;i<object.trianglelist.length;i++) {
				Triangle modeltri = object.trianglelist[i];
				trianglearraylist.add(-scale*(float)modeltri.pos1.x);
				trianglearraylist.add(scale*(float)modeltri.pos1.y);
				trianglearraylist.add(scale*(float)modeltri.pos1.z);
				trianglearraylist.add(-scale*(float)modeltri.pos2.x);
				trianglearraylist.add(scale*(float)modeltri.pos2.y);
				trianglearraylist.add(scale*(float)modeltri.pos2.z);
				trianglearraylist.add(-scale*(float)modeltri.pos3.x);
				trianglearraylist.add(scale*(float)modeltri.pos3.y);
				trianglearraylist.add(scale*(float)modeltri.pos3.z);
				trianglearraylist.add(-(float)modeltri.norm.dx);
				trianglearraylist.add((float)modeltri.norm.dy);
				trianglearraylist.add((float)modeltri.norm.dz);
				trianglearraylist.add((float)modeltri.pos1.tex.u);
				trianglearraylist.add((float)modeltri.pos1.tex.v);
				trianglearraylist.add((float)modeltri.pos2.tex.u);
				trianglearraylist.add((float)modeltri.pos2.tex.v);
				trianglearraylist.add((float)modeltri.pos3.tex.u);
				trianglearraylist.add((float)modeltri.pos3.tex.v);
				trianglearraylist.add((float)modeltri.mat.imageid);
				float[] matfacecolor = modeltri.mat.facecolor.getRGBComponents(new float[4]);
				trianglearraylist.add((float)matfacecolor[0]);
				trianglearraylist.add((float)matfacecolor[1]);
				trianglearraylist.add((float)matfacecolor[2]);
				trianglearraylist.add((float)matfacecolor[3]);
				float[] matemissivecolor = modeltri.mat.emissivecolor.getRGBComponents(new float[4]);
				trianglearraylist.add((float)matemissivecolor[0]);
				trianglearraylist.add((float)matemissivecolor[1]);
				trianglearraylist.add((float)matemissivecolor[2]);
				trianglearraylist.add((float)matemissivecolor[3]);
				float[] lightmapcolor = {0.0f,0.0f,0.0f,0.0f};
				if (modeltri.mat.ambientcolor!=null) {lightmapcolor = modeltri.mat.ambientcolor.getRGBComponents(new float[4]);}
				trianglearraylist.add((float)lightmapcolor[0]);
				trianglearraylist.add((float)lightmapcolor[1]);
				trianglearraylist.add((float)lightmapcolor[2]);
				trianglearraylist.add((float)lightmapcolor[3]);
				trianglearraylist.add((float)modeltri.mat.roughness);
				trianglearraylist.add((float)modeltri.mat.metallic);
				trianglearraylist.add((float)modeltri.mat.refraction);
				trianglearraylist.add((float)modeltri.mat.transparency);
			}
		}

		Float[] trianglefloats = trianglearraylist.toArray(new Float[trianglearraylist.size()]);
		triobjent.triangles = new float[trianglefloats.length];
		for (int i=0;i<trianglefloats.length;i++) {
			triobjent.triangles[i] = trianglefloats[i];
		}
		
		Float[] objectfloats = objectarraylist.toArray(new Float[objectarraylist.size()]);
		triobjent.objects = new float[objectfloats.length];
		for (int i=0;i<objectfloats.length;i++) {
			triobjent.objects[i] = objectfloats[i];
		}

		Float[] entityfloats = entityarraylist.toArray(new Float[entityarraylist.size()]);
		triobjent.entities = new float[entityfloats.length];
		for (int i=0;i<entityfloats.length;i++) {
			triobjent.entities[i] = entityfloats[i];
		}
		
		return triobjent;
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
				if (key==GLFW.GLFW_KEY_Q) {keyrleft = true;}
				if (key==GLFW.GLFW_KEY_E) {keyrright = true;}
				if (key==GLFW.GLFW_KEY_LEFT_CONTROL) {keyspeed = true;}
			}
			if (action==GLFW.GLFW_RELEASE) {
				if (key==GLFW.GLFW_KEY_W) {keyfwd = false;}
				if (key==GLFW.GLFW_KEY_S) {keyback = false;}
				if (key==GLFW.GLFW_KEY_A) {keyleft = false;}
				if (key==GLFW.GLFW_KEY_D) {keyright = false;}
				if (key==GLFW.GLFW_KEY_SPACE) {keyup = false;}
				if (key==GLFW.GLFW_KEY_LEFT_SHIFT) {keydown = false;}
				if (key==GLFW.GLFW_KEY_Q) {keyrleft = false;}
				if (key==GLFW.GLFW_KEY_E) {keyrright = false;}
				if (key==GLFW.GLFW_KEY_LEFT_CONTROL) {keyspeed = false;}
			}
		}
	}
	private class MousePositionProcessor implements GLFWCursorPosCallbackI {
		@Override public void invoke(long window, double xpos, double ypos) {
			mousex[0] = xpos;
			mousey[0] = ypos;
		}
	}
	private class MouseButtonProcessor implements GLFWMouseButtonCallbackI {
		@Override public void invoke(long window, int button, int action, int mods) {
			if ((button==0)&&(action==1)) {
				AL10.alSourcePlay(sourcebuf);
			}
		}
	}
	private class MouseWheelProcessor implements GLFWScrollCallbackI {
		@Override public void invoke(long window, double xoffset, double yoffset) {
			System.out.println("xoffset: "+xoffset+" yoffset: "+yoffset);
		}
	}


}
