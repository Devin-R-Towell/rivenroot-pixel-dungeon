package com.shatteredpixel.shatteredpixeldungeon.items.weapon.ranged;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.RangedWeapon;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class LightCrossbow extends RangedWeapon {
    {
        image = ItemSpriteSheet.LIGHT_CROSSBOW;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 0.8f;

        //check Dart.class for additional properties
        //This is an experiment.
        //third item down and so far nothing but success.

        tier = 3;
    }
}
