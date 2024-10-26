package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

public class StrengthBuff extends Buff{
    {
        type = buffType.NEUTRAL;
        announced = true;
    }

    public static final float DURATION	= 20f;

    @Override
    public int icon() {
        return BuffIndicator.STRENGTH;
    }

    @Override
    public float iconFadePercent() {
        return Math.max(0, (DURATION - visualcooldown()) / DURATION);
    }


    @Override
    public boolean attachTo(Char target) {
        Buff.detach( target, Vulnerable.class);
        return super.attachTo(target);
    }

    @Override
    public void onDetach(Char target){
        Buff.prolong(target, Vertigo.class, 2);
        Buff.affect(target, Weakness.class);
    }
}