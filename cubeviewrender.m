close all; clear; output_precision(16);

function vec = normalizevector(vector)
  vec = vector / vecnorm(vector);
endfunction
function plane = planefromnormalatpoint(normal,point)
  plane = [normal -dot(normal,point)];
endfunction
function distance = planepointdistance(plane,point)
  distance = dot(plane,[point 1]);
endfunction
function distance = rayplanedistance(plane,point,direction)
  distance = -dot(plane,[point 1]) / dot(plane(1:3),direction);
endfunction

v = [4 2 1];
vn = normalizevector(v);
p = [-1 2 7];
pl = planefromnormalatpoint(vn,p);
pt = pl * [p 1]';
s = [-3 2 7 5];
d = planepointdistance(pl,s(1:3));
h = abs(d)<s(4);

c = [0 0 0];
cd = [1 0 0];
cdn = normalizevector(cd);
cpl = planefromnormalatpoint(cdn,c);
p1 = [-1 2 -3];
p2 = [1 2 -3];
p3 = [1 2 3];
v12 = p2 - p1;
v13 = p3 - p1;
cr12 = rayplanedistance(cpl,p1,v12);
cr13 = rayplanedistance(cpl,p1,v13);
c12 = p1 + cr12 * v12;
c13 = p1 + cr13 * v13;

