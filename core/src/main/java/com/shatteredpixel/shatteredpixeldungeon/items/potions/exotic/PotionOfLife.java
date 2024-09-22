/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Poison;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;

public class PotionOfLife extends ExoticPotion {

    {
        icon = ItemSpriteSheet.Icons.POTION_MENDING;

        bones = true;
    }

    @Override
    public void apply( Hero hero ) {
        identify();
        heal(hero);
    }

    public static void heal( Char ch ){
        if (ch == Dungeon.hero && Dungeon.isChallenged(Challenges.NO_HEALING)){
            pharmacophobiaProc(Dungeon.hero);
        } else {
            //starts out healing 30 hp, equalizes with hero health total at level 11
            Healing healing = Buff.affect(ch, Healing.class);
            healing.setHeal((int) (ch.HT * 1.0f), 1.0f, 0);
            healing.applyVialEffect();
            Buff.detachAllNegativeBuffs(ch);
        }
    }

    public static void pharmacophobiaProc( Hero hero ){
        // harms the hero for ~20% of their max HP in poison
        Buff.affect( hero, Poison.class).set(2 + hero.lvl/4);
    }

    @Override
    public int value() {
        return isKnown() ? 30 * quantity : super.value();
    }
}
