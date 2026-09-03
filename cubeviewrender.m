close all; clear; output_precision(16);

function vector = normalizevector(v)
  vector = v / vecnorm(v);
endfunction

function plane = planefromnormalatpoint(normal,point)
  plane = [normal -dot(normal,point)];
endfunction

v = [4 2 1];
vn = normalizevector(v);
p = [-1 2 7];
pl = planefromnormalatpoint(vn,p);
pt = pl * [p 1]';

