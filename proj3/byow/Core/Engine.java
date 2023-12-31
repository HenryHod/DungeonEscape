package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.algs4.StdDraw;
import edu.princeton.cs.algs4.Stopwatch;



public class Engine {
    /* Feel free to change the width and height. */
    TERenderer ter = new TERenderer();
    public static final int WIDTH = 80;
    public static final int HEIGHT = 80;
    private boolean quitGame = false;
    private boolean gameOver = false;
    private MapBuilder builder = null;
    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        builder = new MapBuilder(this, WIDTH, HEIGHT);
        StringBuilder playString = new StringBuilder();
        Stopwatch stopwatch = new Stopwatch();
        builder.initializeGUI();
        while (!quitGame) {
            if (StdDraw.hasNextKeyTyped()) {
                Character nextChar = Character.toLowerCase(StdDraw.nextKeyTyped());
                playString = builder.interactWithUserInput(playString, nextChar);
            }
            if (builder.mapCreated() && !gameOver) {
                builder.updateEnemies(stopwatch.elapsedTime());
            }
            if (gameOver) {
                builder.finalScreen(stopwatch.elapsedTime());
                stopwatch = new Stopwatch();
                playString = new StringBuilder();
            }
        }
        System.exit(0);
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, running both of these:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.
        input = input.toLowerCase();
        if (builder == null) {
            builder = new MapBuilder(this, WIDTH, HEIGHT);
        }
        return builder.interactWithPlayString(input);
    }
    public void setGameOver(boolean b) {
        this.gameOver = b;
    }

    public void setQuitGame(boolean b) {
        this.quitGame = b;
    }
}
