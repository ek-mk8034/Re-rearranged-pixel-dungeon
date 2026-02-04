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

package com.shatteredpixel.shatteredpixeldungeon.items.spells;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.Random;

public abstract class TargetedSpell extends Spell {

	protected int collisionProperties = Ballistica.PROJECTILE;

	@Override
	protected void onCast(Hero hero) {
		// ✅ TargetedSpell은 셀 선택 UI로 들어감
		GameScene.selectCell(targeter);
	}

	/**
	 * 실제 효과(데미지/상태이상/블롭 생성 등)는 각 스펠이 여기서 구현.
	 * 주의: 어떤 스펠은 내부에서 onSpellused()를 직접 호출할 수도 있다.
	 */
	protected abstract void affectTarget( Ballistica bolt, Hero hero );

	/**
	 * 기본 FX (마법 미사일)
	 * 일부 스펠은 이걸 override 해서 다른 연출을 사용할 수 있음 (예: CursedWand).
	 */
	protected void fx( Ballistica bolt, Callback callback ) {
		MagicMissile.boltFromChar( curUser.sprite.parent,
				MagicMissile.MAGIC_MISSILE,
				curUser.sprite,
				bolt.collisionPos,
				callback);
		Sample.INSTANCE.play( Assets.Sounds.ZAP );
	}

	/**
	 * ✅ 소비 처리(수량 감소/턴 소모/은신 해제/퀵슬롯 갱신/카탈로그/재능 트리거)
	 *
	 * - 커스텀 스펠에서 이걸 빼먹으면: hero.busy()만 걸리고 턴이 소비되지 않아
	 *   "턴 인디케이터 무한 회전" 같은 문제가 날 수 있음.
	 *
	 * - 그래서 TargetedSpell 콜백 끝에서 '안 불렸으면' 자동으로 한 번 호출하는 안전망을 둔다.
	 */
	protected void onSpellused(){
		// ✅ 중복 방지 플래그 (이번 시전에서 이미 처리했으면 재호출 방지)
		spellUsedThisCast = true;

		detach( curUser.belongings.backpack );
		Invisibility.dispel();
		updateQuickslot();
		curUser.spendAndNext( timeToCast() );
		Catalog.countUse(getClass());
		if (Random.Float() < talentChance){
			Talent.onScrollUsed(curUser, curUser.pos, talentFactor, getClass());
		}
	}

	protected float timeToCast(){
		return Actor.TICK;
	}

	private static CellSelector.Listener targeter = new CellSelector.Listener(){

		@Override
		public void onSelect( Integer target ) {

			if (target != null) {

				//FIXME this safety check shouldn't be necessary
				//it would be better to eliminate the curItem static variable.
				final TargetedSpell curSpell;
				if (curItem instanceof TargetedSpell) {
					curSpell = (TargetedSpell)curItem;
				} else {
					return;
				}

				final Ballistica shot = new Ballistica( curUser.pos, target, curSpell.collisionProperties );
				int cell = shot.collisionPos;

				curUser.sprite.zap(cell);

				//attempts to target the cell aimed at if something is there, otherwise targets the collision pos.
				if (Actor.findChar(target) != null)
					QuickSlotButton.target(Actor.findChar(target));
				else
					QuickSlotButton.target(Actor.findChar(cell));

				// ✅ 애니메이션 동안 행동 잠금
				curUser.busy();

				curSpell.fx(shot, new Callback() {
					public void call() {
						// 1) 스펠 효과 적용
						curSpell.affectTarget(shot, curUser);

						// 2) ✅ 안전망: 스펠이 직접 onSpellused()를 안 불렀으면 여기서 한 번 호출
						//    (이걸로 커스텀 스펠에서 흔히 나는 "무한 인디케이터 + 수량 미감소" 버그 방지)
						if (!curSpell.spellUsedThisCast) {
							curSpell.onSpellused();
						}
					}
				});

			}

		}

		@Override
		public String prompt() {
			return Messages.get(TargetedSpell.class, "prompt");
		}
	};

}
