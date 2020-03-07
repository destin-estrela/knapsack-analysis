import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.*;

public class Main {

    /**
     * Used to represent an item from the knapsack.
     */
    static class Item {
        int weight;
        int value;
        int index;
        double ratio;

        protected Item(int index, int weight, int value)
        {
            this.weight = weight;
            this.value = value;
            this.index = index;
            ratio = (double) value / weight;
        }
    }

    public static void main(String[] args)
    {
        assert (args.length > 0);
        boolean doExhaustive = false;
        boolean doGreedy = false;
        boolean doDynamic = false;
        boolean doBranch = false;
        ArrayList<String> files = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-e")) {
                doExhaustive = true;
            } else if (arg.equals("-g")) {
                doGreedy = true;
            } else if (arg.equals("-d")) {
                doDynamic = true;
            } else if (arg.equals("-b")) {
                doBranch = true;
            } else {
                files.add(arg);
            }
        }

        String path = "C:\\Users\\Destin Gigabyte\\Desktop\\ALG_FINAL\\knapsack-analysis\\implementation\\src\\";
        //String path = "C:\\Users\\desti\\OneDrive - California Polytechnic State University\\Winter 2020\\CPE 349\\Final project\\knapsack-analysis\\implementation\\src\\";

        for (String fileName : files) {
            ArrayList<Item> items = new ArrayList<>();
            int maxCapacity = -1;
            System.out.println("---------------------------------------------\nFOR FILE " + fileName + "\n");
            // read from file into list of items and get max weight
            try {
                List<String> allLines = Files.readAllLines(Paths.get(path + fileName));
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

            if (doExhaustive && fileName.equals("easy20.txt"))
            {
                long startTime = System.currentTimeMillis();
                exhaustiveSolution(items, maxCapacity);
                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                System.out.println("Execution time in milliseconds: " + timeElapsed + "\n");
            }
            if (doGreedy)
            {
                long startTime = System.currentTimeMillis();
                greedySolution(items, maxCapacity);
                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                System.out.println("Execution time in milliseconds: " + timeElapsed + "\n");

            }
            if (doDynamic)
            {
                long startTime = System.currentTimeMillis();
                dynamicSolution(items, maxCapacity);
                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                System.out.println("Execution time in milliseconds: " + timeElapsed + "\n");
            }
            if (doBranch)
            {
                long startTime = System.currentTimeMillis();
                branchAndBoundSolution(items, maxCapacity);
                long endTime = System.currentTimeMillis();
                long timeElapsed = endTime - startTime;
                System.out.println("Execution time in milliseconds: " + timeElapsed + "\n");
            }
            System.out.println();
            System.out.println();
        }

    }

    /**
     * Find all possible subsets.
     * Keep a running counter of the maximum solution so far, and print the best one.
     */
    public static void exhaustiveSolution(List<Item> items, int maxWeight)
    {
        items.sort(Comparator.comparingInt(item -> item.index));
        int bestVal = Integer.MIN_VALUE;
        int bestWeight = -1;
        ArrayList<Integer> bestSolution = null;

        ArrayList<Integer> currSolution = new ArrayList<>();
        Item currItem;
        long numitems = items.size();
        long numSubSets = 1 << numitems;

        // iterate through all possible subsets
        for (long i = 0; i < numSubSets; i++) {
            currSolution.clear();
            int curVal = 0;
            int curWeight = 0;

            // Generate result from subset
            for (int j = 0; j < numitems; j++) {
                if ((i & (1 << j)) > 0) {
                    currItem = items.get(j);
                    curVal += currItem.value;
                    curWeight += currItem.weight;
                    currSolution.add(currItem.index);
                }
            }

            if (curWeight <= maxWeight && curVal > bestVal) {
                bestSolution = new ArrayList<>();
                bestSolution.addAll(currSolution);
                bestWeight = curWeight;
                bestVal = curVal;
            }
        }

        printResults("Brute Force", bestSolution, bestVal, bestWeight);
    }

    /**
     * Using criterion: highest value/weight ratio first
     * Sort list of items by criterion and add consecutively to knapsack until full.
     */
    public static void greedySolution(List<Item> items, int maxWeight)
    {
        items.sort(Comparator.comparingDouble((Item item) -> item.ratio).reversed());

        ArrayList<Integer> currSolution = new ArrayList<>();
        int curWeight = 0;
        int curVal = 0;

        for (int i = 0; i < items.size(); i++) {
            if (curWeight + items.get(i).weight <= maxWeight) {
                curWeight += items.get(i).weight;
                curVal += items.get(i).value;
                currSolution.add(items.get(i).index);
            }
        }

        currSolution.sort(Integer::compareTo);
        printResults("Greedy", currSolution, curVal, curWeight);
    }

    /**
     * MaxVal(i, w is the maximum value obtainable by picking up
     * up to i items with w capacity.
     * <p>
     * Using recurrence relation:
     * MaxVal(i, w) = Max( MaxVal(i-1, w), MaxVal(i-1, w - wi) + vi)
     * With base case MaxVal(0, 0) = 0
     * <p>
     * Using backtrack method: If MaxVal(i, w) equal to MaxVal(i-1, w)
     * then item was not picked up. Else item was chosen, i = i-1, w = w - (weight of i)
     */
    public static void dynamicSolution(List<Item> items, int maxWeight)
    {

        // initiate table with base case (implied with initialization)
        int[][] table = new int[items.size() + 1][maxWeight + 1];

        // fill in table using recurrence relation
        for (int i = 1; i < table.length; i++) {
            Item item = items.get(i - 1);
            for (int j = 1; j <= maxWeight; j++) {

                table[i][j] = Integer.max(table[i - 1][j],
                        j - item.weight >= 0 ? table[i - 1][j - item.weight] + item.value : 0);
            }
        }

        // backtrack using backtracking method
        int finalVal = 0;
        int finalWeight = 0;
        LinkedList<Integer> finalSolution = new LinkedList<>();
        int j = maxWeight;
        for (int i = items.size(); i > 0; i--) {
            Item item = items.get(i - 1);

            if (table[i][j] != table[i - 1][j]) {
                if (j - item.weight < 0) {
                    break;
                }
                finalWeight += item.weight;
                finalVal += item.value;
                j = j - item.weight;
                finalSolution.add(0, item.index);
            }
        }

        finalSolution.sort(Integer::compareTo);
        printResults("Dynamic Programming", finalSolution, finalVal, finalWeight);
    }

    /**
     * Traverse state space tree, incrementally discovering better solutions.
     * Tree has N levels for n items. At each level: either take the item or do not.
     * <p>
     * Do not further explore subtree if its maximum bound (see calculateBound below())
     * Is lower than the best solution found so far. Subtree can not possibly yield a better solution.
     * <p>
     * Once tree has been fully pruned, the current best solution will be the best solution.
     */
    static Entry finalRes = null;

    public static void branchAndBoundSolution(List<Item> items, int maxWeight)
    {

        // sort by highest ratio value/weight first
        items.sort(Comparator.comparingDouble((Item item) -> item.ratio).reversed());

        PriorityQueue<Entry> queue = new PriorityQueue<>(items.size());
        queue.add(new Entry(0, 0, 0, new ArrayList<>(), -1));

        // add shutdown interrupt to show best solution found so far
        Thread shutdownHook = new Thread() {
            public void run()
            {
                finalRes.currSolution.sort(Integer::compareTo);
                printResults("INTERRUPTED Branch and Bound", finalRes.currSolution, finalRes.curVal,
                        finalRes.currWeight);
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        int maxSoFar = 0;
        while (!queue.isEmpty()) {
            Entry entry = queue.remove();

            if (entry.currIndex == items.size() - 1)
                continue;

            entry.currIndex += 1;

            // Assume you take the item, update entry with attributes of next item
            Item nextItem = items.get(entry.currIndex);
            Entry newEntry = new Entry(0, entry.currWeight + nextItem.weight,
                    entry.curVal + nextItem.value,
                    new ArrayList<>(), entry.currIndex);

            newEntry.currSolution.addAll(entry.currSolution);
            newEntry.currSolution.add(nextItem.index);

            // better solution found
            if (newEntry.curVal > maxSoFar && newEntry.currWeight <= maxWeight) {
                maxSoFar = newEntry.curVal;
                finalRes = newEntry;
            }

            // Add entry to queue if its bound is better than the best solution found so far
            newEntry.key = calculateBound(items, newEntry, maxWeight);
            if (newEntry.key > maxSoFar) {
                queue.add(newEntry);
            }

            // Assume you do NOT take the item
            entry.key = calculateBound(items, entry, maxWeight);

            // No changes needed to the entry besides its index (changed above)
            if (entry.key > maxSoFar) {
                queue.add(entry);
            }

        }

        Runtime.getRuntime().removeShutdownHook(shutdownHook);
        finalRes.currSolution.sort(Integer::compareTo);
        printResults("Branch and Bound", finalRes.currSolution, finalRes.curVal, finalRes.currWeight);

    }

    /**
     * In order to provide a much tighter bound than what was taught in class:
     * <p>
     * From the jth item to the last item, use the greedy approach: highest item/value
     * ratio first, until the knapsack is nearly full. Then take a fraction of the last item.
     * This upper bound is will be greater than  the exact solution.
     * <p>
     * Credit goes to
     * https://www.geeksforgeeks.org/implementation-of-0-1-knapsack-using-branch-and-bound/
     * For insight on providing a much tighter bound.
     * <p>
     */
    static int calculateBound(List<Item> items, Entry entry, int maxWeight)
    {
        // capacity exceeded, immediately prune
        if (entry.currWeight > maxWeight) {
            return -1;
        }

        int bound = entry.curVal;
        int totalWeight = entry.currWeight;
        int j = entry.currIndex + 1;

        // use greedy approach from this item to the last item that will fit
        while (j < items.size() && totalWeight + items.get(j).weight <= maxWeight) {
            bound += items.get(j).value;
            totalWeight += items.get(j).weight;
            j++;
        }

        // take a fract of the last item (using the upper bound discussed in class)
        if (j < items.size()) {
            bound += (maxWeight - totalWeight) * items.get(j).ratio;
        }

        return bound;

    }


    /**
     * Display the result 0-1 knapsack solution.
     */
    static void printResults(String type, List<Integer> res, int val, int weight)
    {
        System.out.println("Using " + type + " approach the best feasible solution found " + val
                + " Weight " + weight);
        for (Integer integer : res) {
            System.out.print(integer + " ");
        }
        System.out.println();
    }


    /**
     * Represents a node in the state-space tree for the branch and bound solution.
     * <p>
     * double key: represents the upper bound for the partial solution.
     * key becomes a solution when currIndex == items.size() - 1
     */
    public static class Entry implements Comparable<Entry> {
        double key;
        int currWeight;
        int curVal;
        ArrayList<Integer> currSolution;
        int currIndex;

        public Entry(double key, int currWeight, int curVal, ArrayList<Integer> currSolution, int currIndex)
        {
            this.key = key;
            this.currWeight = currWeight;
            this.curVal = curVal;
            this.currSolution = currSolution;
            this.currIndex = currIndex;
        }

        @Override
        public int compareTo(Entry other)
        {
            if (this.key < other.key) {
                return 1;
            } else if (this.key == other.key) {
                return 0;
            }
            return -1;
        }
    }
}
