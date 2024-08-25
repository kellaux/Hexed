package main.java.hexed.generation;

import arc.func.Cons;
import arc.struct.Seq;
import mindustry.game.Rules;
import mindustry.maps.filters.GenerateFilter;
import mindustry.maps.filters.OreFilter;
import mindustry.type.Planet;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import mindustry.world.blocks.environment.Floor;

import static arc.math.Mathf.*;
import static arc.util.noise.Simplex.noise2d;
import static main.java.hexed.Vars.planets;
import static mindustry.content.Blocks.air;

public class GenerateType {
    public static final Seq<GenerateType> maps = new Seq<>();

    public static final int size = 516;
    public static final int width = size;
    public static final int height = size;
    public static final int spacing = 78;
    public static final int diameter = 74;
    public static final int side = diameter / 2;

    public final String name;
    public final Planet planet;
    public final Block[][] blocks;
    public Cons<Rules> ruleSetter;
    public GenerateFilter[] filters;

    public GenerateType(String name, Planet planet, Cons<Rules> ruleSetter, Block[][] blocks, GenerateFilter[] filters) {
        this.name = name;
        this.planet = planet;
        this.blocks = blocks;
        this.ruleSetter = ruleSetter;
        this.filters = filters;

        maps.add(this);
    }

    public GenerateType(String name, Planet planet, Block[][] blocks, GenerateFilter[] filters) {
        this.name = name;
        this.planet = planet;
        this.blocks = blocks;
        this.filters = filters;

        maps.add(this);
    }

    public GenerateType(String name, Planet planet, Block[][] blocks) {
        this.name = name;
        this.planet = planet;
        this.blocks = blocks;

        maps.add(this);
    }

    public GenerateType nextMap() {
        return maps.random(this);
    }

    // generate map
    public void apply(Tiles tiles) {
        int seed1 = random(1000), seed2 = random(1000);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int temp = clamp((int) ((noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * blocks.length), 0,
                        blocks.length - 1);
                int elev = clamp((int) (((noise2d(seed2, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 + 0.15f) * blocks[0].length),
                        0, blocks[0].length - 1);
                Floor floor = blocks[temp][elev].asFloor();
                Block wall = floor.wall;

                tiles.set(x, y, new Tile(x, y, floor, air, wall));

            }
        }

        // add ores
        Seq<OreFilter> ores = planets.get(planet).ores.map(block -> new OreFilter() {{
            threshold = block.asFloor().oreThreshold - 0.04f;
            scl = block.asFloor().oreScale + 8f;
            ore = block.asFloor();
        }});

        applyFilters(tiles, ores.toArray(OreFilter.class));

    }

    public void applyFilters(Tiles tiles, GenerateFilter[] filters) {
        GenerateFilter.GenerateInput input = new GenerateFilter.GenerateInput();
        for (GenerateFilter filter : filters) {
            filter.randomize();
            input.begin(width, height, tiles::get);
            filter.apply(tiles, input);
        }
    }

    public Rules applyRules(Rules rules) {
        if (ruleSetter != null)
            ruleSetter.get(rules);
        rules.env = planet.defaultEnv;
        rules.hiddenBuildItems.addAll(planet.hiddenItems);
        return rules;
    }
}
