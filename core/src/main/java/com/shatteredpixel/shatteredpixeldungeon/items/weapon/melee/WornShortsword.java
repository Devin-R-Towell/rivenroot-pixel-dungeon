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

package com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.util.Arrays;

public class WornShortsword extends MeleeWeapon {

	{
		image = ItemSpriteSheet.WORN_SHORTSWORD;
		hitSound = Assets.Sounds.HIT_SLASH;
		hitSoundPitch = 1.1f;

		tier = 1;

		bones = false;
	}

	private static Weapon.Enchantment enchantment;

	public static final String AC_REFORGE = "REFORGE";

	public boolean canTransferEnchantment() {
		if (enchantment == null) {
			return false;
		}
		if (Dungeon.hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) == 2) {
			return true;
		} else if (Dungeon.hero.pointsInTalent(Talent.RUNIC_TRANSFERENCE) == 1
				&& (Arrays.asList(Weapon.Enchantment.common).contains(enchantment.getClass())
				|| Arrays.asList(Armor.Glyph.uncommon).contains(enchantment.getClass()))) {
			return true;
		} else {
			return false;
		}
	}

	public Weapon.Enchantment getEnchantment() {
		return enchantment;

	}

	public void setEnchantment(Weapon.Enchantment enchantment) {
		this.enchantment = enchantment;
	}

	@Override
	protected int baseChargeUse(Hero hero, Char target) {
		if (hero.buff(Sword.CleaveTracker.class) != null) {
			return 0;
		} else {
			return 1;
		}
	}

	@Override
	public String targetingPrompt() {
		return Messages.get(this, "prompt");
	}

	@Override
	protected void duelistAbility(Hero hero, Integer target) {
		//+(3+lvl) damage, roughly +55% base dmg, +67% scaling
		int dmgBoost = augment.damageFactor(3 + buffedLvl());
		Sword.cleaveAbility(hero, target, 1, dmgBoost, this);
	}

	@Override
	public String abilityInfo() {
		int dmgBoost = levelKnown ? 3 + buffedLvl() : 3;
		if (levelKnown) {
			return Messages.get(this, "ability_desc", augment.damageFactor(min() + dmgBoost), augment.damageFactor(max() + dmgBoost));
		} else {
			return Messages.get(this, "typical_ability_desc", min(0) + dmgBoost, max(0) + dmgBoost);
		}
	}

	public String upgradeAbilityStat(int level) {
		int dmgBoost = 3 + level;
		return augment.damageFactor(min(level) + dmgBoost) + "-" + augment.damageFactor(max(level) + dmgBoost);
	}

	@Override
	public void onSelect(Item item) {

	}

	@Override
	public String name() {
		return enchantment != null ? enchantment.name(super.name()) : super.name();
	}

	@Override
	public String info() {
		String info = super.info();
		if (enchantment != null) {
			info += "\n\n" + Messages.get(this, "inscribed", enchantment.name());
			info += " " + enchantment.desc();
		}
		return info;
	}

	@Override
	//scroll of upgrade can be used directly once, same as upgrading weapon the WornShortsword is reforged to.
	public boolean isUpgradable() {
		return level() == 0;
	}

	protected static WndBag.ItemSelector weaponSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(WornShortsword.class, "prompt");
		}

		@Override
		public Class<? extends Bag> preferredBag() {
			return Belongings.Backpack.class;
		}

		@Override
		public boolean itemSelectable(Item item) {
			return item instanceof Weapon;
		}

		@Override
		public void onSelect(Item item) {
			WornShortsword sword = (WornShortsword) curItem;
			if (item != null && item instanceof Weapon) {
				Weapon weapon = (Weapon) item;
				if (!weapon.levelKnown) {
					GLog.w(Messages.get(WornShortsword.class, "unknown_weapon"));

				} else if (weapon.cursed && (sword.getEnchantment() == null || !sword.getEnchantment().curse())) {
					GLog.w(Messages.get(WornShortsword.class, "cursed_weapon"));

				} else if (weapon.enchantment != null && sword.getEnchantment() != null
						&& weapon.enchantment.getClass() != sword.getEnchantment().getClass()) {
					GameScene.show(new WndOptions(new ItemSprite(sword),
							Messages.get(WornShortsword.class, "choose_title"),
							Messages.get(WornShortsword.class, "choose_desc"),
							weapon.enchantment.name(),
							sword.getEnchantment().name()) {
						@Override
						protected void onSelect(int index) {
							if (index == 0) sword.setEnchantment(null);
							//if index is 1, then the glyph transfer happens in affixSeal

							GLog.p(Messages.get(WornShortsword.class, "reforge"));
							Dungeon.hero.sprite.operate(Dungeon.hero.pos);
							Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
							weapon.reforgeSword(sword);
							sword.detach(Dungeon.hero.belongings.backpack);
						}
					});
				} else {
					GLog.p(Messages.get(WornShortsword.class, "reforge"));
					Dungeon.hero.sprite.operate(Dungeon.hero.pos);
					Sample.INSTANCE.play(Assets.Sounds.UNLOCK);
					weapon.reforgeSword((WornShortsword) curItem);
					curItem.detach(Dungeon.hero.belongings.backpack);
				}
			}
		}

		;

		private static final String ENCHANTMENT = "enchantment";

		@Override
		public void storeInBundle(Bundle bundle) {
			super.storeInBundle(bundle);
			bundle.put(ENCHANTMENT, enchantment);
		}

		@Override
		public void restoreFromBundle(Bundle bundle) {
			super.restoreFromBundle(bundle);
			enchantment = (Weapon.Enchantment) bundle.get(ENCHANTMENT);
		}
	};
}

