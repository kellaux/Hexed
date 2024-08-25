package main.java.hexed.generation;

import arc.func.Cons;
import arc.func.Intc2;
import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Intersector;
import arc.util.Structs;
import arc.util.Tmp;
import main.java.hexed.Hex;
import mindustry.game.Team;
import mindustry.world.Tile;
import mindustry.world.Tiles;
import main.java.hexed.data.PlanetData.Loadout;

import java.util.stream.IntStream;

import static main.java.hexed.generation.GenerateType.*;
import static main.java.hexed.Utils.*;
import static mindustry.Vars.world;
import static mindustry.content.Blocks.air;


public class HexedGenerator implements Cons<Tiles> {

    public void get(Tiles tiles) {
        type.apply(tiles);

        // draw hexes
        getHexesPos((x, y) -> {
            Geometry.circle(x, y, diameter, (cx, cy) -> {
                if (Intersector.isInsideHexagon(x, y, diameter, cx, cy))
                    tiles.getc(cx, cy).remove();
            });

            // draw passages
            IntStream.range(0, 3).forEach(side -> {
                float angle = side * 120f - 30f;
                Tmp.v1.trnsExact(angle, spacing + 12).add(x, y);
                if (!Structs.inBounds((int) Tmp.v1.x, (int) Tmp.v1.y, width, height))
                    return;
                Tmp.v1.trnsExact(angle, spacing / 2f + 7).add(x, y);
                Bresenham2.line(x, y, (int) Tmp.v1.x, (int) Tmp.v1.y, (cx, cy) -> {
                    int radius = 3;
                    Geometry.circle(cx, cy, radius, (bx, by) -> tiles.getc(bx, by).remove());
                });
            });
        });
    }

    public static void getHexesPos(Intc2 cons) {
        float height = Mathf.sqrt3 * spacing / 4f;
        for (int x = 0; x < size / spacing - 2; x++) {
            for (int y = 0; y < size / height - 2; y++) {
                int cx = (int) (x * spacing * 1.5f + (y % 2) * spacing * 0.75f) + spacing / 2;
                int cy = (int) (y * height) + spacing / 2;
                cons.get(cx, cy);
            }

        }
    }

    public static void randomPointInHexagon(Loadout loadout, Team team, Hex hex, int radius) {
        double blocks = (int) (Math.random() * (loadout.end - loadout.start + 1) + loadout.start);

            int firstAngleFactor = (int) (Math.floor(Math.random() * 6));
            int secondAngleFactor = firstAngleFactor + 2;

            double x1 = Math.cos(Math.PI * secondAngleFactor / 3) * side;
            double y1 = Math.sin(Math.PI * secondAngleFactor / 3) * side;
            double x2 = Math.cos(Math.PI * firstAngleFactor / 3) * side;
            double y2 = Math.sin(Math.PI * firstAngleFactor / 3) * side;

            double r1 = Math.random() * loadout.chance;
            double r2 = Math.random() * loadout.chance;
            double Lx = x1 * r1 + x2 * r2 + hex.x;
            double Ly = y1 * r1 + y2 * r2 + hex.y;

            for(int j = 0; j < blocks; j++) {
                double angle = Math.random() * 2 * Math.PI;
                double r = radius * Math.sqrt(Math.random()) * 0.6f;
                double Dx = Lx + r * Math.cos(angle);
                double Dy = Ly + r * Math.sin(angle);

                Tile tile = world.tile((int) Dx, (int) Dy);
                if (tile.block() == air) {
                    tile.setNet(loadout.build, team, 0);
                }
            }

    }

}
