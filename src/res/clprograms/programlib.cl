kernel void renderview(global int *img, global const float *cam, global const float *tri) {
	unsigned int tid=get_global_id(0);
	float3 campos = (float3)(cam[0],cam[1],cam[2]);
	float3 camdir = (float3)(cam[3],cam[4],cam[5]);
	float camdrot = cam[6];
	float2 camfov = (float2)(cam[7],cam[8]);
	int2 camres = (int2)((int)cam[9],(int)cam[10]);
	//printf("width=%i height=%i\n",camres.x,camres.y);
	
	float3 tripoint1 = (float3)(tri[tid*13+0],tri[tid*13+1],tri[tid*13+2]);
	float3 tripoint2 = (float3)(tri[tid*13+3],tri[tid*13+4],tri[tid*13+5]);
	float3 tripoint3 = (float3)(tri[tid*13+6],tri[tid*13+7],tri[tid*13+8]);
	float4 tricolor = (float4)(tri[tid*13+9],tri[tid*13+10],tri[tid*13+11],tri[tid*13+12]);
	
	float4 rgbapixel = (float4)(tricolor.r,tricolor.g,tricolor.b,tricolor.a);
	uchar4 rgbacolor = (uchar4)(convert_uchar_sat(255*rgbapixel.r), convert_uchar_sat(255*rgbapixel.g), convert_uchar_sat(255*rgbapixel.b), convert_uchar_sat(255*rgbapixel.a));
	int rgbacolorint = as_int(rgbacolor);
	
	for (int y=0;y<camres.y;y++) {
		for (int x=0;x<camres.x;x++) {
			img[y*camres.x+x] = rgbacolorint;
		}
	}
}
