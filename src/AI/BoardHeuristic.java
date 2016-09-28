import java.awt.Point;
import java.util.Map;

import connectK.BoardModel;


// Takes a Board State and returns its heuristic value.
public class BoardHeuristic {
	// Shortens code
	int k;
	// Score of a point
	double pointScore;
	// Scoring of the heuristic, can be negative!
	double totalScore;
	
	// The 8 K-length possible winning arrays from a particular point
	// Ex) For k=4, horizontal_left=[1,1,0,2], horizontal_right=[1,2,2,0]
	byte[] horizontal_left = null, horizontal_right = null, vertical_up = null, vertical_down = null, diag_up_right = null, diag_down_right = null, diag_up_left = null, diag_down_left = null;
	
	// An array of the 8 K-length arrays
	// Ex) For k=4, lightRaysForPoint=[[1,1,0,2],[1,2,2,0],...]
	byte[][] lightRaysForPoint = null;
	
	public BoardHeuristic(byte player, byte opponent, BoardModel state, Map<Point, Byte> movesThusFar) {
		// Initialize k
		k = state.getkLength();
		// Scoring of the AI and opponent
		double AIScore = 0, opponentScore = 0;
		
		// Debugging Strings
//		String AIString = "\n";
//		String opponentString = "\n";
		String AIString = "";
		String opponentString = "";
		
		// Loop over all moves made thus far 
		for (Map.Entry<Point, Byte> entry : movesThusFar.entrySet()) {
			lightRays(entry.getKey(), state);
			pointScore = scorePoint(entry.getKey());
			if (entry.getValue() == player) {
//				AIString += "\nAt move: " + "x=" + entry.getKey().x + ",y=" + entry.getKey().y;
//				AIString += toString(lightRaysForPoint);
				AIScore += pointScore;
			} else {
//				opponentString += "\nAt move: " + "x=" + entry.getKey().x + ",y=" + entry.getKey().y;
//				opponentString += " " + toString(lightRaysForPoint);
				opponentScore += pointScore;
			}
		}
		
		totalScore = AIScore - opponentScore;
		if (state.winner() != -1) {
			if (state.winner() == player) { AIScore += 10000; }
			if (state.winner() == opponent) { opponentScore += 10000; }
		}
		System.out.print("AI SCORE: " + AIScore + AIString);
		System.out.print(" PLAYER 2 SCORE: " + opponentScore + opponentString);
		System.out.print(" TOTAL SCORE: " + totalScore + "\n");
		
	}
	
	public void lightRays(Point p, BoardModel state) {
		// Updates the lightRaysForPoint variable, an array of 8 K-length arrays

		// Player considered for that point
		byte player = state.getSpace(p.x, p.y);
		byte opponent;
		if (player == 1) {opponent = 2;} else {opponent = 1;}
		
		// Placeholder for each space we look at
		byte space;
		
		// Quiescence to reduce unnecessary calculations for impossible winning arrays
		boolean h_left = p.x - k + 1 >= 0;
		boolean h_right = p.x + k - 1 <= state.width - 1;
		boolean v_up = p.y + k - 1 <= state.height - 1;
		boolean v_down = p.y - k + 1 >= 0;
		
		// If it is possible to have k positions on the horizontal axis to the left...
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
		
		// Construct the array of 8 possible k length positions
		lightRaysForPoint = new byte[][] {vertical_up, diag_up_right, horizontal_right, diag_down_right, vertical_down, diag_down_left, horizontal_left, diag_up_left};
		
		// Resetting values or next point
		horizontal_left = null; horizontal_right = null; vertical_up = null; vertical_down = null; diag_up_right = null; diag_down_right = null; diag_up_left = null; diag_down_left = null;
	}
	
	public double scorePoint(Point p) {
		// How to score each point/move with lightRaysForPoint
		int numOfConnections = 0;
		int combo = -1;
		double total = 0;
		for (int i = 0; i < 8; ++i) {
			if (lightRaysForPoint[i] != null) {
				for (byte space: lightRaysForPoint[i]) {
					if (space != 0) {
						++numOfConnections;
						++combo;
						total += Math.pow(2, combo);
					} else {
						combo = -1;
					}
				}
				if (combo == k - 1) {
					System.out.println("COMBOOO BREAKER!!!");
					total += 1000;
				}
				if (i % 2 == 0) { // If horizontal or vertial
					total += Math.pow(2, numOfConnections);
				} else { // If diagonal
					total += 1.5 * Math.pow(2, numOfConnections);
				}
				numOfConnections = 0;
				
			}
		}
		return total;
	}


	
	public String toString(byte[][] lightRays) {
		// Returns a string representation of the lightRaysForPoint
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
