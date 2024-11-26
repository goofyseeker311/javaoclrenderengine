# Java OpenCL Render Engine

Java LWJGL OpenCL vectorized cpu/gpu ray traced raster render engine.

Draws OpenCL image buffer to screen using OpenGL shared context interoperation or CPU buffer copy.

![progress19](https://github.com/user-attachments/assets/cbb1debd-647d-4349-8250-ec0242e89444)

Input:
----------------
```
W          - forward
S          - backward
A          - strafe left
D          - strafe right
SPACE      - strafe up
SHIFT      - strafe down
Q          - roll left
E          - roll right
MOUSE-X    - look horizontal
MOUSE-Y    - look vertical
MOUSE-LMB  - fire weapon
```

Compiling:
----------------

LWJGL/OpenCL 3.3.4: https://www.lwjgl.org/customize

Eclipse IDE for Java Developers: https://www.eclipse.org/downloads/packages/

Insert base lwjgl, opencl, opengl, glfw and openal platform native jar packages as user library and in runnable jar export.

Running:
----------------

Java 23 JDK: https://www.oracle.com/java/technologies/downloads/

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
  - OpenCL api functions and direct context buffer sharing require OpenCL C 1.2 with OpenGL interoperation support from the OpenCL device. Falls back to CPU buffer copy if OpenCL device does not support context sharing.
