//参考：https://webbibouroku.com/Blog/Article/suffix-array#outline__3_2
//参考：http://midarekazu.g2.xrea.com/quicksort.html

package s4.B223323;

// import java.util.concurrent.ForkJoinPool;
// import java.util.concurrent.RecursiveAction;
// import java.util.stream.IntStream;

import java.util.Arrays;
import java.util.function.Function;

// public class SuffixArray {
//     static final int parallelism = Runtime.getRuntime().availableProcessors();
//     private int N;
//     private int[] rank;
//     private int[] tmp;
//     private int[] sa;
//     private int K;

//     public void createSuffixArray(int[] suffixArray, int[] str, int[] tmp1, int[] tmp2, int N) {
//         if (N < 2) return;
//         this.N = N;
//         this.rank = str;
//         this.sa = suffixArray;
//         this.tmp = tmp2;
//         int[] t = tmp1;
//         K = 1;
//         ForkJoinPool pool = new ForkJoinPool(parallelism);
//         MergeSortA task = new MergeSortA(sa, 0, N, parallelism);

//         for (;;) {
//             task.reinitialize();
//             pool.invoke(task);
//             if ((K << 1) >= N) break;
//             int i = 1, j = 0, l = sa[0], ri = rank[l], rj, it = (l + K) < N ? rank[l + K] : -1, jt;
//             tmp[l] = j;
//             while (i < N) {
//                 l = sa[i];
//                 rj = rank[l];
//                 jt = (l + K) < N ? rank[l + K] : -1;
//                 if (ri < rj || ri == rj && it < jt)  j++;
//                 i++;
//                 ri = rj;
//                 it = jt;
//                 tmp[l] = j;
//             }
//             K <<= 1;
//             rank = tmp; tmp = t; t = rank;
//         }
//         this.N = 0;
//         this.rank = null;
//         this.sa = null;
//         this.tmp = null;
//     }

//     // public void isSorted() {
//     //     isSorted(sa, 0, N, "out");
//     // }
//     // public void isSorted(int[] array, int i, int n) {
//     //     isSorted(array, i, n, "out");
//     // }

//     // public void isSorted(int[] array, int i, int n, String msg) {
//     //     while (++i < n) {
//     //         if (cmp(array[i-1], array[i]) > 0) System.out.println(msg);
//     //     }
//     // }

//     private int cmp(int i, int j) {
//         if (rank[i] != rank[j]) return rank[i] - rank[j];
//         int ik = i + K;
//         int jk = j + K;
//         return ik >= N ? (jk > N ? 0 : -1) : (jk >= N ? 1 : rank[ik] - rank[jk]);
//     }

//     private final void merge(final int src[], final int dist[], final int left, final int mid, final int right, final int[] rank, final int K, final int N) {
//         int l = left, r = mid, tp = left;
//         int lval = src[l];
//         int rval = src[r];
//         int tmp;

//         final int Nk = N - K;
//         int ri = rank[lval], rj = rank[rval], ik = lval + K, jk = rval + K;
//         boolean ikout = lval >= Nk, jkout = rval >= Nk;

//         for(;;) {
//             if (ri < rj || (ri == rj && (ikout || (!jkout && rank[ik] <= rank[jk]))))  {
//                 if (++l == mid) {
//                     dist[tp] = lval;
//                     System.arraycopy(src, r, dist, tp + 1, right - r);
//                     return;
//                 }
//                 tmp = lval;
//                 lval = src[l];
//                 ik = lval + K;
//                 ri = rank[lval];
//                 ikout = lval >= Nk;
//             } else {
//                 if (++r == right) {
//                     dist[tp] = rval;
//                     System.arraycopy(src, l, dist, tp + 1, mid - l);
//                     return;
//                 }
//                 tmp = rval;
//                 rval = src[r];
//                 jk = rval + K;
//                 rj = rank[rval];
//                 jkout = rval >= Nk;
//             }
//             dist[tp++] = tmp;
//         }
//     }

//     //arrayに出力
//     private final void insertSortA(final int array[], final int left, final int right) {
//         int i, j, t;
//         i = array[left];
//         if (cmp(i, array[left + 1]) > 0) {
//             array[left] = array[left + 1]; array[left + 1] = i;
//         }
//         for (i = left + 2; i < right; i++) {
//             t = array[i];
//             if (cmp(array[i - 1], t) > 0){
//                 j = i;
//                 do {
//                     array[j] = array[j - 1];
//                     j--;
//                 } while (j > left && cmp(t, array[j - 1]) < 0);
//                 array[j] = t;
//             }
//         }
//     }

//     //tmpに出力
//     private final void insertSortB(final int array[], final int tmp[], final int left, final int right) {
//         int i = left + 1;
//         if (cmp(array[left], array[i]) <= 0) {
//             tmp[left] = array[left]; tmp[i] = array[i];
//         } else {
//             tmp[left] = array[i]; tmp[i] = array[left];
//         }
//         int j;
//         while (++i < right) {
//             if (cmp(tmp[i-1], array[i]) > 0){
//                 j = i;
//                 do {
//                     tmp[j] = tmp[j - 1];
//                     j--;
//                 } while (j > left && cmp(array[i], tmp[j - 1]) < 0);
//                 tmp[j] = array[i];
//             } else tmp[i] = array[i];
//         }
//     }

//     private class SingleMergeSortB {
//         final int array[], left, mid, right;
//         final boolean haveTask;
//         SingleMergeSortA leftTask, rightTask;

//         public SingleMergeSortB(final int[] array, final int left, final int right) {
//             this.array = array;
//             this.left = left;
//             this.mid = (left + right) >> 1;
//             this.right = right;
//             if (right - left > 8) {
//                 haveTask = true;
//                 leftTask = new SingleMergeSortA(array, left, mid);
//                 rightTask = new SingleMergeSortA(array, mid, right);
//             } else haveTask = false;
//         }

//         public void sort(final int[] tmp, final int[] rank, final int K, final int N) {
//             if (haveTask) {
//                 leftTask.sort(tmp, rank, K, N);
//                 rightTask.sort(tmp, rank, K, N);
//                 merge(array, tmp, left, mid, right, rank, K, N);
//             } else {
//                 insertSortB(array, tmp, left, right);
//             }
//         }
//     }

//     private class SingleMergeSortA {
//         final int array[], left, mid, right;
//         final boolean haveTask;
//         SingleMergeSortB leftTask, rightTask;

//         public SingleMergeSortA(final int[] array, final int left, final int right) {
//             this.array = array;
//             this.left = left;
//             this.mid = (left + right) >> 1;
//             this.right = right;
//             if (right - left > 8) {
//                 haveTask = true;
//                 leftTask = new SingleMergeSortB(array, left, mid);
//                 rightTask = new SingleMergeSortB(array, mid, right);
//             } else {haveTask = false;}
//         }
        
//         public void sort(final int[] tmp, final int[] rank, final int K, final int N) {
//             if (haveTask) {
//                 leftTask.sort(tmp, rank, K, N);
//                 rightTask.sort(tmp, rank, K, N);
//                 merge(tmp, array, left, mid, right, rank, K, N);
//             } else {
//                 insertSortA(array, left, right);
//             }
//         }
//     }

//     /**
//      * 出力はtmp
//      */
//     private class MergeSortB extends RecursiveAction {
//         private final int threshold = 8;
//         final int[] array;
//         final int left, right, mid, n;
//         final boolean haveTask;
//         MergeSortA leftTask, rightTask;
//         SingleMergeSortA sleftTask, srightTask;
        
//         public MergeSortB(final int array[], final int left, final int right, int parallelism) {
//             this.array = array;
//             this.n = right - left;
//             this.left = left;
//             this.mid = (left + right) >> 1;
//             this.right = right;

//             if ((parallelism >>= 1) > 0 && n > threshold) {
//                 leftTask = new MergeSortA(array, left, mid, parallelism);
//                 rightTask = new MergeSortA(array, mid, right, parallelism);
//                 haveTask = true;
//             } else {
//                 if (n > threshold) {
//                     sleftTask = new SingleMergeSortA(array, left, mid);
//                     srightTask = new SingleMergeSortA(array, mid, right);
//                 }
//                 haveTask = false;
//             }
//         }

//         @Override
//         protected void compute() {
//             if (haveTask) {
//                 leftTask.reinitialize();
//                 leftTask.fork();
//                 rightTask.compute();
//                 leftTask.join();
//                 leftTask.reinitialize();
//                 merge(array, tmp, left, mid, right, rank, K, N);
//             } else if (n > threshold){
//                 sleftTask.sort(tmp, rank, K, N);
//                 srightTask.sort(tmp, rank, K, N);
//                 merge(array, tmp, left, mid, right, rank, K, N);
//             } else {
//                 insertSortB(array, tmp, left, right);
//             }
//         }
//     }

//     /**
//      * 出力はarray
//      */
//     private class MergeSortA extends RecursiveAction{
        

//         private final int threshold = 8;
//         final int[] array;
//         final int left, right, mid, n;
//         final boolean haveTask;
//         MergeSortB leftTask, rightTask;
//         SingleMergeSortB sleftTask, srightTask;

//         public MergeSortA(final int array[], final int left, final int right, int parallelism) {
//             this.array = array;
//             this.n = right - left;
//             this.left = left;
//             this.mid = (left + right) >> 1;
//             this.right = right;

//             if ((parallelism >>= 1) > 0 && n > threshold) {
//                 leftTask = new MergeSortB(array, left, mid, parallelism);
//                 rightTask = new MergeSortB(array, mid, right, parallelism);
//                 haveTask = true;
//             } else {
//                 if (n > threshold) {
//                     sleftTask = new SingleMergeSortB(array, left, mid);
//                     srightTask = new SingleMergeSortB(array, mid, right);
//                 }
//                 haveTask = false;
//             }
//         }
//         @Override
//         protected void compute() {
//             if (haveTask) {
//                 leftTask.reinitialize();
//                 leftTask.fork();
//                 rightTask.compute();
//                 leftTask.join();
//                 merge(tmp, array, left, mid, right, rank, K, N);
//             } else if (n > threshold) {
//                 sleftTask.sort(tmp, rank, K, N);
//                 srightTask.sort(tmp, rank, K, N);
//                 merge(tmp, array, left, mid, right, rank, K, N);
//             } else {
//                 insertSortA(array, left, right);
//             }
//         }
//     }


//     private final void printSuffixArray(byte[] str, int[] suffixArray, int n) {
//         System.out.println("printSuffixArray");
//         for(int i=0; i< n; i++) {
//             int s = suffixArray[i];
//             System.out.printf("suffixArray[%2d]=%2d:", i, s);
//             for(int j=s;j<n;j++) {
//                 System.out.write(str[j]);
//             }
//             System.out.write('\n');
//         }
//     }

//     public static void main(String[] args) {
//         byte[] array = "Hi Ho Hi Ho Hi Ho Hi Ho".getBytes();

//         final int n = array.length;
//         int[] str = IntStream.range(0, n).parallel().map(i->array[i] & 0xFF).toArray();
//         int[] suffixArray = IntStream.range(0, n).parallel().toArray(), tmp1 = new int[n], tmp2 = new int[n];
//         SuffixArray sort = new SuffixArray();
//         sort.createSuffixArray(suffixArray, str, tmp1, tmp2, n);
//         sort.printSuffixArray(array, suffixArray, n);
//     }
// }

class SuffixArray {
    public static int[] suffixArray(char[] s) {
        int n = s.length;
        int[] a = new int[n];
        Arrays.setAll(a, i -> s[i] - 'a');
        return sais(a, 26);
    }

    public static int[] createSuffixArray(byte[] s) {
        int n = s.length;
        int[] a = new int[n];
        Arrays.setAll(a, i -> s[i] + 128);
        return sais(a, 256);
    }

    public static int[] createSuffixArray(int[] s) {
        int min, max;
        if (s[0] < s[1]) {
            min = s[0]; max = s[1];
        } else {
            min = s[1]; max = s[0];
        }
        for (int i = 2, len = s.length; i < len; i++) {
            int t = s[i];
            if (min > t) min = t;
            else if (max < t) max = t;
        }
        if (min < 0) {
            int m = min;
            Arrays.setAll(s, i -> s[i] - m);
        }
        return sais(s, max - min);
    }

    private static int[] sais(int[] s, int k) {
        int n = s.length;
        int m = 0;

        boolean[] ls = new boolean[n];
        int[] lmsMap = new int[n];
        int[] sumL = new int[k + 1];
        int[] sumS = new int[k + 1];
        for (int i = n - 2; i >= 0; i--) {
            ls[i] = s[i] < s[i + 1] || s[i] == s[i + 1] && ls[i + 1];
            if (!ls[i] && ls[i + 1]) {
                m++;
            }
        }
        lmsMap[0] = -1;
        if (ls[0]) {
            sumL[s[0] + 1]++;
        } else {
            sumS[s[0]]++;
        }

        int[] lms = new int[m];
        for (int i = 1, j = 0; i < n; i++) {
            if (!ls[i - 1] && ls[i]) {
                lmsMap[i] = j;
                lms[j++] = i;
            } else {
                lmsMap[i] = -1;
            }
            if (ls[i]) {
                sumL[s[i] + 1]++;
            } else {
                sumS[s[i]]++;
            }
        }

        for (int i = 0; i < k; i++) {
            sumS[i] += sumL[i];
            sumL[i + 1] += sumS[i];
        }
        sumS[k] += sumL[k];

        Function<int[], int[]> inducedSort = a -> {
            int[] sa = new int[n];
            Arrays.fill(sa, -1);
            int[] buf = new int[k + 1];
            System.arraycopy(sumS, 0, buf, 0, k + 1);
            for (int d : a) {
                sa[buf[s[d]]++] = d;
            }
            System.arraycopy(sumL, 0, buf, 0, k + 1);
            sa[buf[s[n - 1]]++] = n - 1;
            for (int i = 0; i < n; i++) {
                int v = sa[i] - 1;
                if (v >= 0 && !ls[v]) {
                    sa[buf[s[v]]++] = v;
                }
            }
            System.arraycopy(sumL, 0, buf, 0, k + 1);
            for (int i = n - 1; i >= 0; i--) {
                int v = sa[i] - 1;
                if (v >= 0 && ls[v]) {
                    sa[--buf[s[v] + 1]] = v;
                }
            }
            return sa;
        };
        int[] sa = inducedSort.apply(lms);
        if (m == 0) {
            return sa;
        }
        int[] sortedLms = new int[m];
        int p = 0;
        for (int i : sa) {
            if (lmsMap[i] != -1) {
                sortedLms[p++] = i;
            }
        }
        int[] recS = new int[m];
        int recK = 0;
        recS[lmsMap[sortedLms[0]]] = 0;
        for (int i = 1; i < m; i++) {
            int l = sortedLms[i - 1];
            int r = sortedLms[i];
            int endL = lmsMap[l] + 1 < m ? lms[lmsMap[l] + 1] : n;
            int endR = lmsMap[r] + 1 < m ? lms[lmsMap[r] + 1] : n;
            boolean same = endL - l == endR - r;
            if (same) {
                while (l < endL && s[l] == s[r]) {
                    l++;
                    r++;
                }
                same = l < n && s[l] == s[r];
            }
            if (!same) {
                recK++;
            }
            recS[lmsMap[sortedLms[i]]] = recK;
        }
        int[] recSA = sais(recS, recK);
        Arrays.setAll(sortedLms, i -> lms[recSA[i]]);
        return inducedSort.apply(sortedLms);
    }
}