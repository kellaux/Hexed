package main.java.hexed;

import arc.func.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.maps.filters.*;
import mindustry.maps.filters.GenerateFilter.*;
import mindustry.world.*;

import static mindustry.Vars.*;

import static arc.math.Mathf.*;
import static arc.util.noise.Simplex.noise2d;
import static mindustry.content.Blocks.*;

import mindustry.world.blocks.environment.Floor;

public class HexGenerator implements Cons<Tiles> {

	// public static final IntSeq hexesPos = new IntSeq();
	public static final int size = 516;
	public static final int width = size;
	public static final int height = size;
	public static final int spacing = 78;
	public static final int hexDiameter = 74;
	public static final int hexRadius = hexDiameter / 2;

	Block[][] blocks = {
			{ darksand, darksandTaintedWater, sporeMoss, darksand },
			{ darksand, darksandTaintedWater, dacite, darksand },
			
	};

	public void get(Tiles tiles) {
		// generate map
		int seed1 = random(1000), seed2 = random(1000);
		int temp, elev;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				temp = clamp((int) ((noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * blocks.length), 0,
						blocks.length - 1);
				elev = clamp((int) (((noise2d(seed2, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 + 0.15f) * blocks[0].length),
						0, blocks[0].length - 1);
				Floor floor = blocks[temp][elev].asFloor();
				Block wall = floor.wall;

				tiles.set(x, y, new Tile(x, y, floor, air, wall));

			}
		}

		// add ores
		Seq<GenerateFilter> ores = new Seq<>();
		maps.addDefaultOres(ores);
		ores.add(new OreFilter() {
			{
				threshold -= 0.003f;
				scl += 4f;
			}
		});

		GenerateInput input = new GenerateInput();
		for (GenerateFilter filter : ores) {
			filter.randomize();
			input.begin(width, height, tiles::get);
			filter.apply(tiles, input);
		}

		// draw hexes
		getHexesPos((x, y) -> {
			Geometry.circle(x, y, hexDiameter, (cx, cy) -> {
				if (Intersector.isInsideHexagon(x, y, hexDiameter, cx, cy))
					tiles.getc(cx, cy).remove();
			});

			// draw passages
			for (int side = 0; side < 3; side++) {
				float angle = side * 120f - 30f;
				Tmp.v1.trnsExact(angle, spacing + 12).add(x, y);
				if (!Structs.inBounds((int) Tmp.v1.x, (int) Tmp.v1.y, width, height))
					continue;

				Tmp.v1.trnsExact(angle, spacing / 2f + 7).add(x, y);
				Bresenham2.line(x, y, (int) Tmp.v1.x, (int) Tmp.v1.y, (cx, cy) -> {
					int radius = 3;
					Geometry.circle(cx, cy, radius, (bx, by) -> tiles.getc(bx, by).remove());
				});
			}
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
