# Laboratory Exercise 3: Algorithm Specific Hardware Design
The last two labs give you some background information on how to design an accelerator. In this lab, we will design and implement an accelerator for a real-world application.

In this lab, we focus more on the algorithm side. We will first describe how the algorithm works. It is your task to pick the right elements and implement the algorithm using the provided templates.

## Convolution
In class, we went through an example of performing a 1D convolution. In this example, we will be working on figuring out how to perform a 2D convolution on an image. Namely, we would like to implement a Sobel filter. Given an image of N by M, we convolve each pixel of the image with a horizontal kernel and a vertical kernel as described [here](https://en.wikipedia.org/wiki/Sobel_operator)

First, we introduce a LineBuffer memory structure. A LineBuffer is a special case on an N-buffered 1D SRAM exposed to the user. It allows one or more rows of DRAM to be buffered into an on-chip memory while the previous rows can be accessed in a logically-rotating way. A LineBuffer is generally coupled with a shift register, and the animation below shows the specific usage of this pair in this tutorial.

![image](./img/lbsr.gif)

Note that in the last frame, the “buffer” row of the line buffer contains row 7 of the image. This is because this line buffer is physically implemented with four SRAMs and uses access redirection to create the logical behavior shown in the animation. After the last row is loaded and we drain the last frame, the buffers inside the line buffer will rotate but no new line will fill the buffer SRAM, leaving behind the data from row 7 even though it will not get used in this particular case. The Spatial compiler will also determine how to bank and duplicate the SRAMs that compose the line buffer automatically, should you choose to have a strided convolution.

It is also possible now to see why we must set a hard cap on the number of columns in the image if we are to use the line buffer - shift register combination. The logic that handles the rotation of the line buffer rows is tied to the controller hierarchy that manages the writes and reads about the line buffer. If we were to try to tile this operation along the columns, then our line buffer would load one tile of the row into the buffer, while row 0 of the line buffer would contain the previous part of that row. This splitting of a single line is semantically incorrect for convolution.

For this 2D convolution, we also introduce the lookup table (LUT). This is a read-only memory whose values are known at compile time. It is implemented using registers and muxes to index into it. In this example, we will be using the LUTs to store the two kernels.

## Your Turn:
Given the two kernels and the input data, perform 2 2-D convolution on the image. You can add your implementation to Lab3Part1Convolution. Report the resource utilization and cycle counts of your design. 

To test your application, you can use the following arguments: 
```bash 
64 64
```

This pair of arguments will create an image of 64 by 64. 


## Implementation of [Needleman-Wunsch (NW)](https://en.wikipedia.org/wiki/Needleman%E2%80%93Wunsch_algorithm)
In class, we have covered the details of NW algorithm. In this lab, we will be focusing on implementing this algorithm. NW algorithm contains two stages, one for creating the score table and the other for tracing back the path that gives the most matches.

### Score Matrix Population
In this first section, we will make a forward pass to fill out the score matrix. In this algorithm, we need to embed two pieces of information in each matrix entry: the score at that entry and the direction to travel to achieve that score (N, W, or NW). We will start by defining a new struct that can contain this tuple up above our main():

```scala
@struct case class nw_tuple(score: Int16, ptr: Int16)
```
As we traverse the score matrix, we check the left, top, and top-left entries, add a score update, and check which path gives us the maximum score for that entry. To determine the additional score when coming from the top-left, we check if the letter at the top of the column (from string A) matches the letter from the left of the row (from string B). If there is a match, then this path is rewarded with an addition of 1. If they do not match, then we penalize this path with a score of -1. We then look at the cost when coming from the left and from the top. These transitions correspond to skipping an entry in B and skipping an entry in A, respectively, and we penalize them as they do not correspond to string matches. This transition is called a "gap." Let's now assign vals to keep track of these properties:

```scala
val SKIPB = 0 // move left
val SKIPA = 1 // move up
val ALIGN = 2 // move diagonal
val MATCH_SCORE = 1
val MISMATCH_SCORE = -1
val GAP_SCORE = -1
```

When populating the score matrix, each entry would be an nw_tuple struct that stores the score at the current location and the path it takes from the last position (SKIPA, SKIPB, or ALIGN). Later in the second stage of NW, you will be using the ptr information to reconstruct the path.

#### Your Turn:
Write the code that will traverse the matrix from top-left to bottom-right and update each entry of the score matrix.

### Traceback
Now we can traverse the score matrix, starting from the bottom right. We will use a FIFO to store the aligned result, and a finite state machine (FSM) to handle the back trace and complete when the FIFOs are filled. The state in the FSM starts at 0, which we use for the state to trace back through the matrix. When we either hit the top edge or the left edge of the score matrix, we jump to state 1 which is used to pad both of the FIFOs until they fill up. Once the FSM detects that they are full, it exits and the results are stored to DRAM. The branch conditions can be implemented using the if/else if/else statements. 

#### Your Turn:
Write the code that can traceback from the bottom-right to the top-left of the score matrix. You can add your implementation in Lab3Part2NW. Report the resource utilization and cycle counts of your NW implementation.

To test your implementation, you can use the following two sequences as test arguments: 
```bash
# Sequence A
tcgacgaaataggatgacagcacgttctcgt 
# Sequence B
ttcgagggcgcgtgtcgcggtccatcgacat
```
These two sequences should have roughly 90% match. 

 
## Extra Credits (5 points out of 100)
* In the last lab, we show that parallelization can improve the performance of running a matrix multiplication application. Can you take the same approach for Convoluation and for NW? If you can, what's the improvement in performance? If you cannot, why?
