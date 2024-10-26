package com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.utils.Random;

public class ScrollOfOvergrowth extends ExoticScroll {

    {
        icon = ItemSpriteSheet.Icons.SCROLL_OVERGROWTH;
    }

    @Override
    public void doRead() {
        setAllToHighGrassAndPlants();

        identify();

        readAnimation();
    }

    private void setAllToHighGrassAndPlants() {
        Level level = Dungeon.level;

        for (int i = 0; i < level.length(); i++) {
            if (level.map[i] == Terrain.EMPTY || level.map[i] == Terrain.GRASS) {
                if (!(level.map[i] == Terrain.TRAP || level.map[i] == Terrain.SECRET_TRAP)) {
                    Level.set(i, Terrain.HIGH_GRASS);
                    GameScene.updateMap(i);
                    if (Random.Float() < 0.15f && level.plants.get(i) == null) {
                        level.plant((Plant.Seed) Generator.randomUsingDefaults(Generator.Category.SEED), i);
                        GameScene.updateMap(i);
                    }
                }
            }
        }
    }

    @Override
    public int value() {
        return isKnown() ? 160 * quantity : super.value();
    }
}

