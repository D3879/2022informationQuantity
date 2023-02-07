package s4.B223323; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;

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
    private static final double C0 = 1/Math.log10(2d);
    private static final double DOUBLE_MAX = Double.MAX_VALUE;
    private static final int N = Runtime.getRuntime().availableProcessors();

    private double C1 = 0;                                         //log10(mySpace.length)
    private int len;                                               //target.length

    // IQ: information quantity for a count,  -log2(count/sizeof(space))
    private final double iq(int freq) {
        return (-Math.log10((double) freq) + C1) * C0;
    }

    public final void setTarget(byte [] target) { 
        len = target.length; //このクラスで直接必要なtargetの情報はこれだけ
        if (len == 0) {
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
        C1 = Math.log10((double) space.length);
    }

    public final double estimation() {
        if (myTargetNotReady) return 0d; // returns 0.0 when the target is not set or Target's length is zero;
        if (mySpaceNotReady) return DOUBLE_MAX; // It returns Double.MAX_VALUE, when the true value is infinite, or the space is not set.

        int start = len - 1;
        double min;
        double[] memo = new double[len];
        
        for (;;) {
            min = DOUBLE_MAX;
            myFrequencer.reset(start);
            for (int i = start;i < len; i++) {
                int freq = myFrequencer.subByteFrequency();
                if (freq == 0) break;
                double iq = -Math.log10((double) freq) + C1 + memo[i];
                if (min > iq) min = iq;
            }
            
            if (start == 0) break;
            memo[--start] = min;
        }
        
        return min == DOUBLE_MAX ? min : min * C0;
    }

    private final double slowEstimation(){
        if (myTargetNotReady) return 0d;
        if (mySpaceNotReady) return DOUBLE_MAX;

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

    public static void main(String[] args) {
        InformationEstimator myObject;
        double value;
        myObject = new InformationEstimator();
        myObject.setSpace("abc".getBytes());
        myObject.setTarget("abc".getBytes());
        value = myObject.check();
        System.out.println(">a "+value);
        myObject.setSpace("3210321001230123".getBytes());
        myObject.setTarget("0".getBytes());
        value = myObject.check();
        System.out.println(">0 "+value);
        myObject.setTarget("01".getBytes());
        value = myObject.check();
        System.out.println(">01 "+value);
        myObject.setTarget("0123".getBytes());
        value = myObject.check();
        System.out.println(">0123 "+value);
        myObject.setTarget("00".getBytes());
        value = myObject.check();
        System.out.println(">00 "+value);
        myObject.setTarget("4".getBytes());
        value = myObject.check();
        System.out.println(">4 "+value);
        myObject.setTarget("321".getBytes());
        value = myObject.check();
        System.out.println(">321 "+value);
        myObject.setTarget("3210".getBytes());
        value = myObject.check();
        System.out.println(">3210 "+value);
        myObject.setTarget("32121".getBytes());
        value = myObject.check();
        System.out.println(">32121 "+value);
        myObject.setTarget("31313131".getBytes());
        value = myObject.check();
        System.out.println(">31313131 "+value);
        /* */
        byte[] space = new byte[1048576];
        byte[] target = new byte[10240];
        // java.security.SecureRandom rnd = new java.security.SecureRandom();
        
        // rnd.setSeed(System.nanoTime());
        // rnd.nextBytes(target);
        // rnd.nextBytes(space);
        
        long t0 = System.currentTimeMillis();
        myObject = new InformationEstimator();
        long t1 = System.currentTimeMillis();
        myObject.setSpace(space);
        long t2 = System.currentTimeMillis();
        myObject.setTarget(target);
        value = myObject.estimation();
        long t3 = System.currentTimeMillis();
        System.out.println(">setSpace in " + (t2 - t1) + "ms");
        System.out.println(">space(" + (space.length >> 10) + "k), target(" + (target.length >> 10) + "k) "  + value + " in " + (t3 - t0) + " ms");
        /* */
    }
}