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

package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.InventoryScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.YellowDewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.RedDewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.PurpleDewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.BlackDewdrop;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.GameMath;


import java.util.ArrayList;

public class Waterskin extends Item {

	private static final int MAX_VOLUME = 100;

	private static final String AC_DRINK = "DRINK";
	private static final String AC_SIP = "SIP";
	private static final String AC_SPLASH = "SPLASH";
	private static final String AC_BLESS = "BLESS";

	private static final float TIME_TO_DRINK = 1f;

	private static final String TXT_STATUS = "%d/%d";

	{
		image = ItemSpriteSheet.WATERSKIN;

		defaultAction = AC_DRINK;

		unique = true;
	}

	private int volume = 0;

	private static final String VOLUME = "volume";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(VOLUME, volume);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		volume = bundle.getInt(VOLUME);
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if ((volume > 0) && (volume < 3)) {
			actions.add(AC_SIP);
			actions.remove(AC_DRINK);
			actions.remove(AC_SPLASH);
			actions.remove(AC_BLESS);
		} else if ((volume >= 3) && (volume < 20)) {
			actions.add(AC_SIP);
			actions.add(AC_DRINK);
			actions.remove(AC_SPLASH);
			actions.remove(AC_BLESS);
		} else if ((volume >= 20) && (volume < 50)) {
			actions.add(AC_SIP);
			actions.add(AC_DRINK);
			actions.add(AC_SPLASH);
			actions.remove(AC_BLESS);
		} else if ((volume >= 50)) {
			actions.add(AC_SIP);
			actions.add(AC_DRINK);
			actions.add(AC_SPLASH);
			actions.add(AC_BLESS);
		}

		return actions;
	}

	@Override
	public void execute(final Hero hero, String action) {

		super.execute(hero, action);

		if (volume > 0) {

			// Action for drinking
			if (action.equals(AC_DRINK)) {
				float missingHealthPercent = 1f - (hero.HP / (float) hero.HT);

				int curShield = hero.buff(Barrier.class) != null ? hero.buff(Barrier.class).shielding() : 0;
				int maxShield = Math.round(hero.HT * 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW));

				if (hero.hasTalent(Talent.SHIELDING_DEW)) {
					float missingShieldPercent = 1f - (curShield / (float) maxShield);
					missingHealthPercent += missingShieldPercent * 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW);
				}

				int dropsNeeded = (int) Math.ceil((missingHealthPercent / 0.05f) - 0.01f);
				dropsNeeded = (int) GameMath.gate(1, dropsNeeded, volume);

				if (Dewdrop.consumeDew(dropsNeeded, hero, true)) {
					volume -= dropsNeeded;
					Catalog.countUses(Dewdrop.class, dropsNeeded);
				}
			}

			// Action for sipping
			else if (action.equals(AC_SIP)) {
				int dropsNeeded = 1;
				if (Dewdrop.consumeDew(dropsNeeded, hero, true)) {
					volume -= dropsNeeded;
					Catalog.countUses(Dewdrop.class, 1);
				}
			}

			// Action for splashing
			else if (action.equals(AC_SPLASH)) {
				if (volume >= 20) {
					Buff.detachAllNegativeBuffs(hero);
					Catalog.countUses(Dewdrop.class, 10);
					volume -= 20;
				}
			}

			// Action for blessing
			else if (action.equals(AC_BLESS)) {
				if (volume >= 50) {
					GameScene.selectItem(ItemSelector);
					volume -= 50;
				}
			}

			hero.spend(TIME_TO_DRINK);
			hero.busy();
			Sample.INSTANCE.play(Assets.Sounds.DRINK);
			hero.sprite.operate(hero.pos);

			updateQuickslot();

		} else {
			GLog.w(Messages.get(this, "empty"));
		}
	}


	@Override
	public String info() {
		String info = super.info();

		if (volume == 0) {
			info += "\n\n" + Messages.get(this, "desc_water");
		} else {
			info += "\n\n" + Messages.get(this, "desc_heal");
		}

		if (isFull()) {
			info += "\n\n" + Messages.get(this, "desc_full");
		}

		return info;
	}

	public void empty() {
		volume = 0;
		updateQuickslot();
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	public boolean isFull() {
		return volume >= MAX_VOLUME;
	}

	public void collectDew(Dewdrop dew) {

		GLog.i(Messages.get(this, "collected"));
			volume += dew.dewValue;

		if (volume >= MAX_VOLUME) {
			volume = MAX_VOLUME;
			GLog.p(Messages.get(this, "full"));
		}

		updateQuickslot();
	}

	public void fill() {
		volume += 20;
		if (volume > MAX_VOLUME) {
			volume = MAX_VOLUME;
		}
		updateQuickslot();
	}

	@Override
	public String status() {
		return Messages.format(TXT_STATUS, volume, MAX_VOLUME);
	}

	protected static WndBag.ItemSelector ItemSelector = new WndBag.ItemSelector() {

		@Override
		public String textPrompt() {
			return Messages.get(Waterskin.class, "bless_prompt");
		}

		@Override
		public boolean itemSelectable(Item item) {
			Catalog.countUses(Dewdrop.class, 50);
			return item.isUpgradable();
		}

		@Override
		public void onSelect(Item item) {
			if (item.cursed) {
				ScrollOfRemoveCurse scroll1 = new ScrollOfRemoveCurse();
				scroll1.forceRead(item);
			} else {
				ScrollOfUpgrade scroll2 = new ScrollOfUpgrade();
				scroll2.forceRead(item);
			}
		}
	};
}
