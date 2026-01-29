/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2025 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.potions.elixirs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Bee;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;


import com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings.ClericTempleBlessing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;



public class ElixirOfHoneyedHealing extends Elixir {

	{
		image = ItemSpriteSheet.ELIXIR_HONEY;
	}

	private boolean forbidsHealFor(Hero hero){
	    ClericTempleBlessing b = hero.buff(ClericTempleBlessing.class);
	    return b != null && b.forbidsHealingPotion();
	}
	
	@Override
	public void apply(Hero hero) {

	    if (forbidsHealFor(hero)){
	        // ✅ 회복/정화/허기회복 전부 금지할지 선택해야 함
	        // 컨셉상 "회복 엘릭서" 자체를 못 마시게 하는 게 자연스럽기 때문에 여기서 return 추천
	        GLog.w(Messages.get(this, "forbidden"));	
	        return;
   		}

	    PotionOfHealing.cure(hero);
	    PotionOfHealing.heal(hero);
	    Buff.affect(hero, Hunger.class).satisfy(Hunger.HUNGRY/2f);
	    Talent.onFoodEaten(hero, Hunger.HUNGRY/2f, this);
	}
	
	@Override
	public void shatter(int cell) {
		splash( cell );
		if (Dungeon.level.heroFOV[cell]) {
			Sample.INSTANCE.play( Assets.Sounds.SHATTER );
		}
		
		Char ch = Actor.findChar(cell);
		if (ch != null){

		    // ✅ 대상이 Hero이고, 그 Hero가 금지 상태면 회복/정화 스킵
   			if (ch instanceof Hero){
       			Hero h = (Hero) ch;
       			ClericTempleBlessing b = h.buff(ClericTempleBlessing.class);
        		if (b != null && b.forbidsHealingPotion()){
        		    // 회복/정화는 안 주지만, 나머지 로직(벌 아군화)은 그대로
        		} else {
            		PotionOfHealing.cure(ch);
            		PotionOfHealing.heal(ch);
		        }
    		} else {
        		// 다른 캐릭터(몹, 벌 등)는 그대로 회복 가능
        		PotionOfHealing.cure(ch);
        		PotionOfHealing.heal(ch);
		    }
		    if (ch instanceof Bee && ch.alignment != curUser.alignment){
        		ch.alignment = Char.Alignment.ALLY;
        		((Bee)ch).setPotInfo(-1, null);
		    }
		}

		//Char ch = Actor.findChar(cell);
		//if (ch != null){
		//	PotionOfHealing.cure(ch);
		//	PotionOfHealing.heal(ch);
		//	if (ch instanceof Bee && ch.alignment != curUser.alignment){
		//		ch.alignment = Char.Alignment.ALLY;
		//		((Bee)ch).setPotInfo(-1, null);
		//	}
		//}
	}

	//lower values, as it's cheaper to make
	@Override
	public int value() {
		return quantity * 40;
	}

	@Override
	public int energyVal() {
		return 8;
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {
		
		{
			inputs =  new Class[]{PotionOfHealing.class, Honeypot.ShatteredPot.class};
			inQuantity = new int[]{1, 1};
			
			cost = 2;
			
			output = ElixirOfHoneyedHealing.class;
			outQuantity = 1;
		}
		
	}
}
