package main.java.hexed;

import arc.files.Fi;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import main.java.hexed.data.PlanetData;
import main.java.hexed.generation.GenerateType;
import mindustry.game.Rules;
import mindustry.game.Schematics;
import mindustry.type.Planet;

import static main.java.hexed.generation.GenerateType.side;
import static mindustry.Vars.tilesize;
import static mindustry.content.Blocks.*;
import static mindustry.content.Blocks.oreThorium;
import static mindustry.content.Items.*;
import static mindustry.content.Items.titanium;
import static mindustry.content.Planets.serpulo;
import static mindustry.type.ItemStack.list;

public class Vars {

    public static final float leftTeamDestroyTime = 90f;
    public static final float roundTime = 60 * 60 * 90f;
    public static final int itemRequirement = 100;
    public static final int winCaptureCount = 41;
    public static final int maxStartBlocks = 20;

    public static final Rules rules = new Rules();
    public static final ObjectMap<Planet, PlanetData> planets = new ObjectMap<>();

    public static void load() {
        //set default rules
        rules.enemyCoreBuildRadius = side * tilesize;
        rules.canGameOver = false;
        rules.coreCapture = true;
        rules.buildSpeedMultiplier = 2f;
        rules.blockHealthMultiplier = 1.5f;
        rules.unitBuildSpeedMultiplier = 1.75f;

        // uses for tests
        rules.infiniteResources = true;

        // set planet, ore, core, and default resources.
        planets.put(serpulo, new PlanetData(
                Seq.with(oreCopper, oreLead, oreScrap, oreCoal, oreTitanium, oreThorium),
                Schematics.readBase64("bXNjaAB4nE2SgY7CIAyGC2yDsXkXH2Tvcq+AkzMmc1tQz/j210JpXDL8hu3/lxYY4FtBs4ZbBLvG1ync4wGO87bvMU2vsCzTEtIlwvCxBW7e1r/43hKYkGY4nFN4XqbfMD+29IbhvmHOtIc1LjCmuIcrfm3X9QH2PofHIyYY5y3FaX3OS3ze4fiRwX7dLa5nDHTPddkCkT3l1DcA/OALihZNq4H6NHnV+HZCVshJXA9VYZC9kfVU+VQGKSsbjVT1lOgp1qO4rGIo9yvnquxH1ORIohap6HVIDbtpaNlDi4cWD80eFJdrNhbJc8W61Jzdqi/3wrRIRii7GYdelvWMZDQs1kNbqtYe9/KuGvDX5zD6d5SML66+5dwRqXgQee5GK3Edxw1ITfb3SJ71OomzUAdjuWsWqZyJavd8Issdb5BqVbaoGCVzJqrddaUGTWSFHPs67m6H5HlaTqbqpFc91Kfn+2eQSp9pr96/Xtx6cevZjeKKDuUOklvvXy9uPGdNZFjZi7IXZS/n8Hyf/wFbjj/q"),
                list(copper, 350, lead, 250, graphite, 150, metaglass, 150, silicon, 250, titanium, 50)));

        //TODO
//        planets.put(serpulo, new PlanetData(
//                Seq.with(oreCopper, oreLead, oreCoal, oreTitanium, oreThorium),
//                PlanetData.list(
//                        copperWallLarge, 0.7f, 5, 10,
//                        airFactory, 0.4f, 1, 1,
//                        powerNode, 0.8f, 4, 7
//                ) //block, chance, interval [start; end]
//
//        ));

    }
}
