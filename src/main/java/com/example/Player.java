package com.example;

/**
 * Represents one controllable basketball player during a match.
 *
 * <p>A {@code Player} stores the information that belongs to one competitor:
 * player number, selected character, score, and current court position. The
 * JavaFX application owns the visual {@code ImageView}; this model stores the
 * game-space center point that is used for shot zones, scoring, and defender
 * spacing.</p>
 *
 * <p>Shot type values are intentionally simple integers because the rest of the
 * original project already uses this convention:</p>
 *
 * <ul>
 *   <li>{@code 1} means a three-point shot.</li>
 *   <li>{@code 2} means a midrange two-point shot.</li>
 *   <li>{@code 3} means a layup or close shot.</li>
 * </ul>
 *
 * @see Data#distanceProb(int, int)
 * @see FinalProj#shoot(int)
 */
public class Player {
    /**
     * X-coordinate of the hoop/rim in game space.
     *
     * <p>This value is shared by scoring, shot probability, shot animation, and
     * the rendered three-point line. Keeping the hoop coordinate in one place
     * helps the visual court and the scoring rules stay aligned.</p>
     */
    public static final double HOOP_X = 750.0;
    /**
     * Y-coordinate of the hoop/rim in game space.
     *
     * <p>The game uses a top-left origin, so smaller y-values are closer to the
     * top of the screen.</p>
     */
    public static final double HOOP_Y = 90.0;
    /**
     * Radius used to decide whether a shot is inside or outside the three-point line.
     *
     * <p>A player whose center point is farther than this radius from the hoop
     * is treated as shooting a three-pointer. A player inside this radius gets a
     * two-point shot.</p>
     */
    public static final double THREE_POINT_RADIUS = 520.0;
    private static final double LAYUP_RADIUS = 165.0;

    private int pNum;
    private int nbaplayer;
    private int score;
    private double x;
    private double y;

    /**
     * Creates a player model with an initial score and selected character.
     *
     * <p>The constructor does not set a court position. The JavaFX controller
     * calls {@link #setPlayerX(double)} and {@link #setPlayerY(double)} when a
     * round starts or when the player moves.</p>
     *
     * @param n player number, normally {@code 1} or {@code 2}
     * @param nba selected character number from the character-select screen
     * @param s starting score for the match
     * @apiNote Character numbers match the visible selection keys in the game.
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
     * @return player number, normally {@code 1} or {@code 2}
     */
    public int getNum() {
        return pNum;
    }

    /**
     * Gets the selected character number.
     *
     * @return character number from {@code 1} to {@code 5}
     * @see FinalProj#playerNBAImage(int)
     */
    public int getNBA() {
        return nbaplayer;
    }

    /**
     * Updates the selected character number.
     *
     * @param charNum character number from {@code 1} to {@code 5}
     * @apiNote This is mainly useful during character selection before a match
     * starts.
     */
    public void setNBA(int charNum){
        nbaplayer = charNum;
    }

    /**
     * Looks up the static shooting stat for the selected character.
     *
     * <p>The returned value is a character stat, not the final make chance.
     * Final make chance is calculated by {@link Data#distanceProb(int, int)},
     * which combines this kind of stat with distance and defender spacing.</p>
     *
     * @param shotType {@code 1} for three-point, {@code 2} for midrange,
     *                 {@code 3} for layup
     * @return the character's base shooting percentage for that shot type
     * @throws ArrayIndexOutOfBoundsException if the selected character number is
     * outside the supported {@code 1..5} range
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
     * <p>The method first classifies the player's current location using
     * {@link #shotType()}, asks {@link Data#distanceProb(int, int)} for the make
     * probability, and then compares that probability with {@link Math#random()}.
     * If the shot is made, this method immediately adds either two or three
     * points to the player's score.</p>
     *
     * @param data active game state used to calculate shot probability
     * @return {@code true} if the shot went in, otherwise {@code false}
     * @implNote Scoring happens here rather than in {@code FinalProj} so the
     * model remains the source of truth for each player's score.
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
     * Gets the point value for a shot from the player's current position.
     *
     * <p>This method is used by the UI after a make so it can show the correct
     * feedback text, such as {@code SWISH! +3} or {@code BUCKET! +2}.</p>
     *
     * @return {@code 3} outside the arc, {@code 2} inside the arc
     */
    public int pointValueForCurrentPosition() {
        return (shotType() == 1) ? 3 : 2;
    }

    /**
     * Checks whether a court coordinate is inside the three-point line.
     *
     * <p>The coordinate should represent the player's center point, not the
     * ImageView's top-left corner. The visual three-point line in
     * {@link FinalProj} uses the same hoop coordinate and radius.</p>
     *
     * @param playerX player center x-coordinate in game space
     * @param playerY player center y-coordinate in game space
     * @return {@code true} if the coordinate is inside the arc, otherwise
     * {@code false}
     */
    public static boolean isInsideThreePointLine(double playerX, double playerY) {
        double distFromHoop = Math.sqrt(Math.pow(playerX - HOOP_X, 2) + Math.pow(playerY - HOOP_Y, 2));
        return distFromHoop <= THREE_POINT_RADIUS;
    }

    /**
     * Sets the player's center x-coordinate from the ImageView layout coordinate.
     *
     * <p>The JavaFX sprite is positioned by its top-left corner, but gameplay
     * checks need an approximate body center. This method applies a fixed offset
     * so collision, block, and shot-zone calculations use the center of the
     * character.</p>
     *
     * @param pX ImageView layout x-coordinate
     */
    public void setPlayerX(double pX){
        x = pX + 50;        //Staggers the ImageView's x coords so it's not the x coord of the Player's top-left corner
    }

    /**
     * Sets the player's center y-coordinate from the ImageView layout coordinate.
     *
     * <p>The offset matches the visible player sprite height used in the game.
     * Keeping this conversion inside the model prevents every gameplay formula
     * from needing to know about ImageView layout details.</p>
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
     * @return score accumulated during the current match
     */
    public int getScore(){
        return score;
    }

    /**
     * Classifies the player's current shot by distance from the hoop.
     *
     * <p>The method uses two geometric thresholds. Very close shots are layups,
     * shots inside {@link #THREE_POINT_RADIUS} are midrange/two-point attempts,
     * and shots outside that radius are three-point attempts.</p>
     *
     * @return {@code 1} for three-point, {@code 2} for midrange,
     * {@code 3} for layup
     * @see #pointValueForCurrentPosition()
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
     * <p>The JavaFX game uses {@link FinalProj#statsScreen()} for the actual
     * end screen. This method is retained as a simple console helper from the
     * original prototype.</p>
     *
     * @param player1 first player to compare
     * @param player2 second player to compare
     * @apiNote This method writes to standard output and does not modify game
     * state.
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
    /**
     * Minimal console smoke-test entry point for the model.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        Player p1 = new Player(13, 1, 0);

        p1.getNum();
        p1.getNBA();
    }
    
}
