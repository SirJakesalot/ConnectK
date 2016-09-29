# ConnectK
CS 171 project, created an AI to play connect-K

Poor AI, Average AI, and Good AI were from students from a previous class.
DunamisAI is the AI I wrote.

To run:
With Java 7 installed
<code>$ java -jar bin/ConnectK_1.7.jar</code>
Configure AI settings by adding the DunamisAI.class and any opponent.
Select New game and watch them play!

You may notice the DunamisAI runs faster with gravity enabled, this is because I decided to stop searching for better moves after I hit a maxDepth of 5. This becomes a problem when gravity is disabled because the amount of possible moves grows dramatically. With gravity enabled, the number of possible moves is at most the width of the board. With gravity disabled, the number of possible moves is at most the <i>ENTIRE</i> board.


![alt BoardHeuristics](BoardHeuristics.png)
Here is a brief example of how I go about using heuristics to score a connect-k board.
