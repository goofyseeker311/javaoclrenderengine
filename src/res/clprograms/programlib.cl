typedef struct {
	float4 pos;
	float3 scale;
	float3 rot;
	float4 sph;
	int ind;
	int len;
	int phys;
} entity;

typedef struct {
	float4 pos;
	float3 scale;
	float3 rot;
	float4 sph;
	int ind;
	int len;
} object;

typedef struct {
	float4 pos1;
	float4 pos2;
	float4 pos3;
	float4 norm;
	float4 pos1uv;
	float4 pos2uv;
	float4 pos3uv;
	int texid;
	float4 facecolor;
	float4 emissivecolor;
	float4 lightmapcolor;
	float roughness;
	float metallic;
	float refractind;
	float opacity;
} triangle;

float4 matrixposmult(float4 pos, float16 mat);
float16 matrixmatmult(float16 vmat1, float16 vmat2);
float16 scalingmatrix(float3 sca);
float16 rotationmatrix(float3 rot);
float16 rotationmatrixaroundaxis(float4 axis, float rot);
float vectorangle(float4 dir1, float4 dir2);
float4 planefromnormalatpos(float4 pos, float4 dir);
float rayplanedistance(float4 pos, float4 dir, float4 plane);
float planepointdistance(float4 pos, float4 plane);
float4 translatepos(float4 point, float4 dir, float mult);
float linearanglelengthinterpolation(float4 vpos, float8 vline, float vangle);
float4 triangleplane(triangle *vtri);
float16 planetriangleintersection(float4 plane, triangle *vtri);
float8 raytriangleintersection(float4 vpos, float4 vdir, triangle *vtri);
float raypointdistance(float4 vpos, float4 vdir, float4 vpoint);
float4 planenormal(float4 vplane);
float refractionoutangle(float anglein, float refraction1, float refraction2);
float8 planereflectionray(float8 vray, float4 vplane);
float8 planerefractionray(float8 vray, float4 vplane, float refraction1, float refraction2);
float4 sourceblend(float4 source, float alpha);
float4 sourceoverblend(float4 dest, float4 source, float alpha);
float4 sourcemixblend(float4 dest, float4 source, float alpha);
float8 renderray(float8 vray, int *imh, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit);
kernel void movecamera(global float *cam, global float *cmv, global float *ent);
kernel void clearview(global float *img, global float *imz, global int *imh, global float *cam);
kernel void transformentity(global float *tli, global float *oli, global float *eli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent);
kernel void physicscollision(global float *tli, global float *oli, global float *eli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global float *dts);
kernel void viewfilter(global float *imf, global float *img, global float *cam);
kernel void rendercross(global float *img, global float *imz, global int *imh, global float *cam);
kernel void renderrayview(global float *img, global float *imz, global int *imh, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn);

float4 matrixposmult(float4 pos, float16 mat) {
	float4 retpos = (float4)(0.0f);
	retpos.x = mat.s0*pos.x + mat.s1*pos.y + mat.s2*pos.z + mat.s3*pos.w;
	retpos.y = mat.s4*pos.x + mat.s5*pos.y + mat.s6*pos.z + mat.s7*pos.w;
	retpos.z = mat.s8*pos.x + mat.s9*pos.y + mat.sA*pos.z + mat.sB*pos.w;
	retpos.w = mat.sC*pos.x + mat.sD*pos.y + mat.sE*pos.z + mat.sF*pos.w;
	return retpos;
}
float16 matrixmatmult(float16 vmat1, float16 vmat2) {
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

float4 triangleplane(triangle *vtri) {
	float4 plane = (float4)(0.0f,0.0f,0.0f,0.0f);
	float4 p1 = vtri->pos1;
	float4 p2 = vtri->pos2;
	float4 p3 = vtri->pos3;
	float4 v1 = p2 - p1;
	float4 v2 = p3 - p1;
	float4 nm = normalize(cross(v1, v2));
	plane = planefromnormalatpos(p1, nm);
	return plane;
}

float16 planetriangleintersection(float4 plane, triangle *vtri) {
	float16 retline = (float16)(NAN);
	float4 pos1 = vtri->pos1;
	float4 pos2 = vtri->pos2;
	float4 pos3 = vtri->pos3;
	float4 pos1uv = vtri->pos1uv;
	float4 pos2uv = vtri->pos2uv;
	float4 pos3uv = vtri->pos3uv;
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

float8 raytriangleintersection(float4 vpos, float4 vdir, triangle *vtri) {
	float8 intposuvdist = (float8)(NAN);
	float4 tplane = triangleplane(vtri);
	float tpdist = rayplanedistance(vpos, vdir, tplane);
	float4 p4 = translatepos(vpos, vdir, tpdist);
	float4 p1 = vtri->pos1;
	float4 p2 = vtri->pos2;
	float4 p3 = vtri->pos3;
	float4 p1uv = vtri->pos1uv;
	float4 p2uv = vtri->pos2uv;
	float4 p3uv = vtri->pos3uv;
	float4 p4uv = (float4)(NAN);
	float4 v1 = p1 - vpos;
	float4 v2 = p2 - vpos;
	float4 v3 = p3 - vpos;
	float4 v12norm = cross(v1,v2);
	float4 v23norm = cross(v2,v3);
	float4 v31norm = cross(v3,v1);
	float4 v12plane = planefromnormalatpos(vpos,v12norm);
	float4 v23plane = planefromnormalatpos(vpos,v23norm);
	float4 v31plane = planefromnormalatpos(vpos,v31norm);
	float v12dist = planepointdistance(p4, v12plane);
	float v23dist = planepointdistance(p4, v23plane);
	float v31dist = planepointdistance(p4, v31plane);
	const float tol = 0.0f;
	if(((v12dist<=tol)&&(v23dist<=tol)&&(v31dist<=tol))||((v12dist>=-tol)&&(v23dist>=-tol)&&(v31dist>=-tol))) {
		float4 v12 = p2 - p1;
		float4 v21 = -v12;
		float4 v13 = p3 - p1;
		float vl12 = length(v12);
		float vl13 = length(v13);
		float ai1 = vectorangle(v21,v13);
		float4 t1 = p4 - p1;
		float tl1 = length(t1);
		float h12 = vectorangle(v12,t1); float h13 = vectorangle(v13,t1);
		float n12len = tl1*(sin(h13)/sin(ai1));
		float n13len = tl1*(sin(h12)/sin(ai1));
		float n12mult = n12len/vl12;
		float n13mult = n13len/vl13;
		float4 uv12delta = p2uv-p1uv;
		float4 uv13delta = p3uv-p1uv;
		p4uv = p1uv;
		p4uv = translatepos(p4uv, uv12delta, n12mult);
		p4uv = translatepos(p4uv, uv13delta, n13mult);
		intposuvdist.s0123 = p4;
		intposuvdist.s4567 = p4uv;
		intposuvdist.s6 = tpdist;
	}
	return intposuvdist;
}

float raypointdistance(float4 vpos, float4 vdir, float4 vpoint) {
	float dist = 0.0f;
	float4 raypospointdir = vpoint - vpos;
	float vdirlength = length(vdir);
	float4 vdircross = cross(vdir, raypospointdir);
	float vdircrosslen = length(vdircross); 
	dist = vdircrosslen/vdirlength;
	return dist;
}

float4 planenormal(float4 vplane) {
	float4 retnorm = (float4)(vplane.xyz, 0.0f);
	return retnorm;
}

float refractionoutangle(float anglein, float refraction1, float refraction2) {
	float retang = asin((refraction1/refraction2)*sin(anglein));
	return retang;
}

float8 planereflectionray(float8 vray, float4 vplane) {
	float8 reflectray = (float8)(NAN);
	float4 raypos = vray.s0123;
	float4 raydir = vray.s4567;
	float4 vplanenorm = planenormal(vplane);
	float rayintdist = rayplanedistance(raypos, raydir, vplane);
	if ((isfinite(rayintdist))&&(rayintdist>0.0f)) {
		float4 rayint = translatepos(raypos, raydir, rayintdist);
		float16 rayvplanerot = rotationmatrixaroundaxis(vplanenorm, M_PI_F);
		float4 mirrorraydir = matrixposmult(raydir, rayvplanerot);
		float4 mirrorraydirninv = -normalize(mirrorraydir);
		reflectray.s0123 = rayint;
		reflectray.s4567 = mirrorraydirninv;
	}
	return reflectray;
}
float8 planerefractionray(float8 vray, float4 vplane, float refraction1, float refraction2) {
	float8 refractray = (float8)(NAN);
	float4 raypos = vray.s0123;
	float4 raydir = vray.s4567;
	float vref1 = refraction2;
	float vref2 = refraction1;
	float rayintdist = rayplanedistance(raypos, raydir, vplane);
	float4 vplanenorm = planenormal(vplane);
	float rayvplaneangle = vectorangle(vplanenorm, raydir);
	if (rayvplaneangle>M_PI_2_F) {
		rayvplaneangle = M_PI_F - rayvplaneangle;
		vplanenorm = -vplanenorm;
		vref1 = refraction1;
		vref2 = refraction2;
	}
	if ((isfinite(rayintdist))&&(rayintdist>0.001f)) {
		float4 rayint = translatepos(raypos, raydir, rayintdist);
		float4 refnormal = cross(vplanenorm, raydir);
		if ((refnormal.x==0.0f)&&(refnormal.y==0.0f)&&(refnormal.z==0.0f)) {
			refractray.s0123 = rayint;
			refractray.s4567 = raydir;
		} else {
			float rayvplaneangleout = refractionoutangle(rayvplaneangle, vref1, vref2);
			if (isfinite(rayvplaneangleout)) {
				float4 refdownnormal = normalize(cross(refnormal, vplanenorm));
				const float4 zeropos = (float4)(0.0f,0.0f,0.0f,0.0f);
				float4 vplanezero = planefromnormalatpos(zeropos, vplanenorm);
				float raydist = planepointdistance(raydir, vplanezero);
				float refdowndist = tan(rayvplaneangleout)*raydist;
				float4 refractionraydirn = raydist*vplanenorm + refdowndist*refdownnormal;
				refractray.s0123 = rayint;
				refractray.s4567 = refractionraydirn;
			}
		}
	}
	return refractray;
}

float4 sourceblend(float4 source, float alpha) {
	float4 retcolor = alpha * source;
	return retcolor;
}
float4 sourceoverblend(float4 dest, float4 source, float alpha) {
	float4 retcolor = dest;
	retcolor.s0 = alpha*source.s0 + dest.s0*(1.0f-alpha*source.s0);
	retcolor.s1 = alpha*source.s1 + dest.s1*(1.0f-alpha*source.s1);
	retcolor.s2 = alpha*source.s2 + dest.s2*(1.0f-alpha*source.s2);
	retcolor.s3 = alpha*source.s3 + dest.s3*(1.0f-alpha*source.s3);
	return retcolor;
}
float4 sourcemixblend(float4 dest, float4 source, float alpha) {
	float4 retcolor = alpha*source + (1.0f-alpha)*dest;
	return retcolor;
}

float spherespheredistance(float4 vsphere1, float4 vsphere2) {
	float dist = sqrt(pow(vsphere2.x-vsphere1.x,2)+pow(vsphere2.y-vsphere1.y,2)+pow(vsphere2.z-vsphere1.z,2)) - (vsphere1.w+vsphere2.w); 
	return dist;
}

float8 renderray(float8 vray, int *imh, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit) {
	float8 raycolordist = (float8)(NAN);
	float4 campos = vray.s0123;
	float4 camdir = vray.s4567;
	int entc = enc[0];
	int texs = tes[0];
	int tlit = lit[0];

	const int ts = 45, os = 16, es = 17;
	float rayz = INFINITY;

	for (int eid=0;eid<entc;eid++) {
		entity vent;
		vent.pos = (float4)(ent[eid*es+0],ent[eid*es+1],ent[eid*es+2],ent[eid*es+3]);
		vent.scale = (float3)(ent[eid*es+4],ent[eid*es+5],ent[eid*es+6]);
		vent.rot = (float3)(ent[eid*es+7],ent[eid*es+8],ent[eid*es+9]);
		vent.sph = (float4)(ent[eid*es+10],ent[eid*es+11],ent[eid*es+12],ent[eid*es+13]);
		vent.ind = (int)ent[eid*es+14];
		vent.len = (int)ent[eid*es+15];
		vent.phys = (int)ent[eid*es+16];

		float eppdist = raypointdistance(campos, camdir, vent.sph);
		if (fabs(eppdist)<=vent.sph.w) {

			for (int oid=vent.ind;oid<(vent.ind+vent.len);oid++) {
				object vobj;
				vobj.pos = (float4)(obj[oid*os+0],obj[oid*os+1],obj[oid*os+2],obj[oid*os+3]);
				vobj.scale = (float3)(obj[oid*os+4],obj[oid*os+5],obj[oid*os+6]);
				vobj.rot = (float3)(obj[oid*os+7],obj[oid*os+8],obj[oid*os+9]);
				vobj.sph = (float4)(obj[oid*os+10],obj[oid*os+11],obj[oid*os+12],obj[oid*os+13]);
				vobj.ind = (int)obj[oid*os+14];
				vobj.len = (int)obj[oid*os+15];

				float oppdist = raypointdistance(campos, camdir, vobj.sph);
				if (fabs(oppdist)<=vobj.sph.w) {

					for (int tid=vobj.ind;tid<(vobj.ind+vobj.len);tid++) {
						triangle vtri;
						vtri.pos1 = (float4)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],tri[tid*ts+3]);
						vtri.pos2 = (float4)(tri[tid*ts+4],tri[tid*ts+5],tri[tid*ts+6],tri[tid*ts+7]);
						vtri.pos3 = (float4)(tri[tid*ts+8],tri[tid*ts+9],tri[tid*ts+10],tri[tid*ts+11]);
						vtri.norm = (float4)(tri[tid*ts+12],tri[tid*ts+13],tri[tid*ts+14],tri[tid*ts+15]);
						vtri.pos1uv = (float4)(tri[tid*ts+16],tri[tid*ts+17],tri[tid*ts+18],tri[tid*ts+19]);
						vtri.pos2uv = (float4)(tri[tid*ts+20],tri[tid*ts+21],tri[tid*ts+22],tri[tid*ts+23]);
						vtri.pos3uv = (float4)(tri[tid*ts+24],tri[tid*ts+25],tri[tid*ts+26],tri[tid*ts+27]);
						vtri.texid = (int)tri[tid*ts+28];
						vtri.facecolor = (float4)(tri[tid*ts+29],tri[tid*ts+30],tri[tid*ts+31],tri[tid*ts+32]);
						vtri.emissivecolor = (float4)(tri[tid*ts+33],tri[tid*ts+34],tri[tid*ts+35],tri[tid*ts+36]);
						vtri.lightmapcolor = (float4)(tri[tid*ts+37],tri[tid*ts+38],tri[tid*ts+39],tri[tid*ts+40]);
						vtri.roughness = tri[tid*ts+41];
						vtri.metallic = tri[tid*ts+42];
						vtri.refractind = tri[tid*ts+43];
						vtri.opacity = tri[tid*ts+44];
						
						float4 triplane = triangleplane(&vtri);
						float8 intpos = raytriangleintersection(campos, camdir, &vtri);
						float4 raypos = intpos.s0123;
						float4 rayposuv = (float4)(intpos.s45,0.0f,0.0f);
						float raydist = intpos.s6;

						if (!isnan(raypos.x)) {
							float drawdistance = raydist;
							float4 camray = camdir;

							float2 posuv = (float2)(rayposuv.x-floor(rayposuv.x), rayposuv.y-floor(rayposuv.y));
							int posuvintx = convert_int_rte(posuv.x*(texs-1));
							int posuvinty = convert_int_rte(posuv.y*(texs-1));
							int texind = posuvinty*texs+posuvintx + vtri.texid*texs*texs;

							if ((drawdistance>0.001f)&&(drawdistance<rayz)) {
								rayz = drawdistance;
								imh[0] = tid;
								float4 texcolor = vtri.facecolor;
								if (vtri.texid>=0) {
									float4 texrgbaf = convert_float4(as_uchar4(tex[texind])) / 255.0f;
									texcolor = (float4)(texrgbaf.s2, texrgbaf.s1, texrgbaf.s0, texrgbaf.s3);
								}
								float4 pixelcolor = (float4)(0.0f);
								if (tlit) {
									pixelcolor = vtri.emissivecolor + vtri.lightmapcolor*texcolor*(1.0f-vtri.metallic);
								} else {
									pixelcolor = vtri.emissivecolor + texcolor;
								}
								raycolordist.s0 = pixelcolor.s0;
								raycolordist.s1 = pixelcolor.s1;
								raycolordist.s2 = pixelcolor.s2;
								raycolordist.s3 = pixelcolor.s3;
								raycolordist.s4 = drawdistance;
							}
						}
					}
				}
			}
		}
	}
	return raycolordist;
}

kernel void movecamera(global float *cam, global float *cmv, global float *ent) {
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = radians((float2)(cam[3],cam[4]));
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	float16 cammat = (float16)(cam[7],cam[8],cam[9],cam[10],cam[11],cam[12],cam[13],cam[14],cam[15],cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22]);
	float4 camposdelta = (float4)(cmv[0],cmv[1],cmv[2],0.0f);
	float3 camrotdelta = (float3)(cmv[3],cmv[4],cmv[5]);

	float4 camdir = (float4)(0.0f,0.0f,-1.0f,0.0f);
	float4 camrightdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float4 camupdir = (float4)(0.0f,-1.0f,0.0f,0.0f);
	float4 camdirrot = matrixposmult(camdir, cammat);
	float4 camrightdirrot = matrixposmult(camrightdir, cammat);
	float4 camupdirrot = matrixposmult(camupdir, cammat);

	campos = translatepos(campos,camdirrot,camposdelta.x);
	campos = translatepos(campos,camrightdirrot,camposdelta.y);
	campos = translatepos(campos,camupdirrot,camposdelta.z);

	float16 camrotdeltamat = rotationmatrix(camrotdelta);
	cammat = matrixmatmult(cammat, camrotdeltamat);

	cam[0] = campos.x; cam[1] = campos.y; cam[2] = campos.z;
	cam[7] = cammat.s0; cam[8] = cammat.s1; cam[9] = cammat.s2;
	cam[10] = cammat.s3; cam[11] = cammat.s4; cam[12] = cammat.s5;
	cam[13] = cammat.s6; cam[14] = cammat.s7; cam[15] = cammat.s8;
	cam[16] = cammat.s9; cam[17] = cammat.sA; cam[18] = cammat.sB;
	cam[19] = cammat.sC; cam[20] = cammat.sD; cam[21] = cammat.sE;
	cam[22] = cammat.sF;

	ent[0] = campos.x; ent[1] = campos.y; ent[2] = campos.z;
}

kernel void clearview(global float *img, global float *imz, global int *imh, global float *cam) {
	unsigned int xid = get_global_id(0);
	unsigned int vid = get_global_id(1);
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	
	const int vs = 8;
	int camresystep = camres.y / vs;
	int campresystart = camresystep*vid;
	int campresyend = camresystep*vid + camresystep-1;

	imh[0] = -1;
	for (int y=campresystart;y<=campresyend;y++) {
		int pixelind = y*camres.x+xid;
		img[pixelind*4+0] = 0.0f;
		img[pixelind*4+1] = 0.0f;
		img[pixelind*4+2] = 0.0f;
		img[pixelind*4+3] = 0.0f;
		imz[pixelind] = INFINITY;
	}
}

kernel void transformentity(global float *tli, global float *oli, global float *eli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent) {
	unsigned int eid = get_global_id(0);
	int objc = obc[0];
	int tric = trc[0];
	const int ts = 45, os = 16, es = 17;

	entity vent;
	vent.pos = (float4)(ent[eid*es+0],ent[eid*es+1],ent[eid*es+2],ent[eid*es+3]);
	vent.scale = (float3)(ent[eid*es+4],ent[eid*es+5],ent[eid*es+6]);
	vent.rot = (float3)(ent[eid*es+7],ent[eid*es+8],ent[eid*es+9]);
	vent.sph = (float4)(ent[eid*es+10],ent[eid*es+11],ent[eid*es+12],ent[eid*es+13]);
	vent.ind = (int)ent[eid*es+14];
	vent.len = (int)ent[eid*es+15];
	vent.phys = (int)ent[eid*es+16];

	float16 entscamat = scalingmatrix(vent.scale);
	float16 entrotmat = rotationmatrix(vent.rot);
	float16 entmat = matrixmatmult(entscamat, entrotmat);
	float entmaxsca = fmax(fmax(vent.scale.x, vent.scale.y), vent.scale.z);

	float4 entsphdir = (float4)(vent.sph.x, vent.sph.y, vent.sph.z, 0.0f);
	float4 entsphdirrot = matrixposmult(entsphdir, entmat);
	float4 entbvc = vent.pos + entsphdirrot; entbvc.w = vent.sph.w*entmaxsca;

	eli[eid*es+0] = vent.pos.x; eli[eid*es+1] = vent.pos.y; eli[eid*es+2] = vent.pos.z; eli[eid*es+3] = vent.pos.w;
	eli[eid*es+4] = vent.scale.x; eli[eid*es+5] = vent.scale.y; eli[eid*es+6] = vent.scale.z;
	eli[eid*es+7] = vent.rot.x; eli[eid*es+8] = vent.rot.y; eli[eid*es+9] = vent.rot.z;
	eli[eid*es+10] = entbvc.x; eli[eid*es+11] = entbvc.y; eli[eid*es+12] = entbvc.z; eli[eid*es+13] = entbvc.w;
	eli[eid*es+14] = vent.ind; eli[eid*es+15] = vent.len; eli[eid*es+16] = vent.phys;

	for (int oid=vent.ind;oid<(vent.ind+vent.len);oid++) {
		object vobj;
		vobj.pos = (float4)(obj[oid*os+0],obj[oid*os+1],obj[oid*os+2],obj[oid*os+3]);
		vobj.scale = (float3)(obj[oid*os+4],obj[oid*os+5],obj[oid*os+6]);
		vobj.rot = (float3)(obj[oid*os+7],obj[oid*os+8],obj[oid*os+9]);
		vobj.sph = (float4)(obj[oid*os+10],obj[oid*os+11],obj[oid*os+12],obj[oid*os+13]);
		vobj.ind = (int)obj[oid*os+14];
		vobj.len = (int)obj[oid*os+15];

		float16 objscamat = scalingmatrix(vobj.scale);
		float16 objrotmat = rotationmatrix(vobj.rot);
		float16 objmat = matrixmatmult(objscamat, objrotmat);
		float objmaxsca = fmax(fmax(vobj.scale.x, vobj.scale.y), vobj.scale.z);

		objmat = matrixmatmult(entmat, objmat);
		float4 objpos = translatepos(vobj.pos, vent.pos, 1.0f);

		float4 objsphdir = (float4)(vobj.sph.x, vobj.sph.y, vobj.sph.z, 0.0f);
		float4 objsphdirrot = matrixposmult(objsphdir, objmat);
		float4 objbvc = objpos + objsphdirrot; objbvc.w = vobj.sph.w*objmaxsca*entmaxsca;

		oli[oid*os+0] = vobj.pos.x; oli[oid*os+1] = vobj.pos.y; oli[oid*os+2] = vobj.pos.z; oli[oid*os+3] = vobj.pos.w;
		oli[oid*os+4] = vobj.scale.x; oli[oid*os+5] = vobj.scale.y; oli[oid*os+6] = vobj.scale.z;
		oli[oid*os+7] = vobj.rot.x; oli[oid*os+8] = vobj.rot.y; oli[oid*os+9] = vobj.rot.z;
		oli[oid*os+10] = objbvc.x; oli[oid*os+11] = objbvc.y; oli[oid*os+12] = objbvc.z; oli[oid*os+13] = objbvc.w;
		oli[oid*os+14] = vobj.ind; oli[oid*os+15] = vobj.len;

		for (int tid=vobj.ind;tid<(vobj.ind+vobj.len);tid++) {
			triangle vtri;
			vtri.pos1 = (float4)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],tri[tid*ts+3]);
			vtri.pos2 = (float4)(tri[tid*ts+4],tri[tid*ts+5],tri[tid*ts+6],tri[tid*ts+7]);
			vtri.pos3 = (float4)(tri[tid*ts+8],tri[tid*ts+9],tri[tid*ts+10],tri[tid*ts+11]);
			vtri.norm = (float4)(tri[tid*ts+12],tri[tid*ts+13],tri[tid*ts+14],tri[tid*ts+15]);
			vtri.pos1uv = (float4)(tri[tid*ts+16],tri[tid*ts+17],tri[tid*ts+18],tri[tid*ts+19]);
			vtri.pos2uv = (float4)(tri[tid*ts+20],tri[tid*ts+21],tri[tid*ts+22],tri[tid*ts+23]);
			vtri.pos3uv = (float4)(tri[tid*ts+24],tri[tid*ts+25],tri[tid*ts+26],tri[tid*ts+27]);
			vtri.texid = (int)tri[tid*ts+28];
			vtri.facecolor = (float4)(tri[tid*ts+29],tri[tid*ts+30],tri[tid*ts+31],tri[tid*ts+32]);
			vtri.emissivecolor = (float4)(tri[tid*ts+33],tri[tid*ts+34],tri[tid*ts+35],tri[tid*ts+36]);
			vtri.lightmapcolor = (float4)(tri[tid*ts+37],tri[tid*ts+38],tri[tid*ts+39],tri[tid*ts+40]);
			vtri.roughness = tri[tid*ts+41];
			vtri.metallic = tri[tid*ts+42];
			vtri.refractind = tri[tid*ts+43];
			vtri.opacity = tri[tid*ts+44];
			
			float4 tripos1 = matrixposmult(vtri.pos1, objmat);
			float4 tripos2 = matrixposmult(vtri.pos2, objmat);
			float4 tripos3 = matrixposmult(vtri.pos3, objmat);
			float4 trinorm = matrixposmult(vtri.norm, objmat);
			tripos1 = translatepos(tripos1, objpos, 1.0f);
			tripos2 = translatepos(tripos2, objpos, 1.0f);
			tripos3 = translatepos(tripos3, objpos, 1.0f);

			tli[tid*ts+0] = tripos1.x; tli[tid*ts+1] = tripos1.y; tli[tid*ts+2] = tripos1.z; tli[tid*ts+3] = tripos1.w;
			tli[tid*ts+4] = tripos2.x; tli[tid*ts+5] = tripos2.y; tli[tid*ts+6] = tripos2.z; tli[tid*ts+7] = tripos2.w;
			tli[tid*ts+8] = tripos3.x; tli[tid*ts+9] = tripos3.y; tli[tid*ts+10] = tripos3.z; tli[tid*ts+11] = tripos3.w;
			tli[tid*ts+12] = trinorm.x; tli[tid*ts+13] = trinorm.y; tli[tid*ts+14] = trinorm.z; tli[tid*ts+15] = trinorm.w;
			tli[tid*ts+16] = vtri.pos1uv.x; tli[tid*ts+17] = vtri.pos1uv.y; tli[tid*ts+18] = vtri.pos1uv.z; tli[tid*ts+19] = vtri.pos1uv.w;
			tli[tid*ts+20] = vtri.pos2uv.x; tli[tid*ts+21] = vtri.pos2uv.y; tli[tid*ts+22] = vtri.pos2uv.z; tli[tid*ts+23] = vtri.pos2uv.w;
			tli[tid*ts+24] = vtri.pos3uv.x; tli[tid*ts+25] = vtri.pos3uv.y; tli[tid*ts+26] = vtri.pos3uv.z; tli[tid*ts+27] = vtri.pos3uv.w;
			tli[tid*ts+28] = vtri.texid;
			tli[tid*ts+29] = vtri.facecolor.s0; tli[tid*ts+30] = vtri.facecolor.s1; tli[tid*ts+31] = vtri.facecolor.s2; tli[tid*ts+32] = vtri.facecolor.s3;
			tli[tid*ts+33] = vtri.emissivecolor.s0; tli[tid*ts+34] = vtri.emissivecolor.s1; tli[tid*ts+35] = vtri.emissivecolor.s2; tli[tid*ts+36] = vtri.emissivecolor.s3;
			tli[tid*ts+37] = vtri.lightmapcolor.s0; tli[tid*ts+38] = vtri.lightmapcolor.s1; tli[tid*ts+39] = vtri.lightmapcolor.s2; tli[tid*ts+40] = vtri.lightmapcolor.s3;
			tli[tid*ts+41] = vtri.roughness;
			tli[tid*ts+42] = vtri.metallic;
			tli[tid*ts+43] = vtri.refractind;
			tli[tid*ts+44] = vtri.opacity;
		}
	}
}

kernel void physicscollision(global float *tli, global float *oli, global float *eli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global float *dts) {
	unsigned int eix = get_global_id(0);
	int entc = enc[0];
	float deltatime = dts[0];
	const int ts = 45, os = 16, es = 17;

	entity cent;
	cent.pos = (float4)(eli[eix*es+0],eli[eix*es+1],eli[eix*es+2],eli[eix*es+3]);
	cent.scale = (float3)(eli[eix*es+4],eli[eix*es+5],eli[eix*es+6]);
	cent.rot = (float3)(eli[eix*es+7],eli[eix*es+8],eli[eix*es+9]);
	cent.sph = (float4)(eli[eix*es+10],eli[eix*es+11],eli[eix*es+12],eli[eix*es+13]);
	cent.ind = (int)eli[eix*es+14];
	cent.len = (int)eli[eix*es+15];
	cent.phys = (int)eli[eix*es+16];
	if (cent.phys!=1) {return;}

	float4 entdir = (float4)(0.0f);

	for (int eid=0;eid<entc;eid++) {
		if (eid!=eix) {
			entity vent;
			vent.pos = (float4)(eli[eid*es+0],eli[eid*es+1],eli[eid*es+2],eli[eid*es+3]);
			vent.scale = (float3)(eli[eid*es+4],eli[eid*es+5],eli[eid*es+6]);
			vent.rot = (float3)(eli[eid*es+7],eli[eid*es+8],eli[eid*es+9]);
			vent.sph = (float4)(eli[eid*es+10],eli[eid*es+11],eli[eid*es+12],eli[eid*es+13]);
			vent.ind = (int)eli[eid*es+14];
			vent.len = (int)eli[eid*es+15];
			vent.phys = (int)eli[eid*es+16];

			float sphdist = spherespheredistance(cent.sph, vent.sph);
			if (sphdist<0.0f) {
				float4 sphdir = normalize(cent.sph - vent.sph); sphdir.w = 0.0f;
				entdir += sphdir;
			}
		}
	}

	float4 entpos = (float4)(ent[eix*es+0],ent[eix*es+1],ent[eix*es+2],ent[eix*es+3]);
	float4 enddirlim = entdir * deltatime;
	float enddirlimlen = length(enddirlim);
	if (enddirlimlen>1.0f) {
		enddirlim /= enddirlimlen;
	}
	entpos += enddirlim;
	ent[eix*es+0] = entpos.x; ent[eix*es+1] = entpos.y; ent[eix*es+2] = entpos.z; ent[eix*es+3] = entpos.w;
}

kernel void lightobject(global float *img, global float *imz, global int *imh, global float *tli, global float *tri, global float *obj, global float *ent, global int *enc, global int *tex, global int *tes) {
	unsigned int tid = get_global_id(2);

	const int ts = 45, lit = 1, nor = 1, cmstep = 32*32;

	float4 tripos1 = (float4)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],tri[tid*ts+3]);
	float4 tripos2 = (float4)(tri[tid*ts+4],tri[tid*ts+5],tri[tid*ts+6],tri[tid*ts+7]);
	float4 tripos3 = (float4)(tri[tid*ts+8],tri[tid*ts+9],tri[tid*ts+10],tri[tid*ts+11]);

	float4 centerpos = (tripos1 + tripos2 + tripos3) / 3.0f;
	float16 tm = rotationmatrix((float3)(0.0f,0.0f,0.0f));
	float16 tm2 = rotationmatrix((float3)(M_PI_F,0.0f,0.0f));
	float16 tm3 = rotationmatrix((float3)(M_PI_2_F,0.0f,0.0f));
	float16 tm4 = rotationmatrix((float3)(M_PI_2_F,0.0f,M_PI_2_F));
	float16 tm5 = rotationmatrix((float3)(M_PI_2_F,0.0f,M_PI_F));
	float16 tm6 = rotationmatrix((float3)(M_PI_2_F,0.0f,3.0f*M_PI_2_F));
	float tricam[23] = {centerpos.x,centerpos.y,centerpos.z, 90.0f,90.0f,32.0f,32.0f,tm[0],tm[1],tm[2],tm[3],tm[4],tm[5],tm[6],tm[7],tm[8],tm[9],tm[10],tm[11],tm[12],tm[13],tm[14],tm[15]};
	float tricam2[23] = {centerpos.x,centerpos.y,centerpos.z, 90.0f,90.0f,32.0f,32.0f,tm2[0],tm2[1],tm2[2],tm2[3],tm2[4],tm2[5],tm2[6],tm2[7],tm2[8],tm2[9],tm2[10],tm2[11],tm2[12],tm2[13],tm2[14],tm2[15]};
	float tricam3[23] = {centerpos.x,centerpos.y,centerpos.z, 90.0f,90.0f,32.0f,32.0f,tm3[0],tm3[1],tm3[2],tm3[3],tm3[4],tm3[5],tm3[6],tm3[7],tm3[8],tm3[9],tm3[10],tm3[11],tm3[12],tm3[13],tm3[14],tm3[15]};
	float tricam4[23] = {centerpos.x,centerpos.y,centerpos.z, 90.0f,90.0f,32.0f,32.0f,tm4[0],tm4[1],tm4[2],tm4[3],tm4[4],tm4[5],tm4[6],tm4[7],tm4[8],tm4[9],tm4[10],tm4[11],tm4[12],tm4[13],tm4[14],tm4[15]};
	float tricam5[23] = {centerpos.x,centerpos.y,centerpos.z, 90.0f,90.0f,32.0f,32.0f,tm5[0],tm5[1],tm5[2],tm5[3],tm5[4],tm5[5],tm5[6],tm5[7],tm5[8],tm5[9],tm5[10],tm5[11],tm5[12],tm5[13],tm5[14],tm5[15]};
	float tricam6[23] = {centerpos.x,centerpos.y,centerpos.z, 90.0f,90.0f,32.0f,32.0f,tm6[0],tm6[1],tm6[2],tm6[3],tm6[4],tm6[5],tm6[6],tm6[7],tm6[8],tm6[9],tm6[10],tm6[11],tm6[12],tm6[13],tm6[14],tm6[15]};
	//renderplaneview(&img[cmstep*0+cmstep*6*tid], &imz[cmstep*0+cmstep*6*tid], &imh[0+tid*6], &tricam, tri, trc, tex, tes, &lit, &nor);
	//renderplaneview(&img[cmstep*1+cmstep*6*tid], &imz[cmstep*1+cmstep*6*tid], &imh[1+tid*6], &tricam2, tri, trc, tex, tes, &lit, &nor);
	//renderplaneview(&img[cmstep*2+cmstep*6*tid], &imz[cmstep*2+cmstep*6*tid], &imh[2+tid*6], &tricam3, tri, trc, tex, tes, &lit, &nor);
	//renderplaneview(&img[cmstep*3+cmstep*6*tid], &imz[cmstep*3+cmstep*6*tid], &imh[3+tid*6], &tricam4, tri, trc, tex, tes, &lit, &nor);
	//renderplaneview(&img[cmstep*4+cmstep*6*tid], &imz[cmstep*4+cmstep*6*tid], &imh[4+tid*6], &tricam5, tri, trc, tex, tes, &lit, &nor);
	//renderplaneview(&img[cmstep*5+cmstep*6*tid], &imz[cmstep*5+cmstep*6*tid], &imh[5+tid*6], &tricam6, tri, trc, tex, tes, &lit, &nor);
}

kernel void viewfilter(global float *imf, global float *img, global float *cam) {
	unsigned int xid = get_global_id(0);
	unsigned int yid = get_global_id(1);
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	const float fac = 0.0625f;

	int pind = yid*camres.x+xid;
	if ((xid>0)&&(xid<(camres.x-1))&&(yid>0)&&(yid<(camres.y-1))) {
		int pindN = (yid-1)*camres.x+(xid+0);
		int pindS = (yid+1)*camres.x+(xid+0);
		int pindW = (yid+0)*camres.x+(xid-1);
		int pindE = (yid+0)*camres.x+(xid+1);
		int pindNW = (yid-1)*camres.x+(xid-1);
		int pindNE = (yid-1)*camres.x+(xid+1);
		int pindSW = (yid+1)*camres.x+(xid-1);
		int pindSE = (yid+1)*camres.x+(xid+1);
		imf[pind*4+0] = (1.0f-fac*8.0f)*img[pind*4+0] + fac*img[pindN*4+0] + fac*img[pindS*4+0] + fac*img[pindW*4+0] + fac*img[pindE*4+0] + fac*img[pindNW*4+0] + fac*img[pindNE*4+0] + fac*img[pindSW*4+0] + fac*img[pindSE*4+0];
		imf[pind*4+1] = (1.0f-fac*8.0f)*img[pind*4+1] + fac*img[pindN*4+1] + fac*img[pindS*4+1] + fac*img[pindW*4+1] + fac*img[pindE*4+1] + fac*img[pindNW*4+1] + fac*img[pindNE*4+1] + fac*img[pindSW*4+1] + fac*img[pindSE*4+1];
		imf[pind*4+2] = (1.0f-fac*8.0f)*img[pind*4+2] + fac*img[pindN*4+2] + fac*img[pindS*4+2] + fac*img[pindW*4+2] + fac*img[pindE*4+2] + fac*img[pindNW*4+2] + fac*img[pindNE*4+2] + fac*img[pindSW*4+2] + fac*img[pindSE*4+2];
		imf[pind*4+3] = (1.0f-fac*8.0f)*img[pind*4+3] + fac*img[pindN*4+3] + fac*img[pindS*4+3] + fac*img[pindW*4+3] + fac*img[pindE*4+3] + fac*img[pindNW*4+3] + fac*img[pindNE*4+3] + fac*img[pindSW*4+3] + fac*img[pindSE*4+3];
	} else {
		imf[pind*4+0] = img[pind*4+0];
		imf[pind*4+1] = img[pind*4+1];
		imf[pind*4+2] = img[pind*4+2];
		imf[pind*4+3] = img[pind*4+3];
	}
}

kernel void rendercross(global float *img, global float *imz, global int *imh, global float *cam) {
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	int2 camhalfres = camres/2;
	int crosslength = 20;

	float4 crosscolor = (float4)(1000.0f,0.0f,0.0f,1.0f);
	for (int y=camhalfres.y-crosslength;y<camhalfres.y+crosslength;y++) {
		int pixelind = y*camres.x+camhalfres.x;
		img[pixelind*4+0] = crosscolor.s0;
		img[pixelind*4+1] = crosscolor.s1;
		img[pixelind*4+2] = crosscolor.s2;
		img[pixelind*4+3] = crosscolor.s3;
	}
	for (int x=camhalfres.x-crosslength;x<camhalfres.x+crosslength;x++) {
		int pixelind = camhalfres.y*camres.x+x;
		img[pixelind*4+0] = crosscolor.s0;
		img[pixelind*4+1] = crosscolor.s1;
		img[pixelind*4+2] = crosscolor.s2;
		img[pixelind*4+3] = crosscolor.s3;
	}
}

kernel void renderrayview(global float *img, global float *imz, global int *imh, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn) {
	unsigned int xid = get_global_id(0);
	unsigned int yid = get_global_id(1);
	const int ts = 45, os = 16, es = 17;

	int entc = enc[0];
	int objc = obc[0];
	int tric = trc[0];
	
	int rstepx = rsx[0];
	int rstepy = rsy[0];
	int rstepnum = rsn[0];
	int xidstep = xid % rstepx;
	int yidstep = yid % rstepy;
	int xstep = rstepnum % rstepx;
	int ystep = rstepnum / rstepx;
	if ((xidstep!=xstep)||(yidstep!=ystep)) {return;}

	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = radians((float2)(cam[3],cam[4]));
	int2 camres = (int2)((int)cam[5],(int)cam[6]);
	float16 cammat = (float16)(cam[7],cam[8],cam[9],cam[10],cam[11],cam[12],cam[13],cam[14],cam[15],cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22]);
	int sphnor = nor[0];

	float2 camhalffov = camfov/2.0f;
	float2 camhalffovlen = (float2)(tan(camhalffov.x), tan(camhalffov.y));
	int2 camhalfres = camres/2;
	float camraylenx = -camhalffovlen.x + (camhalffovlen.x/(camhalfres.x-0.5f))*xid;
	float camrayleny = -camhalffovlen.y + (camhalffovlen.y/(camhalfres.y-0.5f))*yid;
	float4 raydir = (float4)(camraylenx,-camrayleny,-1.0f,0.0f);
	float4 raydirrot = matrixposmult(raydir, cammat);
	float raydirrotlen = length(raydirrot);

	float8 camray = (float8)(NAN);
	camray.s0123 = campos;
	camray.s4567 = raydirrot;
	int hitid = -1;
	float8 rayint = renderray(camray, &hitid, tri, trc, obj, obc, ent, enc, tex, tes, lit);
	float4 raycolor = rayint.s0123;
	float raydist = rayint.s4;

	if (!isnan(raycolor.s0)) {
		float drawdistance = raydist;
		int pixelind = (camres.y-yid-1)*camres.x+xid;
		if (drawdistance<imz[pixelind]) {
			imz[pixelind] = drawdistance;
			if ((xid==camhalfres.x)&&(yid==camhalfres.y)) {imh[0] = hitid;}
			if (sphnor) {raycolor.s012=raycolor.s012/raydirrotlen;}
			img[pixelind*4+0] = raycolor.s0;
			img[pixelind*4+1] = raycolor.s1;
			img[pixelind*4+2] = raycolor.s2;
			img[pixelind*4+3] = raycolor.s3;
		}
	}
}
