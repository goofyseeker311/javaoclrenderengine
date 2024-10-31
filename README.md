# Java OpenCL Render Engine

Java LWJGL OpenCL vectorized cpu/gpu ray traced raster render engine. Uses OpenGL to draw OpenCL cpu image buffer to primary display.

Compiling:
----------------

LWJGL/OpenCL 3.3.4: https://www.lwjgl.org/customize

Eclipse IDE for Java Developers: https://www.eclipse.org/downloads/packages/

Insert base lwjgl, opencl, opengl and glfw platform native jar packages as user library and in runnable jar export.

Running:
----------------

Java 23 JDK: https://www.oracle.com/java/technologies/downloads/

Double click the javaoclrendergine.jar file to run the application directly.

Console run command to have extra debug output information:
```
java -jar javaoclrendergine.jar <device-index=0>
```

CPU OpenCL:
----------------
Enabling CPU as Windows OpenCL device:

install w_opencl_runtime_p_2024.2.0.980.exe:

https://www.intel.com/content/www/us/en/developer/articles/technical/intel-cpu-runtime-for-opencl-applications-with-sycl-support.html
