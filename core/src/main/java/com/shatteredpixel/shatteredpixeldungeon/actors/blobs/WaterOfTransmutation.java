package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;

public class WaterOfTransmutation extends WellWater {

    @Override
    protected boolean affectHero(Hero hero) {
        return false; // No hero effect
    }

    @Override
    protected Item affectItem(Item item, int pos) {
        if (item != null) {
            Item result = ScrollOfTransmutation.changeItem(item);

            if (result != null) {
                CellEmitter.center(pos).burst(Speck.factory(Speck.CHANGE), 6);
                return result;
            }
        }
        return null;
    }

    @Override
    public Notes.Landmark landmark() {
        return Notes.Landmark.WELL_OF_TRANSMUTATION;
    }

    @Override
    public void use(BlobEmitter emitter) {
        super.use(emitter);
        emitter.start(Speck.factory(Speck.CHANGE), 0.5f, 0);
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }
}