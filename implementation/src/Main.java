import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Main {

    static class Item {
        int weight;
        int value;
        int index;

        protected Item(int index, int weight, int value)
        {
            this.weight = weight;
            this.value = value;
            this.index = index;
        }
    }

    public static void main(String[] args)
    {
        assert (args.length > 0);

        String fileString = "C:\\Users\\desti\\OneDrive - California Polytechnic State University\\Winter 2020\\CPE 349\\Final project\\knapsack-analysis\\implementation\\src\\" + args[0];

        ArrayList<Item> items = new ArrayList<>();
        int maxCapacity = -1;

        // read from file into list of items and get max weight
        try {
            List<String> allLines = Files.readAllLines(Paths.get(fileString));
            for (int i = 1; i < allLines.size(); i++) {
                if (i == allLines.size() - 1) {
                    maxCapacity = Integer.valueOf(allLines.get(allLines.size() - 1));
                } else {
                    StringTokenizer st1 = new StringTokenizer(allLines.get(i));
                    int index = Integer.valueOf(st1.nextToken());
                    int value = Integer.valueOf(st1.nextToken());
                    int weight = Integer.valueOf(st1.nextToken());
                    items.add(new Item(index, weight, value));
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        assert (maxCapacity > -1);
        for (int i = 1; i < args.length; i++) {
            String flag = args[i];
            if (flag.equals("-e")) {
                exhaustiveSolution(items, maxCapacity);
            } else if (flag.equals("-g")) {
                greedySolution(items, maxCapacity);
            } else if (flag.equals("-d")) {
                dynamicSolution(items, maxCapacity);
            } else if (flag.equals("-b")) {
                branchAndBoundSolution(items, maxCapacity);
            }
        }

    }

    public static void exhaustiveSolution(List<Item> items, int max)
    {
        // Value 234, Weight 17
        //1 3 7 12...
        int bestVal = Integer.MIN_VALUE;
        int bestWeight = -1;
        ArrayList<Integer> bestSolution = null;

        ArrayList<Integer> currSolution = new ArrayList<>();
        int curVal = 0;
        int curWeight = 0;
        Item currItem;
        long numitems = items.size();
        System.out.println("Item size " + numitems + " Total sets " + (1 << numitems));
        long numSubSets = 1 << numitems;

        // iterate through all possible subsets
        for (long i = 0; i < numSubSets; i++) {
            currSolution.clear();
            curVal = 0;
            curWeight = 0;

            // Print current subset
            for (int j = 0; j < numitems; j++) {
                if ((i & (1 << j)) > 0) {
                    currItem = items.get(j);
                    curVal += currItem.value;
                    curWeight += currItem.weight;
                    currSolution.add(currItem.index);
                }
            }

            if (curWeight <= max && curVal > bestVal) {
                bestSolution = (ArrayList<Integer>) currSolution.clone();
                bestWeight = curWeight;
                bestVal = curVal;
            }
        }
        System.out.println("Using Brute force the best feasible solution found: Value "
                + bestVal + " Weight " + bestWeight);
        for (Integer integer : bestSolution) {
            System.out.print(integer + " ");
        }
    }

    public static void greedySolution(List<Item> items, int max)
    {

    }

    public static void dynamicSolution(List<Item> items, int max)
    {

    }

    public static void branchAndBoundSolution(List<Item> items, int max)
    {

    }
}
