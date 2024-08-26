package main.java.hexed.data;

import arc.struct.Seq;
import mindustry.game.Schematic;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class PlanetData {
    public Seq<Ore> ores;
    public Schematic schematic;
    public Seq<ItemStack> items;
    public Seq<Loadout> blocks;

    public PlanetData(Seq<Ore> ores, Schematic schematic, Seq<ItemStack> items) {
        this.ores = ores;
        this.schematic = schematic;
        this.items = items;
    }

    public PlanetData(Seq<Ore> ores, Seq<Loadout> blocks) {
        this.ores = ores;
        this.blocks = blocks;
    }

    public static Seq<Loadout> list(Object... objects) {
        Seq<Loadout> loadouts = new Seq<>(objects.length / 4);

        for (int i = 0; i < objects.length; i += 4) {
            loadouts.add(new Loadout((Block) objects[i], (float) objects[i + 1], (int) objects[i + 2], (int) objects[i + 3]));
        }

        return loadouts;
    }

    public static class Ore {
        public Block ore;
        public float threshold;
        public float scl;

        public Ore(Block ore, float threshold, float scl) {
            this.ore = ore;
            this.threshold = ore.asFloor().oreThreshold + threshold;
            this.scl = ore.asFloor().oreScale + scl;
        }

        public Ore(Block ore) {
            this.ore = ore;
            this.threshold = ore.asFloor().oreThreshold;
            this.scl = ore.asFloor().oreScale;
        }

        public static void setDefault(float threshold, float scl) {
        }
    }

    public static class Loadout {
        public final Block build;
        public final float chance;
        public final int start;
        public final int end;

        public Loadout(Block build, float chance, int start, int end) {
            this.build = build;
            this.chance = chance;
            this.start = start;
            this.end = end;
        }

    }

}
