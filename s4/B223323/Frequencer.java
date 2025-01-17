package s4.B223323;

import java.util.Arrays;
import java.util.stream.IntStream;

import s4.B223323.Frequencer.Tree.Node;
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

    private static final int MAX_WORD_LENGTH = -1;



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

        suffixArray = SuffixArray.createSuffixArray(space);
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


    public class Tree {
        final double C1;
        final Node root;
        // int lastFreq;
        // Double lastIq = 0d;

        // int hit;
        // int n;

        public Tree(double c1, byte target) {
            C1 = c1;
            // lastFreq = slen;
            root = create(target, 0, slen, 0);
        }

        public class Node {
            private final byte item;
            private Node left, right, next;
            private final int start, end; //itemの範囲 freq=0でもしっかり設定する必要がある。　←　次のノード作成の参考にしているため
            private final int istart, iend; //初期範囲
            private final int offset;
            private final boolean max;
            private final double iq;

            public Node(byte item, int start, int end, int istart, int iend, int offset, double iq) {
                this.item = item;
                this.start = start;
                this.end = end;
                this.istart = istart;
                this.iend = iend;
                this.offset = offset;
                this.max = start == end;
                this.iq = iq;
            }
            public Node(byte item, int start, int istart, int iend, int offset) {
                this.item = item;
                this.start = start;
                this.end = start;
                this.istart = istart;
                this.iend = iend;
                this.offset = offset;
                this.max = true;
                this.iq = 0;
            }

            //同一offset内でtargetを探索し、一致するノードを返却
            //未定義の場合は定義する
            public final Node get(final byte target) {               
                Node p = this;
                while (target != p.item) {
                    if (target < p.item) {
                        if (p.left == null) { //左に追加
                            p.left = create(target, p.istart, p.start, offset);
                            return p.left;
                        }
                        p = p.left;
                    } else {
                        if (p.right == null) { //右に追加
                            p.right = create(target, p.end, p.iend, offset);
                            return p.right;
                        }
                        p = p.right;
                    }
                }
                return p;
            }

            //offsetを+1したNodeを探索
            public final Node getNext(byte target) {
                if (next == null) {
                    next = create(target, start, end, offset + 1);
                    return next;
                } else {
                    return next.get(target);
                }
            }

            //return iq == Double.MAX_VALUE
            public final boolean max() {
                return max;
            }

            //maxがfalseの時のみ有効
            public final double iq() {
                return iq;
            }
        }

        public final Node create(final byte target, int start, int end, final int offset) {
            if (start == end || offset == MAX_WORD_LENGTH) { return new Node(target, start, start, end, offset); }
            final int spointer = suffixArray[start] + offset, epointer = suffixArray[end-1] + offset;
            
            int istart = start;
            int iend = end;
            //2 targetのfreqを計算(start,endを求める)
            if (spointer >= slen || mySpace[spointer] != target) { //startの位置が正しくない場合
                int tmp = end;
                /* StartIndex */
                for (;;) {
                    int q = (start + tmp) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    int suffixp = suffixArray[q] + offset; //suffixp >= slen then space < target
                    if (start == q) { if (suffixp >= slen || mySpace[suffixp] < target) start++; break; }
                    if (suffixp >= slen || mySpace[suffixp] < target) start = q;
                    else tmp = q;
                }
                if (start == end) { return new Node(target, start, istart, iend, offset); }
            }
            if (epointer >= slen || mySpace[epointer] != target) {
                int tmp = start;
                /* EndIndex */
                for(;;) {
                    int q = (tmp + end) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    int suffixp = suffixArray[q] + offset; //suffixp >= slen then space < target
                    if (tmp == q) { if (suffixp < slen && mySpace[suffixp] > target) end--; break;}
                    if (suffixp >= slen || mySpace[suffixp] > target) end = q;
                    else tmp = q;
                }
                if (start == end) { return new Node(target, start, istart, iend, offset); }
            }

            // n++;

            // int freq = end - start;
            // if (freq != lastFreq) { lastFreq = freq; lastIq = C1 - Math.log10(freq); }

            // else hit++;


            return new Node(target, start, end, istart, iend, offset, C1 - Math.log10(end - start));
        }
    }

    /**
     * 
     * spaceはほとんど処理時間に影響を与えない。おそらくlogオーダー
     * targetは2乗のオーダーで処理時間に影響を与える
     * 
     * 改善案1：１文字として扱う長さを可変にし、疑似的にtargetの長さを短くし、これを基にiq計算　→　本当に可能？
     * 改善案3：何らかの方法で区切りを確定させ、targetを分割する　→　区切りを見つける方法が思いつかない(freq=0になる前に行えないと意味がない←freq=0になったらそれ以降その地点を超えて計算が進むことはない)
     * 改善案4：下限のチェック　→　ランダムケースで意味がなさそう(チェックのための時間のほうが大きくなりそう)
     * 改善案5: データ構造の変更(改善の余地があり)
     * 現在のデータ構造は三分探索木にあたる
     * 代替データ構造
     * ・ダブル配列(トライ木)
     * ・AA木
     * ・スプレー木(最近アクセスした要素に素早くアクセス可能)
     * 
     * 
     * calculate()と比較して
     * 空間使用量はtreeの分だけ増加
     * ランダムケース：spaceの大きさが大きくなるほど改善がみられる 確認した範囲では1.0~2.0倍程度の改善がみられる
     * 最悪ケース：spaceの大きさが大きくなるほど改善がみられる 参考：space:1MB target:10KB の時7倍程度 space:10MB target:10KB の時15倍程度
     * space:100B,target:10Bのときランダム、最悪ともに悪化することを確認した。最大で13msの悪化を確認。
     * spaceとtargetの長さが短く、オーバーヘッドの影響が大きくなることに起因するため、spaceとtargetの大きさを基に使用アルゴリズムの変更することで対処可能。
     * 数千回繰り返し使用するような特殊ケースを除いて、有限時間内に終了するという目的を考えると、誤差として許容可能な範囲だと考えられる。
     * 
     * @return
     */ 
    public final double calculate3() {
        final double memo[] = new double[tlen], C0 = 1 / Math.log10(2d);
        final Tree tree = new Tree(Math.log10(slen), myTarget[tlen - 1]);
        int s, targetstart = s = tlen - 1;
        Node node = tree.root;
        double min = Double.MAX_VALUE;
        for (;;) {
            if (node.max) {
                if (targetstart == 0) {
                    // System.out.println((double)tree.hit / (double)tree.n);
                    return min == Double.MAX_VALUE ? Double.MAX_VALUE : min * C0;
                }

                memo[--targetstart] = min;
                s = targetstart; min = Double.MAX_VALUE;
                node = tree.root.get(myTarget[s]);
            } else {
                double iq = memo[s] + node.iq;
                if (iq < min) min = iq;
                if (++s == tlen) {
                    if (targetstart == 0) {
                        // System.out.println((double)tree.hit / (double)tree.n);
                        return min * C0;
                    }
                    memo[--targetstart] = min;
                    s = targetstart; min = Double.MAX_VALUE;
                    node = tree.root.get(myTarget[s]);
                } else {
                    node = node.getNext(myTarget[s]);
                }
            }
        }
    }

    /**
     * start/endが変化したときにLCPにRMQを行う→RMQresult O(1)
     * offset < RMQresultの時はoffsetをRMQresultまでジャンプ
     * 
     */
    
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
            // if (targetstart == tlen - END) {
            //     return memo[tlen - END];
            // }
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

            // if (targetstart < tlen - END + 2) {
            //     print(s, target, start, end);
            // }
            
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
    public static void print(Object ...i) {
        for (Object object : i) {
            System.out.print(object);
            System.out.print(' ');
        }
        System.out.println();
    }
    public static void print(String header, Object ...i) { //[debug]数値の一括出力
        System.out.print(header);
        print(i);
    }/**/
    public static <T> void printArray(T[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(' ');
        }
    }
    public static void printArray(double[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(' ');
        }
    }
    public static void printArray(int[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(' ');
        }
    }
    public static void printArray(byte[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(' ');
        }
    }
    public static void printArray(char[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(' ');
        }
    }
    public static void printArray(long[] array) {
        for (int i = 0; i < array.length; i++) {
            System.out.print(array[i]);
            System.out.print(' ');
        }
    }

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

    
    /**
     * calculateと結果が変わる　→　どちらかにバグあり　→　多分こちら
     * 空間使用量がO(n^2)
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
    public final boolean calculate2_1(final int[] targetSuffix, int tstart, int tend, final int[] spaceSuffix, int sstart, int send, final double[] memo, int offset, int memoOffset, final double C1) {
        int end = send;
        int spointer = suffixArray[sstart] + offset;
        int epointer = suffixArray[send-1] + offset;
        int tepointer = targetSuffix[tend-1] + offset;
        int targetIndex = tend;
        for(;;) {
            //1 targetをsuffixArrayから取得
            while (targetSuffix[tstart] + offset >= tlen) {
                tstart++;
                if (tstart >= tend) return false;
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
            if (targetIndex < tend && end < send) {
                if (calculate2_1(targetSuffix, targetIndex, tend, spaceSuffix, end, send, memo, offset, memoOffset, C1)) return true;
            }

            //5 memoに書き込み freq=0の時は終了
            if (end != sstart) {
                double iq = C1 - Math.log10(end - sstart);
                for (int i = tstart; i < targetIndex; i++) {
                    memo[targetSuffix[i] + memoOffset] = iq;
                }
            } else return offset == 0;

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
        final double memo[] = new double[((tlen + 1) >> 1) * (tlen + ((tlen & 1) ^ 1))], C1 = Math.log10((double) slen), C0 = 1 / Math.log10(2d);
        int[] targetSuffix = IntStream.range(0, tlen).parallel().toArray();

        targetSuffix = SuffixArray.createSuffixArray(myTarget);
        // printSuffixArray(targetSuffix, myTarget, tlen);

        Arrays.fill(memo, Double.MAX_VALUE);
        if (calculate2_1(targetSuffix, 0, tlen, suffixArray, 0, slen, memo, 0, 0, C1)) return Double.MAX_VALUE;

        // memoの出力
        // StringBuilder sb = new StringBuilder();
        // for (int i = 0, offset = 0; i < tlen;) {
        //     for (int j = i; j < tlen; j++) {
        //         sb.append(memo[j + offset] * C0);
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

        // sb.setLength(0);
        // for (int i = 0, offset = 0; i < tlen;) {
        //     for (int j = i; j < tlen; j++) {
        //         sb.append(memo[j + offset] * C0);
        //         sb.append(' ');
        //     }
        //     sb.append('\n');
        //     i++;
        //     offset += tlen - i;
        // }
        // System.out.println(sb.toString());
        
        return memo[0] >= Double.MAX_VALUE ? Double.MAX_VALUE : memo[0] * C0;
    }

    int sall, eall, sn, en, sen, h;

    public final double count() {
        double res = _count();

        StringBuilder sb = new StringBuilder();
        sb.append("check     (start, end):");
        sb.append(sall);
        sb.append(", ");
        sb.append(eall);
        sb.append("\nnot match (start, end):");
        sb.append(sn);
        sb.append(", ");
        sb.append(en);
        sb.append("\nfrec change           :");
        sb.append(sen);
        sb.append("\ncompare               :");
        sb.append(h);
        System.out.println(sb.toString());
        return res;
    }
    
    private final double _count() { //result.length = target.length
        final int suffix0 = this.suffix0, suffixl = this.suffixl, tlen = this.tlen, slen = this.slen;
        final double memo[] = new double[tlen];
        final double DOUBLE_MAX = Double.MAX_VALUE, C1 = Math.log10((double) slen);

        int start, p = start = 0, end = slen, targetstart, s = targetstart = tlen - 1, tmp, q, suffixp, freq, freqc = slen;
        int spointer = suffix0; //suffixArray[start] + p
        int epointer = suffixl; //suffixArray[end] + p
        double min = DOUBLE_MAX, cache = 0d, iq;

        sall = 0;
        eall = 0; //それぞれの検査数
        sn = 0; //start 不一致数
        en = 0; //end 不一致数
        sen = 0; //start|end 不一致数
        h = 0; //比較回数
        boolean flag;

        for (;;) {
            sall++;
            flag = true;
            byte target = myTarget[s];
            h++;
            if (spointer >= slen || mySpace[spointer] != target) { //startの位置が正しくない場合
                tmp = end;
                sn++;
                sen++;
                flag = false;
                /* StartIndex */
                for (;;) {
                    q = (start + tmp) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    suffixp = suffixArray[q] + p; //suffixp >= slen then space < target
                    h++;
                    if (start == q) { if (suffixp >= slen || mySpace[suffixp] < target) start++; break; }
                    if (suffixp >= slen || mySpace[suffixp] < target) start = q;
                    else tmp = q;
                }

                if (start == end) {
                    if (targetstart == 0) {
                        return min == DOUBLE_MAX ? DOUBLE_MAX : min * C0;
                    }
                    memo[--targetstart] = min;
                    s = targetstart; start = p = 0; end = slen; spointer = suffix0; epointer = suffixl; min = DOUBLE_MAX; //frequencerを初期化
                    continue;
                }
                spointer = suffixArray[start] + p;
            }
            eall++;
            h++;
            if (epointer >= slen || mySpace[epointer] != target) {
                tmp = start;
                en++;
                if (flag) sen++;

                /* EndIndex */
                for(;;) {
                    q = (tmp + end) >> 1; //(s+e)/2 -> qの定義域[s,e-1]
                    suffixp = suffixArray[q] + p; //suffixp >= slen then space < target
                    h++;
                    if (tmp == q) { if (suffixp < slen && mySpace[suffixp] > target) end--; break;}
                    if (suffixp >= slen || mySpace[suffixp] > target) end = q;
                    else tmp = q;
                }

                if (start == end) { 
                    if (targetstart == 0) {
                        return min == DOUBLE_MAX ? DOUBLE_MAX : min * C0;
                    }
                    memo[--targetstart] = min;
                    s = targetstart; start = p = 0; end = slen; spointer = suffix0; epointer = suffixl; min = DOUBLE_MAX; //frequencerを初期化
                    continue;
                }
                epointer = suffixArray[end - 1] + p;
            }

            // if (targetstart < tlen - END + 2) {
            //     print(s, target, start, end);
            // }
            
            freq = end - start;
            if (freqc != freq) {
                freqc = freq;
                cache = C1 - Math.log10((double) freq);
            }
            iq = cache + memo[s];
            if (iq < min) min = iq;
            if (++s == tlen) {
                if (targetstart == 0) {
                    return min * C0;
                }
                memo[--targetstart] = min;
                s = targetstart; start = p = 0; end = slen; spointer = suffix0; epointer = suffixl; min = DOUBLE_MAX; //frequencerを初期化
                continue;
            }
            p++;
            spointer++;
            epointer++;
        }
    }
}