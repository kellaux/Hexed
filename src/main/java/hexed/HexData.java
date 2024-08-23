package main.java.hexed;

import arc.struct.*;
import arc.math.geom.*;
import mindustry.gen.*;

public class HexData {

    public static Seq<Hex> hexes = new Seq<>();

    public static void initHexes() {
        HexGenerator.getHexesPos((x, y) -> hexes.add(new Hex(hexes.size + 1, x, y)));
    }

    public static Hex getFirstHex() {
        return hexes.select(hex -> !hex.hasCore()).random();
    }

    public static Hex getClosestHex(Position position) {
        return hexes.min(hex -> position.dst(hex.wx, hex.wy));
    }


    public static class HexCaptureEvent {
        public Hex hex;
        public Player player;

        public HexCaptureEvent(Hex hex, Player player) {
            this.hex = hex;
            this.player = player;
        }
    }

}
