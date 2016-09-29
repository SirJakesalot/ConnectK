import java.awt.Point;
import java.util.Map;

import connectK.BoardModel;


/*
 * BoardHeuristic will evaluate a game state and return a numerical value
 * representing that board in favor of the player.
 */
public class BoardHeuristic {
	/* number of spaces in a row to win */
	int k;
	/* score of a particular space on the board */
	double pointScore;
	/* total score of the board */
	double totalScore;
	
    /* there are 8 possible paths to search when considering a winning position
     * these are calculated for every non-empty position on the board
     * ex) for k=4 and position (4,0), horizontal_left=[1,1,0,2], horizontal_right=[1,2,2,0], etc.
     */
	byte[] horizontal_left  = null;
    byte[] horizontal_right = null;
    byte[] vertical_up      = null;
    byte[] vertical_down    = null;
    byte[] diag_up_right    = null;
    byte[] diag_down_right  = null;
    byte[] diag_up_left     = null;
    byte[] diag_down_left   = null;
	/* an array of the 8 k-length above arrays
	 * ex) for k=4, lightRaysForPoint=[[1,1,0,2],[1,2,2,0],...]
     */
	byte[][] lightRaysForPoint = null;


    /*
     * For the given board state and all moves made thus far, score each player
     * based on how close they are to having k spaces in a row
     */	
	public BoardHeuristic(byte player, byte opponent, BoardModel state, Map<Point, Byte> movesThusFar) {
		k = state.getkLength();
		/* scoring of the AI and opponent */
		double AIScore = 0;
        double opponentScore = 0;
	
        /* debuging information */	
		String AIString = "";
		String opponentString = "";

        	
		for (Map.Entry<Point, Byte> entry : movesThusFar.entrySet()) {
			lightRays(entry.getKey(), state);
            pointScore = scorePoint(entry.getKey());
			if (entry.getValue() == player) {
                AIString += "\nAt move: " + "x=" + entry.getKey().x + ",y=" + entry.getKey().y;
                AIString += toString(lightRaysForPoint);
                AIScore += pointScore;
			} else {
                opponentString += "\nAt move: " + "x=" + entry.getKey().x + ",y=" + entry.getKey().y;
                opponentString += " " + toString(lightRaysForPoint);
                opponentScore += pointScore;
			}
		}
		
		totalScore = AIScore - opponentScore;
		if (state.winner() != -1) {
			if (state.winner() == player) { AIScore += 10000; }
			if (state.winner() == opponent) { opponentScore += 10000; }
		}
		// System.out.print("AI SCORE: " + AIScore + AIString);
		// System.out.print(" PLAYER 2 SCORE: " + opponentScore + opponentString);
		// System.out.print(" TOTAL SCORE: " + totalScore + "\n");
		
	}

    /*
     * Updates the lightRaysForPoint variable, an array of 8 k-length arrays
     * @param p the space being considered
     * @param state the current state of the board
     */	
	public void lightRays(Point p, BoardModel state) {

		/* player considered for that point */
		byte player = state.getSpace(p.x, p.y);
		byte opponent;
		if (player == 1) {opponent = 2;} else {opponent = 1;}
		
		/* Placeholder for each space we look at */
		byte space;
	
        /* only continue calculation on the paths that have a possibility to
         * win on the board
         */	
        boolean h_left = p.x - k + 1 >= 0;
		boolean h_right = p.x + k - 1 <= state.width - 1;
		boolean v_up = p.y + k - 1 <= state.height - 1;
		boolean v_down = p.y - k + 1 >= 0;
	
        /* 
         * having an opponent's piece disrupt your path or the path reach off
         * the board will result in null.
         * if it is possible to win from the position, record the spaces
         */
		if (h_left) {
			horizontal_left = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x - i, p.y);
				if (space == opponent) {
					horizontal_left = null;
					break;
				}
				horizontal_left[i] = space;
			}
		}
		
		if (h_right) {
			horizontal_right = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x + i, p.y);
				if (space == opponent) {
					horizontal_right = null;
					break;
				}
				horizontal_right[i] = space;
			}
		}
		
		if (v_up) {
			vertical_up = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x, p.y + i);
				if (space == opponent) {
					vertical_up = null;
					break;
				}
				vertical_up[i] = space;
			}
		}
		
		if (v_down) {
			vertical_down = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x, p.y - i);
				if (space == opponent) {
					vertical_down = null;
					break;
				}
				vertical_down[i] = space;
			}
		}
		
		if (h_right && v_up) {
			diag_up_right = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x + i, p.y + i);;
				if (space == opponent) {
					diag_up_right = null;
					break;
				}
				diag_up_right[i] = space;
			}
		}
		
		if (v_down && h_right) {
			diag_down_right = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x + i, p.y - i);
				if (space == opponent) {
					diag_down_right = null;
					break;
				}
				diag_down_right[i] = space;
			}
		}
		
		if (h_left && v_up) {
			diag_up_left = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x - i, p.y + i);
				if (space == opponent) {
					diag_up_left = null;
					break;
				}
				diag_up_left[i] = space;
			}
		}
		
		if (v_down && h_left) {
			diag_down_left = new byte[k];
			for (int i = 0; i < k; ++i) {
				space = state.getSpace(p.x - i, p.y - i);
				if (space == opponent) {
					diag_down_left = null;
					break;
				}
				diag_down_left[i] = space;
			}
		}
		
		/* construct the array of 8 possible k length positions */
		lightRaysForPoint = new byte[][] {vertical_up, diag_up_right, horizontal_right, diag_down_right, vertical_down, diag_down_left, horizontal_left, diag_up_left};
		
		/* reset values for next point */
		horizontal_left  = null;
        horizontal_right = null;
        vertical_up      = null;
        vertical_down    = null;
        diag_up_right    = null;
        diag_down_right  = null;
        diag_up_left     = null;
        diag_down_left   = null;
	}

    /*
     * A point will be scored by examining their lightRaysForPoint array
     * @param p a space on the board
     * @return the total score for that space
     */	
	public double scorePoint(Point p) {
        /* number of possible winning paths*/
		int numOfConnections = 0;
        /* number of spaces in a row */
        int inARow = 0;
        /* total score for the space */
		double total = 0;

        /* score the point for the possible winning paths */
		for (int i = 0; i < 8; ++i) {
			if (lightRaysForPoint[i] != null) {
				for (byte space: lightRaysForPoint[i]) {
					if (space != 0) {
						++numOfConnections;
						++inARow;
                        if (inARow > 1) {
                            total += Math.pow(2, inARow);
                        }
					} else {
                        if (inARow == k - 1) {
                            total += 500;
                        }
                        inARow = 0;
					}
				}
                /* diagonal winning moves are weighted higher than hoizontal or vertical */
				if (i % 2 == 0) {
                    /* horizontal or vertical */
					total += Math.pow(2, numOfConnections);
				} else {
                    /* diagonal */
					total += 1.5 * Math.pow(2, numOfConnections);
				}
				numOfConnections = 0;
                inARow = 0;
			}
		}
		return total;
	}

    /*
     * convert lightRays to a string for debugging
     * @param lightRays the 8 possible winning paths for a space
     * @return a string representation of the 2D array of bytes
     */
	public String toString(byte[][] lightRays) {
		String output = "";
		for (byte[] lightRay: lightRays) {
			if (lightRay != null) {
				output += "[";
				for (byte b : lightRay) {
					output += b + ",";
				}
				output = output.substring(0, output.length() - 1);
				output += "],";
			} else {
				output += lightRay + ",";
			}
		}
		output = output.substring(0, output.length() - 1);
		return output;
	}

	public double getTotalScore() {
		return totalScore;
	}
}
