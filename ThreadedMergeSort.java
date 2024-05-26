import java.util.concurrent.*;
import java.io.*;
import java.util.Random;

public class ThreadedMergeSort {
    private static final int ARRAY_SIZE = 1 << 23; // 2^23
    private static final int MAX_THREADS = 1024; // Max thread count
    private static final Random random = new Random(0);

    public static void main(String[] args) {
        int[] array = new int[ARRAY_SIZE];
        // Generate a random array
        for (int i = 0; i < ARRAY_SIZE; i++) {
            array[i] = random.nextInt();
        }

        try (PrintWriter writer = new PrintWriter(new File("merge_sort_results.csv"))) {
            // Writing headers for CSV file
            writer.println("Thread Count,Average Time (ms)");

            for (int threadCount = 1; threadCount <= MAX_THREADS; threadCount *= 2) {
                System.out.println("Starting experiments with " + threadCount + " threads.");
                double averageTime = runSort(array.clone(), threadCount);
                writer.println(threadCount + "," + averageTime);
                System.out.println("Completed: Threads=" + threadCount + ", Average Time=" + averageTime + "ms");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static double runSort(int[] array, int threadCount) {
        int trials = 5;
        long totalTime = 0;

        for (int trial = 1; trial <= trials; trial++) {
            System.out.println("Trial " + trial + "/" + trials + " for thread count: " + threadCount);
            long startTime = System.currentTimeMillis();

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            mergeSortParallel(array, 0, array.length - 1, executor);
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                    System.err.println("Threads didn't finish in the expected time");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long endTime = System.currentTimeMillis();
            totalTime += (endTime - startTime);
        }

        return totalTime / (double) trials;
    }

    private static void mergeSortParallel(int[] array, int left, int right, ExecutorService executor) {
        if (left < right) {
            int middle = left + (right - left) / 2;
            System.out.println("Sorting from " + left + " to " + middle + " and " + (middle + 1) + " to " + right);
            // Parallelize tasks
            Future<?> leftFuture = executor.submit(() -> mergeSortParallel(array, left, middle, executor));
            Future<?> rightFuture = executor.submit(() -> mergeSortParallel(array, middle + 1, right, executor));

            try {
                leftFuture.get();
                rightFuture.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }

            merge(array, left, middle, right);
            System.out.println("Merging from " + left + " to " + right);
        }
    }

    private static void merge(int[] array, int left, int middle, int right) {
        int[] leftArray = new int[middle - left + 1];
        int[] rightArray = new int[right - middle];

        System.arraycopy(array, left, leftArray, 0, leftArray.length);
        System.arraycopy(array, middle + 1, rightArray, 0, rightArray.length);

        int leftIndex = 0, rightIndex = 0;
        int targetIndex = left;

        while (leftIndex < leftArray.length && rightIndex < rightArray.length) {
            if (leftArray[leftIndex] <= rightArray[rightIndex]) {
                array[targetIndex++] = leftArray[leftIndex++];
            } else {
                array[targetIndex++] = rightArray[rightIndex++];
            }
        }

        while (leftIndex < leftArray.length) {
            array[targetIndex++] = leftArray[leftIndex++];
        }

        while (rightIndex < rightArray.length) {
            array[targetIndex++] = rightArray[rightIndex++];
        }
    }
}
