package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.UpgradeGoo;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class WaterOfUpgrade extends WellWater {
// this is only placeable as a plant.

    @Override
    protected boolean affectHero(Hero hero) {
        if (!hero.isAlive()) return false;

        Sample.INSTANCE.play( Assets.Sounds.DRINK );

        hero.exp = 0;
        if (hero.lvl < Hero.MAX_LEVEL) {
            hero.lvl++;

            // Star burst effect for level up
            CellEmitter.center(hero.pos).burst(Speck.factory(Speck.STAR), 5);
            Sample.INSTANCE.play(Assets.Sounds.LEVELUP);
        }

            Dungeon.hero.interrupt();

            GLog.p( Messages.get(this, "procced") );

        return true;
    }

    @Override
    protected Item affectItem(Item item, int pos) {
        if (item != null) {
            int itemLevel = item.trueLevel();

            if (itemLevel >= 10) {
                itemLevel = 10;
            } else if (itemLevel >= 6 && itemLevel <= 8) {
                itemLevel = 6;
            } else if (itemLevel <= 5 && itemLevel > 0){
                itemLevel = 2;
            } else { //Item level 0
                return item;
            }

            CellEmitter.center(pos).burst(Speck.factory(Speck.TWINKLE), 6);
            UpgradeGoo goo = new UpgradeGoo(itemLevel);
            return goo;
        }
        return null;
    }

    @Override
    public Notes.Landmark landmark() {
        return Notes.Landmark.WELL_OF_UPGRADE;
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.start(Speck.factory(Speck.TWINKLE), 0.8f,0);
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}