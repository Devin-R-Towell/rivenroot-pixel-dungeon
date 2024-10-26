package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.ShadowCaster;
import com.shatteredpixel.shatteredpixeldungeon.plants.BlandfruitBush;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Point;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class StoneOfNature extends Runestone {

    private static final int DIST = 8;

    {
        image = ItemSpriteSheet.STONE_NATURE;
    }

    @Override
    protected void activate(final int cell) {
        boolean[] FOV = new boolean[Dungeon.level.length()];
        Point c = Dungeon.level.cellToPoint(cell);
        ShadowCaster.castShadow(c.x, c.y, Dungeon.level.width(), FOV, Dungeon.level.losBlocking, DIST);

        int sX = Math.max(0, c.x - DIST);
        int eX = Math.min(Dungeon.level.width() - 1, c.x + DIST);

        int sY = Math.max(0, c.y - DIST);
        int eY = Math.min(Dungeon.level.height() - 1, c.y + DIST);

        ArrayList<Plant> seedCandidates = new ArrayList<>();

        for (int y = sY; y <= eY; y++) {
            int curr = y * Dungeon.level.width() + sX;
            for (int x = sX; x <= eX; x++) {

                if (FOV[curr]) {

                    Plant p = Dungeon.level.plants.get(curr);
                    if (p != null && !(p instanceof BlandfruitBush)) {
                        seedCandidates.add(p);
                    }

                }
                curr++;
            }
        }

        Collections.shuffle(seedCandidates);
        Collections.sort(seedCandidates, new Comparator<Plant>() {
            @Override
            public int compare(Plant o1, Plant o2) {
                float diff = Dungeon.level.trueDistance(cell, o1.pos) - Dungeon.level.trueDistance(cell, o2.pos);
                if (diff < 0) {
                    return -1;
                } else if (diff == 0) {
                    return 0;
                } else {
                    return 1;
                }
            }
        });

        //convert at most 9 plants to seeds
        while (seedCandidates.size() > 9) {
            seedCandidates.remove(9);
        }

        for (Plant p : seedCandidates) {
            Plant.Seed seed = p.getSeed();
            Dungeon.level.drop(seed, p.pos).sprite.drop();
            p.wither();
        }

        Sample.INSTANCE.play(Assets.Sounds.TELEPORT);
    }
}
