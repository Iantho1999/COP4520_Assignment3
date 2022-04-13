import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


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
    private Node head;

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

class Servant extends Thread
{
    LazyList list;

    // Constructor
    Servant(LazyList l)
    {
        list = l;
    }

    @Override
    public void run()
    {
        while (Program.addCounter.get() < Program.NUM_PRESENTS || Program.removeCounter.get() < Program.NUM_PRESENTS)
        {
            // Alternate between adding presents to the chain and writing thank you notes
            list.add(Program.unsortedPresents.get(Math.min(Program.addCounter.getAndIncrement(), Program.NUM_PRESENTS - 1)));
            list.remove(Program.unsortedPresents.get(Math.min(Program.removeCounter.getAndIncrement(), Program.NUM_PRESENTS - 1)));
        }
    }
}

public class Program
{
    public static final int NUM_PRESENTS = 500000;
    public static final int NUM_SERVANTS = 4;
    public static ArrayList<Integer> unsortedPresents = new ArrayList<>();
    public static AtomicInteger addCounter = new AtomicInteger(0);
    public static AtomicInteger removeCounter = new AtomicInteger(0);

    public static void main(String[] args)
    {
        int i;
        LazyList presentChain = new LazyList();


        // Set up the unsorted present bag
        for (i = 1; i <= NUM_PRESENTS; i++)
        {
            unsortedPresents.add(i);
        }

        Collections.shuffle(unsortedPresents);

        // Create servants as threads
        Servant[] servants = new Servant[NUM_SERVANTS];

        for (i = 0; i < NUM_SERVANTS; i++)
        {
            Servant s = new Servant(presentChain);
            servants[i] = s;
            s.start();
        }

        // Wait for threads to complete
        try
        {
            for (i = 0; i < NUM_SERVANTS; i++)
            {
                servants[i].join();
            }
        }
        catch(InterruptedException e)
        {

        }

        System.out.println("The servants have completed their task.");
        
    }
}