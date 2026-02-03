/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2022 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Blob;
import com.shatteredpixel.shatteredpixeldungeon.actors.blobs.Fire;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDragonsBreath;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class FireMaker extends TargetedSpell {

	{
		image = ItemSpriteSheet.FIREMAKER;
		usesTargeting = true;
		talentChance = 1/(float) Recipe.OUT_QUANTITY;
	}

	@Override
	protected void affectTarget(Ballistica bolt, Hero hero) {
		int cell = bolt.collisionPos;

		// 1) 안전망: 맵 밖이면 아무것도 안 하고 "사용 처리"로 턴 종료
		if (!Dungeon.level.insideMap(cell)) {
			onSpellused(); // 턴 소모 + 아이템 소모 + busy 해제 보장
			return;
		}

		// 2) 불 생성
		GameScene.add(Blob.seed(cell, 3, Fire.class));

		// 3) 지형이 불에 의해 변할 수 있으니 맵 갱신(안전)
		GameScene.updateMap(cell);

		// 4) 중요: 스펠 사용 처리(아이템 소모/턴 소모/인비 해제/카운트)
		onSpellused();
	}

	@Override
	public int value() {
		// prices of ingredients, divided by output quantity
		return Math.round((50 + 40) * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	@Override
	public int energyVal() {
		return (int)(14 * (quantity/(float) Recipe.OUT_QUANTITY));
	}

	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe.SimpleRecipe {

		public static final int OUT_QUANTITY = 3;

		{
			inputs =  new Class[]{PotionOfDragonsBreath.class};
			inQuantity = new int[]{1};

			cost = 4;

			output = FireMaker.class;
			outQuantity = OUT_QUANTITY;
		}
	}
}
