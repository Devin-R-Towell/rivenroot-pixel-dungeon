package com.shatteredpixel.shatteredpixeldungeon.items.weapon.ranged;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class LightCrossbow extends RangedWeapon {
    {
        image = ItemSpriteSheet.LIGHT_CROSSBOW;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.2f;

        //check Dart.class for additional properties
        //This is an exsperiment.

        tier = 3;
        weaponType = BOW;
    }


}
