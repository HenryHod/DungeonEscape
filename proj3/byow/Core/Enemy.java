package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.*;

public class Enemy {
    private TileWrapper enemy;
    private LinkedList<TileWrapper> path;
    private MapBuilder builder;

    private static final int enemySight = 10;
    private static class TileWrapper {
        private final int xValue;
        private final int yValue;
        public TileWrapper(Integer x,Integer y) {
            xValue = x;
            yValue = y;
        }

        @Override
        public int hashCode() {
            return (xValue + 1) * (yValue + 1);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != this.getClass()) {
                return false;
            }
            return ((TileWrapper) obj).xValue == this.xValue && ((TileWrapper) obj).yValue == this.yValue;
        }
    }
    private static class DistanceToTarget implements Comparator<TileWrapper> {
        private final TileWrapper player;
        public DistanceToTarget(TileWrapper p) {
            player = p;
        }
        @Override
        public int compare(TileWrapper o1, TileWrapper o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            return (int) ((RoomGraph.distance(o1.xValue, o1.yValue, player.xValue, player.yValue) * 1000) - (RoomGraph.distance(o2.xValue, o2.yValue, player.xValue, player.yValue) * 1000));
        }
    }
    public Enemy(Integer[] startingSpace, MapBuilder b) {
        enemy = new TileWrapper(startingSpace[0], startingSpace[1]);
        builder = b;
    }
    public void move(Integer[] player) {
        double enemyDistance = RoomGraph.distance(player[0], player[1], enemy.xValue, enemy.yValue);
        if (enemyDistance < 10 && inLineOfSight(player) && !contains(player)) {
            TileWrapper playerTile = new TileWrapper(player[0], player[1]);
            path = new LinkedList<>();
            findPath(enemy, playerTile, new ArrayList<>(), path);
            TileWrapper nextSpace = path.removeFirst();
            builder.setTile(nextSpace.xValue, nextSpace.yValue, Tileset.ENEMY);
            builder.setTile(enemy.xValue, enemy.yValue, Tileset.FLOOR);
            enemy = nextSpace;
        }
    }
    private void findPath(TileWrapper currentTile, TileWrapper player, ArrayList<TileWrapper> visitedTiles, LinkedList<TileWrapper> currentPath) {
        if (currentTile.equals(player)) {
            currentPath.add(currentTile);
        } else if (!visitedTiles.contains(currentTile) && RoomGraph.distance(currentTile.xValue, currentTile.yValue, enemy.xValue, enemy.yValue) < 10) {
            int[][] possibleTiles = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
            DistanceToTarget comp = new DistanceToTarget(player);
            TreeSet<TileWrapper> allPossibleTiles = new TreeSet<>(comp);
            visitedTiles.add(currentTile);
            for (int[] tile : possibleTiles) {
                int x = currentTile.xValue + tile[0];
                int y = currentTile.yValue + tile[1];
                if (builder.getTile(x, y).equals(Tileset.AVATAR) || builder.getTile(x, y).equals(Tileset.FLOOR)) {
                    allPossibleTiles.add(new TileWrapper(x, y));
                }
            }
            TileWrapper nextSpace = allPossibleTiles.first();
            currentPath.addLast(nextSpace);
            findPath(nextSpace, player, visitedTiles, currentPath);
        }

    }
    public boolean contains(Integer[] player) {
        return enemy.xValue == player[0] && enemy.yValue == player[1];
    }
    private double angle(int x1, int y1, int x2, int y2) {
            return Math.atan((double)  (y2 - y1) / (x2 - x1));
    }
    private boolean inLineOfSight(Integer[] player) {
        double enemyAngle = angle(enemy.xValue, enemy.yValue, player[0], player[1]);
        for (int radius = 1; radius < Math.min(enemySight, RoomGraph.distance(enemy.xValue, enemy.yValue, player[0], player[1])); radius++) {
            int x = (int) (enemy.xValue + Math.cos(enemyAngle) * radius);
            int y = (int) (enemy.yValue + Math.sin(enemyAngle) * radius);
            if (builder.getTile(x, y).equals(Tileset.NOTHING)) {
                return false;
            }
        }
        return true;
    }
}
