package fi.jkauppa.javaoclrenderengine;

import static org.lwjgl.system.MemoryUtil.NULL;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.lwjgl.BufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLContextCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

public class ComputeLib {
	private MemoryStack clStack = MemoryStack.stackPush();
	private TreeMap<Long,Long> devicecontexts = initClDevices();
	private Set<Long> devices = devicecontexts.keySet();
	public Long[] devicelist = devices.toArray(new Long[devices.size()]);
	
	public ComputeLib() {
		int n = 0;
		for (Iterator<Long> i=devices.iterator();i.hasNext();n++) {
			Long device = i.next();
			String devicename = getClDeviceInfo(device, CL12.CL_DEVICE_NAME);
			System.out.println("OpenCL device["+n+"]: "+devicename);
		}
	}

	public long writeBuffer(long device, long queue, float[] v) {
		long context = devicecontexts.get(device);
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
		long context = devicecontexts.get(device);
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
	
	public void runProgram(long device, long queue, long program, String entry, long amem, long bmem, long cmem, int size) {
		long kernel = CL12.clCreateKernel(program, entry, (IntBuffer)null);
		CL12.clSetKernelArg1p(kernel, 0, amem);
		CL12.clSetKernelArg1p(kernel, 1, bmem);
		CL12.clSetKernelArg1p(kernel, 2, cmem);
		int dimensions = 1;
		PointerBuffer globalWorkSize = BufferUtils.createPointerBuffer(dimensions);
		globalWorkSize.put(0, size);
		CL12.clEnqueueNDRangeKernel(queue, kernel, dimensions, null, globalWorkSize, null, null, null);
	}
	
	public long compileProgram(long device, String source) {
		long context = devicecontexts.get(device);
		long program = CL12.clCreateProgramWithSource(context, source, (IntBuffer)null);
		CL12.clBuildProgram(program, device, "", null, NULL);
		return program;
	}
	
	public long createQueue(long device) {
		long context = devicecontexts.get(device);
		long queue = CL12.clCreateCommandQueue(context, device, CL12.CL_QUEUE_PROFILING_ENABLE, (IntBuffer)null);
		return queue;
	}

	private TreeMap<Long,Long> initClDevices() {
		TreeMap<Long,Long> devices = new TreeMap<Long,Long>();
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
						devices.put(device, context);
					}
				}
			}
		}
		return devices;
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

	private String getClDeviceInfo(long cl_device_id, int param_name) {
		String deviceinfo = null;
		PointerBuffer pp = clStack.mallocPointer(1);
		if (CL12.clGetDeviceInfo(cl_device_id, param_name, (ByteBuffer)null, pp)==CL12.CL_SUCCESS) {
			int bytes = (int)pp.get(0);
			ByteBuffer buffer = clStack.malloc(bytes);
			if (CL12.clGetDeviceInfo(cl_device_id, param_name, buffer, null)==CL12.CL_SUCCESS) {
				deviceinfo = MemoryUtil.memUTF8(buffer, bytes - 1);
			}
		}
		return deviceinfo;
	}
}
