# An Overview on CORDIC Method (w/ Chatgpt)

This is a pseudocode example for implementing the CORDIC algorithm to compute the square root. The CORDIC algorithm will be implemented in vectoring mode to find the square root.

## Pseudocode Overview
1. **Initialization**:
   - Set initial values for $ x $, $ y $, and $ z $.
   - Precompute the CORDIC angles θ_i (atan values).

2. **CORDIC Iterations**:
   - Iterate through a fixed number of steps to update $ x $, $ y $, and $ z $ using the CORDIC equations in vectoring mode.

3. **Scaling**:
   - Apply the CORDIC gain $ K $ to the final result to get the accurate square root.


## Python Pseudocode
```python
def cordic_sqrt(value, iterations):
    if value == 0:
        return 0
    
    # Initial values
    x = value
    y = 0.0
    z = 0.0
    
    # Precomputed angles for vectoring mode (atan(2^-i))
    angles = [2 ** (-i) for i in range(iterations)]
    
    # Vectoring mode
    for i in range(iterations):
        if y < 0:
            d = -1
        else:
            d = 1
        
        new_x = x - d * y * angles[i]
        new_y = y + d * x * angles[i]
        z = z - d * angles[i]
        
        x = new_x
        y = new_y
    
    # The value of x will be approximately sqrt(value) * K
    # K is the CORDIC gain factor and can be precomputed or approximated
    K = 1.646760258121065  # For 16 iterations
    sqrt_value = x / K
    
    return sqrt_value

# Example usage
value = 9.0  # Calculate sqrt(9)
iterations = 16
result = cordic_sqrt(value, iterations)
print(f"sqrt({value}) ≈ {result}")
```


## Verilog Pseudocode

```verilog
module cordic_sqrt (
    input wire clk,
    input wire rst,
    input wire [31:0] in_value, // Input value in fixed-point format
    output reg [31:0] sqrt_out  // Output value in fixed-point format
);

    // Fixed-point format parameters (e.g., Q16.16)
    parameter INTEGER_BITS = 16;
    parameter FRACTIONAL_BITS = 16;
    parameter TOTAL_BITS = INTEGER_BITS + FRACTIONAL_BITS;

    // CORDIC iteration parameters
    parameter ITERATIONS = 16;
    reg signed [TOTAL_BITS-1:0] x, y, z;
    reg signed [TOTAL_BITS-1:0] new_x, new_y;
    reg signed [31:0] angles [0:ITERATIONS-1];

    // Precompute atan(2^-i) in fixed-point format
    initial begin
        angles[0] = 32'h20000000; // atan(2^0) = 0.785398163 (fixed-point representation)
        angles[1] = 32'h12E4051F; // atan(2^-1) = 0.463647609
        angles[2] = 32'h09FB385B; // atan(2^-2) = 0.244978663
        angles[3] = 32'h051111D4; // atan(2^-3) = 0.124354995
        // Add more precomputed angles as needed
    end

    // CORDIC Gain
    parameter K = 32'h26DD3B6A; // Approximate value of 1/K for 16 iterations in fixed-point

    always @(posedge clk or posedge rst) begin
        if (rst) begin
            sqrt_out <= 0;
        end else begin
            // Initialize variables
            x <= in_value;
            y <= 0;
            z <= 0;

            // CORDIC iterations
            integer i;
            for (i = 0; i < ITERATIONS; i = i + 1) begin
                if (y < 0) begin
                    new_x = x - (y >>> i);
                    new_y = y + (x >>> i);
                    z = z - angles[i];
                end else begin
                    new_x = x + (y >>> i);
                    new_y = y - (x >>> i);
                    z = z + angles[i];
                end
                x = new_x;
                y = new_y;
            end

            // Scale result by 1/K
            sqrt_out <= (x * K) >>> FRACTIONAL_BITS;
        end
    end

endmodule
```

### Additional Explanation for the Verilog Pseudocode

1. **Initialization**:
   - The `in_value` is the input value whose square root needs to be computed, in fixed-point format.
   - The `sqrt_out` is the output square root value in fixed-point format.
   - The `angles` array contains precomputed fixed-point values of $\arctan(2^{-i})$.

2. **CORDIC Iterations**:
   - The `for` loop iterates for a fixed number of steps (`ITERATIONS`).
   - In each iteration, the values of `x`, `y`, and `z` are updated based on the direction determined by the sign of `y`.

3. **Scaling**:
   - After completing the iterations, the result `x` is scaled by the inverse of the CORDIC gain `K` to get the accurate square root.
   - The result is adjusted for the fixed-point format by right-shifting it by the number of fractional bits.

This pseudocode demonstrates a basic implementation of the CORDIC algorithm for computing square roots in Verilog. Adjustments may be needed for specific FPGA architectures or precision requirements.


## CORDIC Gain and Precomputed Angles

In the CORDIC algorithm, the CORDIC gain (K) and precomputed values (angles) are essential components. They are used to ensure the correctness and accuracy of the iterative computations. Below is a detailed explanation of these components and how to determine them.

### CORDIC Gain (K)

The CORDIC gain (K) is a scaling factor that accumulates as a result of the iterative rotations or vectoring operations. For each iteration, the length of the vector is scaled by a factor $\sqrt{1 + 2^{-2i}}$. The overall CORDIC gain after $n$ iterations is the product of these individual gains.

$ K_n = \prod_{i=0}^{n-1} \frac{1}{\sqrt{1 + 2^{-2i}}} $

For a large number of iterations, the CORDIC gain approaches a constant value. For example, after about 16 iterations, $ K \approx 1.64676 $.

### Precomputed Angles

The precomputed values in the CORDIC algorithm are typically the angles corresponding to $ \arctan(2^{-i}) $ for rotation mode or hyperbolic functions, depending on the application. These angles are used to adjust the vector's direction in each iteration.

The precomputed angles for the basic CORDIC rotation mode are:

$ θ_i = \arctan(2^{-i}) $

These values are computed once and stored in a lookup table (LUT) for use during the iterative process.

### Calculating CORDIC Gain and Precomputed Angles

#### Example Calculation of CORDIC Gain

To calculate the CORDIC gain for $n$ iterations:

$ K_n = \prod_{i=0}^{n-1} \frac{1}{\sqrt{1 + 2^{-2i}}} $

For example, for 10 iterations:

$ K_{10} = \prod_{i=0}^{9} \frac{1}{\sqrt{1 + 2^{-2i}}} $

You can approximate this gain value using the product formula.

#### Example Precomputed Angles

The precomputed angles for the first few iterations are:

$θ_0 = \arctan(2^0) = 45^\circ \approx 0.785398163 \text{ radians}$

$ θ_1 = \arctan(2^{-1}) = 26.56505118^\circ \approx 0.463647609 \text{ radians} $

$ θ_2 = \arctan(2^{-2}) = 14.03624347^\circ \approx 0.244978663 \text{ radians} $

$ θ_3 = \arctan(2^{-3}) = 7.125016349^\circ \approx 0.124354995 \text{ radians} $
