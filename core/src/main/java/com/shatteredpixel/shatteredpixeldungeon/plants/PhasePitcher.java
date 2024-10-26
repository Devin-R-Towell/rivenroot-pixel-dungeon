package com.shatteredpixel.shatteredpixeldungeon.plants;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.level;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WaterOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.WellWater;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

public class PhasePitcher extends Plant {

    {
        image = 15;
        seedClass = Seed.class;
    }

    private static int originalTerrain;

    @Override
    public void activate(Char ch) {
        if (ch != null) {
            //do nothing handled by Well files.
        }
    }

    @Override
    public void wither() {
        if (originalTerrain != -1) {
            // Clear the water blob first
            WaterOfTransmutation water = (WaterOfTransmutation)Dungeon.level.blobs.get(WaterOfTransmutation.class);
            if (water != null) {
                water.fullyClear();
            }

            // Then restore terrain
            Dungeon.level.set(pos, originalTerrain);
            GameScene.updateMap(pos);
        }
        super.wither();
    }

    private static final String ORIGINAL_TERRAIN = "original_terrain";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ORIGINAL_TERRAIN, originalTerrain);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        originalTerrain = bundle.getInt(ORIGINAL_TERRAIN);
    }

    public static class Seed extends Plant.Seed {
        {
            image = ItemSpriteSheet.SEED_PHASE_PITCHER;
            plantClass = PhasePitcher.class;
        }

        @Override
        public void onThrow(int cell) {
            if (! ( Dungeon.level.map[cell] == Terrain.PASSABLE ||
                    Dungeon.level.map[cell] != Terrain.TRAP ||
                    Dungeon.level.map[cell] != Terrain.SECRET_TRAP ||
                    Dungeon.level.map[cell] != Terrain.WELL ) ) {

                originalTerrain = -1;
                Heap heap = Dungeon.level.drop(this, cell);
                if (!heap.isEmpty()) {
                    heap.sprite.drop(cell);
                }
                return;
            } else {
                originalTerrain = Dungeon.level.map[cell];
                Painter.set(Dungeon.level, cell, Terrain.WELL);
                WellWater.seed(cell, 1, WaterOfTransmutation.class, Dungeon.level);
                GameScene.updateMap(cell);
                super.onThrow(cell);
            }
        }
    }
}