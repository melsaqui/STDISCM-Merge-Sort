/*
 * USED FOR AUTOMATION PURPOSES ONLY
 * USED FOR AUTOMATION PURPOSES ONLY
 * USED FOR AUTOMATION PURPOSES ONLY
 * USED FOR AUTOMATION PURPOSES ONLY
 * USED FOR AUTOMATION PURPOSES ONLY
 * USED FOR AUTOMATION PURPOSES ONLY
 * USED FOR AUTOMATION PURPOSES ONLY
 */
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadedMergeSort {
    private static final long SEED = 1234; // Fixed seed for reproducibility
    private static final int N = (int) Math.pow(2, 23); // Size of the array, fixed at 2^23

    public static void main(String[] args) {
        Random random = new Random(SEED);
        int[] array = new int[N];
        for (int i = 0; i < N; i++) {
            array[i] = i + 1;
        }
        // Shuffle array using Fisher-Yates algorithm
        for (int i = N - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        int[] threadCounts = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};

        try (FileWriter writer = new FileWriter("merge_sort_results.csv")) {
            writer.append("Thread Count,Run 1,Run 2,Run 3,Run 4,Run 5,Average Time\n");

            for (int numThreads : threadCounts) {
                System.out.println("Testing with " + numThreads + " threads");
                long[] times = new long[5]; // To store times of 5 runs

                // Warm-up runs to allow the OS to cache the memory locations
                for (int i = 0; i < 3; i++) {
                    runSort(numThreads, array.clone()); // Ignore results
                }

                // Timed runs
                writer.append(numThreads + "");
                for (int i = 0; i < 5; i++) {
                    times[i] = runSort(numThreads, array.clone());
                    System.out.println("Run " + (i + 1) + ": " + times[i] + "ms");
                    writer.append("," + times[i]);
                }

                long averageTime = computeAverage(times);
                writer.append("," + averageTime + "\n");
                System.out.println("Average time for " + numThreads + " threads: " + averageTime + "ms\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static long runSort(int numThreads, int[] array) {
        List<Interval> intervals = generate_intervals(0, N - 1);
        long startTime = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        for (Interval interval : intervals) {
            executor.execute(() -> merge(array, interval.getStart(), interval.getEnd()));
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> frontier = new ArrayList<>();
        frontier.add(new Interval(start, end));

        int i = 0;
        while (i < frontier.size()) {
            int s = frontier.get(i).getStart();
            int e = frontier.get(i).getEnd();
            i++;

            if (s == e) {
                continue;
            }

            int m = s + (e - s) / 2;
            frontier.add(new Interval(m + 1, e));
            frontier.add(new Interval(s, m));
        }

        List<Interval> retval = new ArrayList<>();
        for (i = frontier.size() - 1; i >= 0; i--) {
            retval.add(frontier.get(i));
        }

        return retval;
    }

    public static void merge(int[] array, int s, int e) {
        if (s < e) {
            int m = s + (e - s) / 2;
            int[] left = new int[m - s + 1];
            int[] right = new int[e - m];
            int l_ptr = 0, r_ptr = 0;
            for (int i = s; i <= e; i++) {
                if (i <= m) {
                    left[l_ptr++] = array[i];
                } else {
                    right[r_ptr++] = array[i];
                }
            }
            l_ptr = r_ptr = 0;
            int i = s;
            while (l_ptr < left.length || r_ptr < right.length) {
                if (l_ptr < left.length && (r_ptr == right.length || left[l_ptr] <= right[r_ptr])) {
                    array[i++] = left[l_ptr++];
                } else if (r_ptr < right.length) {
                    array[i++] = right[r_ptr++];
                }
            }
            
        }
    }

    private static long computeAverage(long[] times) {
        long sum = 0;
        for (long time : times) {
            sum += time;
        }
        return sum / times.length;
    }
}

class Interval {
    private int start;
    private int end;

    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }
}
