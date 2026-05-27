package com.example;

import java.util.Scanner;

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


    public void makePlayer(int pNum, int nba, int s) {
        Player player = new Player(pNum, nba, s);
        if (pNum == 1) {
            player1 = player;
        }
        else if (pNum == 2) {
            player2 = player;
        }
    }

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

    public void selectPlayer() {
        for (int i = 1; i < 3; i++) {
            System.out.println(
                    "Please enter which player you want to use: \n 1: LeBron James \n 2: Stephen Curry \n 3: Giannis Antetokounmpo \n 4: Kevin Durant \n 5: Luka Doncic");
            Scanner scan = new Scanner(System.in);
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

    public int getPossession() {
        if (possession == 1) {
            return 1;
        } else if (possession == 2) {
                return 2;
        }
        return 0;
    }

    public void swapPossession(int possessionState){        //Either Player 1 or 2
        possession = possessionState;
    }

    public double getDistBetwnPlayers() {       //Distance Formula
        double distance = Math.sqrt(Math.pow(player1.getPlayerX() - player2.getPlayerX(), 2) + Math.pow(player1.getPlayerY() - player2.getPlayerY(), 2));
        return distance;
    }

    public double distanceProb(int pNum, int shotType){
        Player p = (pNum == 1) ? player1 : player2;
        int nba = p.getNBA();

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
        if (nba == 3) {
            charBonus += 0.04;                        // Durant still gets a small star boost.
        }

        double accuracy = (base + charBonus) * defenseMultiplier;
        return Math.max(0.05, Math.min(0.92, accuracy));
    }

    //TODO: Incomplete
    public boolean stealProb(int pNum){
        return true;
    }

    //Any other Methods to add?

    public static void main(String[] args) {
        Data bballData = new Data();
        bballData.selectPlayer();
    }
}
