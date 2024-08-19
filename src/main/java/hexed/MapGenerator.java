package main.java.hexed;

import arc.func.Cons;
import arc.struct.Seq;
import mindustry.content.Planets;
import mindustry.game.Rules;
import mindustry.maps.filters.GenerateFilter;
import mindustry.maps.filters.GenerateFilter.GenerateInput;
import mindustry.type.Planet;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.Tiles;

import static arc.math.Mathf.*;
import static arc.util.noise.Simplex.noise2d;
import static mindustry.Vars.*;

public class MapGenerator {
    Seq<MapGenerator> maps = new Seq<>();
    Seq<GenerateFilter> ores = new Seq<>();

    public final String name;
    public final Block[][] blocks;
    public Cons<Rules> ruleSetter;
    public GenerateFilter[] filters;

    public MapGenerator(String name, Cons<Rules> ruleSetter, Block[][] blocks, GenerateFilter[] filters) {
        this.name = name;
        this.blocks = blocks;
        this.ruleSetter = ruleSetter;
        this.filters = filters;
        
        maps.add(this);
    }

    public MapGenerator(String name, Block[][] blocks, GenerateFilter[] filters) {
        this.name = name;
        this.blocks = blocks;
        this.filters = filters;
       
        maps.add(this);
    }

    public MapGenerator nextMap() {
        return maps.random(this);
    }

    public void apply(Tiles tiles) {

    }

    public void applyFilters(Tiles tiles, GenerateFilter[] filters) {
    
    }

    public Rules applyRules(Rules rules) {
       return rules;
    }

}
