package com.shatteredpixel.shatteredpixeldungeon.items.weapon.ranged;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class Longbow extends RangedWeapon {
    {
        image = ItemSpriteSheet.LONGBOW;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1.2f;

        tier = 6;
        weaponType = BOW;
        weaponHand = TWO_HANDED;
    }
}
