# Laboratory Exercise 3: Design Algorithm Specific Hardwares Using Spatial
In the last two labs, we went through a few examples of how to use Spatial. Our major focus was to get you familiarized with the basic functionalities of Spatial. In this lab, we would like to help you design and implement algorithms that are widely used in the real world. The goal is to provide you with examples that organize all the component we have learnt so far, and prepare you ready for the final project.

Compared to the last two labs, in this lab we will focus more on the algorithm side of the task instead of on the details of your implementation. We will first describe how the algorithm operates. Then it is up to your choice of picking the correct elements and fill in the provided templates. 

## Convolution
In class, we went through an example of performing a 1D convolution. In this example, we will be working on figuring out how to perform a 2D convolution on an image. Namely, we would like to implement a Sobel filter such that given an image of N by M, we would convolve each pixel of the image with a horizontal kernel and a vertical kernel as described [here](https://en.wikipedia.org/wiki/Sobel_operator)

First, we will introduce a LineBuffer memory structure. A LineBuffer is a special case on an N-buffered 1D SRAM exposed to the user. It allows one or more rows of DRAM to be buffered into on-chip memory while previous rows can be accessed in a logically-rotating way. A LineBuffer is generally coupled with a shift register, and the animation below shows the specific usage of this pair in this tutorial.

![image](./img/lbsr.gif)

Note that in the last frame, the “buffer” row of the line buffer contains row 7 of the image. This is because this line buffer is physically implemented with four SRAMs and uses access redirection to create the logical behavior shown in the animation. After the last row is loaded and we drain the last frame, the buffers inside the line buffer will rotate but no new line will fill the buffer SRAM, leaving behind the data from row 7 even though it will not get used in this particular case. The Spatial compiler will also determine how to bank and duplicate the SRAMs that compose the line buffer automatically, should you choose to have a strided convolution.

It is also possible now to see why we must set a hard cap on the number of columns in the image if we are to use the line buffer - shift register combination. The logic that handles the rotation of the line buffer rows is tied to the controller hierarchy that manages the writes and reads about the line buffer. If we were to try to tile this operation along the columns, then our line buffer would load one tile of the row into the buffer, while row 0 of the line buffer would contain the previous part of that row. This splitting of a single line is semantically incorrect for convolution.

For this 2D convolution, we also introduce the lookup table (LUT). This is a read-only memory whose values are known at compile time. It is implemented using registers and muxes to index into it. In this example, we will be using the LUTs to store the two kernels.

Here is an overview of what it would look like in hardware: 
1. For each column of the image, we load it into the linebuffer. Let's say that we are at column c.
2. For each element in the column:
  - We first reset the register file. Let's say that the register file is Kh by Kw.
  - For each row in the register file, 

