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
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Healing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.VialOfBlood;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Random;

public class Dewdrop extends Item {

	private static int item1Count = 0;
	private static int item2Count = 0;
	private static int item3Count = 0;
	private static int item4Count = 0;
	public int dewValue = 1;

	public Dewdrop getRandomItem(int dungeonDepth, int maxDepth) {
		// First, check for guaranteed drops
		Dewdrop guaranteedDrop = checkGuaranteedDrops();
		if (guaranteedDrop != null) {
			return guaranteedDrop;
		}

		// If no guaranteed drop, proceed with random selection
		return getRandomDewdrop(dungeonDepth, maxDepth);
	}

	private Dewdrop checkGuaranteedDrops() {
		if (item4Count >= 64) {
			item4Count = 0;
			item3Count = 0;
			item2Count = 0;
			item1Count = 0;
			return new BlackDewdrop();
		}
		if (item3Count >= 16) {
			item3Count = 0;
			item4Count++;
			return new PurpleDewdrop();
		}
		if (item2Count >= 8) {
			item2Count = 0;
			item3Count++;
			return new RedDewdrop();
		}
		if (item1Count >= 4) {
			item1Count = 0;
			item2Count++;
			return new YellowDewdrop();
		}
		return null;
	}

	private Dewdrop getRandomDewdrop(int dungeonDepth, int maxDepth) {
		Dewdrop blue = new Dewdrop();
		Dewdrop yellow = new YellowDewdrop();
		Dewdrop red = new RedDewdrop();
		Dewdrop purple = new PurpleDewdrop();

		// Calculate weights based on dungeon depth
		double progressFactor = (double) dungeonDepth / maxDepth;
		double[] weights = new double[4];
		weights[0] = Math.max(0.1, 1 - progressFactor); // Blue
		weights[1] = Math.min(0.4, progressFactor); // Yellow
		weights[2] = Math.min(0.3, progressFactor * 0.75); // Red
		weights[3] = Math.min(0.2, progressFactor * 0.5); // Purple

		// Normalize weights
		double totalWeight = 0;
		for (double weight : weights) {
			totalWeight += weight;
		}
		for (int i = 0; i < weights.length; i++) {
			weights[i] /= totalWeight;
		}

		// Weighted random selection
		double random = Random.Float();
		double cumulativeWeight = 0;
		Dewdrop[] dewdrops = {blue, yellow, red, purple};

		for (int i = 0; i < weights.length; i++) {
			cumulativeWeight += weights[i];
			if (random < cumulativeWeight) {
				switch (i) {
					case 0: item1Count++; break;
					case 1: item2Count++; break;
					case 2: item3Count++; break;
					case 3: item4Count++; break;
				}
				return dewdrops[i];
			}
		}
		// Fallback (should never reach here)
		item1Count++;
		return blue;
	}

	{
		image = ItemSpriteSheet.DEWDROP;

		stackable = false;
		dropsDownHeap = true;
		dewValue = 1;
	}

	@Override
	public String name() {
			return Messages.get(Dewdrop.class, "name0");
		}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		Waterskin flask = hero.belongings.getItem(Waterskin.class);
		Catalog.setSeen(getClass());

		if (flask != null && !flask.isFull()) {
			flask.collectDew(this);
			GameScene.pickUp(this, pos);
		} else {
			int terr = Dungeon.level.map[pos];
			if (!consumeDew(1, hero, terr == Terrain.ENTRANCE || terr == Terrain.ENTRANCE_SP
					|| terr == Terrain.EXIT || terr == Terrain.UNLOCKED_EXIT)) {
				return false;
			} else {
				Catalog.countUse(getClass());
			}
		}

		Sample.INSTANCE.play(Assets.Sounds.DEWDROP);
		hero.spendAndNext(TIME_TO_PICK_UP);
		return true;
	}

	public static boolean consumeDew(int quantity, Hero hero, boolean force) {
		// 20 drops for a full heal
		int heal = Math.round(hero.HT * 0.05f * quantity);

		int effect = Math.min(hero.HT - hero.HP, heal);
		int shield = 0;
		if (hero.hasTalent(Talent.SHIELDING_DEW)) {
			shield = heal - effect;
			int maxShield = Math.round(hero.HT * 0.2f * hero.pointsInTalent(Talent.SHIELDING_DEW));
			int curShield = 0;
			if (hero.buff(Barrier.class) != null) curShield = hero.buff(Barrier.class).shielding();
			shield = Math.min(shield, maxShield - curShield);
		}
		if (effect > 0 || shield > 0) {

			if (effect > 0 && quantity > 1 && VialOfBlood.delayBurstHealing()) {
				Healing healing = Buff.affect(hero, Healing.class);
				healing.setHeal(effect, 0, VialOfBlood.maxHealPerTurn());
				healing.applyVialEffect();
			} else {
				hero.HP += effect;
				if (effect > 0) {
					hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(effect), FloatingText.HEALING);
				}
			}

			if (shield > 0) {
				Buff.affect(hero, Barrier.class).incShield(shield);
				hero.sprite.showStatusWithIcon(CharSprite.POSITIVE, Integer.toString(shield), FloatingText.SHIELDING);
			}

		} else if (!force) {
			GLog.i(Messages.get(Dewdrop.class, "already_full"));
			return false;
		}

		return true;
	}

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	// Max of one dew in a stack
	@Override
	public Item merge(Item other) {
		if (isSimilar(other)) {
			quantity = 1;
			other.quantity = 0;
		}
		return this;
	}

	@Override
	public Item quantity(int value) {
		quantity = Math.min(value, 1);
		dewValue = 1;
		return this;
	}

	public class YellowDewdrop extends Dewdrop {
		{
			image = ItemSpriteSheet.YELLOW_DEW;
			dewValue = 3;
		}

		@Override
		public Item quantity(int value) {
			quantity = Math.min(value, 1);
			dewValue = 3;
			return this;
		}
		@Override
		public String name() {
			return Messages.get(Dewdrop.class, "name1");
		}
	}

	public class RedDewdrop extends Dewdrop {
		{
			image = ItemSpriteSheet.RED_DEW;
			dewValue = 5;
		}
		@Override
		public Item quantity(int value) {
			quantity = Math.min(value, 1);
			dewValue = 5;
			return this;
		}
		@Override
		public String name() {
			return Messages.get(Dewdrop.class, "name2");
		}
	}

	public class PurpleDewdrop extends Dewdrop {
		{
			image = ItemSpriteSheet.PURPLE_DEW;
			dewValue = 10;
		}
		@Override
		public Item quantity(int value) {
			quantity = Math.min(value, 1);
			dewValue = 10;
			return this;
		}
		@Override
		public String name() {
			return Messages.get(Dewdrop.class, "name3");
		}
	}

	public class BlackDewdrop extends Dewdrop {

		{
			image = ItemSpriteSheet.BLACK_DEW;
			dewValue = 50;
		}

		@Override
		public Item quantity(int value) {
			quantity = Math.min(value, 1);
			dewValue = 50;
			return this;
		}

		@Override
		public String name() {
			return Messages.get(Dewdrop.class, "name4");
	}

	}
}
