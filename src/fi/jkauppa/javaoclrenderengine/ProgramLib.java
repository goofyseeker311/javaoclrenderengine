package fi.jkauppa.javaoclrenderengine;

public class ProgramLib {
	public static final String programSource = ""
			+ "kernel void renderrgbapixels(global float *c) {"
				+ "unsigned int xid=get_global_id(0);"
				+ "c[xid*4+0]=0.006f;"
				+ "c[xid*4+1]=1.0f;"
				+ "c[xid*4+2]=0.0f;"
				+ "c[xid*4+3]=0.0f;"
			+ "}";
}
