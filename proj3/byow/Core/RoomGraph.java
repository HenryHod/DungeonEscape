package byow.Core;


import byow.TileEngine.Tileset;


import java.util.*;
import java.util.stream.Stream;

public class RoomGraph { //overlying class that makes the game board
    private ArrayList<RoomGraphNode> nodes; // a list with all the room(RoomGraphNodes) in it
    private RoomGraphNode centerRoom;


    private static class RoomGraphNode { //a private class that constructs a room
        boolean containsKey = false;
        int xValue; // x coordinate for center of room
        int yValue; //y coordinate for center of room
        ArrayList<Integer[]> wallSpaces; //x, y coordinate for each wall coordinate for a room
        ArrayList<RoomGraphNode> roomEdges;
        private int distance = 0;

        private RoomGraphNode(int x, int y, ArrayList<Integer[]> spaces, ArrayList<RoomGraphNode> edges) {
            xValue = x;
            yValue = y;
            wallSpaces = spaces;
            roomEdges = edges;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            } else if (obj.getClass() != this.getClass()) {
                return false;
            } else return !(this.xValue != ((RoomGraphNode) obj).xValue | this.yValue != ((RoomGraphNode) obj).yValue);
        }
    }
    private static class CenterDistance implements Comparator<RoomGraphNode> {

        @Override
        public int compare(RoomGraphNode o1, RoomGraphNode o2) {
            return o2.distance - o1.distance;
        }
    }
    private static class TileDistance implements Comparator<RoomGraphNode> {
        private final Integer[] tile;
        public TileDistance(Integer[] t) {
            tile = t;
        }
        @Override
        public int compare(RoomGraphNode o1, RoomGraphNode o2) {
            return (int) (distance(o1.xValue, o1.yValue, tile[0], tile[1]) * 1000 - distance(o2.xValue, o2.yValue, tile[0], tile[1]) * 1000);
        }
    }
    private static class RoomDistance implements Comparator<RoomGraphNode> {
        //Comparator used to compare distances of rooms to a room
        RoomGraphNode room;

        public RoomDistance(RoomGraphNode node) {
            room = node;
        }

        @Override
        public int compare(RoomGraphNode o1, RoomGraphNode o2) {
            if (o1 == room) {
                return (int) Double.POSITIVE_INFINITY;
            } else if (o2 == room) {
                return (int) Double.NEGATIVE_INFINITY;
            }
            return (int) (distance(o1.xValue, o1.yValue, room.xValue, room.yValue) * 1000 - distance(o2.xValue, o2.yValue, room.xValue, room.yValue) * 1000);
        }
    }
    private static class KeyDistance implements Comparator<RoomGraphNode> {
        //Comparator used to find the farthest key from the center of map and other keys
        private final ArrayList<RoomGraphNode> keys;
        public KeyDistance(ArrayList<RoomGraphNode> k) {
            keys = k;
        }

        @Override
        public int compare(RoomGraphNode o1, RoomGraphNode o2) {
            int distance1 = o1.distance;
            int distance2 = o2.distance;
            if (keys.contains(o1)) {
                return (int) Double.POSITIVE_INFINITY;
            } else if (keys.contains(o2)) {
                return (int) Double.NEGATIVE_INFINITY;
            }
            for (RoomGraphNode key: keys) {
                distance1 += distance(o1.xValue, o1.yValue, key.xValue, key.yValue);
                distance2 += distance(o2.xValue, o2.yValue, key.xValue, key.yValue);
            }
            return distance2 * 1000 - distance1 * 1000;
        }
    }

    private static class SpaceDistance implements Comparator<Integer[]> {
        //Comparator for comparing distances to end of hallway weighted by the number of turns it takes to get there
        int numTurns;
        Integer[] currentSpace;
        Integer[] end;
        boolean horizontal;
        boolean vertical;

        public SpaceDistance(Integer[] space, Integer[] lastSpace, Integer[] endSpace, int turns) {
            currentSpace = space;
            end = endSpace;
            horizontal = Math.abs(lastSpace[0] - currentSpace[0]) == 0;
            vertical = Math.abs(lastSpace[1] - currentSpace[1]) == 0;
            numTurns = turns;
        }

        @Override
        public int compare(Integer[] o1, Integer[] o2) {
            int turn1 = 0;
            int turn2 = 0;
            double distance1 = distance(o1[0], o1[1], end[0], end[1]);
            double distance2 = distance(o2[0], o2[1], end[0], end[1]);
            if (horizontal) {
                turn1 += Math.abs(o1[1] - end[1]);
                turn2 += Math.abs(o2[1] - end[1]);
            }
            if (vertical) {
                turn1 += Math.abs(o1[0] - end[0]);
                turn2 += Math.abs(o1[0] - end[0]);
            }
            return (int) ((distance1 * Math.pow(numTurns + 1, 2 + turn1)) * 1000 - (distance2 * Math.pow(numTurns + 1, 2 + turn2)) * 1000);
        }
    }

    public RoomGraph(ArrayList<RoomGraphNode> n) {
        nodes = n; //making an array that will hold every room on the board
    }

    public int size() {
        return nodes.size();
    }

    public void addRoom(int x, int y, ArrayList<Integer[]> spaces) { // literally does exactly what the title of the function is
        nodes.add(new RoomGraphNode(x, y, spaces, new ArrayList<>()));
    }

    public void addAllRooms(RoomGraph otherGraph) {
        nodes.addAll(otherGraph.nodes);
    }

    private Integer[] closestWall(int x, int y, RoomGraphNode otherRoom) { //takes in two rooms then finds the closest wall from the center of the current-room variable
        double minWallDistance = 2 * Engine.WIDTH;
        Integer[] wallToReturn = null;
        for (Integer[] wall : otherRoom.wallSpaces) {
            double currentDistance = distance(x, y, wall[0], wall[1]);
            if (currentDistance < minWallDistance) {
                minWallDistance = currentDistance;
                wallToReturn = wall;
            }
        }
        return wallToReturn;
    }

    private ArrayList<Integer[]> findClosestSpaces(RoomGraphNode room, RoomGraphDisjointSet rGraphSet) {  // passing in a room and utilizes all the functions above
        RoomGraphNode closestRoom = rGraphSet.stream().filter(rGraph -> rGraph != this).map(roomGraph -> roomGraph.nodes.stream().min(new RoomDistance(room))).filter(Optional::isPresent).map(Optional::get).min(new RoomDistance(room)).get();
        room.roomEdges.add(closestRoom);
        closestRoom.roomEdges.add(room);
        rGraphSet.connect(this, rGraphSet.stream().filter(rGraph -> rGraph.contains(closestRoom)).toList().get(0));
        ArrayList<Integer[]> arrToReturn = new ArrayList<>();
        Integer[] otherRoomWall = closestWall(room.xValue, room.yValue, closestRoom);
        assert otherRoomWall != null;
        arrToReturn.add(closestWall(otherRoomWall[0], otherRoomWall[1], room));
        arrToReturn.add(otherRoomWall);
        room.wallSpaces.remove(arrToReturn.get(0));
        closestRoom.wallSpaces.remove(arrToReturn.get(1));
        return arrToReturn;

    }

    public void addHallways(MapBuilder builder, RoomGraphDisjointSet rGraphSet) {
        ArrayList<RoomGraphNode> nodesCopy = (ArrayList<RoomGraphNode>) nodes.clone();
        for (RoomGraphNode node : nodesCopy) {
            if (!rGraphSet.isAllConnected()) {
                ArrayList<Integer[]> hallwayEndpoints = findClosestSpaces(node, rGraphSet);

                Integer[] hallwayStart = hallwayEndpoints.get(0);
                Integer[] hallwayEnd = hallwayEndpoints.get(1);

                builder.setTile(hallwayEnd[0], hallwayEnd[1], Tileset.FLOOR);
                builder.setTile(hallwayStart[0], hallwayStart[1], Tileset.FLOOR);

                Integer[] currentSpace = hallwayStart.clone();
                Integer[] lastSpace = currentSpace;
                int numTurns = 0;
                ArrayList<Integer[]> hallwaySpaces = new ArrayList<>();
                hallwaySpaces.add(hallwayStart);
                while (!Objects.equals(currentSpace[0], hallwayEnd[0]) | !Objects.equals(currentSpace[1], hallwayEnd[1])) {
                    if (hallwaySpaces.size() > 1) {
                        lastSpace = hallwaySpaces.get(hallwaySpaces.size() - 1);
                    }
                    Integer[] nextSpace = nextClosestSpace(currentSpace, lastSpace, hallwayEnd, numTurns);
                    if ((Math.abs(lastSpace[0] - nextSpace[0]) < 2 && Objects.equals(lastSpace[1], nextSpace[1])) || (Math.abs(lastSpace[1] - nextSpace[1]) < 2) && lastSpace[1].equals(nextSpace[1])) {
                        numTurns++;
                    }
                    List<RoomGraphNode> coveredWallSpaces = nodes.stream().filter(room -> room.wallSpaces.contains(nextSpace)).toList();
                    if (coveredWallSpaces.size() > 0) {
                        coveredWallSpaces.forEach(room -> room.wallSpaces.remove(nextSpace));
                    }
                    currentSpace = nextSpace;
                    builder.setTile(currentSpace[0], currentSpace[1], Tileset.FLOOR);

                    hallwaySpaces.add(currentSpace);
                }

                addHallwayWalls(builder, hallwaySpaces);
            }
        }
    }

    private static Integer[] nextClosestSpace(Integer[] currentSpace, Integer[] lastSpace, Integer[] endSpace, int numTurns) {
        int[][] possibleTiles = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};
        SpaceDistance comp = new SpaceDistance(currentSpace, lastSpace, endSpace, numTurns);
        TreeSet<Integer[]> possibleSpaces = new TreeSet<>(comp);
        for (int[] tile : possibleTiles) {
            if (MapBuilder.coordinateBoundsChecker(currentSpace[0] + tile[0], currentSpace[1] + tile[1])) {
                possibleSpaces.add(new Integer[]{currentSpace[0] + tile[0], currentSpace[1] + tile[1]});
            }
        }
        return possibleSpaces.first();
    }

    private void addHallwayWalls(MapBuilder builder, ArrayList<Integer[]> hallway) {
        int[][] possibleTiles = new int[][]{{1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}};
        for (Integer[] currentSpace : hallway) {
            for (int[] tile: possibleTiles) {
                int x = currentSpace[0] + tile[0];
                int y = currentSpace[1] + tile[1];
                if (MapBuilder.coordinateBoundsChecker(x, y)) {
                    if (builder.getTile(x, y).equals(Tileset.NOTHING)) {
                        builder.setTile(x, y, Tileset.WALL);
                    }
                }
            }

        }
    }

    public Integer[] closestToCenter() {
        Optional<RoomGraphNode> possible = nodes.stream().filter(node -> node.wallSpaces.size() > 0).min(new TileDistance(new Integer[]{Engine.WIDTH / 2, Engine.HEIGHT / 2}));
        possible.ifPresent(roomGraphNode -> centerRoom = roomGraphNode);
        return new Integer[]{centerRoom.xValue, centerRoom.yValue};
    }

    public static double distance(int x1, int y1, int x2, int y2) {
        return Math.pow(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2), 0.5);
    }

    public void insertKeys(MapBuilder builder, int maxKeys) {
        int numKeys = 0;
        ArrayList<RoomGraphNode> keys = new ArrayList<>();
        while (numKeys < maxKeys) {
            RoomGraphNode keyRoom = nodes.stream().min(new KeyDistance(keys)).get();
            keyRoom.containsKey = true;
            builder.setTile(keyRoom.xValue, keyRoom.yValue, Tileset.KEY);
            keys.add(keyRoom);
            numKeys++;
        }
    }
    public Integer[][] enemyStart(int numEnemies) {
        List<RoomGraphNode> keys = nodes.stream().filter(room -> room.containsKey && room != centerRoom).toList();
        Integer[][] enemies = new Integer[numEnemies][2];
        int i = 0;
        for (RoomGraphNode key: keys) {
            RoomGraphNode enemyRoom = closestEdge(key);
            if (i < numEnemies ) {
                enemies[i] = new Integer[]{enemyRoom.xValue, enemyRoom.yValue};
                i++;
            } else {
                break;
            }
        }
        return enemies;
    }
    private RoomGraphNode closestEdge(RoomGraphNode room) {
        return room.roomEdges.stream().min(new CenterDistance()).get();
    }
    public void setDistances() {
        distanceHelper(centerRoom, 0, new ArrayList<>());
    }
    private void distanceHelper(RoomGraphNode o, int currentDistance, ArrayList<RoomGraphNode> visitedNodes) {
        List<RoomGraphNode> notVisitedNodes = o.roomEdges.stream().filter(room -> !visitedNodes.contains(room)).toList();
        visitedNodes.add(o);
        if (notVisitedNodes.size() > 0) {
            for (RoomGraphNode node: notVisitedNodes) {
                node.distance = currentDistance + 1;
                distanceHelper(node, currentDistance + 1, visitedNodes);
            }
        }

    }
    public Integer[] doorSpace() {
        return closestWall(centerRoom.xValue, centerRoom.yValue, centerRoom);
    }
    public boolean contains(RoomGraphNode room) {
        return nodes.stream().anyMatch(rGraphNode -> rGraphNode.equals(room));
    }

}
