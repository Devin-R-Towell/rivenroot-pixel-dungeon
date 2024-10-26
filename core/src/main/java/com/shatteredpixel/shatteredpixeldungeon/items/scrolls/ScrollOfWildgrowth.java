package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;


import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;



public class ScrollOfWildgrowth extends Scroll {

    {
        icon = ItemSpriteSheet.Icons.SCROLL_WILDGROWTH;
    }

    @Override
    public void doRead() {
        setAllToHighGrass();

        identify();
    }

    private void setAllToHighGrass() {
        Level level = Dungeon.level;

        for (int i = 0; i < level.length(); i++) {
            if (level.map[i] == Terrain.EMPTY || level.map[i] == Terrain.GRASS) {
                if (!(level.map[i] == Terrain.TRAP || level.map[i] == Terrain.SECRET_TRAP)) {
                    Level.set(i, Terrain.HIGH_GRASS);
                    GameScene.updateMap(i);
                }
            }
        }
    }

    @Override
    public int value() {
        return isKnown() ? 80 * quantity : super.value();
    }
}
