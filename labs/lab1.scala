import spatial.dsl._


// Register
@spatial object Lab1Part1RegExample extends SpatialApp {

  type T = Int

  def main(args: Array[String]): Unit = {
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
    val cksum = gold == argRegOutResult
    println("PASS = " + cksum + "(Lab1Part1RegExample)")
  }
}


// DRAM/SRAM
@spatial object Lab1Part2DramSramExample extends SpatialApp {

  val N = 32
  type T = Int

  def simpleLoadStore(srcHost: Array[T], value: T) = {
    val tileSize = 16
    val srcFPGA = DRAM[T](N)
    val dstFPGA = DRAM[T](N)
    setMem(srcFPGA, srcHost)

    val x = ArgIn[T]
    setArg(x, value)
    Accel {
      Sequential.Foreach(N by tileSize) { i =>
        val b1 = SRAM[T](tileSize)

        b1 load srcFPGA(i::i+tileSize)

        val b2 = SRAM[T](tileSize)
        Foreach(tileSize by 1) { ii =>
          b2(ii) = b1(ii) * x
        }

        dstFPGA(i::i+tileSize) store b2
      }
    }
    getMem(dstFPGA)
  }

  def main(args: Array[String]): Unit = {
    val arraySize = N
    val value = args(0).to[Int]

    val src = Array.tabulate[Int](arraySize) { i => i % 256 }
    val dst = simpleLoadStore(src, value)

    val gold = src.map { _ * value }

    println("Sent in: ")
    (0 until arraySize) foreach { i => print(gold(i) + " ") }
    println("Got out: ")
    (0 until arraySize) foreach { i => print(dst(i) + " ") }
    println("")

    val cksum = dst.zip(gold){_ == _}.reduce{_&&_}
    println("PASS: " + cksum + "(Lab1Part2DramSramExample)")
  }
}


// // FIFO
// @spatial object Lab1Part4FIFOExample extends SpatialApp {
//   val N = 32
//   type T = Int

//   def simpleLoadStore(srcHost: Array[T], value: T) = {
//     val tileSize = 16
//     val srcFPGA = DRAM[T](N)
//     val dstFPGA = DRAM[T](N)
//     setMem(srcFPGA, srcHost)

//     val x = ArgIn[T]
//     setArg(x, value)
//     Accel {
//       // Your code here
//     }
//     getMem(dstFPGA)
//   }

//   def main(args: Array[String]): Unit = {
//     val arraySize = N
//     val value = args(0).to[Int]

//     val src = Array.tabulate[Int](arraySize) { i => i % 256 }
//     val dst = simpleLoadStore(src, value)

//     val gold = src.map { _ * value }

//     println("Sent in: ")
//     (0 until arraySize) foreach { i => print(gold(i) + " ") }
//     println("Got out: ")
//     (0 until arraySize) foreach { i => print(dst(i) + " ") }
//     println("")

//     val cksum = dst.zip(gold){_ == _}.reduce{_&&_}
//     println("PASS: " + cksum + "(Lab1Part4FIFOExample)")
//   }
// }


// Reduce
@spatial object Lab1Part5ReduceExample extends SpatialApp {
  val N = 32
  val tileSize = 16
  type T = Int

  def main(args: Array[String]): Unit = {
    val arraySize = N
    val srcFPGA = DRAM[T](N)
    val src = Array.tabulate[Int](arraySize) { i => i % 256 }
    setMem(srcFPGA, src)
    val destArg = ArgOut[T]

    Accel {
      val accum = Reg[T](0)
      Sequential.Reduce(accum)(N by tileSize) { i =>
        val b1 = SRAM[T](tileSize)
        b1 load srcFPGA(i::i+tileSize)
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
    println("PASS: " + cksum + "(Lab1Part5ReduceExample)")
  }
}


// // Fold
// @spatial object Lab1Part6FoldExample extends SpatialApp {
//   val N = 32
//   val tileSize = 16
//   type T = Int


//   def main(args: Array[String]): Unit = {
//     val arraySize = N
//     val srcFPGA = DRAM[T](N)
//     val src = Array.tabulate[Int](arraySize) { i => i % 256 }
//     setMem(srcFPGA, src)
//     val destArg = ArgOut[T]

//     Accel {
//       // Your code here
//     }

//     val result = getArg(destArg)
//     val gold = src.reduce{_+_}
//     println("Gold: " + gold)
//     println("Result: : " + result)
//     println("")

//     val cksum = gold == result
//     println("PASS: " + cksum + "(Lab1Part6FoldExample)")
//   }
// }
