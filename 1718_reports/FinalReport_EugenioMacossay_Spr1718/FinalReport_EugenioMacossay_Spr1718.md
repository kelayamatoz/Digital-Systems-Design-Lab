# EE109 Digital System Lab Final Report
Eugenio Javier Macossay-Hernández

## Table of Contents
- Application Overview
- Software Simulation
- Hardware Implementation
- Design Tradeoffs
- Appendix

## Application Overview
This application accelerates the hardware that provides a basis for simple autonomous driving. However, due to time constraints, the application focuses solely on accelerating the computation of centroids, which is the most computationally expensive component of a simplified autonomous driving project. The physical components and the robotics are not included in this project.

For this system to work, we assume a world where a vehicle is modeled by two markers of different colors. Each color, different from the background, has a centroid in the image as a whole; the centroid is continuously found (in the case of a finished project) in order to track the vehicle. Once the direction and position of the vehicle are resolved, a simple FSM can determine the next movement of the vehicle.

Tracking the centroid of an image for a specific color requires having three numbers: the number of pixels that contain that color, the running sum of  x-coordinates of every pixel that contains that color, and the running sum of y-coordinates of every pixel that contains that color. When a pixel is green, for example, we add the current x- and y-coordinates to a running sum of these, and we add 1 to the count of the number of pixels that are of that color.

To find the centroid for a frame, we use the following formulas:

X-Centroid: Sum of X-Coordinates / Count of Pixels
Y-Centroid: Sum of Y-Coordinates / Count of Pixels

## Software Simulation 
This application  was first run in MATLAB to verify that this method of calculating the centroid provided accurate results. The MATLAB code goes through every pixel, determines whether the intensity of a color is above a certain threshold, and keeps a running sum as specified before. To process a single image, find a centroid, and format in the appropriate way, this takes ~25 lines of MATLAB Code. As we will see, this step is similar in Spatial, although the loading/preprocessing in Spatial takes up more code.

## Hardware Implementation
The Spatial application functions in the same way as the Software simulation. However, this is implemented in Hardware to accelerate the runtime. The main part of the application is a reduce where we loop through the picture as in the software simulation. Most of the implementation itself is more complex due to the structure of Spatial. This application takes ~100 lines of code. 


## Design Tradeoffs
For the software simulation, we used digitally-generated images (on Powerpoint) with two different shapes of different colors. However, as soon as we moved into real pictures, we ran into a couple of problems. First, we needed the difference in color to be sharp. A black background, for example, was not useful, as taking pictures of it meant there was a lot of reflection and uneven colors in the background. A white background solved this. Additionally, a high resolution camera means that there is too much data to process. Therefore, to process data quickly, we decimate the images and take away some accuracy from the application.

In terms of code, the Spatial code would be more readable if there was a reduce for each data point that we are calculating (redX, redY, blueX, blueY). However, although this implementation is simpler, it is extremely inefficient. We use a struct to pack information together and do all computation for a single color at once. 

Finally, data had to be preprocessed in MATLAB since no real camera is connected to the FPGA. The pictures are flattened after being decimated, and then the data for the pictures was concatenated for each picture.Additionally, processing the colors can be parallelized, so a single function was written and called in parallel in Spatial. 

## Appendix
I loved the class in the sense that I got to put a lot into practice. However, the lectures could involve each of us writing code while it is being explained. That could help (maybe pilot that?)

I am also quite sad that I was by myself for the project. I wanted to do something that felt more of a complete project, but unfortunately that is the way life is.

Having said that, I am extremely grateful for how great both of you were. You were always supportive and attentive to my needs and capacities. I have never had another class like this.
