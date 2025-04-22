# Designing An Accelerator for Matrix Multiplication and Optimizing Performance 

In this part of the lab, we will implement the GEMM operation in Vitis-HLS (which was done in Lab2 in Spatial). Additionally, we will try to improve performance using HLS pragmas for a vector addition accelerator. 

## Table of Contents

- [Designing Accelerator for GEMM](#designing-accelerator-for-gemm)  
  - [Your Turn](#your-turn)
- [Optimizing Performance in Vitis-HLS](#optimizing-performance-in-vitis-hls)  
  - [Your Turn](#your-turn-1)
- [Submission](#submission)

## Designing Accelerator for GEMM
In this exercise, we will fill in the code for the performing tile-wise matrix multiplication. To keep it simple, the GEMM if for a 4x4 input matrices with 2x2 tiles for output computation. You are welcome to try out the same for different input matrix size (do remember to change the values in `Lab2Part6GEMM/src/host.cpp` as well)

### Your Turn
1. Fill in the `TODO` in `Lab2Part6GEMM/src/vadd.cpp` and make sure hardware emulation functionality is meeting.

## Optimizing Performance in Vitis-HLS
In this exercise, we will look at ways of optimizing performance in HLS. First, fill in the vector addition code `TODO` in the `Lab3VectorAddOpt/src/vadd.cpp` and check functionality. Once functionality is good, report the runtime for the unoptimized case and then try to improve the performance by modifying the `#pragma HLS unroll off=true` pragma for the operation. 

With the current implementation, there is no unrolling or parallelism done by the hardware. Use `#pragma HLS unroll factor=<power of two, limit till 8>` to implement unrolling. 

AWS has recently deprecated kernel cycle profiling, hence we will be checking the `xrt::run::run` in the generated `summary.csv`. You can expect to see a xrt::run::run time reduction of about 5% to 15%. Please note that this number can change from run to run and since it is the simulation time data (which is not that reliably profiled), you can expect to see some discrepancies. 

### Your Turn
1. Fill in the `TODO` in `Lab3VectorAddOpt/src/vadd.cpp` and make sure hardware emulation functionality is meeting
2. Optimize the runtime by unrolling more hardware through hls pragma.
2. Report the unoptimized runtime, optimization strategy and optimized runtime in `lab3_submit.md` 

## Submission
* Make sure to commit your changes in `Lab2Part6GEMM/src/vadd.cpp` and `Lab2Part4LUT/src/vadd.cpp`
* Write the answers to the questions given in `lab3_submit.md`
* Gradescope: a doc with your commit ID & repo (for the entire lab3). Be sure to push all the changes required for submission (Part 1 and Part 2).