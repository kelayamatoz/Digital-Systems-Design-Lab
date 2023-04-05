import spatial.dsl._


// @spatial object Lab3Part1Convolution extends SpatialApp {

//   val Kh = 3
//   val Kw = 3
//   val Cmax = 16

//   type T = Int

//   def convolve(image: Matrix[T]): Matrix[T] = {
//     val B = 16

//     val R = ArgIn[Int]
//     val C = ArgIn[Int]
//     setArg(R, image.rows)
//     setArg(C, image.cols)
//     val lb_par = 8

//     val img = DRAM[T](R, C)
//     val imgOut = DRAM[T](R, C)

//     setMem(img, image)

//     Accel {
//       val lb = LineBuffer[T](Kh, Cmax)

//       val kh = LUT[T](3,3)(1.to[T], 0.to[T], -1.to[T],
//                            2.to[T], 0.to[T], -2.to[T],
//                            1.to[T], 0.to[T], -1.to[T])
//       val kv = LUT[T](3,3)(1.to[T],  2.to[T],  1.to[T],
//                            0.to[T],  0.to[T],  0.to[T],
//                           -1.to[T], -2.to[T], -1.to[T])

//       val sr = RegFile[T](Kh, Kw)
//       val lineOut = SRAM[T](Cmax)

//       // TODO: Your implementation here

//     }

//     getMatrix(imgOut)
//   }

//   def main(args: Array[String]): Unit = {
//     val R = 16
//     val C = 16
//     val border = 3
//     val image = (0::R, 0::C){(i,j) => if (j > border && j < C-border && i > border && i < C - border) i*16 else 0}
//     val ids = (0::R, 0::C){(i,j) => if (i < 2) 0 else 1}

//     val kh = List((List(1,2,1), List(0,0,0), List(-1,-2,-1)))
//     val kv = List((List(1,0,-1), List(2,0,-2), List(1,0,-1)))

//     val output = convolve(image)

//     /*
//       Filters:
//       1   2   1
//       0   0   0
//      -1  -2  -1

//       1   0  -1
//       2   0  -2
//       1   0  -1

//     */
//     val gold = (0::R, 0::C){(i,j) =>
//       val px00 = if ((j-2) > border && (j-2) < C-border && (i-2) > border && (i-2) < C - border) (i-2)*16 else 0
//       val px01 = if ((j-1) > border && (j-1) < C-border && (i-2) > border && (i-2) < C - border) (i-2)*16 else 0
//       val px02 = if ((j+0) > border && (j+0) < C-border && (i-2) > border && (i-2) < C - border) (i-2)*16 else 0
//       val px10 = if ((j-2) > border && (j-2) < C-border && (i-1) > border && (i-1) < C - border) (i-1)*16 else 0
//       val px11 = if ((j-1) > border && (j-1) < C-border && (i-1) > border && (i-1) < C - border) (i-1)*16 else 0
//       val px12 = if ((j+0) > border && (j+0) < C-border && (i-1) > border && (i-1) < C - border) (i-1)*16 else 0
//       val px20 = if ((j-2) > border && (j-2) < C-border && (i+0) > border && (i+0) < C - border) (i+0)*16 else 0
//       val px21 = if ((j-1) > border && (j-1) < C-border && (i+0) > border && (i+0) < C - border) (i+0)*16 else 0
//       val px22 = if ((j+0) > border && (j+0) < C-border && (i+0) > border && (i+0) < C - border) (i+0)*16 else 0
//       abs(px00 * 1 + px01 * 2 + px02 * 1 - px20 * 1 - px21 * 2 - px22 * 1) + abs(px00 * 1 - px02 * 1 + px10 * 2 - px12 * 2 + px20 * 1 - px22 * 1)
//     };

//     printMatrix(image, "Image")
//     printMatrix(gold, "Gold")
//     printMatrix(output, "Output")

//     val gold_sum = gold.map{g => g}.reduce{_+_}
//     val output_sum = output.zip(ids){case (o,i) => i * o}.reduce{_+_}
//     println("gold " + gold_sum + " =?= output " + output_sum)
//     val cksum = gold_sum == output_sum
//     println("PASS: " + cksum + " (Convolution_FPGA)")
//   }
// }


// @spatial object Lab3Part2NW extends SpatialApp {
//  /*

//   Needleman-Wunsch Genetic Alignment algorithm

//     LETTER KEY:         Scores                   Ptrs
//       a = 0                   T  T  C  G                T  T  C  G
//       c = 1                0 -1 -2 -3 -4 ...         0  ←  ←  ←  ← ...
//       g = 2             T -1  1  0 -1 -2          T  ↑  ↖  ←  ←  ←
//       t = 3             C -2  0 -1  1  0          C  ↑  ↑  ↑  ↖  ←
//       - = 4             G -3 -2 -2  0  2          G  ↑  ↑  ↑  ↑  ↖
//       _ = 5             A -4 -3 -3 -1  1          A  ↑  ↑  ↑  ↑  ↖
//                            .                         .
//                            .                         .
//                            .                         .

//     PTR KEY:
//       ← = 0 = skipB
//       ↑ = 1 = skipA
//       ↖ = 2 = align

//  */

//   @struct case class nw_tuple(score: Int16, ptr: Int16)

//   def main(args: Array[String]): Unit = {

//     // FSM setup
//     // In this state, we need to continue traverse from bottom right to top left
//     val traverseState = 0
//     // In this state, we need to pad both sequences by adding an INDEL
//     val padBothState = 1
//     // In this state, we are done traversing
//     val doneState = 2


//     val a = 'a'.to[Int8]
//     val c = 'c'.to[Int8]
//     val g = 'g'.to[Int8]
//     val t = 't'.to[Int8]
//     val d = '-'.to[Int8]
//     val underscore = '_'.to[Int8]
//     val dash = ArgIn[Int8]

//     setArg(dash,d)

//     val par_load = 1
//     val par_store = 1
//     val row_par = 1

//     val SKIPB = 0
//     val SKIPA = 1
//     val ALIGN = 2
//     val MATCH_SCORE = 1
//     val MISMATCH_SCORE = -1
//     val GAP_SCORE = -1
//     // bash run.sh tcgacgaaataggatgacagcacgttctcgt ttcgagggcgcgtgtcgcggtccatcgacat

//     val seqa_string = args(0) //"tcgacgaaataggatgacagcacgttctcgtattagagggccgcggtacaaaccaaatgctgcggcgtacagggcacggggcgctgttcgggagatcgggggaatcgtggcgtgggtgattcgccggc"
//     val seqb_string = args(1) //"ttcgagggcgcgtgtcgcggtccatcgacatgcccggtcggtgggacgtgggcgcctgatatagaggaatgcgattggaaggtcggacgggtcggcgagttgggcccggtgaatctgccatggtcgat"
//     val measured_length = seqa_string.length
//     val length = ArgIn[Int]
//     val lengthx2 = ArgIn[Int]
//     setArg(length, measured_length)
//     setArg(lengthx2, 2*measured_length)

//     val max_length = 64
//     assert(max_length >= length, "Cannot have string longer than 64 elements")

//     // Prepare the two sequences.

//     val seqa_bin = seqa_string.map{c => c.to[Int8] }
//     val seqb_bin = seqb_string.map{c => c.to[Int8] }

//     val seqa_dram_raw = DRAM[Int8](length)
//     val seqb_dram_raw = DRAM[Int8](length)
//     val seqa_dram_aligned = DRAM[Int8](lengthx2)
//     val seqb_dram_aligned = DRAM[Int8](lengthx2)
//     setMem(seqa_dram_raw, seqa_bin)
//     setMem(seqb_dram_raw, seqb_bin)

//     Accel{
//       val seqa_sram_raw = SRAM[Int8](max_length)
//       val seqb_sram_raw = SRAM[Int8](max_length)

//       // These two FIFOs are used to store the aligned results
//       val seqa_fifo_aligned = FIFO[Int8](max_length*2)
//       val seqb_fifo_aligned = FIFO[Int8](max_length*2)

//       seqa_sram_raw load seqa_dram_raw(0::length par par_load)
//       seqb_sram_raw load seqb_dram_raw(0::length par par_load)

//       val score_matrix = SRAM[nw_tuple](max_length+1,max_length+1)

//       // Step 1: Build score matrix
//       Foreach(length+1 by 1 par row_par) { r =>
//         // TODO: Populate the score matrix row by row
//         // Your implementation here
//       }

//       // Step 2: Reconstruct the path
//       val b_addr = Reg[Int](0) // Index of the current position in sequence b
//       val a_addr = Reg[Int](0) // Index of the current position in sequence a
//       Parallel{b_addr := length; a_addr := length} // Set the position to start from bottom right
//       val done_backtrack = Reg[Bit](false) // A flag to tell if traceback is done
//       // TODO: Implement an FSM that traces the path information to create the best aligntment. You can use doneState, padBothState, traverseState to track the current state of your FSM.
//       // Your implementation here

//       // Alignment completed. Send the aligned results back.
//       Parallel{
//         seqa_dram_aligned(0::length*2 par par_store) store seqa_fifo_aligned
//         seqb_dram_aligned(0::length*2 par par_store) store seqb_fifo_aligned
//       }
//     }

//     val seqa_aligned_result = getMem(seqa_dram_aligned)
//     val seqb_aligned_result = getMem(seqb_dram_aligned)
//     val seqa_aligned_string = charArrayToString(seqa_aligned_result.map(_.to[Char]))
//     val seqb_aligned_string = charArrayToString(seqb_aligned_result.map(_.to[Char]))

//     // Pass if >70% match
//     val matches = seqa_aligned_result.zip(seqb_aligned_result){(a,b) => if ((a == b) || (a == dash) || (b == dash)) 1 else 0}.reduce{_+_}
//     val cksum = matches.to[Float] > 0.70.to[Float]*measured_length.to[Float]*2

//     println("Result A: " + seqa_aligned_string)
//     println("Result B: " + seqb_aligned_string)
//     println("Found " + matches + " matches out of " + measured_length*2 + " elements")
//     println("PASS: " + cksum + " (Lab3Part2NW)")
//   }
// }
