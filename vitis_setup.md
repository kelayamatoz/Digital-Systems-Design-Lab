# Vitis Design Flow
1. [How to create project from scratch](./vitis_setup.md#how-to-create-project-from-scratch)
2. [How to get the simulation results?](./vitis_setup.md#how-to-get-the-simulation-results)
    * [Test Result](./vitis_setup.md#test-result)
    * [Performance (Latency)](./vitis_setup.md#performance-latency)
    * [Resource Utilization](./vitis_setup.md#resource-utilization)
3. [Are we meant to use Vitis HLS in IDE mode or full on script mode?](./vitis_setup.md#are-we-meant-to-use-vitis-hls-in-ide-mode-or-full-on-script-mode)
4. [Tips on how to actually adapt software to a Vitis HLS system on the F1](./vitis_setup.md#tips-on-how-to-actually-adapt-software-to-a-vitis-hls-system-on-the-f1)


## How to create project from scratch:
This is essentially trying to replicate the folder structure of the `hello_world_demo` folder we used for lab3.
Your project folder should be placed under `$VITIS_DIR/examples/xilinx`. The structure of the folder should be:
```
$VITIS_DIR/examples/xilinx
|__ $NAME_OF_YOUR_PROJECT_FOLDER
    |__ src
        |__ host.cpp
        |__ $DESIGN_NAME.cpp
    |__ description.json
    |__ Makefile
    |__ qor.json
    |__ utils.mk
    |__ xrt.ini

```

0. Start the AWS instance and run the following command to move into the AWS FPGA repo and to set up the environment.
    ```bash
    cd $AWS_FPGA_REPO_DIR
    source vitis_setup.sh  
    ```
1. Create a folder under `$VITIS_DIR/examples/xilinx`. You should replace `$(NAME_OF_YOUR_PROJECT_FOLDER)` with the name of your project folder.
    ```bash
    cd $VITIS_DIR/examples/xilinx
    mkdir $(NAME_OF_YOUR_PROJECT_FOLDER)
    ```

2. Create a `src` folder and create a `host.cpp` file with the host code and the `$(DESIGN_NAME).cpp` file that contains your accelerator design. `$(DESIGN_NAME).cpp` should be a name for your accelerator design. For example, in lab3, the accelerator was doing a vector addition and we named the file with the accelerator design as `vadd.cpp`.

3. Copy in the `description.json` file from the `hello_world_demo` folder in lab3.
    - Change the "host_exe" field under "host" to the name of the project folder you set at step 1. 
        ```json
        "host": {
            "host_exe": "$(NAME_OF_YOUR_PROJECT_FOLDER)", 
            "compiler": {
                "sources": [
                    "REPO_DIR/common/includes/xcl2/xcl2.cpp",
                    "./src/host.cpp"
                ], 
                "includepaths": [
                    "REPO_DIR/common/includes/xcl2"
                ]
            }
        },
        ```

        For example, if your accelerator design file name is `convolution.cpp`:
        ```json
        "host": {
            "host_exe": "convolution", 
            "compiler": {
                "sources": [
                    "REPO_DIR/common/includes/xcl2/xcl2.cpp",
                    "./src/host.cpp"
                ], 
                "includepaths": [
                    "REPO_DIR/common/includes/xcl2"
                ]
            }
        },
        ```

    - Change the "containers" field with the name of your accelerator design file's name:
        ```json
        "containers": [
            {
                "accelerators": [
                    {
                        "name": "$(DESIGN_NAME)", 
                        "location": "src/$(DESIGN_NAME).cpp"
                    }
                ], 
                "name": "$(DESIGN_NAME)"
            }
        ], 
        ```

    - Change the "cmd_args" field under "launch" with your accelerator design file's name:
        ```json
        "launch": [
            {
                "cmd_args": "BUILD/$(DESIGN_NAME).xclbin", 
                "name": "generic launch for all flows"
            }
        ], 
        ```


4. Download this `Makefile` from this [link](https://drive.google.com/file/d/15I29_pOt3WK2BVTatlqgJpmxWH7wkpRv/view?usp=sharing). Change `NAME_OF_YOUR_PROJECT_FOLDER` and `DESIGN_NAME` in line 50 and 51 to the name of the project folder and the name of your accelerator design. For example:
    ```
    NAME_OF_YOUR_PROJECT_FOLDER := hello_world
    DESIGN_NAME := vadd
    ```

5. Create a `qor.json` file with the name of your accelerator design as shown below:
    ```json
    {
        "containers": [
            {
                "name": "$(DESIGN_NAME)", 
                "meet_system_timing": "true", 
                "accelerators": [
                    {
                        "name": "$(DESIGN_NAME)", 
                        "check_timing": "true", 
                        "PipelineType": "none", 
                        "check_latency": "true", 
                        "check_warning": "false" 
                    }
                ]
            }
        ]
    }
    ```


6. Copy in the `utils.mk` file from the `hello_world_demo` folder in lab3.  Replace line 102's `vadd` with your `$(DESIGN_NAME)`.
    ```bash
    $(ECHO) '$(EXECUTABLE) $(DESIGN_NAME).xclbin' >> run_app.sh
    ```

7. Copy in the `xrt.ini` file from the `hello_world_demo` folder in lab3. You can change the options based on what you would like to record during the emulation.


## How to get the simulation results?
### Test Result
If you add print statements of whether the test passed or not in your host code, the test result will be printed out in the terminal.

### Performance (Latency)
The kernel execution time can be found in the 'profile summary' tab in the `xrt.run_summary' file.
You can open this with a gui by running:
```bash
cd $VITIS_DIR/examples/xilinx/hello_world_demo
vitis_analyzer xrt.run_summary
```

### Resource Utilization
The compute unit utilization can be found in the 'profile summary' tab in the `xrt.run_summary' file.
You can open this with a gui by running (replace the `$PROJECT_NAME` with your project directory name):
```bash
cd $VITIS_DIR/examples/xilinx/$PROJECT_NAME
vitis_analyzer xrt.run_summary
```

<div style="padding-left: 0px; padding-top: 0px; text-align: center;">
    <img src="./img/hw_emul_off.png" width="90%">
</div>




## Are we meant to use Vitis HLS in IDE mode or full on script mode? 
Unfortunately, the FPGA Developer AMI (Amazon Machine Image) provided by AWS does not include the Vitis IDE with a graphical user interface (GUI). The FPGA Developer AMI is typically configured for command-line based development and includes tools and drivers necessary for FPGA development on AWS F1 instances, such as Xilinx Vivado for FPGA design, and Xilinx SDK for software development. These tools are primarily operated through the command line.



## Tips on how to actually adapt software to a Vitis HLS system on the F1
1. Write the computation that you plan to accelerate in C++. This will be similar to a software demo, but since Vitis uses C++ and pragmas to design hardware, you can use this as the starting C++ code.

2. Once you know you have a working implementation, think about how you would move your data between the off-chip DRAM memory and the on-chip SRAM memory. Using lab3 as an example, when we were trying to compute addtion between long vectors, we created a load, compute, store pipeline where each stage loads, adds, and stores 16 elements at a time.

3. One you have an initial design of how you want to move the data between the DRAM and on-chip SRAM, implement this logic in  `host.cpp` and your cpp file for the accelerator design. Include test code in `host.cpp` to see whether the data transfer is happening properly. You can see the test result by running software or hardware emulation.

    For SIMD execution, you can use [hls::vector](https://docs.amd.com/r/en-US/ug1399-vitis-hls/HLS-Vector-Library) and for dataflow execution, consider using [hls::stream](https://docs.amd.com/r/en-US/ug1399-vitis-hls/HLS-Stream-Library). Looking at how example implementations move data between the DRAM and on-chip SRAM using these libraries will be useful to get a grasp of how you would want to implement your design.
    * Example2 (`hls::vector`): Lab3 hello world example
    * Example3 (`hls::stream`): [Vitis hello world](https://github.com/Xilinx/Vitis_Accel_Examples/tree/f61637e9263ecc1be3df34182ea6c53a0ca10447/hello_world/src)
    * Example4 (`hls::stream`): [dataflow_stream](https://github.com/Xilinx/Vitis_Accel_Examples/tree/f61637e9263ecc1be3df34182ea6c53a0ca10447/cpp_kernels/dataflow_stream/src)

    In lab3, we have used `hls::vector` for SIMD processing. However, to get a simple initial design, it's also possible to not use these hls datatypes. A simple example that does not use `hls::vector` or `hls::stream` can be found below:
    * [loop_pipeline](https://github.com/Xilinx/Vitis_Accel_Examples/tree/f61637e9263ecc1be3df34182ea6c53a0ca10447/cpp_kernels/loop_pipeline/src)

4. Implement your design for the computation. Include test code in `host.cpp` to check correctness. You can see the test result by running software or hardware emulation.


5. **Optimization**: Once you're done upto step 4, you can optimize your design by adding pragmas or rewriting parts of your code. 
    * Loop pipelining & Loop unrolling: These are the basic optimizations we've seen in the previous labs. You can apply them by adding pragmas to the loops.
    * Additional optimizations: [This page](https://github.com/Xilinx/Vitis_Accel_Examples/tree/f61637e9263ecc1be3df34182ea6c53a0ca10447/cpp_kernels) contains example codes and what optimizations they are doing. Some of the optimizations may also be applicable for your code too. Using software emulation to check whether the optimization breaks the correctness is fine. However, if you want to see the accurate effect of applying the optimization, run the hardware emulation.

6. **Porting to F1 FPGA**:
The [AWS F1 repository](https://github.com/aws/aws-fpga/tree/master/Vitis#quick-start-guide-to-accelerating-your-cc-application-on-an-aws-f1-fpga-instance-with-vitis) provided insturctions on how to port your design to an F1 instance. However, we will release a more detailed step-by-step tutorial on how to port your design to an actual F1 instance before milestone 3 starts. 



