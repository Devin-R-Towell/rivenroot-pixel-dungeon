package com.shatteredpixel.shatteredpixeldungeon.items.food;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StrengthBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class CromwellsReserve extends Food {

    public static final String AC_DRINK = "DRINK";

    {
        stackable = false;
        image = ItemSpriteSheet.CROMWELLSRESERVE;

        bones = true;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
            ArrayList<String> actions = super.actions(hero);
            actions.remove(AC_EAT);
            actions.add(AC_DRINK);
            return actions;
        }

    protected void drinkSFX(){
        Sample.INSTANCE.play( Assets.Sounds.DRINK );
    }

    protected float drinkingTime(){
        return Potion.TIME_TO_DRINK;
    }

    @Override
    public void execute(Hero hero, String action) {
            Hero ch = Dungeon.hero;
        if (Dungeon.isChallenged(Challenges.NO_FOOD)) {
            GLog.n(Messages.get(CromwellsReserve.class, "no_drink"));
        } else {

            if (action.equals(AC_DRINK)) {
                Buff.affect(ch, StrengthBuff.class);

                detach(hero.belongings.backpack);
                Catalog.countUse(getClass());
                GLog.i( Messages.get(CromwellsReserve.class, "drink_msg") );

                hero.sprite.operate(hero.pos);
                hero.busy();
                SpellSprite.show(hero, SpellSprite.STRENGTH);
                drinkSFX();

                hero.spend(drinkingTime());

                Statistics.foodEaten++;
                Badges.validateFoodEaten();
            }
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int value() {
        return 30 * quantity;
    }
}

