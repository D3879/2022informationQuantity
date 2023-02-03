//参考：https://webbibouroku.com/Blog/Article/suffix-array#outline__3_2

package s4.B223323;

import java.lang.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.IntStream;

public class SuffixArray {
    private int N;
    private int[] rank;
    private int[] tmp;
    private Integer[] sa;
    private int K;

    public void createSuffixArray(byte[] s, int[] suffixArray) {
        N = s.length;
        rank = new int[N];
        sa = IntStream.range(0, N).parallel().boxed().toArray(Integer[]::new);
        tmp = suffixArray;
        K = 1;
        Comparator<Integer> cmp = (arg0, arg1) -> cmp(arg0, arg1), cmp2 = (arg0, arg1) -> cmp2(arg0, arg1);

        Arrays.parallelSetAll(rank, i -> s[i]);
//        System.arraycopy(suffixArray, 0, rank, 0, N);

        // for (K = 1; K <= N; K <<= 1) {
        //     Arrays.parallelSort(sa, cmp);
        //     tmp[sa[0]] = 0;
        //     for (int i = 1; i <= N; i++) {
        //         tmp[sa[i]] = tmp[sa[i - 1]] + (cmp(sa[i - 1], sa[i]) < 0 ? 1 : 0);
        //     }
        //     System.arraycopy(tmp, 0, rank, 0, N + 1);
        // }
        for(;;) {
            Arrays.parallelSort(sa, cmp);
            if ((K << 1) >= N)
                break;
            tmp[sa[0]] = 0;
            for (int i = 1;i < N; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]] + (cmp(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            
            K <<= 1;

            Arrays.parallelSort(sa, cmp2);
            if ((K << 1) >= N)
                break;
            rank[sa[0]] = 0;
            for (int i = 1; i < N; i++) {
                rank[sa[i]] = rank[sa[i - 1]] + (cmp2(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            
            K <<= 1;
        }
        Arrays.parallelSetAll(suffixArray,  i -> sa[i]);
        return;
    }

    //compare sa with rank
    private int cmp(Integer _i, Integer _j) {
        int i = _i.intValue();
        int j = _j.intValue();
        if (rank[i] != rank[j]) return rank[i] - rank[j];
        int ik = i + K;
        int jk = j + K;
        return (ik < N ? rank[ik] : -1) - (jk < N ? rank[jk] : -1);
    }

    //compare sa with tmp as rank
    private int cmp2(Integer _i, Integer _j) {
        int i = _i.intValue();
        int j = _j.intValue();
        if (tmp[i] != tmp[j]) return tmp[i] - tmp[j];
        int ik = i + K;
        int jk = j + K;
        return (ik < N ? tmp[ik] : -1) - (jk < N ? tmp[jk] : -1);
    }
}
