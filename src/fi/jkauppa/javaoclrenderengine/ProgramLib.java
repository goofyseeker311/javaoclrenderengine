package fi.jkauppa.javaoclrenderengine;

public class ProgramLib {
	public static final String programSource = ""
			+ "kernel void range(global float *c) {"
				+ "unsigned int xid=get_global_id(0);"
				+ "c[xid]=((float)xid)+13.5f;"
			+ "}";
}
