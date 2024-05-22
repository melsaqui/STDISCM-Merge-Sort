import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;


public class Main {
    public static void main(String[] args) {
        int N; //array size
        int nThread;
        int[]array;
        Scanner s = new Scanner(System.in);
        // TODO: Seed your randomizer
        final int seed = 0; // Make sure the seed is defined as a constant
        Random random =new Random(seed);

        // TODO: Get array size and thread count from user
        System.out.println("Enter Array Size: ");
        N= s.nextInt();
        System.out.println("Enter Thread Count: ");
        nThread =s.nextInt();
        array = new int[N];

        s.close();
        System.out.println(" ");

        // TODO: Generate a random array of given size
        for (int i = 0; i<N; i++){
            array[i] = i+1;
        }
        for (int j = 0; j < N; j++) {
			int randomIndexToSwap = random.nextInt(array.length);
			int temp = array[randomIndexToSwap];
			array[randomIndexToSwap] = array[j];
			array[j] = temp;
		}
        /*System.out.println(" shuffled Array ");
        for (int value :array) { 
            System.out.println(value+" "); 
        } */

        // TODO: Call the generate_intervals method to generate the merge 
        // sequence

        // TODO: Call merge on each interval in sequence

        List<Interval> intervals;
        intervals = generate_intervals(1, N);
        for (int k =0; k<intervals.size();k++){
            merge(array,intervals.get(k).getStart()-1,intervals.get(k).getEnd()-1);
        }
        // Once you get the single-threaded version to work, it's time to 
        // implement the concurrent version. Good luck :)
        System.out.println("Sorted Array ");
        for (int value :array) { 
            System.out.println(value+" "); 
        } 
    }

    /*
    This function generates all the intervals for merge sort iteratively, given 
    the range of indices to sort. Algorithm runs in O(n).

    Parameters:
    start : int - start of range
    end : int - end of range (inclusive)

    Returns a list of Interval objects indicating the ranges for merge sort.
    */
    public static List<Interval> generate_intervals(int start, int end) {
        List<Interval> frontier = new ArrayList<>();
        frontier.add(new Interval(start,end));

        int i = 0;
        while(i < frontier.size()){
            int s = frontier.get(i).getStart();
            int e = frontier.get(i).getEnd();

            i++;

            // if base case
            if(s == e){
                continue;
            }

            // compute midpoint
            int m = s + (e - s) / 2;

            // add prerequisite intervals
            frontier.add(new Interval(m + 1,e));
            frontier.add(new Interval(s,m));
        }

        List<Interval> retval = new ArrayList<>();
        for(i = frontier.size() - 1; i >= 0; i--) {
            retval.add(frontier.get(i));
        }

        return retval;
    }

    /*
    This function performs the merge operation of merge sort.

    Parameters:
    array : vector<int> - array to sort
    s     : int         - start index of merge
    e     : int         - end index (inclusive) of merge
    */
    public static void merge(int[] array, int s, int e) {
        int m = s + (e - s) / 2;
        int[] left = new int[m - s + 1];
        int[] right = new int[e - m];
        int l_ptr = 0, r_ptr = 0;
        for(int i = s; i <= e; i++) {
            if(i <= m) {
                left[l_ptr++] = array[i];
            } else {
                right[r_ptr++] = array[i];
            }
        }
        l_ptr = r_ptr = 0;

        for(int i = s; i <= e; i++) {
            // no more elements on left half
            if(l_ptr == m - s + 1) {
                array[i] = right[r_ptr];
                r_ptr++;

            // no more elements on right half or left element comes first
            } else if(r_ptr == e - m || left[l_ptr] <= right[r_ptr]) {
                array[i] = left[l_ptr];
                l_ptr++;
            } else {
                array[i] = right[r_ptr];
                r_ptr++;
            }
        }
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

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
}