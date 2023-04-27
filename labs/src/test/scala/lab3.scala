import Utils.toSpatialIntArray
import spatial.dsl._

@spatial class Lab3Part1Convolution extends SpatialTest {

  val Kh = 3
  val Kw = 3
  val Cmax = 16

  type T = Int

  def convolve(image: Matrix[T]): Matrix[T] = {
     val B = 16

     val R = ArgIn[Int]
     val C = ArgIn[Int]
     setArg(R, image.rows)
     setArg(C, image.cols)
     val lb_par = 8

     val img = DRAM[T](R, C)
     val imgOut = DRAM[T](R, C)

     setMem(img, image)

     Accel {
       val lb = LineBuffer[T](Kh, Cmax)

       val kh = LUT[T](3,3)(1.to[T], 0.to[T], -1.to[T],
                            2.to[T], 0.to[T], -2.to[T],
                            1.to[T], 0.to[T], -1.to[T])
       val kv = LUT[T](3,3)(1.to[T],  2.to[T],  1.to[T],
                            0.to[T],  0.to[T],  0.to[T],
                           -1.to[T], -2.to[T], -1.to[T])

       val sr = RegFile[T](Kh, Kw)
       val lineOut = SRAM[T](Cmax)
       Foreach(0 until R) { r =>
         //TODO: Load image row into linebuffer

         Sequential.Foreach(0 until C) { c =>
           Pipe {sr.reset(c == 0)}
           // Shifting in data from the line buffer
           Foreach(0 until Kh par Kh){i =>
             sr(i, *) <<= lb(i, c)
           }
           // Implement the computation part for a 2-D convolution.
           // Use horz and vert to store your convolution results.
           // Your code here:
           val horz = Reg[T](0)
           val vert = Reg[T](0)

           //TODO: Your implementation here
         }
       }
     }

     getMatrix(imgOut)
   }

   def main(args: Array[String]): Unit = {
     val R = 16
     val C = 16
     val border = 3
     val image = (0::R, 0::C){(i,j) => if (j > border && j < C-border && i > border && i < C - border) i*16 else 0}
     val ids = (0::R, 0::C){(i,j) => if (i < 2) 0 else 1}

     val kh = List((List(1,2,1), List(0,0,0), List(-1,-2,-1)))
     val kv = List((List(1,0,-1), List(2,0,-2), List(1,0,-1)))

     val output = convolve(image)

     /*
       Filters:
       1   2   1
       0   0   0
      -1  -2  -1
       1   0  -1
       2   0  -2
       1   0  -1
     */
     val gold = (0::R, 0::C){(i,j) =>
       val px00 = if ((j-2) > border && (j-2) < C-border && (i-2) > border && (i-2) < C - border) (i-2)*16 else 0
       val px01 = if ((j-1) > border && (j-1) < C-border && (i-2) > border && (i-2) < C - border) (i-2)*16 else 0
       val px02 = if ((j+0) > border && (j+0) < C-border && (i-2) > border && (i-2) < C - border) (i-2)*16 else 0
       val px10 = if ((j-2) > border && (j-2) < C-border && (i-1) > border && (i-1) < C - border) (i-1)*16 else 0
       val px11 = if ((j-1) > border && (j-1) < C-border && (i-1) > border && (i-1) < C - border) (i-1)*16 else 0
       val px12 = if ((j+0) > border && (j+0) < C-border && (i-1) > border && (i-1) < C - border) (i-1)*16 else 0
       val px20 = if ((j-2) > border && (j-2) < C-border && (i+0) > border && (i+0) < C - border) (i+0)*16 else 0
       val px21 = if ((j-1) > border && (j-1) < C-border && (i+0) > border && (i+0) < C - border) (i+0)*16 else 0
       val px22 = if ((j+0) > border && (j+0) < C-border && (i+0) > border && (i+0) < C - border) (i+0)*16 else 0
       abs(px00 * 1 + px01 * 2 + px02 * 1 - px20 * 1 - px21 * 2 - px22 * 1) + abs(px00 * 1 - px02 * 1 + px10 * 2 - px12 * 2 + px20 * 1 - px22 * 1)
     };

     printMatrix(image, "Image")
     printMatrix(gold, "Gold")
     printMatrix(output, "Output")

     val gold_sum = gold.map{g => g}.reduce{_+_}
     val output_sum = output.zip(ids){case (o,i) => i * o}.reduce{_+_}
     println("gold " + gold_sum + " =?= output " + output_sum)
     val cksum = gold_sum == output_sum
     println("PASS: " + cksum + " (Convolution_FPGA)")
     assert(cksum == 1)
   }
 }


@spatial class Lab3Part2OptimizeHomework extends SpatialTest {
  def main(args: Array[String]): Unit = {

    val nItems = 7
    val capacity = 15
    val solverValues = scala.Array(7, 9, 5, 12, 14, 6, 12)
    val solverSizes = scala.Array(3, 4, 2, 6, 7, 3, 5)

    val solver = new Solver(capacity, solverSizes, solverValues)
    solver.solveKnapsackIterative()
    solver.printDPArray()

    val sizes = toSpatialIntArray(solverSizes)
    val values = toSpatialIntArray(solverValues)

    val dpMatrix = DRAM[Int]((nItems + 1).to[I32], (capacity + 1).to[I32])
    val dSizes = DRAM[Int](nItems.to[I32])
    val dValues = DRAM[Int](nItems.to[I32])

    val bucketSize = 2 * nItems

    def getBucketDRAM = DRAM[Int](bucketSize.to[I32])

    val resultBuckets = getBucketDRAM
    val resultBucketSizes = getBucketDRAM
    val resultBucketValues = getBucketDRAM
    val resultNBuckets = ArgOut[Int]

    setMem(
      dpMatrix,
      Matrix.tabulate[Int]((nItems + 1).to[I32], (capacity + 1).to[I32])((_, _) =>
        -1.to[Int]))
    setMem(dSizes, sizes)
    setMem(dValues, values)

    val dpMatrixResult = DRAM[Int]((nItems + 1).to[I32], (capacity + 1).to[I32])

    Accel {
      val nRows = (nItems + 1).to[I32]
      val nCols = (capacity + 1).to[I32]
      val step = 1.to[I32]
      val base = 0.to[I32]
      val sizes = SRAM[Int](nRows - step)
      val values = SRAM[Int](nRows - step)
      val DPArray = SRAM[Int](nRows, nCols)
      val ip = 4.to[I32]

      sizes load dSizes(base :: (nRows - 1.to[Int]) par ip)
      values load dValues(base :: (nRows - 1.to[Int]) par ip)
      DPArray load dpMatrix(base :: nRows, base :: nCols par ip)

      // Step 1: Generate the score table. The score table is stored in DPArray.

      // TODO: Your code here

      // Step 2: traverse the DP matrix backward and find the optimal path.
      // val traverseState = 0.to[I32]
      val doneState = 0.to[I32]
      val pickedBuckets = FIFO[Int](bucketSize.to[I32])
      val pickedValues = FIFO[Int](bucketSize.to[I32])
      val pickedSizes = FIFO[Int](bucketSize.to[I32])

      val sIdx = Reg[Int](nCols - step)
      val nBuckets = Reg[Int](base)

      FSM(/*TODO*/)(/*TODO*/) { state =>

        // TODO: Your code here.

      } { /*TODO: Fill in next state logic*/}

      // Step 3: store back the results
      resultNBuckets := nBuckets.value
      resultBuckets(base :: pickedBuckets.numel) store pickedBuckets
      resultBucketSizes(base :: pickedSizes.numel) store pickedSizes
      resultBucketValues(base :: pickedValues.numel) store pickedValues
    }

    val buckets = getMem(resultBuckets)
    val bucketSizes = getMem(resultBucketSizes)
    val bucketValues = getMem(resultBucketValues)
    val nBuckets = getArg(resultNBuckets)
    printArray(buckets, "buckets = ")
    printArray(bucketSizes, "bucketSizes = ")
    printArray(bucketValues, "bucketValues = ")
    println("nBuckets = " + nBuckets)
    assert(1 == 1)
  }
}
