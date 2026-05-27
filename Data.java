package com.example;
import java.util.Scanner;

public class Data {
    private Player player1;
    private Player player2;

    double[][] shooting_pct = {
            { 1, 0.317, 0.418, 0.752 },
            { 2, 0.393, 0.516, 0.697 },
            { 3, 0.333, 0.311, 0.781 },
            { 4, 0.413, 0.541, 0.764 },
            { 5, 0.366, 0.508, 0.816 }
    };


    public void makePlayer(int pNum, int nba, int s) {
        Player player = new Player(pNum, nba, s, 700, 700);
        if (pNum == 1) {
            player1 = player;
            player1.setY(600);
        }
        else if (pNum == 2) {
            player2 = player;
        }
    }

    public Player getPlayer(int pNum) {
        if (pNum == 1) {
            return player1;
        }
        else if (pNum == 2) {
            return player2;
        }
        else {
            return null;
        }
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
    int possession = 2;
    public int getPossession() {
        if (possession == 2) {
            if (getPlayer(2).shoot == true) {
                possession = 1;
                return 1;
            }
            else {
                return 2;
            }
        }
        else {
            if (getPlayer(1).shoot == true) {
                possession = 2;
                return 2;
            }
            else {
                return 1;
            }
        }
    }

    public double getDistance(Player player1, Player player2) {
        double distance = Math.sqrt(Math.pow(player1.getX() - player2.getX(), 2) + Math.pow(player1.getY() - player2.getY(), 2));
        return distance;
    }

    public double distanceProb(Player player1, Player player2) {
        if (getDistance(player1, player2) < 100) {
            return 0.5;
        }
        else if (getDistance(player1, player2) < 200) {
            return 0.75;
        }
        else {
            return 1.0;
        }
    }

    public static void main(String[] args) {
        Data bballData = new Data();
        bballData.selectPlayer();
    }
}