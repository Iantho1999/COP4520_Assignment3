import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.ThreadLocalRandom;

class Node
{
    public int key;
    public Node next;
    public ReentrantLock lock = new ReentrantLock();
    public boolean marked = false;

    public Node(int item)
    {
        key = item;
    }
}

class LazyList
{
    Node head;

    // Constructor
    public LazyList()
    {
        head = new Node(Integer.MIN_VALUE);
        head.next = new Node(Integer.MAX_VALUE);
    }

    public boolean validate(Node pred, Node curr)
    {
        return (!pred.marked && !curr.marked && pred.next == curr);
    }

    public boolean contains(int key)
    {
        Node curr = head;

        while (curr.key < key)
        {
            curr = curr.next;
        }

        return (curr.key == key && !curr.marked);
    }

    public boolean add(int key)
    {
        while (true)
        {
            Node pred = head;
            Node curr = head.next;
            

            while (curr.key < key)
            {
                pred = curr;
                curr = curr.next;
            }
            

            pred.lock.lock();
            try
            {
                curr.lock.lock();
                try
                {
                    if (validate(pred, curr))
                    {
                        if (curr.key == key)
                        {
                            return false;
                        }
                        else
                        {
                            Node node = new Node(key);
                            node.next = curr;
                            pred.next = node;
                            return true;
                        }
                    }
                }
                finally
                {
                    curr.lock.unlock();
                }
            }
            finally
            {
                pred.lock.unlock();
            }
        }
    }

    public boolean remove(int key)
    {
        while (true)
        {
            Node pred = head;
            Node curr = head.next;
            
            while (curr.key < key)
            {
                pred = curr;
                curr = curr.next;
            }

            pred.lock.lock();

            try
            {
                curr.lock.lock();
                try
                {
                    if (validate(pred, curr))
                    {
                        if (curr.key != key)
                        {
                            return false;
                        }
                        else
                        {
                            curr.marked = true;
                            pred.next = curr.next;
                            return true;
                        }
                    }
                    
                }
                finally
                {
                    curr.lock.unlock();
                }
            }
            finally
            {
                pred.lock.unlock();
            }
        }
    }
}

class Sensor extends Thread
{
    int minutesPassed;
    LazyList list;

    // Constructor
    Sensor(LazyList l)
    {
        list = l;
        minutesPassed = 0;
    }

    public void calculateDifference(int lowerBound, int upperBound, int minutesPassed)
    {
        int tempDifference;

        tempDifference = Math.abs(upperBound - lowerBound);

        if (tempDifference > Program.largestDifference)
        {
            Program.largestDifference = tempDifference;
            Program.differenceEndInterval = minutesPassed;
        }
    }

    @Override
    public void run()
    {
        int currentTemp = 0, lowerBound = 0;

        for (minutesPassed = 0; minutesPassed < 60; minutesPassed++)
        {
            // Record the current temperature by generating a random value between -100F and 70F
            currentTemp = ThreadLocalRandom.current().nextInt(-100, 70);

            // Checks for difference in the 10 minute interval
            if (minutesPassed % 10 == 0)
            {
                if (minutesPassed > 0)
                {
                    calculateDifference(lowerBound, currentTemp, minutesPassed);
                }

                lowerBound = currentTemp;
            }

            // Add the value to the list
            list.add(currentTemp);
        }

        // Checks for the difference in the 50-60 minute interval
        calculateDifference(lowerBound, currentTemp, 60);
    }
}

public class Program
{
    public static final int NUM_SENSORS = 8;
    public static int largestDifference = 0;
    public static int differenceEndInterval = 0;

    public static void main(String[] args)
    {
        int i;
        LazyList recordedTemperatures = new LazyList();
        Random r = new Random();
        Node temp;
        ArrayList<Integer> temperatureArray = new ArrayList<>();
        List<Integer> topFive;
        long start, end;

        // Create sensors as threads
        Sensor[] sensors = new Sensor[NUM_SENSORS];

        // Get the starting time before the threads run
        start = System.currentTimeMillis();

        // Start each thread individually
        for (i = 0; i < NUM_SENSORS; i++)
        {
            Sensor s = new Sensor(recordedTemperatures);
            sensors[i] = s;
            s.start();
        }

        // Wait for threads to complete
        try
        {
            for (i = 0; i < NUM_SENSORS; i++)
            {
                sensors[i].join();
            }
        }
        catch(InterruptedException e)
        {

        }

        // Get the ending time after the threads have run, and print the time taken
        end = System.currentTimeMillis();
        System.out.println("Threads took " + (end - start) + "ms");
        
        System.out.println("--Report for Hour--");

        // Get and print out the top 5 highest temperatures
        System.out.println("Top 5 Highest Temperatures:");

        temp = recordedTemperatures.head.next;

        while (temp.next != null)
        {
            temperatureArray.add(temp.key);
            temp = temp.next;
        }

        // Get a list of the top five highest temperatures by getting a sublist of the array, and print its components
        topFive = temperatureArray.subList(temperatureArray.size() - 5, temperatureArray.size());

        for (i = 1; i <= 5; i++)
        {
            System.out.println(i + ". " + topFive.get(5 - i) + "F");
        }

        temp = recordedTemperatures.head.next;   


        // Get and print out the top 5 lowest temperatures
        System.out.println("Top 5 Lowest Temperatures: ");
        for (i = 1; i <= 5; i++)
        {
            System.out.println(i + ". " + temp.key + "F");
            temp = temp.next;
        }

        // Print out the interval with the greatest temperature difference
        System.out.println("Interval with largest temperature difference: Between " + (differenceEndInterval - 10) + " and " + differenceEndInterval + " minutes.");
        
    }
}