package s4.B223323; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.Random;

import s4.specification.*;

/* What is imported from s4.specification
package s4.specification;
public interface InformationEstimatorInterface{
    void setTarget(byte target[]); // set the data for computing the information quantities
    void setSpace(byte space[]); // set data for sample space to computer probability
    double estimation(); // It returns 0.0 when the target is not set or Target's length is zero;
// It returns Double.MAX_VALUE, when the true value is infinite, or space is not set.
// The behavior is undefined, if the true value is finete but larger than Double.MAX_VALUE.
// Note that this happens only when the space is unreasonably large. We will encounter other problem anyway.
// Otherwise, estimation of information quantity, 
}                        
*/

public class InformationEstimator implements InformationEstimatorInterface{
    private final Frequencer myFrequencer = new Frequencer();  // Object for counting frequency
    private boolean mySpaceNotReady = true;
    private boolean myTargetNotReady = true;

    //定数群
    // private static final double C0 = 1/Math.log10(2d);
    // private static final double DOUBLE_MAX = Double.MAX_VALUE;

    // private double C1 = 0;                                         //log10(mySpace.length)
    // private int len;                                               //target.length

    // IQ: information quantity for a count,  -log2(count/sizeof(space))
    private final double iq(int freq) {
        return (-Math.log10((double) freq / myFrequencer.mySpace.length)) / Math.log10(2d);
    }

    public final void setTarget(byte [] target) { 
        // int len = target.length;
        if (target.length == 0) {
            myTargetNotReady = true; 
        } else {
            myTargetNotReady = false;
            myFrequencer.setTarget(target); //targetの内容はmyFrequencerで処理する。
        }
    }
    
    public final void setSpace(byte []space) {
        mySpaceNotReady = space.length == 0;
        if (mySpaceNotReady) return;
        myFrequencer.setSpace(space);
        // C1 = Math.log10((double) space.length);
    }


    public final double estimation() {
        if (myTargetNotReady) return 0d; // returns 0.0 when the target is not set or Target's length is zero;
        if (mySpaceNotReady) return Double.MAX_VALUE; // It returns Double.MAX_VALUE, when the true value is infinite, or the space is not set.
        return myFrequencer.calculate3();
    }

    private final double slowEstimation(){
        if (myTargetNotReady) return 0d;
        if (mySpaceNotReady) return Double.MAX_VALUE;

        int len = myFrequencer.myTarget.length;

        boolean [] partition = new boolean[len+1];
        int np;
        np = 1<<(len-1);
        // System.out.println("np="+np+" length="+len);
        double value = Double.MAX_VALUE; // value = mininimum of each "value1".

        for(int p=0; p<np; p++) { // There are 2^(n-1) kinds of partitions.
            // binary representation of p forms partition.
            // for partition {"ab" "cde" "fg"}
            // a b c d e f g   : myTarget
            // T F T F F T F T : partition:
            partition[0] = true; // I know that this is not needed, but..
            for(int i=0; i<len -1;i++) {
                partition[i+1] = (0 !=((1<<i) & p));
            }
            partition[len] = true;

            // Compute Information Quantity for the partition, in "value1"
            // value1 = IQ(#"ab")+IQ(#"cde")+IQ(#"fg") for the above example
            double value1 = (double) 0.0;
            int end = 0;
            int start = end;
            while(start<len) {
                // System.out.write(myTarget[end]);
                end++;
                while(partition[end] == false) { 
                    // System.out.write(myTarget[end]);
                    end++;
                }
                // System.out.print("("+start+","+end+")");
                int freq = myFrequencer.subByteFrequency(start, end);
                value1 = freq == 0 ? Double.MAX_VALUE : value1 + iq(freq);
                
                start = end;
            }
            // System.out.println(" "+ value1);

            // Get the minimal value in "value"
            if(value1 < value) value = value1;
        }
        return value;
    }

    private final double check() {
        double fast = estimation();
        double slow = slowEstimation();
        if (Math.abs(fast - slow) > 1e-15) System.out.println("ERROR: WRONG\nfast:" + fast + "\nslow:" + slow);
        return fast;
    }

    public static String getMemoryInfo() {
        StringBuilder info = new StringBuilder();
        
        DecimalFormat format_mem =   new DecimalFormat("#,###KB");
        DecimalFormat format_ratio = new DecimalFormat("##.#");
        long free =  Runtime.getRuntime().freeMemory() >> 10;
        long total = Runtime.getRuntime().totalMemory() >> 10;
        long max =   Runtime.getRuntime().maxMemory() >> 10;
        long used =  total - free;
        double ratio = (used * 100 / (double)total);

        info.append("Total   = ");
        info.append(format_mem.format(total));
        info.append("\nFree    = ");
        info.append(format_mem.format(free));
        info.append("\nuse     = ");
        info.append(format_mem.format(used));
        info.append(" (");
        info.append(format_ratio.format(ratio));
        info.append("%)\ncan use = ");
        info.append(format_mem.format(max));
        return info.toString();
    }

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
        // myObject = new InformationEstimator();
        // myObject.setSpace("abc".getBytes());
        // myObject.setTarget("abc".getBytes());
        // value = myObject.check();
        // System.out.println(">a "+value);
        // myObject.setSpace("3210321001230123".getBytes());
        // myObject.setTarget("0".getBytes());
        // value = myObject.check();
        // System.out.println(">0 "+value);
        // myObject.setTarget("01".getBytes());
        // value = myObject.check();
        // System.out.println(">01 "+value);
        // myObject.setTarget("0123".getBytes());
        // value = myObject.check();
        // System.out.println(">0123 "+value);
        // myObject.setTarget("00".getBytes());
        // value = myObject.check();
        // System.out.println(">00 "+value);
        // myObject.setTarget("4".getBytes());
        // value = myObject.check();
        // System.out.println(">4 "+value);
        // myObject.setTarget("321".getBytes());
        // value = myObject.check();
        // System.out.println(">321 "+value);
        // myObject.setTarget("3210".getBytes());
        // value = myObject.check();
        // System.out.println(">3210 "+value);
        // myObject.setTarget("32121".getBytes());
        // value = myObject.check();
        // System.out.println(">32121 "+value);
        // myObject.setTarget("31313131".getBytes());
        // value = myObject.check();
        // System.out.println(">31313131 "+value);


        // byte[] space = new byte[100];
        // byte[] target = new byte[5];

        // Random rnd = new Random();
        // long seed = System.nanoTime();
        // Frequencer.print("seed:", seed);
        // rnd.setSeed(seed);
        // rnd.nextBytes(target);
        // rnd.nextBytes(space);

        // Frequencer frequencer = new Frequencer();
        // frequencer.setSpace(space);
        // frequencer.setTarget(target);
        // System.out.println(frequencer.calculate2());
        /* */
        byte[] space = new byte[100000];
        byte[] target = new byte[10000];


        System.out.println("length (space, target):" + space.length + "," + target.length);

        // SecureRandom rnd = new SecureRandom();
        // // Random rnd = new Random();
        // long seed = System.nanoTime();
        // Frequencer.print("seed:", seed);
        // rnd.setSeed(seed);
        // rnd.nextBytes(target);
        // rnd.nextBytes(space);
        
        // long t0 = System.currentTimeMillis();
        myObject = new InformationEstimator();
        // long t1 = System.currentTimeMillis();
        myObject.setSpace(space);
        // long t2 = System.currentTimeMillis();
        myObject.setTarget(target);
        // long t3 = System.currentTimeMillis();
        // System.out.println(getMemoryInfo());
        // System.out.println("gc");
        // System.gc();
        // System.out.println(getMemoryInfo());
        
        // long t4 = System.currentTimeMillis();
        // value = myObject.estimation();
        // long t5 = System.currentTimeMillis();
        // System.out.println(">set space in " + (t2 - t1) + " ms");
        // System.out.println(">set target in " + (t3 - t2) + " ms");
        // System.out.println(">calculate in " + (t5 - t4) + " ms");
        // System.out.println(">space(" + (space.length >> 10) + "k), target(" + (target.length >> 10) + "k) "  + value + " in " + (t3 - t1 + t5 - t4) + " ms");
        // System.out.println(getMemoryInfo());
        // System.gc();
        
        // t0 = System.currentTimeMillis();
        double value2 = myObject.myFrequencer.count();
        // t1 = System.currentTimeMillis();
        
        System.out.println("result                :"+ value2);
        // System.out.println(getMemoryInfo());

        // if (value != value2) System.out.println("WRONG");
        // if (t5 - t4 - t1 + t0 >= 0) System.out.println("SLOW");

        // myObject = new InformationEstimator();
        // myObject.setSpace(space);
        // myObject.setTarget(target);

        // double v1 = 0d, v2 = 0d;
        // v1 = myObject.myFrequencer.calculate();
        // System.out.println("cal :" + v1);
        // v2 = myObject.myFrequencer.calculate3();
        // System.out.println("cal3:" + v2);

        // while (v1 == v2) {
        //     myObject.myFrequencer.END+=1;
        //     v1 = myObject.myFrequencer.calculate();
        //     v2 = myObject.myFrequencer.calculate3();
        // }

        // Frequencer.print(myObject.myFrequencer.END, v1, v2);


        /* */
    }
}