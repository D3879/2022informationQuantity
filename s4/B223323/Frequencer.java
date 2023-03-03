package s4.B223323; // Please modify to s4.Bnnnnnn, where nnnnnn is your student ID. 

import java.util.Arrays;
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
    boolean targetReady; //default: false
    boolean spaceReady;  //default: false

    SuffixArray sa = new SuffixArray();

    int []  suffixArray; // Suffix Arrayの実装に使うデータの型をint []とせよ。

    int suffix0, suffixl;

    private int slen;
    private int tlen;

    private static final double C0 = 1/Math.log10(2d);

    // The variable, "suffixArray" is the sorted array of all suffixes of mySpace.                                    
    // Each suffix is expressed by a integer, which is the starting position in mySpace. 
                            
    // The following is the code to print the contents of suffixArray.
    // This code could be used on debugging.                                                                

    // この関数は、デバッグに使ってもよい。mainから実行するときにも使ってよい。
    // リポジトリにpushするときには、mainメッソド以外からは呼ばれないようにせよ。
    //
    private final void printSuffixArray() {
        if(spaceReady) {
            printSuffixArray(suffixArray, mySpace, slen);
        }
    }
    private final void printSuffixArray(int[] suffix, byte[] str, int n) {
        for(int i=0; i< n; i++) {
            int s = suffix[i];
            System.out.printf("suffixArray[%2d]=%2d:", i, s);
            for(int j=s;j<n;j++) {
                System.out.write(str[j]);
            }
            System.out.write('\n');
        }
    }

    public final void setSpace(byte []space) { 
        slen = space.length;
        spaceReady = slen>0;
        if (!spaceReady) return;
        mySpace = space;

        suffixArray = IntStream.range(0, slen).parallel().toArray();
        int[] tmp1 = new int[slen];
        int[] tmp2 = new int[slen];
        int[] str = IntStream.range(0, slen).parallel().map(i->space[i] & 0xFF).toArray();
        sa.createSuffixArray(suffixArray, str, tmp1, tmp2, slen);
        suffix0 = suffixArray[0];
        suffixl = suffixArray[slen - 1];
    }
    // ここから始まり、指定する範囲までは変更してはならないコードである。

    public final void setTarget(byte [] target) {
        tlen = target.length;
        if(tlen > 0) {
            targetReady = true; 
            myTarget = target;
        } else targetReady = false;
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

    /**
     * 一応ok？
     * 
     * targetとspaceのsuffixArrayを用いてすべての部分文字列の頻度を求め、
     * log(slen/freq)をmemoに記録する
     * 
     * memoのサイズは計算できる場所からiqを計算すれば少しは減らせるはず
     * 
     * @param targetSuffix targetのsuffixArray
     * @param tstart       targetのsuffixArrayの範囲
     * @param tend         targetのsuffixArrayの範囲
     * @param spaceSuffix  spaceのsuffixArray
     * @param sstart       spaceのsuffixArrayの範囲
     * @param send         spaceのsuffixArrayの範囲
     * @param memo         Double.MAX_VALUEで初期化されたサイズ[tlen*(tlen+1)/2]の配列
     * @param offset       offset文字目から見る
     * @param memoOffset   メモ記入時のoffset
     * @param C1           log(slen)
     */
    public final void calculate2_1(final int[] targetSuffix, int tstart, int tend, final int[] spaceSuffix, int sstart, int send, final double[] memo, int offset, int memoOffset, final double C1) {
        int end = send;
        int spointer = suffixArray[sstart] + offset;
        int epointer = suffixArray[send-1] + offset;
        int tepointer = targetSuffix[tend-1] + offset;
        int targetIndex = tend;
        for(;;) {
            //1 targetをsuffixArrayから取得
            while (targetSuffix[tstart] + offset >= tlen) {
                tstart++;
                if (tstart >= tend) return;
            }
            byte target = myTarget[targetSuffix[tstart] + offset];

            // print(tstart, tend, sstart, send);
            
            //2 targetのfreqを計算(sstart,endを求める)
            if (spointer >= slen || mySpace[spointer] != target) { //startの位置が正しくない場合
                int tmp = end;
                /* StartIndex */
                for (;;) {
                    int q = (sstart + tmp) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    int suffixp = suffixArray[q] + offset; //suffixp >= slen then space < target
                    if (sstart == q) { if (suffixp >= slen || mySpace[suffixp] < target) sstart++; break; }
                    if (suffixp >= slen || mySpace[suffixp] < target) sstart = q;
                    else tmp = q;
                }
                if (sstart < end) spointer = suffixArray[sstart] + offset;
            }
            if (sstart < end && (epointer >= slen || mySpace[epointer] != target)) {
                int tmp = sstart;
                /* EndIndex */
                for(;;) {
                    int q = (tmp + end) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    int suffixp = suffixArray[q] + offset; //suffixp >= slen then space < target
                    if (tmp == q) { if (suffixp < slen && mySpace[suffixp] > target) end--; break;}
                    if (suffixp >= slen || mySpace[suffixp] > target) end = q;
                    else tmp = q;
                }
                if (sstart < end) epointer = suffixArray[end - 1] + offset;
            }

            //3 targetで部分文字列の等しい部分を探索
            if (tepointer >= tlen || myTarget[tepointer] != target) {
                int tmp = tstart;
                for(;;) {
                    int q = (tmp + targetIndex) >> 1;
                    int suffixp = targetSuffix[q] + offset;
                    if (tmp == q) { if (suffixp < tlen && myTarget[suffixp] > target) targetIndex--; break; }
                    if (suffixp >= tlen || myTarget[suffixp] > target) targetIndex = q;
                    else tmp = q;
                }
                tepointer = targetSuffix[targetIndex - 1] + offset;
            }

            //4 targetと異なる部分は再帰的に実行する
            if (targetIndex < tend) {
                calculate2_1(targetSuffix, targetIndex, tend, spaceSuffix, end, send, memo, offset, memoOffset, C1);
            }

            //5 memoに書き込み freq=0の時は終了
            if (end != sstart) {
                double iq = C1 - Math.log(end - sstart);
                for (int i = tstart; i < targetIndex; i++) {
                    memo[targetSuffix[i] + memoOffset] = iq;
                }
            } else return;

            //6 offsetを+1、関連パラメータの更新
            //calculate2_1(targetSuffix, tstart, targetIndex, spaceSuffix, sstart ,  end, memo, offset + 1, memoOffset + tlen - offset, C1);
            tend = targetIndex;
            send = end;
            memoOffset += tlen - offset;
            offset++;
            spointer++;
            epointer++;
        } 
    }

    public final double calculate2() {
        if (tlen > 65535) return calculate(); //必要なメモリサイズがInteger.MAX_VALUEを超える
        final double memo[] = new double[((tlen + 1) >> 1) * (tlen + ((tlen & 1) ^ 1))], C1 = Math.log((double) slen), C0 = 1 / Math.log(2d);
        int[] targetSuffix = IntStream.range(0, tlen).parallel().toArray(), tmparr = new int[tlen], tmparr2 = new int[tlen];
        int[] targetStr = IntStream.range(0, tlen).parallel().map(i -> myTarget[i] & 0xFF).toArray();

        sa.createSuffixArray(targetSuffix, targetStr, tmparr, tmparr2, tlen);
        // printSuffixArray(targetSuffix, myTarget, tlen);

        Arrays.fill(memo, Double.MAX_VALUE);
        calculate2_1(targetSuffix, 0, tlen, suffixArray, 0, slen, memo, 0, 0, C1);

        //memoの出力
        // int offset = 0;
        // StringBuilder sb = new StringBuilder();
        // for (int i = 0; i < tlen;) {
        //     for (int j = i; j < tlen; j++) {
        //         sb.append(memo[j + offset]);
        //         sb.append(' ');
        //     }
        //     sb.append('\n');
        //     i++;
        //     offset += tlen - i;
        // }
        // System.out.println(sb.toString());
        
        //結合
        for (int i = 2; i <= tlen; i++) {
            int j = tlen - i;
            int offset = 0;
            for (int k = 1; k < i; k++) {
                //print("1:", j + offset, j + k);
                memo[j + offset] += memo[j + k];
                offset += tlen - k + 1;
            }

            double min = Double.MAX_VALUE;
            j = tlen - i;
            offset = 0;
            for (int k = 0; k < i; k++) {
                //print("2:", j + offset);
                if (min > memo[j + offset]) min = memo[j + offset];
                offset += tlen - k;
            }
            memo[tlen-i] = min;
        }
        
        return memo[0] >= Double.MAX_VALUE ? Double.MAX_VALUE : memo[0] * C0;
    }
    
    /* estimationの関数呼び出しを展開 */
    public final double calculate() { //result.length = target.length
        final int suffix0 = this.suffix0, suffixl = this.suffixl, tlen = this.tlen, slen = this.slen;
        final double memo[] = new double[tlen];
        final double DOUBLE_MAX = Double.MAX_VALUE, C1 = Math.log10((double) slen);

        int start, p = start = 0, end = slen, targetstart, s = targetstart = tlen - 1, tmp, q, suffixp, freq, freqc = slen;
        int spointer = suffix0; //suffixArray[start] + p
        int epointer = suffixl; //suffixArray[end] + p
        double min = DOUBLE_MAX, cache = 0d, iq;
        for (;;) {
            byte target = myTarget[s];
            if (spointer >= slen || mySpace[spointer] != target) { //startの位置が正しくない場合
                tmp = end;
                /* StartIndex */
                for (;;) {
                    q = (start + tmp) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    suffixp = suffixArray[q] + p; //suffixp >= slen then space < target
                    if (start == q) { if (suffixp >= slen || mySpace[suffixp] < target) start++; break; }
                    if (suffixp >= slen || mySpace[suffixp] < target) start = q;
                    else tmp = q;
                }

                if (start == end) {
                    if (targetstart == 0) return min == DOUBLE_MAX ? DOUBLE_MAX : min * C0;
                    memo[--targetstart] = min;
                    s = targetstart; start = p = 0; end = slen; spointer = suffix0; epointer = suffixl; min = DOUBLE_MAX; //frequencerを初期化
                    continue;
                }
                spointer = suffixArray[start] + p;
            }
            if (epointer >= slen || mySpace[epointer] != target) {
                tmp = start;
                /* EndIndex */
                for(;;) {
                    q = (tmp + end) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    suffixp = suffixArray[q] + p; //suffixp >= slen then space < target
                    if (tmp == q) { if (suffixp < slen && mySpace[suffixp] > target) end--; break;}
                    if (suffixp >= slen || mySpace[suffixp] > target) end = q;
                    else tmp = q;
                }

                if (start == end) { 
                    if (targetstart == 0) return min == DOUBLE_MAX ? DOUBLE_MAX : min * C0;
                    memo[--targetstart] = min;
                    s = targetstart; start = p = 0; end = slen; spointer = suffix0; epointer = suffixl; min = DOUBLE_MAX; //frequencerを初期化
                    continue;
                }
                epointer = suffixArray[end - 1] + p;
            }
            freq = end - start;
            if (freqc != freq) {
                freqc = freq;
                cache = C1 - Math.log10((double) freq);
            }
            iq = cache + memo[s];
            if (iq < min) min = iq;
            if (++s == tlen) {
                if (targetstart == 0) return min * C0;
                memo[--targetstart] = min;
                s = targetstart; start = p = 0; end = slen; spointer = suffix0; epointer = suffixl; min = DOUBLE_MAX; //frequencerを初期化
                continue;
            }
            p++;
            spointer++;
            epointer++;
        }
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

    /* */
    //DEBUG用
    public static void print(int ...i) {
        print("", i);
    }
    public static void print(String header, int ...i) { //[debug]数値の一括出力
        System.out.print(header);
        for (int object : i) {
            System.out.print(object);
            System.out.print(' ');
        }
        System.out.println();
    }/**/

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


            frequencerObject.setTarget("HiHi".getBytes());
            System.out.println(frequencerObject.calculate());
            System.out.println(frequencerObject.calculate2());

            //                                         
            // ****  Please write code to check subByteStartIndex, and subByteEndIndex
            //

            int result = frequencerObject.frequency();
            System.out.print("Freq = "+ result+" ");
            if(4 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
            
            frequencerObject.setTarget("Hi Ho Hi".getBytes());
            result = frequencerObject.subByteStartIndex(0, 2); //Hi
            if(3 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
            result = frequencerObject.subByteStartIndex(0, 1); //H
            if(3 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
            result = frequencerObject.subByteStartIndex(0, 8); //Hi Ho Hi
            if(4 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
            result = frequencerObject.subByteEndIndex(0, 2); //Hi
            if(5 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
            result = frequencerObject.subByteEndIndex(0, 1); //H
            if(7 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
            result = frequencerObject.subByteEndIndex(0, 8); //Hi Ho Hi
            if(5 == result) { System.out.println("OK"); } else {System.out.println("WRONG:" + result); }
        }
        catch(Exception e) {
            System.out.println("STOP");
            e.printStackTrace();
        }
    }
}