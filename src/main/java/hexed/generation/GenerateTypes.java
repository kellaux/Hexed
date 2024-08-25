package main.java.hexed.generation;

import static mindustry.content.Planets.*;

import mindustry.world.Block;

import static mindustry.content.Blocks.*;

public class GenerateTypes {
    public static GenerateType winter, lavaLand;

    public static void load() {
        winter = new GenerateType("Winter", serpulo, new Block[][]{
                {darksand, darksandTaintedWater, sporeMoss, darksand},
                {darksand, darksandTaintedWater, dacite, darksand},
        });

        lavaLand = new GenerateType("Lava Land", serpulo, new Block[][]{
                {darksand, stone, sand, shale, sand},
                {shale, basalt, slag, stone, basalt},
                {darksand, hotrock, hotrock, magmarock, darksand},
                {shale, craters, slag, hotrock, sand},
                {dacite, shale, basalt, shale, shale}
        });
    }
}
