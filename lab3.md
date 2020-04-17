# Laboratory Exercise 3: Algorithm Specific Hardware Design
Lab 1 covers the basics of data types and controls. Lab 2 gives an example of implementing and optimizing a math kernel. In this lab, we will design a domain-specific hardware accelerator that combines the components introduced in the previous labs.

## Convolution
In this example, we will design an accelerator for 2D convolution on an image, e.g. a Sobel filter. Given an image of N by M, we convolve each pixel of the image with a horizontal kernel and a vertical kernel as described [here](https://en.wikipedia.org/wiki/Sobel_operator). The same idea of implementing a 1-D convolution certainly transfers, but there are more edge cases to consider for a 2-D one. 

To support buffering 2-D data, we introduce a LineBuffer memory structure. A LineBuffer is a special case on an N-buffered 1D SRAM exposed to the user. It allows one or more rows of DRAM to be buffered into an on-chip memory while the previous rows can be accessed in a logically-rotating way. A LineBuffer is generally coupled with a shift register, and the animation below shows the specific usage of this pair in this tutorial.

![image](./img/lbsr.gif)

Note that in the last frame, the “buffer” row of the line buffer contains row 7 of the image. This is because this line buffer is physically implemented with four SRAMs and uses access redirection to create the logical behavior shown in the animation. After the last row is loaded and we drain the last frame, the buffers inside the line buffer will rotate but no new line will fill the buffer SRAM, leaving behind the data from row 7 even though it will not get used in this particular case. The Spatial compiler will also determine how to bank and duplicate the SRAMs that compose the line buffer automatically, should you choose to have a strided convolution.

It is also possible now to see why we must set a hard cap on the number of columns in the image if we are to use the line buffer - shift register combination. The logic that handles the rotation of the line buffer rows is tied to the controller hierarchy that manages the writes and reads about the line buffer. If we were to try to tile this operation along the columns, then our line buffer would load one tile of the row into the buffer, while row 0 of the line buffer would contain the previous part of that row. This splitting of a single line is semantically incorrect for convolution.

For this 2D convolution, we also introduce the lookup table (LUT). This is a read-only memory whose values are known at compile time. It is implemented using registers and muxes to index into it. In this example, we will be using the LUTs to store the two kernels.

## Your Turn:
Given the two kernels and the input data, perform 2 2-D convolutions on the image. This design is equivalent to a hardware Sobel filter. You can add your implementation to Lab3Part1Convolution. Report the resource utilization and cycle counts of your design.

To test your application, you can use the following arguments:
```bash
64 64
```

This pair of arguments will create an image of 64 by 64.


## Designing a hardware accelerator for combinatorial optimization.
In class, we introduced the Needleman-Wunsch (NW) algorithm to find aligned genetic sequences. It turns out that the core algorithm of NW, dynamic programming, can be used to solve a broader class of optimization problems called combinatorial optimization. In this lab, we are looking at one particular use case of combinatorial optimization: solving the knapsack problem. 

A knapsack problem is defined as follows: Given a set of items, each with a weight and a value, determine the number of each item to pick such that the overall value is as large as possible (or as small as possible if the weights are negative). For example, let's say that we are trying to maximize the scores you get for this assignment should it have 7 questions instead of just 2, and you have only 15 hours to spend (by the way, this lab won't take you 15 hours). The goal is to maximize the score you get on this assignment without overspending on your time budget:
* Time budget: 15

| Question Number | 0 | 1 | 2 | 3 | 4 | 5 | 6 |
| --------------- | - | - | - | - | - | - | - |
|   Score         | 7 | 9 | 5 | 12| 14| 6 | 12|
|   Time          | 3 | 4 | 2 | 6 | 7 | 3 | 5 | 

The solution to this problem is similar to the one we introduced in class: 
1. Build a score table that stores the scores of possible combinations.
2. Traverse back the score table and build a path.

We provide a template that generates the gold answer for you. The `solver` object at line 116 will print out the score table when you run `bin/spatial`. You will see messages that look like:
```bash
[info] running OptimizeHomework --sim
[warn] No target specified. Specify target using: --fpga <device> or
[warn] override val target = <device>
[warn] Defaulting to 'Default' device.
[warn] Area model file Zynq_Area.csv for target Zynq was missing expected fields: 
[warn] BRAM
[info] Compiling OptimizeHomework to /home/tianzhao/teaching_labs/knapsack/gen/OptimizeHomework/
0, 0, 0, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
0, 0, 0, 7, 9, 9, 9, 16, 16, 16, 16, 16, 16, 16, 16, 16
0, 0, 5, 7, 9, 12, 14, 16, 16, 21, 21, 21, 21, 21, 21, 21
0, 0, 5, 7, 9, 12, 14, 16, 17, 21, 21, 24, 26, 28, 28, 33
0, 0, 5, 7, 9, 12, 14, 16, 17, 21, 21, 24, 26, 28, 30, 33
0, 0, 5, 7, 9, 12, 14, 16, 18, 21, 22, 24, 27, 28, 30, 33
0, 0, 5, 7, 9, 12, 14, 17, 19, 21, 24, 26, 28, 30, 33, 34
[info] emptiness (/home/tianzhao/bin/emptiness): Installed Version = 1.2, Required Version = 1.2
```

If you want to query the gold result, take a look at `solver.reportPath`. For example, you can get the gold results from `solver` by adding the following lines to your app:
```scala
val (bucketIDs, buckVals, buckSizes) = solver.reportPath()
val bucketsGold = toSpatialIntArray(bucketIDs.toArray)
val bucketValsGold = toSpatialIntArray(buckVals.toArray)
val bucketSizesGold = toSpatialIntArray(buckSizes.toArray)
```
These lines will create three arrays, `bucketsGold, bucketValsGold, bucketSizesGold`. These arrays report the picked questions and their time requirements / scores.


If you need some hints, here are two useful links:
[Python example of solving a knapsack problem](https://dev.to/downey/solving-the-knapsack-problem-with-dynamic-programming-4hce) and 
[Spatial NW example](https://spatial-lang.org/nwsw).



## Extra Credits (5 points out of 100)
* In lab 2, we show that parallelization can improve the performance of a matrix multiplication application. Can you take the same approach for the two applications in this lab? If you can, what's the improvement in performance? If you cannot, why?