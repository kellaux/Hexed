package main.java.hexed;

import static main.java.hexed.generation.GenerateType.*;
import static mindustry.Vars.tilesize;
import static mindustry.Vars.world;

import arc.math.geom.Intersector;
import arc.util.Nullable;
import mindustry.game.Team;
import mindustry.type.ItemStack;
import mindustry.world.Tile;
import mindustry.world.blocks.storage.CoreBlock;
import static main.java.hexed.Vars.*;

public class Hex {

	public final int id;
	public final int x, y;
	public final int wx, wy;
	public final float[] progress = new float[256];

	public Team controller;

	public Hex(int id, int x, int y) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.wx = x * tilesize;
		this.wy = y * tilesize;
	}

	public boolean hasCore() {
		return world.tile(x, y).team() != Team.derelict && world.tile(x, y).block() instanceof CoreBlock;
	}

	public float getProgressPercent(Team team) {
		return progress[team.id] * itemRequirement / 100;
	}


	//It needs to be optimized
	public boolean isCaptureProgress(Team team) {
		int id = team.id;
		progress[id] = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				if (Intersector.isInsideHexagon(wx, wy, diameter * tilesize, x * tilesize, y * tilesize)) {
					Tile tile = world.tile(x, y);
					for (ItemStack stack : tile.block().requirements) {
						progress[id] += stack.item.cost * 2f;
					}
				}
			}
		}

		return progress[id] >= itemRequirement;
	}

	@Nullable
	public Team getController() {
		if (hasCore()) {
			return world.tile(x, y).team();
		}
		return null;
	}

}
