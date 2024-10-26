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

package com.shatteredpixel.shatteredpixeldungeon.levels.features;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.ArmoredStatue;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.Blacksmith;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.LeafParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.YellowDewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.RedDewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Dewdrop.PurpleDewdrop;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.DriedRose;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.SandalsOfNature;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.missiles.ThrowingStone;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant.Fruit;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.PetrifiedSeed;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.levels.MiningLevel;
import com.shatteredpixel.shatteredpixeldungeon.levels.Terrain;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class HighGrass {

	//prevents items dropped from grass, from trampling that same grass.
	//yes this is a bit ugly, oh well.
	private static boolean freezeTrample = false;

	public static void trample( Level level, int pos ) {

		if (freezeTrample) return;

		Char ch = Actor.findChar(pos);

		if (level.map[pos] == Terrain.FURROWED_GRASS){
			if (ch instanceof Hero && ((Hero) ch).heroClass == HeroClass.HUNTRESS){
				//Do nothing
				freezeTrample = true;
			} else {
				Level.set(pos, Terrain.GRASS);
			}

		} else {
			if (ch instanceof Hero && ((Hero) ch).heroClass == HeroClass.HUNTRESS){
				Level.set(pos, Terrain.FURROWED_GRASS);
				freezeTrample = true;
			} else {
				Level.set(pos, Terrain.GRASS);
			}

			int naturalismLevel = 0;

			if (ch != null) {
				SandalsOfNature.Naturalism naturalism = ch.buff(SandalsOfNature.Naturalism.class);
				if (naturalism != null) {
					if (!naturalism.isCursed()) {
						naturalismLevel = naturalism.itemLevel() + 1;
						naturalism.charge();
					} else {
						naturalismLevel = -1;
					}
				}

				//berries try to drop with a 5% chance that halves every 5 floor levels
				//berries can be found by anyone but the Huntress has a greater chance to find them.

				if (ch instanceof Hero) {
					int berriesAvailable = 2 + 2 * ((Hero) ch).pointsInTalent(Talent.NATURES_BOUNTY) * Dungeon.depth / 2;

					Talent.NatureBerriesDropped dropped = Buff.affect(ch, Talent.NatureBerriesDropped.class);
					berriesAvailable -= dropped.count();

					if (berriesAvailable > 0) {
						// Base find rate starts at 5% and halves every 5 floors
						double baseFindRate = 0.05 / Math.pow(2, (Dungeon.depth - 1) / 5);

						// Talent multiplier significantly boosts the find rate
						double talentMultiplier = 1 + 0.25 * ((Hero) ch).pointsInTalent(Talent.NATURES_BOUNTY);  // Example: +25% per talent point

						// Final find rate
						double finalFindRate = baseFindRate * talentMultiplier;

						// Cap the find rate to ensure it doesn't get too high
						finalFindRate = Math.min(finalFindRate, 0.5);  // max 5%

						if (Random.Float() < finalFindRate) {
							dropped.countUp(1);
							level.drop(Generator.random(Generator.Category.FRUIT), pos).sprite.drop();
						}
					}
				}
			}

			//grass gives 1/3 the normal amount of loot in fungi level
			if (Dungeon.level instanceof MiningLevel
					&& Blacksmith.Quest.Type() == Blacksmith.Quest.FUNGI
					&& Random.Int(3) != 0){
				naturalismLevel = -1;
			}

			if (naturalismLevel >= 0) {
				// Seed, scales from 1/25 to 1/9
				float lootChance = 1/(25f - naturalismLevel*4f);

				// absolute max drop rate is ~1/6.5 with footwear of nature, ~1/18 without
				lootChance *= PetrifiedSeed.grassLootMultiplier();

				if (Random.Float() < lootChance) {
					if (Random.Float() < PetrifiedSeed.stoneInsteadOfSeedChance()) {
						level.drop(Generator.randomUsingDefaults(Generator.Category.STONE), pos).sprite.drop();
					} else {
						level.drop(Generator.random(Generator.Category.SEED), pos).sprite.drop();
					}
				}

				// Dew, scales from 1/5 to 1/2
				lootChance = 1/(5f -naturalismLevel/2f);

				//grassy levels spawn half as much dew
				if (Dungeon.level != null && Dungeon.level.feeling == Level.Feeling.GRASS){
					lootChance /= 2;
				}

				if (Random.Float() < lootChance) {
					Dewdrop dewdropGenerator = new Dewdrop();
					Dewdrop dewdrop = dewdropGenerator.getRandomItem(Dungeon.depth, 30);
					level.drop(dewdrop, pos).sprite.drop();
					// 1/4th the normal loot chance for dew.
					if (Random.Float() < lootChance /4) {
						int nDrops = Random.NormalIntRange(3, 6);

						ArrayList<Integer> candidates = new ArrayList<>();
						for (int i : PathFinder.NEIGHBOURS8){
							if (Dungeon.level.passable[pos+i]
									&& pos+i != Dungeon.level.entrance()
									&& pos+i != Dungeon.level.exit()){
								candidates.add(pos+i);
							}
						}
						for (int i = 0; i < nDrops && !candidates.isEmpty(); i++){
							Integer c = Random.element(candidates);
							if (Dungeon.level.heaps.get(c) == null) {
								Dungeon.level.drop(new Dewdrop(), c).sprite.drop(pos);
							} else {
								Dungeon.level.drop(new Dewdrop(), c).sprite.drop(c);
							}
							candidates.remove(c);
						}
					}
				}
				//stones can be found in grass occasionally
				// ~1 % chance
				if (Random.Int(100) <=1 ) {
					ThrowingStone stone = new ThrowingStone();
					Dungeon.level.drop(stone, pos).sprite.drop();
				}
			}

			//Camouflage
			if (ch instanceof Hero) {
				Hero hero = (Hero) ch;
				if (hero.belongings.armor() != null && hero.belongings.armor().hasGlyph(Camouflage.class, hero)) {
					Camouflage.activate(hero, hero.belongings.armor.buffedLvl());
				}
			} else if (ch instanceof DriedRose.GhostHero){
				DriedRose.GhostHero ghost = (DriedRose.GhostHero) ch;
				if (ghost.armor() != null && ghost.armor().hasGlyph(Camouflage.class, ghost)){
					Camouflage.activate(ghost, ghost.armor().buffedLvl());
				}
			} else if (ch instanceof ArmoredStatue){
				ArmoredStatue statue = (ArmoredStatue) ch;
				if (statue.armor() != null && statue.armor().hasGlyph(Camouflage.class, statue)){
					Camouflage.activate(statue, statue.armor().buffedLvl());
				}
			}

		}

		freezeTrample = false;

		if (ShatteredPixelDungeon.scene() instanceof GameScene) {
			GameScene.updateMap(pos);

			CellEmitter.get(pos).burst(LeafParticle.LEVEL_SPECIFIC, 4);
			if (Dungeon.level.heroFOV[pos]) Dungeon.observe();
		}
	}
}
