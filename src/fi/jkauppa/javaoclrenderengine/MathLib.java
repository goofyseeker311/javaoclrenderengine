package fi.jkauppa.javaoclrenderengine;

import fi.jkauppa.javaoclrenderengine.ComputeLib.Device;

public class MathLib {
	private ComputeLib computelib = new ComputeLib();
	
	public MathLib() {
		int selecteddevice = 0;
		int buffersize = 10;
		float[] vbuffer = new float[buffersize];
		long device = this.computelib.devicelist[selecteddevice];
		Device devicedata = this.computelib.devicemap.get(device);
		System.out.println("Using device["+selecteddevice+"]: "+devicedata.devicename);
		long queue = devicedata.queue;
		long[] buffer = {this.computelib.createBuffer(device, queue, buffersize)};
		String programSource = "kernel void range(global float *c) { unsigned int xid=get_global_id(0); c[xid]=((float)xid)+13.5f; }";
		long program = this.computelib.compileProgram(device, programSource);
		this.computelib.runProgram(device, queue, program,"range", buffer, 0, buffersize);
		this.computelib.readBuffer(device, queue, buffer[0], vbuffer);
		System.out.print("vbuffer=");
		for (int i=0;i<vbuffer.length;i++) {
			System.out.print(" "+vbuffer[i]);
		}
		System.out.println();
	}
}
