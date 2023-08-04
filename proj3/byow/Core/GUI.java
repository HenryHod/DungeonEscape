package byow.Core;

import edu.princeton.cs.algs4.StdDraw;

import java.awt.*;

public class GUI {
    private final int WIDTH;
    private final int HEIGHT;
    private String hudOutput;
    public GUI(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
    }
    public void addGUI() {
        StdDraw.setCanvasSize(WIDTH / 2 * 16, WIDTH / 2 * 16);
        Font font = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(font);
        StdDraw.setXscale(0, WIDTH / 2 + 1);
        StdDraw.setYscale(0, HEIGHT / 2 + 1);
        StdDraw.clear(Color.BLACK);
        StdDraw.enableDoubleBuffering();

    }
    public void addText(String str) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 8,HEIGHT / 8, str);
        StdDraw.show();
    }
    public void addStartMenu() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.setXscale(0, WIDTH / 2 + 1);
        StdDraw.setYscale(0, HEIGHT / 2 + 1);
        StdDraw.text(WIDTH / 4, 4 * HEIGHT / 10, "ESCAPE ROOM");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 4, 3 * HEIGHT / 10, "New Game (Press N)");
        StdDraw.text(WIDTH / 4, 5 * HEIGHT / 20, "Load Game (Press L)");
        StdDraw.text(WIDTH / 4, 2 * HEIGHT / 10, "Quit Game (Press Q)");
        StdDraw.show();
    }
    public void addDifficultyGUI() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 4, 4 * HEIGHT / 10, "CHOOSE DIFFICULTY");
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 4, 3 * HEIGHT / 10, "EASY (Press E)");
        StdDraw.text(WIDTH / 4, 5 * HEIGHT / 20, "MEDIUM (Press M)");
        StdDraw.text(WIDTH / 4, 2 * HEIGHT / 10, "HARD (Press H)");
        StdDraw.show();
    }
    public void addSeedInput(String str) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 4, 4 * HEIGHT / 10, "TYPE SEED NUMBER");
        StdDraw.text(WIDTH / 4,HEIGHT / 4, str);
        if (str.length() > 0) {
            Font fontSmall = new Font("Monaco", Font.BOLD, 20);
            StdDraw.setFont(fontSmall);
            StdDraw.text(WIDTH / 4, 3 * HEIGHT / 10, "(Press S When Finished)");
        }
        StdDraw.show();
    }
    public void addGameHUD(int keys, int maxKeys, double time) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(fontSmall);
        StdDraw.text( WIDTH / 4 - 1, HEIGHT / 4, "Keys: " +  keys + " / " + maxKeys);
        StdDraw.text(WIDTH / 8, HEIGHT / 4, hudOutput);
        StdDraw.text(2, HEIGHT / 4, time + " ");
        StdDraw.line(0, HEIGHT / 4 - 1, WIDTH / 4 + 1, HEIGHT / 4 - 1);
    }
    public void gameWon(double time) {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setXscale(0, WIDTH / 2 + 1);
        StdDraw.setYscale(0, HEIGHT / 2 + 1);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 4,4 * HEIGHT / 10, "YOU ESCAPED in " + Math.round(time) + "s!");
        StdDraw.setFont(fontSmall);
        StdDraw.text(WIDTH / 4, 3 * HEIGHT / 10, "Main Menu (Press M)");
        StdDraw.text(WIDTH / 4, 5 * HEIGHT / 20, "Quit Game (Press Q)");
        StdDraw.show();
    }
    public void gameLost() {
        StdDraw.clear(Color.BLACK);
        StdDraw.setPenColor(Color.WHITE);
        Font fontBig = new Font("Monaco", Font.BOLD, 30);
        Font fontSmall = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setXscale(0, WIDTH / 2 + 1);
        StdDraw.setYscale(0, HEIGHT / 2 + 1);
        StdDraw.setFont(fontBig);
        StdDraw.text(WIDTH / 4,4 * HEIGHT / 10, "GAME OVER");
        StdDraw.setFont(fontSmall);
        StdDraw.text( WIDTH / 4, 3 * HEIGHT / 10, "Restart Game (Press R)");
        StdDraw.text(WIDTH / 4, 5 * HEIGHT / 20, "Main Menu (Press M)");
        StdDraw.text(WIDTH / 4, 2 * HEIGHT / 10, "Quit Game (Press Q)");
        StdDraw.show();
    }

    public void setHudOutput(String output) {
        this.hudOutput = output;
    }
}
