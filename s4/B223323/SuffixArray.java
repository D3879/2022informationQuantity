//参考：https://webbibouroku.com/Blog/Article/suffix-array#outline__3_2

package s4.B223323;

import java.lang.*;
import java.util.Arrays;
import java.util.stream.IntStream;

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
        sa = IntStream.range(0, N).toArray();
        tmp = suffixArray;
        K = 1;
        sort = new ParallelSort(PROCESSOR_NUM << 1);

        Arrays.parallelSetAll(rank, i -> s[i]);

        for (K = 1; K < N; K <<= 1) {
            sort.run();
            tmp[sa[0]] = 0;
            for (int i = 1; i < N; i++) {
                tmp[sa[i]] = tmp[sa[i - 1]] + (cmp(sa[i - 1], sa[i]) < 0 ? 1 : 0);
            }
            System.arraycopy(tmp, 0, rank, 0, N);
        }
        Arrays.parallelSetAll(suffixArray,  i -> sa[i]);
        return;
    }

    private int cmp(int i, int j) {
        if (rank[i] != rank[j]) return rank[i] - rank[j];
        int ik = i + K;
        int jk = j + K;
        return (ik < N ? rank[ik] : -1) - (jk < N ? rank[jk] : -1);
    }

    /* 参考:https://detail.chiebukuro.yahoo.co.jp/qa/question_detail/q1332844948 */
    private void msort(int[] data, int[] tmp, int left, int right) {
        // int mid = (right + left) >> 1;
        for (int i = 2; i < right + left; i <<= 1){
            int l = left, r = left + i;
            for (; r < right; r+=i, l+=i) {
                merge(data, tmp, l, (r + l) >> 1, r);
            }
            merge(data, tmp, l, (l + right) >> 1, right);
        }
        // if(mid > left) msort(data, tmp, left, mid);
        // ++mid;
        // if(right > mid) msort(data, tmp, mid, right);
        merge(data, tmp, left, (left + right + 2) >> 1, right);
        // merge(data, tmp, left, mid, right);
    }

    private void merge(int[] data, int[] tmp, int left, int mid, int right) {
        int lend = mid - 1, tp = left, l0 = left, N = right - left + 1;
        if (left > lend || mid > right){
            if (mid < right) for(;;) {
                tmp[tp] = data[mid];
                if (mid == right) { System.arraycopy(tmp, l0, data, l0, N); return; }
                ++mid; ++tp;
            } else for(;;) {
                tmp[tp] = data[mid];
                if (left == lend) { System.arraycopy(tmp, l0, data, l0, N); return; }
                ++left; ++tp;
            }
        }
        for (;;) {
            if (cmp(data[left], data[mid]) <= 0) {
                tmp[tp] = data[left];
                tp++;
                if (left != lend){
                    left++;
                    continue;
                }
                for(;;) {
                    tmp[tp] = data[mid];
                    if (mid == right) { System.arraycopy(tmp, l0, data, l0, N); return; }
                    ++mid; ++tp;
                }
            } else {
                tmp[tp] = data[mid];
                tp++;
                if (mid != right){
                    mid++;
                    continue;
                }
                for(;;) {
                    tmp[tp] = data[left];
                    if (left == lend) { System.arraycopy(tmp, l0, data, l0, N); return; }
                    ++left; ++tp;
                }
            }
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
        int num, mid;

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
