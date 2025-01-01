# Java OpenCL Render Engine

Java LWJGL OpenCL vectorized cpu/gpu/fpga ray traced raster render engine.

Draws OpenCL image buffer to screen using OpenGL shared context interoperation or CPU buffer copy.

![progress66](https://github.com/user-attachments/assets/9cc4438d-b1cd-4337-8507-a99fa86cbd45)
![progress66a](https://github.com/user-attachments/assets/6ec6cf6e-16d6-4a6c-80b4-a0bd1f8344d5)
![progress66b](https://github.com/user-attachments/assets/b51ed509-de06-4111-8439-4bb1e73c7ac4)
![progress66c](https://github.com/user-attachments/assets/b80652b8-d431-4da8-9c66-ea1cd5d75185)
![progress66d](https://github.com/user-attachments/assets/3006f77b-7d82-45fc-b234-78d81cd9e08c)

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
