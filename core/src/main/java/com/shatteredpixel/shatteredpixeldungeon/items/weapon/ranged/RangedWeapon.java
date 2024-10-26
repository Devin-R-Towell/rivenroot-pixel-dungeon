package com.shatteredpixel.shatteredpixeldungeon.items.weapon.ranged;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.GreaterHaste;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MonkEnergy;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.wands.WandOfBlastWave;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.MissileWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.ranged.darts.Dart;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Reflection;

import java.util.ArrayList;

public class RangedWeapon extends Weapon {

    public static final String AC_SHOOT = "SHOOT";
    public static final String AC_KNOCK = "KNOCK";

    protected MissileWeapon loadedAmmo;
    protected Class<? extends MissileWeapon> ammoType;
    public int tier;

    {
        defaultAction = AC_SHOOT;
        usesTargeting = true;
        ammoType = Dart.class;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_KNOCK);
        actions.add(AC_SHOOT);
        return actions;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_SHOOT)) {
            if (loadedAmmo == null) {
                GLog.w(Messages.get(this, "no_ammo"));
                return;
            }
            curUser = hero;
            curItem = this;
            GameScene.selectCell(shooter);
        } else if (action.equals(AC_KNOCK)) {
            GameScene.selectItem(itemSelector);
        }
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        if (super.doUnequip(hero, collect, single)) {
            if (hero.buff(ChargedShot.class) != null &&
                    !(hero.belongings.weapon() instanceof RangedWeapon)
                    && !(hero.belongings.secondWep() instanceof RangedWeapon)) {
                hero.buff(ChargedShot.class).detach();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public float accuracyFactor(Char owner, Char target) {
        if (owner.buff(ChargedShot.class) != null) {
            Actor.add(new Actor() {
                {
                    actPriority = VFX_PRIO;
                }

                @Override
                protected boolean act() {
                    if (owner instanceof Hero && !target.isAlive()) {
                        MeleeWeapon.onAbilityKill((Hero) owner, target);
                    }
                    Actor.remove(this);
                    return true;
                }
            });
            return Float.POSITIVE_INFINITY;
        } else {
            return super.accuracyFactor(owner, target);
        }
    }

    @Override
    public int proc(Char attacker, Char defender, int damage) {
        int dmg = super.proc(attacker, defender, damage);

        if (attacker.buff(ChargedShot.class) != null && !(curItem instanceof Dart)) {
            Ballistica trajectory = new Ballistica(attacker.pos, defender.pos, Ballistica.STOP_TARGET);
            trajectory = new Ballistica(trajectory.collisionPos, trajectory.path.get(trajectory.path.size() - 1), Ballistica.PROJECTILE);
            WandOfBlastWave.throwChar(defender,
                    trajectory,
                    3,
                    true,
                    true,
                    this);
            attacker.buff(ChargedShot.class).detach();
        }
        return dmg;
    }

    protected boolean acceptsAmmo(MissileWeapon ammo) {
        if (ammoType == ThrowingStone.class) {
            return ammo.getClass() == ThrowingStone.class;
        } else {
            return ammo instanceof Dart;
        }
    }

    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {
        @Override
        public String textPrompt() {
            return Messages.get(RangedWeapon.class, "select_ammo");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof MissileWeapon && acceptsAmmo((MissileWeapon) item);
        }

        @Override
        public void onSelect(Item item) {
            if (item != null && itemSelectable(item)) {
                loadedAmmo = (MissileWeapon) item;
                GLog.i(Messages.get(RangedWeapon.class, "ammo_loaded"));

                if (curUser.belongings.contains(item)) {
                    curUser.sprite.operate(curUser.pos);
                    Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
                    updateQuickslot();
                }
            }
        }
    };

    private CellSelector.Listener shooter = new CellSelector.Listener() {
        @Override
        public void onSelect(Integer target) {
            if (target != null && loadedAmmo != null) {
                try {
                    MissileWeapon missile = (MissileWeapon) Reflection.newInstance(loadedAmmo.getClass());
                    missile.cast(curUser, target);
                } catch (Exception e) {
                    ShatteredPixelDungeon.reportException(e);
                }
            }
        }

        @Override
        public String prompt() {
            return Messages.get(RangedWeapon.class, "prompt");
        }
    };

    protected int baseChargeUse(Hero hero, Char target) {
        return 1; //abilities use 1 charge by default
    }

    public final float abilityChargeUse(Hero hero, Char target) {
        return baseChargeUse(hero, target);
    }

    protected void beforeAbilityUsed(Hero hero, Char target) {
        hero.belongings.abilityWeapon = this;
        MeleeWeapon.Charger charger = Buff.affect(hero, MeleeWeapon.Charger.class);

        charger.partialCharge -= abilityChargeUse(hero, target);
        while (charger.partialCharge < 0 && charger.charges > 0) {
            charger.charges--;
            charger.partialCharge++;
        }

        if (hero.heroClass == HeroClass.DUELIST
                && hero.hasTalent(Talent.AGGRESSIVE_BARRIER)
                && (hero.HP / (float) hero.HT) <= 0.5f) {
            int shieldAmt = 1 + 2 * hero.pointsInTalent(Talent.AGGRESSIVE_BARRIER);
            Buff.affect(hero, Barrier.class).setShield(shieldAmt);
            hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shieldAmt), FloatingText.SHIELDING);
        }

        updateQuickslot();
    }

    public static void onAbilityKill(Hero hero, Char killed) {
        if (killed.alignment == Char.Alignment.ENEMY && hero.hasTalent(Talent.LETHAL_HASTE)) {
            //effectively 3/5 turns of greater haste
            Buff.affect(hero, GreaterHaste.class).set(2 + 2 * hero.pointsInTalent(Talent.LETHAL_HASTE));
        }
    }

    protected void afterAbilityUsed(Hero hero) {
        hero.belongings.abilityWeapon = null;
        if (hero.hasTalent(Talent.PRECISE_ASSAULT)) {
            Buff.prolong(hero, Talent.PreciseAssaultTracker.class, hero.cooldown() + 4f);
        }
        if (hero.hasTalent(Talent.VARIED_CHARGE)) {
            Talent.VariedChargeTracker tracker = hero.buff(Talent.VariedChargeTracker.class);
            if (tracker == null || tracker.weapon == getClass() || tracker.weapon == null) {
                Buff.affect(hero, Talent.VariedChargeTracker.class).weapon = getClass();
            } else {
                tracker.detach();
                MeleeWeapon.Charger charger = Buff.affect(hero, MeleeWeapon.Charger.class);
                charger.gainCharge(hero.pointsInTalent(Talent.VARIED_CHARGE) / 6f);
                ScrollOfRecharging.charge(hero);
            }
        }
        if (hero.hasTalent(Talent.COMBINED_LETHALITY)) {
            Talent.CombinedLethalityAbilityTracker tracker = hero.buff(Talent.CombinedLethalityAbilityTracker.class);
            if (tracker == null || tracker.weapon == this || tracker.weapon == null) {
                Buff.affect(hero, Talent.CombinedLethalityAbilityTracker.class, hero.cooldown()).weapon = this;
            } else {
                //we triggered the talent, so remove the tracker
                tracker.detach();
            }
        }
        if (hero.hasTalent(Talent.COMBINED_ENERGY)) {
            Talent.CombinedEnergyAbilityTracker tracker = hero.buff(Talent.CombinedEnergyAbilityTracker.class);
            if (tracker == null || !tracker.monkAbilused) {
                Buff.prolong(hero, Talent.CombinedEnergyAbilityTracker.class, 5f).wepAbilUsed = true;
            } else {
                tracker.wepAbilUsed = true;
                Buff.affect(hero, MonkEnergy.class).processCombinedEnergy(tracker);
            }
        }
        if (hero.buff(Talent.CounterAbilityTacker.class) != null) {
            MeleeWeapon.Charger charger = Buff.affect(hero, MeleeWeapon.Charger.class);
            charger.gainCharge(hero.pointsInTalent(Talent.COUNTER_ABILITY) * 0.375f);
            hero.buff(Talent.CounterAbilityTacker.class).detach();
        }
    }

    protected void duelistAbility(Hero hero, Integer target) {
        //do nothing by default
    }


    public String abilityInfo() {
        if (levelKnown) {
            return Messages.get(this, "ability_desc", 3 + buffedLvl(), 3 + buffedLvl());
        } else {
            return Messages.get(this, "typical_ability_desc", 3, 3);
        }
    }


    public String upgradeAbilityStat(int level) {
        return Integer.toString(3 + level);
    }

    @Override
    public String info() {
        if (Dungeon.hero != null) {
            String info = super.info();

            info += "\n\n" + Messages.get(RangedWeapon.class, "stats",
                    Math.round(augment.damageFactor(min())),
                    Math.round(augment.damageFactor(max())),
                    STRReq());

            if (STRReq() > Dungeon.hero.STR()) {
                info += " " + Messages.get(Weapon.class, "too_heavy");
            } else if (Dungeon.hero.STR() > STRReq()) {
                info += " " + Messages.get(Weapon.class, "excess_str", Dungeon.hero.STR() - STRReq());
            }

            if (loadedAmmo != null) {
                info += "\n\n" + Messages.get(RangedWeapon.class, "loaded_ammo", loadedAmmo.name());
            }

            info += "\n\n" + Messages.get(MissileWeapon.class, "distance");

            return info;
        } else {
            String info = super.info();
        }
        return super.info();
    }

    @Override
    public int STRReq(int lvl) {
        return STRReq(tier, lvl);
    }

    @Override
    public int min(int lvl) {
        return tier + lvl;
    }

    @Override
    public int max(int lvl) {
        return 5*(tier+1) + lvl*(tier+1);
    }

    @Override
    public int targetingPos(Hero user, int dst) {
        if (loadedAmmo != null) {
            return loadedAmmo.targetingPos(user, dst);
        }
        return dst;
    }

    public static class ChargedShot extends Buff {
        {
            announced = true;
            type = buffType.POSITIVE;
        }

        @Override
        public int icon() {
            return BuffIndicator.DUEL_XBOW;
        }
    }

    private static final String LOADED_AMMO = "loaded_ammo";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(LOADED_AMMO, loadedAmmo);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        loadedAmmo = (MissileWeapon)bundle.get(LOADED_AMMO);
    }
}
