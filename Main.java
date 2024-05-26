import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final long SEED = 1234; // Fixed seed for reproducibility

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the size of the array (N): ");
        int n = scanner.nextInt();
        System.out.print("Enter the number of threads: ");
        int numThreads = scanner.nextInt();
        scanner.close();

        Random random = new Random(SEED);
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = i + 1;
        }
        // Shuffle array using Fisher-Yates algorithm
        for (int i = n - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }

        List<Interval> intervals = generate_intervals(0, n - 1);

        long startTime = System.currentTimeMillis();

        if (numThreads > 1) {
            ExecutorService executor = Executors.newFixedThreadPool(numThreads);
            for (Interval interval : intervals) {
                executor.execute(() -> merge(array, interval.getStart(), interval.getEnd()));
            }
            executor.shutdown();
            try {
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            for (Interval interval : intervals) {
                merge(array, interval.getStart(), interval.getEnd());
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime) + "ms");

        // Verify if array is sorted
        if (isSorted(array)) {
            System.out.println("Array is sorted!");
        } else {
            System.out.println("Array is NOT sorted!");
        }
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

    public static boolean isSorted(int[] array) {
        for (int i = 0; i < array.length - 1; i++) {
            if (array[i] > array[i + 1]) {
                return false;
            }
        }
        return true;
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
