import java.awt.Point;

public class BoardInstance {
	double score;
	Point bestMove;
	int depth;

	public BoardInstance(double totalScore, int currentDepth) {
		score = totalScore;
        depth = currentDepth;
	}
	public double getScore() {
		return score;
	}
	public Point getBestMv() {
		return bestMove;
	}
}
