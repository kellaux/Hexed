package main.java.hexed;

import arc.struct.Seq;
import mindustry.game.Schematic;
import mindustry.type.ItemStack;
import mindustry.world.Block;

public class PlanetData {
    Seq<Block> ores;
    Schematic schematic;
    Seq<ItemStack> items;

    public PlanetData(Seq<Block> ores, Schematic schematic, Seq<ItemStack> items ) {
        this.ores = ores;
        this.schematic = schematic;
        this.items = items;
    }
}
