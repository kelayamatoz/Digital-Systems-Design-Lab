# Designing Control logic and Memories using Vitis HLS 

In this part of the lab, we will try to replicate the FSM logic and LUT functionality done in Spatial for Vitis HLS and run a hardware emulation for the same. The testing procedure remains same as Lab 1. We will perform the GEMM operations in the lab3. 

1. [Designing Control Logic and Memories using Vitis HLS](#designing-control-logic-and-memories-using-vitis-hls)
2. [Your Turn](#your-turn)
   - [FSM Control Logic – Lab2Part3BasicCondFSMAlt](#fsm-control-logic--lab2part3basiccondfsmalt)
   - [LUT Initialization – Lab2Part4LUT](#lut-initialization--lab2part4lut)
3. [Extra Credit](#extra-credit)
4. [Submission](#submission)

## Your Turn
1. Fill in the FSM control logic `TODO` in `Lab2Part3BasicCondFSMAlt/src/vadd.cpp` and make sure hardware emulation functionality is meeting
2. Fill in the LUT initialization code `TODO` in `Lab2Part4LUT/src/vadd.cpp` and make sure hardware emulation functionality is meeting

## Extra Credit
You could try and generate the bitstreams for either or both the cases and check out their functionality. 

## Submission
* Make sure to commit your changes in `Lab2Part3BasicCondFSMAlt/src/vadd.cpp` and `Lab2Part4LUT/src/vadd.cpp`
* If you decide do the extra credit, copy the respective `vadd.awsxclbin` to the folders `Lab2Part3BasicCondFSMAlt` and `Lab2Part4LUT`.
* Gradescope: a doc with your commit ID & repo (for the entire lab2). Be sure to push all the changes required for submission (Part 1 and Part 2).