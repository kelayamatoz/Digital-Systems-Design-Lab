# Designing An Accelerator for Matrix Multiplication and Optimizing Performance 

In this part of the lab, we will implement the GEMM operation in Vitis-HLS (which was done in Lab2 in Spatial). Additionally, we will try to improve performance using HLS pragmas for a vector addition accelerator. 

## Table of Contents

- [Designing Accelerator for GEMM](#designing-accelerator-for-gemm)  
  - [HLS Flow](#HLS-Flow)
- [Optimizing Performance in Vitis-HLS](#optimizing-performance-in-vitis-hls)  
  - [Your Turn](#your-turn-1)
- [Submission](#submission)

## Designing Accelerator for GEMM
In this exercise, we will fill in the code for the performing tile-wise matrix multiplication. To keep it simple, the GEMM is for a 4x4 input matrix with 2x2 tiles.

## HLS Flow

Source the AWS scripts and enter the lab directory:

``` bash
cd ~/aws-fpga/
source hdk_setup.sh
source sdk_setup.sh

cd ~/skeleton-lab-3/
```
1. Enter GEMM directory

``` bash
cd Lab2Part6GEMM/src/
```
2. Fill in the TODO sections in the source file, and answer the following question in `lab3_submit.md`:
      * What is the width of the shared memory interface of the `vadd` module?
      * The input matrices are initially stored into DRAM. The block accesses some elements to form the input tiles, prior to computing one output tile. What is the purpose of tiling?
      * Given the size of the tiles, and the width of the memory interface, what do you think the runtime of this block will be dominated by? Compute time (i.e. the time it takes to perform the math for a single output tile), or Memory time (i.e. the time it takes to load the necessary data from DRAM into the two SRAM input tiles, and the time it takes to write back the output tile)? Explain.


3. Before running HLS synthesis, lets verify our GEMM logic with a quick C++ testbench. This compiles `vadd.cpp` as plain C++ and checks the output against a reference.

```bash
make test_cpp
```

4. Answer the following question in `lab3_submit.md`:
      * Does verifying the high level functionality of the code (through the previous testbench) even require an AWS F2 terminal?
      * Compare, qualitatively, the amount of time taken to run the C++ testbench, vs the time taken by synthesis and system verilog simulation.
      * Given the above, what is the benefit of this early C++ testbench? How does it affect the design loop?
      * Explain why it is still necessary to perform a post-synthesis verilog simulation.

5. Lets generate RTL from this source code. Run the following:

```bash
cd ../design_top/
source setup.sh
make gen_rtl
```

6. Proceed to the RTL simulation. Run the following:

```bash
make hw_sim
```
8. The RTL testbench can be found under `verif/test/design_top_base_test.sv`. Answer the following:
      * What are the RTL Sim data transfer cycles?
      * What are the RTL Sim compute cycles?
      * Given the mismatch between the width of the design's memory port, and the actual width of the memory controller, explain one design optimization that could improve performance. (Hint: Currently the width of the design's port is smaller than the controller's, but every narrow block access still carries the full overhead of the controller's wide port. How can this be amortized?)

9. We can now move on to the FPGA test. First we need to perform synthesis and implementation, by running the following command:

```bash
make fpga_build
```
10. After the build finishes, generate the AWS FPGA Image AFI as such:

```bash
make generate_afi
```
11. We now need to wait until the AFI becomes available. Run the following command. The AFI will most likely be listed as "Pending", and will take about 20 minutes to become available. Run the command periodically and only proceed when it shows "Available".

```bash
make check_afi_available
```
12. Now that the AFI is available, program the FPGA, and run the FPGA test.

```bash
make program_fpga
make run_fpga_test
```
13. The FPGA test loads our custom logic to the FPGA, and then executes the C testbench under `software/src/design_top.c`. This testbench mimics our RTL test. Answer the following questions:
    * What are the RTL Sim data transfer cycles?
    * What are the RTL Sim compute cycles?
    * We already ran a C++ testbench early in our design iteration. What's the point of running a C testbench here? 

## Optimizing Performance in Vitis-HLS
In this exercise, we will look at ways of optimizing performance in HLS. First, fill in the vector addition code `TODO` in the `Lab3VectorAddOpt/src/vadd.cpp` and check functionality. Before running HLS or hardware emulation, you can run a native C++ testbench that uses the same 64-element vectors as `design_top_base_test.sv` and `design_top.c`:

```bash
cd Lab3VectorAddOpt/src/
make test_cpp
```

Once functionality is good, report the runtime for the unoptimized case and then try to improve the performance by modifying the `#pragma HLS unroll off=true` pragma for the operation. 

With the current implementation, there is no unrolling or hardware parallelism. Use `#pragma HLS unroll factor=<power of two, limit till 8>` to implement unrolling. **Note that the pragma needs to be after the loop**.

Unrolling generally improves performance, is this the case here too?

## Submission
Make sure all the files below are included in **your Github Classroom repository**.

### Part 1
You shoud add the following implementation to each file and answer part 1 of `lab3_submit.md`
* In `Lab3Part1Convolution`: Fill in the commented TODO section.

### Part 2
You shoud add the following implementation to each file and answer part 2 of `lab3_submit.md`
* In `Lab2Part6GEMM/src/vadd.cpp`: Fill in the commented TODO section.
* In `Lab3VectorAddOpt/src/vadd.cpp`: Fill in the commented TODO section.
* Make sure the `logs` directory of each part contains: `gen_rtl.log.txt`, `hw_sim.log.txt` & `fpga_test.log.txt` in **your Github Classroom repository**.

### Gradescope
* Gradescope: a doc with your commit ID & repo (for the entire lab2). Be sure to push all the changes required for submission (Part 1 and Part 2).