#define ts 46
#define os 16
#define es 17
#define vs 40
#define cs 32
#define zs 128
#define ld 4.0f
#define lm 1000.0f
#define cw 4.0f

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
	int prelit;
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
float8 renderray(float8 vray, float campixelang, int *ihe, int *iho, int *iht, int *iti, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit);
void rayview(int xid, int yid, float *img, float *imz, int *imh, int *ihe, int *iho, int *iht, int *iti, float *cam, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit, int *nor, int *rsx, int *rsy, int *rsn);
void bounceview(int xid, int yid, float *img, float *imz, int *imh, int *ihe, int *iho, int *iht, float *cam, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit, int *nor, int *rsx, int *rsy, int *rsn);
void planeview(int xid, int vid, int vst, float *img, float *imz, int *imh, int *ihe, int *iho, int *iht, int *iti, float *cam, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit, int *nor, int *rsx, int *rsy, int *rsn);

kernel void movecamera(global float *cam, global float *cmv);
kernel void clearview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global float *cam);
kernel void transformentity(global float *ttr, global float *otr, global float *etr, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent);
kernel void physicscollision(global float *cam, global float *tli, global float *oli, global float *eli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global float *dts);
kernel void lightcopy(global float *tli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes);
kernel void lightentity(global float *tli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes);
kernel void viewfilter(global float *imf, global float *img, global float *cam);
kernel void rendercross(global float *img, global float *imz, global int *imh, global float *cam);
kernel void renderrayview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global int *iti, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn);
kernel void bounceraysview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn);
kernel void renderplaneview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global int *iti, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn);

float4 matrixposmult(float4 pos, float16 mat) {
	float4 retpos = (float4)(0.0f);
	retpos.x = mat.s0*pos.x + mat.s1*pos.y + mat.s2*pos.z;
	retpos.y = mat.s4*pos.x + mat.s5*pos.y + mat.s6*pos.z;
	retpos.z = mat.s8*pos.x + mat.s9*pos.y + mat.sA*pos.z;
	return retpos;
}
float16 matrixmatmult(float16 vmat1, float16 vmat2) {
	float16 retmat = (float16)(0.0f);
	retmat.s0 = vmat1.s0*vmat2.s0 + vmat1.s1*vmat2.s4 + vmat1.s2*vmat2.s8;
	retmat.s1 = vmat1.s0*vmat2.s1 + vmat1.s1*vmat2.s5 + vmat1.s2*vmat2.s9;
	retmat.s2 = vmat1.s0*vmat2.s2 + vmat1.s1*vmat2.s6 + vmat1.s2*vmat2.sA;
	retmat.s4 = vmat1.s4*vmat2.s0 + vmat1.s5*vmat2.s4 + vmat1.s6*vmat2.s8;
	retmat.s5 = vmat1.s4*vmat2.s1 + vmat1.s5*vmat2.s5 + vmat1.s6*vmat2.s9;
	retmat.s6 = vmat1.s4*vmat2.s2 + vmat1.s5*vmat2.s6 + vmat1.s6*vmat2.sA;
	retmat.s8 = vmat1.s8*vmat2.s0 + vmat1.s9*vmat2.s4 + vmat1.sA*vmat2.s8;
	retmat.s9 = vmat1.s8*vmat2.s1 + vmat1.s9*vmat2.s5 + vmat1.sA*vmat2.s9;
	retmat.sA = vmat1.s8*vmat2.s2 + vmat1.s9*vmat2.s6 + vmat1.sA*vmat2.sA;
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

float8 renderray(float8 vray, float campixelang, int *ihe, int *iho, int *iht, int *iti, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit) {
	float8 raycolordistpos = (float8)(NAN);
	float4 campos = vray.s0123;
	float4 camdir = vray.s4567;
	int entc = enc[0];
	int texs = tes[0];
	int tlit = lit[0];

	float imzbuf = INFINITY;
	int ihebuf = -1;
	int ihobuf = -1;
	int ihtbuf = -1;
	int itibuf = -1;

	for (int eid=0;eid<entc;eid++) {
		entity vent;
		vent.sph = (float4)(ent[eid*es+10],ent[eid*es+11],ent[eid*es+12],ent[eid*es+13]);
		vent.ind = (int)ent[eid*es+14];
		vent.len = (int)ent[eid*es+15];

		float4 ventsphvec = vent.sph-campos;
		float ventsphdist = length(ventsphvec);
		float ventang = asin(vent.sph.w/ventsphdist);

		if (ventang>=campixelang) {

			float eppdist = raypointdistance(campos, camdir, vent.sph);
			if (fabs(eppdist)<=vent.sph.w) {

				for (int oid=vent.ind;oid<(vent.ind+vent.len);oid++) {
					object vobj;
					vobj.sph = (float4)(obj[oid*os+10],obj[oid*os+11],obj[oid*os+12],obj[oid*os+13]);
					vobj.ind = (int)obj[oid*os+14];
					vobj.len = (int)obj[oid*os+15];

					float4 vobjsphvec = vobj.sph-campos;
					float vobjsphdist = length(vobjsphvec);
					float vobjang = asin(vobj.sph.w/vobjsphdist);

					if (vobjang>=campixelang) {

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
								
								float4 triplane = planefromnormalatpos(vtri.pos1, vtri.norm);

								float8 intpos = raytriangleintersection(campos, camdir, &vtri);
								float4 raypos = intpos.s0123;
								float4 rayposuv = (float4)(intpos.s45,0.0f,0.0f);
								float raydist = intpos.s6;

								if (!isnan(raypos.x)) {
									float drawdistance = raydist;
									float4 camray = camdir;

									float rayangle = vectorangle(camray, vtri.norm);
									bool frontface = rayangle>=M_PI_2_F;

									float2 posuv = (float2)(rayposuv.x-floor(rayposuv.x), rayposuv.y-floor(rayposuv.y));
									int posuvintx = convert_int_rte(posuv.x*(texs-1));
									int posuvinty = convert_int_rte(posuv.y*(texs-1));
									int texind = posuvinty*texs+posuvintx + vtri.texid*texs*texs;

									if ((drawdistance>0.001f)&&(drawdistance<imzbuf)) {
										imzbuf = drawdistance;
										ihebuf = eid;
										ihobuf = oid;
										ihtbuf = tid;
										itibuf = texind;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	int drawdistance = imzbuf;
	int eid = ihebuf;
	int oid = ihobuf;
	int tid = ihtbuf;
	int texind = itibuf;

	if (drawdistance<INFINITY) {
		float4 camray = camdir;

		triangle vtri;
		vtri.norm = (float4)(tri[tid*ts+12],tri[tid*ts+13],tri[tid*ts+14],tri[tid*ts+15]);
		vtri.facecolor = (float4)(tri[tid*ts+29],tri[tid*ts+30],tri[tid*ts+31],tri[tid*ts+32]);
		vtri.emissivecolor = (float4)(tri[tid*ts+33],tri[tid*ts+34],tri[tid*ts+35],tri[tid*ts+36]);
		vtri.lightmapcolor = (float4)(tri[tid*ts+37],tri[tid*ts+38],tri[tid*ts+39],tri[tid*ts+40]);
		vtri.metallic = tri[tid*ts+42];

		float rayangle = vectorangle(camray, vtri.norm);
		bool frontface = rayangle>=M_PI_2_F;

		ihe[0] = eid;
		iho[0] = oid;
		iht[0] = tid;
		iti[0] = texind;
		float4 texcolor = vtri.facecolor;
		if (texind>=0) {
			float4 texrgbaf = convert_float4(as_uchar4(tex[texind])) / 255.0f;
			texcolor = (float4)(texrgbaf.s2, texrgbaf.s1, texrgbaf.s0, texrgbaf.s3);
		}
		float4 pixelcolor = (float4)(0.0f);
		if (tlit) {
			pixelcolor = lm*vtri.emissivecolor + vtri.lightmapcolor*texcolor*(1.0f-vtri.metallic);
		} else {
			pixelcolor = lm*vtri.emissivecolor + texcolor;
		}
		if (!frontface) {pixelcolor = (float4)(0.0f,0.0f,0.0f,0.0f);}
		raycolordistpos.s0 = pixelcolor.s0;
		raycolordistpos.s1 = pixelcolor.s1;
		raycolordistpos.s2 = pixelcolor.s2;
		raycolordistpos.s3 = pixelcolor.s3;
		raycolordistpos.s4 = drawdistance;
	}
	
	return raycolordistpos;
}

kernel void movecamera(global float *cam, global float *cmv) {
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = radians((float2)(cam[12],cam[13]));
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
	float16 cammat = (float16)(cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22],cam[23],cam[24],cam[25],cam[26],cam[27],cam[28],cam[29],cam[30],cam[31]);
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
	cam[3] = camdirrot.x; cam[4] = camdirrot.y; cam[5] = camdirrot.z;
	cam[6] = camrightdirrot.x; cam[7] = camrightdirrot.y; cam[8] = camrightdirrot.z;
	cam[9] = camupdirrot.x; cam[10] = camupdirrot.y; cam[11] = camupdirrot.z;
	cam[16] = cammat.s0; cam[17] = cammat.s1; cam[18] = cammat.s2;
	cam[19] = cammat.s3; cam[20] = cammat.s4; cam[21] = cammat.s5;
	cam[22] = cammat.s6; cam[23] = cammat.s7; cam[24] = cammat.s8;
	cam[25] = cammat.s9; cam[26] = cammat.sA; cam[27] = cammat.sB;
	cam[28] = cammat.sC; cam[29] = cammat.sD; cam[30] = cammat.sE;
	cam[31] = cammat.sF;
}

kernel void clearview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global float *cam) {
	unsigned int xid = get_global_id(0);
	unsigned int vid = get_global_id(1);
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
	
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
		//ihe[pixelind] = -1;
		//iho[pixelind] = -1;
		//iht[pixelind] = -1;
	}
}

kernel void transformentity(global float *ttr, global float *otr, global float *etr, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent) {
	unsigned int eid = get_global_id(0);
	int objc = obc[0];
	int tric = trc[0];

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

	etr[eid*es+0] = vent.pos.x; etr[eid*es+1] = vent.pos.y; etr[eid*es+2] = vent.pos.z; etr[eid*es+3] = vent.pos.w;
	etr[eid*es+4] = vent.scale.x; etr[eid*es+5] = vent.scale.y; etr[eid*es+6] = vent.scale.z;
	etr[eid*es+7] = vent.rot.x; etr[eid*es+8] = vent.rot.y; etr[eid*es+9] = vent.rot.z;
	etr[eid*es+10] = entbvc.x; etr[eid*es+11] = entbvc.y; etr[eid*es+12] = entbvc.z; etr[eid*es+13] = entbvc.w;
	etr[eid*es+14] = vent.ind; etr[eid*es+15] = vent.len; etr[eid*es+16] = vent.phys;

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

		otr[oid*os+0] = vobj.pos.x; otr[oid*os+1] = vobj.pos.y; otr[oid*os+2] = vobj.pos.z; otr[oid*os+3] = vobj.pos.w;
		otr[oid*os+4] = vobj.scale.x; otr[oid*os+5] = vobj.scale.y; otr[oid*os+6] = vobj.scale.z;
		otr[oid*os+7] = vobj.rot.x; otr[oid*os+8] = vobj.rot.y; otr[oid*os+9] = vobj.rot.z;
		otr[oid*os+10] = objbvc.x; otr[oid*os+11] = objbvc.y; otr[oid*os+12] = objbvc.z; otr[oid*os+13] = objbvc.w;
		otr[oid*os+14] = vobj.ind; otr[oid*os+15] = vobj.len;

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
			vtri.prelit = (int)tri[tid*ts+45];
			
			float4 tripos1 = matrixposmult(vtri.pos1, objmat);
			float4 tripos2 = matrixposmult(vtri.pos2, objmat);
			float4 tripos3 = matrixposmult(vtri.pos3, objmat);
			float4 trinorm = matrixposmult(vtri.norm, objmat);
			tripos1 = translatepos(tripos1, objpos, 1.0f);
			tripos2 = translatepos(tripos2, objpos, 1.0f);
			tripos3 = translatepos(tripos3, objpos, 1.0f);

			ttr[tid*ts+0] = tripos1.x; ttr[tid*ts+1] = tripos1.y; ttr[tid*ts+2] = tripos1.z; ttr[tid*ts+3] = tripos1.w;
			ttr[tid*ts+4] = tripos2.x; ttr[tid*ts+5] = tripos2.y; ttr[tid*ts+6] = tripos2.z; ttr[tid*ts+7] = tripos2.w;
			ttr[tid*ts+8] = tripos3.x; ttr[tid*ts+9] = tripos3.y; ttr[tid*ts+10] = tripos3.z; ttr[tid*ts+11] = tripos3.w;
			ttr[tid*ts+12] = trinorm.x; ttr[tid*ts+13] = trinorm.y; ttr[tid*ts+14] = trinorm.z; ttr[tid*ts+15] = trinorm.w;
			ttr[tid*ts+16] = vtri.pos1uv.x; ttr[tid*ts+17] = vtri.pos1uv.y; ttr[tid*ts+18] = vtri.pos1uv.z; ttr[tid*ts+19] = vtri.pos1uv.w;
			ttr[tid*ts+20] = vtri.pos2uv.x; ttr[tid*ts+21] = vtri.pos2uv.y; ttr[tid*ts+22] = vtri.pos2uv.z; ttr[tid*ts+23] = vtri.pos2uv.w;
			ttr[tid*ts+24] = vtri.pos3uv.x; ttr[tid*ts+25] = vtri.pos3uv.y; ttr[tid*ts+26] = vtri.pos3uv.z; ttr[tid*ts+27] = vtri.pos3uv.w;
			ttr[tid*ts+28] = vtri.texid;
			ttr[tid*ts+29] = vtri.facecolor.s0; ttr[tid*ts+30] = vtri.facecolor.s1; ttr[tid*ts+31] = vtri.facecolor.s2; ttr[tid*ts+32] = vtri.facecolor.s3;
			ttr[tid*ts+33] = vtri.emissivecolor.s0; ttr[tid*ts+34] = vtri.emissivecolor.s1; ttr[tid*ts+35] = vtri.emissivecolor.s2; ttr[tid*ts+36] = vtri.emissivecolor.s3;
			ttr[tid*ts+37] = vtri.lightmapcolor.s0; ttr[tid*ts+38] = vtri.lightmapcolor.s1; ttr[tid*ts+39] = vtri.lightmapcolor.s2; ttr[tid*ts+40] = vtri.lightmapcolor.s3;
			ttr[tid*ts+41] = vtri.roughness;
			ttr[tid*ts+42] = vtri.metallic;
			ttr[tid*ts+43] = vtri.refractind;
			ttr[tid*ts+44] = vtri.opacity;
			ttr[tid*ts+45] = vtri.prelit;
		}
	}
}

kernel void physicscollision(global float *cam, global float *tli, global float *oli, global float *eli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global float *dts) {
	unsigned int eid = get_global_id(0);
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	int entc = enc[0];
	float deltatime = dts[0];

	ent[0] = campos.x; ent[1] = campos.y; ent[2] = campos.z;

	entity cent;
	cent.pos = (float4)(eli[eid*es+0],eli[eid*es+1],eli[eid*es+2],eli[eid*es+3]);
	cent.scale = (float3)(eli[eid*es+4],eli[eid*es+5],eli[eid*es+6]);
	cent.rot = (float3)(eli[eid*es+7],eli[eid*es+8],eli[eid*es+9]);
	cent.sph = (float4)(eli[eid*es+10],eli[eid*es+11],eli[eid*es+12],eli[eid*es+13]);
	cent.ind = (int)eli[eid*es+14];
	cent.len = (int)eli[eid*es+15];
	cent.phys = (int)eli[eid*es+16];
	if (cent.phys!=1) {return;}

	float4 entdir = (float4)(0.0f);

	for (int eix=0;eix<entc;eix++) {
		if (eix!=eid) {
			entity vent;
			vent.pos = (float4)(eli[eix*es+0],eli[eix*es+1],eli[eix*es+2],eli[eix*es+3]);
			vent.scale = (float3)(eli[eix*es+4],eli[eix*es+5],eli[eix*es+6]);
			vent.rot = (float3)(eli[eix*es+7],eli[eix*es+8],eli[eix*es+9]);
			vent.sph = (float4)(eli[eix*es+10],eli[eix*es+11],eli[eix*es+12],eli[eix*es+13]);
			vent.ind = (int)eli[eix*es+14];
			vent.len = (int)eli[eix*es+15];
			vent.phys = (int)eli[eix*es+16];

			if (vent.phys!=-1) {
				float sphdist = spherespheredistance(cent.sph, vent.sph);
				if (sphdist<0.0f) {
					float4 sphdir = normalize(cent.sph - vent.sph); sphdir.w = 0.0f;
					entdir += sphdir;
				}
			}
		}
	}

	float4 entpos = (float4)(ent[eid*es+0],ent[eid*es+1],ent[eid*es+2],ent[eid*es+3]);
	float4 enddirlim = entdir;
	float enddirlimlen = length(enddirlim);
	if (enddirlimlen>1.0f) {
		enddirlim /= enddirlimlen;
	}
	enddirlim = enddirlim * deltatime * 0.1f;
	entpos += enddirlim;
	ent[eid*es+0] = entpos.x; ent[eid*es+1] = entpos.y; ent[eid*es+2] = entpos.z; ent[eid*es+3] = entpos.w;
}

kernel void lightcopy(global float *tli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes) {
	unsigned int tid = get_global_id(0);
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
	vtri.prelit = (int)tri[tid*ts+45];

	tli[tid*ts+0] = vtri.pos1.x; tli[tid*ts+1] = vtri.pos1.y; tli[tid*ts+2] = vtri.pos1.z; tli[tid*ts+3] = vtri.pos1.w;
	tli[tid*ts+4] = vtri.pos2.x; tli[tid*ts+5] = vtri.pos2.y; tli[tid*ts+6] = vtri.pos2.z; tli[tid*ts+7] = vtri.pos2.w;
	tli[tid*ts+8] = vtri.pos3.x; tli[tid*ts+9] = vtri.pos3.y; tli[tid*ts+10] = vtri.pos3.z; tli[tid*ts+11] = vtri.pos3.w;
	tli[tid*ts+12] = vtri.norm.x; tli[tid*ts+13] = vtri.norm.y; tli[tid*ts+14] = vtri.norm.z; tli[tid*ts+15] = vtri.norm.w;
	tli[tid*ts+16] = vtri.pos1uv.x; tli[tid*ts+17] = vtri.pos1uv.y; tli[tid*ts+18] = vtri.pos1uv.z; tli[tid*ts+19] = vtri.pos1uv.w;
	tli[tid*ts+20] = vtri.pos2uv.x; tli[tid*ts+21] = vtri.pos2uv.y; tli[tid*ts+22] = vtri.pos2uv.z; tli[tid*ts+23] = vtri.pos2uv.w;
	tli[tid*ts+24] = vtri.pos3uv.x; tli[tid*ts+25] = vtri.pos3uv.y; tli[tid*ts+26] = vtri.pos3uv.z; tli[tid*ts+27] = vtri.pos3uv.w;
	tli[tid*ts+28] = vtri.texid;
	tli[tid*ts+29] = vtri.facecolor.s0; tli[tid*ts+30] = vtri.facecolor.s1; tli[tid*ts+31] = vtri.facecolor.s2; tli[tid*ts+32] = vtri.facecolor.s3;
	tli[tid*ts+33] = vtri.emissivecolor.s0; tli[tid*ts+34] = vtri.emissivecolor.s1; tli[tid*ts+35] = vtri.emissivecolor.s2; tli[tid*ts+36] = vtri.emissivecolor.s3;
	tli[tid*ts+41] = vtri.roughness;
	tli[tid*ts+42] = vtri.metallic;
	tli[tid*ts+43] = vtri.refractind;
	tli[tid*ts+44] = vtri.opacity;
	tli[tid*ts+45] = vtri.prelit;

	if (vtri.prelit==1) {
		tli[tid*ts+37] = vtri.lightmapcolor.s0; tli[tid*ts+38] = vtri.lightmapcolor.s1; tli[tid*ts+39] = vtri.lightmapcolor.s2; tli[tid*ts+40] = vtri.lightmapcolor.s3;
	}
}

kernel void lightentity(global float *tli, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes) {
	unsigned int tid = get_global_id(0);
	triangle vtri;
	vtri.pos1 = (float4)(tri[tid*ts+0],tri[tid*ts+1],tri[tid*ts+2],tri[tid*ts+3]);
	vtri.pos2 = (float4)(tri[tid*ts+4],tri[tid*ts+5],tri[tid*ts+6],tri[tid*ts+7]);
	vtri.pos3 = (float4)(tri[tid*ts+8],tri[tid*ts+9],tri[tid*ts+10],tri[tid*ts+11]);

	float4 centerpos = (vtri.pos1 + vtri.pos2 + vtri.pos3) / 3.0f;
	float tricam[32] = {centerpos.x,centerpos.y,centerpos.z, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 90.0f,90.0f,cs,cs, 1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	float tricam2[32] = {centerpos.x,centerpos.y,centerpos.z, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 90.0f,90.0f,cs,cs, 1.0f,0.0f,0.0f,0.0f, 0.0f,-1.0f,0.0f,0.0f, 0.0f,0.0f,-1.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	float tricam3[32] = {centerpos.x,centerpos.y,centerpos.z, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 90.0f,90.0f,cs,cs, 1.0f,0.0f,0.0f,0.0f, 0.0f,0.0f,-1.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	float tricam4[32] = {centerpos.x,centerpos.y,centerpos.z, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 90.0f,90.0f,cs,cs, 0.0f,0.0f,1.0f,0.0f, 1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	float tricam5[32] = {centerpos.x,centerpos.y,centerpos.z, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 90.0f,90.0f,cs,cs, -1.0f,0.0f,0.0f,0.0f, 0.0f,0.0f,1.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};
	float tricam6[32] = {centerpos.x,centerpos.y,centerpos.z, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 0.0f,0.0f,0.0f, 90.0f,90.0f,cs,cs, 0.0f,0.0f,-1.0f,0.0f, -1.0f,0.0f,0.0f,0.0f, 0.0f,1.0f,0.0f,0.0f, 0.0f,0.0f,0.0f,1.0f};

	int lit = 1, nor = 1, rsx = 1, rsy = 1, rsn = 0;
	int cmlen = cs*cs*6;

	float img[cs*cs*4];
	float imz[cs*cs];
	int hitid = -1;
	int ihe[cs*cs];
	int iho[cs*cs];
	int iht[cs*cs];
	int iti[cs*cs];
	float *cam = tricam;
	float4 lightmapcolor = (float4)(0.0f,0.0f,0.0f,1.0f);

	for (int i=0;i<6;i++) {
		if (i==1) {cam = tricam2;} else if (i==2) {cam = tricam3;} else if (i==3) {cam = tricam4;} else if (i==4) {cam = tricam5;} else if (i==5) {cam = tricam6;}
		for (int y=0;y<cs;y++) {for (int x=0;x<cs;x++) {
			int pind=y*cs+x; img[pind*4+0]=0.0f; img[pind*4+1]=0.0f; img[pind*4+2]=0.0f; img[pind*4+3]=0.0f; imz[pind]=INFINITY; ihe[pind]=-1; iho[pind]=-1; iht[pind]=-1; iti[pind]=-1;
		}}
		/*
		for (int y=0;y<cs;y++) {for (int x=0;x<cs;x++) {
			rayview(x, y, img, imz, &hitid, ihe, iho, iht, iti, cam, tri, trc, obj, obc, ent, enc, tex, tes, &lit, &nor, &rsx, &rsy, &rsn);
		}}
		*/
		for (int x=0;x<cs;x++) {
			planeview(x, 0, 1, img, imz, &hitid, ihe, iho, iht, iti, cam, tri, trc, obj, obc, ent, enc, tex, tes, &lit, &nor, &rsx, &rsy, &rsn);
		}
		for (int y=0;y<cs;y++) {for (int x=0;x<cs;x++) {
			bounceview(x, y, img, imz, &hitid, ihe, iho, iht, cam, tri, trc, obj, obc, ent, enc, tex, tes, &lit, &nor, &rsx, &rsy, &rsn);
		}}
		for (int y=0;y<cs;y++) {for (int x=0;x<cs;x++) {
			int pind=y*cs+x;lightmapcolor.s0+=img[pind*4+0]; lightmapcolor.s1+=img[pind*4+1]; lightmapcolor.s2+=img[pind*4+2];
		}}
	}

	lightmapcolor.s0 = lightmapcolor.s0 * ld/cmlen;
	lightmapcolor.s1 = lightmapcolor.s1 * ld/cmlen;
	lightmapcolor.s2 = lightmapcolor.s2 * ld/cmlen;

	tli[tid*ts+37] = lightmapcolor.s0; tli[tid*ts+38] = lightmapcolor.s1; tli[tid*ts+39] = lightmapcolor.s2; tli[tid*ts+40] = lightmapcolor.s3;
}

kernel void viewfilter(global float *imf, global float *img, global float *cam) {
	unsigned int xid = get_global_id(0);
	unsigned int yid = get_global_id(1);
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
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
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
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

kernel void renderrayview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global int *iti, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn) {
	unsigned int xid = get_global_id(0);
	unsigned int yid = get_global_id(1);
	rayview(xid, yid, img, imz, imh, ihe, iho, iht, iti, cam, tri, trc, obj, obc, ent, enc, tex, tes, lit, nor, rsx, rsy, rsn);
}
void rayview(int xid, int yid, float *img, float *imz, int *imh, int *ihe, int *iho, int *iht, int *iti, float *cam, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit, int *nor, int *rsx, int *rsy, int *rsn) {
	int rstepx = rsx[0];
	int rstepy = rsy[0];
	int rstepnum = rsn[0];
	int xidstep = xid % rstepx;
	int yidstep = yid % rstepy;
	int xstep = rstepnum % rstepx;
	int ystep = rstepnum / rstepx;
	if ((xidstep!=xstep)||(yidstep!=ystep)) {return;}

	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = radians((float2)(cam[12],cam[13]));
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
	float16 cammat = (float16)(cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22],cam[23],cam[24],cam[25],cam[26],cam[27],cam[28],cam[29],cam[30],cam[31]);
	int sphnor = nor[0];

	float campixelang = cw*(camfov.x/camres.x);

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
	int hiteid = -1, hitoid = -1, hittid = -1, hittexind = -1;
	float8 rayint = renderray(camray, campixelang, &hiteid, &hitoid, &hittid, &hittexind, tri, trc, obj, obc, ent, enc, tex, tes, lit);
	float4 raycolor = rayint.s0123;
	float raydist = rayint.s4;

	if (!isnan(raycolor.s0)) {
		float drawdistance = raydist;
		int pixelind = (camres.y-yid-1)*camres.x+xid;
		if (drawdistance<imz[pixelind]) {
			imz[pixelind] = drawdistance;
			ihe[pixelind] = hiteid;
			iho[pixelind] = hitoid;
			iht[pixelind] = hittid;
			iti[pixelind] = hittexind;
			if ((xid==camhalfres.x+xstep)&&(yid==camhalfres.y+ystep)) {imh[0] = hiteid;}
			if (sphnor) {raycolor.s012=raycolor.s012/raydirrotlen;}
			img[pixelind*4+0] = raycolor.s0;
			img[pixelind*4+1] = raycolor.s1;
			img[pixelind*4+2] = raycolor.s2;
			img[pixelind*4+3] = raycolor.s3;
		}
	}
}

kernel void bounceraysview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn) {
	unsigned int xid = get_global_id(0);
	unsigned int yid = get_global_id(1);
	bounceview(xid, yid, img, imz, imh, ihe, iho, iht, cam, tri, trc, obj, obc, ent, enc, tex, tes, lit, nor, rsx, rsy, rsn);
}
void bounceview(int xid, int yid, float *img, float *imz, int *imh, int *ihe, int *iho, int *iht, float *cam, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit, int *nor, int *rsx, int *rsy, int *rsn) {
	int rstepx = rsx[0];
	int rstepy = rsy[0];
	int rstepnum = rsn[0];
	int xidstep = xid % rstepx;
	int yidstep = yid % rstepy;
	int xstep = rstepnum % rstepx;
	int ystep = rstepnum / rstepx;
	if ((xidstep!=xstep)||(yidstep!=ystep)) {return;}

	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = radians((float2)(cam[12],cam[13]));
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
	float16 cammat = (float16)(cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22],cam[23],cam[24],cam[25],cam[26],cam[27],cam[28],cam[29],cam[30],cam[31]);
	int sphnor = nor[0];

	float campixelang = cw*(camfov.x/camres.x);

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

	int pixelind = (camres.y-yid-1)*camres.x+xid;
	int tid = iht[pixelind];

	if (tid>-1) {
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
		vtri.prelit = (int)tri[tid*ts+45];

		float4 triplane = planefromnormalatpos(vtri.pos1, vtri.norm);

		float rayangle = vectorangle(raydirrot, vtri.norm);
		bool frontface = rayangle>=M_PI_2_F;

		if (vtri.opacity<1.0f) {
			if (frontface) {
				float8 refractionray = planerefractionray(camray, triplane, 1.0f, vtri.refractind);

				int eid2 = -1, oid2 = -1, tid2 = -1, texind2 = -1;
				float8 rayint = renderray(refractionray, campixelang, &eid2, &oid2, &tid2, &texind2, tri, trc, obj, obc, ent, enc, tex, tes, lit);
				float4 raycolor = rayint.s0123;
				float raydist = rayint.s4;

				if (!isnan(raycolor.s0)) {
					if (tid2>-1) {

						triangle vtri2;
						vtri2.pos1 = (float4)(tri[tid2*ts+0],tri[tid2*ts+1],tri[tid2*ts+2],tri[tid2*ts+3]);
						vtri2.pos2 = (float4)(tri[tid2*ts+4],tri[tid2*ts+5],tri[tid2*ts+6],tri[tid2*ts+7]);
						vtri2.pos3 = (float4)(tri[tid2*ts+8],tri[tid2*ts+9],tri[tid2*ts+10],tri[tid2*ts+11]);
						vtri2.norm = (float4)(tri[tid2*ts+12],tri[tid2*ts+13],tri[tid2*ts+14],tri[tid2*ts+15]);
						vtri2.pos1uv = (float4)(tri[tid2*ts+16],tri[tid2*ts+17],tri[tid2*ts+18],tri[tid2*ts+19]);
						vtri2.pos2uv = (float4)(tri[tid2*ts+20],tri[tid2*ts+21],tri[tid2*ts+22],tri[tid2*ts+23]);
						vtri2.pos3uv = (float4)(tri[tid2*ts+24],tri[tid2*ts+25],tri[tid2*ts+26],tri[tid2*ts+27]);
						vtri2.texid = (int)tri[tid2*ts+28];
						vtri2.facecolor = (float4)(tri[tid2*ts+29],tri[tid2*ts+30],tri[tid2*ts+31],tri[tid2*ts+32]);
						vtri2.emissivecolor = (float4)(tri[tid2*ts+33],tri[tid2*ts+34],tri[tid2*ts+35],tri[tid2*ts+36]);
						vtri2.lightmapcolor = (float4)(tri[tid2*ts+37],tri[tid2*ts+38],tri[tid2*ts+39],tri[tid2*ts+40]);
						vtri2.roughness = tri[tid2*ts+41];
						vtri2.metallic = tri[tid2*ts+42];
						vtri2.refractind = tri[tid2*ts+43];
						vtri2.opacity = tri[tid2*ts+44];
						vtri2.prelit = (int)tri[tid2*ts+45];

						float4 triplane2 = planefromnormalatpos(vtri2.pos1, vtri2.norm);

						if (vtri2.opacity<1.0f) {
							float8 refractionray2 = planerefractionray(refractionray, triplane2, vtri.refractind, 1.0f);

							int hiteid = -1, hitoid = -1, hittid = -1, hittexind = -1;
							float8 rayint2 = renderray(refractionray2, campixelang, &hiteid, &hitoid, &hittid, &hittexind, tri, trc, obj, obc, ent, enc, tex, tes, lit);
							float4 raycolor2 = rayint2.s0123;
							float raydist2 = rayint2.s4;

							if (!isnan(raycolor2.s0)) {
								float4 pixelcolor = (float4)(img[pixelind*4+0],img[pixelind*4+1],img[pixelind*4+2],img[pixelind*4+3]);
								float4 newpixelcolor = sourcemixblend(pixelcolor, raycolor2, 1.0f-vtri.opacity);
								img[pixelind*4+0] = newpixelcolor.s0;
								img[pixelind*4+1] = newpixelcolor.s1;
								img[pixelind*4+2] = newpixelcolor.s2;
								img[pixelind*4+3] = newpixelcolor.s3;
							}
						}
					}
				}
			}
		}

		if (vtri.roughness<1.0f) {
			float8 reflectionray = planereflectionray(camray, triplane);
			int hiteid = -1, hitoid = -1, hittid = -1, hittexind = -1;
			float8 rayint = renderray(reflectionray, 0.0f, &hiteid, &hitoid, &hittid, &hittexind, tri, trc, obj, obc, ent, enc, tex, tes, lit);
			float4 raycolor = rayint.s0123;
			float raydist = rayint.s4;

			if (!isnan(raycolor.s0)) {
				float4 pixelcolor = (float4)(img[pixelind*4+0],img[pixelind*4+1],img[pixelind*4+2],img[pixelind*4+3]);
				float4 newpixelcolor = sourcemixblend(pixelcolor, raycolor, 1.0f-vtri.roughness);
				img[pixelind*4+0] = newpixelcolor.s0;
				img[pixelind*4+1] = newpixelcolor.s1;
				img[pixelind*4+2] = newpixelcolor.s2;
				img[pixelind*4+3] = newpixelcolor.s3;
			}
		}
	}
}

kernel void renderplaneview(global float *img, global float *imz, global int *imh, global int *ihe, global int *iho, global int *iht, global int *iti, global float *cam, global float *tri, global int *trc, global float *obj, global int *obc, global float *ent, global int *enc, global int *tex, global int *tes, global int *lit, global int *nor, global int *rsx, global int *rsy, global int *rsn) {
	unsigned int xid = get_global_id(0);
	unsigned int vid = get_global_id(1);
	int vst = vs;
	planeview(xid, vid, vst, img, imz, imh, ihe, iho, iht, iti, cam, tri, trc, obj, obc, ent, enc, tex, tes, lit, nor, rsx, rsy, rsn);
}
void planeview(int xid, int vid, int vst, float *img, float *imz, int *imh, int *ihe, int *iho, int *iht, int *iti, float *cam, float *tri, int *trc, float *obj, int *obc, float *ent, int *enc, int *tex, int *tes, int *lit, int *nor, int *rsx, int *rsy, int *rsn) {
	const float4 camposzero = (float4)(0.0f,0.0f,0.0f,0.0f);

	int entc = enc[0];
	int objc = obc[0];
	int tric = trc[0];

	int rstepx = rsx[0];
	int rstepy = rsy[0];
	int rstepnum = rsn[0];
	int xidstep = xid % rstepx;
	int xstep = rstepnum % rstepx;
	int ystep = rstepnum / rstepx;
	if (xidstep!=xstep) {return;}
	
	float4 campos = (float4)(cam[0],cam[1],cam[2],0.0f);
	float2 camfov = radians((float2)(cam[12],cam[13]));
	int2 camres = (int2)((int)cam[14],(int)cam[15]);
	float16 cammat = (float16)(cam[16],cam[17],cam[18],cam[19],cam[20],cam[21],cam[22],cam[23],cam[24],cam[25],cam[26],cam[27],cam[28],cam[29],cam[30],cam[31]);
	int texs = tes[0];
	int tlit = lit[0];
	int sphnor = nor[0];

	float campixelang = cw*(camfov.x/camres.x);

	float imzbuf[zs];
	int ihebuf[zs];
	int ihobuf[zs];
	int ihtbuf[zs];
	int itibuf[zs];
	for (int i=0;i<zs;i++) {
		imzbuf[i] = INFINITY;
		ihebuf[i] = -1;
		ihobuf[i] = -1;
		ihtbuf[i] = -1;
		itibuf[i] = -1;
	}

	int camresystep = camres.y / vst;
	float2 camhalffov = camfov/2.0f;
	float2 camhalffovlen = (float2)(tan(camhalffov.x), tan(camhalffov.y));
	int2 camhalfres = camres/2;
	float camcollenx = -camhalffovlen.x + (camhalffovlen.x/(camhalfres.x-0.5f))*xid;

	float4 camdir = (float4)(0.0f,0.0f,-1.0f,0.0f);
	float4 camrightdir = (float4)(1.0f,0.0f,0.0f,0.0f);
	float4 camupdir = (float4)(0.0f,-1.0f,0.0f,0.0f);
	float4 coldir = (float4)(0.0f,camcollenx,-1.0f,0.0f);
	float4 colupdir = (float4)(camcollenx,-camhalffovlen.y,-1.0f,0.0f);
	float4 coldowndir = (float4)(camcollenx,camhalffovlen.y,-1.0f,0.0f);
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

	int campresystind = camresystep*vid;
	int campresyendind = camresystep*vid + camresystep-1;

	for (int eid=0;eid<entc;eid++) {
		entity vent;
		vent.sph = (float4)(ent[eid*es+10],ent[eid*es+11],ent[eid*es+12],ent[eid*es+13]);
		vent.ind = (int)ent[eid*es+14];
		vent.len = (int)ent[eid*es+15];

		float4 ventsphvec = vent.sph-campos;
		float ventsphdist = length(ventsphvec);
		float ventang = asin(vent.sph.w/ventsphdist);

		if (ventang>=campixelang) {

			float eppdist = planepointdistance(vent.sph, colplane);
			if (fabs(eppdist)<=vent.sph.w) {

				for (int oid=vent.ind;oid<(vent.ind+vent.len);oid++) {
					object vobj;
					vobj.sph = (float4)(obj[oid*os+10],obj[oid*os+11],obj[oid*os+12],obj[oid*os+13]);
					vobj.ind = (int)obj[oid*os+14];
					vobj.len = (int)obj[oid*os+15];

					float4 vobjsphvec = vobj.sph-campos;
					float vobjsphdist = length(vobjsphvec);
					float vobjang = asin(vobj.sph.w/vobjsphdist);

					if (vobjang>=campixelang) {

						float oppdist = planepointdistance(vobj.sph, colplane);
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

								float4 triplane = planefromnormalatpos(vtri.pos1, vtri.norm);

								float16 intline = planetriangleintersection(colplane, &vtri);
								float4 colpos1 = intline.s01234567.s0123;
								float4 colpos2 = intline.s01234567.s4567;
								float4 colpos1uv = intline.s89abcdef.s0123;
								float4 colpos2uv = intline.s89abcdef.s4567;

								if (!isnan(colpos1.x)) {
									float fwdintpointsdist1 = planepointdistance(colpos1, camdirplane);
									float fwdintpointsdist2 = planepointdistance(colpos2, camdirplane);
									float upintpointsdist1 = planepointdistance(colpos1, camupdirplane);
									float upintpointsdist2 = planepointdistance(colpos2, camupdirplane);

									if ((fwdintpointsdist1>=0.001f)||(fwdintpointsdist2>=0.001f)) {
										if ((fwdintpointsdist1<0.001f)||(fwdintpointsdist2<0.001f)) {
											float4 drawlinedir12 = colpos2-colpos1;
											float drawlinedir12dist = rayplanedistance(colpos1, drawlinedir12, rendercutplane);
											float4 drawlinepos3 = translatepos(colpos1, drawlinedir12, drawlinedir12dist);
											float fwdintpointsdist3 = planepointdistance(drawlinepos3, camdirplane);
											float upintpointsdist3 = planepointdistance(drawlinepos3, camupdirplane);
											float4 drawlinetexdir12 = colpos2uv - colpos1uv;
											float4 drawlinepos3uv = translatepos(colpos1uv, drawlinetexdir12, drawlinedir12dist);
											if (fwdintpointsdist1>=0.001f) {
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
											if (py1<0) {py1=0;} if (py1>=camres.y) {py1=camres.y-1;}
											if (py2<0) {py2=0;} if (py2>=camres.y) {py2=camres.y-1;}
											int py1s = py1;
											int py2s = py2;
											if (py1>py2) {
												py1s = py2; py2s = py1;
												float4 vpixelpointtemp = vpixelpointd1; vpixelpointd1 = vpixelpointd2; vpixelpointd2 = vpixelpointtemp;
												float vpixelyangtemp = vpixelyang1; vpixelyang1 = vpixelyang2; vpixelyang2 = vpixelyangtemp;
												float4 colpostemp = colpos1; colpos1 = colpos2; colpos2 = colpostemp;
												float4 colposuvtemp = colpos1uv; colpos1uv = colpos2uv; colpos2uv = colposuvtemp;
											}

											int campresystart = campresystind;
											int campresyend = campresyendind;
											if (py1s>campresystart) {campresystart=py1s;}
											if (py2s<campresyend) {campresyend=py2s;}

											float4 vpixelpointdir12 = colpos2 - colpos1;
											for (int y=campresystart;y<=campresyend;y++) {
												float camcolleny = -camhalffovlen.y + (camhalffovlen.y/(camhalfres.y-0.5f))*y;
												float4 raydir = (float4)(camcollenx,-camcolleny,-1.0f,0.0f);
												float4 raydirrot = matrixposmult(raydir, cammat);
												float raydirrotlen = length(raydirrot);
												float verticalangle = atan(camcolleny);
												float vpixelcampointangle = verticalangle - vpixelyang1;
												float8 vpixelpointdline = (float8)(0.0f);
												vpixelpointdline.s0123 = vpixelpointd1;
												vpixelpointdline.s4567 = vpixelpointd2;
												float vpixelpointlenfrac = linearanglelengthinterpolation(camposzero, vpixelpointdline, vpixelcampointangle);
												float4 linepoint = translatepos(colpos1, vpixelpointdir12, vpixelpointlenfrac);
												float4 camray = linepoint - campos;
												float drawdistance = length(camray);

												float4 vpixelpointdir12uv = colpos2uv - colpos1uv;
												float4 lineuvpos = translatepos(colpos1uv, vpixelpointdir12uv, vpixelpointlenfrac);
												float2 lineuv = (float2)(lineuvpos.x-floor(lineuvpos.x), lineuvpos.y-floor(lineuvpos.y));
												int lineuvx = convert_int_rte(lineuv.x*(texs-1));
												int lineuvy = convert_int_rte(lineuv.y*(texs-1));
												int texind = lineuvy*texs+lineuvx + vtri.texid*texs*texs;

												int pixelind = (camres.y-y-1)*camres.x+xid;
												int pixely = y-campresystind;
												if (drawdistance<imzbuf[pixely]) {
													imzbuf[pixely] = drawdistance;
													ihebuf[pixely] = eid;
													ihobuf[pixely] = oid;
													ihtbuf[pixely] = tid;
													itibuf[pixely] = texind;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	for (int y=campresystind;y<=campresyendind;y++) {
		int pixelind = (camres.y-y-1)*camres.x+xid;
		int pixely = y-campresystind;

		float camcolleny = -camhalffovlen.y + (camhalffovlen.y/(camhalfres.y-0.5f))*y;
		float4 raydir = (float4)(camcollenx,-camcolleny,-1.0f,0.0f);
		float4 raydirrot = matrixposmult(raydir, cammat);
		float raydirrotlen = length(raydirrot);
		float4 camray = raydirrot;

		float drawdistance = imzbuf[pixely];
		int eid = ihebuf[pixely];
		int oid = ihobuf[pixely];
		int tid = ihtbuf[pixely];
		int texind = itibuf[pixely];

		if (drawdistance<INFINITY) {
			triangle vtri;
			vtri.norm = (float4)(tri[tid*ts+12],tri[tid*ts+13],tri[tid*ts+14],tri[tid*ts+15]);
			vtri.facecolor = (float4)(tri[tid*ts+29],tri[tid*ts+30],tri[tid*ts+31],tri[tid*ts+32]);
			vtri.emissivecolor = (float4)(tri[tid*ts+33],tri[tid*ts+34],tri[tid*ts+35],tri[tid*ts+36]);
			vtri.lightmapcolor = (float4)(tri[tid*ts+37],tri[tid*ts+38],tri[tid*ts+39],tri[tid*ts+40]);
			vtri.metallic = tri[tid*ts+42];

			float rayangle = vectorangle(camray, vtri.norm);
			bool frontface = rayangle>=M_PI_2_F;

			if (drawdistance<imz[pixelind]) {
				imz[pixelind] = drawdistance;
				ihe[pixelind] = eid;
				iho[pixelind] = oid;
				iht[pixelind] = tid;
				iti[pixelind] = texind;
				if ((xid==camhalfres.x+xstep)&&(y==camhalfres.y)) {imh[0] = eid;}
				float4 texcolor = vtri.facecolor;
				if (texind>=0) {
					float4 texrgbaf = convert_float4(as_uchar4(tex[texind])) / 255.0f;
					texcolor = (float4)(texrgbaf.s2, texrgbaf.s1, texrgbaf.s0, texrgbaf.s3);
				}
				float4 pixelcolor = (float4)(0.0f);
				if (tlit) {
					pixelcolor = lm*vtri.emissivecolor + vtri.lightmapcolor*texcolor*(1.0f-vtri.metallic);
				} else {
					pixelcolor = lm*vtri.emissivecolor + texcolor;
				}
				if (sphnor) {pixelcolor.s012=pixelcolor.s012/raydirrotlen;}
				if (!frontface) {pixelcolor = (float4)(0.0f,0.0f,0.0f,0.0f);}
				img[pixelind*4+0] = pixelcolor.s0;
				img[pixelind*4+1] = pixelcolor.s1;
				img[pixelind*4+2] = pixelcolor.s2;
				img[pixelind*4+3] = pixelcolor.s3;
			}
		}
	}
}
