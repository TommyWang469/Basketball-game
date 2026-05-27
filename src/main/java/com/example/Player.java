package com.example;

/**
 * Represents one basketball player during a match.
 *
 * <p>The player stores the selected character, current score, and court position.
 * It also owns shot classification and score updates when a shot is made.</p>
 */
public class Player {
    /** X-coordinate of the hoop/rim in game space. */
    public static final double HOOP_X = 750.0;
    /** Y-coordinate of the hoop/rim in game space. */
    public static final double HOOP_Y = 90.0;
    /** Radius used to decide whether a shot is inside or outside the three-point line. */
    public static final double THREE_POINT_RADIUS = 520.0;
    private static final double LAYUP_RADIUS = 165.0;

    private int pNum;
    private int nbaplayer;
    private int score;
    private double x;
    private double y;

    /**
     * Creates a player model.
     *
     * @param n player number, usually 1 or 2
     * @param nba selected character number from the character-select screen
     * @param s starting score
     */
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

    /**
     * Gets this player's game number.
     *
     * @return player number
     */
    public int getNum() {
        return pNum;
    }

    /**
     * Gets the selected character number.
     *
     * @return character number from 1 to 5
     */
    public int getNBA() {
        return nbaplayer;
    }

    /**
     * Updates the selected character number.
     *
     * @param charNum character number from 1 to 5
     */
    public void setNBA(int charNum){
        nbaplayer = charNum;
    }

    /**
     * Looks up the static shooting stat for the selected character.
     *
     * @param shotType 1 for three-point, 2 for midrange, 3 for layup
     * @return the character's base shooting percentage for that shot type
     */
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

    /**
     * Runs the make/miss roll for the current shot and updates score on a make.
     *
     * @param data game state used to calculate shot probability
     * @return {@code true} if the shot went in, otherwise {@code false}
     */
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

    /**
     * Gets the score value for a shot from the player's current position.
     *
     * @return 3 outside the arc, 2 inside the arc
     */
    public int pointValueForCurrentPosition() {
        return (shotType() == 1) ? 3 : 2;
    }

    /**
     * Checks whether a court coordinate is inside the three-point line.
     *
     * @param playerX player center x-coordinate
     * @param playerY player center y-coordinate
     * @return {@code true} if the coordinate is inside the arc
     */
    public static boolean isInsideThreePointLine(double playerX, double playerY) {
        double distFromHoop = Math.sqrt(Math.pow(playerX - HOOP_X, 2) + Math.pow(playerY - HOOP_Y, 2));
        return distFromHoop <= THREE_POINT_RADIUS;
    }

    /**
     * Sets the player's center x-coordinate from the ImageView layout coordinate.
     *
     * @param pX ImageView layout x-coordinate
     */
    public void setPlayerX(double pX){
        x = pX + 50;        //Staggers the ImageView's x coords so it's not the x coord of the Player's top-left corner
    }

    /**
     * Sets the player's center y-coordinate from the ImageView layout coordinate.
     *
     * @param pY ImageView layout y-coordinate
     */
    public void setPlayerY(double pY){
        y = pY + 80;        //Staggers the ImageView's x coords so it's not the x coord of the Player's top-left corner
    }

    /**
     * Gets the player's center x-coordinate.
     *
     * @return x-coordinate in game space
     */
    public double getPlayerX(){
        return x;
    }

    /**
     * Gets the player's center y-coordinate.
     *
     * @return y-coordinate in game space
     */
    public double getPlayerY(){
        return y;
    }

    /**
     * Gets the player's current score.
     *
     * @return score
     */
    public int getScore(){
        return score;
    }

    /**
     * Classifies the player's current shot by distance from the hoop.
     *
     * @return 1 for three-point, 2 for midrange, 3 for layup
     */
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

    /**
     * Prints the winner when either player reaches 11.
     *
     * @param player1 first player
     * @param player2 second player
     */
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
