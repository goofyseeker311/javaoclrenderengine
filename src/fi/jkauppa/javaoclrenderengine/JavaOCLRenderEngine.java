package fi.jkauppa.javaoclrenderengine;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

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
import fi.jkauppa.javarenderengine.ModelLib.Sphere;
import fi.jkauppa.javarenderengine.ModelLib.Triangle;
import fi.jkauppa.javarenderengine.UtilLib;

public class JavaOCLRenderEngine {
	private static String programtitle = "Java OpenCL Render Engine v1.0.8.4";
	private int screenwidth = 0, screenheight = 0, graphicswidth = 0, graphicsheight = 0, graphicslength = 0;
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
	private long graphicsbufferptr = NULL, graphicszbufferptr = NULL, graphicshbufferptr = NULL, camposbufferptr = NULL, cammovbufferptr = NULL;
	private long tri1ptr = NULL, tri1lenptr = NULL, tex1ptr = NULL, tex1lenptr = NULL, obj1ptr = NULL, obj1lenptr = NULL;
	private long tri2ptr = NULL, tri2lenptr = NULL, tex2ptr = NULL, tex2lenptr = NULL, obj2ptr = NULL, obj2lenptr = NULL;
	private long tri3ptr = NULL, tri3lenptr = NULL, tex3ptr = NULL, tex3lenptr = NULL, obj3ptr = NULL, obj3lenptr = NULL;
	private long litptr = NULL;
	@SuppressWarnings("unused")
	private long trianglesptr = NULL, triangleslenptr = NULL, texturesptr = NULL, textureslenptr = NULL, objectsptr = NULL, objectslenptr = NULL;
	private float[] graphicsbuffer = null;
	@SuppressWarnings("unused")
	private float[] graphicszbuffer = null;
	private int[] graphicshbuffer = null;
	private float[] camerapos3fov2res2rotmat16 = null;
	private float[] cameramov3rot3 = null;
	private float[] trianglelistpos3uv3id = null;
	private float[] trianglelist2pos3uv3id = null;
	private float[] trianglelist3pos3uv3id = null;
	private int[] trianglelistlength = {0};
	private int[] trianglelist2length = {0};
	private int[] trianglelist3length = {0};
	private int[] triangleslength = {0};
	private int[] triangletexturelist = null;
	private int[] triangletexture2list = null;
	private int[] triangletexture3list = null;
	private int[] triangletexturelength = {0};
	private int[] triangletexture2length = {0};
	private int[] triangletexture3length = {0};
	private float[] objectlistpos3sca3rot3relsph4 = null;
	private float[] objectlist2pos3sca3rot3relsph4 = null;
	private float[] objectlist3pos3sca3rot3relsph4 = null;
	private int[] objectlistlength = {0};
	private int[] objectlist2length = {0};
	private int[] objectlist3length = {0};
	private int[] renderlit = {1};
	private boolean keyfwd = false;
	private boolean keyback = false;
	private boolean keyleft = false;
	private boolean keyright = false;
	private boolean keyup = false;
	private boolean keydown = false;
	private boolean keyrleft = false;
	private boolean keyrright = false;
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
		this.screenwidth = 1280;
		this.screenheight = 720;
		long fullscreenmonitor = NULL;
		if (vfullscreen!=0) {
			this.isfullscreen = true;
			fullscreenmonitor = monitor;
			this.screenwidth = videomode.width();
			this.screenheight = videomode.height();
		}
		this.graphicswidth = screenwidth*2;
		this.graphicsheight = screenheight*2;
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
		if ((window=GLFW.glfwCreateWindow(screenwidth, screenheight, programtitle, fullscreenmonitor, NULL))==NULL) {System.out.println("GLFW create window failed."); System.exit(2);}
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

		Entity loadmodel = ModelLib.loadOBJFileEntity("res/models/asteroid8.obj", true);
		Entity loadmodel2 = ModelLib.loadOBJFileEntity("res/models/ship.obj", true);
		Entity loadmodel3 = ModelLib.loadOBJFileEntity("res/models/spaceboxgreen.obj", true);
		ArrayList<Float> trianglelistpos3uv3arraylist = new ArrayList<Float>();
		for (int j=0;j<loadmodel.childlist.length;j++) {
			Entity object = loadmodel.childlist[j];
			for (int i=0;i<object.trianglelist.length;i++) {
				Triangle modeltri = object.trianglelist[i];
				trianglelistpos3uv3arraylist.add((float)modeltri.pos1.x);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos1.y);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos1.z);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos2.x);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos2.y);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos2.z);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos3.x);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos3.y);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos3.z);
				trianglelistpos3uv3arraylist.add((float)modeltri.norm.dx);
				trianglelistpos3uv3arraylist.add((float)modeltri.norm.dy);
				trianglelistpos3uv3arraylist.add((float)modeltri.norm.dz);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos1.tex.u);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos1.tex.v);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos2.tex.u);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos2.tex.v);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos3.tex.u);
				trianglelistpos3uv3arraylist.add((float)modeltri.pos3.tex.v);
				trianglelistpos3uv3arraylist.add((float)modeltri.mat.materialid);
				float[] matfacecolor = modeltri.mat.facecolor.getRGBComponents(new float[4]);
				trianglelistpos3uv3arraylist.add((float)matfacecolor[0]);
				trianglelistpos3uv3arraylist.add((float)matfacecolor[1]);
				trianglelistpos3uv3arraylist.add((float)matfacecolor[2]);
				trianglelistpos3uv3arraylist.add((float)matfacecolor[3]);
				float[] matemissivecolor = modeltri.mat.emissivecolor.getRGBComponents(new float[4]);
				trianglelistpos3uv3arraylist.add((float)matemissivecolor[0]);
				trianglelistpos3uv3arraylist.add((float)matemissivecolor[1]);
				trianglelistpos3uv3arraylist.add((float)matemissivecolor[2]);
				trianglelistpos3uv3arraylist.add((float)matemissivecolor[3]);
				float[] lightmapcolor = {0.0f,0.0f,0.0f,0.0f};
				if (modeltri.mat.ambientcolor!=null) {lightmapcolor = modeltri.mat.ambientcolor.getRGBComponents(new float[4]);}
				trianglelistpos3uv3arraylist.add((float)lightmapcolor[0]);
				trianglelistpos3uv3arraylist.add((float)lightmapcolor[1]);
				trianglelistpos3uv3arraylist.add((float)lightmapcolor[2]);
				trianglelistpos3uv3arraylist.add((float)lightmapcolor[3]);
				trianglelistpos3uv3arraylist.add((float)modeltri.mat.roughness);
				trianglelistpos3uv3arraylist.add((float)modeltri.mat.metallic);
				trianglelistpos3uv3arraylist.add((float)modeltri.mat.refraction);
				trianglelistpos3uv3arraylist.add((float)modeltri.mat.transparency);
			}
		}
		ArrayList<Float> trianglelist2pos3uv3arraylist = new ArrayList<Float>();
		for (int j=0;j<loadmodel2.childlist.length;j++) {
			Entity object = loadmodel2.childlist[j];
			for (int i=0;i<object.trianglelist.length;i++) {
				Triangle modeltri = object.trianglelist[i];
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos1.x);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos1.y);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos1.z);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos2.x);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos2.y);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos2.z);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos3.x);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos3.y);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos3.z);
				trianglelist2pos3uv3arraylist.add((float)modeltri.norm.dx);
				trianglelist2pos3uv3arraylist.add((float)modeltri.norm.dy);
				trianglelist2pos3uv3arraylist.add((float)modeltri.norm.dz);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos1.tex.u);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos1.tex.v);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos2.tex.u);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos2.tex.v);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos3.tex.u);
				trianglelist2pos3uv3arraylist.add((float)modeltri.pos3.tex.v);
				trianglelist2pos3uv3arraylist.add((float)modeltri.mat.materialid);
				float[] matfacecolor = modeltri.mat.facecolor.getRGBComponents(new float[4]);
				trianglelist2pos3uv3arraylist.add((float)matfacecolor[0]);
				trianglelist2pos3uv3arraylist.add((float)matfacecolor[1]);
				trianglelist2pos3uv3arraylist.add((float)matfacecolor[2]);
				trianglelist2pos3uv3arraylist.add((float)matfacecolor[3]);
				float[] matemissivecolor = modeltri.mat.emissivecolor.getRGBComponents(new float[4]);
				trianglelist2pos3uv3arraylist.add((float)matemissivecolor[0]);
				trianglelist2pos3uv3arraylist.add((float)matemissivecolor[1]);
				trianglelist2pos3uv3arraylist.add((float)matemissivecolor[2]);
				trianglelist2pos3uv3arraylist.add((float)matemissivecolor[3]);
				float[] lightmapcolor = {0.0f,0.0f,0.0f,0.0f};
				if (modeltri.mat.ambientcolor!=null) {lightmapcolor = modeltri.mat.ambientcolor.getRGBComponents(new float[4]);}
				trianglelist2pos3uv3arraylist.add((float)lightmapcolor[0]);
				trianglelist2pos3uv3arraylist.add((float)lightmapcolor[1]);
				trianglelist2pos3uv3arraylist.add((float)lightmapcolor[2]);
				trianglelist2pos3uv3arraylist.add((float)lightmapcolor[3]);
				trianglelist2pos3uv3arraylist.add((float)modeltri.mat.roughness);
				trianglelist2pos3uv3arraylist.add((float)modeltri.mat.metallic);
				trianglelist2pos3uv3arraylist.add((float)modeltri.mat.refraction);
				trianglelist2pos3uv3arraylist.add((float)modeltri.mat.transparency);
			}
		}
		ArrayList<Float> trianglelist3pos3uv3arraylist = new ArrayList<Float>();
		for (int j=0;j<loadmodel3.childlist.length;j++) {
			Entity object = loadmodel3.childlist[j];
			for (int i=0;i<object.trianglelist.length;i++) {
				Triangle modeltri = object.trianglelist[i];
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos1.x);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos1.y);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos1.z);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos2.x);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos2.y);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos2.z);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos3.x);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos3.y);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos3.z);
				trianglelist3pos3uv3arraylist.add((float)modeltri.norm.dx);
				trianglelist3pos3uv3arraylist.add((float)modeltri.norm.dy);
				trianglelist3pos3uv3arraylist.add((float)modeltri.norm.dz);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos1.tex.u);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos1.tex.v);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos2.tex.u);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos2.tex.v);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos3.tex.u);
				trianglelist3pos3uv3arraylist.add((float)modeltri.pos3.tex.v);
				trianglelist3pos3uv3arraylist.add((float)modeltri.mat.materialid);
				float[] matfacecolor = modeltri.mat.facecolor.getRGBComponents(new float[4]);
				trianglelist3pos3uv3arraylist.add((float)matfacecolor[0]);
				trianglelist3pos3uv3arraylist.add((float)matfacecolor[1]);
				trianglelist3pos3uv3arraylist.add((float)matfacecolor[2]);
				trianglelist3pos3uv3arraylist.add((float)matfacecolor[3]);
				float[] matemissivecolor = modeltri.mat.emissivecolor.getRGBComponents(new float[4]);
				trianglelist3pos3uv3arraylist.add((float)matemissivecolor[0]);
				trianglelist3pos3uv3arraylist.add((float)matemissivecolor[1]);
				trianglelist3pos3uv3arraylist.add((float)matemissivecolor[2]);
				trianglelist3pos3uv3arraylist.add((float)matemissivecolor[3]);
				float[] lightmapcolor = {0.0f,0.0f,0.0f,0.0f};
				if (modeltri.mat.ambientcolor!=null) {lightmapcolor = modeltri.mat.ambientcolor.getRGBComponents(new float[4]);}
				trianglelist3pos3uv3arraylist.add((float)lightmapcolor[0]);
				trianglelist3pos3uv3arraylist.add((float)lightmapcolor[1]);
				trianglelist3pos3uv3arraylist.add((float)lightmapcolor[2]);
				trianglelist3pos3uv3arraylist.add((float)lightmapcolor[3]);
				trianglelist3pos3uv3arraylist.add((float)modeltri.mat.roughness);
				trianglelist3pos3uv3arraylist.add((float)modeltri.mat.metallic);
				trianglelist3pos3uv3arraylist.add((float)modeltri.mat.refraction);
				trianglelist3pos3uv3arraylist.add((float)modeltri.mat.transparency);
			}
		}

		Float[] trianglelistpos3uv3idfloats = trianglelistpos3uv3arraylist.toArray(new Float[trianglelistpos3uv3arraylist.size()]);
		this.trianglelistpos3uv3id = new float[trianglelistpos3uv3idfloats.length];
		for (int i=0;i<trianglelistpos3uv3idfloats.length;i++) {
			this.trianglelistpos3uv3id[i] = trianglelistpos3uv3idfloats[i];
		}
		this.trianglelistlength[0] = this.trianglelistpos3uv3id.length/35;
		Float[] trianglelist2pos3uv3idfloats = trianglelist2pos3uv3arraylist.toArray(new Float[trianglelist2pos3uv3arraylist.size()]);
		this.trianglelist2pos3uv3id = new float[trianglelist2pos3uv3idfloats.length];
		for (int i=0;i<trianglelist2pos3uv3idfloats.length;i++) {
			this.trianglelist2pos3uv3id[i] = trianglelist2pos3uv3idfloats[i];
		}
		this.trianglelist2length[0] = this.trianglelist2pos3uv3id.length/35;
		Float[] trianglelist3pos3uv3idfloats = trianglelist3pos3uv3arraylist.toArray(new Float[trianglelist3pos3uv3arraylist.size()]);
		this.trianglelist3pos3uv3id = new float[trianglelist3pos3uv3idfloats.length];
		for (int i=0;i<trianglelist3pos3uv3idfloats.length;i++) {
			this.trianglelist3pos3uv3id[i] = trianglelist3pos3uv3idfloats[i];
		}
		this.trianglelist3length[0] = this.trianglelist3pos3uv3id.length/35;

		if (loadmodel.materiallist[0].fileimage!=null) {
			BufferedImage textureimage = loadmodel.materiallist[0].fileimage;
			this.triangletexturelength[0] = textureimage.getWidth();
			int texturesize = this.triangletexturelength[0]*this.triangletexturelength[0];
			this.triangletexturelist = new int[loadmodel.materiallist.length*texturesize];
			for (int j=0;j<loadmodel.materiallist.length;j++) {
				textureimage = loadmodel.materiallist[j].fileimage;
				DataBufferInt textureimagedataint = (DataBufferInt)textureimage.getRaster().getDataBuffer();
				int[] texturedata = textureimagedataint.getData();
				for (int i=0;i<texturesize;i++) {
					this.triangletexturelist[j*texturesize+i] = texturedata[i];
				}
			}
		}
		if (loadmodel2.materiallist[0].fileimage!=null) {
			BufferedImage textureimage = loadmodel2.materiallist[0].fileimage;
			this.triangletexture2length[0] = textureimage.getWidth();
			int texturesize = this.triangletexture2length[0]*this.triangletexture2length[0];
			this.triangletexture2list = new int[loadmodel2.materiallist.length*texturesize];
			for (int j=0;j<loadmodel2.materiallist.length;j++) {
				textureimage = loadmodel2.materiallist[j].fileimage;
				DataBufferInt textureimagedataint = (DataBufferInt)textureimage.getRaster().getDataBuffer();
				int[] texturedata = textureimagedataint.getData();
				for (int i=0;i<texturesize;i++) {
					this.triangletexture2list[j*texturesize+i] = texturedata[i];
				}
			}
		}
		if (loadmodel3.materiallist[0].fileimage!=null) {
			BufferedImage textureimage = loadmodel3.materiallist[0].fileimage;
			this.triangletexture3length[0] = textureimage.getWidth();
			int texturesize = this.triangletexture3length[0]*this.triangletexture3length[0];
			this.triangletexture3list = new int[loadmodel3.materiallist.length*texturesize];
			for (int j=0;j<loadmodel3.materiallist.length;j++) {
				textureimage = loadmodel3.materiallist[j].fileimage;
				DataBufferInt textureimagedataint = (DataBufferInt)textureimage.getRaster().getDataBuffer();
				int[] texturedata = textureimagedataint.getData();
				for (int i=0;i<texturesize;i++) {
					this.triangletexture3list[j*texturesize+i] = texturedata[i];
				}
			}
		}

		Sphere sphbv = loadmodel.sphereboundaryvolume;
		this.objectlistpos3sca3rot3relsph4 = new float[]{
				10.0f,0.0f,0.0f, 1.0f,1.0f,1.0f, 0.0f,0.0f,0.0f, (float)sphbv.x,(float)sphbv.y,(float)sphbv.z,(float)sphbv.r,
				-10.0f,0.0f,0.0f, 1.0f,1.0f,1.0f, 45.0f,0.0f,0.0f, (float)sphbv.x,(float)sphbv.y,(float)sphbv.z,(float)sphbv.r,
				0.0f,10.0f,0.0f, 1.0f,1.0f,1.0f, 0.0f,70.0f,0.0f, (float)sphbv.x,(float)sphbv.y,(float)sphbv.z,(float)sphbv.r,
				0.0f,-10.0f,0.0f, 1.0f,1.0f,1.0f, 0.0f,0.0f,20.0f, (float)sphbv.x,(float)sphbv.y,(float)sphbv.z,(float)sphbv.r,
		};
		this.objectlistlength[0] = this.objectlistpos3sca3rot3relsph4.length/13;
		Sphere sphbv2 = loadmodel2.sphereboundaryvolume;
		this.objectlist2pos3sca3rot3relsph4 = new float[]{
				0.0f,0.0f,0.0f, 1.0f,1.0f,1.0f, 0.0f,0.0f,0.0f, (float)sphbv2.x,(float)sphbv2.y,(float)sphbv2.z,(float)sphbv2.r,
				0.0f,0.0f,5.0f, 1.0f,1.0f,1.0f, 0.0f,0.0f,0.0f, (float)sphbv2.x,(float)sphbv2.y,(float)sphbv2.z,(float)sphbv2.r,
				0.0f,5.0f,0.0f, 1.0f,1.0f,1.0f, 90.0f,0.0f,0.0f, (float)sphbv2.x,(float)sphbv2.y,(float)sphbv2.z,(float)sphbv2.r,
				0.0f,-5.0f,0.0f, 1.0f,1.0f,1.0f, 90.0f,0.0f,0.0f, (float)sphbv2.x,(float)sphbv2.y,(float)sphbv2.z,(float)sphbv2.r,
		};
		this.objectlist2length[0] = this.objectlist2pos3sca3rot3relsph4.length/13;
		Sphere sphbv3 = loadmodel3.sphereboundaryvolume;
		this.objectlist3pos3sca3rot3relsph4 = new float[]{
				0.0f,0.0f,0.0f, 1.0f,1.0f,1.0f, 0.0f,0.0f,0.0f, (float)sphbv3.x,(float)sphbv3.y,(float)sphbv3.z,(float)sphbv3.r,
		};
		this.objectlist3length[0] = this.objectlist3pos3sca3rot3relsph4.length/13;
		
		triangleslength[0] = 0;

		if (this.glinterop) {
			this.graphicsbufferptr = computelib.createSharedGLBuffer(opencldevice, buf);
		} else {
			this.graphicsbuffer = new float[graphicslength*4];
			this.graphicsbufferptr = computelib.createBuffer(opencldevice, graphicslength*4);
		}
		this.graphicszbufferptr = computelib.createBuffer(opencldevice, graphicslength);
		this.graphicszbuffer = new float[graphicslength];
		this.graphicshbufferptr = computelib.createBuffer(opencldevice, 1);
		this.graphicshbuffer = new int[1];
		this.camposbufferptr = computelib.createBuffer(opencldevice, camerapos3fov2res2rotmat16.length);
		computelib.writeBufferf(opencldevice, queue, camposbufferptr, camerapos3fov2res2rotmat16);
		this.cammovbufferptr = computelib.createBuffer(opencldevice, cameramov3rot3.length);
		computelib.writeBufferf(opencldevice, queue, cammovbufferptr, cameramov3rot3);

		this.trianglesptr = computelib.createBuffer(opencldevice, triangleslength[0]);
		
		this.tri1ptr = computelib.createBuffer(opencldevice, trianglelistpos3uv3id.length);
		computelib.writeBufferf(opencldevice, queue, tri1ptr, trianglelistpos3uv3id);
		this.tri1lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, tri1lenptr, trianglelistlength);
		this.tex1ptr = computelib.createBuffer(opencldevice, triangletexturelist.length);
		computelib.writeBufferi(opencldevice, queue, tex1ptr, triangletexturelist);
		this.tex1lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, tex1lenptr, triangletexturelength);
		this.obj1ptr = computelib.createBuffer(opencldevice, objectlistpos3sca3rot3relsph4.length);
		computelib.writeBufferf(opencldevice, queue, obj1ptr, objectlistpos3sca3rot3relsph4);
		this.obj1lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, obj1lenptr, objectlistlength);

		this.tri2ptr = computelib.createBuffer(opencldevice, trianglelist2pos3uv3id.length);
		computelib.writeBufferf(opencldevice, queue, tri2ptr, trianglelist2pos3uv3id);
		this.tri2lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, tri2lenptr, trianglelist2length);
		this.tex2ptr = computelib.createBuffer(opencldevice, triangletexture2list.length);
		computelib.writeBufferi(opencldevice, queue, tex2ptr, triangletexture2list);
		this.tex2lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, tex2lenptr, triangletexture2length);
		this.obj2ptr = computelib.createBuffer(opencldevice, objectlist2pos3sca3rot3relsph4.length);
		computelib.writeBufferf(opencldevice, queue, obj2ptr, objectlist2pos3sca3rot3relsph4);
		this.obj2lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, obj2lenptr, objectlist2length);

		this.tri3ptr = computelib.createBuffer(opencldevice, trianglelist3pos3uv3id.length);
		computelib.writeBufferf(opencldevice, queue, tri3ptr, trianglelist3pos3uv3id);
		this.tri3lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, tri3lenptr, trianglelist3length);
		this.tex3ptr = computelib.createBuffer(opencldevice, triangletexture3list.length);
		computelib.writeBufferi(opencldevice, queue, tex3ptr, triangletexture3list);
		this.tex3lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, tex3lenptr, triangletexture3length);
		this.obj3ptr = computelib.createBuffer(opencldevice, objectlist3pos3sca3rot3relsph4.length);
		computelib.writeBufferf(opencldevice, queue, obj3ptr, objectlist3pos3sca3rot3relsph4);
		this.obj3lenptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, obj3lenptr, objectlist3length);

		this.litptr = computelib.createBuffer(opencldevice, 1);
		computelib.writeBufferi(opencldevice, queue, litptr, renderlit);
		
		String programSource = ComputeLib.loadProgram("res/clprograms/programlib.cl", true);
		this.program = this.computelib.compileProgram(opencldevice, programSource);
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
		if (this.keyfwd) {cameramov3rot3[0] += ds;}
		if (this.keyback) {cameramov3rot3[0] -= ds;}
		if (this.keyleft) {cameramov3rot3[1] -= ds;}
		if (this.keyright) {cameramov3rot3[1] += ds;}
		if (this.keyup) {cameramov3rot3[2] += ds;}
		if (this.keydown) {cameramov3rot3[2] -= ds;}
		if (this.keyrleft) {cameramov3rot3[3] += ds;}
		if (this.keyrright) {cameramov3rot3[3] -= ds;}
		cameramov3rot3[5] = (float)(0.001f*(mousex[0]-lastmousex));
		cameramov3rot3[4] = (float)(0.001f*(mousey[0]-lastmousey));
		lastmousex = mousex[0];
		lastmousey = mousey[0];
	}

	public void render() {
		long framestarttime = System.nanoTime();
		computelib.writeBufferf(opencldevice, queue, cammovbufferptr, cameramov3rot3);
		computelib.runProgram(opencldevice, queue, program, "movecamera", new long[]{camposbufferptr,cammovbufferptr}, new int[]{0}, new int[]{1});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "clearview", new long[]{graphicsbufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr}, new int[]{0,0}, new int[]{graphicswidth,4});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "renderplaneview", new long[]{graphicsbufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr,tri1ptr,tri1lenptr,tex1ptr,tex1lenptr,obj1ptr,obj1lenptr,litptr}, new int[]{0,0}, new int[]{graphicswidth,4});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "renderplaneview", new long[]{graphicsbufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr,tri2ptr,tri2lenptr,tex2ptr,tex2lenptr,obj2ptr,obj2lenptr,litptr}, new int[]{0,0}, new int[]{graphicswidth,4});
		computelib.insertBarrier(queue);
		computelib.runProgram(opencldevice, queue, program, "renderplaneview", new long[]{graphicsbufferptr,graphicszbufferptr,graphicshbufferptr,camposbufferptr,tri3ptr,tri3lenptr,tex3ptr,tex3lenptr,obj3ptr,obj3lenptr,litptr}, new int[]{0,0}, new int[]{graphicswidth,4});
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
