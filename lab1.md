# Laboratory Exercise 1: Basic Usage of Spatial

Project due: 04/10/2019 11:59 pm

This is an introductory exercise that involves using Spatial controllers and memories to design circuits. In this section, you will be building a few Spatial applications using the following elements:

Off-Chip Memory: DRAM

On-Chip Memories: Register, SRAM, FIFO, FILO

Registers: ArgIn, ArgOut Registers, Register

Controllers: Foreach, Fold, Reduce

Each part of the exercise covers the usage of a Spatial element. We will first guide you through some examples. Then you will need to modify the examples to make a few new apps.

## Setup
First, open your terminal app and login to tucson by running:
```bash
ssh -Y USERNAME@tucson.stanford.edu
```

Tucson is the main server that we will be using to host our development environment. On tucson, the development directory is under spatial. Go to your development directory by running:
```bash
cd ~/spatial
```

If you haven't installed Spatial before, run the following command to install it:
```bash
make && make install && make publish
```

Under the spatial directory, the apps/src directory stores the source code of Spatial apps. The gen directory contains the generated FPGA projects. To start developing apps, go to apps/src:
```bash
cd apps/src
```
<!-- ![apps](./img/apps.png) -->

You will see that all the Spatial apps are stored as .scala files. For this exercise, we will complete our apps in lab1.scala.

## Using Registers
### Demo:
In this example, we build a circuit that reads in two inputs and add them together. First, we need to set up the Spatial template:
```scala
import spatial.dsl._

@spatial object Lab1Part1RegExample extends SpatialApp {
  def main() {
    // Your code here
  }
}
```

The first line imports the spatial library. The 3rd line declares an app called "Lab1Part1RegExample". Before we move forward, we need to think about the design of the app. In this case, we want to send two scalars from the CPU side to the accelerator side. We then perform the addition at the accelerator side, and send the result back to the CPU. How can we do this? First, we will need two ArgIn registers and one ArgOut register to establish the communication between the host and the accelerator:
```scala

@spatial object Lab1Part1RegExample extends SpatialApp {
  def main() {

  // In this app, the type of numbers is Int.
  type T = Int

  def main() {
    // In Spatial, you can get the Nth argument from the command line by using args(N). 
    // We need to cast it as type T because we use T as the type of the values throughout the whole app. 
    val N = args(0).to[T]
    val M = args(1).to[T]

    // Create two ArgIn registers
    val argRegIn0 = ArgIn[T]
    val argRegIn1 = ArgIn[T]

    // Set two ArgIn registers with N and M
    setArg(argRegIn0, N)
    setArg(argRegIn1, M)

    // Create one ArgOut register
    val argRegOut = ArgOut[T]
  }
}
```

We just finished writing our code for the CPU side; now we need to design our accelerator. In this demo, we are going to use a design that fetches two values from the CPU side and passes their sum back to the CPU:
```scala
    Accel {
      // Get values of the two argIn registers. We get the value of a register by using .value. 
      val argRegIn0Value = argRegIn0.value
      val argRegIn1Value = argRegIn1.value

      // Perform the addition, then set the output register with the result. The := sign is used to assign a value to a register.
      argRegOut := argRegIn0Value + argRegIn1Value
    }
```

We are not done yet. After we specify the accelerator design, we still need to fetch the result and verify that we get the right one: 
```scala 
    // Get the result from the accelerator.
    val argRegOutResult = getArg(argRegOut)

    // Print the result.
    println("Result = " + argRegOutResult)

    // Calculate the reference result. Make sure that it matches the accelerator output.
    val gold = M + N
    println("Gold = " + gold)
    val chksum = gold == argRegOutResult

    // Print PASS if the reference result matches the accelerator result.
    println("PASS = " + chksum)

```

Here's what the app looks like:
```scala
import spatial.dsl._

@spatial object Lab1Part1RegExample extends SpatialApp {

  type T = Int

  def main() {
    val N = args(0).to[T]
    val M = args(1).to[T]
    val argRegIn0 = ArgIn[T]
    val argRegIn1 = ArgIn[T]
    setArg(argRegIn0, N)
    setArg(argRegIn1, M)
    val argRegOut = ArgOut[T]

    Accel {
      val argRegIn0Value = argRegIn0.value
      val argRegIn1Value = argRegIn1.value
      argRegOut := argRegIn0Value + argRegIn1Value
    }

    val argRegOutResult = getArg(argRegOut)
    println("Result = " + argRegOutResult)

    val gold = M + N
    println("Gold = " + gold)
    val chksum = gold == argRegOutResult
    println("PASS = " + chksum)
  }
}
```

After you are done designing the app, go back to the spatial directory. We will need to verify that the app is written correctly.

There are two ways to verify the correctness of your design. The first way is to run a Scala simulation. The second way is to run a VCS simulation. If you just want to quickly check what your design produces, you should use the Scala simulation. To start the Scala simulation, you need to run:
```scalar
cd ~/spatial
bin/spatial Lab1Part1RegExample --sim
```
<!-- ![runssim](./img/runssim.png) -->

The second command generates the files needed for scala simulation. To start the simulation, run:

```scalar
./Lab1Part1RegExample.sim "3 5"
```
<!-- ![scalare](./img/scalare.png) -->

This command runs scala simulation for the app with command line inputs 3 and 5, and produces the correct result.

However, if you want to make sure that your design is [cycle-accurate](https://retrocomputing.stackexchange.com/questions/1191/what-exactly-is-a-cycle-accurate-emulator), you will need to run the VCS simulation. Unlike Scala simulation, VCS simulation generates the Verilog description of your design and runs a cycle-accurate simulation. Compared to Scala simulation, VCS simulation takes longer to complete (because the circuit needs to be simulated at every clock cycle), but it gives a simulation environment that's more similar to what will be running on the board. For example, you can have a design that passes the Scala simulation, but fails the VCS simulation because the circuit that gets generated is not correct. In addition, we can also use the VCS simulation results to help us tune our design. We will cover the details in Lab 2.

In order to generate the files for VCS simulation, you need to run the following commands:
```bash
cd ~/spatial
bin/spatial Lab1Part1RegExample --synth --instrumentation --fpga=VCS
cd ./gen
ls
```

The "--synth" flag means that we want to generate a synthesizable design. The "--instrumentation" flag means that we want to learn about the number of clock cycles needed to finish the design. The "--fgpa" flag refers to the target you are generating for. In this example, you are generating a design for the VCS simulation, and hence the flag "--fpga=VCS".

<!-- <img src="./img/gen.png" width="70%" height="60%"> -->

The synthesizable design of your Spatial app is under "Lab1Part1RegExample". Let's take a look at the generated files.
```bash
cd Lab1Part1RegExample
ls
```

<!-- <img src="./img/genls.png" width="70%" height="60%"> -->
The chisel folder contains the RTL code generated from your design. Here are the commands to run VCS simulation:

```bash
# make vcs simulation binary, then redirect the console output to vcs.log
make > vcs.log
# run vcs simulation, then redirect the console output to dramsim.log. 
bash run.sh 3 5 > dramsim.log
```

VCS simulation would start. Wait till the simulation completes, and you can view the simulation results by running:
<!-- After the simulation finishes, you will see the following messages:
![vcs](./img/vcs.png)
To view the simulation result, run: -->
```bash
cat dramsim.log
```
<!-- ![dramsim](./img/dramsimre.png) -->

The VCS simulation result states that your app is simulated successfully and ran for 3 cycles. Now we can clean up the VCS simulation directory.

```bash
cd ~/spatial
rm -rf gen/Lab1Part1RegExample
```

Now we can start deploying your design on the FPGA board. In this class, we are using [Xilinx ZC706](https://www.xilinx.com/products/boards-and-kits/ek-z7-zc706-g.html) as our platform. Before synthesizing the design, we need to do a few more setups. First, we need to start a [screen](https://kb.iu.edu/d/acuy) session to run the synthesizer. This way, you can keep the job running even when you sign off the server. The following command starts a screen session called Lab1Part1RegExample.
```bash
screen -S Lab1Part1RegExample
```

In the screen session, you need to first generate a design for your FPGA. In this class, your FPGA will be either a zcu or a zynq board. The TA will send you information about the board assigned to you. For example, let us assume that you have access to a zynq board. Go back to the spatial directory, and replace the "--fgpa" flag with "zynq":
```bash
bin/spatial Lab1Part1RegExample --synth --instrumentation --fpga=zynq
```

Then, go to the generated folder and run make:
```bash
cd gen/Lab1Part1RegExample
make | tee make.log
```

The "tee" command would log the output from the synthesizer to "make.log".
After you start the synthesizer, you can detach the screen session by pressing "Ctrl+A D". You can view the running screen sessions by using the command "screen -ls". To reattach a screen session, you can run "screen -r SESSIONNAME".


<!-- <img src="./img/screenjobs.png" width="70%" height="60%"> -->
The synthesis process would take ~20 min to run. After the synthesis finishes, we would want to deploy the design onto the FPGA board. You will need to resume the session, copy the synthesized bitstream onto board, and then ssh onto the board:

```bash
screen -r Lab1Part1RegExample
// In the screen session. scp copies a file to a remote FPGA board.
// Your board may have a different name.
scp Lab1Part1RegExample.tar.gz YOUR_NAME@holodeck-zc706.stanford.edu:~/
ssh YOUR_NAME@holodeck-zc706.stanford.edu
```
<!-- <img src="./img/scp.png" width="70%" height="60%"> -->

Lab1Part1RegExample.tar.gz is a compressed file. Once you login, you will need to first decompress it. After the decompression finishes, you can run the design by using the "Top" executable:
```bash
x Lab1Part1RegExample.tar.gz
cd Lab1Part1RegExample
bash run.sh 32 32
```

<!-- If your design runs successfully, you will see the following message: -->
<!-- <img src="./img/deploydesign.png" width="70%" height="60%"> -->

The console would print a few messages indicating that your design runs for 1 cycle. Congratulations! You have successfully deployed your first accelerator design!


### Your Turn
Can you modify this app so that it fetches three numbers from the CPU side and calculates their sum? You can assume that your user only enters integers.

* Report on the modifications you made to the original app.

## Using DRAM and SRAM
### Demo
In this example, we build a circuit that reads in an array of values, augment each element in the array by x times, and then stores the array back. To do so, we will need 3 basic Spatial elements: DRAM, SRAM and Foreach Controller.

A DRAM specifies a piece of memory that's accessible to both the host and the accelerator. It has the following syntax:
```scala
val dram = DRAM[data_type](n0, n1, n2, ...) // n0, n1, n2 are the sizes of each dimension
setMem(dram, array) // set dram with array
val array_result = getMem(dram) // get the content in dram
```

An SRAM specifies a piece of memory that's embedded on the FPGA (accelerator). It has the following syntax:
```scala
val sram = SRAM[data_type](n0, n1, n2, ...) // n0, n1, n2 are the sizes of each dimension
val ele_i = sram(i) // get the ith element of sram
sram(i) = 1.to[T] // set the ith element of sram to 1
sram load dram(k::k+n0) // load the elements from index k to k+n0 in dram to sram
dram(k::k+n0) store sram // store the elements from index k to k+n0 into dram
```

DRAM and SRAM are quite different. First, you can only access DRAM data through bursts, whereas you can access SRAM data element by element. Second, SRAM reads / writes can be much faster than DRAM reads / writes. On an FPGA, a single SRAM access usually completes in one cycle.

However, modern FPGAs usually don't have a lot of SRAM resources. For example, An FPGA SoC has 1GB of DRAM and only 31 Mbits of SRAM. Therefore, when designing your accelerator, you need to think of the design trade-off between using SRAM and DRAM. We will cover this topic in Lab 2.

A Foreach Controller can be thought of as a for loop. It has the following syntax:
```scala

Foreach (N by n) { i =>
  // loop body
}
```

These elements would be enough to implement the circuit we want. Let's say that the size of our SRAM is tileSize, and we have an array of N elements. First, we need to bring the N elements from the host side into DRAM. Second, we need to load the N elements into the accelerator. Third, we need to multiply each element by a factor of x. Fourth, we need to store the N elements into DRAM. Fifth, we need to instruct the host to fetch the results from DRAM. To translate these steps into a circuit, we would write the Spatial app that looks like this:

```scala
@spatial object Lab1Part2DramSramExample extends SpatialApp {

  val N = 32
  type T = Int

  // In this example, we write the accelerator code in a function.
  // [T:Type:Num] means that this function takes in a type T.
  // The operator "=" means that this function is returning a value.
  def simpleLoadStore(srcHost: Array[T], value: T) = {
    val tileSize = 16

    val srcFPGA = DRAM[T](N)
    val dstFPGA = DRAM[T](N)

    // 1. Bring the N elements from the host side into DRAM
    setMem(srcFPGA, srcHost)

    val x = ArgIn[T]
    setArg(x, value)
    Accel {

      Sequential.Foreach(N by tileSize) { i =>
        val b1 = SRAM[T](tileSize)

        b1 load srcFPGA(i::i+tileSize)

        // 2. Bring the elements into the accelerator
        val b2 = SRAM[T](tileSize)
        Foreach(tileSize by 1) { ii =>
          // 3. Multiply each element by a factor of x
          b2(ii) = b1(ii) * x
        }

        // 4. Store the result back to DRAM
        dstFPGA(i::i+tileSize) store b2
      }
    }

    // 5. Intruct the host to fetch data from DRAM
    getMem(dstFPGA)
  }

  def main() {
    val arraySize = N
    val value = args(0).to[Int]

    // This line means that we are creating an array of size "arraySize", where each 
    // element is an integer. "i => i % 256" means that for each index i, populate an 
    // element with value i % 256. 
    val src = Array.tabulate[Int](arraySize) { i => i % 256 }
    val dst = simpleLoadStore(src, value)

    // This line means that for each element in src, generate an element using 
    // the function "_ * value". Map is an operator that maps a function to 
    // every single element of an array.
    val gold = src.map { _ * value }

    println("Sent in: ")
    (0 until arraySize) foreach { i => print(gold(i) + " ") }
    println("Got out: ")
    (0 until arraySize) foreach { i => print(dst(i) + " ") }
    println("")

    // This line means that for every pair of elements in dst, gold, check if each 
    // pair contains equal elements. Reduce coalesces all the pairs by using the 
    // function "_&&_".
    val cksum = dst.zip(gold){_ == _}.reduce{_&&_}
    println("PASS: " + cksum)
  }
}
```

### Your Turn
* Simulate the app using Scala and VCS simulation. Report on the simulation result. 
* Synthesize the design and run on the FPGA. Report the number of cycles needed for the design. 

Cycle count is not the only aspect that shows how good your design is. Moreover, we would want to understand the resource utilization of your design. The synthesizer would give you some information about it.  

After the synthesis finishes, go to ~/spatial/gen/Lab1Part2DramSramExample/verilog-zynq/ (if you are using a zcu, you need to go to verilog-zcu). The resource utilization report is named "par_utilization.rpt", and it contains information that looks like this:

```bash
+--------------------------------------+-------+-------+-----------+-------+
|               Site Type              |  Used | Fixed | Available | Util% |
+--------------------------------------+-------+-------+-----------+-------+
| Slice LUTs                           | 18243 |     0 |    218600 |  8.35 |
|   LUT as Logic                       | 12198 |     0 |    218600 |  5.58 |
|   LUT as Memory                      |  3122 |     0 |     70400 |  4.43 |
|     LUT as Distributed RAM           |  1288 |     0 |           |       |
|     LUT as Shift Register            |  1834 |     0 |           |       |
|   LUT used exclusively as pack-thrus |  2923 |     0 |    218600 |  1.34 |
| Slice Registers                      | 19710 |     0 |    437200 |  4.51 |
|   Register as Flip Flop              | 19710 |     0 |    437200 |  4.51 |
|   Register as Latch                  |     0 |     0 |    437200 |  0.00 |
|   Register as pack-thrus             |     0 |     0 |    437200 |  0.00 |
| F7 Muxes                             |   592 |     0 |    109300 |  0.54 |
| F8 Muxes                             |     0 |     0 |     54650 |  0.00 |
+--------------------------------------+-------+-------+-----------+-------+
```

* Report the resource utilization of your design.

## Using FIFO
A [FIFO](https://stanford-ppl.github.io/spatial-doc/v1.1/spatial/lang/FIFO.html) can be thought of as a queue.
### Demo
Here is the syntax of using a FIFO:
```scala
// Create a FIFO called f1 with type T and with size tileSize
val f1 = FIFO[T](tileSize)
// Load the elements from index i to index i + tileSize in dram to f1
f1 load dram(i::i+tileSize)
// Create an enqueue port for data to f1 
f1.enq(data)
// Create a deque port for data to f1 
f1.deq(data)
// Peek the head of f1 without removing it
f1.peek()
// 
```

<!-- Here is some syntax of using FILO functions: -->
<!-- ```scala
// Create a FILO called f1 with type T and with size tileSize
val f1 = FILO[T](tileSize)
// Load the elements from index i to index i + tileSize in dram to f1
f1 load dram(i::i+tileSize) 
// Create a write port for data to f1
// f1.push(data) 
// Create a read port for data to f1
// f1.pop(data)
// Peek the tail of f1 without removing it
// f1.peek()
```  -->

In Spatial, a FIFO is implemented using embedded FPGA memories. Therefore, you can reimplement the example in Part 2 using FIFO.

### Your Turn
* Reimplement the example in Part 2 using FIFO. You can leave your implementation under Lab1Part4FIFOExample.
* Run Scala simulation and VCS simulation. Report the results of VCS simulation.
* Synthesize the design and run it on the board. Report the number of cycles for running the design.
* Check the utilization report. Report the resource utilization of your design.

## Using Controllers
We have already introduced the usage of the Foreach controller. In this part, we will be learning about other controllers: Fold, Reduce, MemFold and MemReduce.

Before we dive into the details of these controllers, we need to understand what a fold / reduce operation is. Let's say that we have a list of elements, and we have a binary operator. Our goal is to combine all the elements together using the binary operator. The process of combining all the elements is a reduce operation.

Let's take the task of calculating the sum of a list of numbers as an example. In this case, let's say we have a list [1,2,3,4,5]. Our binary operator would be +. If we use a for loop to calculate the sum, it would look like:
```python
# pseudo code
list = [1,2,3,4,5]
accum = 0
for (i; i < list.len; i = i + 1)
  accum = accum + i
return accum
```

We can also use a reduce operation to describe the sum:
```python
# pseudo code
list = [1,2,3,4,5]
accum = reduce(list){_+_}
return accum
```

What the reduce does is that it first performs + on the first two elements, 1 and 2, and stores the temporary result 3 somewhere. It then performs + on the temporary result and the third element, 3, and gets 6. This process is repeated until no elements are left in the list.

Similarly, fold performs the same operation. The only difference is that a fold operation can take in a value as the initial starting point.

Now we have a general idea of what reduce and fold do. The Reduce and Fold controllers in Spatial implement these two operations, and can be used as follows:
```scala
val accum = Reg[T](0) // create a register to hold the reduced result
Reduce(accum)(N by n) { i =>
  // map body
}{// binary operator}
```

### Demo
Here is an example of using Reduce to compute the sum of a list of elements in Spatial. In this example, we have two Reduce controllers. The first Reduce loads a block of elements from the DRAM, and leave this block for the second Reduce to consume. The second Reduce takes in the block, adds all the elements in the block, and save the result in a register. At last, the first Reduce collects the results created by the second Reduce and coalesces the results using + to create the final sum.

```scala
@spatial object Lab1Part6ReduceExample extends SpatialApp {
  val N = 32
  val tileSize = 16
  type T = Int

  @virtualize
  def main() {
    val arraySize = N
    val srcFPGA = DRAM[T](N)
    val src = Array.tabulate[Int](arraySize) { i => i % 256 }
    setMem(srcFPGA, src)
    val destArg = ArgOut[T]

    Accel {
      // First Reduce Controller
      val accum = Reg[T](0)
      Sequential.Reduce(accum)(N by tileSize) { i =>
        val b1 = SRAM[T](tileSize)
        b1 load srcFPGA(i::i+tileSize)
        // Second Reduce Controller. In Scala / Spatial, the last element
        // of a function will be automatically returned (if your function
        // should return anything). Therefore you don't need to write a
        // return at this line explicitly.
        Reduce(0)(tileSize by 1) { ii => b1(ii) }{_+_}
      }{_+_}


      destArg := accum.value
    }

    val result = getArg(destArg)
    val gold = src.reduce{_+_}
    println("Gold: " + gold)
    println("Result: : " + result)
    println("")

    val cksum = gold == result
    println("PASS: " + cksum)
  }
}
```

Although Reduce and Fold operate in similar manners, Fold requires user to specify
a initial value to perform the reduction, whereas Reduce starts from the first
element of the list. For example, when using Fold, you have to manually
initialize your fold register:
```scala
val a = Reg[T](0)
Fold(a)(N by n){ i =>
  // Fold body
}{ // binary operator }
```
Compared to Foreach, Reduce and Fold allow users to write more precise code. Moreover, it provides the compiler with more information so that the compiler can perform more aggressive optimizations.

### Your Turn
* Use Fold controller to calculate the sum of an element. You can leave your implementation in Lab1Part6FoldExample.

MemFold and MemReduce perform the same way as Fold and Reduce; however they are used to operate on on-chip memories. We will cover more details of these two controllers in the next lab.

## Submission
* Please fill in the lab1_submission.md. After completing the lab, you can upload this file in your home directory on tucson.  We will collect the submission files from your home directory after the deadline.
