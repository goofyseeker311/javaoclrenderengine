package fi.jkauppa.javaoclrenderengine;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.TreeMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CL12GL;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.opengl.GL31;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ComputeLib {
	public TreeMap<Long,Device> devicemap = null;
	public Long[] devicelist = null;

	public ComputeLib() {
		this.devicemap = initClDevices();
		devicelist = devicemap.keySet().toArray(new Long[devicemap.size()]);
		for (int i=0;i<devicelist.length;i++) {
			long device = devicelist[i];
			Device devicedata = devicemap.get(device);
			System.out.println("OpenCL device["+i+"]: "+devicedata.devicename);
		}
	}

	public void writeBufferf(long device, long queue, long vmem, float[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueWriteBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
		MemoryStack.stackPop();
	}
	public void writeBufferi(long device, long queue, long vmem, int[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueWriteBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
		MemoryStack.stackPop();
	}

	public void readBufferf(long device, long queue, long vmem, float[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueReadBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
		MemoryStack.stackPop();
	}
	public void readBufferi(long device, long queue, long vmem, int[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueReadBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
		MemoryStack.stackPop();
	}

	public void fillBufferf(long vmem, long queue, float fill, int size) {
		MemoryStack clStack = MemoryStack.stackPush();
		ByteBuffer pattern = clStack.malloc(4);
		pattern.putFloat(fill);
		pattern.rewind();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueFillBuffer(queue, vmem, pattern, 0, size*4, null, event);
		CL12.clWaitForEvents(event);
		MemoryStack.stackPop();
	}
	public void fillBufferi(long vmem, long queue, int fill, int size) {
		MemoryStack clStack = MemoryStack.stackPush();
		ByteBuffer pattern = clStack.malloc(4);
		pattern.putInt(fill);
		pattern.rewind();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueFillBuffer(queue, vmem, pattern, 0, size*4, null, event);
		CL12.clWaitForEvents(event);
		MemoryStack.stackPop();
	}

	public long createQueue(long device) {
		MemoryStack clStack = MemoryStack.stackPush();
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		IntBuffer errcode_ret = clStack.callocInt(1);
		long queue = CL12.clCreateCommandQueue(context, device, CL12.CL_QUEUE_PROFILING_ENABLE, errcode_ret);
		MemoryStack.stackPop();
		return queue;
	}
	public void waitForQueue(long queue) {
		CL12.clFinish(queue);
	}

	public long createBuffer(long device, int size) {
		MemoryStack clStack = MemoryStack.stackPush();
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		IntBuffer errcode_ret = clStack.callocInt(1);
		long buffer = CL12.clCreateBuffer(context, CL12.CL_MEM_READ_WRITE, size*4, errcode_ret);
		MemoryStack.stackPop();
		return buffer;
	}
	public void removeBuffer(long vmem) {
		CL12.clReleaseMemObject(vmem);
	}

	public long createBufferFromGLTexture(long device, int texture) {
		MemoryStack clStack = MemoryStack.stackPush();
		IntBuffer errcode_ret = clStack.callocInt(1);
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		long buffer = CL12GL.clCreateFromGLTexture2D(context, CL12.CL_MEM_READ_WRITE, GL31.GL_TEXTURE_2D, 0, texture, errcode_ret);
		MemoryStack.stackPop();
		return buffer;
	}
	public void acquireGLTextureBuffer(long queue, long vmem) {
		CL12GL.clEnqueueAcquireGLObjects(queue, vmem, null, null);
	}
	public void releaseGLTextureBuffer(long queue, long vmem) {
		CL12GL.clEnqueueReleaseGLObjects(queue, vmem, null, null);
	}

	public String loadProgram(String filename, boolean loadresourcefromjar) {
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
	public long compileProgram(long device, String source) {
		long program = MemoryUtil.NULL;
		MemoryStack clStack = MemoryStack.stackPush();
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		IntBuffer errcode_ret = clStack.callocInt(1);
		program = CL12.clCreateProgramWithSource(context, source, errcode_ret);
		if (CL12.clBuildProgram(program, device, "", null, MemoryUtil.NULL)!=CL12.CL_SUCCESS) {
			String buildinfo = getClProgramBuildInfo(program, device, CL12.CL_PROGRAM_BUILD_LOG);
			System.out.println("compileProgram build failed:");
			System.out.println(buildinfo);
		}
		MemoryStack.stackPop();
		return program;
	}
	public float runProgram(long device, long queue, long program, String entry, long[] fmem, int[] offset, int[] size, boolean waitgetruntime) {
		float runtime = 0.0f;
		MemoryStack clStack = MemoryStack.stackPush();
		IntBuffer errcode_ret = clStack.callocInt(1);
		long kernel = CL12.clCreateKernel(program, entry, errcode_ret);
		int errcode_ret_int = errcode_ret.get(errcode_ret.position());
		if (errcode_ret_int==CL12.CL_SUCCESS) {
			for (int i=0;i<fmem.length;i++) {
				CL12.clSetKernelArg1p(kernel, i, fmem[i]);
			}
			int dimensions = offset.length; if (size.length<dimensions) {dimensions = size.length;}
			PointerBuffer globalWorkOffset = BufferUtils.createPointerBuffer(dimensions);
			PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
			for (int i=0;i<dimensions;i++) {
				globalWorkOffset.put(i, offset[i]);
				globalWorkSize.put(i, size[i]);
			}
			PointerBuffer event = clStack.mallocPointer(1);
			int kernel_error_int = CL12.clEnqueueNDRangeKernel(queue, kernel, dimensions, globalWorkOffset, globalWorkSize, null, null, event);
			if ((kernel_error_int==CL12.CL_SUCCESS)&&(waitgetruntime)) {
				CL12.clWaitForEvents(event);
				long eventLong = event.get(0);
				long[] ctimestart = {0};
				long[] ctimeend = {0};
				CL12.clGetEventProfilingInfo(eventLong, CL12.CL_PROFILING_COMMAND_START, ctimestart, (PointerBuffer)null);
				CL12.clGetEventProfilingInfo(eventLong, CL12.CL_PROFILING_COMMAND_END, ctimeend, (PointerBuffer)null);
				runtime = (ctimeend[0]-ctimestart[0])/1000000.0f;
			}
		}
		MemoryStack.stackPop();
		return runtime;
	}

	public static class Device {
		public long platform = MemoryUtil.NULL;
		public long context = MemoryUtil.NULL;
		public long queue = MemoryUtil.NULL;
		public String platformname = null;
		public CLCapabilities plaformcaps = null;
		public String devicename = null;
	}

	private TreeMap<Long,Device> initClDevices() {
		TreeMap<Long,Device> devicesinit = new TreeMap<Long,Device>();
		MemoryStack clStack = MemoryStack.stackPush();
		IntBuffer pi = clStack.mallocInt(1);
		if (CL12.clGetPlatformIDs(null, pi)==CL12.CL_SUCCESS) {
			PointerBuffer clPlatforms = clStack.mallocPointer(pi.get(0));
			if (CL12.clGetPlatformIDs(clPlatforms, (IntBuffer)null)==CL12.CL_SUCCESS) {
				for (int p = 0; p < clPlatforms.capacity(); p++) {
					long platform = clPlatforms.get(p);
					PointerBuffer clCtxProps = clStack.mallocPointer(3);
					clCtxProps.put(0, CL12.CL_CONTEXT_PLATFORM).put(1, platform).put(2, 0);
					IntBuffer pi2 = clStack.mallocInt(1);
					if (CL12.clGetDeviceIDs(platform, CL12.CL_DEVICE_TYPE_ALL, null, pi2)==CL12.CL_SUCCESS) {
						PointerBuffer clDevices = clStack.mallocPointer(pi2.get(0));
						if (CL12.clGetDeviceIDs(platform, CL12.CL_DEVICE_TYPE_ALL, clDevices, (IntBuffer)null)==CL12.CL_SUCCESS) {
							for (int d = 0; d < clDevices.capacity(); d++) {
								long device = clDevices.get(d);
								IntBuffer errcode_ret = clStack.callocInt(1);
								long context = CL12.clCreateContext(clCtxProps, device, (CLContextCallback)null, MemoryUtil.NULL, errcode_ret);
								int errcode_ret_int = errcode_ret.get(errcode_ret.position());
								if (errcode_ret_int==CL12.CL_SUCCESS) {
									Device devicedesc = new Device();
									devicedesc.platform = platform;
									devicedesc.context = context;
									devicedesc.queue = CL12.clCreateCommandQueue(context, device, CL12.CL_QUEUE_PROFILING_ENABLE, (IntBuffer)null);
									devicedesc.platformname = getClPlatformInfo(platform, CL12.CL_PLATFORM_NAME).trim();
									devicedesc.plaformcaps = CL.createPlatformCapabilities(platform);
									devicedesc.devicename = getClDeviceInfo(device, CL12.CL_DEVICE_NAME).trim();
									devicesinit.put(device, devicedesc);
								}
							}
						}
					}
				}
			}
		}
		MemoryStack.stackPop();
		return devicesinit;
	}

	private String getClPlatformInfo(long platform, int param) {
		String platforminfo = null;
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer pp = clStack.mallocPointer(1);
		if (CL12.clGetPlatformInfo(platform, param, (ByteBuffer)null, pp)==CL12.CL_SUCCESS) {
			int bytes = (int)pp.get(0);
			ByteBuffer buffer = clStack.malloc(bytes);
			if (CL12.clGetPlatformInfo(platform, param, buffer, null)==CL12.CL_SUCCESS) {
				platforminfo = MemoryUtil.memUTF8(buffer, bytes - 1);
			}
		}
		MemoryStack.stackPop();
		return platforminfo;
	}

	private String getClDeviceInfo(long device, int param) {
		String deviceinfo = null;
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer pp = clStack.mallocPointer(1);
		if (CL12.clGetDeviceInfo(device, param, (ByteBuffer)null, pp)==CL12.CL_SUCCESS) {
			int bytes = (int)pp.get(0);
			ByteBuffer buffer = clStack.malloc(bytes);
			if (CL12.clGetDeviceInfo(device, param, buffer, null)==CL12.CL_SUCCESS) {
				deviceinfo = MemoryUtil.memUTF8(buffer, bytes - 1);
			}
		}
		MemoryStack.stackPop();
		return deviceinfo;
	}

	private String getClProgramBuildInfo(long program, long device, int param) {
		String buildinfo = null;
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer pp = clStack.mallocPointer(1);
		if (CL12.clGetProgramBuildInfo(program, device, param, (ByteBuffer)null, pp)==CL12.CL_SUCCESS) {
			int bytes = (int)pp.get(0);
			ByteBuffer buffer = clStack.malloc(bytes);
			if (CL12.clGetProgramBuildInfo(program, device, param, buffer, pp)==CL12.CL_SUCCESS) {
				buildinfo = MemoryUtil.memUTF8(buffer, bytes - 1);
			}
		}
		MemoryStack.stackPop();
		return buildinfo;
	}
}
