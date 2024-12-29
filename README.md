# Java OpenCL Render Engine

Java LWJGL OpenCL vectorized cpu/gpu/fpga ray traced raster render engine.

Draws OpenCL image buffer to screen using OpenGL shared context interoperation or CPU buffer copy.

![progress62](https://github.com/user-attachments/assets/c0cb0529-f42a-4a0f-bab8-7022b810a404)
![progress62a](https://github.com/user-attachments/assets/8601da67-7573-4429-a6bf-239536b84471)
![progress62b](https://github.com/user-attachments/assets/221fe092-03e1-4541-94a1-127c9e4c7316)
![progress62c](https://github.com/user-attachments/assets/2b28e024-ca5c-4e58-a55f-80839cd41161)
![progress62d](https://github.com/user-attachments/assets/d101f547-fdde-49c7-b2aa-8010fe866e15)
![progress62e](https://github.com/user-attachments/assets/8e4e5368-43d1-442f-a70f-94602ed782b5)

Input:
----------------
```
W          - forward
S          - backward
A          - strafe left
D          - strafe right
SPACE      - strafe up
LSHIFT     - strafe down
LCTRL      - fast speed
Q          - roll left
E          - roll right
MOUSE-X    - look horizontal
MOUSE-Y    - look vertical
MOUSE-LMB  - fire weapon
ENTER      - update lighting
```

Compiling:
----------------

LWJGL/OpenCL 3.3.4 or later: https://www.lwjgl.org/customize

Eclipse IDE for Java Developers: https://www.eclipse.org/downloads/packages/

Insert base lwjgl, opencl, opengl, glfw and openal platform native jar packages as user library and in runnable jar export.

Running:
----------------

Java SE 8 JDK or later: https://www.oracle.com/java/technologies/downloads/

Double click the javaoclrendergine.jar file to run the application directly.

Console run command to have extra debug output information:
```
java -jar javaoclrendergine.jar <device-index=0> <full-screen=1> <gl-interop=1>
```

OpenCL:
----------------
- Enabling CPU as Windows OpenCL device:
  - install w_opencl_runtime_p_2025.0.0.1166.exe: https://www.intel.com/content/www/us/en/developer/articles/technical/intel-cpu-runtime-for-opencl-applications-with-sycl-support.html

- Enabling CPU/GPU as Debian OpenCL device:
  - sudo apt install ocl-icd-opencl-dev
  - setup intel oneapi debian apt repo: https://www.intel.com/content/www/us/en/docs/oneapi/installation-guide-linux/2025-0/apt-005.html
  - sudo apt install intel-oneapi-runtime-opencl

- Features support:
  - OpenCL api functions and direct context buffer sharing require OpenCL C 1.2 (2.0 for global pointer conversion) with OpenGL interoperation support from the OpenCL device. Falls back to CPU buffer copy if OpenCL device does not support context sharing.
