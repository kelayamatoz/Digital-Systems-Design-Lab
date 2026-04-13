# Designing Control logic and Memories using Vitis HLS 

In this part of the lab, we will replicate the FSM and LUT logic in Vitis HLS, while the GEMM operations will be performed in Lab 3. The flow remains the same as Lab 1.

1. [Designing Control Logic and Memories using Vitis HLS](#designing-control-logic-and-memories-using-vitis-hls)
2. [AWS F2 HLS Flow](#AWS-F2-HLS-Flow)
2. [Your Turn](#your-turn)
   - [FSM Control Logic – Lab2Part3BasicCondFSMAlt](#fsm-control-logic--lab2part3basiccondfsmalt)
   - [LUT Initialization – Lab2Part4LUT](#lut-initialization--lab2part4lut)
3. [Extra Credit](#extra-credit)
4. [Submission](#submission)

## AWS F2 HLS Flow

Source the AWS scripts and enter the lab directory:

``` bash
cd ~/aws-fpga/
source hdk_setup
source sdk_setup

cd skeleton-lab-2/
```
1. Enter the part 3 directory.

``` bash
cd Lab1Part3BasicCondFSMAlt/src/
```
2. Fill in the TODO sections in the source file, and answer the following question in `lab2_submit.md`:
      * By inspecting the module interface, what is the width of the `bram` port?

3. Lets generate RTL from this source code. Run the following:

```bash
cd ../design_top/
source setup.sh
make gen_rtl
```
4. The generated RTL is visible here: `design_top/design/concat_top.sv`. The `vadd` module definition can be seen at the bottom of the file, and includes the typical clock, reset, start, and done ports, the axi master, and slave axi control ports that you are familiar with.

5. Take a look at the `design_top.sv` wrapper. Specifically take a look at the code block starting at line 362, and answer the following question:
      * Why is this codeblock necessary, specifically for this `vadd` block? What does it do and how? (Hint; you don't need to go in depth into how the axi specifics unfold, just provide a high level description of what is being done.)

6. Proceed to the RTL simulation. Run the following:

```bash
make hw_sim
```
7. The RTL testbench can be found under `verif/test/design_top_base_test.sv`. The test configures the block with the correct `bram` array pointer, and provides the start signal to the custom logic. When the block finishes, it checks the values written at the `bram` pointer. Answer the following:
      * What are the RTL Sim data transfer cycles?
      * What are the RTL Sim compute cycles?
      * Recall that the compute cycles include the time it takes for the block to interact with the DRAM. How many DRAM accesses does the custom logic perform? (Hint: the logic itself, ignore the testbench).
      * Given the runtimes you've seen in this and Lab 1's simulations, as well as what you can infer from FSM behaviour, provide an estimate of the cycles taken by the DRAM access, and the cycles taken by the computation.

8. We can now move on to the FPGA test. First we need to perform synthesis and implementation, by running the following command:

```bash
make fpga_build
```
9. After the build finishes, generate the AWS FPGA Image AFI as such:

```bash
make generate_afi
```
10. We now need to wait until the AFI becomes available. Run the following command. The AFI will most likely be listed as "Pending", and will take about 20 minutes to become available. Run the command periodically and only proceed when it shows "Available".

```bash
make check_afi_available
```
11. Now that the AFI is available, program the FPGA, and run the FPGA test.

```bash
make program_fpga
make run_fpga_test
```
12. The FPGA test loads our custom logic to the FPGA, and then executes the C testbench under `software/src/design_top.c`. This testbench mimics our RTL test. Answer the following questions:
    * What are the RTL Sim data transfer cycles?
    * What are the RTL Sim compute cycles?

## Your Turn
1. Fill in the LUT initialization code `TODO` in `Lab2Part4LUT/src/vadd.cpp` and go through the entire F2 HLS flow, answering the relevant questions in `lab2_submit.md`.


## Submission
### Part 1
You should add the following implementation to each file and answer part 1 of `lab2_submit.md`
* In `Lab2Part1.scala`: Fill in the commented `TODO` section in `Lab2Part2SimpleMemFold`. For more details, refer to [MemReduce, MemFold section](#your-turn)
* In `Lab2Part3.scala`: Fill in the commented `TODO` section in `Lab2Part3BasicCondFSMAlt`. For more details, refer to [FSM section](#your-turn-1)
* In `Lab2Part4.scala`: Fill the commented `TODO` section in `Lab2Part4LUT`. For more details, refer to [LUT section](#your-turn-2)
* In `Lab2GEMM.scala`: Fill the commented `TODO` section in `Lab2Part5GEMM` and `Lab2Part6GEMM`. For more details, refer to [Lab2Part5GEMM](#your-turn-3), [Lab2Part6GEMM](#your-turn-4).

### Part 2
You shoud add the following implementation to each file and answer part 2 of `lab2_submit.md`
* In `Lab2Part3BasicCondFSMAlt/src/vadd.cpp`: Fill in the commented TODO section.
* In `Lab2Part4LUT/src/vadd.cpp`: Fill in the commented TODO section.

### Gradescope
* Gradescope: a doc with your commit ID & repo (for the entire lab2). Be sure to push all the changes required for submission (Part 1 and Part 2).