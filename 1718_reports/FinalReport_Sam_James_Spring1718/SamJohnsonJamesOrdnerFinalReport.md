# EE109 Digital System Lab Final Report
Sam Johnson, James Ordner

## Table of Contents
- What is Pathtracing?
- Software Simulation
- Hardware Implementation
- Design Tradeoffs
- Appendix

## Application Overview
Pathtracing is a graphics rendering technique based on the simpler technique of Raytracing. Raytracing involves projecting a ray from a camera to an object and then calculating the brightness and color of the pixel based on the interaction of the ray with objects and light sources in the scene. This is shown in the figure below.
![Raytracing](https://upload.wikimedia.org/wikipedia/commons/thumb/8/83/Ray_trace_diagram.svg/1280px-Ray_trace_diagram.svg.png)

Pathtracing expands on this technique by recognizing that in the real world, objects reflect light and therefore the color of a point depends not only on its interactions with light sources but with light bouncing off other surfaces in the scene as well.

Pathtracing uses recursion and Monte-Carlo sampling to ensure that the paths taken by the rays once they leave the camera accurately reflect the scene they interact with.



## Software Simulation 

We algorithmically demonstrated our application in Spatial using the Scala simulator in order to both accurately debug the algorithm with code that could be immediately translated into hardware. In addition to utilizing Spatial's simulation environment, we looked at example demos from CUDA in order to understand the pathtracing algorithm and how it can be implemented on GPUs.

We also used [Blender](https://en.wikipedia.org/wiki/Blender_(software)) in order to create the scene and verify that the output of the simulations matched what we created using existing software. This ensured that our algorithm was actually producing the exact results desired rather than just something visually appealing.

## Hardware Implementation
Our design is split into three layers: Controller, Pathtracer, and Raytracer according to the following block diagram:
![Block Diagram](https://i.imgur.com/9cO4vqc.jpg)

### Controller
The controller is responsible for input and output on the FPGA side and is designed to ensure that the pathtracer gets the inputs it needs in order to accurately run the rendering. This includes calculating the pixel's RayVector (the direction from the camera that corresponds to each pixel), pass the triangle information to the pathtracing unit, and parallelize the operation so that we can leverage the FPGAs entire hardware.

## Pathtracer
The Pathtracer takes in a pixel's origin and direction and the triangles in the scene and returns the color of the pixel. It works through an FSM that recursively traces a ray until it either bounces a set number of times or it misses the scene entirely. Each bounce, the pathtracer calculates the color and brightness of the surface it hit and adds that to the current value in order to generate the completed pixel. It calls Raytracer both to find the next hit, as well as to calculate the brightness of the hit based on whether the point is in shadow or not.

## Raytracer
The Raytracer is a low-level method that takes in a ray's origin and vector and returns the point and surface where it hits. It does this by calculating the intersection and distance for each triangle and choosing the closest intersection point that is a valid hit. 



## Design Tradeoffs
The primary tradeoffs we were forced to make were with regards to the parallelization. We didn't have enough time to fully explore the parallelization possibilities, or to fully optimize the code. We are able to run multiple pathtracing units at once, but things like detailed parallelization within RayTrace or CalculateShadow are not implemented. Also not implemented, but a great source of optimization would be to calculate each pixel multiple times in parallel, thus greatly speeding up the quality improvement of the picture. This comes at a direct cost of framerate, so we would want to balance between having a good framerate and having significant cleaning of the image with each write to the framebuffer.

This brings us to our second main design tradeoff. We weren't able to get the framebuffer working because we ran into an unexpectely large number of generated C++ issues which prevented us from synthesizing our design on hardware. We tried several workarounds suggested by Tian to overcome the limitations of Spatial support for structs, including reading in the triangle data from precomputed CSV files, but despite the hardware portion of the design sucessfully sythesizing, we were unable to get the C++ code to compile and thus were unable to synthesize the full design. Had we had more time to work on the FPGAs, we would have implemented the framebuffer and been able to see the output on the displays.


## Appendix
Progression of our results demonstrating first Raytracing and then Pathtracing in Scala simulation to demonstrate algorithmic accuracy.

Basic Raytracing showing just the color of the first object interacted with by the rays in the scene.
![Raytracing](https://i.imgur.com/sf1j9dn.png)


Animation demonstrating the simulated performance of our algorithm over 50 iterations. Note how the sides of the cube clean up and become lightly colored with the blue and red base below them.
![Pathtracing Animation](https://media.giphy.com/media/8vXvC8Ke8UlpiPbkbl/giphy.gif)

Simple 1 sample render of the scene demonstrating rendering reading traingle information from a CSV file into DRAM (no scene information was present in the Spatial files).

![DRAM Render](https://i.imgur.com/Oy8i8sN.png)

## Acknowledgments
We would like to thank our TA Tian Zhao for his help debugging and supporting our app within the Spatial framework. Without his assistence, we would never have been able to progress as far as we did with this project.
Additional thanks to Professor Kunle Olukotun for his guidance during the project and for leading an interesting course focused on the cutting edge of digital design.
