package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

public class Player {
    private String playerKeys = "";
    private Integer[] currentSpace;
    private int numKeysToFind;
    private final MapBuilder builder;
    public Player(int numKeys, MapBuilder b, Integer[] startingSpace) {
        numKeysToFind = numKeys;
        builder = b;
        currentSpace = startingSpace;
    }
    public boolean movePlayer(char move) {
        playerKeys += move;
        boolean returnValue = false;
        char[] keys = new char[]{'w', 's', 'd', 'a'};
        int[][] moves = new int[][]{{0, 1}, {0, -1}, {1, 0}, {-1, 0}};
        Integer[] nextSpace = currentSpace.clone();
        for (int i  = 0; i < keys.length; i++) {
            if (move == keys[i]) {
                nextSpace[0] += moves[i][0];
                nextSpace[1] += moves[i][1];
            }
        }
        TETile nextSpaceMapping = builder.getTile(nextSpace[0], nextSpace[1]);
        if (!nextSpaceMapping.equals(Tileset.WALL) && !nextSpaceMapping.equals(Tileset.LOCKED_DOOR)) {
            builder.setHudOutput(builder.getnumEnemies() + " ENEMIES NEARBY!");
            if (nextSpaceMapping.equals(Tileset.KEY)) {
                numKeysToFind -= 1;
                if (numKeysToFind == 0) {
                    builder.unlockDoor();
                    builder.setHudOutput("DOOR IS NOW UNLOCKED!");
                } else {
                    builder.setHudOutput(numKeysToFind + " KEYS REMAINING");
                }
            }
            if (nextSpaceMapping.equals(Tileset.UNLOCKED_DOOR) || nextSpaceMapping.equals(Tileset.ENEMY)) {
                builder.setGameOver(true);
            }
            builder.setTile(nextSpace[0], nextSpace[1], Tileset.AVATAR);
            builder.setTile(currentSpace[0], currentSpace[1], Tileset.FLOOR);
            returnValue = true;
            currentSpace = nextSpace;
        }
        if (nextSpaceMapping.equals(Tileset.LOCKED_DOOR)) {
            builder.setHudOutput("THE DOOR IS LOCKED");
        }
        return returnValue;
    }
    public Integer[] getCurrentSpace() {
        return currentSpace.clone();
    }
    public int getNumKeysToFind() {
        return numKeysToFind;
    }
    public String getPlayerKeys() {return playerKeys;}
}
