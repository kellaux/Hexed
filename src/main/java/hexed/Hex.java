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

	public boolean isCaptureProgress(Tile tile) {
		int team = tile.team().id;
		if (Intersector.isInsideHexagon(wx, wy, diameter * tilesize, tile.worldx(), tile.worldy())) {
			for (ItemStack stack : tile.block().requirements) {
				progress[team] += stack.amount * stack.item.cost;
			}
		}
		return progress[team] >= itemRequirement;
	}

	@Nullable
	public Team getController() {
		if (hasCore()) {
			return world.tile(x, y).team();
		}
		return null;
	}

}
