close all; clear; output_precision(16);

function vector = normalizevector(v)
  vector = v / vecnorm(v);
endfunction

function plane = planefromnormalatpoint(normal,point)
  plane = [normal -dot(normal,point)];
endfunction

function distance = planepointdistance(plane,point)
  distance = dot(plane,[point 1]);
endfunction

v = [4 2 1];
vn = normalizevector(v);
p = [-1 2 7];
pl = planefromnormalatpoint(vn,p);
pt = pl * [p 1]';
s = [-3 2 7 5];
d = planepointdistance(pl,s(1:3));
h = abs(d)<s(4);

