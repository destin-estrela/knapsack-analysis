import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.*;

public class Main {

    static class Item {
        int weight;
        int value;
        int index;
        double ratio;

        protected Item(int index, int weight, int value) {
            this.weight = weight;
            this.value = value;
            this.index = index;
            ratio = (double)value / weight;
        }
    }

    public static void main(String[] args) {
        assert (args.length > 0);

        String fileString = "C:\\Users\\Destin Gigabyte\\Desktop\\ALG_FINAL\\knapsack-analysis\\implementation\\src\\"
                + args[0];

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

    public static void exhaustiveSolution(List<Item> items, int max) {
        items.sort(Comparator.comparingInt(item -> item.index));
        int bestVal = Integer.MIN_VALUE;
        int bestWeight = -1;
        ArrayList<Integer> bestSolution = null;

        ArrayList<Integer> currSolution = new ArrayList<>();
        int curVal = 0;
        int curWeight = 0;
        Item currItem;
        long numitems = items.size();
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
        System.out.println();

    }

    /**
     * Using criterion: highest value/weight ratio first
     *
     * @param items The list of items to choose from
     * @param max   the max weight capacity of the knapsack
     */
    public static void greedySolution(List<Item> items, int max) {
        items.sort(Comparator.comparingDouble(item -> (double) item.value / (double) item.weight));
        ArrayList<Integer> currSolution = new ArrayList<>();
        int curWeight = 0;
        int curVal = 0;

        for (int i = items.size() - 1; i >= 0; i--) {
            if (curWeight + items.get(i).weight <= max) {
                curWeight += items.get(i).weight;
                curVal += items.get(i).value;
                currSolution.add(items.get(i).index);
            }
        }

        currSolution.sort(Integer::compareTo);
        System.out.println("Using Greedy approach the best feasible solution found: Value "
                + curVal + " Weight " + curWeight);
        for (Integer integer : currSolution) {
            System.out.print(integer + " ");
        }
        System.out.println();

    }

    public static void dynamicSolution(List<Item> items, int max) {
        int[][] table = new int[items.size() + 1][max + 1];
        for (int i = 1; i < table.length; i++) {
            Item item = items.get(i - 1);
            for (int j = 1; j <= max; j++) {
                table[i][j] = Integer.max(table[i - 1][j],
                        j - item.weight >= 0 ? table[i - 1][j - item.weight] + item.value : 0);
            }
        }

        int finalVal = 0;
        int finalWeight = 0;
        LinkedList<Integer> finalSolution = new LinkedList<>();
        int j = max;
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

        System.out.println("Using Dynamic approach the best feasible solution found: Value "
                + finalVal + " Weight " + finalWeight);
        for (Integer integer : finalSolution) {
            System.out.print(integer + " ");
        }
        System.out.println();

    }

    /**
     * Use an upper bound of current value + weight available * (value/weight)
     * Initially attempted independently using explanation in slides, got very close to final answer,
     * then used
     * <p>
     * https://www.geeksforgeeks.org/implementation-of-0-1-knapsack-using-branch-and-bound/
     * <p>
     * To as a replacement for my faulty bounding function.
     *
     * @param items
     * @param max
     */
    public static void branchAndBoundSolution(List<Item> items, int max) {
        // sort by highest ratio first
        items.sort(Comparator.comparingDouble((Item item) -> item.ratio).reversed());

        int maxSoFar = 0;
        PriorityQueue<Entry> queue = new PriorityQueue<>(items.size());
        queue.add(new Entry(0,
                0, 0, new ArrayList<>(), -1));
        Entry finalRes = new Entry();

        while (!queue.isEmpty()) {
            Entry entry = queue.remove();

            if (entry.currIndex == items.size() - 1)
                continue;

            entry.currIndex += 1;

            // get bound with item (IE 1)
            Entry newEntry = new Entry(entry);
            Item nextItem = items.get(newEntry.currIndex);

            newEntry.currSolution = new ArrayList<>();
            newEntry.currSolution.addAll(entry.currSolution);
            newEntry.currSolution.add(nextItem.index);

            newEntry.curVal += nextItem.value;
            newEntry.currWeight += nextItem.weight;

            if (newEntry.curVal > maxSoFar && newEntry.currWeight <= max) {
                maxSoFar = newEntry.curVal;
                finalRes = newEntry;
            }

            newEntry.key = calculateBound(items, newEntry, max);
            if (newEntry.key > maxSoFar) {
                queue.add(newEntry);
            }

            // get bound without item (IE 0)
            entry.key = calculateBound(items, entry, max);

            if (entry.key > maxSoFar) {
                Entry nother1 = new Entry(entry);
                queue.add(nother1);
            }

        }

        finalRes.currSolution.sort(Integer::compareTo);
        System.out.println("Using Brute force the best feasible solution found: Value "
                + finalRes.curVal + " Weight " + finalRes.currWeight);
        for (Integer integer : finalRes.currSolution) {
            System.out.print(integer + " ");
        }
        System.out.println();

    }

    static int calculateBound(List<Item> items, Entry entry, int max) {
        if (entry.currWeight > max) {
            return -1;
        }

        int bound = entry.curVal;
        int totalWeight = entry.currWeight;
        int j = entry.currIndex + 1;

        while (j < items.size() && totalWeight + items.get(j).weight <= max) {
            bound += items.get(j).value;
            totalWeight += items.get(j).weight;
            j++;
        }

        if (j < items.size()) {
            bound += (max - totalWeight) * items.get(j).ratio;
        }

        return bound;

    }


    public static class Entry implements Comparable<Entry> {
        double key;
        int currWeight;
        int curVal;
        ArrayList<Integer> currSolution;
        int currIndex;

        public Entry(double key, int currWeight, int curVal, ArrayList<Integer> currSolution, int currIndex) {
            this.key = key;
            this.currWeight = currWeight;
            this.curVal = curVal;
            this.currSolution = currSolution;
            this.currIndex = currIndex;
        }

        public Entry(Entry o) {
            this.key = o.key;
            this.currWeight = o.currWeight;
            this.curVal = o.curVal;
            this.currSolution = o.currSolution;
            this.currIndex = o.currIndex;
        }

        public Entry() {
        }


        @Override
        public int compareTo(Entry other) {
            if (this.key < other.key) {
                return 1;
            } else if (this.key == other.key) {
                return 0;
            }
            return -1;
        }
    }
}
