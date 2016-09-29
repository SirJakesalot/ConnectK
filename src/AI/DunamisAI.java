import connectK.CKPlayer;
import connectK.BoardModel;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Random;

public class DunamisAI extends CKPlayer{

    /*
     * Known Issues
     * Settings: AI goes first
     * Moves: [(4,4), (5,3), (6,2), (1,0)]
     * Desc: Will take too long to return a move
     */

	double WIN = Double.POSITIVE_INFINITY;
	double LOSS = Double.NEGATIVE_INFINITY;

	byte player;
	byte opponent;

    /* max number future moves to observe */ 
	int maxDepth = 5;
	/* move counter for the game */
	int moveCount = 0;
    /* best possible move found */ 
	Point bestMove;
    /* remember the current move sequence */
	Point[] movSeq = new Point[maxDepth];
    /* center of the board*/
	Point center;
    /* for gravity enabled, records column heights as the game plays */
	int[] boardHeights;
    /* 0 or 1 for the player or the opponent */	

    /* hashmap of possible moves in the game */
	HashSet<Point> freeSpaces = new HashSet<Point>();
    /* hashmap of moves made in the game */
	Map<Point, Byte> movesThusFar = new HashMap<Point, Byte>();
    /* hashmap of all available playBooks */
	Map<String, Point[][]> playBook = new HashMap<String, Point[][]>();

    /* attempting optimization of zobrist keys and transposition tables */
	long[][] zobrist;
	Map<Long, BoardInstance> transpositionTable = new HashMap<Long, BoardInstance>();

    /**
     * DunamisAI will initialize by generating several internal objects
     * @param player 0 or 1 representation of the player
     * @param state the current state of the game
     */
	public DunamisAI(byte player, BoardModel state) {
		super(player, state);
		teamName = "Dunamis";
		/* remember the board's center */
		center = new Point(state.getWidth()/2, state.getHeight()/2);
		/* remember which player I am and who the opponent is */
		this.player = player;
		if (player == 1) {opponent = 2;} else {opponent = 1;}
		/* record of current column maximums */
		genBoardHeights(state.getWidth());
		/* initiate the ability to compute Zobrist keys for each board positions */
		initZobrist(state.getWidth(), state.getHeight());
        /* fill freeSpaces with all possible moves */
		initFreeSpaces(state.getWidth(), state.getHeight());
	}
	
    /**
     * genBoardHeights is used when gravity is ENABLED and will keep record
     * of the heights for each column.
     * @param width the width of the board
     */
	void genBoardHeights(int width) {
		this.boardHeights = new int[width];
		for (int i = 0; i < width; ++i) {
			this.boardHeights[i] = -1;
		}
	}

    /**
     * Based on the current game state, determine the best move to make.
     * @param state the current state of the game
     * @return the best possible move for the player
     */
	@Override
	public Point getMove(BoardModel state) {
		/* if not the first move of the game, record the last move made */
		if (state.getLastMove() != null) {
			recordMove(state.getLastMove(), state.gravityEnabled(), state.getHeight(), opponent, false);
		} else {
            if (state.getSpace(center) == 0) {
                recordMove(center, state.gravityEnabled(), state.getHeight(), player, false);
                return center;
            }
        }
		
        
		double ab = alpha_beta(state, maxDepth, player, LOSS, WIN);
        // System.out.println("BEST SCORE IN " + maxDepth + " MOVES: " + ab );
		recordMove(bestMove, state.gravityEnabled(), state.getHeight(), player, false);
		return bestMove;
	}

    /**
     * Not using the deadline, instead using a maxDepth to determine when to stop.
     * @param state the current state of the game
     * @param deadline number of seconds to compute a move
     * @return the best possible move for the player
     */
	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}
	
    /**
     * Generate all possible moves to consider for the current state of the game.
     * @param state the current state of the game
     * @param print flag to print debug statements
     * @return A queue of Points to that should be considered as possible moves
     */
	public Queue<Point> genPossMoves(BoardModel state, boolean print) {
		Queue<Point> possibleMoves = new LinkedList<Point>();
		if (state.gravityEnabled()) {
            /* gravity enabled games should only consider the highest available move on each column */
			int increment = 0;
            /* add each move to the queue starting from the center and walking outward
             * this is because moves in the center of the board are considered higher than the edges of the board
             * ex) for a board with width 7 the following would be the queue positions
             * | 5 | 3 | 1 | 0 | 2 | 4 | 6 | */
			for (int i = 0; i < state.getWidth(); ++i) {
				if (boardHeights[center.x + increment] == state.getHeight()-1) {
					/* do not add because the column is filled */
				} else {
					possibleMoves.add(new Point(center.x+increment,boardHeights[center.x+increment] + 1));
				}
                /* change the increment to alternate from left to right */
				if (increment == 0) {increment--;}
				else if (increment < 0) {increment *= -1;}
				else {++increment; increment *= -1;}
			}
		
		} else {
            /* gravity disabled will add all empty spaces surrounding the moves already made */
            Point mv;
            Point[] surroundingMvs = new Point[8];
            for (Map.Entry<Point, Byte> entry : movesThusFar.entrySet()) {
                mv = entry.getKey();
                surroundingMvs[0] = new Point(mv.x, mv.y + 1);
                surroundingMvs[1] = new Point(mv.x + 1, mv.y + 1);
                surroundingMvs[2] = new Point(mv.x + 1, mv.y);
                surroundingMvs[3] = new Point(mv.x + 1, mv.y - 1);
                surroundingMvs[4] = new Point(mv.x, mv.y - 1);
                surroundingMvs[5] = new Point(mv.x - 1, mv.y - 1);
                surroundingMvs[6] = new Point(mv.x - 1, mv.y);
                surroundingMvs[7] = new Point(mv.x - 1, mv.y + 1);
                for (Point p: surroundingMvs) {
                    if (freeSpaces.contains(p) && !possibleMoves.contains(p)) {
                        possibleMoves.add(p);
                    }
                }
            }
		}
		if (print) {
            String possMovString = "Possible moves: ";
            /* print a string representation of what was just computed */	
			for (Point p: possibleMoves) {
				if (p == null) {
					possMovString += "null,";
				} else {
					possMovString += "[" + p.x + "," + p.y + "],";
				}
			}
			possMovString = possMovString.substring(0, possMovString.length() -1);
            System.out.println("Possible Moves: " + possMovString);
        }
		return possibleMoves;
	}

    /**
     * Create a string representation the current state of the game.
     * @return A string of all moves made so far.
     */
	public String movesThusFarToString() {
		String result = "";
		for (Map.Entry<Point, Byte> entry : movesThusFar.entrySet()) {
			result += "[" + entry.getKey().x + "," + entry.getKey().y + "]" + "->" + entry.getValue() + ",";
		}
		result = result.substring(0,result.length()-1);
		return result;
	}

    /**
     * Record a move that was made and remove it from freeSpaces.
     * @param mv the move made
     * @param gavity the gavity state of the board
     * @param height the height of the board
     * @param player the player that made the move
     * @param print whether to print debug messages
     */
	public void recordMove(Point mv, boolean gravity, int height,  byte player, boolean print) {
		if (gravity) {
            /* updates board heights only if gravity is on */
			/* sanitizes opponents failure to correctly record moves */
			if (boardHeights[mv.x]+1 < height) {
				++boardHeights[mv.x];
				movesThusFar.put(new Point(mv.x,boardHeights[mv.x]), player);
			} else {
                /* they attempted a move that doesn't exist */
            }
		} else {
			movesThusFar.put(mv, player);
			freeSpaces.remove(mv);
		}
		if (print) { System.out.println("Recorded move: x=" + mv.x + ",y=" + mv.y); }
	}

    /**
     * When testing the score of possible moves, we need to remove them after.
     * @param lastMv the move that needs to be removed
     * @param gravity the gravity state of the board
     * @param print whether to print debug messages
     */
	public void removeMove(Point lastMv, boolean gravity, boolean print) {
		if (print) { System.out.println("Removing the last move: x=" + lastMv.x + ",y=" + lastMv.y); }
		if (gravity) {
			--boardHeights[lastMv.x];
		} else {
			freeSpaces.add(lastMv);
		}
		movesThusFar.remove(lastMv);
	}

    /**
     * Perform alpha beta pruning when searching for the best available move.
     * @param state the current state of the board
     * @param depth the current depth from the bottom
     * @param turn the player for that turn
     * @param alpha the alpha value for the search
     * @param beta the beta value for the search
     */
	public double alpha_beta(BoardModel state, int depth, byte turn, double alpha, double beta) {
        // System.out.println("AT DEPTH: " + depth);

        if (state.winner() != -1) {
            // transpositionTable.put(key, new BoardInstance(LOSS));
            // System.out.println("Determined winner based on: " + moveSeqToString(movSeq));
            double score = 10000 + 10000 * depth;
            if (state.winner() == turn) { return score; }
            return -score;
        }

        Point bestMv = null;
        /* computes based on moves made thus far */
        long key = computeZobristKey(state.getWidth(), state.getHeight());
        BoardInstance bi = transpositionTable.get(key);

        if (bi != null) {
            /* already calculated! */
            return bi.getScore();
        }

        byte nextTurn = turn == player ? opponent : player;
        double value;

        if (depth == 0 || !state.hasMovesLeft() ) {
            value = new BoardHeuristic(turn, nextTurn, state, movesThusFar).getTotalScore();
            // System.out.println("MoveSeq: " + moveSeqToString(movSeq) + "Score: " + value);
            transpositionTable.put(key, new BoardInstance(value, 0));
            return value;
        }
        Queue<Point> possMvs = genPossMoves(state, false);
        double best = LOSS;
        Point mv = null;
        BoardModel nextBoard;

        // Long key = computeZobristkey(state.getWidth(), state.getHeight());
        // BoardInstance bi = transpositionTable.get(key);
        // if (bi != null && bi.depth <= depth) {
        //     System.out.println("This has been calculated before!");
        //     return bi.getScore();
        // }

        while (!possMvs.isEmpty()) {
            mv = possMvs.remove();
            movSeq[depth-1] = mv;
            nextBoard = state.placePiece(mv, turn);
            recordMove(mv, state.gravityEnabled(), state.getHeight(), turn, false);
            value = -alpha_beta(nextBoard, depth-1, nextTurn, -beta, -alpha);
            removeMove(mv, state.gravityEnabled(), false);

            if (value > best) {
                // System.out.println("Found that " + value + " > " + best);
                best = value;
                if (depth == maxDepth) {
                    // System.out.println("Updating new bestMove to: x=" + mv.x + ",y=" + mv.y);
                    bestMv = mv;
                }
            }
            if (best > alpha) { alpha = best; }
            if (best >= beta) {
                if (best == WIN && beta == WIN) {
                }
                // System.out.println("PRUNING!");
                break;
            }
        }
        if (depth == maxDepth && bestMv != null) {
            bestMove = bestMv;
        }
        return best;


	}
	
    /**
     * Initialized the zobrist table by generating random numbers for each player for each space
     * A zorbist table is a 2-D array of available spaces by available players (1 or 2)
     * Generate random numbers for each of these slots
     * @param w the width of the board
     * @param h the height of the board
     */
	public void initZobrist(int w, int h) {
		// System.out.println("Initializing Zorbrist Table");
		Random rand = new Random();
		zobrist = new long[w*h][2];
		for (int space = 0; space<zobrist.length; ++space) {
			for (int side = 0; side < zobrist[space].length; ++side) {
				zobrist[space][side] = rand.nextLong();
			}
		}
	}
    /**
     * Every move made thus far constributes to the calculation of the zobrist key
     * @param w the width of the board
     * @param h the height of the board
     * @return zorbrist key
     */
	public Long computeZobristKey(int w, int h) { // WARNING! Player 1 = index 0, Player 2 = index 1
		long hashKey = 0;
		for (Map.Entry<Point, Byte> entry : movesThusFar.entrySet()) {
			int index = entry.getKey().x * h + entry.getKey().y;
			hashKey ^= zobrist[index][entry.getValue()-1];
		}
		return hashKey;
	}
	
    
	public long updateZobristKey (long oldKey, int moveSquare, int moveSide) {
		// To unmake the move and get the old key, the above function works too!
		// XOR behaves like negation in logic, so applying it twice gets you back your original key.
		return oldKey ^ zobrist[moveSquare][moveSide];
	}

    /**
     * Fill freeSpaces with all available spaces on the board
     * @param w the width of the board
     * @param h the height of the board
     */
	public void initFreeSpaces(int w, int h) {
		freeSpaces = new HashSet<Point>();
		for (int col = 0; col < w; ++col) {
			for (int row = 0; row < h; ++row) {
				freeSpaces.add(new Point(col,row));
			}
		}
	}

    /**	
     * Determine the opponent of the current player
     * @param p the current player
     * @return the other player
     */
	public byte opponentOf(byte p) {
		return p == 1 ? 2 : (byte)1;
	}
	
    /**
     * Convert a move sequence to a string representation
     * @param movSeq an array of moves to be made
     * @return the string representation
     */
	public String moveSeqToString(Point[] movSeq) {
		String bestMoveSeq = "";
		for (int i = maxDepth - 1; i > -1; --i) {
			bestMoveSeq += "[x=" + movSeq[i].x + ",y=" + movSeq[i].y + "],";
		}
		return bestMoveSeq.substring(0, bestMoveSeq.length()-1);
	}
}
