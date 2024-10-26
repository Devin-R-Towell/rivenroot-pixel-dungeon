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
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.BArray;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

abstract public class KindOfWeapon extends EquipableItem {

	protected String hitSound = Assets.Sounds.HIT;
	protected float hitSoundPitch = 1f;
	public static boolean adamantine;

	public static final String AXE 			= "Axe";
	public static final String BLADE 		= "Blade";
	public static final String BLUDGEON 	= "Bludgeon";
	public static final String DAGGER 		= "Dagger";
	public static final String FIST			= "Fist";
	public static final String POLEARM 		= "Polearm";
	public static final String WHIP 		= "Whip";
	public static final String SHIELD		= "Shield";
	public static final String MISC			= "Misc";
	public static final String BOW			= "Bow";
	public static final String ARROW		= "Arrow";
	public static final String TWO_HANDED 	= "TwoHanded";
	public static final String ONE_HANDED	= "OneHanded";

	public String weaponType;
	public String weaponHand;

	public void setWeaponType(String type) { this.weaponType = type; }
	public void setWeaponHand(String hand) { this.weaponHand = hand; }

	public String getWeaponType() { return this.weaponType; }
	public String getWeaponHand() { return this.weaponHand; }

	@Override
	public void execute(Hero hero, String action) {
		if (hero.heroClass == HeroClass.DUELIST && action.equals(AC_EQUIP)) {
			usesTargeting = false;
			String primaryName = Messages.titleCase(hero.belongings.weapon != null ? hero.belongings.weapon.trueName() : Messages.get(KindOfWeapon.class, "empty"));
			String secondaryName = Messages.titleCase(hero.belongings.secondWep != null ? hero.belongings.secondWep.trueName() : Messages.get(KindOfWeapon.class, "empty"));
			if (primaryName.length() > 18) primaryName = primaryName.substring(0, 15) + "...";
			if (secondaryName.length() > 18) secondaryName = secondaryName.substring(0, 15) + "...";
			GameScene.show(new WndOptions(
					new ItemSprite(this),
					Messages.titleCase(name()),
					Messages.get(KindOfWeapon.class, "which_equip_msg"),
					Messages.get(KindOfWeapon.class, "which_equip_primary", primaryName),
					Messages.get(KindOfWeapon.class, "which_equip_secondary", secondaryName)
			) {
				@Override
				protected void onSelect(int index) {
					super.onSelect(index);
					if (index == 0 || index == 1) {
						//In addition to equipping itself, item reassigns itself to the quickslot
						//This is a special case as the item is being removed from inventory, but is staying with the hero.
						int slot = Dungeon.quickslot.getSlot(KindOfWeapon.this);
						slotOfUnequipped = -1;
						if (index == 0) {
							doEquip(hero);
						} else {
							equipSecondary(hero);
						}
						if (slot != -1) {
							Dungeon.quickslot.setSlot(slot, KindOfWeapon.this);
							updateQuickslot();
							//if this item wasn't quickslotted, but the item it is replacing as equipped was
							//then also have the item occupy the unequipped item's quickslot
						} else if (slotOfUnequipped != -1 && defaultAction() != null) {
							Dungeon.quickslot.setSlot(slotOfUnequipped, KindOfWeapon.this);
							updateQuickslot();
						}
					}
				}
			});
		}
		else {
			super.execute(hero, action);
		}
	}

	@Override
	public boolean isEquipped(Hero hero) {
		return hero.belongings.weapon() == this || hero.belongings.secondWep() == this;
	}

	private static boolean isSwiftEquipping = false;

	protected float timeToEquip(Hero hero) {
		return isSwiftEquipping ? 0f : super.timeToEquip(hero);
	}

	@Override
	public boolean doEquip(Hero hero) {

		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)) {
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
				isSwiftEquipping = true;
			}
		}

		detachAll(hero.belongings.backpack);

		if (hero.belongings.weapon == null || hero.belongings.weapon.doUnequip(hero, true)) {

			hero.belongings.weapon = this;
			activate(hero);
			Talent.onItemEquipped(hero, this);
			Badges.validateDuelistUnlock();
			updateQuickslot();

			cursedKnown = true;
			if (cursed) {
				equipCursed(hero);
				GLog.n(Messages.get(KindOfWeapon.class, "equip_cursed"));
			}

			hero.spendAndNext(timeToEquip(hero));
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null) {
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;

		} else {
			isSwiftEquipping = false;
			collect(hero.belongings.backpack);
			return false;
		}
	}

	public boolean equipAmmo(Hero hero) {

		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)) {
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
				isSwiftEquipping = true;
			}
		}

		boolean wasInInv = hero.belongings.contains(this);
		detachAll(hero.belongings.backpack);

		if (hero.belongings.secondWep == null || hero.belongings.secondWep.doUnequip(hero, true)) {

			hero.belongings.ammo = this;
			activate(hero);
			Talent.onItemEquipped(hero, this);
			Badges.validateDuelistUnlock();
			updateQuickslot();

			hero.spendAndNext(timeToEquip(hero));
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null) {
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;

		} else {
			isSwiftEquipping = false;
			collect(hero.belongings.backpack);
			return false;
		}
	}

	public boolean equipSecondary(Hero hero) {

		isSwiftEquipping = false;
		if (hero.belongings.contains(this) && hero.hasTalent(Talent.SWIFT_EQUIP)) {
			if (hero.buff(Talent.SwiftEquipCooldown.class) == null
					|| hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
				isSwiftEquipping = true;
			}
		}

		boolean wasInInv = hero.belongings.contains(this);
		detachAll(hero.belongings.backpack);

		if (hero.belongings.secondWep == null || hero.belongings.secondWep.doUnequip(hero, true)) {

			hero.belongings.secondWep = this;
			activate(hero);
			Talent.onItemEquipped(hero, this);
			Badges.validateDuelistUnlock();
			updateQuickslot();

			cursedKnown = true;
			if (cursed) {
				equipCursed(hero);
				GLog.n(Messages.get(KindOfWeapon.class, "equip_cursed"));
			}

			hero.spendAndNext(timeToEquip(hero));
			if (isSwiftEquipping) {
				GLog.i(Messages.get(this, "swift_equip"));
				if (hero.buff(Talent.SwiftEquipCooldown.class) == null) {
					Buff.affect(hero, Talent.SwiftEquipCooldown.class, 19f)
							.secondUse = hero.pointsInTalent(Talent.SWIFT_EQUIP) == 2;
				} else if (hero.buff(Talent.SwiftEquipCooldown.class).hasSecondUse()) {
					hero.buff(Talent.SwiftEquipCooldown.class).secondUse = false;
				}
				isSwiftEquipping = false;
			}
			return true;

		} else {
			isSwiftEquipping = false;
			collect(hero.belongings.backpack);
			return false;
		}
	}


	@Override
	public boolean doUnequip(Hero hero, boolean collect, boolean single) {
		if (hero == null) {
			return false; // Early exit if hero is null
		}

		// Check if the item is the second weapon or ammo
		boolean isSecondWeapon = hero.belongings.secondWep == this;
		boolean isAmmo = hero.belongings.ammo == this;

		// Handle unequipping the second weapon
		if (isSecondWeapon) {
			// Clear the second weapon slot first
			hero.belongings.secondWep = null;
		} else if (isAmmo) {
			// Clear the ammo slot first
			hero.belongings.ammo = null;
		}

		// Attempt to unequip the item
		if (super.doUnequip(hero, collect, single)) {
			// If not the second weapon or ammo, clear the main weapon
			if (!isSecondWeapon && !isAmmo) {
				hero.belongings.weapon = null;
			}
			return true;
		} else {
			// If unequipping failed, restore the original state
			if (isSecondWeapon) {
				hero.belongings.secondWep = this; // Restore second weapon
			} else if (isAmmo) {
				hero.belongings.ammo = this; // Restore ammo
			}
			return false;
		}
	}
	//right because ammo is going to have to be equipped now.

	public int min() {
		return min(buffedLvl());
	}

	public int max() {
		return max(buffedLvl());
	}

	abstract public int min(int lvl);

	abstract public int max(int lvl);

	public int damageRoll(Char owner) {
		if (owner instanceof Hero) {
			return Hero.heroDamageIntRange(min(), max());
		} else {
			return Random.NormalIntRange(min(), max());
		}
	}

	public float accuracyFactor(Char owner, Char target) {
		return 1f;
	}

	public float delayFactor(Char owner) {
		return 1f;
	}

	public int reachFactor(Char owner) {
		return 1;
	}

	public boolean canReach(Char owner, int target) {

		int reach = reachFactor(owner); // Calculate the reach factor for the owner

		// Check if the distance to the target is greater than the reach
		if (Dungeon.level.distance(owner.pos, target) > reach) {
			return false; // Cannot reach the target
		}

		// Create an array to determine which positions are passable
		boolean[] passable = BArray.not(Dungeon.level.solid, null);

		// Mark positions occupied by other characters as non-passable
		for (Char ch : Actor.chars()) {
			if (ch != owner) {
				passable[ch.pos] = false;
			}
		}

		// Build a distance map from the target position
		PathFinder.buildDistanceMap(target, passable, reach);

		// Check if the owner can reach the target based on the distance map
		return PathFinder.distance[owner.pos] <= reach;
	}

	public int ammoCount() {
		// Check if the hero has ammo
		if (Dungeon.hero.belongings.ammo == null) {
			return 0; // Return 0 if there's no ammo
	} else {
		int ammoQuantity = Dungeon.hero.belongings.ammo.quantity();
		return ammoQuantity;
		}
	}

	public int defenseFactor( Char owner ) {
		return 0;
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		return damage;
	}

	public void hitSound( float pitch ){
		Sample.INSTANCE.play(hitSound, 1, pitch * hitSoundPitch);
	}
}
