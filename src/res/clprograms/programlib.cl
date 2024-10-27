kernel void renderview(global int *img, global const float *cam) {
	unsigned int xid=get_global_id(0);
	float3 campos = (float3)(cam[0],cam[1],cam[2]);
	float3 camdir = (float3)(cam[3],cam[4],cam[5]);
	//img[xid] = 0x800000ff;
	//float4 rgbapixel = (float4)(1.0f,0.0f,0.0f,0.5f);
	float4 rgbapixel = (float4)(campos.x,campos.y,campos.z,camdir.x);
	uchar4 rgbacolor = (uchar4)(255*rgbapixel.r, 255*rgbapixel.g, 255*rgbapixel.b, 255*rgbapixel.a);
	img[xid] = as_int(rgbacolor);
}
