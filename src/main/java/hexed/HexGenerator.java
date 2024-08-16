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
import static arc.util.noise.Simplex.noise2d;
import static arc.math.Mathf.*;

public class HexGenerator implements Cons<Tiles> {

	// public static final IntSeq hexesPos = new IntSeq();
	public static final int size = 516;
	public static final int width = size;
	public static final int height = size;
	public static final int spacing = 78;
	public static final int hexDiameter = 74;
	public static final int hexRadius = hexDiameter / 2;

	Block[][] floors = {
			{ Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.sand, Blocks.grass },
			{ Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.grass },
			{ Blocks.darksandWater, Blocks.darksand, Blocks.darksand, Blocks.darksand, Blocks.grass, Blocks.shale },
			{ Blocks.darksandTaintedWater, Blocks.darksandTaintedWater, Blocks.moss, Blocks.moss, Blocks.sporeMoss,
					Blocks.stone },
			{ Blocks.ice, Blocks.iceSnow, Blocks.snow, Blocks.dacite, Blocks.hotrock, Blocks.salt }
	};

	Block[][] blocks = {
			{ Blocks.stoneWall, Blocks.stoneWall, Blocks.sandWall, Blocks.sandWall, Blocks.pine, Blocks.pine },
			{ Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine },
			{ Blocks.stoneWall, Blocks.stoneWall, Blocks.duneWall, Blocks.duneWall, Blocks.pine, Blocks.pine },
			{ Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall, Blocks.sporeWall,
					Blocks.stoneWall },
			{ Blocks.iceWall, Blocks.snowWall, Blocks.snowWall, Blocks.snowWall, Blocks.stoneWall, Blocks.saltWall }
	};

	public void get(Tiles tiles) {

		// add ores
		Seq<GenerateFilter> ores = new Seq<>();
		GenerateInput input = new GenerateInput();
		maps.addDefaultOres(ores);
		ores.insert(0, new OreFilter() {
			{
				scl += 2 / 2.1f;
			}
		});
		ores.each(GenerateFilter::randomize);

		// fill map
		int seed1 = Mathf.random(1000), seed2 = Mathf.random(1000);
		int temp, elev;
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				temp = clamp((int) ((noise2d(seed1, 12, 0.6, 1.0 / 400, x, y) - 0.5) * 10 * blocks.length), 0,
						blocks.length - 1);
				elev = clamp((int) (((noise2d(seed2, 12, 0.6, 1.0 / 700, x, y) - 0.5) * 10 + 0.15f) * blocks[0].length),
						0, blocks[0].length - 1);
				Block floor = floors[temp][elev];
				Block wall = blocks[temp][elev];
				Block ore = Blocks.air;

				for (GenerateFilter filter : ores) {
					input.floor = Blocks.stone;
					input.block = wall;
					input.overlay = ore;
					input.x = x;
					input.y = y;
					input.width = input.height = size;
					filter.apply(input);
					if (input.overlay != Blocks.air) {
						ore = input.overlay;
					}
				}

				tiles.set(x, y, new Tile(x, y, floor, ore, wall));
			}
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
