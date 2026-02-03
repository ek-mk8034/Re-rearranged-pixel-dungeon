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

package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Burning;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.FlameParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.plants.Plant;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;

public class Fire extends Blob {

	@Override
	protected void evolve() {

		// flammable map (Terrain.FLAMABLE 기반으로 buildFlagMaps에서 채워짐)
		boolean[] flamable = Dungeon.level.flamable;

		// 맵 크기 (height()가 없을 수도 있어서 length/width로 계산)
		int w = Dungeon.level.width();
		int h = Dungeon.level.length() / w;

		int cell;
		int fire;

		Freezing freeze = (Freezing) Dungeon.level.blobs.get(Freezing.class);

		boolean observe = false;

		// area는 확장될 수 있고, 원본 로직상 left-1/top-1까지 훑기 때문에 범위를 clamp하는 게 가장 안전
		int x0 = Math.max(0, area.left - 1);
		int x1 = Math.min(w - 1, area.right);
		int y0 = Math.max(0, area.top - 1);
		int y1 = Math.min(h - 1, area.bottom);

		for (int x = x0; x <= x1; x++) {
			for (int y = y0; y <= y1; y++) {

				cell = x + y * w;

				if (cur[cell] > 0) {

					// Freezing과 상쇄: 얼음이 있으면 불 제거 + 얼음도 제거
					if (freeze != null && freeze.volume > 0 && freeze.cur[cell] > 0) {
						freeze.clear(cell);
						off[cell] = cur[cell] = 0;
						continue;
					}

					// 캐릭터/아이템/식물에 불 효과 적용
					burn(cell);

					// 불의 세기 감소
					fire = cur[cell] - 1;

					// 이번 틱에 불이 꺼지는 프레임이고, 해당 타일이 가연성이면 지형 파괴(책장/바리케이드 등)
					if (fire <= 0 && flamable[cell]) {
						Dungeon.level.destroy(cell);
						observe = true;
						GameScene.updateMap(cell);
					}

				} else if (freeze == null || freeze.volume <= 0 || freeze.cur[cell] <= 0) {

					// 4방향에 인접한 불이 있는지 확인 (좌우 wrap 방지 + 경계 안전)
					boolean nearFire =
							(x > 0       && cur[cell - 1] > 0) ||
							(x < w - 1   && cur[cell + 1] > 0) ||
							(y > 0       && cur[cell - w] > 0) ||
							(y < h - 1   && cur[cell + w] > 0);

					// 가연성 + 인접 불이면 새 불 생성
					if (flamable[cell] && nearFire) {
						fire = 4;
						burn(cell);
						// blob 활성 영역 확장
						area.union(x, y);
					} else {
						fire = 0;
					}

				} else {
					// Freezing이 있는 칸에는 불이 생기지 않음
					fire = 0;
				}

				// off에 다음 상태 저장 + volume 누적
				volume += (off[cell] = fire);
			}
		}

		// 지형이 바뀌었으면 시야/탐색 갱신
		if (observe) {
			Dungeon.observe();
		}
	}

	// 불이 타는 칸에서 발생하는 실제 효과(캐릭터/아이템/식물)
	public static void burn(int pos) {

		// 캐릭터: Burning 부여/갱신
		Char ch = Actor.findChar(pos);
		if (ch != null && !ch.isImmune(Fire.class)) {
			Buff.affect(ch, Burning.class).reignite(ch);
		}

		// 바닥 아이템 더미: 태우기
		Heap heap = Dungeon.level.heaps.get(pos);
		if (heap != null) {
			heap.burn();
		}

		// 식물: 시들게 처리
		Plant plant = Dungeon.level.plants.get(pos);
		if (plant != null) {
			plant.wither();
		}
	}

	@Override
	public void use(BlobEmitter emitter) {
		super.use(emitter);
		// 불 파티클 효과
		emitter.pour(FlameParticle.FACTORY, 0.03f);
	}

	@Override
	public String tileDesc() {
		return Messages.get(this, "desc");
	}
}
