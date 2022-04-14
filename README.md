# COP4520_Assignment3

## Compilation Instructions
Go to the respective folder (Part1 or Part2)

Enter the following:
```
javac Program.java
java Program
```

## Documentation

### Overview
For this assignment, I am using Java as the programming language.

### Part 1
The problem proposed is that the servants had more presents than thank you cards. This could be due to a synchronization error when adding and removing from the chain of presents, and can be resolved by using a concurrent Linked List implementation. For this part, I have decided to implement the LazyList implementation shown in Chapter 9 of the textbook. This is because it only traverses the list once as opposed to Optimistic synchronization, thus proving to be more efficient when working with larger lists. 

Upon successful compilation, the output should look like this:
![Part 1 Output](/Images/part1Output.PNG)

As seen above, there are no issues regarding performance when testing on my machine.

### Part 2
Similar to the first part, I have used a LazyList implementation here as well. In order to generate a random value between -100F and 70F inside of the threads in a safe and efficient manner, I use the ThreadLocalRandom type.

Upon successful compilation, the output should look like this:
![Part 2 Output](/Images/part2Output.PNG)

As seen above, there are no issues regarding performance with this program either on my machine. 