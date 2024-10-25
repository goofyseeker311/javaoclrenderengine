kernel void renderview(global int *c) {
	unsigned int xid=get_global_id(0);
	//c[xid] = 0x800000ff;
	float4 rgbapixel = (float4)(1.0f,0.0f,0.0f,0.5f);
	uchar4 rgbacolor = (uchar4)(255*rgbapixel.r, 255*rgbapixel.g, 255*rgbapixel.b, 255*rgbapixel.a);
	c[xid] = as_int(rgbacolor);
}
