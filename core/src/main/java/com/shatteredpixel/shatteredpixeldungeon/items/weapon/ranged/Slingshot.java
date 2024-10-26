package com.shatteredpixel.shatteredpixeldungeon.items.weapon.ranged;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;


public class Slingshot extends RangedWeapon {

    {
        image = ItemSpriteSheet.SLINGSHOT;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 0.8f;

        tier = 1;
        weaponType = MISC;
        weaponHand = TWO_HANDED;
        ammoType = ThrowingStone.class;
    }
}
