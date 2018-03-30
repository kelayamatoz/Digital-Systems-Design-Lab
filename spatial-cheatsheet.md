### Compiling Spatial
```bash
# Goto Spatial main directory
cd ~/spatial-lang
# Godo app directory of Spatial
cd ~/spatial-lang/apps/src
# Simulate an app foo with Scala backend
bin/spatial foo --sim
# Run Scala sim
cd ~/spatial-lang/
./foo.sim "arg0 arg1 ..."
# Synthesize an app foo with VCS backend, with instrumentation and retiming turned on
bin/spatial foo --synth --instrumentation --retime
# View the generated design
cd ~/spatial-lang/gen/foo
ls
# Run VCS sim
make vcs && bash run.sh arg0 arg1 ...
# Remove VCS files and prepare for synthesis
make vcs-clean
# Prepare bitstream that runs on Arria10 SoC:
make arria10 | tee make.log
```

### Basic Spatial Template
```scala
import spatial.dsl._
import org.virtualized._

object Lab1Part1RegExample extends SpatialApp {

  // Types
  type T = Int

  @virtualize
  def main() {
    // Your host code here
    ...

    Accel {
      // Your accelerator code here
      ...
    }
  }

  // Define a function with return
  @virtualize
  def functionWithReturn[T:Type:Num](a: Array[T], b: T, c: SRAM[T], ...) = { // function body }

  // Define a function without return
  @virtualize
  def functionWithoutReturn[T:Type:Num](a: Array[T], b: T, c: SRAM[T], ...) { // function body }
}
```

### Basic APIs of Spatial Memory Elements
Let's say that the data type is T.
```scala
// Host <-> Accel Registers
val argIn = ArgIn[T]
val argOut = ArgOut[T]

// Register initialized with value 0
val reg = Reg[T](0)

// DRAM
val dm = DRAM[T](N) 
setMem(dm, data)
val readData = getMem(dm)

// SRAM
val sm = SRAM[T](N)
sm load dm(k::k+n0)
dm(k::k+n0) tore sm

// FIFO
val f1 = FIFO[T](tileSize)
f1 load dram(i::i+tileSize)
f1.enq(data)
f1.deq(data)
f1.peek()

// FILO
val f1 = FILO[T](tileSize)
f1 load dram(i::i+tileSize) 
f1.push(data) 
f1.pop(data)
f1.peek()
```

### Controller Examples
```scala
# Foreach, non-sequential. Compiler will attempt to parallelize / pipeline the body
Foreach(N by n) { i =>
  ...
}

// Foreach, sequential
Sequential.Foreach(N by n) { i =>
  ...        
}

// Reduce, non-sequential
val accum = Reg[T](0) // create a register to hold the reduced result
Reduce(accum)(N by n) { i =>
  // map body	
}{// binary operator}

// Reduce, sequential
val accum = Reg[T](0) // create a register to hold the reduced result
Sequential.Reduce(accum)(N by n) { i =>
  // map body	
}{// binary operator}

// Fold, non-sequential
val accum = Reg[T](0) // create a register to hold the reduced result
Fold(accum)(N by n) { i =>
  // map body	
}{// binary operator}

// Fold, sequential
val accum = Reg[T](0) // create a register to hold the reduced result
Sequential.Reduce(accum)(N by n) { i =>
  // map body	
}{// binary operator}

```

### Useful Screen Commands
Let's say you want to start a screen s.
```bash
# Start s
screen -S s
# Detach from s
Ctrl-A D
# Show session name
Ctrl-A sessionname
# List all the screen sessions
screen -ls
# Resume s
screen -r s
# Reattach to an attached session
screen -r -d s
```

### ssh and scp
```bash
# ssh into tucson
ssh -Y username@tucson.stanford.edu
# scp a file f to /foo directory on the board. Let's say the board name is arria10.
scp f username@arria10.stanford.edu:/foo/
# scp a directory to /foo directory on the board.
scp -r f username@arria10.stanford.edu:/foo/
```
