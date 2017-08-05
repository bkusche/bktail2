public class BoolFlipTest
{
  public static void main(String[] args)
  {
    final int iterations = 100000000;
   
    // Warmup to avoid JIT compilation while performing the real test.
    // For Client HotSpot VM the limit is 1500 invocations, see
    // http://blogs.sun.com/watt/resource/jvm-options-list.html
    for (int i = 1501; --i > 0; )
    {
      testXOR(1);
      testNot(1);
      testCond(1);
    }

    long start;

    start = -System.currentTimeMillis();
    @SuppressWarnings("unused")
    final boolean dummy1 = testXOR(iterations);
    start += System.currentTimeMillis();
    System.out.println("Time for xor: " + start + " ms");
    
    start = -System.currentTimeMillis();
    @SuppressWarnings("unused")
    final boolean dummy2 = testNot(iterations);
    start += System.currentTimeMillis();
    System.out.println("Time for !: " + start + " ms");

    start = -System.currentTimeMillis();
    @SuppressWarnings("unused")
    final boolean dummy3 = testCond(iterations);
    start += System.currentTimeMillis();
    System.out.println("Time for cond: " + start + " ms");
  }

  private static boolean testXOR(final int iterations)
  {
    boolean flag = true;
   
    for (int i = iterations; --i > 0; )
    {
      flag ^= true;
    }
    
    return flag; // So that the entire method body is not optimized away.
  }
 
  private static boolean testNot(final int iterations)
  {
    boolean flag = true;
   
    for (int i = iterations; --i > 0; )
    {
      flag = !flag;
    }
    
    return flag; // So that the entire method body is not optimized away.
  }
 
  private static boolean testCond(final int iterations)
  {
    boolean flag = true;
   
    for (int i = iterations; --i > 0; )
    {
      flag = flag ? false : true;
    }
    
    return flag; // So that the entire method body is not optimized away.
  }
}
