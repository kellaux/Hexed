package main.java.hexed;

import arc.func.Cons;
import arc.func.Intc2;
import arc.math.Mathf;
import arc.math.geom.Bresenham2;
import arc.math.geom.Geometry;
import arc.math.geom.Intersector;
import arc.util.Structs;
import arc.util.Tmp;
import mindustry.world.Tiles;

import java.util.stream.IntStream;

import static main.java.hexed.GenerateType.*;
import static main.java.hexed.Main.type;

public class HexGenerator implements Cons<Tiles> {

    public void get(Tiles tiles) {
        type.apply(tiles);

        // draw hexes
        getHexesPos((x, y) -> {
            Geometry.circle(x, y, hexDiameter, (cx, cy) -> {
                if (Intersector.isInsideHexagon(x, y, hexDiameter, cx, cy))
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
}
