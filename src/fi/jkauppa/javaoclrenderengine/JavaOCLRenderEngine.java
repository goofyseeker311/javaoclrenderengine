package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Timer;
import java.util.TimerTask;

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
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.Callback;
import org.lwjgl.system.MemoryUtil;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class JavaOCLRenderEngine {
	private static String programtitle = "Java OpenCL Render Engine v1.0.0.2";
	private int graphicswidth = 0, graphicsheight = 0, graphicslength = 0;
	private long window = NULL;
    @SuppressWarnings("unused")
	private GLCapabilities caps = null;
    @SuppressWarnings("unused")
	private Callback debugProc = null;
	private int vao = 0;
	private int tex = 0;
	private int quadProgram = 0;
	private int quadProgram_inputPosition = 0;
	private int quadProgram_inputTextureCoords = 0;
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
	private long monitor = NULL;
	@SuppressWarnings("unused")
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
		this.graphicswidth = 1280;
		this.graphicsheight = 720;
		this.graphicslength = this.graphicswidth*this.graphicsheight;
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
		if ((window=GLFW.glfwCreateWindow(graphicswidth, graphicsheight, programtitle, NULL, NULL))==NULL) {System.out.println("GLFW create window failed."); System.exit(2);}
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
		GL31.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		GL31.glClear(GL31.GL_COLOR_BUFFER_BIT);
		GLFW.glfwSwapBuffers(window);
		this.graphicsbuffer = new int[this.graphicslength];
		this.graphicszbuffer = new float[this.graphicslength];
		this.cameraposrot3fovres = new float[]{0.0f,0.0f,0.2f, 0.0f,0.0f,0.0f, 70.0f,39.375f, graphicswidth,graphicsheight};
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

	public void run() {
		while(!GLFW.glfwWindowShouldClose(window)) {
			GL31.glClear(GL31.GL_COLOR_BUFFER_BIT | GL31.GL_DEPTH_BUFFER_BIT);
			updateTexture(tex, graphicswidth, graphicsheight, graphicsbuffer);
			GL46.glViewport(0, 0, graphicswidth, graphicsheight);
	    	GL46.glUseProgram(quadProgram);
	    	GL46.glBindVertexArray(vao);
	    	GL46.glDrawArrays(GL46.GL_TRIANGLES, 0, 6);
	    	GL46.glBindVertexArray(0);
	    	GL46.glUseProgram(0);
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
		try {argdevice = Integer.parseInt(args[0]);} catch(Exception ex) {}
		JavaOCLRenderEngine app = new JavaOCLRenderEngine(argdevice);
		app.run();
	}

	private class TickTimerTask extends TimerTask {@Override public void run() {
		long nanonewtimetick = System.nanoTime();
		lasttimedeltaseconds = (nanonewtimetick - nanolasttimetick)/1000000000.0f;
		nanolasttimetick = nanonewtimetick;
		tick(lasttimedeltaseconds);}
	}

	private void tick(float deltatimeseconds) {
		float ds = deltatimeseconds;
		GLFW.glfwSetWindowTitle(window, programtitle+": "+String.format("%.0f",1000.0f/frametimeavg).replace(',', '.')+
				"fps, computetime: "+String.format("%.3f",computetimeavg).replace(',', '.')+"ms ["+usingdevice+"] ("
				+graphicswidth+"x"+graphicsheight+") tickdeltatime: "+String.format("%.0f",deltatimeseconds*1000.0f)+"ms"
				);
		int len = trianglelistlength[0]-1;
		trianglelistpos3rgba[13*len+9] += 0.1f*ds; if (trianglelistpos3rgba[13*len+9]>1.0f) {trianglelistpos3rgba[13*len+9]=0.0f;}
		trianglelistpos3rgba[13*len+10] += 0.15f*ds; if (trianglelistpos3rgba[13*len+10]>1.0f) {trianglelistpos3rgba[13*len+10]=0.0f;}
		trianglelistpos3rgba[13*len+11] += 0.175f*ds; if (trianglelistpos3rgba[13*len+11]>1.0f) {trianglelistpos3rgba[13*len+11]=0.0f;}
		if (this.keyfwd) {cameraposrot3fovres[0] += ds;}
		if (this.keyback) {cameraposrot3fovres[0] -= ds;}
		if (this.keyleft) {cameraposrot3fovres[1] -= ds;}
		if (this.keyright) {cameraposrot3fovres[1] += ds;}
		if (this.keyup) {cameraposrot3fovres[2] += ds;}
		if (this.keydown) {cameraposrot3fovres[2] -= ds;}
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
				int[] newgraphicsbuffer = new int[graphicslength];
				computelib.readBufferi(device, queue, graphicspointerbuffer[0], newgraphicsbuffer);
				graphicsbuffer = newgraphicsbuffer;
				long frameendtime = System.nanoTime();
				frametime = (frameendtime-framestarttime)/1000000.0f;
				frametimeavg = frametimeavg*0.9f+frametime*0.1f;
				threadrunning = false;
			}
		}
	}
	
    private void createQuadProgram() {
        int program = GL46.glCreateProgram();
        int vshader = createShader("res/glshaders/texturedquad.vs", GL46.GL_VERTEX_SHADER, true);
        int fshader = createShader("res/glshaders/texturedquad.fs", GL46.GL_FRAGMENT_SHADER, true);
        GL46.glAttachShader(program, vshader);
        GL46.glAttachShader(program, fshader);
        GL46.glLinkProgram(program);
        int linked = GL46.glGetProgrami(program, GL46.GL_LINK_STATUS);
        String programLog = GL46.glGetProgramInfoLog(program);
        if (programLog.trim().length() > 0) {System.err.println(programLog);}
        if (linked == 0) {throw new AssertionError("Could not link program");}
        GL46.glUseProgram(program);
        int texLocation = GL46.glGetUniformLocation(program, "tex");
        GL46.glUniform1i(texLocation, 0);
        quadProgram_inputPosition = GL46.glGetAttribLocation(program, "position");
        quadProgram_inputTextureCoords = GL46.glGetAttribLocation(program, "texCoords");
        GL46.glUseProgram(0);
        this.quadProgram = program;
    }

    private void createFullScreenQuad() {
        vao = GL46.glGenVertexArrays();
        GL46.glBindVertexArray(vao);
        int positionVbo = GL46.glGenBuffers();
        FloatBuffer fb = BufferUtils.createFloatBuffer(2 * 6);
        fb.put(-1.0f).put(-1.0f);
        fb.put(1.0f).put(-1.0f);
        fb.put(1.0f).put(1.0f);
        fb.put(1.0f).put(1.0f);
        fb.put(-1.0f).put(1.0f);
        fb.put(-1.0f).put(-1.0f);
        fb.flip();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, positionVbo);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, fb, GL46.GL_STATIC_DRAW);
        GL46.glVertexAttribPointer(quadProgram_inputPosition, 2, GL46.GL_FLOAT, false, 0, 0L);
        GL46.glEnableVertexAttribArray(quadProgram_inputPosition);
        int texCoordsVbo = GL46.glGenBuffers();
        fb = BufferUtils.createFloatBuffer(2 * 6);
        fb.put(0.0f).put(1.0f);
        fb.put(1.0f).put(1.0f);
        fb.put(1.0f).put(0.0f);
        fb.put(1.0f).put(0.0f);
        fb.put(0.0f).put(0.0f);
        fb.put(0.0f).put(1.0f);
        fb.flip();
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, texCoordsVbo);
        GL46.glBufferData(GL46.GL_ARRAY_BUFFER, fb, GL46.GL_STATIC_DRAW);
        GL46.glVertexAttribPointer(quadProgram_inputTextureCoords, 2, GL46.GL_FLOAT, true, 0, 0L);
        GL46.glEnableVertexAttribArray(quadProgram_inputTextureCoords);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, 0);
        GL46.glBindVertexArray(0);
    }

    private int createTexture(int texturewidth, int textureheight) {
        int id = GL46.glGenTextures();
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, id);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MIN_FILTER, GL46.GL_NEAREST);
        GL46.glTexParameteri(GL46.GL_TEXTURE_2D, GL46.GL_TEXTURE_MAG_FILTER, GL46.GL_NEAREST);
        GL46.glTexImage2D(GL46.GL_TEXTURE_2D, 0, GL46.GL_RGBA8, texturewidth, textureheight, 0, GL46.GL_RGBA, GL46.GL_UNSIGNED_INT_8_8_8_8, MemoryUtil.NULL);
        return id;
    }
    
    private void updateTexture(int id, int texturewidth, int textureheight, int[] texturebuffer) {
        GL46.glBindTexture(GL46.GL_TEXTURE_2D, id);
        GL46.glTexSubImage2D(GL46.GL_TEXTURE_2D, 0, 0, 0, texturewidth, textureheight, GL46.GL_RGBA, GL46.GL_UNSIGNED_INT_8_8_8_8, texturebuffer);
    }
    
    private int createShader(String resource, int type, boolean loadresourcefromjar) {
        int shader = GL46.glCreateShader(type);
        String sourceShader = loadShader(resource, loadresourcefromjar);
        ByteBuffer source = BufferUtils.createByteBuffer(8192);
        source.put(sourceShader.getBytes()).rewind();
        PointerBuffer strings = BufferUtils.createPointerBuffer(1);
        IntBuffer lengths = BufferUtils.createIntBuffer(1);
        strings.put(0, source);
        lengths.put(0, source.remaining());
        GL46.glShaderSource(shader, strings, lengths);
        GL46.glCompileShader(shader);
        int compiled = GL46.glGetShaderi(shader, GL46.GL_COMPILE_STATUS);
        String shaderLog = GL46.glGetShaderInfoLog(shader);
        if (shaderLog.trim().length() > 0) {System.err.println(shaderLog);}
        if (compiled == 0) {throw new AssertionError("Could not compile shader");}
        return shader;
    }    
    
	private String loadShader(String filename, boolean loadresourcefromjar) {
		String k = null;
		if (filename!=null) {
			try {
				File textfile = new File(filename);
				BufferedInputStream textfilestream = null;
				if (loadresourcefromjar) {
					textfilestream = new BufferedInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(textfile.getPath().replace(File.separatorChar, '/')));
				}else {
					textfilestream = new BufferedInputStream(new FileInputStream(textfile));
				}
				k = new String(textfilestream.readAllBytes());
				textfilestream.close();
			} catch (Exception ex) {ex.printStackTrace();}
		}
		return k;
	}
	

	private class KeyProcessor implements GLFWKeyCallbackI {
		@Override public void invoke(long window, int key, int scancode, int action, int mods) {
			System.out.println("key: "+key+" scancode: "+scancode+" action: "+action+" mods: "+mods);
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
