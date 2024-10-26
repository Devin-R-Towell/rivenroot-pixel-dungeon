package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.Image;
import com.watabou.utils.Bundle;

public class Heater extends MeleeWeapon {
    {
        image = ItemSpriteSheet.HEATER;
        hitSound = Assets.Sounds.HIT;
        hitSoundPitch = 1f;

        tier = 4;
        weaponType = SHIELD;
    }

    @Override
    public int max(int lvl) {
        return  Math.round(3f*(tier+1)) +   //6 base, down from 12
                lvl*(tier-1);               //+0 per level, down from +2
    }

    @Override
    public int defenseFactor( Char owner ) {
        return DRMax();
    }

    public int DRMax(){
        return DRMax(buffedLvl());
    }

    //2 extra defence, plus 1 per level
    public int DRMax(int lvl){
        return 2 + lvl;
    }

    public String statsInfo(){
        if (isIdentified()){
            return Messages.get(this, "stats_desc", 4+buffedLvl());
        } else {
            return Messages.get(this, "typical_stats_desc", 4);
        }
    }

    @Override
    protected void duelistAbility(Hero hero, Integer target) {
        RoundShield.guardAbility(hero, 5+buffedLvl(), this);
    }

    @Override
    public String abilityInfo() {
        if (levelKnown){
            return Messages.get(this, "ability_desc", 5+buffedLvl());
        } else {
            return Messages.get(this, "typical_ability_desc", 5);
        }
    }

    @Override
    public String upgradeAbilityStat(int level) {
        return Integer.toString(5 + level);
    }

    public static void guardAbility(Hero hero, int duration, MeleeWeapon wep){
        wep.beforeAbilityUsed(hero, null);
        Buff.prolong(hero, RoundShield.GuardTracker.class, duration).hasBlocked = false;
        hero.sprite.operate(hero.pos);
        hero.spendAndNext(Actor.TICK);
        wep.afterAbilityUsed(hero);
    }

    public static class GuardTracker extends FlavourBuff {

        {
            announced = true;
            type = buffType.POSITIVE;
        }

        public boolean hasBlocked = false;

        @Override
        public int icon() {
            return BuffIndicator.DUEL_GUARD;
        }

        @Override
        public void tintIcon(Image icon) {
            if (hasBlocked){
                icon.tint(0x651f66, 0.5f);
            } else {
                icon.resetColor();
            }
        }

        @Override
        public float iconFadePercent() {
            return Math.max(0, (5 - visualcooldown()) / 5);
        }

        private static final String BLOCKED = "blocked";

        @Override
        public void storeInBundle(Bundle bundle) {
            super.storeInBundle(bundle);
            hasBlocked = bundle.getBoolean(BLOCKED);
        }

        @Override
        public void restoreFromBundle(Bundle bundle) {
            super.restoreFromBundle(bundle);
            bundle.put(BLOCKED, hasBlocked);
        }
    }
}
