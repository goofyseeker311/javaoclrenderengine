float4 matrixposmult(const float4 pos, const float16 mat);
float16 matrixmatmult(const float16 vmat1, const float16 vmat2);
float16 scalingmatrix(float3 sca);
float16 rotationmatrix(float3 rot);
float16 rotationmatrixaroundaxis(float4 axis, float rot);
float16 rotationmatrixlookhorizontalroll(float3 rot);
float16 rotationmatrixlookdir(float4 lookat, float rollaxis);
float vectorangle(float4 dir1, float4 dir2);
float4 planefromnormalatpos(float4 pos, float4 dir);
float rayplanedistance(float4 pos, float4 dir, float4 plane);
float planepointdistance(float4 pos, float4 plane);
float4 translatepos(float4 point, float4 dir, float mult);
float linearanglelengthinterpolation(float4 vpos, float8 vline, float vangle);
float4 triangleplane(float16 vtri);
float16 planetriangleintersection(float4 plane, float16 vtri);
float8 raytriangleintersection(float4 vpos, float4 vdir, float16 vtri);
kernel void movecamera(global float *cam, global const float *cmv);
kernel void clearview(global int *img, global float *imz, global int *imh, global const float *cam);
kernel void rendercross(global int *img, global float *imz, global int *imh, global const float *cam);
kernel void renderplaneview(global int *img, global float *imz, global int *imh, global const float *cam, global const float *tri, global const int *tex, global const float *obj);
kernel void renderrayview(global int *img, global float *imz, global int *imh , global const float *cam, global const float *tri, global const int *tex, global const float *obj);

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

float16 scalingmatrix(float3 sca) {
	float16 retmat = (float16)(sca.x,0,0,0,0,sca.y,0,0,0,0,sca.z,0,0,0,0,1);
	return retmat;
}
float16 rotationmatrix(float3 rot) {
	float16 xrot = (float16)(1,0,0,0,0,cos(rot.x),-sin(rot.x),0,0,sin(rot.x),cos(rot.x),0,0,0,0,1);
	float16 yrot = (float16)(cos(rot.y),0,sin(rot.y),0,0,1,0,0,-sin(rot.y),0,cos(rot.y),0,0,0,0,1);
	float16 zrot = (float16)(cos(rot.z),-sin(rot.z),0,0,sin(rot.z),cos(rot.z),0,0,0,0,1,0,0,0,0,1);
	float16 retmat = matrixmatmult(zrot,matrixmatmult(yrot, xrot));
	return retmat;
}
float16 rotationmatrixaroundaxis(float4 axis, float rot) {
	float4 axisn = normalize(axis);
	float cosval = cos(rot);
	float sinval = sin(rot);
	float16 retmat = 
			(float16)(cosval+axisn.x*axisn.x*(1-cosval),axisn.x*axisn.y*(1-cosval)-axisn.z*sinval,axisn.x*axisn.z*(1-cosval)+axisn.y*sinval,0,
			axisn.y*axisn.x*(1-cosval)+axisn.z*sinval,cosval+axisn.y*axisn.y*(1-cosval),axisn.y*axisn.z*(1-cosval)-axisn.x*sinval,0,
			axisn.z*axisn.x*(1-cosval)-axisn.y*sinval,axisn.z*axisn.y*(1-cosval)+axisn.x*sinval,cosval+axisn.z*axisn.z*(1-cosval),0,
			0,0,0,1);
	return retmat;
}

float vectorangle(float4 dir1, float4 dir2) {
	float retangle = acos(dot(dir1,dir2)/(length(dir1)*length(dir2)));
	return retangle;
}

float4 planefromnormalatpos(float4 pos, float4 dir) {
	float4 retplane = normalize(dir);
	retplane.w = -(pos.x*retplane.x + pos.y*retplane.y + pos.z*retplane.z);
	return retplane;
}
float rayplanedistance(float4 pos, float4 dir, float4 plane) {
	float top = plane.x*pos.x + plane.y*pos.y + plane.z*pos.z + plane.w;
	float bottom = plane.x*dir.x + plane.y*dir.y + plane.z*dir.z;
	float retdist = -top/bottom;
	return retdist;
}
float planepointdistance(float4 pos, float4 plane) {
	float4 planedir = plane;
	planedir.w = 0.0f;
	float planedirlen = length(planedir);
	float retdist = (plane.x*pos.x+plane.y*pos.y+plane.z*pos.z+plane.w)/planedirlen;
	return retdist;
}

float4 translatepos(float4 pos, float4 dir, float mult) {
	float4 retpos = (float4)(0.0f,0.0f,0.0f,0.0f);
	retpos.x = pos.x+mult*dir.x;
	retpos.y = pos.y+mult*dir.y;
	retpos.z = pos.z+mult*dir.z;
	retpos.w = pos.w+mult*dir.w;
	return retpos;
}

float linearanglelengthinterpolation(float4 vpos, float8 vline, float vposangle) {
	float retlenfrac = 0.0f;
	float4 startpos = vline.s0123;
	float4 endpos = vline.s4567;
	float4 vposstartdir = startpos - vpos;
	float4 startvposdir = -vposstartdir;
	float4 startenddir = endpos - startpos;
	float vposstartdirlen = length(vposstartdir);
	float startenddirlen = length(startenddir);
	float startposangle = vectorangle(startvposdir, startenddir);
	float endposangle = M_PI_F-startposangle-vposangle;
	float startendangledirlen = vposstartdirlen*(sin(vposangle)/sin(endposangle));
	float startendangledirlenfrac = startendangledirlen/startenddirlen;
	retlenfrac = startendangledirlenfrac;
	return retlenfrac;
}

float4 triangleplane(float16 vtri) {
	float4 plane = (float4)(0.0f,0.0f,0.0f,0.0f);
	float4 p1 = (float4)(vtri.s012,0.0f);
	float4 p2 = (float4)(vtri.s345,0.0f);
	float4 p3 = (float4)(vtri.s678,0.0f);
	float4 v1 = p2 - p1;
	float4 v2 = p3 - p1;
	float4 nm = normalize(cross(v1, v2));
	plane = planefromnormalatpos(p1, nm);
	return plane;
}

float16 planetriangleintersection(float4 plane, float16 vtri) {
	float16 retline = (float16)(NAN);
	float4 pos1 = (float4)(vtri.s012,0.0f);
	float4 pos2 = (float4)(vtri.s345,0.0f);
	float4 pos3 = (float4)(vtri.s678,0.0f);
	float4 pos1uv = (float4)(vtri.sAB,0.0f,0.0f);
	float4 pos2uv = (float4)(vtri.sCD,0.0f,0.0f);
	float4 pos3uv = (float4)(vtri.sEF,0.0f,0.0f);
	float4 vtri12 = pos2-pos1;
	float4 vtri13 = pos3-pos1;
	float4 vtri23 = pos3-pos2;
	float ptd12 = rayplanedistance(pos1, vtri12,  plane);
	float ptd13 = rayplanedistance(pos1, vtri13,  plane);
	float ptd23 = rayplanedistance(pos2, vtri23,  plane);
	bool ptlhit12 = (ptd12>=0)&&(ptd12<=1);
	bool ptlhit13 = (ptd13>=0)&&(ptd13<=1);
	bool ptlhit23 = (ptd23>=0)&&(ptd23<=1);
	if (ptlhit12|ptlhit13|ptlhit23) {
		float4 ptlint12 = translatepos(pos1, vtri12, ptd12);
		float4 ptlint13 = translatepos(pos1, vtri13, ptd13);
		float4 ptlint23 = translatepos(pos2, vtri23, ptd23);
		float4 uvdelta12 = pos2uv-pos1uv;
		float4 uvdelta13 = pos3uv-pos1uv;
		float4 uvdelta23 = pos3uv-pos2uv;
		float4 ptlint12uv = translatepos(pos1uv, uvdelta12, ptd12);
		float4 ptlint13uv = translatepos(pos1uv, uvdelta13, ptd13);
		float4 ptlint23uv = translatepos(pos2uv, uvdelta23, ptd23);
		if (ptlhit12&&ptlhit13) {
			retline.s01234567.s0123 = ptlint12;
			retline.s01234567.s4567 = ptlint13;
			retline.s89abcdef.s0123 = ptlint12uv;
			retline.s89abcdef.s4567 = ptlint13uv;
		} else if (ptlhit12&&ptlhit23) {
			retline.s01234567.s0123 = ptlint12;
			retline.s01234567.s4567 = ptlint23;
			retline.s89abcdef.s0123 = ptlint12uv;
			retline.s89abcdef.s4567 = ptlint23uv;
		} else if (ptlhit13&&ptlhit23) {
			retline.s01234567.s0123 = ptlint13;
			retline.s01234567.s4567 = ptlint23;
			retline.s89abcdef.s0123 = ptlint13uv;
			retline.s89abcdef.s4567 = ptlint23uv;
		}
	}
	return retline;
}

float8 raytriangleintersection(float4 vpos, float4 vdir, float16 vtri) {
	float8 intpos = (float8)(NAN);
	float4 tplane = triangleplane(vtri);
	float tpdist = rayplanedistance(vpos, vdir, tplane);
	float4 p4 = translatepos(vpos, vdir, tpdist);
	float4 p1 = (float4)(vtri.s012,0.0f);
	float4 p2 = (float4)(vtri.s345,0.0f);
	float4 p3 = (float4)(vtri.s678,0.0f);
	float4 p1uv = (float4)(vtri.sAB,0.0f,0.0f);
	float4 p2uv = (float4)(vtri.sCD,0.0f,0.0f);
	float4 p3uv = (float4)(vtri.sEF,0.0f,0.0f);
	float4 p4uv = (float4)(NAN);
	float4 v12 = p2 - p1; float4 v21 = -v12;
	float4 v13 = p3 - p1; float4 v31 = -v13;
	float4 v23 = p3 - p2; float4 v32 = -v23;
	float vl12 = length(v12);
	float vl13 = length(v13);
	float a1 = vectorangle(v12,v13);
	float a2 = vectorangle(v21,v23);
	float a3 = vectorangle(v31,v32);
	float ai1 = vectorangle(v21,v13);
	float4 t1 = p4 - p1;
	float4 t2 = p4 - p2;
	float4 t3 = p4 - p3;
	float tl1 = length(t1);
	float h12 = vectorangle(v12,t1); float h13 = vectorangle(v13,t1);
	float h21 = vectorangle(v21,t2); float h23 = vectorangle(v23,t2);
	float h31 = vectorangle(v31,t3); float h32 = vectorangle(v32,t3);
	bool isatpoint1 = (t1.x==0)&&(t1.y==0)&&(t1.z==0);
	bool isatpoint2 = (t2.x==0)&&(t2.y==0)&&(t2.z==0);
	bool isatpoint3 = (t3.x==0)&&(t3.y==0)&&(t3.z==0);
	bool withinangles = (h12<=a1)&&(h13<=a1)&&(h21<=a2)&&(h23<=a2)&&(h31<=a3)&&(h32<=a3);
	if(isatpoint1||isatpoint2||isatpoint3||withinangles) {
		if (isatpoint1) {
			p4uv = p1uv;
		} else if (isatpoint2) {
			p4uv = p2uv;
		} else if (isatpoint3) {
			p4uv = p3uv;
		} else {
			float n12len = tl1*(sin(h13)/sin(ai1));
			float n13len = tl1*(sin(h12)/sin(ai1));
			float n12mult = n12len/vl12;
			float n13mult = n13len/vl13;
			float4 uv12delta = p2uv-p1uv;
			float4 uv13delta = p3uv-p1uv;
			p4uv = p1uv;
			p4uv = translatepos(p4uv, uv12delta, n12mult);
			p4uv = translatepos(p4uv, uv13delta, n13mult);
		}
		intpos.s0123 = p4;
		intpos.s4567 = p4uv;
	}
	return intpos;
}

kernel void movecamera(global float *cam, global const float *cmv) {
	unsigned int xid=get_global_id(0);
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = (float2)(cam[3],cam[4]);
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	float16 cammat = (float16)(cam[7],cam[8],cam[9],cam[10],cam[11],cam[12],cam[13],cam[14],cam[15],cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22]);
	float4 camposdelta = (float4)(cmv[0],cmv[1],cmv[2],0.0f);
	float3 camrotdelta = (float3)(cmv[3],cmv[4],cmv[5]);

	float4 camdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float4 camrightdir = (float4)(0.0f,1.0f,0.0f,0.0f);
	float4 camupdir = (float4)(0.0f,0.0f,1.0f,0.0f);
	float4 camdirrot = matrixposmult(camdir, cammat);
	float4 camrightdirrot = matrixposmult(camrightdir, cammat);
	float4 camupdirrot = matrixposmult(camupdir, cammat);

	campos = translatepos(campos,camdirrot,camposdelta.x);
	campos = translatepos(campos,camrightdirrot,camposdelta.y);
	campos = translatepos(campos,camupdirrot,camposdelta.z);

	float16 camrotdeltamat = rotationmatrix(camrotdelta);
	cammat = matrixmatmult(cammat, camrotdeltamat);

	cam[0] = campos.x;
	cam[1] = campos.y;
	cam[2] = campos.z;
	cam[7] = cammat.s0;
	cam[8] = cammat.s1;
	cam[9] = cammat.s2;
	cam[10] = cammat.s3;
	cam[11] = cammat.s4;
	cam[12] = cammat.s5;
	cam[13] = cammat.s6;
	cam[14] = cammat.s7;
	cam[15] = cammat.s8;
	cam[16] = cammat.s9;
	cam[17] = cammat.sA;
	cam[18] = cammat.sB;
	cam[19] = cammat.sC;
	cam[20] = cammat.sD;
	cam[21] = cammat.sE;
	cam[22] = cammat.sF;
}

kernel void clearview(global int *img, global float *imz, global int *imh, global const float *cam) {
	unsigned int xid=get_global_id(0);
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	imh[0] = -1;
	for (int y=0;y<camres.y;y++) {
		int pixelind = y*camres.x+xid;
		img[pixelind] = 0x00000000;
		imz[pixelind] = INFINITY;
	}
}

kernel void rendercross(global int *img, global float *imz, global int *imh, global const float *cam) {
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	int2 camhalfres = camres/2;
	int crosslength = 20;

	int crosscolor = 0b11000000000000000000001111111111;
	for (int y=camhalfres.y-crosslength;y<camhalfres.y+crosslength;y++) {
		int pixelind = y*camres.x+camhalfres.x;
		img[pixelind] = crosscolor;
	}
	for (int x=camhalfres.x-crosslength;x<camhalfres.x+crosslength;x++) {
		int pixelind = camhalfres.y*camres.x+x;
		img[pixelind] = crosscolor;
	}
}

kernel void renderplaneview(global int *img, global float *imz, global int *imh , global const float *cam, global const float *tri, global const int *tex, global const float *obj) {
	unsigned int xid = get_global_id(0);
	unsigned int tid = get_global_id(1);
	unsigned int oid = get_global_id(2);
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = (float2)(cam[3],cam[4]);
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	float16 cammat = (float16)(cam[7],cam[8],cam[9],cam[10],cam[11],cam[12],cam[13],cam[14],cam[15],cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22]);

	const float4 camposzero = (float4)(0.0f,0.0f,0.0f,0.0f);
	const int ts = 16, os = 9;
	const int texturesize = 1024;
	static global atomic_int isdrawing[7680*4320];

	float2 camhalffovrad = radians(camfov/2.0f);
	float2 camhalffovlen = (float2)(tan(camhalffovrad.x), tan(camhalffovrad.y));
	int2 camhalfres = camres/2;
	float camcollen = -camhalffovlen.x + (camhalffovlen.x/(camhalfres.x-0.5f))*xid;

	float4 camdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float4 camrightdir = (float4)(0.0f,1.0f,0.0f,0.0f);
	float4 camupdir = (float4)(0.0f,0.0f,1.0f,0.0f);
	float4 coldir = (float4)(1.0f,camcollen,0.0f,0.0f);
	float4 colupdir = (float4)(1.0f,camcollen,camhalffovlen.y,0.0f);
	float4 coldowndir = (float4)(1.0f,camcollen,-camhalffovlen.y,0.0f);
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
	float4 rendercutplanepos = translatepos(campos, camdirrot, 0.001f);
	float4 rendercutplane = planefromnormalatpos(rendercutplanepos, camdirrot);

	float16 vtri = (float16)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],tri[tid*ts+3],tri[tid*ts+4],tri[tid*ts+5],tri[tid*ts+6],tri[tid*ts+7],tri[tid*ts+8],tri[tid*ts+9],tri[tid*ts+10],tri[tid*ts+11],tri[tid*ts+12],tri[tid*ts+13],tri[tid*ts+14],tri[tid*ts+15]);
	float4 tripos1 = (float4)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],0.0f);
	float4 tripos2 = (float4)(tri[tid*ts+3],tri[tid*ts+4],tri[tid*ts+5],0.0f);
	float4 tripos3 = (float4)(tri[tid*ts+6],tri[tid*ts+7],tri[tid*ts+8],0.0f);
	float tritexid = tri[tid*ts+9];
	float4 tripos1uv = (float4)(tri[tid*ts+10],tri[tid*ts+11],0.0f,0.0f);
	float4 tripos2uv = (float4)(tri[tid*ts+12],tri[tid*ts+13],0.0f,0.0f);
	float4 tripos3uv = (float4)(tri[tid*ts+14],tri[tid*ts+15],0.0f,0.0f);

	float4 objpos = (float4)(obj[oid*os+0],obj[oid*os+1],obj[oid*os+2],0.0f);
	float3 objsca = (float3)(obj[oid*os+3],obj[oid*os+4],obj[oid*os+5]);
	float3 objrot = (float3)(radians(obj[oid*os+6]),radians(obj[oid*os+7]),radians(obj[oid*os+8]));

	float16 objscamat = scalingmatrix(objsca);
	float16 objrotmat = rotationmatrix(objrot);
	float16 objmat = matrixmatmult(objscamat, objrotmat);
	
	tripos1 = matrixposmult(tripos1, objmat);
	tripos2 = matrixposmult(tripos2, objmat);
	tripos3 = matrixposmult(tripos3, objmat);
	tripos1 = translatepos(tripos1, objpos, 1.0f);
	tripos2 = translatepos(tripos2, objpos, 1.0f);
	tripos3 = translatepos(tripos3, objpos, 1.0f);

	vtri.s0 = tripos1.x;
	vtri.s1 = tripos1.y;
	vtri.s2 = tripos1.z;
	vtri.s3 = tripos2.x;
	vtri.s4 = tripos2.y;
	vtri.s5 = tripos2.z;
	vtri.s6 = tripos3.x;
	vtri.s7 = tripos3.y;
	vtri.s8 = tripos3.z;

	float16 intline = planetriangleintersection(colplane, vtri);
	float4 colpos1 = intline.s01234567.s0123;
	float4 colpos2 = intline.s01234567.s4567;
	float4 colpos1uv = intline.s89abcdef.s0123;
	float4 colpos2uv = intline.s89abcdef.s4567;

	if (!isnan(colpos1.x)) {
		float fwdintpointsdist1 = planepointdistance(colpos1, camdirplane);
		float fwdintpointsdist2 = planepointdistance(colpos2, camdirplane);
		float upintpointsdist1 = planepointdistance(colpos1, camupdirplane);
		float upintpointsdist2 = planepointdistance(colpos2, camupdirplane);

		if ((fwdintpointsdist1>=0.0f)||(fwdintpointsdist2>=0.0f)) {
			if ((fwdintpointsdist1<0.0f)||(fwdintpointsdist2<0.0f)) {
				float4 drawlinedir12 = colpos2-colpos1;
				float drawlinedir12dist = rayplanedistance(colpos1, drawlinedir12, rendercutplane);
				float4 drawlinepos3 = translatepos(colpos1, drawlinedir12, drawlinedir12dist);
				float fwdintpointsdist3 = planepointdistance(drawlinepos3, camdirplane);
				float upintpointsdist3 = planepointdistance(drawlinepos3, camupdirplane);
				float4 drawlinetexdir12 = colpos2uv - colpos1uv;
				float4 drawlinepos3uv = translatepos(colpos1uv, drawlinetexdir12, drawlinedir12dist);
				if (fwdintpointsdist1>=0.0f) {
					fwdintpointsdist2 = fwdintpointsdist3;
					upintpointsdist2 = upintpointsdist3;
					colpos2 = drawlinepos3;
					colpos2uv = drawlinepos3uv;
				} else {
					fwdintpointsdist1 = fwdintpointsdist3;
					upintpointsdist1 = upintpointsdist3;
					colpos1 = drawlinepos3;
					colpos1uv = drawlinepos3uv;
				}
			}

			float vpixelyang1 = atan(upintpointsdist1/fwdintpointsdist1);
			float vpixelyang2 = atan(upintpointsdist2/fwdintpointsdist2);
			float4 vpixelpointd1 = (float4)(fwdintpointsdist1,upintpointsdist1,0.0f,0.0f);
			float4 vpixelpointd2 = (float4)(fwdintpointsdist2,upintpointsdist2,0.0f,0.0f);

			int py1 = (camhalfres.y/camhalffovlen.y)*(upintpointsdist1/fwdintpointsdist1)+camhalfres.y;
			int py2 = (camhalfres.y/camhalffovlen.y)*(upintpointsdist2/fwdintpointsdist2)+camhalfres.y;
			if (!((py1<0)&&(py2<0))&&(!((py1>=camres.y)&&(py2>=camres.y)))) {
				if (py1<0) {py1=0;}
				if (py1>=camres.y) {py1=camres.y-1;}
				if (py2<0) {py2=0;}
				if (py2>=camres.y) {py2=camres.y-1;}
				int py1s = py1;
				int py2s = py2;
				if (py1>py2) {
					py1s = py2;
					py2s = py1;
					float4 vpixelpointtemp  = vpixelpointd1;
					vpixelpointd1 = vpixelpointd2;
					vpixelpointd2 = vpixelpointtemp;
					float vpixelyangtemp = vpixelyang1;
					vpixelyang1 = vpixelyang2;
					vpixelyang2 = vpixelyangtemp;
					float4 colpostemp = colpos1;
					colpos1 = colpos2;
					colpos2 = colpostemp;
					float4 colposuvtemp = colpos1uv;
					colpos1uv = colpos2uv;
					colpos2uv = colposuvtemp;
				}

				float4 vpixelpointdir12 = colpos2 - colpos1;
				for (int y=py1s;y<=py2s;y++) {
					float camcolleny = -camhalffovlen.y + (camhalffovlen.y/(camhalfres.y-0.5f))*y;
					float verticalangle = atan(camcolleny);
					float vpixelcampointangle = verticalangle - vpixelyang1;
					float8 vpixelpointdline = (float8)(0.0f);
					vpixelpointdline.s0123 = vpixelpointd1;
					vpixelpointdline.s4567 = vpixelpointd2;
					float vpixelpointlenfrac = linearanglelengthinterpolation(camposzero, vpixelpointdline, vpixelcampointangle);
					float4 linepoint = translatepos(colpos1, vpixelpointdir12, vpixelpointlenfrac);
					float4 linepointdir = linepoint - campos;
					float drawdistance = length(linepointdir);
					float4 vpixelpointdir12uv = colpos2uv - colpos1uv;
					float4 lineuvpos = translatepos(colpos1uv, vpixelpointdir12uv, vpixelpointlenfrac);
					float2 lineuv = (float2)(lineuvpos.x-floor(lineuvpos.x), lineuvpos.y-floor(lineuvpos.y));
					int lineuvx = convert_int_rte(lineuv.x*(texturesize-1));
					int lineuvy = convert_int_rte(lineuv.y*(texturesize-1));
					int texind = lineuvy*texturesize+lineuvx;

					int pixelind = (camres.y-y-1)*camres.x+xid;
					int checkval = 0;
					while(!atomic_compare_exchange_strong_explicit(&isdrawing[pixelind], &checkval, 1, memory_order_acquire, memory_order_relaxed, memory_scope_device)) {checkval = 0;}
					if (drawdistance<imz[pixelind]) {
						imz[pixelind] = drawdistance;
						if ((xid==camhalfres.x)&&(y==camhalfres.y)) {imh[0] = oid;}
						int texpixel = tex[texind];
						uchar4 texrgba = as_uchar4(texpixel);
						float4 texrgbaf = convert_float4(texrgba) / 255.0f;
						float4 rgbapixel = (float4)(texrgbaf.s2,texrgbaf.s1,texrgbaf.s0,texrgbaf.s3);
						int pixelcolor = (convert_int(rgbapixel.s3*3.0f)<<30) | (convert_int(rgbapixel.s2*1023.0f)<<20) | (convert_int(rgbapixel.s1*1023.0f)<<10) | convert_int(rgbapixel.s0*1023.0f);
						img[pixelind] = pixelcolor;
					}
					atomic_store_explicit(&isdrawing[pixelind], 0, memory_order_release, memory_scope_device);
				}
			}
		}
	}
}

kernel void renderrayview(global int *img, global float *imz, global int *imh , global const float *cam, global const float *tri, global const int *tex, global const float *obj) {
	unsigned int xid = get_global_id(0);
	unsigned int tid = get_global_id(1);
	unsigned int oid = get_global_id(2);
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = (float2)(cam[3],cam[4]);
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	float16 cammat = (float16)(cam[7],cam[8],cam[9],cam[10],cam[11],cam[12],cam[13],cam[14],cam[15],cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22]);

	const float4 camposzero = (float4)(0.0f,0.0f,0.0f,0.0f);
	const int ts = 16, os = 9;
	const int texturesize = 1024;
	static global atomic_int isdrawing[7680*4320];

	float2 camhalffovrad = radians(camfov/2.0f);
	float2 camhalffovlen = (float2)(tan(camhalffovrad.x), tan(camhalffovrad.y));
	int2 camhalfres = camres/2;
	float camraylenx = -camhalffovlen.x + (camhalffovlen.x/(camhalfres.x-0.5f))*xid;

	float4 camdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float4 camrightdir = (float4)(0.0f,1.0f,0.0f,0.0f);
	float4 camupdir = (float4)(0.0f,0.0f,1.0f,0.0f);
	float4 camdirrot = matrixposmult(camdir, cammat);
	float4 camrightdirrot = matrixposmult(camrightdir, cammat);
	float4 camupdirrot = matrixposmult(camupdir, cammat);
	float4 camdirplane = planefromnormalatpos(campos, camdirrot);
	float4 camrightdirplane = planefromnormalatpos(campos, camrightdirrot);
	float4 camupdirplane = planefromnormalatpos(campos, camupdirrot);

	float16 vtri = (float16)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],tri[tid*ts+3],tri[tid*ts+4],tri[tid*ts+5],tri[tid*ts+6],tri[tid*ts+7],tri[tid*ts+8],tri[tid*ts+9],tri[tid*ts+10],tri[tid*ts+11],tri[tid*ts+12],tri[tid*ts+13],tri[tid*ts+14],tri[tid*ts+15]);
	float4 tripos1 = (float4)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],0.0f);
	float4 tripos2 = (float4)(tri[tid*ts+3],tri[tid*ts+4],tri[tid*ts+5],0.0f);
	float4 tripos3 = (float4)(tri[tid*ts+6],tri[tid*ts+7],tri[tid*ts+8],0.0f);
	float tritexid = tri[tid*ts+9];
	float4 tripos1uv = (float4)(tri[tid*ts+10],tri[tid*ts+11],0.0f,0.0f);
	float4 tripos2uv = (float4)(tri[tid*ts+12],tri[tid*ts+13],0.0f,0.0f);
	float4 tripos3uv = (float4)(tri[tid*ts+14],tri[tid*ts+15],0.0f,0.0f);

	float4 objpos = (float4)(obj[oid*os+0],obj[oid*os+1],obj[oid*os+2],0.0f);
	float3 objsca = (float3)(obj[oid*os+3],obj[oid*os+4],obj[oid*os+5]);
	float3 objrot = (float3)(radians(obj[oid*os+6]),radians(obj[oid*os+7]),radians(obj[oid*os+8]));

	float16 objscamat = scalingmatrix(objsca);
	float16 objrotmat = rotationmatrix(objrot);
	float16 objmat = matrixmatmult(objscamat, objrotmat);
	
	tripos1 = matrixposmult(tripos1, objmat);
	tripos2 = matrixposmult(tripos2, objmat);
	tripos3 = matrixposmult(tripos3, objmat);
	tripos1 = translatepos(tripos1, objpos, 1.0f);
	tripos2 = translatepos(tripos2, objpos, 1.0f);
	tripos3 = translatepos(tripos3, objpos, 1.0f);

	vtri.s0 = tripos1.x;
	vtri.s1 = tripos1.y;
	vtri.s2 = tripos1.z;
	vtri.s3 = tripos2.x;
	vtri.s4 = tripos2.y;
	vtri.s5 = tripos2.z;
	vtri.s6 = tripos3.x;
	vtri.s7 = tripos3.y;
	vtri.s8 = tripos3.z;

	for (int y=0;y<camres.y;y++) {
		float camrayleny = -camhalffovlen.y + (camhalffovlen.y/(camhalfres.y-0.5f))*y;
		float4 raydir = (float4)(1.0f,camraylenx,camrayleny,0.0f);
		float4 raydirrot = matrixposmult(raydir, cammat);
		float8 intposuv = raytriangleintersection(campos, raydirrot, vtri);
		float4 intpos = intposuv.s0123;
		float4 intuv = intposuv.s4567;

		if (!isnan(intpos.x)) {
			float4 linepointdir = intpos - campos;
			float drawdistance = length(linepointdir);
			float camrayintposdist = planepointdistance(intpos, camdirplane);

			if (camrayintposdist>=0.0f) {
				float2 pointuv = (float2)(intuv.x-floor(intuv.x), intuv.y-floor(intuv.y));
				int pointuvx = convert_int_rte(pointuv.x*(texturesize-1));
				int pointuvy = convert_int_rte(pointuv.y*(texturesize-1));
				int texind = pointuvy*texturesize+pointuvx;

				int pixelind = (camres.y-y-1)*camres.x+xid;
				int checkval = 0;
				while(!atomic_compare_exchange_strong_explicit(&isdrawing[pixelind], &checkval, 1, memory_order_acquire, memory_order_relaxed, memory_scope_device)) {checkval = 0;}
				if (drawdistance<imz[pixelind]) {
					imz[pixelind] = drawdistance;
					if ((xid==camhalfres.x)&&(y==camhalfres.y)) {imh[0] = oid;}
					int texpixel = tex[texind];
					uchar4 texrgba = as_uchar4(texpixel);
					float4 texrgbaf = convert_float4(texrgba) / 255.0f;
					float4 rgbapixel = (float4)(texrgbaf.s2,texrgbaf.s1,texrgbaf.s0,texrgbaf.s3);
					int pixelcolor = (convert_int(rgbapixel.s3*3.0f)<<30) | (convert_int(rgbapixel.s2*1023.0f)<<20) | (convert_int(rgbapixel.s1*1023.0f)<<10) | convert_int(rgbapixel.s0*1023.0f);
					img[pixelind] = pixelcolor;
				}
				atomic_store_explicit(&isdrawing[pixelind], 0, memory_order_release, memory_scope_device);
			}
		}
	}
}
