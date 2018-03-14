package com.minemaarten.signals.util.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;

import com.minemaarten.signals.rail.network.EnumHeading;
import com.minemaarten.signals.rail.network.NetworkObject;
import com.minemaarten.signals.rail.network.NetworkRail;
import com.minemaarten.signals.rail.network.NetworkSignal;
import com.minemaarten.signals.util.Pos2D;
import com.minemaarten.signals.util.railnode.DefaultRailNode;
import com.minemaarten.signals.util.railnode.RailNodeExpectedEdge;
import com.minemaarten.signals.util.railnode.RailNodeExpectedIntersection;
import com.minemaarten.signals.util.railnode.ValidatingRailNode;

public class NetworkParser{

    public static NetworkParser createDefaultParser(){
        NetworkParser parser = new NetworkParser();
        parser.objCreators.put('+', pos -> new DefaultRailNode(pos));
        parser.objCreators.put('d', pos -> new DefaultRailNode(pos).setDestination());
        parser.objCreators.put('s', pos -> new DefaultRailNode(pos).setStart());

        parser.objCreators.put('^', pos -> new NetworkSignal<>(pos, EnumHeading.NORTH));
        parser.objCreators.put('>', pos -> new NetworkSignal<>(pos, EnumHeading.EAST));
        parser.objCreators.put('v', pos -> new NetworkSignal<>(pos, EnumHeading.SOUTH));
        parser.objCreators.put('<', pos -> new NetworkSignal<>(pos, EnumHeading.WEST));
        return parser;
    }

    public final Map<Character, Function<Pos2D, NetworkObject<Pos2D>>> objCreators = new HashMap<>();

    public NetworkParser addExpectedIntersection(int index, EnumHeading expectedDirIn, EnumHeading expectedDirOut){
        objCreators.put(Character.forDigit(index, 10), pos -> new RailNodeExpectedIntersection(pos, index, expectedDirIn, expectedDirOut));
        return this;
    }

    public NetworkParser addEdgeGroups(String groups){
        for(char c : groups.toCharArray()) {
            objCreators.put(c, pos -> new RailNodeExpectedEdge(pos, c));
        }
        return this;
    }

    public NetworkParser addObjCreator(char c, Function<Pos2D, NetworkObject<Pos2D>> creator){
        objCreators.put(c, creator);
        return this;
    }

    public NetworkParser addValidator(char c, BiConsumer<NetworkRail<Pos2D>, TestRailNetwork> validator){
        return addObjCreator(c, pos -> new ValidatingRailNode(pos){
            @Override
            public void validate(TestRailNetwork network){
                validator.accept(this, network);
            }
        });
    }

    public TestRailNetwork parse(List<String> map){
        Validate.noNullElements(map);
        if(map.isEmpty()) throw new IllegalArgumentException("Empty network is not allowed!");

        int xSize = map.get(0).length();
        List<NetworkObject<Pos2D>> networkObjects = new ArrayList<>();

        for(int y = 0; y < map.size(); y++) {
            String yLine = map.get(y);
            if(yLine.length() != xSize) throw new IllegalArgumentException("Inconsistent map lengths! Violating row: " + y + ", expecting " + xSize + ", got " + yLine.length());

            for(int x = 0; x < xSize; x++) {
                Pos2D pos = new Pos2D(x, y);
                char c = yLine.charAt(x);

                if(c != ' ') {
                    Function<Pos2D, NetworkObject<Pos2D>> objCreator = objCreators.get(c);
                    if(objCreator != null) {
                        networkObjects.add(objCreator.apply(pos));
                    } else {
                        throw new IllegalArgumentException("Unknown character at " + x + ", " + y + ": '" + c + "'!");
                    }
                }
            }
        }

        return new TestRailNetwork(networkObjects);
    }
}
