package byow.Core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Stream;

public class RoomGraphDisjointSet implements Iterable<RoomGraph>{
    private ArrayList<RoomGraph> rGraphs;

    public RoomGraphDisjointSet() {
        rGraphs = new ArrayList<>();
    }
    public RoomGraphDisjointSet(ArrayList<RoomGraph> graphs) {
        rGraphs = graphs;
    }
    public void addGraph(RoomGraph rGraph) {
        rGraphs.add(rGraph);
    }
    public void connect(RoomGraph rGraph1, RoomGraph rGraph2) {
        if (rGraph1.size() >= rGraph2.size()) {
            rGraph1.addAllRooms(rGraph2);
            rGraphs.remove(rGraph2);
        } else {
            rGraph2.addAllRooms(rGraph1);
            rGraphs.remove(rGraph1);
        }

    }
    public int numSets() {
        return rGraphs.size();
    }

    public boolean isAllConnected() {
        return numSets() == 1;
    }
    public void addConnections(MapBuilder builder) {
        while (!isAllConnected()) {
            ArrayList<RoomGraph> rGraphsCopy = (ArrayList<RoomGraph>) rGraphs.clone();
            for (RoomGraph rGraph : rGraphsCopy) {
                if (rGraphs.contains(rGraph)) {
                    rGraph.addHallways(builder, this);
                }
            }

        }
    }
    public String toString() {
        StringBuilder stringToReturn = new StringBuilder();
        for(RoomGraph rGraph: rGraphs) {
            stringToReturn.append(rGraph.size()).append(" ");
        }
        return stringToReturn.toString();
    }

    @Override
    public Iterator<RoomGraph> iterator() {
        return rGraphs.iterator();
    }
    public RoomGraphDisjointSet clone() {
        return new RoomGraphDisjointSet((ArrayList<RoomGraph>) rGraphs.clone());
    }
    public Stream<RoomGraph> stream() {
        return rGraphs.stream();
    }
    public boolean contains (RoomGraph rGraph) {
        return rGraphs.contains(rGraph);
    }
    public RoomGraph getFinal() {
        if (isAllConnected()) {
            for (RoomGraph rGraph: rGraphs) {
                return rGraph;
            }
        }
        return null;
    }
}
