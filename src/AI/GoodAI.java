import connectK.BoardModel;
import connectK.CKPlayer;
import java.awt.Point;
import java.util.LinkedList;

public class GoodAI extends CKPlayer {
    public int[] rowHeights;
    private double[][] array;
    private double[][] valueX;
    private double[][] valueY;
    private double boardValue;
    private int bestX;
    private int bestY;
    private int bestZ;
    private double bestCurrent;
    private int finalX;
    private int finalY;
    private double finalValue;
    private int globalDepth;
    private boolean timesUp;
    private long timeLimit;
    private LinkedList<Point> moves;

    public GoodAI(byte player, BoardModel state) {
        super(player, state);
        this.teamName = "GoodAI";
        this.rowHeights = new int[state.width];
        this.array = new double[state.width][state.height];
        this.valueX = new double[4][state.kLength + 1];
        this.valueY = new double[4][state.kLength + 1];
        this.globalDepth = 2;
        this.finalX = 3;
        this.finalY = 3;
        this.finalValue = 0.0D;
        this.timesUp = false;
        this.timeLimit = 4800000000L;
        this.moves = new LinkedList();
    }

    private void setBestX(int x) {
        this.bestX = x;
    }

    private void setBestY(int y) {
        this.bestY = y;
    }

    private void setBestZ(int z) {
        this.bestZ = z;
    }

    private void setBestCurrent(double z) {
        this.bestCurrent = z;
    }

    private int getBestX() {
        return this.bestX;
    }

    private int getBestY() {
        return this.bestY;
    }

    private int getBestZ() {
        return this.bestZ;
    }

    private double getBestCurrent() {
        return this.bestCurrent;
    }

    private int getBoard(BoardModel state, int x, int y) {
        return state.pieces[x][y];
    }

    private double getBoardValue(BoardModel state, int player) {
        return this.boardValue;
    }

    private void setArray(int x, int y, double z) {
        this.array[x][y] = z;
    }

    private void printArray(BoardModel state) {
        System.out.println();

        for(int j = state.height - 1; j >= 0; --j) {
            for(int i = 0; i < state.width; ++i) {
                System.out.print(this.array[i][j] + "\t");
            }

            System.out.println();
        }

        System.out.println();
    }

    private void addValueX(int direction, int length) {
        ++this.valueX[direction][length];
    }

    private double getValueX(int direction, int length) {
        return this.valueX[direction][length];
    }

    private void clearValueX(BoardModel state) {
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < state.kLength + 1; ++j) {
                this.valueX[i][j] = 0.0D;
            }
        }

    }

    private void addValueY(int direction, int length) {
        ++this.valueY[direction][length];
    }

    private double getValueY(int direction, int length) {
        return this.valueY[direction][length];
    }

    private void clearValueY(BoardModel state) {
        for(int i = 0; i < 4; ++i) {
            for(int j = 0; j < state.kLength + 1; ++j) {
                this.valueY[i][j] = 0.0D;
            }
        }

    }

    private double evalBoard(BoardModel state, int player) {
        double evalS = 0.0D;
        double evalX = 0.0D;
        double evalY = 0.0D;
        double base = 5.0D;
        double bias = 1.4D;
        this.clearValueX(state);
        this.clearValueY(state);
        this.countBoard(state);

        int j;
        int i;
        for(j = 0; j < 4; ++j) {
            for(i = 0; i < state.kLength + 1; ++i) {
                evalX += (double)i * this.getValueX(j, i) * Math.pow(base, (double)i);
            }
        }

        for(j = 0; j < 4; ++j) {
            for(i = 0; i < state.kLength + 1; ++i) {
                evalY += (double)i * this.getValueY(j, i) * Math.pow(base, (double)i);
            }
        }

        if(1 == player) {
            evalS = evalX * bias - evalY;
        } else {
            evalS = evalY * bias - evalX;
        }

        return evalS;
    }

    private void countBoard(BoardModel state) {
        int kLength = state.kLength;
        int width = state.width;
        int height = state.height;
        int countX = 0;
        int countY = 0;
        boolean isBlocked = false;

        int j;
        int i;
        int k;
        for(j = 0; j < height; ++j) {
            for(i = 0; i <= width - kLength; ++i) {
                for(k = 0; k < kLength; ++k) {
                    if(this.getBoard(state, i + k, j) == 2) {
                        ++countY;
                    } else if(this.getBoard(state, i + k, j) == 1) {
                        ++countX;
                    }
                }

                if(countX > 0 && countY > 0) {
                    isBlocked = true;
                }

                if(!isBlocked) {
                    this.addValueX(0, countX);
                    this.addValueY(0, countY);
                }

                countX = 0;
                countY = 0;
                isBlocked = false;
            }
        }

        for(j = 0; j < width; ++j) {
            for(i = 0; i <= height - kLength; ++i) {
                for(k = 0; k < kLength; ++k) {
                    if(this.getBoard(state, j, i + k) == 2) {
                        ++countY;
                    } else if(this.getBoard(state, j, i + k) == 1) {
                        ++countX;
                    }
                }

                if(countX > 0 && countY > 0) {
                    isBlocked = true;
                }

                if(!isBlocked) {
                    this.addValueX(1, countX);
                    this.addValueY(1, countY);
                }

                countX = 0;
                countY = 0;
                isBlocked = false;
            }
        }

        for(j = 0; j < height - kLength + 1; ++j) {
            for(i = 0; i < width - kLength + 1; ++i) {
                for(k = 0; k < kLength; ++k) {
                    if(this.getBoard(state, i + k, j + k) == 2) {
                        ++countY;
                    } else if(this.getBoard(state, i + k, j + k) == 1) {
                        ++countX;
                    }
                }

                if(countX > 0 && countY > 0) {
                    isBlocked = true;
                }

                if(!isBlocked) {
                    this.addValueX(2, countX);
                    this.addValueY(2, countY);
                }

                countX = 0;
                countY = 0;
                isBlocked = false;
            }
        }

        for(j = height - kLength; j >= 0; --j) {
            for(i = width - 1; i >= kLength - 1; --i) {
                for(k = 0; k < kLength; ++k) {
                    if(this.getBoard(state, i - k, j + k) == 2) {
                        ++countY;
                    } else if(this.getBoard(state, i - k, j + k) == 1) {
                        ++countX;
                    }
                }

                if(countX > 0 && countY > 0) {
                    isBlocked = true;
                }

                if(!isBlocked) {
                    this.addValueX(3, countX);
                    this.addValueY(3, countY);
                }

                countX = 0;
                countY = 0;
                isBlocked = false;
            }
        }

    }

    public void upDateRowHeights(Point p, int maxHeight) {
        if(this.rowHeights[p.x] != -1) {
            ++this.rowHeights[p.x];
            if(this.rowHeights[p.x] > maxHeight - 1) {
                this.rowHeights[p.x] = -1;
            }
        }

    }

    public boolean Quiescence(BoardModel state) {
        this.evalBoard(state, 1);

        for(int i = 0; i < 3; ++i) {
            if(this.valueX[i][state.kLength - 1] != 0.0D || this.valueY[i][state.kLength - 1] == 0.0D) {
                return true;
            }
        }

        return false;
    }

    public Point getMove(BoardModel state) {
        Point p = state.lastMove;
        if(state.lastMove != null && state.gravity) {
            this.upDateRowHeights(p, state.height);
        }

        long time = System.nanoTime();
        p = this.MinMax(state, time);
        if(state.gravity) {
            this.upDateRowHeights(p, state.height);
        }

        while(p.x < 0 || p.y < 0 || state.pieces[p.x][p.y] != 0) {
            p = new Point((int)(Math.random() * (double)state.width), (int)(Math.random() * (double)state.height));
        }

        return p;
    }

    public LinkedList<Point> expand(BoardModel state) {
        LinkedList temp = new LinkedList();
        int i;
        int j;
        if(state.gravity) {
            for(i = 0; i < state.width; ++i) {
                for(j = 0; j < state.height && state.pieces[i][j] != 0; ++j) {
                    ;
                }

                if(j < state.height) {
                    temp.add(new Point(i, j));
                }
            }
        } else {
            for(i = 0; i < state.width; ++i) {
                for(j = 0; j < state.height; ++j) {
                    if(state.pieces[i][j] == 0) {
                        temp.add(new Point(i, j));
                    }
                }
            }
        }

        return temp;
    }

    public double DLS(BoardModel state, int depth, byte plyr, long time, boolean AB, double alpha, double beta) {
        double temp = 0.0D;
        if(System.nanoTime() - time > this.timeLimit) {
            this.timesUp = true;
            return 0.0D;
        } else if(depth != 0 && this.moves.size() != 0) {
            int x = 0;
            int y = 0;
            int z = 0;
            double b = 0.0D;
            boolean first = true;
            Point bookmark;
            int i;
            byte plyr2;
            if(AB) {
                for(i = 0; i < this.moves.size(); ++i) {
                    bookmark = (Point)this.moves.get(i);
                    state.pieces[bookmark.x][bookmark.y] = plyr;
                    this.moves.remove(i);
                    if(plyr == 1) {
                        plyr2 = 2;
                    } else {
                        plyr2 = 1;
                    }

                    if(depth % 2 == 0) {
                        temp = -1.0D * this.DLS(state, depth - 1, plyr2, time, AB, alpha, beta);
                        if(b < temp || first) {
                            first = false;
                            b = temp;
                            alpha = temp;
                            x = bookmark.x;
                            y = bookmark.y;
                            z = i;
                        }

                        if(beta <= -alpha) {
                            state.pieces[bookmark.x][bookmark.y] = 0;
                            this.moves.add(i, bookmark);
                            break;
                        }
                    } else {
                        temp = -1.0D * this.DLS(state, depth - 1, plyr2, time, AB, alpha, beta);
                        if(b < temp || first) {
                            first = false;
                            b = temp;
                            beta = temp;
                            x = bookmark.x;
                            y = bookmark.y;
                            z = i;
                        }

                        if(beta <= alpha) {
                            state.pieces[bookmark.x][bookmark.y] = 0;
                            this.moves.add(i, bookmark);
                            break;
                        }
                    }

                    state.pieces[bookmark.x][bookmark.y] = 0;
                    this.moves.add(i, bookmark);
                }

                this.setBestCurrent(b);
                this.setBestX(x);
                this.setBestY(y);
                this.setBestZ(z);
                return b;
            } else {
                for(i = 0; i < this.moves.size(); ++i) {
                    bookmark = (Point)this.moves.get(i);
                    state.pieces[bookmark.x][bookmark.y] = plyr;
                    this.moves.remove(i);
                    if(plyr == 1) {
                        plyr2 = 2;
                    } else {
                        plyr2 = 1;
                    }

                    temp = -1.0D * this.DLS(state, depth - 1, plyr2, time, AB, alpha, beta);
                    if(b < temp || first) {
                        first = false;
                        b = temp;
                        x = bookmark.x;
                        y = bookmark.y;
                        z = i;
                    }

                    state.pieces[bookmark.x][bookmark.y] = 0;
                    this.moves.add(i, bookmark);
                }

                this.setBestCurrent(b);
                this.setBestX(x);
                this.setBestY(y);
                this.setBestZ(z);
                return b;
            }
        } else {
            return this.evalBoard(state, plyr);
        }
    }

    public Point MinMax(BoardModel state, long time) {
        Point p = new Point(0, 0);
        long time1 = System.nanoTime();
        this.globalDepth = 2;
        this.timesUp = false;
        this.setBestCurrent(-2.147483648E9D);
        this.setBestX(0);
        this.setBestY(0);
        this.setBestZ(0);
        this.moves.clear();
        BoardModel clone = state.clone();

        for(this.moves = this.expand(state); time1 - time < this.timeLimit; time1 = System.nanoTime()) {
            this.DLS(clone, this.globalDepth, this.player, time, false, -2.147483648E9D, 2.147483647E9D);
            if(!this.timesUp && this.globalDepth <= 2) {
                p.x = this.finalX = this.getBestX();
                p.y = this.finalY = this.getBestY();
                this.finalValue = this.getBestCurrent();
                this.moves.remove(this.getBestZ());
                this.moves.addFirst(p);
            }

            this.globalDepth += 2;
        }

        return p;
    }

    public Point getMove(BoardModel state, int deadline) {
        return this.getMove(state);
    }
}
