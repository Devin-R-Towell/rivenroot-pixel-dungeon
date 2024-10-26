package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import static com.shatteredpixel.shatteredpixeldungeon.items.artifacts.HornOfPlenty.itemSelector;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.MagicImmune;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.StrengthBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.SpellSprite;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.Potion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfMending;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfLife;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Sungrass;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class AaronsHeart extends Artifact {
    {
        image = ItemSpriteSheet.ARTIFACT_AARONS_HEART;

        levelCap = 10;

        defaultAction = "NONE"; //so it can be quickslotted
    }

    private static final String HEART_BONUS = "heart_bonus";
    private int heartBonus;
    public static final String AC_POUR = "POUR";
    public static final String AC_POOR = "POOR";
    private static boolean isEquipped;

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(HEART_BONUS, heartBonus);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        heartBonus = bundle.getInt(HEART_BONUS);
    }

    @Override
    public void charge(Hero target, float amount) {
        //do nothing, this artifact doesn't need to charge
    }

    @Override
    public String status() {
        //return null so no status is shown
        return null;
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        if (isEquipped(hero)
                && level() < levelCap
                && !cursed
                && !hero.isInvulnerable(getClass())
                && hero.buff(MagicImmune.class) == null
                && Dungeon.gold > 0) {
            actions.add(AC_POUR);
            actions.remove(AC_POOR);
            isEquipped = true;
            return actions;
        } else if (isEquipped(hero)
                && level() < levelCap
                && !cursed
                && !hero.isInvulnerable(getClass())
                && hero.buff(MagicImmune.class) == null) {
            actions.add(AC_POOR);
            actions.remove(AC_POUR);
            isEquipped = true;
            return actions;
        } else {
            actions.remove(AC_POUR);
            actions.remove(AC_POOR);
            isEquipped = false;
            return actions;
        }
    }

    @Override
    public String desc() {
        String desc = super.desc();

        if ( isEquipped( Dungeon.hero ) ){
            if (!cursed) {
                if (level() < levelCap)
                    desc += "\n\n" +Messages.get(this, "desc");
            } else {
                desc += "\n\n" +Messages.get(this, "desc_cursed");
            }
        }
        return desc;
    }

    @Override
        public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (hero == null || hero.buff(MagicImmune.class) != null) return;

        if (action.equals(AC_POUR) || action.equals(AC_POOR)) {
            if (!isEquipped(hero)) {
                GLog.i(Messages.get(Artifact.class, "need_to_equip"));
                return;
            }

            hero.sprite.operate(hero.pos);
            hero.busy();
            SpellSprite.show(hero, SpellSprite.STRENGTH);
            Sample.INSTANCE.play(Assets.Sounds.HEART);
            GLog.i(Messages.get(this, "heart"));

            GameScene.selectItem(itemSelector);
        }
    }

    @Override
    public int level() {
        int bonus = heartBonus;
            return (int) (super.level() + bonus);
    }

    @Override
    public boolean doEquip(final Hero hero) {
        if (super.doEquip(hero)) {
            identify();
            Buff.affect(hero, HealthBonus.class);
            return true;
        }
        return false;
    }

    @Override
    public boolean doUnequip(Hero hero, boolean collect, boolean single) {
        Buff.detach(hero, HealthBonus.class);
        return super.doUnequip(hero, collect, single);
    }

    private double HB;
    public class HealthBonus extends ArtifactBuff {
        public int heartBoost(int HT) {
            if (isCursed()) {
                return (int)(-0.1f * HT);
            }

            //only apply this bonus if equipped and not cursed
            if (isEquipped(Dungeon.hero) && !isCursed()) {
                double bonus = (level() * 0.01f * HT) / 2;
                return Math.max(1, (int)bonus);  //minimum bonus of 1 if not cursed
            }

            return 0;
        }

        @Override
        public boolean act() {
            spend(TICK);
            return true;
        }
    }

    @Override
    protected ArtifactBuff passiveBuff() {
        return new HealthBonus();
    }

    protected static WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return Messages.get(AaronsHeart.class, "prompt");
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        private boolean BlandCheck(Item item) {
            if (!(item instanceof Blandfruit)) return false;
            Blandfruit fruit = (Blandfruit)item;
            return fruit.potionAttrib != null &&
                    (fruit.potionAttrib instanceof PotionOfMending ||
                            fruit.potionAttrib instanceof PotionOfHealing);
        }

        @Override
        public boolean itemSelectable(Item item) {
            return item instanceof PotionOfMending ||
                    item instanceof PotionOfHealing ||
                    item instanceof PotionOfLife ||
                    BlandCheck(item);
        }

            @Override
            public void onSelect( Item item ) {
                if (item != null && ((item instanceof PotionOfMending) || (item instanceof PotionOfHealing) || (item instanceof Blandfruit))) {
                    if (item instanceof Blandfruit && ((Blandfruit) item).potionAttrib == null || (!(BlandCheck(item)))) {
                        GLog.w( Messages.get(AaronsHeart.class, "reject") );
                    } else if (item instanceof PotionOfMending || item instanceof PotionOfHealing || BlandCheck(item)){
                        Hero hero = Dungeon.hero;
                        hero.sprite.operate( hero.pos );
                        hero.busy();
                        hero.spend( Potion.TIME_TO_DRINK );

                        ((AaronsHeart)curItem).heartBonus += 1;

                        item.detach(hero.belongings.backpack);
                        GLog.w( Messages.get(AaronsHeart.class, "levelup") );
                        }
                    } else {
                        Hero hero = Dungeon.hero;
                        hero.sprite.operate( hero.pos );
                        hero.busy();
                        hero.spend( Potion.TIME_TO_DRINK );

                        ((AaronsHeart)curItem).heartBonus += 2;
                        item.detach(hero.belongings.backpack);
                        GLog.w( Messages.get(AaronsHeart.class, "levelup") );
                    }
                }
            };
}



