package drrsps.drrsps;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Scanner;
/**
 *
 * @author mdfuadanan
 * @id 251014032
 */
public class DRRSPS {
    private static final double INF = 1.0e18;

    static class Location {
        String name;
        String category;
        int priority;

        Location(String name, String category, int priority) {
            this.name = name;
            this.category = category;
            this.priority = priority;
        }
    }

    static class Road {
        int to;
        double distance;
        double time;
        double cost;
        double risk;
        boolean damaged;

        Road(int to, double distance, double time, double cost, double risk, boolean damaged) {
            this.to = to;
            this.distance = distance;
            this.time = time;
            this.cost = cost;
            this.risk = risk;
            this.damaged = damaged;
        }

        double weight(String metric) {
            if ("time".equals(metric)) {
                return time;
            }
            if ("cost".equals(metric)) {
                return cost;
            }
            if ("risk".equals(metric)) {
                return risk;
            }
            return distance;
        }
    }

    static class ReliefPackage {
        String name;
        int weight;
        int quantity;
        int priorityScore;

        ReliefPackage(String name, int weight, int quantity, int priorityScore) {
            this.name = name;
            this.weight = weight;
            this.quantity = quantity;
            this.priorityScore = priorityScore;
        }

        int benefit() {
            return quantity * priorityScore;
        }
    }

    static class AllocationResult {
        List<Integer> selected = new ArrayList<>();
        int totalWeight;
        int totalBenefit;
        int capacity;
    }

    static class ReliefNetwork {
        List<Location> locations = new ArrayList<>();
        List<List<Road>> adjacency = new ArrayList<>();

        int addLocation(String name, String category, int priority) {
            if (name.isBlank() || priority < 1 || findLocation(name) != -1) {
                return -1;
            }
            locations.add(new Location(name, category, priority));
            adjacency.add(new ArrayList<>());
            return locations.size() - 1;
        }

        boolean addRoad(String source, String destination, double distance, double time,
                        double cost, double risk, boolean damaged) {
            int from = findLocation(source);
            int to = findLocation(destination);
            if (from == -1 || to == -1 || from == to) {
                return false;
            }
            if (distance < 0 || time < 0 || cost < 0 || risk < 0) {
                return false;
            }
            adjacency.get(from).add(new Road(to, distance, time, cost, risk, damaged));
            adjacency.get(to).add(new Road(from, distance, time, cost, risk, damaged));
            return true;
        }

        int findLocation(String name) {
            for (int i = 0; i < locations.size(); i++) {
                if (locations.get(i).name.equals(name)) {
                    return i;
                }
            }
            return -1;
        }

        List<Integer> bfs(int start) {
            boolean[] visited = new boolean[locations.size()];
            Queue<Integer> queue = new ArrayDeque<>();
            List<Integer> order = new ArrayList<>();

            visited[start] = true;
            queue.add(start);
            while (!queue.isEmpty()) {
                int current = queue.remove();
                order.add(current);
                for (Road road : adjacency.get(current)) {
                    if (!road.damaged && !visited[road.to]) {
                        visited[road.to] = true;
                        queue.add(road.to);
                    }
                }
            }
            return order;
        }

        List<Integer> dfs(int start) {
            boolean[] visited = new boolean[locations.size()];
            ArrayDeque<Integer> stack = new ArrayDeque<>();
            List<Integer> order = new ArrayList<>();

            visited[start] = true;
            stack.push(start);
            while (!stack.isEmpty()) {
                int current = stack.pop();
                order.add(current);
                List<Road> roads = adjacency.get(current);
                for (int i = roads.size() - 1; i >= 0; i--) {
                    Road road = roads.get(i);
                    if (!road.damaged && !visited[road.to]) {
                        visited[road.to] = true;
                        stack.push(road.to);
                    }
                }
            }
            return order;
        }

        boolean routeExists(int start, int goal) {
            return bfs(start).contains(goal);
        }

        List<List<Integer>> bfsConnectivityGroups() {
            boolean[] visited = new boolean[locations.size()];
            List<List<Integer>> groups = new ArrayList<>();
            for (int i = 0; i < locations.size(); i++) {
                if (visited[i]) {
                    continue;
                }
                Queue<Integer> queue = new ArrayDeque<>();
                List<Integer> group = new ArrayList<>();
                visited[i] = true;
                queue.add(i);
                while (!queue.isEmpty()) {
                    int current = queue.remove();
                    group.add(current);
                    for (Road road : adjacency.get(current)) {
                        if (!road.damaged && !visited[road.to]) {
                            visited[road.to] = true;
                            queue.add(road.to);
                        }
                    }
                }
                groups.add(group);
            }
            return groups;
        }

        List<Integer> greedyRoute(int start, int goal, String metric) {
            boolean[] visited = new boolean[locations.size()];
            List<Integer> path = new ArrayList<>();
            int current = start;

            while (path.size() < locations.size()) {
                path.add(current);
                if (current == goal) {
                    return path;
                }
                visited[current] = true;

                Road best = null;
                for (Road road : adjacency.get(current)) {
                    if (road.damaged || visited[road.to]) {
                        continue;
                    }
                    if (road.to == goal) {
                        best = road;
                        break;
                    }
                    if (best == null || road.weight(metric) < best.weight(metric)) {
                        best = road;
                    }
                }

                if (best == null) {
                    return new ArrayList<>();
                }
                current = best.to;
            }
            return new ArrayList<>();
        }

        double routeWeight(List<Integer> path, String metric) {
            double total = 0.0;
            for (int i = 0; i < path.size() - 1; i++) {
                int from = path.get(i);
                int to = path.get(i + 1);
                for (Road road : adjacency.get(from)) {
                    if (road.to == to && !road.damaged) {
                        total += road.weight(metric);
                        break;
                    }
                }
            }
            return total;
        }

        int usableRoadCount(int locationIndex) {
            int count = 0;
            for (Road road : adjacency.get(locationIndex)) {
                if (!road.damaged) {
                    count++;
                }
            }
            return count;
        }
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            switch (args[0]) {
                case "--demo" -> runDemo();
                case "--self-test" -> {
                    if (!runSelfTest()) {
                        System.exit(1);
                    }
                }
                case "--performance" -> runPerformanceExperiment();
                default -> {
                    System.out.println("Unknown option: " + args[0]);
                    System.out.println("Use --demo, --self-test, --performance, or no option for menu mode.");
                    System.exit(1);
                }
            }
            return;
        }
        runMenu();
    }

    static ReliefNetwork buildSampleNetwork() {
        ReliefNetwork network = new ReliefNetwork();
        network.addLocation("Central Relief Center", "relief center", 5);
        network.addLocation("North Shelter", "shelter", 4);
        network.addLocation("East Hospital", "hospital", 5);
        network.addLocation("Riverbank Community", "affected community", 5);
        network.addLocation("Hill Tract Village", "affected community", 4);
        network.addLocation("South Warehouse", "warehouse", 3);
        network.addLocation("Old Bridge Camp", "temporary camp", 3);

        network.addRoad("Central Relief Center", "North Shelter", 7, 12, 80, 2, false);
        network.addRoad("Central Relief Center", "East Hospital", 10, 15, 120, 1, false);
        network.addRoad("North Shelter", "Riverbank Community", 5, 8, 70, 4, false);
        network.addRoad("East Hospital", "Riverbank Community", 3, 5, 50, 2, false);
        network.addRoad("East Hospital", "South Warehouse", 8, 10, 60, 1, false);
        network.addRoad("South Warehouse", "Hill Tract Village", 12, 20, 140, 5, false);
        network.addRoad("Riverbank Community", "Hill Tract Village", 9, 16, 100, 3, false);
        network.addRoad("North Shelter", "Old Bridge Camp", 6, 14, 40, 7, true);
        network.addRoad("Old Bridge Camp", "Hill Tract Village", 4, 9, 30, 8, true);
        return network;
    }

    static List<ReliefPackage> samplePackages() {
        return Arrays.asList(
            new ReliefPackage("Emergency Food Kits", 12, 80, 5),
            new ReliefPackage("Drinking Water Boxes", 18, 120, 5),
            new ReliefPackage("First Aid Packs", 6, 40, 4),
            new ReliefPackage("Temporary Shelter Tents", 25, 20, 5),
            new ReliefPackage("Blankets", 10, 60, 3),
            new ReliefPackage("Sanitation Kits", 9, 35, 4)
        );
    }

    static AllocationResult allocatePackages(List<ReliefPackage> packages, int capacity) {
        int[][] dp = new int[packages.size() + 1][capacity + 1];
        for (int i = 1; i <= packages.size(); i++) {
            ReliefPackage item = packages.get(i - 1);
            for (int c = 0; c <= capacity; c++) {
                int withoutItem = dp[i - 1][c];
                int withItem = withoutItem;
                if (item.weight <= c) {
                    withItem = dp[i - 1][c - item.weight] + item.benefit();
                }
                dp[i][c] = Math.max(withoutItem, withItem);
            }
        }

        AllocationResult result = new AllocationResult();
        result.capacity = capacity;
        int remaining = capacity;
        for (int i = packages.size(); i >= 1; i--) {
            if (dp[i][remaining] != dp[i - 1][remaining]) {
                int index = i - 1;
                ReliefPackage item = packages.get(index);
                result.selected.add(0, index);
                result.totalWeight += item.weight;
                result.totalBenefit += item.benefit();
                remaining -= item.weight;
            }
        }
        return result;
    }

    static void runMenu() {
        Scanner scanner = new Scanner(System.in);
        ReliefNetwork network = buildSampleNetwork();
        List<ReliefPackage> packages = samplePackages();
        System.out.println("Disaster Relief Route and Supply Planning System (Java)");
        System.out.println("Sample disaster scenario loaded.");

        while (true) {
            printMenu();
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();
            if ("0".equals(choice)) {
                System.out.println("Exiting. Stay prepared and deliver relief safely.");
                return;
            }
            handleMenuChoice(scanner, network, packages, choice);
        }
    }

    static void printMenu() {
        System.out.println("\nMenu");
        System.out.println("1. Show relief network");
        System.out.println("2. Check whether a route exists");
        System.out.println("3. Traverse reachable locations");
        System.out.println("4. Analyze network connectivity using BFS");
        System.out.println("5. Find route using Greedy Algorithm");
        System.out.println("6. Allocate relief packages using 0/1 Knapsack");
        System.out.println("7. Run performance comparison");
        System.out.println("0. Exit");
    }

    static void handleMenuChoice(Scanner scanner, ReliefNetwork network,
                                 List<ReliefPackage> packages, String choice) {
        switch (choice) {
            case "1" -> showNetwork(network);
            case "2" -> routeCheckInput(scanner, network);
            case "3" -> traversalInput(scanner, network);
            case "4" -> printConnectivity(network);
            case "5" -> greedyRouteInput(scanner, network);
            case "6" -> {
                System.out.print("Vehicle capacity: ");
                int capacity = Integer.parseInt(scanner.nextLine().trim());
                printAllocation(packages, capacity);
            }
            case "7" -> runPerformanceExperiment();
            default -> System.out.println("Invalid option.");
        }
    }

    static void showNetwork(ReliefNetwork network) {
        System.out.println("\nLocations");
        for (int i = 0; i < network.locations.size(); i++) {
            Location location = network.locations.get(i);
            System.out.printf("- %s | %s | priority %d | usable roads %d%n",
                    location.name, location.category, location.priority, network.usableRoadCount(i));
        }
        System.out.println("\nRoads");
        for (int i = 0; i < network.locations.size(); i++) {
            for (Road road : network.adjacency.get(i)) {
                if (i < road.to) {
                    System.out.printf("- %s <-> %s: distance=%.1f, time=%.1f, cost=%.1f, risk=%.1f, %s%n",
                            network.locations.get(i).name, network.locations.get(road.to).name,
                            road.distance, road.time, road.cost, road.risk,
                            road.damaged ? "damaged" : "usable");
                }
            }
        }
    }

    static void routeCheckInput(Scanner scanner, ReliefNetwork network) {
        System.out.print("Start location: ");
        int start = network.findLocation(scanner.nextLine().trim());
        System.out.print("Target location: ");
        int target = network.findLocation(scanner.nextLine().trim());
        if (start == -1 || target == -1) {
            System.out.println("Unknown location.");
            return;
        }
        System.out.println(network.routeExists(start, target)
                ? "Route exists through usable roads."
                : "No usable route exists.");
    }

    static void traversalInput(Scanner scanner, ReliefNetwork network) {
        System.out.print("Start location: ");
        int start = network.findLocation(scanner.nextLine().trim());
        System.out.print("Traversal method (bfs/dfs): ");
        String method = scanner.nextLine().trim().toLowerCase(Locale.ROOT);
        if (start == -1) {
            System.out.println("Unknown location.");
            return;
        }
        List<Integer> order = "dfs".equals(method) ? network.dfs(start) : network.bfs(start);
        System.out.println("Reachable locations: " + names(network, order));
    }

    static void greedyRouteInput(Scanner scanner, ReliefNetwork network) {
        System.out.print("Start location: ");
        int start = network.findLocation(scanner.nextLine().trim());
        System.out.print("Target location: ");
        int target = network.findLocation(scanner.nextLine().trim());
        System.out.print("Metric (distance/time/cost/risk): ");
        String metric = cleanMetric(scanner.nextLine().trim().toLowerCase(Locale.ROOT));
        if (start == -1 || target == -1) {
            System.out.println("Unknown location.");
            return;
        }
        List<Integer> path = network.greedyRoute(start, target, metric);
        if (path.isEmpty()) {
            System.out.println("No usable greedy route exists.");
            return;
        }
        System.out.println("Greedy route by " + metric + ": " + names(network, path));
        System.out.printf("Total %s: %.1f%n", metric, network.routeWeight(path, metric));
    }

    static void printConnectivity(ReliefNetwork network) {
        List<List<Integer>> groups = network.bfsConnectivityGroups();
        System.out.println(groups.size() == 1 ? "Network is connected." : "Network is not fully connected.");
        for (int i = 0; i < groups.size(); i++) {
            System.out.println("Group " + (i + 1) + ": " + names(network, groups.get(i)));
        }
    }

    static void printAllocation(List<ReliefPackage> packages, int capacity) {
        AllocationResult result = allocatePackages(packages, capacity);
        System.out.println("Selected packages for capacity " + capacity + ":");
        for (int index : result.selected) {
            ReliefPackage item = packages.get(index);
            System.out.printf("- %s: weight=%d, quantity=%d, priority score=%d, benefit=%d%n",
                    item.name, item.weight, item.quantity, item.priorityScore, item.benefit());
        }
        System.out.println("Total weight: " + result.totalWeight);
        System.out.println("Total benefit: " + result.totalBenefit);
        System.out.println("Unused capacity: " + (result.capacity - result.totalWeight));
    }

    static void runDemo() {
        ReliefNetwork network = buildSampleNetwork();
        List<ReliefPackage> packages = samplePackages();
        int start = network.findLocation("Central Relief Center");
        int riverbank = network.findLocation("Riverbank Community");
        int hill = network.findLocation("Hill Tract Village");

        System.out.println("Disaster Relief Route and Supply Planning System (Java)");
        showNetwork(network);
        System.out.println("\nRoute check:");
        System.out.println("Central Relief Center -> Riverbank Community: "
                + (network.routeExists(start, riverbank) ? "Route exists through usable roads" : "No usable route exists"));
        System.out.println("\nBFS traversal from Central Relief Center:");
        System.out.println(names(network, network.bfs(start)));
        System.out.println("\nConnectivity using BFS:");
        printConnectivity(network);
        System.out.println("\nGreedy route by distance:");
        List<Integer> path = network.greedyRoute(start, hill, "distance");
        System.out.println(names(network, path));
        System.out.printf("Total distance: %.1f%n", network.routeWeight(path, "distance"));
        System.out.println("\nSupply allocation for vehicle capacity 45:");
        printAllocation(packages, 45);
        System.out.println();
        runPerformanceExperiment();
    }

    static boolean runSelfTest() {
        ReliefNetwork network = buildSampleNetwork();
        List<ReliefPackage> packages = samplePackages();
        int start = network.findLocation("Central Relief Center");
        int riverbank = network.findLocation("Riverbank Community");
        int oldBridge = network.findLocation("Old Bridge Camp");
        int hill = network.findLocation("Hill Tract Village");
        int passed = 0;
        int total = 0;

        total++; passed += check("route exists to Riverbank Community", network.routeExists(start, 
riverbank)) ? 1 : 0;
        total++; passed += check("damaged roads are ignored by BFS route check", 
!network.routeExists(start, oldBridge)) ? 1 : 0;
        total++; passed += check("BFS connectivity check finds two groups", 
network.bfsConnectivityGroups().size() == 2) ? 1 : 0;
        List<Integer> path = network.greedyRoute(start, hill, "distance");
        total++; passed += check("greedy route reaches target by distance",
                path.size() == 4 && "North Shelter".equals(network.locations.get(path.get(1)).name)
                        && network.routeWeight(path, "distance") == 21.0) ? 1 : 0;
        AllocationResult allocation = allocatePackages(packages, 45);
        total++; passed += check("0/1 knapsack respects capacity", allocation.totalWeight <= 45) ? 1 : 
0;
        total++; passed += check("0/1 knapsack sample benefit is correct",
                allocation.totalWeight == 45 && allocation.totalBenefit == 1300) ? 1 : 0;

        System.out.printf("%nSelf-test result: %d/%d checks passed.%n", passed, total);
        return passed == total;
    }

    static boolean check(String label, boolean condition) {
        System.out.println((condition ? "PASS: " : "FAIL: ") + label);
        return condition;
    }

    static void runPerformanceExperiment() {
        int[] sizes = {10, 25, 50, 100};
        System.out.println("Performance comparison");
        System.out.println("Nodes | Edges | BFS ms | DFS ms | Greedy ms | Knapsack ms");
        for (int size : sizes) {
            ReliefNetwork network = generatedNetwork(size);
            List<ReliefPackage> packages = generatedPackages(Math.min(size, 50));
            int repetitions = 1000;
            int knapsackRepetitions = 200;

            long startTime = System.nanoTime();
            for (int i = 0; i < repetitions; i++) {
                network.bfs(0);
            }
            double bfsMs = elapsedMs(startTime) / repetitions;

            startTime = System.nanoTime();
            for (int i = 0; i < repetitions; i++) {
                network.dfs(0);
            }
            double dfsMs = elapsedMs(startTime) / repetitions;

            startTime = System.nanoTime();
            for (int i = 0; i < repetitions; i++) {
                network.greedyRoute(0, size - 1, "distance");
            }
            double greedyMs = elapsedMs(startTime) / repetitions;

            startTime = System.nanoTime();
            for (int i = 0; i < knapsackRepetitions; i++) {
                allocatePackages(packages, size * 2);
            }
            double knapsackMs = elapsedMs(startTime) / knapsackRepetitions;

            System.out.printf(Locale.US, "%5d | %5d | %6.4f | %6.4f | %9.4f | %11.4f%n",
                    size, roadCount(network), bfsMs, dfsMs, greedyMs, knapsackMs);
        }
    }

    static ReliefNetwork generatedNetwork(int size) {
        ReliefNetwork network = new ReliefNetwork();
        for (int i = 0; i < size; i++) {
            network.addLocation("Location " + i, "generated community", (i % 5) + 1);
        }
        for (int i = 0; i < size - 1; i++) {
            addGeneratedRoad(network, i, i + 1);
        }
        for (int i = 0; i < size; i++) {
            addGeneratedRoad(network, i, (i * 7 + 3) % size);
        }
        return network;
    }

    static void addGeneratedRoad(ReliefNetwork network, int source, int destination) {
        if (source == destination) {
            return;
        }
        double distance = ((source + 3) * (destination + 5)) % 29 + 2;
        network.addRoad("Location " + source, "Location " + destination,
                distance, distance + (source % 7) + 1, distance * (8 + destination % 6),
                (source + destination) % 9 + 1, false);
    }

    static List<ReliefPackage> generatedPackages(int count) {
        List<ReliefPackage> packages = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            packages.add(new ReliefPackage("Package " + i, (i % 9) + 1, i + 3, (i % 5) + 1));
        }
        return packages;
    }

    static int roadCount(ReliefNetwork network) {
        int total = 0;
        for (List<Road> roads : network.adjacency) {
            total += roads.size();
        }
        return total / 2;
    }

    static String cleanMetric(String metric) {
        if (metric.equals("time") || metric.equals("cost") || metric.equals("risk")) {
            return metric;
        }
        return "distance";
    }

    static String names(ReliefNetwork network, List<Integer> indexes) {
        List<String> names = new ArrayList<>();
        for (int index : indexes) {
            names.add(network.locations.get(index).name);
        }
        return String.join(" -> ", names);
    }

    static double elapsedMs(long startTime) {
        return (System.nanoTime() - startTime) / 1_000_000.0;
    }
}
