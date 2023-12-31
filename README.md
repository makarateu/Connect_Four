# Connect_Four

##### Require JDK 14 or higher

How the program should work
You should have one single program that allows the user to choose whether they want to run Part A, B, or C. The code for these three parts is very similar, and so it’s not as much work as you might think to get all three parts working. The fun in the project is in Part C, where you have complete creativity into how you want your heuristic to work.

When your program begins, it should prompt the user for the following pieces of information:

Whether the user wants to run Part A, B, or C.
Whether to print debugging information (for this project, debugging information is just the contents of the transposition table).
The size of the desired board, in rows and columns.
The number of consecutive tokens on the board needed to win (my starter code supports 2, 3, and 4; if you write your own Board class, it should support at least 3 and 4).
Note that having code that works with Connect-2 and Connect-3 is very helpful for debugging, because while these games aren’t very interesting, they have small state spaces that you can look through without getting overwhelmed.
After these steps, your program will follow Part A, B, or C below. All three parts end in a similar fashion: having the user play the game against the computer.

### Part A: Minimax, with transposition table

In this part, your program will then use the minimax algorithm to traverse the entire game tree to determine if the first player (MAX) has a guaranteed win, the second player (MIN) has a guaranteed win, or neither player is guaranteed to win (perfect play on both sides results in a draw).

The minimax value for a terminal state (a state where MAX has won, MIN has won, or the game is a draw) should be the calculated as follows. If MAX has won, the value of the state is int(10000.0 * rows * cols / moves), where moves is the number of moves the game has lasted. For instance, if you are playing on a 4-by-5 grid and MAX wins in 12 moves, then the value of this state is 10000 * 4 * 5 / 12 = 16666. Dividing by the length of the game will prioritize quicker-winning moves. If MIN has won, use the same formula, but negated. If there is a draw, the value of the state is zero.

After the user types in values for the board dimensions and the number in a row needed to win, run the minimax algorithm with a transposition table (see the pseudocode) to compute the optimal way to play the game from any possible state. Then, print out the following pieces of information:

The size of the transposition table (this is an easy way to test if your algorithm is working, because this number should match mine exactly).
Whether the first player (MAX) has a guaranteed win, loss, or draw assuming perfect play on both sides.
If the user asked for debugging information, print the entire contents of the table as well.
Next, let the user play against the computer. The user should be able to choose if they want to move first, or if they want to the computer to move first. The computer should used the saved minimax actions & values in the transposition table to look up each state and figure out what to do. Let the user play over and over again.

If your minimax algorithm determined that there is a guaranteed win for one of the players, and the computer acts as that player, then the computer should never lose.

As the game is being played, the program should print out, for each state as it is encountered, the minimax value of that state, and the optimal action from that state. These values are also helpful for debugging, as they should match mine.

### Part B: Minimax, with alpha-beta pruning and transposition table

You will notice that Part A will not be able to search the full state space for the “traditionally-sized” game of Connect Four (6 rows, 7 columns) in any reasonable amount of time — there are just too many board configurations to consider, even with using a transposition table. In Part B, we will add code to use alpha-beta pruning to remove large sections of the game tree that the algorithm can determine will never be encountered during perfect play.

If the user chooses Part B, your program should act similarly to Part A, except using the pseudocode for alpha-beta pruning, rather than regular minimax. To match my output, your algorithm should consider the possible moves of the game in order from left-to-right. That is, consider the columns of the board starting with column zero (the leftmost column). Alpha-beta works best when one considers the “best” move first, and so this particular move-ordering scheme is not great, but we are using it so that your output will match mine exactly. In Part C, you can change the order in which you consider moves to improve the algorithm more, if you want.

There are a few additional changes:

After running alpha-beta search before the game begins, in addition to the same information printed in Part A, you should also print the number of times the tree was pruned. This should match mine.
When letting the user play against the computer, it is possible that if the user makes a sub-optimal move, the computer may encounter a part of the game tree that alpha-beta pruned away. You will know when this happens because the transposition table will not have the current game board in it. This is ok! If this happens during the game, just re-run alpha-beta, starting from the current state of the game. The algorithm will work just fine starting from partway through the game. After the re-run of the algorithm finishes, the new transposition table should have the current game board in it, and you can continue playing with the updated table. It is possible, that if the user continues to play sub-optimally, that you will need to re-run alpha-beta more than once.

### Part C: Minimax, with alpha-beta pruning, heuristics, and transposition table

It turns out even alpha-beta pruning will not make the full board size of Connect Four feasible. Instead, what we will do is similar to what humans do when faced with a game that can continue for a large number of moves: we will only look ahead a fixed number of moves. To do this, we will require a heuristic function that can estimate the quality of an unfinished game state. (See section 5.3 of the textbook or your notes from class).

Here, the first thing Part C should do is prompt the user for the depth to which the algorithm should search for solutions. This is a variable that refers to the number of moves to “look ahead” in the game tree. For instance, with a depth of 1, the algorithm will only be able to examine the game states resulting from its own next move. If depth is 2, the algorithm will be able to examine game states resulting from its own next move and the user’s response move.

Unlike in Parts A and B, where we used minimax or alpha-beta to “pre-process” the game tree to get the best moves, in Part C we will re-run the algorithm before each move of the game is made. The reason for this is since we are using a cut-off depth, if we run a search after each move, we will always be able to look a fixed number of moves ahead of wherever we are in the game.

In other words, immediately after asking for the depth to search, your program should start the game (as always, let the user pick who goes first). Then the algorithm will run alpha-beta with heuristics, looking ahead the number of moves asked for, and calculate what it thinks is the best move. If the computer moves first, it will make that move, otherwise it will just be printed, and the user can choose to follow it or ignore it. Once the move is made, restart the search again from the new board position, again looking the same number of moves ahead (though now we can look one level relatively deeper in the tree), and continue on until the game ends.

Note: Because we are using a cut-off depth, the computer player is not expected to play perfectly, even in cases where in Part A or B it would have always won. That being said, if we use a deep enough cut-off depth and a good heuristic, the computer player should be pretty good.
