package com.example;

public class Player {
    public static final double HOOP_X = 750.0;
    public static final double HOOP_Y = 90.0;
    public static final double THREE_POINT_RADIUS = 520.0;
    private static final double LAYUP_RADIUS = 165.0;

    private int pNum;
    private int nbaplayer;
    private int score;
    private double x;
    private double y;
    
    public Player(int n, int nba, int s) {
        pNum = n;
        nbaplayer = nba;
        score = s;
    }

    /* 2D array shooting_pct includes the shooting percentages for each NBA player.
    Columns show pNum, three point percentage, midrange percentage, and layup percentage
    Row 1 is LeBron James' stats, Row 2 is Stephen Curry's stats, Row 3 is Giannis Antetokounmpo's stats
    Row 4 is Kevin Durant's stats, Row 5 is Luka Doncic's stats. */
    double[][] shooting_pct = {
        { 1, 0.317, 0.418, 0.752 },
        { 2, 0.393, 0.516, 0.697 },
        { 3, 0.333, 0.311, 0.781 },
        { 4, 0.413, 0.541, 0.764 },
        { 5, 0.366, 0.508, 0.816 }
    };

    //boolean make = false;

    //int keypress = 69;


    // getNum returns the player number of the Player chosen.
    public int getNum() {
        return pNum;
    }

    public int getNBA() {
        //System.out.println("playing as " + nbaplayer);
        return nbaplayer;
    }

    public void setNBA(int charNum){
        nbaplayer = charNum;
    }

    public double getStats(int shotType) {
        int nba = getNBA();
        //three point
        if (shotType == 1) {
            return shooting_pct[nba-1][1];
        }
        //midrange
        else if (shotType == 2) {
            return shooting_pct[nba-1][2];
        }
        //layup
        else {
            return shooting_pct[nba-1][3];
        }
    }

    //boolean shoot = true;

    /*double three_point_pct = getStats(1);
    double midrange_pct = getStats(2);
    double layup_pct = getStats(3);*/

    public boolean makeOrMiss(Data data) {
        int shotType = shotType();
        //distanceProb is the chance that the Player does score
        double distanceProb = data.distanceProb(pNum, shotType);
        double randProb = Math.random();

        //There is a random chance that the Player does not score
        if (randProb <= distanceProb) {
            // Scoring: outside the 3-point line = 3 pts, anywhere inside = 2 pts.
            if (shotType == 1) {        // 3-point zone
                score += 3;
            } else {                    // midrange or layup (inside the arc)
                score += 2;
            }
            return true;
        } else {
            return false;
        }
    }

    /** Public wrapper so the UI can show whether the last shot was 2 or 3 pts. */
    public int pointValueForCurrentPosition() {
        return (shotType() == 1) ? 3 : 2;
    }

    public static boolean isInsideThreePointLine(double playerX, double playerY) {
        double distFromHoop = Math.sqrt(Math.pow(playerX - HOOP_X, 2) + Math.pow(playerY - HOOP_Y, 2));
        return distFromHoop <= THREE_POINT_RADIUS;
    }

    public void setPlayerX(double pX){
        x = pX + 50;        //Staggers the ImageView's x coords so it's not the x coord of the Player's top-left corner
    }

    public void setPlayerY(double pY){
        y = pY + 80;        //Staggers the ImageView's x coords so it's not the x coord of the Player's top-left corner
    }

    public double getPlayerX(){
        return x;
    }

    public double getPlayerY(){
        return y;
    }

    public int getScore(){
        return score;
    }

    public int shotType(){      //Either Layup, Midrange, or 3pt depending on the Player's location
        double distFromHoop = Math.sqrt(Math.pow(x - HOOP_X, 2) + Math.pow(y - HOOP_Y, 2));
        if (distFromHoop <= LAYUP_RADIUS) {
            return 3;       //3 = Layup
        } else if (isInsideThreePointLine(x, y)) {
            return 2;       //2 = Midrange
        } else {
            return 1;       //1 = 3pt Shot
        }
    }

    public void whoIsWinner(Player player1, Player player2) {
        int player1_score = player1.score;
        int player2_score = player2.score;
        if (player1_score == 11) {
            System.out.println("Player 1 has won.");
        }
        else if (player2_score == 11) {
            System.out.println("Player 2 has won.");
        }
        
    }
    public static void main(String[] args) {
        Player p1 = new Player(13, 1, 0);
        Data bballData = new Data();
    
        p1.getNum();
        p1.getNBA();
    }
    
}
