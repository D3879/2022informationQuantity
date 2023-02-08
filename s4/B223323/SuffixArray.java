//参考：https://webbibouroku.com/Blog/Article/suffix-array#outline__3_2

package s4.B223323;

import java.lang.*;
import java.util.Arrays;

public class SuffixArray {
    private int N;
    private int[] rank;
    private int[] tmp;
    private int[] sa;
    private int K;
    private Runnable sort;
    private static final int PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();

    public void createSuffixArray(byte[] s, int[] suffixArray) {
        N = s.length;
        rank = new int[N];
        tmp = new int[N];
        sa = suffixArray;
        K = 1;
        sort = new ParallelSort(PROCESSOR_NUM << 1);

        Arrays.parallelSetAll(rank, i -> s[i]);

        for (; K < N; K <<= 1) {
            sort.run();
            int i = 0, j = 0, l = sa[0], ri = rank[l], rj, it = (l + K) < N ? rank[l + K] : -1, jt;
            for (;;) {
                tmp[l] = j;
                if (++i == N) break;
                l = sa[i];
                rj = rank[l];
                jt = (l + K) < N ? rank[l + K] : -1;
                if (ri < rj) j++; else if (ri == rj && it < jt)  j++;
                ri = rj;
                it = jt;
            }
            int[] t = tmp; tmp = rank; rank = t;
        }
        return;
    }

    // private int cmp(int i, int j) {
    //     if (rank[i] != rank[j]) return rank[i] - rank[j];
    //     int ik = i + K;
    //     int jk = j + K;
    //     return (ik < N ? rank[ik] : -1) - (jk < N ? rank[jk] : -1);
    // }

    /* 参考:https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q1332844948 */
    private void msort(int[] data, int[] tmp, int left, int right) {
        // int mid = (right + left) >> 1;
        int len = right + left, l, r;
        for (l = left; l < right; l+=2) {
            int a = data[l], b = data[l+1];
            if (a < b) {
                tmp[l] = a;
                tmp[l+1] = b;
            } else {
                tmp[l] = b;
                tmp[l+1] = a;
            }
        }
        if (l == right) tmp[l] = data[l];
        for (int i = 4; i < len; i <<= 1){
            l = left; r = left + i;
            for (; r < right; r+=i, l+=i) {
                merge(data, tmp, l, (r + l) >> 1, r);
            }
            merge(data, tmp, l, (l + right) >> 1, right);
        }
        // if(mid > left) msort(data, tmp, left, mid);
        // ++mid;
        // if(right > mid) msort(data, tmp, mid, right);
        merge(data, tmp, left, (len + 2) >> 1, right);
        // merge(data, tmp, left, mid, right);
    }

    private void merge(int[] data, int[] tmp, int left, int mid, int right) {
        int lend = mid - 1, tp = left, l0 = left, N = right - left + 1;
        if (left > lend || mid > right){
            if (mid < right) {
                System.arraycopy(tmp, l0, data, l0, N - right + mid - 1);
                return;
            } else {
                System.arraycopy(data, left, tmp, tp, lend - left + 1);
                System.arraycopy(tmp, l0, data, l0, N);
                return;
            }
        }
        final int K = this.K, NK = this.N - K;
        int i = data[left], j = data[mid], ir = rank[i], jr = rank[j], it = i < NK ? rank[i + K] : -1, jt = j < NK ? rank[j + K] : -1;
        for (;;) {
            if (ir < jr || (ir == jr && it <= jt)) {
                tmp[tp] = i;
                if (left == lend) {
                    System.arraycopy(tmp, l0, data, l0, N - right + mid - 1);
                    return;
                }
                left++;
                i = data[left];
                ir = rank[i];
                it = i < NK ? rank[i + K] : -1;
            } else {
                tmp[tp] = j;
                if (mid == right) {
                    System.arraycopy(data, left, tmp, tp + 1, lend - left + 1);
                    System.arraycopy(tmp, l0, data, l0, N);
                    return; 
                }
                mid++;
                j = data[mid];
                jr = rank[j];
                jt = j < NK ? rank[j + K] : -1;
            }
            tp++;
        }
    }

    public static class Nop implements Runnable {
        private static final Nop instance = new Nop();
        public void run() {}
    }

    public class RangeSortThread implements Runnable {
        int left, right;
        public RangeSortThread(int left, int right) {
            this.left = left; this.right = right;
        }
        public void run() {
            msort(sa, tmp, left, right);
        }
    }

    public class MargeThread implements Runnable {
        Runnable a, b;
        int left, right, mid;

        public MargeThread(int left, int right, int processors) {
            this.left = left; this.right = right; mid = (left + right) >> 1;
            if (processors < 4 || right - left < 4) {
                a = (mid > left) ? new RangeSortThread(left, mid) : Nop.instance;
                ++mid;
                b = (right > mid) ? new RangeSortThread(mid, right) : Nop.instance;
            } else {
                int p1 = processors >> 1;
                a = new MargeThread(left, mid, p1);
                b = new MargeThread(++mid, right, processors - p1);
            }
        }

        public void run() {
            Thread t1 = new Thread(a);
            t1.start();
            Thread t2 = new Thread(b);
            t2.start();
            try {
                t1.join(); t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            merge(sa, tmp, left, mid, right);
        }
    }

    public class ParallelSort implements Runnable{
        Runnable a, b;
        int num, mid; //mid = (N + 1 >> 1)

        public ParallelSort(int processors) {
            num = N - 1; mid = num >> 1;
            if (processors < 4 || num < 4) {
                a = (mid > 0) ? new RangeSortThread(0, mid) : Nop.instance;
                ++mid;
                b = (num > mid) ? new RangeSortThread(mid, num) : Nop.instance;
            } else {
                int p1 = processors >> 1;
                a = new MargeThread(0, mid, p1);
                b = new MargeThread(++mid, num, processors - p1);
            }
        }

        public void run() {
            Thread t1 = new Thread(a);
            t1.start();
            Thread t2 = new Thread(b);
            t2.start();
            try {
                t1.join(); t2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            merge(sa, tmp, 0, mid, num);
        }
    }
}
