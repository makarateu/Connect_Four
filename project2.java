import java.io.InputStream;
import java.util.*;
import java.io.*;

public class project2 {
    private static Integer MAX = Integer.MAX_VALUE;
    private static Integer MIN = Integer.MIN_VALUE;
    private static int prune = 0; //number of pruning 
    private static double start;
    private static double finish;
    private static Board board;
    private static Map<State, MinimaxInfo> transpoTable;
    private static int d; // depth

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        // asks for user input for row, col, type of connect
        System.out.print("Run part A, B, or C? ");
        String choice = scan.nextLine();
        System.out.print("Include debugging info? (y/n) ");
        String debug = scan.nextLine();
        System.out.print("Enter rows: ");
        int row = scan.nextInt();
        System.out.print("Enter columns: ");
        int col = scan.nextInt();
        System.out.print("Enter number in a row to win: ");
        int game = scan.nextInt();
        // create game board
        board = new Board(row, col, game);
        State initialState = new State(board);
        transpoTable = new HashMap<State, MinimaxInfo>();
        double winValue = 0;
        MinimaxInfo winInfo;
        // which program to run
        if (choice.equals("A")) {
            start = System.currentTimeMillis();
            winInfo = minimaxSearch(initialState, transpoTable);
            winValue = winInfo.getValue();
            finish = System.currentTimeMillis();
            // part B
        } else if (choice.equals("B")) {
            start = System.currentTimeMillis();
            winInfo = alphaBetaSearch(initialState, MIN, MAX, transpoTable);
            winValue = winInfo.getValue();
            finish = System.currentTimeMillis();
            // part C
        } else {
            System.out.print("Numver of moves to look ahead (depths): ");
            d = scan.nextInt();
        }
        // only print for program A & B
        if (choice.equals("A") || choice.equals("B")) {
            System.out.println("Search completed in " + (finish - start) / 1000 + "seconds.");
            System.out.println("Transposition table has " + transpoTable.size() + "states.");
            if (choice.equals("B")) {
                System.out.println("The tree was pruned " + prune + " times.");
            }
            // which player win if perfect play
            if (winValue > 0) {
                System.out.println("First player has a guarenteed win with perfect play.");
            } else if (winValue < 0) {
                System.out.println("Second player has a guarenteed win with perfect play.");
            } else {
                System.out.println("Tie with perfect play.");
            }
        }
        // debugging information
        if (debug.equals("y")) {
            System.out.println("Transposition table: ");
            for (State s : transpoTable.keySet()) {
                System.out.println(s.board().toString() + " -> MinimaxInfo[value=" +
                        transpoTable.get(s).getValue() + ", action=" +
                        transpoTable.get(s).getAction());
            }
        }
        // ------------------Play Game with computer----------------
        System.out.print("Who plays first? 1=human, 2=computer: ");
        int agent = scan.nextInt();
        System.out.println();
        System.out.println(initialState.board().to2DString());
        // continue game until there is a winner
        while (initialState.board().getGameState().equals(GameState.IN_PROGRESS)) {
            // Program C
            if (choice.equals("C")) {
                transpoTable.clear();
                start = System.currentTimeMillis();
                winInfo = alphaBetaHeuristicSearch(initialState, MIN, MAX, 0,
                        transpoTable);
                finish = System.currentTimeMillis();
                System.out.println("Search completed in " + (finish - start) / 1000
                        + " seconds");
                System.out.println("Transposition table has " + transpoTable.size()
                        + " states.");
            }
            boolean existed = true;
            System.out.println("Minimax value for this state: " +
                    transpoTable.get(initialState).value +
                    ", optimal move: " + transpoTable.get(initialState).action);
            System.out.println("It is " + toMove(initialState) + "'s turn!");
            // move for computer
            if (agent == 2) {
                System.out.println("Computer chooses move: " +
                        transpoTable.get(initialState).action);
                board = initialState.board().makeMove(transpoTable.get(initialState).action);
                initialState = new State(board);
                agent = 1;
                // move for user player
            } else {
                System.out.print("Enter move: ");
                int move = scan.nextInt();
                board = initialState.board().makeMove(move);
                initialState = new State(board);
                if (choice.equals("B") || choice.equals("C")) {
                    // alpha-beta pruning: if state is not in table, run alpha-beta
                    existed = transpoTable.containsKey(initialState);
                    if (existed == false) {
                        transpoTable.clear();
                        if (choice.equals("B")) {
                            winInfo = alphaBetaSearch(initialState, MIN, MAX, transpoTable);
                        } else {
                            winInfo = alphaBetaHeuristicSearch(initialState, MIN, MAX, 0, transpoTable);
                        }
                    }
                }
                agent = 2;
            }
            System.out.println("2 in a row: " +
                    initialState.board().containNInARow(2,
                            initialState.board().getPlayerToMoveNext().getNumber()));
            System.out.println("3 in a row: " +
                    initialState.board().containNInARow(3,
                            initialState.board().getPlayerToMoveNext().getNumber()));
            // print board state after make move
            System.out.println();
            System.out.println(initialState.board().to2DString());
            if (existed == false && choice.equals("B")) {
                System.out.println("This is a state that was previously pruned; rerunning alpha beta from here.");
            }
        }
        // print out the winner
        System.out.println("Game Over!");
        if (initialState.board().hasWinner()) {
            if (agent - 1 == 1) {
                System.out.println("The winner is " + initialState.board().getWinner() + " (YOU)");
            } else {
                System.out.println("The winner is " + initialState.board().getWinner() + " (COMPUTER)");
            }
        } else {
            System.out.println("No winner. TIE!");
        }
        // Play again?
        System.out.println("Play again? (y/n): ");
        scan.close();
    }

    // ----------------------------REGULARMINIMAXSEARCH-------------------------------
    public static MinimaxInfo minimaxSearch(State state, Map<State, MinimaxInfo> transpoTable) {
        MinimaxInfo info;
        int util;
        if (transpoTable.containsKey(state)) {
            return transpoTable.get(state);
        } else if (isTerminal(state)) {
            util = utility(state);
            info = new MinimaxInfo(util, -1); // -1 is for null
            transpoTable.put(state, info);
            return info;
            // MAX
        } else if (toMove(state).getNumber() == 1) {
            int v = MIN;
            int bestMove = -1;
            for (int a : actions(state)) {
                State childState = result(state, a);
                MinimaxInfo childInfo = minimaxSearch(childState, transpoTable);
                int v2 = childInfo.getValue();
                if (v2 > v) {
                    v = v2;
                    bestMove = a;
                }
            }
            info = new MinimaxInfo(v, bestMove);
            transpoTable.put(state, info);
            return info;
            // MIN move
        } else {
            int v = MAX;
            int bestMove = -1;
            for (int a : actions(state)) {
                State childState = result(state, a);
                MinimaxInfo childInfo = minimaxSearch(childState, transpoTable);
                int v2 = childInfo.getValue();
                if (v2 < v) {
                    v = v2;
                    bestMove = a;
                }
            }
            info = new MinimaxInfo(v, bestMove);
            transpoTable.put(state, info);
            return info;
        }
    }

    // -------------------------------ALPHA-BETASEARCH----------------------------------------------
    public static MinimaxInfo alphaBetaSearch(State state, int alpha, int beta, Map<State, MinimaxInfo> transpoTable) {
        MinimaxInfo info;
        int util;
        if (transpoTable.containsKey(state)) {
            return transpoTable.get(state);
        } else if (isTerminal(state)) {
            util = utility(state);
            info = new MinimaxInfo(util, -1); // -1 is for null
            transpoTable.put(state, info);
            return info;
            // MAX Player
        } else if (toMove(state).getNumber() == 1) {
            int v = MIN;
            int bestMove = -1;
            for (int a : actions(state)) {
                State childState = result(state, a);
                MinimaxInfo childInfo = alphaBetaSearch(childState, alpha, beta,
                        transpoTable);
                int v2 = childInfo.getValue();
                if (v2 > v) {
                    v = v2;
                    bestMove = a;
                    alpha = Math.max(alpha, v);
                }
                if (v >= beta) {
                    prune++;
                    return new MinimaxInfo(v, bestMove);
                }
            }
            info = new MinimaxInfo(v, bestMove);
            transpoTable.put(state, info);
            return info;
        }
        // MIN player
        else {
            int v = MAX;
            int bestMove = -1; // -1 = null
            for (int a : actions(state)) {
                State childState = result(state, a);
                MinimaxInfo childInfo = alphaBetaSearch(childState, alpha, beta,
                        transpoTable);
                int v2 = childInfo.getValue();
                if (v2 < v) {
                    v = v2;
                    bestMove = a;
                    beta = Math.min(beta, v);
                }
                if (v <= alpha) {
                    prune++;
                    return new MinimaxInfo(v, bestMove);
                }
            }
            info = new MinimaxInfo(v, bestMove);
            transpoTable.put(state, info);
            return info;
        }
    }

    // -------------------------ALPHA-BETA-HEURISTICSEARCH---------------------------------------------
    public static MinimaxInfo alphaBetaHeuristicSearch(State state, int alpha, int beta, int depth, Map<State, MinimaxInfo> transpoTable) {
        MinimaxInfo info;
        int util;
        if (transpoTable.containsKey(state)) {
            return transpoTable.get(state);
        } else if (isTerminal(state)) {
            util = utility(state);
            info = new MinimaxInfo(util, -1); // -1 is for null
            transpoTable.put(state, info);
            return info;
        } else if (isCutoff(state, depth)) {
            int heuristic = eval(state);
            info = new MinimaxInfo(heuristic, -1); // -1 = null
            transpoTable.put(state, info);
            return info;
        }
        // MAX Player
        else if (toMove(state).getNumber() == 1) {
            int v = MIN;
            int bestMove = -1;
            for (int a : actions(state)) {
                State childState = result(state, a);
                MinimaxInfo childInfo = alphaBetaHeuristicSearch(childState, alpha,
                        beta, depth + 1, transpoTable);
                int v2 = childInfo.getValue();
                if (v2 > v) {
                    v = v2;
                    bestMove = a;
                    alpha = Math.max(alpha, v);
                }
                if (v >= beta) {
                    prune++;
                    return new MinimaxInfo(v, bestMove);
                }
            }
            info = new MinimaxInfo(v, bestMove);
            transpoTable.put(state, info);
            return info;
        }
        // MIN player
        else {
            int v = MAX;
            int bestMove = -1; // -1 = null
            for (int a : actions(state)) {
                State childState = result(state, a);
                MinimaxInfo childInfo = alphaBetaHeuristicSearch(childState, alpha,
                        beta, depth + 1, transpoTable);
                int v2 = childInfo.getValue();
                if (v2 < v) {
                    v = v2;
                    bestMove = a;
                    beta = Math.min(beta, v);
                }
                if (v <= alpha) {
                    prune++;
                    return new MinimaxInfo(v, bestMove);
                }
            }
            info = new MinimaxInfo(v, bestMove);
            transpoTable.put(state, info);
            return info;
        }
    }

    // ---------------------------IS-TERMINAL-----------------------------
    public static boolean isTerminal(State state) {
        if (!(state.board().getGameState().equals(GameState.IN_PROGRESS))) {
            return true;
        }
        return false;
    }

    // ---------------------------UTILITY--------------------------------
    public static int utility(State state) {
        // assume the state is terminal and return utility value
        int stateUtil = 10000 * state.board().getRows() * state.board().getCols() /
                state.board().getNumberOfMoves();
        if (state.board().getGameState().equals(GameState.MAX_WIN)) {
            return stateUtil;
        } else if (state.board().getGameState().equals(GameState.MIN_WIN)) {
            return -1 * stateUtil;
        } else {
            return 0;
        }
    }

    // -----------------EVAL--------------------------
    public static int eval(State state) {
        // assume that the state is not terminal
        int stateUtil = 0;
        // 2 points for each 2 in a row
        stateUtil += (2 * state.board().containNInARow(2,
                state.board().getPlayerToMoveNext().getNumber()));
        // 3 points for each 3 in a row
        stateUtil += (3 * state.board().containNInARow(3,
                state.board().getPlayerToMoveNext().getNumber()));
        // if MAX is player
        if (state.board().getPlayerToMoveNext().getNumber() == 1) {
            return stateUtil;
            // if MIN is player
        } else if (state.board().getPlayerToMoveNext().getNumber() == -1) {
            return -1 * stateUtil;
        } else {
            return 0;
        }
    }

    // ----------------TO-MOVE----------------------
    public static Player toMove(State state) {
        return state.board().getPlayerToMoveNext();
    }

    // ---------------ACTIONS(state)-----------------------
    public static ArrayList<Integer> actions(State state) {
        ArrayList<Integer> actions = new ArrayList<Integer>();
        // if column is not full, then that column is an action
        for (int i = 0; i < state.board().getCols(); i++) {
            if (!state.board().isColumnFull(i)) {
                actions.add(i);
            }
        }
        return actions;
    }

    // ---------------RESULT(state, action)-----------------
    public static State result(State parent, int action) {
        Board newBoard = parent.board().makeMove(action);
        State child = new State(newBoard);
        return child;
    }

    // ----------------IS-CUTOFF------------------------------
    public static boolean isCutoff(State state, int depth) {
        if (d == depth) {
            return true;
        }
        return false;
    }
}