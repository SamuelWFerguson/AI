# TSP Genetic Algorithm Application
This is an application for solving the Traveling Salesman Problem using a genetic algorithm.

**What is the Traveling Salesman Problem?**
You are a salesman with a list of cities where you must go and sell your product. You want to go and sell to every one of these cities and then return to the city where you started. To find the most efficient way to do this, you must calculate the path around these cities which has the shortest length. The challenge to this problem is finding a solution which does not grow in time polynomially with respect to the amount of cities in your list. 

**What is a genetic algorithm?**
An algorithm which is inspired by the process of natural selection in which all lifeforms breed, mutate, and die.
A genetic algorithm goes through these same cycles and as it does, it improves itself.

**What does the TSP Genetic Algorithm Application do?**
This application aims to find near-optimal results for the TSP in non-polynomial time using a genetic algorithm.
To do this, a 'population' of solutions is put through the 3 basic cycles of life: breeding, mutation, and death.
This population improves itself each cycle until eventually a ceiling is reached where the algorithm is no longer making significant improvments.
At this ceiling, the genetic algorithm has found its near-optimal population.
This application oftenly finds the very best solution but has no way of confirming it has without doing an exhaustive check of every possible path.

<p align="center">
  <img src="./img/Example.png" alt="Genetic Algorithm Application example">
</p>
