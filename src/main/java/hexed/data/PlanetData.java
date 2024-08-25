package main.java.hexed.data;

import arc.struct.Seq;
import mindustry.game.Schematic;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class PlanetData {
    public Seq<Block> ores;
    public Schematic schematic;
    public Seq<ItemStack> items;
    public Seq<Loadout> blocks;

    public PlanetData(Seq<Block> ores, Schematic schematic, Seq<ItemStack> items) {
        this.ores = ores;
        this.schematic = schematic;
        this.items = items;
    }

    public PlanetData(Seq<Block> ores, Seq<Loadout> blocks) {
        this.ores = ores;
        this.blocks = blocks;
    }

    public static Seq<Loadout> list(Object... blocks) {
        Seq<Loadout> loadouts = new Seq<>(blocks.length / 4);

        for (int i = 0; i < blocks.length; i += 4) {
            loadouts.add(new Loadout((Block) blocks[i], (float) blocks[i + 1], (int) blocks[i + 2], (int) blocks[i + 3]));
        }

        return loadouts;
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
