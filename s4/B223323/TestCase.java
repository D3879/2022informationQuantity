package s4.B223323; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import s4.specification.*;

/*
interface FrequencerInterface {     // This interface provides the design for frequency counter.
    void setTarget(byte[]  target); // set the data to search.
    void setSpace(byte[]  space);  // set the data to be searched target from.
    int frequency(); //It return -1, when TARGET is not set or TARGET's length is zero
                    //Otherwise, it return 0, when SPACE is not set or Space's length is zero
                    //Otherwise, get the frequency of TAGET in SPACE
    int subByteFrequency(int start, int end);
    // get the frequency of subByte of taget, i.e target[start], taget[start+1], ... , target[end-1].
    // For the incorrect value of START or END, the behavior is undefined.
}
*/

/*
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


public class TestCase {
    static boolean success = true;

    public static void main(String[] args) {
	try {
	    FrequencerInterface  myObject;
	    int freq;
	    System.out.println("checking Frequencer");

	    // This is smoke test
	    myObject = new Frequencer();
	    myObject.setSpace("Hi Ho Hi Ho".getBytes());
	    myObject.setTarget("H".getBytes());
	    freq = myObject.frequency();
	    assert freq == 4: "Hi Ho Hi Ho, H: " + freq;
	    // Write your testCase here
	    myObject = new Frequencer();
	    myObject.setSpace("Hi Ho Hi Ho".getBytes());
	    myObject.setTarget("Ho Hi".getBytes());
	    freq = myObject.frequency();
	    assert freq == 1: "Hi Ho Hi Ho, Ho Hi: " + freq;
		myObject = new Frequencer();
		freq = myObject.frequency();
		assert freq == -1: "space not set, target not set:" + freq;
		myObject = new Frequencer();
		myObject.setSpace("Hi Ho Hi Ho".getBytes());
		freq = myObject.frequency();
		assert freq == -1: "space set, target not set:" + freq;
		myObject = new Frequencer();
		myObject.setTarget("Hi Ho Hi Ho".getBytes());
		freq = myObject.frequency();
		assert freq == 0: "space not set, target set:" + freq;
		myObject = new Frequencer();
		myObject.setSpace(new byte[0]);
		myObject.setTarget("Hi Ho Hi Ho".getBytes());
		freq = myObject.frequency();
		assert freq == 0: "space length 0:" + freq;
		myObject = new Frequencer();
		myObject.setSpace("Hi Ho Hi Ho".getBytes());
		myObject.setTarget(new byte[0]);
		freq = myObject.frequency();
		assert freq == -1: "target length 0:" + freq;
	    myObject = new Frequencer();
	    myObject.setSpace("Hi Ho Hi Ho".getBytes());
	    myObject.setTarget("Ho Hi".getBytes());
	    freq = myObject.subByteFrequency(3, 5);
	    assert freq == 2: "Hi Ho Hi Ho, 'Ho Hi'.(3 to 4): " + freq;



	}
	catch(Exception e) {
		e.printStackTrace();
	    System.out.println("Exception occurred in Frequencer Object");
	    success = false;
	}

	try {
	    InformationEstimatorInterface myObject;
	    double value;
	    System.out.println("checking InformationEstimator");
	    myObject = new InformationEstimator();
	    myObject.setSpace("3210321001230123".getBytes());
	    value = myObject.estimation();
	    assert (value == 0.0d): "IQ for Target is not set should be 0.0. But it returns "+value;
	    myObject.setTarget(new byte[0]);
	    value = myObject.estimation();
	    assert (value == 0.0d): "IQ for Target's length is 0 should be 0.0. But it returns "+value;
	    myObject = new InformationEstimator();
	    myObject.setTarget("3210321001230123".getBytes());
	    value = myObject.estimation();
	    assert (value == Double.MAX_VALUE): "IQ for Space is not set should be Double.MAX_VALUE(1.7976931348623157E308). But it returns "+value;
		myObject.setSpace(new byte[0]);
	    value = myObject.estimation();
	    assert (value == Double.MAX_VALUE): "IQ for Space's length is 0 should be Double.MAX_VALUE(1.7976931348623157E308). But it returns "+value;
		
	    myObject = new InformationEstimator();
	    myObject.setSpace("3210321001230123".getBytes());
	    myObject.setTarget("0".getBytes());
	    value = myObject.estimation();
	    assert (value > 1.9999) && (2.0001 >value): "IQ for 0 in 3210321001230123 should be 2.0. But it returns "+value;
	    myObject.setTarget("01".getBytes());
	    value = myObject.estimation();
	    assert (value > 2.9999) && (3.0001 >value): "IQ for 01 in 3210321001230123 should be 3.0. But it returns "+value;
	    myObject.setTarget("0123".getBytes());
	    value = myObject.estimation();
	    assert (value > 2.9999) && (3.0001 >value): "IQ for 0123 in 3210321001230123 should be 3.0. But it returns "+value;
	    myObject.setTarget("00".getBytes());
	    value = myObject.estimation();
	    assert (value > 3.9999) && (4.0001 >value): "IQ for 00 in 3210321001230123 should be 4.0. But it returns "+value;
	    myObject.setTarget("30".getBytes());
	    value = myObject.estimation();
	    assert (value > 3.9999) && (4.0001 >value): "IQ for 00 in 3210321001230123 should be 4.0. But it returns "+value;
	    myObject.setTarget("333".getBytes());
	    value = myObject.estimation();
	    assert (value > 5.9999) && (6.0001 >value): "IQ for 00 in 3210321001230123 should be 6.0. But it returns "+value;		
	}
	catch(Exception e) {
	    System.out.println("Exception occurred in InformationEstimator Object");
	    success = false;
	}
        if(success) { System.out.println("TestCase OK"); } 
    }
}	    
	    
