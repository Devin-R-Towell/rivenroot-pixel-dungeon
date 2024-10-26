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

package com.shatteredpixel.shatteredpixeldungeon.items.stones;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.Recipe;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.BlankScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfIdentify;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfLullaby;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMagicMapping;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfMirrorImage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRage;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRemoveCurse;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRetribution;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTeleportation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTerror;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfUpgrade;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfWildgrowth;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class Runestone extends Item {

	{
		stackable = true;
		defaultAction = AC_THROW;
	}

	@Override
	protected void onThrow(int cell) {
		///inventory stones are thrown like normal items, other stones don't trigger when thrown into pits
		if (this instanceof InventoryStone ||
				(Dungeon.level.pit[cell] && Actor.findChar(cell) == null)) {
			super.onThrow(cell);
		} else {
			Catalog.countUse(getClass());
			activate(cell);
			if (Actor.findChar(cell) == null) Dungeon.level.pressCell(cell);
			Invisibility.dispel();
		}
	}

	protected abstract void activate(int cell);

	@Override
	public boolean isUpgradable() {
		return false;
	}

	@Override
	public boolean isIdentified() {
		return true;
	}

	@Override
	public int value() {
		return 15 * quantity;
	}

	@Override
	public int energyVal() {
		return 3 * quantity;
	}

	public static class PlaceHolder extends Runestone {

		{
			image = ItemSpriteSheet.STONE_HOLDER;
		}

		@Override
		protected void activate(int cell) {
			//does nothing
		}

		@Override
		public boolean isSimilar(Item item) {
			return item instanceof Runestone;
		}

		@Override
		public String info() {
			return "";
		}
	}

	public static class StoneToScroll extends Recipe {
		public static HashMap<Class<? extends Runestone>, Class<? extends Scroll>> scrolls = new HashMap<>();

		static {
			scrolls.put(StoneOfAggression.class,     ScrollOfRage.class);
			scrolls.put(StoneOfAugmentation.class,   ScrollOfTransmutation.class);
			scrolls.put(StoneOfBlast.class,          ScrollOfRetribution.class);
			scrolls.put(StoneOfBlink.class,          ScrollOfTeleportation.class);
			scrolls.put(StoneOfClairvoyance.class,   ScrollOfMagicMapping.class);
			scrolls.put(StoneOfDeepSleep.class,      ScrollOfLullaby.class);
			scrolls.put(StoneOfDisarming.class,      ScrollOfRemoveCurse.class);
			scrolls.put(StoneOfEnchantment.class,    ScrollOfUpgrade.class);
			scrolls.put(StoneOfFear.class,           ScrollOfTerror.class);
			scrolls.put(StoneOfFlock.class,          ScrollOfMirrorImage.class);
			scrolls.put(StoneOfIntuition.class,      ScrollOfIdentify.class);
			scrolls.put(StoneOfShock.class,          ScrollOfRecharging.class);
			scrolls.put(StoneOfNature.class,         ScrollOfWildgrowth.class);
		}

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			if (ingredients.size() != 3) return false;

			boolean hasBlankScroll = false;
			Class<? extends Runestone> stoneClass = null;
			int numStones = 0;

			for (Item ingredient : ingredients) {
				if (ingredient instanceof BlankScroll) {
					hasBlankScroll = true;
				} else if (ingredient instanceof Runestone) {
					if (stoneClass == null) {
						stoneClass = ((Runestone)ingredient).getClass();
						numStones = 1;
					} else if (ingredient.getClass() == stoneClass) {
						numStones++;
					}
				}
			}

			return hasBlankScroll && numStones == 2 && scrolls.containsKey(stoneClass);
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			return 4;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			Class<?extends Runestone> stoneClass = null;
			for (Item ingredient : ingredients) {
				if (ingredient instanceof Runestone) {
					stoneClass = ((Runestone)ingredient).getClass();
					break;
				}
			}

			for (Item ingredient : ingredients) {
				ingredient.quantity(ingredient.quantity() - 1);
			}

			return Reflection.newInstance(scrolls.get(stoneClass));
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			if (!testIngredients(ingredients)) return null;

			for (Item ingredient : ingredients) {
				if (ingredient instanceof Runestone) {
					return Reflection.newInstance(scrolls.get(ingredient.getClass()));
				}
			}
			return null;
		}
	}
}
