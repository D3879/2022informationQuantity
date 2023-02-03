package s4.B223323; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 
import java.lang.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

import s4.specification.*;

/*package s4.specification;
  ここは、１回、２回と変更のない外部仕様である。
  public interface FrequencerInterface {     // This interface provides the design for frequency counter.
  void setTarget(byte  target[]); // set the data to search.
  void setSpace(byte  space[]);  // set the data to be searched target from.
  int frequency(); //It return -1, when TARGET is not set or TARGET's length is zero
  //Otherwise, it return 0, when SPACE is not set or SPACE's length is zero
  //Otherwise, get the frequency of TAGET in SPACE
  int subByteFrequency(int start, int end);
  // get the frequency of subByte of taget, i.e target[start], taget[start+1], ... , target[end-1].
  // For the incorrect value of START or END, the behavior is undefined.
  }
*/

public class Frequencer implements FrequencerInterface{
    // Code to start with: This code is not working, but good start point to work.
    byte [] myTarget;
    byte [] mySpace;
    boolean targetReady = false;
    boolean spaceReady = false;

    int []  suffixArray; // Suffix Arrayの実装に使うデータの型をint []とせよ。

    //ソートアルゴリズムの選択閾値
    //要素数 < threshold の時は挿入ソート
    //要素数 >= threshold の時はクイックソート
    private static final int threshold = 14;
    private int slen;

    private final SuffixComparator COMPARATOR = new SuffixComparator();

    // The variable, "suffixArray" is the sorted array of all suffixes of mySpace.                                    
    // Each suffix is expressed by a integer, which is the starting position in mySpace. 
                            
    // The following is the code to print the contents of suffixArray.
    // This code could be used on debugging.                                                                

    // この関数は、デバッグに使ってもよい。mainから実行するときにも使ってよい。
    // リポジトリにpushするときには、mainメッソド以外からは呼ばれないようにせよ。
    //
    private final void printSuffixArray() {
        if(spaceReady) {
            for(int i=0; i< mySpace.length; i++) {
                int s = suffixArray[i];
                System.out.printf("suffixArray[%2d]=%2d:", i, s);
                for(int j=s;j<mySpace.length;j++) {
                    System.out.write(mySpace[j]);
                }
                System.out.write('\n');
            }
        }
    }

    private final int suffixCompare(int i, int j) {
        // suffixCompareはソートのための比較メソッドである。
        // 次のように定義せよ。
        //
        // comparing two suffixes by dictionary order.
        // suffix_i is a string starting with the position i in "byte [] mySpace".
        // When mySpace is "ABCD", suffix_0 is "ABCD", suffix_1 is "BCD", 
        // suffix_2 is "CD", and sufffix_3 is "D".
        // Each i and j denote suffix_i, and suffix_j.                            
        // Example of dictionary order                                            
        // "i"      <  "o"        : compare by code                              
        // "Hi"     <  "Ho"       ; if head is same, compare the next element    
        // "Ho"     <  "Ho "      ; if the prefix is identical, longer string is big  
        //  
        //The return value of "int suffixCompare" is as follows. 
        // if suffix_i > suffix_j, it returns 1   
        // if suffix_i < suffix_j, it returns -1  
        // if suffix_i = suffix_j, it returns 0;   

        // ここにコードを記述せよ 

        if (i == j) return 0; //同じ場所からチェックすることになるので必ず一致
        if (mySpace[i] == mySpace[j]){ //値が一致しない場所を見つける
            int end = slen - 1;
            if (i > j) //どちらが先にendに到達するかを判定
                do
                    if (i == end) return -1; //jが残っているので i < j
                while(mySpace[++i] == mySpace[++j]);
            else
                do
                    if (j == end) return 1; //iが残っているので j < i
                while(mySpace[++i] == mySpace[++j]);
        }
        //ここでは mySpace[i] != mySpace[j]
        return mySpace[i] > mySpace[j] ? 1 : -1;
    }

    //参考：http://wwwa.pikara.ne.jp/okojisan/sort-killer/qsort-killer.html PQuickSort.java
    private final void qsort(int i0, int iN) {
        int N = iN - i0;
        if (N < 2) return;
        int depth;
        for (depth = 0; N > 0; N >>= 1) depth++;
        qsort(i0, iN, depth);
    }

    private final void qsort(int i0, int iN, int d) {
        int N = iN - i0;
        if (N < threshold) { isort(i0, iN); return; }
        int pivot = pseudoMedian(N, i0, d);
        int i = i0, j = iN - 1;
        for (;;) {
            while (suffixCompare(suffixArray[i], pivot) <= 0) i++;
            while (suffixCompare(pivot, suffixArray[j]) <= 0) j--;
            if (i >= j) break;
            int tmp = suffixArray[i]; suffixArray[i] = suffixArray[j]; suffixArray[j] = tmp;
            i++; j--;
        }
        j++;
        if (i - i0 > 1) qsort(i0, i, d);
        if (iN - j > 1) qsort(j, iN, d);
    }


    /* pivotの選択 */
    private final int pseudoMedian(int n, int i, int d) {
        if (n <= d) return suffixArray[i + (n >> 1)];
        int m = n / 3;
        int x = pseudoMedian(m, i, d);
        int y = pseudoMedian(n - (m << 1), i + m, d);
        int z = pseudoMedian(m, i + n - m, d);
        return suffixCompare(x, y) < 0 ?
            (suffixCompare(y, z) < 0 ? y : (suffixCompare(z, x) < 0 ? x : z)) :
            (suffixCompare(z, y) < 0 ? y : (suffixCompare(x, z) < 0 ? x : z));
    }

    /* 挿入ソート */
    private final void isort(int i0, int iN) {
        if (iN - i0 < 2) return; //ソートすべきものがない
        for (int i = i0 + 1; i < iN; i++) {
            int tmp = suffixArray[i];
            int j = i, k = i;
            if (suffixCompare(tmp, suffixArray[i0]) < 0)
                while (i0 < j) { suffixArray[j] = suffixArray[--k]; j = k; }
            else
                while (suffixCompare(tmp, suffixArray[--k]) < 0) { suffixArray[j] = suffixArray[k]; j = k; }
            suffixArray[j] = tmp;
        }
    }

    public final void setSpace(byte []space) { 
        // suffixArrayの前処理は、setSpaceで定義せよ。
        slen = space.length;
        mySpace = space; spaceReady = slen>0;
        // First, create unsorted suffix array.
        // put all suffixes in suffixArray.
        
        if (slen > 5120) {
            suffixArray = new int[slen];
            SuffixArray sa = new SuffixArray();
            sa.createSuffixArray(space, suffixArray);
        } else if (slen > 1024){
            Integer[] boxedSuffixArray = IntStream.range(0, slen).parallel().boxed().toArray(Integer[]::new);
            Arrays.parallelSort(boxedSuffixArray, COMPARATOR); //並列処理によるソート
            suffixArray = IntStream.range(0, slen).parallel().map(i -> boxedSuffixArray[i].intValue()).toArray();
            Arrays.parallelSetAll(suffixArray, i -> boxedSuffixArray[i].intValue());
        } else {
            suffixArray = IntStream.range(0, slen).toArray();
            qsort(0, slen);
        }
    }
    // ここから始まり、指定する範囲までは変更してはならないコードである。

    public final void setTarget(byte [] target) {
        myTarget = target; if(myTarget.length>0) targetReady = true;
    }

    public final int frequency() {
        if(targetReady == false) return -1;
        if(spaceReady == false) return 0;
        return subByteFrequency(0, myTarget.length);
    }

    public final int subByteFrequency(int start, int end) {
        // start, and end specify a string to search in myTarget,
        // if myTarget is "ABCD", 
        //     start=0, and end=1 means string "A".
        //     start=1, and end=3 means string "BC".
        // This method returns how many the string appears in my Space.
        // 
        /* This method should be work as follows, but much more efficient.
           int spaceLength = mySpace.length;                      
           int count = 0;                                        
           for(int offset = 0; offset< spaceLength - (end - start); offset++) {
            boolean abort = false; 
            for(int i = 0; i< (end - start); i++) {
             if(myTarget[start+i] != mySpace[offset+i]) { abort = true; break; }
            }
            if(abort == false) { count++; }
           }
        */
        // The following the counting method using suffix array.
        // 演習の内容は、適切なsubByteStartIndexとsubByteEndIndexを定義することである。
        int first = subByteStartIndex(start, end);
        int last1 = subByteEndIndex(start, end);
        return last1 - first;
    }
    // 変更してはいけないコードはここまで。

    public final int slowsubByteFrequency(int start, int end) {
        int spaceLength = mySpace.length;                      
        int count = 0;                                        
        for(int offset = 0; offset< spaceLength - (end - start); offset++) {
            boolean abort = false; 
            for(int i = 0; i< (end - start); i++) {
                if(myTarget[start+i] != mySpace[offset+i]) { abort = true; break; }
            }
            if(abort == false) { count++; }
        }
        return count;
    }

    private final int targetCompare(int i, int j, int k) {
        // subByteStartIndexとsubByteEndIndexを定義するときに使う比較関数。
        // 次のように定義せよ。
        // suffix_i is a string starting with the position i in "byte [] mySpace".
        // When mySpace is "ABCD", suffix_0 is "ABCD", suffix_1 is "BCD", 
        // suffix_2 is "CD", and sufffix_3 is "D".
        // target_j_k is a string in myTarget start at j-th postion ending k-th position.
        // if myTarget is "ABCD", 
        //     j=0, and k=1 means that target_j_k is "A".
        //     j=1, and k=3 means that target_j_k is "BC".
        // This method compares suffix_i and target_j_k.
        // if the beginning of suffix_i matches target_j_k, it return 0.
        // if suffix_i > target_j_k it return 1; 
        // if suffix_i < target_j_k it return -1;
        // if first part of suffix_i is equal to target_j_k, it returns 0;
        //
        // Example of search 
        // suffix          target
        // "o"       >     "i"
        // "o"       <     "z"
        // "o"       =     "o"
        // "o"       <     "oo"
        // "Ho"      >     "Hi"
        // "Ho"      <     "Hz"
        // "Ho"      =     "Ho"
        // "Ho"      <     "Ho "   : "Ho " is not in the head of suffix "Ho"
        // "Ho"      =     "H"     : "H" is in the head of suffix "Ho"
        // The behavior is different from suffixCompare on this case.
        // For example,
        //    if suffix_i is "Ho Hi Ho", and target_j_k is "Ho", 
        //            targetCompare should return 0;
        //    if suffix_i is "Ho Hi Ho", and suffix_j is "Ho", 
        //            suffixCompare should return 1. (It was written -1 before 2021/12/21)
        //
        // ここに比較のコードを書け
        if (mySpace[i] == myTarget[j]) // 一文字目が一致
            if (slen - i < k - j) { //mySpaceが短い
                int end = slen - 1;
                do
                    if (i == end) return -1; //mySpaceとmyTargetのheadが一致
                while (mySpace[++i] == myTarget[++j]);
            } else { //Targetが短い
                k--;
                do
                    if (k == j) return 0; //headが一致
                while (mySpace[++i] == myTarget[++j]);
            }
        return mySpace[i] > myTarget[j] ? 1 : -1; //比較
    }

    private final int subByteStartIndex(int start, int end) {
        //suffix arrayのなかで、目的の文字列の出現が始まる位置を求めるメソッド
        // 以下のように定義せよ。
        // The meaning of start and end is the same as subByteFrequency.
        /* Example of suffix created from "Hi Ho Hi Ho"
           0: Hi Ho
           1: Ho
           2: Ho Hi Ho
           3:Hi Ho
           4:Hi Ho Hi Ho
           5:Ho
           6:Ho Hi Ho
           7:i Ho
           8:i Ho Hi Ho
           9:o
          10:o Hi Ho
        */

        // It returns the index of the first suffix 
        // which is equal or greater than target_start_end.                         
	// Suppose target is set "Ho Ho Ho Ho"
        // if start = 0, and end = 2, target_start_end is "Ho".
        // if start = 0, and end = 3, target_start_end is "Ho ".
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                 
        // if target_start_end is "Ho", it will return 5.                           
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                 
        // if target_start_end is "Ho ", it will return 6.                
        //                                                                          
        // ここにコードを記述せよ。                                                 
        //     
        int s = 0, e = slen, p = slen >> 1;

        for (;;) {
            if (s == p) return targetCompare(suffixArray[p], start, end) == -1 ? e : s;
            if (targetCompare(suffixArray[p], start, end) == -1) s = p;
            else e = p;
            p = (s + e) >> 1; //(s+e)/2 -> pの定義域[s,e-1]
        }
    }

    private final int subByteEndIndex(int start, int end) {
        //suffix arrayのなかで、目的の文字列の出現しなくなる場所を求めるメソッド
        // 以下のように定義せよ。
        // The meaning of start and end is the same as subByteFrequency.
        /* Example of suffix created from "Hi Ho Hi Ho"
           0: Hi Ho                                    
           1: Ho                                       
           2: Ho Hi Ho                                 
           3:Hi Ho                                     
           4:Hi Ho Hi Ho                              
           5:Ho                                      
           6:Ho Hi Ho                                
           7:i Ho                                    
           8:i Ho Hi Ho                              
           9:o                                       
          10:o Hi Ho                                 
        */
        // It returns the index of the first suffix 
        // which is greater than target_start_end; (and not equal to target_start_end)
	// Suppose target is set "High_and_Low",
        // if start = 0, and end = 2, target_start_end is "Hi".
        // if start = 1, and end = 2, target_start_end is "i".
        // Assuming the suffix array is created from "Hi Ho Hi Ho",                   
        // if target_start_end is "Ho", it will return 7 for "Hi Ho Hi Ho".  
        // Assuming the suffix array is created from "Hi Ho Hi Ho",          
        // if target_start_end is"i", it will return 9 for "Hi Ho Hi Ho".    
        //                                                                   
        //　ここにコードを記述せよ                                           
        //                                                                   
        
        int s = 0, e = slen, p = slen >> 1;

        for(;;) {
            if (s == p) return targetCompare(suffixArray[p], start, end) != 1 ? e : p;
            if (targetCompare(suffixArray[p], start, end) == 1) e = p;
            else s = p;
            p = (s + e) >> 1; //(s+e)/2 -> pの定義域[s,e-1]
        }
        
        // for (int i = suffixArray.length - 1; i > 0; i--) {
        //     int res = targetCompare(suffixArray[i], start, end);
        //     if (res == 0) return i + 1;
        //     if (res == -1) return i + 1;
        // }
        // return 0;
    }

    /*
    //DEBUG用
    private static void print(int ...i) {
        print("", i);
    }
    private static void print(String header, int ...i) { //[debug]数値の一括出力
        System.out.print(header);
        for (int object : i) {
            System.out.print(object);
            System.out.print(' ');
        }
        System.out.println();
    }*/

    // Suffix Arrayを使ったプログラムのホワイトテストは、
    // privateなメソッドとフィールドをアクセスすることが必要なので、
    // クラスに属するstatic mainに書く方法もある。
    // static mainがあっても、呼びださなければよい。
    // 以下は、自由に変更して実験すること。
    // 注意：標準出力、エラー出力にメッセージを出すことは、
    // static mainからの実行のときだけに許される。
    // 外部からFrequencerを使うときにメッセージを出力してはならない。
    // 教員のテスト実行のときにメッセージがでると、仕様にない動作をするとみなし、
    // 減点の対象である。
    public static void main(String[] args) {
        Frequencer frequencerObject;
        try { // テストに使うのに推奨するmySpaceの文字は、"ABC", "CBA", "HHH", "Hi Ho Hi Ho".
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("ABC".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("CBA".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("HHH".getBytes());
            frequencerObject.printSuffixArray();
            frequencerObject = new Frequencer();
            frequencerObject.setSpace("Hi Ho Hi Ho".getBytes());
            frequencerObject.printSuffixArray();
            /* Example from "Hi Ho Hi Ho"    
               0: Hi Ho                      
               1: Ho                         
               2: Ho Hi Ho                   
               3:Hi Ho                       
               4:Hi Ho Hi Ho                 
               5:Ho                          
               6:Ho Hi Ho
               7:i Ho                        
               8:i Ho Hi Ho                  
               9:o                           
              10:o Hi Ho                     
            */

            frequencerObject.setTarget("H".getBytes());
            //                                         
            // ****  Please write code to check subByteStartIndex, and subByteEndIndex
            //

            int result = frequencerObject.frequency();
            System.out.print("Freq = "+ result+" ");
            if(4 == result) { System.out.println("OK"); } else {System.out.println("WRONG"); }
            
            frequencerObject.setTarget("Hi Ho Hi".getBytes());
            result = frequencerObject.subByteStartIndex(0, 2); //Hi
            if(3 == result) { System.out.println("OK"); } else {System.out.println("WRONG" ); }
            result = frequencerObject.subByteStartIndex(0, 1); //H
            if(3 == result) { System.out.println("OK"); } else {System.out.println("WRONG"); }
            result = frequencerObject.subByteStartIndex(0, 8); //Hi Ho Hi
            if(4 == result) { System.out.println("OK"); } else {System.out.println("WRONG"); }
            result = frequencerObject.subByteEndIndex(0, 2); //Hi
            if(5 == result) { System.out.println("OK"); } else {System.out.println("WRONG" ); }
            result = frequencerObject.subByteEndIndex(0, 1); //H
            if(7 == result) { System.out.println("OK"); } else {System.out.println("WRONG"); }
            result = frequencerObject.subByteEndIndex(0, 8); //Hi Ho Hi
            if(5 == result) { System.out.println("OK"); } else {System.out.println("WRONG"); }
        }
        catch(Exception e) {
            System.out.println("STOP");
            e.printStackTrace();
        }
    }

    private class SuffixComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer arg0, Integer arg1) {
            int i = arg0.intValue(), j = arg1.intValue();
            if (i == j) return 0;
            if (mySpace[i] == mySpace[j]){ //値が一致しない場所を見つける
                int end = slen - 1;
                if (i > j) //どちらが先にendに到達するかを判定
                    do
                        if (i == end) return -1; //jが残っているので i < j
                    while(mySpace[++i] == mySpace[++j]);
                else
                    do
                        if (j == end) return 1; //iが残っているので j < i
                    while(mySpace[++i] == mySpace[++j]);
            }
            //ここでは mySpace[i] != mySpace[j]
            return mySpace[i] > mySpace[j] ? 1 : -1;
        }
    }
}

