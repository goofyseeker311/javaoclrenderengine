package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.TreeMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ComputeLib {
	public TreeMap<Long,Device> devicemap = initClDevices();
	public Long[] devicelist = devicemap.keySet().toArray(new Long[devicemap.size()]);
	
	public ComputeLib() {
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
	}
	public void writeBufferi(long device, long queue, long vmem, int[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueWriteBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
	}

	public void readBufferf(long device, long queue, long vmem, float[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueReadBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
	}
	public void readBufferi(long device, long queue, long vmem, int[] v) {
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueReadBuffer(queue, vmem, true, 0, v, null, event);
		CL12.clWaitForEvents(event);
	}
	
	public long createQueue(long device) {
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		return CL12.clCreateCommandQueue(context, device, CL12.CL_QUEUE_PROFILING_ENABLE, (IntBuffer)null);
	}
	
	public long createBuffer(long device, int size) {
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		return CL12.clCreateBuffer(context, CL12.CL_MEM_READ_WRITE, size*4, (IntBuffer)null);
	}
	public void removeBuffer(long vmem) {
		CL12.clReleaseMemObject(vmem);
	}
	public void fillBufferf(long vmem, long queue, float fill, int size) {
		MemoryStack clStack = MemoryStack.stackPush();
		ByteBuffer pattern = clStack.malloc(4);
		pattern.putFloat(fill);
		pattern.rewind();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueFillBuffer(queue, vmem, pattern, 0, size*4, null, event);
		CL12.clWaitForEvents(event);
	}
	public void fillBufferi(long vmem, long queue, int fill, int size) {
		MemoryStack clStack = MemoryStack.stackPush();
		ByteBuffer pattern = clStack.malloc(4);
		pattern.putInt(fill);
		pattern.rewind();
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueFillBuffer(queue, vmem, pattern, 0, size*4, null, event);
		CL12.clWaitForEvents(event);
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
		long program = NULL;
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		program = CL12.clCreateProgramWithSource(context, source, (IntBuffer)null);
		CL12.clBuildProgram(program, device, "", null, NULL);
		return program;
	}
	public float runProgram(long device, long queue, long program, String entry, long[] fmem, int offset, int size, boolean waitgetruntime) {
		float runtime = 0.0f;
		MemoryStack clStack = MemoryStack.stackPush();
		long kernel = CL12.clCreateKernel(program, entry, (IntBuffer)null);
		for (int i=0;i<fmem.length;i++) {
			CL12.clSetKernelArg1p(kernel, i, fmem[i]);
		}
		int dimensions = 1;
		PointerBuffer globalWorkOffset = BufferUtils.createPointerBuffer(dimensions);
		globalWorkOffset.put(0, offset);
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
		globalWorkSize.put(0, size);
		PointerBuffer event = clStack.mallocPointer(1);
		CL12.clEnqueueNDRangeKernel(queue, kernel, dimensions, globalWorkOffset, globalWorkSize, null, null, event);
		if (waitgetruntime) {
			CL12.clWaitForEvents(event);
			long eventLong = event.get(0);
			long[] ctimestart = {0};
			long[] ctimeend = {0};
			CL12.clGetEventProfilingInfo(eventLong, CL12.CL_PROFILING_COMMAND_START, ctimestart, (PointerBuffer)null);
			CL12.clGetEventProfilingInfo(eventLong, CL12.CL_PROFILING_COMMAND_END, ctimeend, (PointerBuffer)null);
			runtime = (ctimeend[0]-ctimestart[0])/1000000.0f;
		}
		return runtime;
	}
	
	public static class Device {
		public long platform = NULL;
		public long context = NULL;
		public long queue = NULL;
		public String devicename = null;
		public String platformname = null;
	}

	private TreeMap<Long,Device> initClDevices() {
		TreeMap<Long,Device> devicesinit = new TreeMap<Long,Device>();
		MemoryStack clStack = MemoryStack.stackPush();
		PointerBuffer clPlatforms = getClPlatforms();
		if (clPlatforms!=null) {
			PointerBuffer clCtxProps = clStack.mallocPointer(3);
			clCtxProps.put(0, CL12.CL_CONTEXT_PLATFORM).put(2, 0);
			for (int p = 0; p < clPlatforms.capacity(); p++) {
				long platform = clPlatforms.get(p);
				clCtxProps.put(1, platform);
				PointerBuffer clDevices = getClDevices(platform);
				for (int d = 0; d < clDevices.capacity(); d++) {
					long device = clDevices.get(d);
					IntBuffer errcode_ret = clStack.callocInt(1);
					long context = CL12.clCreateContext(clCtxProps, device, (CLContextCallback)null, NULL, errcode_ret);
					if (errcode_ret.get(errcode_ret.position())==CL12.CL_SUCCESS) {
						Device devicedesc = new Device();
						devicedesc.platform = platform;
						devicedesc.context = context;
						devicedesc.queue = CL12.clCreateCommandQueue(context, device, CL12.CL_QUEUE_PROFILING_ENABLE, (IntBuffer)null);;
						devicedesc.platformname = getClPlatformInfo(platform, CL12.CL_PLATFORM_NAME).trim();
						devicedesc.devicename = getClDeviceInfo(device, CL12.CL_DEVICE_NAME).trim();
						devicesinit.put(device, devicedesc);
					}
				}
			}
		}
		return devicesinit;
	}

	private PointerBuffer getClPlatforms() {
		PointerBuffer platforms = null;
		MemoryStack clStack = MemoryStack.stackPush();
		IntBuffer pi = clStack.mallocInt(1);
		if (CL12.clGetPlatformIDs(null, pi)==CL12.CL_SUCCESS) {
			PointerBuffer clPlatforms = clStack.mallocPointer(pi.get(0));
			if (CL12.clGetPlatformIDs(clPlatforms, (IntBuffer)null)==CL12.CL_SUCCESS) {
				platforms = clPlatforms;
			}
		}
		return platforms;
	}

	private PointerBuffer getClDevices(long platform) {
		PointerBuffer devices = null;
		MemoryStack clStack = MemoryStack.stackPush();
		IntBuffer pi = clStack.mallocInt(1);
		if (CL12.clGetDeviceIDs(platform, CL12.CL_DEVICE_TYPE_ALL, null, pi)==CL12.CL_SUCCESS) {
			PointerBuffer pp = clStack.mallocPointer(pi.get(0));
			if (CL12.clGetDeviceIDs(platform, CL12.CL_DEVICE_TYPE_ALL, pp, (IntBuffer)null)==CL12.CL_SUCCESS) {
				devices = pp;
			}
		}
		return devices;
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
		return deviceinfo;
	}
}
