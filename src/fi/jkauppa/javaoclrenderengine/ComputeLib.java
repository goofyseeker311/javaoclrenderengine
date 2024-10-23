package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.TreeMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ComputeLib {
	private MemoryStack clStack = MemoryStack.stackPush();
	public TreeMap<Long,Device> devicemap = initClDevices();
	public Long[] devicelist = devicemap.keySet().toArray(new Long[devicemap.size()]);
	
	public ComputeLib() {
		for (int i=0;i<devicelist.length;i++) {
			long device = devicelist[i];
			Device devicedata = devicemap.get(device);
			System.out.println("OpenCL device["+i+"]: "+devicedata.devicename);
		}
	}

	public long writeBuffer(long device, long queue, float[] v) {
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		long vmem = CL12.clCreateBuffer(context, CL12.CL_MEM_COPY_HOST_PTR | CL12.CL_MEM_READ_WRITE, v, null);
		CL12.clEnqueueWriteBuffer(queue, vmem, true, 0, v, null, null);
		CL12.clFinish(queue);
		return vmem;
	}
	
	public void readBuffer(long device, long queue, long vmem, float[] v) {
		FloatBuffer resultBuff = BufferUtils.createFloatBuffer(v.length);
		CL12.clEnqueueReadBuffer(queue, vmem, true, 0, resultBuff, null, null);
		Arrays.fill(v, 0.0f);
		CL12.clFinish(queue);
		resultBuff.rewind();
		resultBuff.get(0, v);
	}
	
	public long createBuffer(long device, long queue, int size) {
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		long vmem = CL12.clCreateBuffer(context, CL12.CL_MEM_READ_WRITE, size*4, (IntBuffer)null);
		ByteBuffer pattern = clStack.malloc(4);
		pattern.putFloat(1.0f);
		CL12.clEnqueueFillBuffer(queue, vmem, pattern, 0, size, null, null);
		CL12.clFinish(queue);
		return vmem;
	}

	public void removeBuffer(long vmem) {
		CL12.clReleaseMemObject(vmem);
	}
	
	public void runProgram(long device, long queue, long program, String entry, long[] vmem, int offset, int size) {
		long kernel = CL12.clCreateKernel(program, entry, (IntBuffer)null);
		for (int i=0;i<vmem.length;i++) {
			CL12.clSetKernelArg1p(kernel, i, vmem[i]);
		}
		int dimensions = 1;
		PointerBuffer globalWorkOffset = BufferUtils.createPointerBuffer(dimensions);
		globalWorkOffset.put(0, offset);
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
		globalWorkSize.put(0, size);
		CL12.clEnqueueNDRangeKernel(queue, kernel, dimensions, globalWorkOffset, globalWorkSize, null, null, null);
	}
	
	public long compileProgram(long device, String source) {
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		long program = CL12.clCreateProgramWithSource(context, source, (IntBuffer)null);
		CL12.clBuildProgram(program, device, "", null, NULL);
		return program;
	}
	
	public long createQueue(long device) {
		Device devicedata = devicemap.get(device);
		long context = devicedata.context;
		long queue = CL12.clCreateCommandQueue(context, device, CL12.CL_QUEUE_PROFILING_ENABLE, (IntBuffer)null);
		return queue;
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
						devicedesc.platformname = getClPlatformInfo(platform, CL12.CL_PLATFORM_NAME);
						devicedesc.devicename = getClDeviceInfo(device, CL12.CL_DEVICE_NAME);
						devicesinit.put(device, devicedesc);
					}
				}
			}
		}
		return devicesinit;
	}

	private PointerBuffer getClPlatforms() {
		PointerBuffer platforms = null;
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
