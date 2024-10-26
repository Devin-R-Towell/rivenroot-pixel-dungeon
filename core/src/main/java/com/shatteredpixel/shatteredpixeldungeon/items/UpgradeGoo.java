package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Degrade;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class UpgradeGoo extends Item {
    private int sprite;
    private int oldLevel;

    {
        image = ItemSpriteSheet.UPGRADEGOO0;
        stackable = false;
        unique = true;
    }

    public UpgradeGoo(int trueLevel) {
        oldLevel = trueLevel / 2;
        sprite = setImage(oldLevel);
        image = sprite;
    }

    private int setImage(int level) {
        if (level < 3 && level != 0) {
            return ItemSpriteSheet.UPGRADEGOO2;
        } else if (level >= 3 && level <= 4) {
            return ItemSpriteSheet.UPGRADEGOO1;
        } else if (level > 4) {
            return ItemSpriteSheet.UPGRADEGOO0;
        }
        return -1;
    }

    public Item upgradeItem(Item item) {
        if (item != null && oldLevel > 0) {
            Degrade.detach(curUser, Degrade.class);

            // Use the new upgrade(levels) method
            item.upgrade(oldLevel);

            curUser.sprite.emitter().start(Speck.factory(Speck.UP), 0.2f, 3);
            GLog.p(Messages.get(this, "upgraded", item.name()));

            Statistics.upgradesUsed += oldLevel;
            Badges.validateItemLevelAquired(item);

            detach(curUser.belongings.backpack);
        }
        return item;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (action.equals(AC_APPLY)) {
            curUser = hero;
            GameScene.selectItem(
                    new WndBag.ItemSelector() {
                        @Override
                        public String textPrompt() {
                            return Messages.get(UpgradeGoo.class, "prompt");
                        }

                        @Override
                        public boolean itemSelectable(Item item) {
                            return  item instanceof Weapon ||
                                    item instanceof Armor  ||
                                    item instanceof Ring   ||
                                    item instanceof Artifact;
                        }

                        @Override
                        public void onSelect(Item item) {
                            if (item != null) {
                                upgradeItem(item);
                            }
                        }
                    }
            );
        }
    }


    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);
        actions.add(AC_APPLY);
        return actions;
    }

    private static final String AC_APPLY = "APPLY";
    private static final String OLD_LEVEL = "oldLevel";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(OLD_LEVEL, oldLevel);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        oldLevel = bundle.getInt(OLD_LEVEL);
    }

    @Override
    public String desc() {
        return Messages.get(UpgradeGoo.class, "desc");
    }

    @Override
    public String name() {
        return Messages.get(UpgradeGoo.class, "name");
    }
}