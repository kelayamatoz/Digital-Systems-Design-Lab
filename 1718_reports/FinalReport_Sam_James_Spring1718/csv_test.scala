import spatial.dsl._
import org.virtualized._

object csv_test extends SpatialApp {


  type B = FixPt[FALSE, _1, _0]
  type T = FixPt[TRUE, _16, _20]
  type L = FixPt[FALSE, _16, _0]
  type C = FixPt[FALSE, _13, _13]
  type A = FixPt[TRUE, _7, _0]
  @struct class Vec3(x: T, y: T, z: T)
  @struct class Color(r: C, g: C, b: C)
  @struct class Triangle(v0: Vec3, v1: Vec3, v2: Vec3, normal: Vec3, color: Color, is_diffuse: B, fill: B)

  @virtualize
  def unpacker(src: RegFile1[Int], dest: Reg[Triangle]) {
    val v0 = Vec3(src(0).to[T], src(1).to[T], src(2).to[T])
    val v1 = Vec3(src(3).to[T], src(4).to[T], src(5).to[T])
    val v2 = Vec3(src(6).to[T], src(7).to[T], src(8).to[T])
    
    val normal = Vec3(src(9).to[T], src(10).to[T], src(11).to[T])
    
    val color = Color(src(12).to[C], src(13).to[C], src(14).to[C])
    
    dest := Triangle(v0, v1, v2, normal, color, src(15).to[B], src(16).to[B])
  }
    
  @virtualize
    def main() { 
      val num_tris = ArgIn[UInt8]
      setArg(num_tris, 12.to[UInt8])
      val triangles_string = loadCSV2D[MString]("/home/samj875/triangles2.csv", " ") //load the triangle as a 9xn matrix of type T
      val colors_string = loadCSV1D[MString]("/home/samj875/colors.csv", " ") //Load the colors as a 3xn matrix
      val nums = argon.lang.String.string2num(triangles_string.apply(0,0))
      val triangle_buf = Array.tabulate(17){ i =>
      
        val num = nums(i)
        val temp = num.to[A]
        temp.to[Int]
      }
 
    printArray(triangle_buf)

    val triangles_dram = DRAM[Int](512)
    setMem(triangles_dram, triangle_buf)
 
      Accel {
      	val triangle_int = RegFile[Int](17)
      	triangle_int load triangles_dram(0::17)

      	println(triangle_int)

      	val triangle_reg = Reg[Triangle]

      	unpacker(triangle_int, triangle_reg)

      	println(triangle_reg)

      	println("Worked")
      }
    }

}
