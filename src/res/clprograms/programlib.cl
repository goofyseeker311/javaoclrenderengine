float4 matrixmult(const float4 pos, const float16 mat);

kernel void renderview(global int *img, global const float *cam, global const float *tri, global const int *trc) {
	unsigned int xid=get_global_id(0);
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float3 camrot = (float3)(cam[3],cam[4],cam[5]);
	float2 camfov = (float2)(cam[6],cam[7]);
	int2 camres = (int2)((int)cam[8],(int)cam[9]);
	
	float4 camcol[2160] = {(float4)(0.0f,0.0f,0.0f,0.0f)};

	float4 camdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float16 cammat = (float16)(1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f);
	float4 camposnew = matrixmult(campos, cammat);
	
	for (int tid=0;tid<trc[0];tid++) {
		float4 tripoint1 = (float4)(tri[tid*13+0],tri[tid*13+1],tri[tid*13+2],0.0f);
		float4 tripoint2 = (float4)(tri[tid*13+3],tri[tid*13+4],tri[tid*13+5],0.0f);
		float4 tripoint3 = (float4)(tri[tid*13+6],tri[tid*13+7],tri[tid*13+8],0.0f);
		float4 tricolor = (float4)(tri[tid*13+9],tri[tid*13+10],tri[tid*13+11],tri[tid*13+12]);
		
		for (int y=0;y<camres.y;y++) {
			camcol[y] = tricolor;
		}
	}

	for (int y=0;y<camres.y;y++) {
		float4 rgbapixel = camcol[y];
		uchar4 rgbacolor = (uchar4)(convert_uchar_sat(255*rgbapixel.a), convert_uchar_sat(255*rgbapixel.b), convert_uchar_sat(255*rgbapixel.g), convert_uchar_sat(255*rgbapixel.r));
		int rgbacolorint = as_int(rgbacolor);
		img[y*camres.x+xid] = rgbacolorint;
	}
}

float4 matrixmult(const float4 pos, const float16 mat) {
	float4 retpos = (float4)(0.0f,0.0f,0.0f,0.0f);
	retpos.x = mat.s0*pos.x + mat.s1*pos.y + mat.s2*pos.z + mat.s3*pos.w;
	retpos.y = mat.s4*pos.x + mat.s5*pos.y + mat.s6*pos.z + mat.s7*pos.w;
	retpos.z = mat.s8*pos.x + mat.s9*pos.y + mat.sA*pos.z + mat.sB*pos.w;
	retpos.w = mat.sC*pos.x + mat.sD*pos.y + mat.sE*pos.z + mat.sF*pos.w;
	return retpos;
}
