float4 matrixposmult(const float4 pos, const float16 mat);
float16 matrixmatmult(const float16 vmat1, const float16 vmat2);
float16 rotationmatrix(float3 rot);
float16 scalingmatrix(float3 sca);
float16 rotationmatrixaroundaxis(float4 axis, float rot);
float vectorangle(float4 dir1, float4 dir2);
float4 planefromnormalatpos(float4 pos, float4 dir);
float rayplaneintersection(float4 pos, float4 dir, float4 plane);
float8 planetriangleintersection(float4 plane, float4 pos1, float4 pos2, float4 pos3);
float planepointdistance(float4 pos, float4 plane);

kernel void renderview(global int *img, global float *imz, global const float *cam, global const float *tri, global const int *trc, global const float *tex, global const int *tec, global const float *bvh, global const int *bvc) {
	unsigned int xid=get_global_id(0);
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float3 camrot = (float3)(cam[3],cam[4],cam[5]);
	float2 camfov = (float2)(cam[6],cam[7]);
	int2 camres = (int2)((int)cam[8],(int)cam[9]);

	const int collen = 2160;
	float4 camcol[collen] = {(float4)(0.0f,1.0f,1.0f,1.0f)};
	float camcolz[collen] = {INFINITY};

	int tricount = trc[0];
	int texcount = tec[0];
	int bvhcount = bvc[0];

	float3 camrotrad = radians(camrot);
	float2 camhalffovrad = radians(camfov/2.0f);
	float2 camhalffovlen = (float2)(tan(camhalffovrad.x), tan(camhalffovrad.y));
	int2 camhalfres = camres/2;
	float camcollen = -camhalffovlen.x + (camhalffovlen.x/(camhalfres.x-0.5f))*xid;

	float4 camdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float4 camrightdir = (float4)(0.0f,1.0f,0.0f,0.0f);
	float4 camupdir = (float4)(0.0f,0.0f,1.0f,0.0f);
	float4 coldir = normalize((float4)(1.0f,camcollen,0.0f,0.0f));
	float4 colupdir = normalize((float4)(1.0f,camcollen,camhalffovlen.y,0.0f));
	float4 coldowndir = normalize((float4)(1.0f,camcollen,-camhalffovlen.y,0.0f));
	float16 cammat = rotationmatrix(camrotrad);
	float4 camdirrot = matrixposmult(camdir, cammat);
	float4 camrightdirrot = matrixposmult(camrightdir, cammat);
	float4 camupdirrot = matrixposmult(camupdir, cammat);
	float4 coldirrot = matrixposmult(coldir, cammat);
	float4 colupdirrot = matrixposmult(colupdir, cammat);
	float4 coldowndirrot = matrixposmult(coldowndir, cammat);
	float colplanerayfov = vectorangle(colupdirrot, coldowndirrot);
	float4 colplanenorm = normalize(cross(coldowndirrot, colupdirrot));
	float4 colplane = planefromnormalatpos(campos, colplanenorm);
	float4 camdirplane = planefromnormalatpos(campos, camdirrot);
	float4 camrightdirplane = planefromnormalatpos(campos, camrightdirrot);
	float4 camupdirplane = planefromnormalatpos(campos, camupdirrot);

	for (int tid=0;tid<tricount;tid++) {
		float4 tripos1 = (float4)(tri[tid*13+0],tri[tid*13+1],tri[tid*13+2],0.0f);
		float4 tripos2 = (float4)(tri[tid*13+3],tri[tid*13+4],tri[tid*13+5],0.0f);
		float4 tripos3 = (float4)(tri[tid*13+6],tri[tid*13+7],tri[tid*13+8],0.0f);
		float4 tricolor = (float4)(tri[tid*13+9],tri[tid*13+10],tri[tid*13+11],tri[tid*13+12]);

		float8 intlines = planetriangleintersection(colplane, tripos1, tripos2, tripos3);
		float4 colpos1 = intlines.s0123;
		float4 colpos2 = intlines.s4567;

		if ((colpos1.x!=NAN)&&(colpos2.x!=NAN)) {
			float fwdintpointsdist1 = planepointdistance(colpos1, camdirplane);
			float fwdintpointsdist2 = planepointdistance(colpos2, camdirplane);
			float upintpointsdist1 = planepointdistance(colpos1, camupdirplane);
			float upintpointsdist2 = planepointdistance(colpos2, camupdirplane);

			if (fwdintpointsdist1>=0.1f) {
				int py1 = (camhalfres.y/camhalffovlen.y)*(upintpointsdist1/fwdintpointsdist1)+camhalffovlen.y;
				if (py1<0) {py1=0;}
				if (py1>=camres.y) {py1=camres.y-1;}
				if (fwdintpointsdist1<camcolz[py1]) {
					camcolz[py1] = fwdintpointsdist1;
					//camcol[py1] = tricolor;
				}
			}
			if (fwdintpointsdist2>=0.1f) {
				int py2 = (camhalfres.y/camhalffovlen.y)*(upintpointsdist2/fwdintpointsdist2)+camhalffovlen.y;
				if (py2<0) {py2=0;}
				if (py2>=camres.y) {py2=camres.y-1;}
				if (fwdintpointsdist2<camcolz[py2]) {
					camcolz[py2] = fwdintpointsdist2;
					//camcol[py2] = tricolor;
				}
			}
		}
	}
	
	for (int y=0;y<camres.y;y++) {
		float4 rgbapixel = camcol[y];
		uchar4 rgbacolor = (uchar4)(convert_uchar_sat(255*rgbapixel.a), convert_uchar_sat(255*rgbapixel.b), convert_uchar_sat(255*rgbapixel.g), convert_uchar_sat(255*rgbapixel.r));
		int rgbacolorint = as_int(rgbacolor);
		img[y*camres.x+xid] = rgbacolorint;
	}
}

float4 matrixposmult(const float4 pos, const float16 mat) {
	float4 retpos = (float4)(0.0f);
	retpos.x = mat.s0*pos.x + mat.s1*pos.y + mat.s2*pos.z + mat.s3*pos.w;
	retpos.y = mat.s4*pos.x + mat.s5*pos.y + mat.s6*pos.z + mat.s7*pos.w;
	retpos.z = mat.s8*pos.x + mat.s9*pos.y + mat.sA*pos.z + mat.sB*pos.w;
	retpos.w = mat.sC*pos.x + mat.sD*pos.y + mat.sE*pos.z + mat.sF*pos.w;
	return retpos;
}
float16 matrixmatmult(const float16 vmat1, const float16 vmat2) {
	float16 retmat = (float16)(0.0f);
	retmat.s0 = vmat1.s0*vmat2.s0 + vmat1.s1*vmat2.s4 + vmat1.s2*vmat2.s8 + vmat1.s3*vmat2.sC;
	retmat.s1 = vmat1.s0*vmat2.s1 + vmat1.s1*vmat2.s5 + vmat1.s2*vmat2.s9 + vmat1.s3*vmat2.sD;
	retmat.s2 = vmat1.s0*vmat2.s2 + vmat1.s1*vmat2.s6 + vmat1.s2*vmat2.sA + vmat1.s3*vmat2.sE;
	retmat.s3 = vmat1.s0*vmat2.s3 + vmat1.s1*vmat2.s7 + vmat1.s2*vmat2.sB + vmat1.s3*vmat2.sF;
	retmat.s4 = vmat1.s4*vmat2.s0 + vmat1.s5*vmat2.s4 + vmat1.s6*vmat2.s8 + vmat1.s7*vmat2.sC;
	retmat.s5 = vmat1.s4*vmat2.s1 + vmat1.s5*vmat2.s5 + vmat1.s6*vmat2.s9 + vmat1.s7*vmat2.sD;
	retmat.s6 = vmat1.s4*vmat2.s2 + vmat1.s5*vmat2.s6 + vmat1.s6*vmat2.sA + vmat1.s7*vmat2.sE;
	retmat.s7 = vmat1.s4*vmat2.s3 + vmat1.s5*vmat2.s7 + vmat1.s6*vmat2.sB + vmat1.s7*vmat2.sF;
	retmat.s8 = vmat1.s8*vmat2.s0 + vmat1.s9*vmat2.s4 + vmat1.sA*vmat2.s8 + vmat1.sB*vmat2.sC;
	retmat.s9 = vmat1.s8*vmat2.s1 + vmat1.s9*vmat2.s5 + vmat1.sA*vmat2.s9 + vmat1.sB*vmat2.sD;
	retmat.sA = vmat1.s8*vmat2.s2 + vmat1.s9*vmat2.s6 + vmat1.sA*vmat2.sA + vmat1.sB*vmat2.sE;
	retmat.sB = vmat1.s8*vmat2.s3 + vmat1.s9*vmat2.s7 + vmat1.sA*vmat2.sB + vmat1.sB*vmat2.sF;
	retmat.sC = vmat1.sC*vmat2.s0 + vmat1.sD*vmat2.s4 + vmat1.sE*vmat2.s8 + vmat1.sF*vmat2.sC;
	retmat.sD = vmat1.sC*vmat2.s1 + vmat1.sD*vmat2.s5 + vmat1.sE*vmat2.s9 + vmat1.sF*vmat2.sD;
	retmat.sE = vmat1.sC*vmat2.s2 + vmat1.sD*vmat2.s6 + vmat1.sE*vmat2.sA + vmat1.sF*vmat2.sE;
	retmat.sF = vmat1.sC*vmat2.s3 + vmat1.sD*vmat2.s7 + vmat1.sE*vmat2.sB + vmat1.sF*vmat2.sF;
	return retmat;
}

float16 rotationmatrix(float3 rot) {
	float16 xrot = (float16)(1,0,0,0,0,cos(rot.x),-sin(rot.x),0,0,sin(rot.x),cos(rot.x),0,0,0,0,1);
	float16 yrot = (float16)(cos(rot.y),0,sin(rot.y),0,0,1,0,0,-sin(rot.y),0,cos(rot.y),0,0,0,0,1);
	float16 zrot = (float16)(cos(rot.z),-sin(rot.z),0,0,sin(rot.z),cos(rot.z),0,0,0,0,1,0,0,0,0,1);
	float16 retmat = matrixmatmult(zrot,matrixmatmult(yrot, xrot));
	return retmat;
}
float16 scalingmatrix(float3 sca) {
	float16 retmat = (float16)(sca.x,0,0,0,0,sca.y,0,0,0,0,sca.z,0,0,0,0,1);
	return retmat;
}
float16 rotationmatrixaroundaxis(float4 axis, float rot) {
	float4 axisn = normalize(axis);
	float cosval = cos(rot);
	float sinval = sin(rot);
	float16 retmat = (float16)(cosval+axisn.x*axisn.x*(1-cosval),axisn.x*axisn.y*(1-cosval)-axisn.z*sinval,axisn.x*axisn.z*(1-cosval)+axisn.y*sinval,0,
			axisn.y*axisn.x*(1-cosval)+axisn.z*sinval,cosval+axisn.y*axisn.y*(1-cosval),axisn.y*axisn.z*(1-cosval)-axisn.x*sinval,0,
			axisn.z*axisn.x*(1-cosval)-axisn.y*sinval,axisn.z*axisn.y*(1-cosval)+axisn.x*sinval,cosval+axisn.z*axisn.z*(1-cosval),0,
			0,0,0,1);
	return retmat;
}

float vectorangle(float4 dir1, float4 dir2) {
	float retang = acos(dot(dir1,dir2))/(length(dir1)*length(dir2));
	return retang;
}

float4 planefromnormalatpos(float4 pos, float4 dir) {
	float4 retpla = normalize(dir);
	retpla.w = -dot(pos,retpla);
	return retpla;
}
float rayplaneintersection(float4 pos, float4 dir, float4 plane) {
	float retdist = -(plane.x*pos.x+plane.y*pos.y+plane.z*pos.z+plane.w)/(plane.x*dir.x+plane.y*dir.y+plane.z*dir.z+plane.w);
	return retdist;
}
float8 planetriangleintersection(float4 plane, float4 pos1, float4 pos2, float4 pos3) {
	float8 retline = (float8)(NAN);
	float4 vtri12 = pos2-pos1;
	float4 vtri13 = pos3-pos1;
	float4 vtri23 = pos3-pos2;
	float ptd12 = rayplaneintersection(pos1, vtri12,  plane);
	float ptd13 = rayplaneintersection(pos1, vtri13,  plane);
	float ptd23 = rayplaneintersection(pos2, vtri23,  plane);
	bool ptlhit12 = (ptd12>=0)&&(ptd12<=1);
	bool ptlhit13 = (ptd13>=0)&&(ptd13<=1);
	bool ptlhit23 = (ptd23>=0)&&(ptd23<=1);
	if (ptlhit12|ptlhit13|ptlhit23) {
		float4 ptlint12 = (float4)(pos1.x+ptd12*vtri12.x,pos1.y+ptd12*vtri12.y,pos1.z+ptd12*vtri12.z,0.0f);
		float4 ptlint13 = (float4)(pos1.x+ptd13*vtri13.x,pos1.y+ptd13*vtri13.y,pos1.z+ptd13*vtri13.z,0.0f);
		float4 ptlint23 = (float4)(pos2.x+ptd23*vtri23.x,pos2.y+ptd23*vtri23.y,pos2.z+ptd23*vtri23.z,0.0f);
		if (ptlhit12&&ptlhit13) {
			retline.s0123 = ptlint12;
			retline.s4567 = ptlint13;
		} else if (ptlhit12&&ptlhit23) {
			retline.s0123 = ptlint12;
			retline.s4567 = ptlint23;
		} else if (ptlhit13&&ptlhit23) {
			retline.s0123 = ptlint13;
			retline.s4567 = ptlint23;
		}
	}
	return retline;
}
float planepointdistance(float4 pos, float4 plane) {
	float4 planedir = plane;
	planedir.w = 0.0f;
	float planedirlen = length(planedir);
	float retdist = (plane.x*pos.x+plane.y*pos.y+plane.z*pos.z+plane.w)/planedirlen;
	return retdist;
}
