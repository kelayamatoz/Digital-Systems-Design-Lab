import spatial.dsl._
import org.virtualized._

object RayTracing extends SpatialApp {
  
  type B = FixPt[FALSE, _1, _0]
  type T = FixPt[TRUE, _16, _20]
  type L = FixPt[FALSE, _16, _0]
  type C = FixPt[FALSE, _13, _13]

  @struct class Vec3(x: T, y: T, z: T)
  @struct class Color(r: C, g: C, b: C)
  @struct class Triangle(v0: Vec3, v1: Vec3, v2: Vec3, normal: Vec3, color: Color, is_diffuse: B, fill: B)
  @struct class RayTraceOut(intersection: Vec3, hit: B, normal: Vec3, color: Color)
  
  val width = 120
  val height = 90
  val pixel_offset = 0.1 // 0.03125 //1/32
  val cam_distance = 40 //1280/32 = 40 so distance = full width of screen for roughly 30 degrees?
  val pi = 3.14159.to[T]
 
  val lr_seed = 11111
  val ud_seed = 12345
 
  @virtualize
  def cross(a: Vec3, b: Vec3) : Vec3 = {
    Vec3(a.y*b.z - a.z*b.y, a.z*b.x - a.x*b.z, a.x*b.y - a.y*b.x)

  }
  
  @virtualize
  def dot(a: Vec3, b: Vec3) : T = {
    a.x*b.x + a.y*b.y + a.z*b.z
  }
  
  @virtualize
  def vec_add(a: Vec3, b: Vec3) : Vec3 = {
    Vec3(a.x + b.x, a.y + b.y, a.z + b.z)
  }
  
  @virtualize
  def vec_sub(a: Vec3, b: Vec3) : Vec3 = {
    Vec3(a.x - b.x, a.y - b.y, a.z - b.z)
  }
  
  @virtualize
  def vec_scalar_mult(a: Vec3, b: T) : Vec3 = {
    Vec3(a.x * b, a.y * b, a.z * b)
  }
  
  @virtualize
  def isqrt(i: T) : T = {
    //val number = i.to[Float]
    //val x2 = number * 0.5.to[Float]
    //val ii = number.as[Int]
    //val j = 0x5f3759df - ( ii >> 1)
    //val k = j.as[Float]
    //val l = k * ( 1.5.to[Float] - (x2 * k * k))
    //l.to[T]
    val x = sqrt_approx(i)
    1/x
  }

  @virtualize
  def RayTrace(rayOrigin: Vec3, rayVector: Vec3, triangles: DRAM1[Triangle], num_tris: UInt8) : RayTraceOut = {
    val tris_local = SRAM[Triangle](512)
    tris_local load triangles
    
    //Set up the regs for storing the nearest triangle
    val nearest_tri = Reg[Triangle] 
    val nearest_distance = Reg[T]
    val nearest_intersection = Reg[Vec3]
    val had_hit = Reg[B]

    nearest_distance := 0.to[T] //Setup the regs as 0
    nearest_intersection := Vec3(0,0,0)
    had_hit := 0
    nearest_tri := Triangle(Vec3(0,0,0), Vec3(0,0,0), Vec3(0,0,0), Vec3(0,0,0), Color(0,0,0), 1.to[B], 0.to[B])
    
    val tri_counter = Reg[UInt8]
    
    // Iterate through triangles buffer and calculate results for each
    Sequential {
      
      tri_counter := num_tris
      
      FSM[Int](state => state == 0) { state =>
        tri_counter := tri_counter.value - 1
	
	val Epsilon = (0.000001).to[T]
	
	val triangle = Reg[Triangle]
	triangle := tris_local(tri_counter.value.to[Index])

	val edge1 = triangle.v1 - triangle.v0
	val edge2 = triangle.v2 - triangle.v0
	val h = cross(rayVector, edge2)
	val a = dot(edge1, h)
	val f = 1/a
	val s = vec_sub(rayOrigin, triangle.v0)
	val u = f * dot(s, h)
	val q = cross(s, edge1)
	val v = f * dot(rayVector, q)
	val t = f * dot(edge2, q)
	
	val intersection = vec_add(rayOrigin, vec_scalar_mult(rayVector, t))
	val hit = mux(t > Epsilon && u >= 0 && u <= 1 && v >= 0 && u + v <= 1, 1.to[B], 0.to[B])

	val distance = get_distance(rayOrigin, intersection) //TODO replace this with t
	val is_nearest = mux((distance < nearest_distance && hit == 1.to[B]) || (nearest_distance == 0.to[T] && hit == 1.to[B]), 1.to[B], 0.to[B]) //Added check for 0 distance

	nearest_distance := mux(is_nearest == 1.to[B], distance, nearest_distance)
	nearest_tri := mux(is_nearest == 1.to[B], triangle, nearest_tri)
	nearest_intersection := mux(is_nearest == 1.to[B], intersection, nearest_intersection)
	had_hit := mux(had_hit == 1.to[B] || hit == 1.to[B], 1.to[B], 0.to[B])

      } { state => mux(tri_counter == 0.to[UInt8], 1, 0) }
    }

    RayTraceOut(nearest_intersection, had_hit, nearest_tri.normal, nearest_tri.color)

  }
  
  @virtualize // Returns diffuse reflection ray direction
  def CosineSampleHemisphere(normal: Vec3, u: T, v: T) : Vec3 = {
    val r = sqrt_approx(u)
    val theta = 6.28.to[T] * v - pi
    
    val sdir = cross(normal, mux(abs(normal.x) < 0.5.to[T], Vec3(1,0,0), Vec3(0,1,0)))
    val tdir = cross(normal, sdir)
    
    vec_scalar_mult(sdir, r*cos_taylor(theta)) + vec_scalar_mult(tdir, r*sin_taylor(theta)) + vec_scalar_mult(normal, sqrt_approx(1.to[T]-u))
  }

  @virtualize
  def LFSR(lfsr: Reg[L]) : T = {
    val bit = ((lfsr >> 0) ^ (lfsr >> 2) ^ (lfsr >> 3) ^ (lfsr >> 5) ) & 1
    val shifted = (lfsr >> 1) | (bit << 15)

    lfsr := shifted    
    shifted.to[T] >> 16
  }
  
  @virtualize
  def CalcShadow(point: Vec3, normal: Vec3, triangles: DRAM1[Triangle], num_tris: UInt8) : Color = {
    val light = Vec3(4,2,9)
    val vec_to_light = light - point
    val trace_out = RayTrace(point, vec_to_light, triangles, num_tris)
    
    val vec_to_light_n = vec_scalar_mult(vec_to_light, isqrt(dot(vec_to_light, vec_to_light)))
    val nDl = dot(normal, vec_to_light_n)
    
    val dist2_light = get_distance(point, light)
    val dist2_hit = get_distance(point, trace_out.intersection)
    
    // Light brightness/color
    val weight = (nDl / dist2_light).to[C]
    val out = Color(weight * 100, weight * 100, weight * 100)
    
    mux((nDl > 0) && (trace_out.hit == 0.to[B]) || (trace_out.hit == 1.to[B] && (dist2_light < dist2_hit)), out, Color(0, 0, 0))
  }
  
  @virtualize
  def PathtracePixel(cam_orig: Vec3, cam_dir: Vec3, seed_u: L, seed_v: L, triangles: DRAM1[Triangle], num_tris: UInt8) : Color = {
    
    val attenuation = Reg[Color]
    val pixel_result = Reg[Color]

    val next_ray_orig = Reg[Vec3]
    val next_ray_dir = Reg[Vec3]

    val trace_result = Reg[RayTraceOut]
    val bounces = Reg[UInt2]
    
    val u = Reg[L]
    val v = Reg[L]
    
    Sequential {

      // Initialize regs
      next_ray_orig := cam_orig
      next_ray_dir := cam_dir
        
      attenuation := Color(1, 1, 1)
      pixel_result := Color(0, 0, 0)
      bounces := 0
    
      u := seed_u
      v := seed_v

      FSM[Int](state => state != 1) { state =>
        // Perform main trace
        trace_result := RayTrace(next_ray_orig.value, next_ray_dir.value, triangles, num_tris)
      
        // Trace lights
        val radiance = mux(trace_result.hit == 1.to[B], CalcShadow(trace_result.intersection, trace_result.normal, triangles, num_tris), Color(0, 0, 0))
        
        val new_attenuation = attenuation.value * trace_result.color
        
        // Update registers for next iteration
        
        // Set up next (reflection) ray
        next_ray_orig := trace_result.intersection
        next_ray_dir := CosineSampleHemisphere(trace_result.normal, LFSR(u), LFSR(v))
        
        pixel_result := pixel_result.value + (radiance * new_attenuation)
        attenuation := new_attenuation
        bounces := bounces.value + 1
      
      } { state => // If hit nothing or above max bounces, terminate
        mux((bounces > 3) || (trace_result.hit == 0.to[B]), 1, 0)
      }
    }
    
    pixel_result
  }

  // Returns squared distance
  @virtualize
  def get_distance(a: Vec3, b: Vec3) : T = {
    (((a.x - b.x) * (a.x - b.x)) + ((a.y - b.y) * (a.y - b.y)) + ((a.z - b.z) * (a.z - b.z))) //Don't need sqrt bc square distance is fine
  }

  @virtualize
  def get_normal(a: Vec3, b: Vec3, c: Vec3) : Vec3 = {
    val U = b - a
    val V = c - a
    cross(U, V)
  }
  
  @virtualize
  def add_sample(pix_old: Color, pix_new: Color, pass: Int) : Color = {
    val new_weight = 1 / pass.to[C]
    val old_weight = 1.to[C] - new_weight
    
    val old_weighted = Color(pix_old.r * old_weight, pix_old.g * old_weight, pix_old.b * old_weight)
    val new_weighted = Color(pix_new.r * new_weight, pix_new.g * new_weight, pix_new.b * new_weight)
    
    old_weighted + new_weighted
  }
  
  @virtualize
  def pack(src: Reg[Triangle]) : Array[T] = {
    val v0x = src.v0.x.to[T]
    val v0y = src.v0.y.to[T]
    val v0z = src.v0.z.to[T]
    val v1x = src.v1.x.to[T]
    val v1y = src.v1.y.to[T]
    val v1z = src.v1.z.to[T]
    val v2x = src.v2.x.to[T]
    val v2y = src.v2.y.to[T]
    val v2z = src.v2.z.to[T]
    val nx = src.normal.x.to[T]
    val ny = src.normal.y.to[T]
    val nz = src.normal.z.to[T]
    val cr = src.color.r.to[T]
    val cb = src.color.g.to[T]
    val cg = src.color.b.to[T]
    val d = src.is_diffuse.to[T]
    val f = src.fill.to[T]
    Array[T](v0x, v0y, v0z, v1x, v1y, v1z, v2x, v2y, v2z, nx, ny, nz, cr, cg, cb, d, f)
  }
  
  @virtualize
  def unpack(src: Array[T], i: Int) : Triangle =  {
    val base_idx = i * 17
    val v0 = Vec3(src(base_idx), src(base_idx + 1), src(base_idx + 2))
    val v1 = Vec3(src(base_idx + 3), src(base_idx + 4), src(base_idx + 5))
    val v2 = Vec3(src(base_idx + 6), src(base_idx + 7), src(base_idx + 8))
    val normal = Vec3(src(base_idx + 9), src(base_idx + 10), src(base_idx + 11))
    val color = Color(src(base_idx + 12).to[C], src(base_idx + 13).to[C], src(base_idx + 14).to[C])
    Triangle(v0, v1, v2, normal, color, src(base_idx + 15).to[B], src(base_idx + 16).to[B])
  }
  
  @virtualize
  def main() {
    
    /** BEGIN INPUT SETUP */
    
    // Load into DRAM
    val tris_csv = loadCSV1D[T]("triangle_full.csv")
    val trianglebuf = Array.tabulate(12) { i =>
      unpack(tris_csv, i)
    }

    val triangles = DRAM[Triangle](512)
    setMem(triangles, trianglebuf)
    
    val num_tris = ArgIn[UInt8]
    setArg(num_tris, 12.to[UInt8])

    // Ray origin and direction
    val rayOrigin = ArgIn[Vec3]
    val rayVector = ArgIn[Vec3]
    val lr_vector = ArgIn[Vec3]
    val ud_vector = ArgIn[Vec3]

    setArg(rayOrigin, Vec3(6, -10, 7))
    setArg(rayVector, Vec3(-0.43301.to[T], 0.75.to[T], -0.5.to[T]))
    
    //The perpendicular vectors to the rayVector so that the camera is easy
    setArg(lr_vector, Vec3(-0.86603.to[T], -0.5.to[T], 0)) // LEFT
    setArg(ud_vector, Vec3(-0.25.to[T], 0.43301.to[T], 0.86603.to[T])) // UP


    /** END INPUT SETUP */
    
    println(width)
    println(height)
    
    Accel {
      val image = SRAM[Color](width, height)
      Foreach(0 until width) { w =>
        Foreach(0 until height) { h =>
          image(w, h) = Color(0,0,0)
        }
      }
      
      val lr_lfsr = Reg[L]
      val ud_lfsr = Reg[L]
      lr_lfsr := lr_seed
      ud_lfsr := ud_seed
      
      val u_seeder = Reg[L]
      val v_seeder = Reg[L]
      u_seeder := 54321
      v_seeder := 32541
      
      val iterations = 1

      val cam_center = vec_add(rayOrigin, vec_scalar_mult(rayVector, cam_distance))
      Sequential.Foreach(0 until iterations) { i =>
        val jitter_lr = LFSR(lr_lfsr) * pixel_offset.to[T]
        val jitter_ud = LFSR(ud_lfsr) * pixel_offset.to[T]
        Sequential.Foreach(0 until height) { y =>
          LFSR(u_seeder)
          LFSR(v_seeder)
          
          Foreach(0 until width) { x =>
            val lr_offset = (x.to[T] - (width/2))*pixel_offset.to[T] + jitter_lr
            val ud_offset = ((height/2) - y.to[T])*pixel_offset.to[T] + jitter_ud
            val lr_spot = vec_add(cam_center, vec_scalar_mult(lr_vector, lr_offset))
            val pixel_vector = vec_add(lr_spot, vec_scalar_mult(ud_vector, ud_offset))
            val u_seed = u_seeder * (x.to[L] + 100)
            val v_seed = v_seeder * (x.to[L] + 100)
            val ret_val = PathtracePixel(rayOrigin, pixel_vector, u_seed, v_seed, triangles, num_tris)
            image(x, y) = add_sample(image(x, y), ret_val, i + 1)
          }
        }
        Sequential.Foreach(0 until height) { y =>
          Sequential.Foreach(0 until width) { x =>
            val pix = image(x, y)
            println(pix.r)
            println(pix.g)
            println(pix.b)
          }
        }
      }
    }
  }
}

