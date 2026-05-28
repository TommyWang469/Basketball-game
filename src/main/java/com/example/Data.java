package com.example;

import java.util.Scanner;

/**
 * Holds match-level game state and shot probability calculations.
 *
 * <p>{@code Data} is the central state object shared by the JavaFX controller
 * and the player models. It owns the two {@link Player} instances, tracks which
 * player currently has possession, and calculates make probability when a shot
 * is attempted.</p>
 *
 * <p>The class intentionally keeps state small: rendering, animations, keyboard
 * input, and round transitions live in {@link FinalProj}. This separation keeps
 * gameplay math testable without starting JavaFX.</p>
 *
 * @see Player
 * @see FinalProj
 */
public class Data {
    private Player player1;
    private Player player2;
    private int possession = 2;

    double[][] shooting_pct = {
            { 1, 0.317, 0.418, 0.752 },
            { 2, 0.393, 0.516, 0.697 },
            { 3, 0.333, 0.311, 0.781 },
            { 4, 0.413, 0.541, 0.764 },
            { 5, 0.366, 0.508, 0.816 }
    };


    /**
     * Creates or replaces one of the two players.
     *
     * <p>This method is used during character selection and when resetting the
     * game. If a player already exists for {@code pNum}, the old instance is
     * replaced with a fresh {@link Player} model.</p>
     *
     * @param pNum player number, {@code 1} or {@code 2}
     * @param nba selected character number from the character-select screen
     * @param s starting score
     * @apiNote Passing any player number other than {@code 1} or {@code 2} has
     * no effect.
     */
    public void makePlayer(int pNum, int nba, int s) {
        Player player = new Player(pNum, nba, s);
        if (pNum == 1) {
            player1 = player;
        }
        else if (pNum == 2) {
            player2 = player;
        }
    }

    /**
     * Gets a player by game number.
     *
     * @param pNum player number, {@code 1} or {@code 2}
     * @return player 1 when {@code pNum == 1}; otherwise player 2
     * @apiNote The current implementation treats every non-1 value as player 2
     * because all runtime callers pass either {@code 1} or {@code 2}.
     */
    public Player getPlayer(int pNum) {
        if (pNum == 1) {
            return player1;
        }
        else {
            return player2;
        }
        /*else {
            return null;
        }*/
    }

    /**
     * Runs console-based character selection for both players.
     *
     * <p>The JavaFX application does not call this method during normal play.
     * It is retained from the original command-line prototype and can still be
     * used for quick manual testing of {@link #makePlayer(int, int, int)}.</p>
     *
     * @apiNote Closing the scanner also closes {@link System#in}. This is fine
     * for this prototype helper, but the JavaFX game should use the on-screen
     * character-select flow instead.
     */
    public void selectPlayer() {
        try (Scanner scan = new Scanner(System.in)) {
            for (int i = 1; i < 3; i++) {
                System.out.println(
                        "Please enter which player you want to use: \n 1: LeBron James \n 2: Stephen Curry \n 3: Giannis Antetokounmpo \n 4: Kevin Durant \n 5: Luka Doncic");
                int selection = scan.nextInt();
                if (selection == 1) {
                    System.out.println("You have chosen LeBron James!");
                    makePlayer(i, 1, 0);
                } else if (selection == 2) {
                    System.out.println("You have chosen Stephen Curry!");
                    makePlayer(i, 2, 0);
                } else if (selection == 3) {
                    System.out.println("You have chosen Giannis Antetokounmpo!");
                    makePlayer(i, 3, 0);
                } else if (selection == 4) {
                    System.out.println("You have chosen Kevin Durant!");
                    makePlayer(i, 4, 0);
                } else if (selection == 5) {
                    System.out.println("You have chosen Luka Doncic!");
                    makePlayer(i, 5, 0);
                }
            }
        }
    }

    /**
     * Gets the player number that currently has the ball.
     *
     * <p>Possession controls which sprite the ball follows in the animation
     * loop and which player is allowed to start a shot when the shoot key is
     * pressed.</p>
     *
     * @return {@code 1} for player 1, {@code 2} for player 2, or {@code 0} if
     * possession is invalid
     */
    public int getPossession() {
        if (possession == 1) {
            return 1;
        } else if (possession == 2) {
                return 2;
        }
        return 0;
    }

    /**
     * Sets possession to a specific player.
     *
     * <p>The JavaFX controller calls this after made shots, misses, blocks, and
     * round resets. The method only changes the stored possession value; moving
     * the visible ball is handled by {@link FinalProj#switchPossession(int, boolean)}
     * and the animation loop.</p>
     *
     * @param possessionState player number that should receive possession
     */
    public void swapPossession(int possessionState){        //Either Player 1 or 2
        possession = possessionState;
    }

    /**
     * Calculates the distance between the two players.
     *
     * <p>The distance uses the center coordinates stored in each {@link Player}.
     * It is used as a defender-spacing factor in {@link #distanceProb(int, int)}:
     * a defender who is closer applies more pressure than a defender who is far
     * away.</p>
     *
     * @return Euclidean distance between player centers, in pixels
     */
    public double getDistBetwnPlayers() {       //Distance Formula
        double distance = Math.sqrt(Math.pow(player1.getPlayerX() - player2.getPlayerX(), 2) + Math.pow(player1.getPlayerY() - player2.getPlayerY(), 2));
        return distance;
    }

    /**
     * Calculates the chance that a shot goes in.
     *
     * <p>Durant, character {@code 3}, gets a special flat {@code 0.90} make
     * chance. All other characters use a formula based on three ingredients:</p>
     *
     * <ol>
     *   <li>Distance from the hoop, where closer shots are easier.</li>
     *   <li>Defender spacing, where more space preserves more of the base chance.</li>
     *   <li>Character shooting stats from {@code shooting_pct}.</li>
     * </ol>
     *
     * <p>The returned number is a probability suitable for comparison with
     * {@link Math#random()}.</p>
     *
     * @param pNum shooting player number, {@code 1} or {@code 2}
     * @param shotType {@code 1} for three-point, {@code 2} for midrange,
     *                 {@code 3} for layup
     * @return make probability between {@code 0.05} and {@code 0.92} for normal
     * players, or exactly {@code 0.90} for Durant
     * @implNote The defensive multiplier bottoms out at {@code 0.70}; this keeps
     * close shots easier than deep shots even when a defender is nearby.
     */
    public double distanceProb(int pNum, int shotType){
        Player p = (pNum == 1) ? player1 : player2;
        int nba = p.getNBA();

        if (nba == 3) {
            return 0.90;
        }

        // Defender pressure: closer defender = smaller multiplier, but it should not erase
        // the main rule that closer shots are easier than deep shots.
        double distBetwnPlayers = getDistBetwnPlayers();
        double defenderSpace = Math.min(1.0, distBetwnPlayers / 300.0);
        double defenseMultiplier = 0.70 + (0.30 * defenderSpace);

        // ===== Closer to the basket = higher chance =====
        // Distance from the rim, matching the scoring boundary in Player.
        double distFromNet = Math.sqrt(Math.pow(p.getPlayerX() - Player.HOOP_X, 2)
                                     + Math.pow(p.getPlayerY() - Player.HOOP_Y, 2));

        // Map distance to a base accuracy. Smaller distance = higher %.
        // Tuned so layups feel automatic and deep 3s feel rare.
        double base;
        if (distFromNet < 150)        base = 0.85;   // layup / point-blank
        else if (distFromNet < 280)   base = 0.62;   // close midrange
        else if (distFromNet < 420)   base = 0.45;   // long 2 / short 3
        else if (distFromNet < 560)   base = 0.32;   // standard 3
        else                          base = 0.22;   // deep 3

        // Small per-character variance from the static shooting %s.
        // shotType: 1=3pt, 2=midrange, 3=layup -> column in the table
        double charStat = shooting_pct[nba - 1][shotType];
        double charBonus = (charStat - 0.40) * 0.5;   // ±0.05ish swing around the base

        double accuracy = (base + charBonus) * defenseMultiplier;
        return Math.max(0.05, Math.min(0.92, accuracy));
    }

    /**
     * Placeholder for an older steal mechanic.
     *
     * <p>The current live game uses timed block attempts in {@link FinalProj}
     * instead of this method. It remains here only so older prototype code and
     * notes still have a documented landing point.</p>
     *
     * @param pNum defending player number
     * @return always {@code true}; the live game now uses timed block attempts instead
     * @deprecated the active game uses {@link FinalProj#playerMoves()} and its
     * timed block flow instead of steals
     */
    @Deprecated
    public boolean stealProb(int pNum){
        return true;
    }

    /**
     * Console entry point for the prototype selection flow.
     *
     * @param args command-line arguments, not used
     */
    public static void main(String[] args) {
        Data bballData = new Data();
        bballData.selectPlayer();
    }
}
