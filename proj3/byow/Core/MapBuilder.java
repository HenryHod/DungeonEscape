package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static byow.Core.RandomUtils.gaussian;
import static byow.Core.RandomUtils.uniform;

public class MapBuilder {
    private final Engine engine;
    private String possibleKeys = "nlq";
    private static int WIDTH;
    private static int HEIGHT;
    private TETile[][] map = null;
    private int numKeys;
    private Player player;
    private ArrayList<Enemy> enemies;
    private double enemyCooldown = 2.0;
    private Integer[] door;
    private int playerMoves = 0;
    private int enemyMoves = 0;
    private GUI gui;
    private String mapString = "";
    public MapBuilder(Engine e, int w, int h) {
        engine = e;
        WIDTH = w;
        HEIGHT = h;
    }
    public StringBuilder interactWithUserInput(StringBuilder playString, Character nextChar) {

        if ((playString.toString().matches("(n[emh]?[0-9]+)") && nextChar.equals('s')) | nextChar.equals('l')) {
            playString.append(nextChar);
            mapString = String.valueOf(playString);
            engine.interactWithInputString(String.valueOf(playString));
            gui.setHudOutput(enemies.size() + " ENEMIES NEARBY!");
            engine.ter.initialize(WIDTH, HEIGHT, player.getCurrentSpace());
            possibleKeys = ":qwasd";
        } else if (playString.isEmpty() && possibleKeys.contains("" + nextChar)) {
            playString.append(nextChar);
            if (nextChar.equals('n')) {;
                possibleKeys = "emh";
                gui.addDifficultyGUI();
            } else if (nextChar.equals('q')) {
                engine.setQuitGame(true);
            } else if (nextChar.equals('m')) {
                map = null;
                gui.addStartMenu();
                possibleKeys = "nlq";
                engine.setGameOver(false);
                playString = new StringBuilder();
            } else if (nextChar.equals('r')) {
                engine.setGameOver(false);
                interactWithPlayString(mapString);
                engine.ter.initialize(WIDTH, HEIGHT, player.getCurrentSpace());
                engine.ter.renderFrame(map, player.getCurrentSpace());
                playString = new StringBuilder(mapString);
                possibleKeys = ":qwasd";
            }
        } else if (playString.toString().equals("n") && possibleKeys.contains("" + nextChar)) {
            playString.append(nextChar);
            gui.addSeedInput(playString.substring(2));
            possibleKeys = "";
        } else if (playString.toString().matches("(n[emh].*)") && Character.isDigit(nextChar)) {
            playString.append(nextChar);
            gui.addSeedInput(playString.substring(2));
        } else if (possibleKeys.contains("" + nextChar)) {
            playString.append(nextChar);
            if (map != null) {
                if (nextChar.equals(':')) {
                    gui.setHudOutput("Press Q to Quit");
                } else if (playString.toString().contains(":q")) {
                    saveGame(mapString + player.getPlayerKeys());
                    playString = new StringBuilder();
                    possibleKeys = "nql";
                } else {
                    moveAllObjects(nextChar);
                    engine.ter.renderFrame(map, player.getCurrentSpace());
                }
            }
        }
        return playString;
    }
    public TETile[][] interactWithPlayString(String input) {

        Integer seed = null;
        StringBuilder seedString = new StringBuilder();
        int movesIndex = 0;
        int numRooms = 20;
        numKeys = 2;
        int roomSize = 12;
        int numEnemies = 2;
        ArrayList<Character> playerKeys =  new ArrayList<>();
        for (int i = 0; i < input.length(); i++) {
            char nextChar = input.charAt(i);
            if (input.matches("n[emh]?[0-9]+s.*")) {
                if (i == 1 && !Character.isDigit(nextChar)) {
                    if (nextChar == 'e') {
                        enemyCooldown = 3.00;
                        numRooms = 10;
                        roomSize = 15;
                        numKeys = 1;
                        numEnemies = 1;
                    } else if (nextChar == 'h') {
                        enemyCooldown = 1.50;
                        numRooms = 30;
                        roomSize = 8;
                        numKeys = 3;
                        numEnemies = 3;
                    }
                }
                if (Character.isDigit(nextChar)) {
                    seedString.append(nextChar);
                    if (input.charAt(i + 1) == 's') {
                        movesIndex = i + 2;
                }
            }
            else if (movesIndex != 0 && movesIndex <= i && !":q".contains("" + nextChar)) {
                playerKeys.add(nextChar);
            }
            } else if (nextChar == 'l') {
                String saveString = readSave();
                Matcher matcher = Pattern.compile("(n[emh]?[0-9]+s)([:qwasd]*)").matcher(saveString);
                matcher.find();
                mapString = matcher.group();
                input = saveString + input.substring(i + 1);
            }
        }
        if (!seedString.toString().equals("")) {
            seed = Integer.parseInt(seedString.toString());
        }
        map = new TETile[WIDTH][HEIGHT];
        if (seed != null) {
            Random random = new Random(seed);
            initializeTiles(random, numRooms, roomSize, numKeys, numEnemies);
        }
        for (Character move: playerKeys) {
            moveAllObjects(move);
        }
        if (input.contains(":q")) {
            saveGame(input.substring(0, input.length() - 2));
            possibleKeys = "nql";
        }
        engine.ter.initializeFull(WIDTH, HEIGHT);
        setTile(player.getCurrentSpace()[0], player.getCurrentSpace()[1], Tileset.AVATAR);
        return map;
    }
    private void initializeTiles(Random random, int numRooms, int maxRoomSize, int numKeys, int numEnemies) {
        RoomGraphDisjointSet rGraphSet = new RoomGraphDisjointSet();
        for (int x = 0; x < WIDTH; x++) {
            for(int y = 0; y < HEIGHT; y++) {
                map[x][y] = Tileset.NOTHING;
            }
        }
        int numNewRooms = 0;
        while (numNewRooms < numRooms ) {
            int randomX = (int) gaussian(random, (double) WIDTH / 2, (double) WIDTH / 8);
            int randomY = (int) gaussian(random, (double) HEIGHT / 2, (double) HEIGHT / 8);
            int randomWidth = uniform(random,2, maxRoomSize);
            int randomHeight = uniform(random,2, maxRoomSize);
            if (roomOverlapChecker(randomX, randomY, randomWidth, randomHeight)) {
                RoomGraph rGraph = new RoomGraph(new ArrayList<>());
                createRoom(rGraph, randomX, randomY, randomWidth, randomHeight);
                rGraphSet.addGraph(rGraph);
                numNewRooms++;
            }
        }
        if (numRooms > 0) {
            rGraphSet.addConnections(this);
            addPlayer(rGraphSet);
            addDoor(rGraphSet);
            rGraphSet.getFinal().setDistances();
            addKeys(numKeys, rGraphSet);
            addEnemies(rGraphSet, numEnemies);
        }
    }
    private void createRoom(RoomGraph roomGraph, int startingX, int startingY, int roomWidth, int roomHeight) {
        int leftEdge = startingX - roomWidth / 2;
        int rightEdge = startingX + roomWidth / 2;
        int bottomEdge = startingY - roomHeight / 2;
        int topEdge = startingY + roomHeight / 2;
        ArrayList<Integer[]> wallSpaces = new ArrayList<>();
        for (int x = leftEdge; x <= rightEdge; x++) {
            for (int y = bottomEdge; y <= topEdge; y++) {
                if ((x == leftEdge) | (x == rightEdge) | (y == bottomEdge) | (y == topEdge)) {
                    map[x][y] = Tileset.WALL;
                    if (!((x == leftEdge | x == rightEdge) && (y == bottomEdge | y == topEdge))) {
                        wallSpaces.add(new Integer[]{x, y});
                    }
                    continue;
                }
                map[x][y] = Tileset.FLOOR;
            }
        }
        roomGraph.addRoom(startingX, startingY, wallSpaces);
    }
    public static boolean coordinateBoundsChecker(int x, int y) {
        return !((y < 0) | (y >= HEIGHT)) && !((x < 0) | (x >= WIDTH));
    }
    private boolean roomOverlapChecker(int startingX, int startingY, int width, int height) {
        int[] verticalEdges = new int[]{startingX - width / 2 - 1, startingX + width / 2 + 1};
        int[] horizontalEdges = new int[]{startingY - height / 2 - 1, startingY + height / 2 + 1};
        for (int x: verticalEdges) {
            for (int y = horizontalEdges[0]; y <= horizontalEdges[1]; y++ ) {
                if (!coordinateBoundsChecker(x, y)) {
                    return false;
                } else if ((map[x][y].equals(Tileset.WALL)) | (map[x][y].equals(Tileset.FLOOR))) {
                    return false;
                }
            }
        }
        for (int y: horizontalEdges) {
            for (int x = verticalEdges[0]; x <= verticalEdges[1]; x++ ) {
                if (!coordinateBoundsChecker(x, y)) {
                    return false;
                } else if ((map[x][y].equals(Tileset.WALL)) | (map[x][y].equals(Tileset.FLOOR))) {
                    return false;
                }
            }
        }
        return map[startingX][startingY].equals(Tileset.NOTHING);
    }
    private void addKeys(int maxKeys, RoomGraphDisjointSet rGraphSet) {
        for (RoomGraph rGraph: rGraphSet) {
            rGraph.insertKeys(this, maxKeys);
        }

    }
    private void addPlayer(RoomGraphDisjointSet rGraphSet) {
        RoomGraph rGraph = rGraphSet.getFinal();
        Integer[] centerSpace = rGraph.closestToCenter();
        map[centerSpace[0]][centerSpace[1]] = Tileset.AVATAR;
        player = new Player(numKeys, this, centerSpace);
    }
    private void addEnemies(RoomGraphDisjointSet rGraphSet, int numEnemies) {
        enemies = new ArrayList<>();
        RoomGraph rGraph = rGraphSet.getFinal();
        Integer[][] startingSpaces = rGraph.enemyStart(numEnemies);
        for (Integer[] space: startingSpaces) {
            enemies.add(new Enemy(space, this));
            map[space[0]][space[1]] = Tileset.ENEMY;
        }
    }
    private void addDoor(RoomGraphDisjointSet rGraphSet) {
        door = rGraphSet.getFinal().doorSpace();
        map[door[0]][door[1]] = Tileset.LOCKED_DOOR;
    }
    public void moveEnemies() {
        for (Enemy enemy: enemies) {
            enemy.move(player.getCurrentSpace());
        }
        if (enemies.stream().anyMatch(enemy -> enemy.contains(player.getCurrentSpace()))) {
            engine.setGameOver(true);
        }
    }
    public boolean mapCreated() {
        return map != null;
    }
    public void updateEnemies(double time) {
        gui.addGameHUD(numKeys - player.getNumKeysToFind(), numKeys, time);
        if (time == enemyCooldown * enemyMoves) {
            moveEnemies();
            enemyMoves++;
        }
        engine.ter.renderFrame(map, player.getCurrentSpace());
    }
    public void initializeGUI() {
        gui = new GUI(WIDTH, HEIGHT);
        gui.addGUI();
        gui.addStartMenu();
    }
    public void finalScreen(double time) {
        if (player.getNumKeysToFind() == 0 && enemies.stream().noneMatch(enemy -> enemy.contains(player.getCurrentSpace()))) {
            gui.gameWon(time);
        } else {
            gui.gameLost();
        }
        possibleKeys = "mqr";
    }
    public int getnumEnemies() {
        return enemies.size();
    }
    public void setHudOutput(String output) {
        gui.setHudOutput(output);
    }
    public void setGameOver(boolean b) {
        engine.setGameOver(b);
    }
    public void unlockDoor() {
        map[door[0]][door[1]] = Tileset.UNLOCKED_DOOR;
    }
    public TETile getTile(int x, int y) {
        return map[x][y];
    }
    public void setTile(int x, int y, TETile newTile) {
        map[x][y] = newTile;
    }
    private void saveGame(String playString) {
        try (PrintWriter out = new PrintWriter("SavedGames.txt")) {
            out.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        map = null;
        gui.addStartMenu();
    }
    private String readSave() {
        String content;
        try {
            content = Files.readString(Path.of("SavedGames.txt")).replace("\n","").replace("\r", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return content;
    }
    private void moveAllObjects(char nextChar) {
        boolean playerMoved = player.movePlayer(nextChar);
        engine.ter.update(player.getCurrentSpace());
        if (playerMoves % enemyCooldown == 0.0 && playerMoved) {
            moveEnemies();
            enemyMoves++;
        }
        playerMoves++;
    }
}
