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

package com.shatteredpixel.shatteredpixeldungeon.items.trinkets;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.journal.Document;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.AlchemyScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.scenes.PixelScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.ItemButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RedButton;
import com.shatteredpixel.shatteredpixeldungeon.ui.RenderedTextBlock;
import com.shatteredpixel.shatteredpixeldungeon.ui.Window;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.IconTitle;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndInfoItem;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndSadGhost;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * TrinketCatalyst: 알케미에서 사용하면 트링킷 선택 창을 띄워주는 "촉매" 아이템
 *
 * 큰 흐름:
 * 1) 촉매 아이템(TrinketCatalyst)을 알케미 재료로 넣는다
 * 2) Recipe.brew가 호출되며 선택창(WndTrinket)을 띄운다
 * 3) 플레이어가 버튼을 누르고 확인(RewardWindow)하면 결과 트링킷을 지급한다
 * 4) 촉매를 소모(detach)하고 저장/통계/배지 갱신
 **/

public class TrinketCatalyst extends Item {

	{
		// 인게임 아이콘(스프라이트) 지정
		image = ItemSpriteSheet.TRINKET_CATA;

		// unique=true면 보통 "중복 소지 제한" 성격(게임 룰)
		unique = true;
	}

	@Override
	public boolean isIdentified() {
		// 촉매는 식별 필요 없이 항상 식별된 상태
		return true;
	}

	@Override
	public boolean isUpgradable() {
		// 강화/업그레이드 불가
		return false;
	}

	@Override
	public boolean doPickUp(Hero hero, int pos) {
		// 기본 아이템 줍기 로직을 수행
		if (super.doPickUp(hero, pos)){

			// 알케미 가이드 페이지를 아직 안 읽었으면 "가이드 플래시"로 안내
			if (!Document.ADVENTURERS_GUIDE.isPageRead(Document.GUIDE_ALCHEMY)){
				GameScene.flashForDocument(Document.ADVENTURERS_GUIDE, Document.GUIDE_ALCHEMY);
			}
			return true;

		} else {
			return false;
		}
	}

	/**
	 * rolledTrinkets:
	 * 선택창에서 보여줄 후보 목록을 저장해 둠.
	 *
	 * - 앞의 3개: 실제 트링킷 3개(중복 제거)
	 * - 4번째: RandomTrinket(‘?’ 아이콘) = 랜덤 선택지 버튼(플레이스홀더)
	 *
	 * 왜 저장하나?
	 * - 선택창을 열어둔 상태에서 게임 종료/복귀해도 "후보가 바뀌지 않게" 하기 위해
	 */
	public ArrayList<Item> rolledTrinkets = new ArrayList<>();

	public boolean hasRolledTrinkets(){
		return !rolledTrinkets.isEmpty();
	}

	private static final String ROLLED_TRINKETS = "rolled_trinkets";

	/**
	 * 저장(세이브) 시 rolledTrinkets도 함께 저장
	 */
	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		if (!rolledTrinkets.isEmpty()){
			bundle.put(ROLLED_TRINKETS, rolledTrinkets);
		}
	}

	/**
	 * 로드(불러오기) 시 rolledTrinkets 복원
	 */
	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		rolledTrinkets.clear();
		if (bundle.contains(ROLLED_TRINKETS)){
			rolledTrinkets.addAll((Collection<Item>) ((Collection<?>)bundle.getCollection(ROLLED_TRINKETS)));
		}
	}

	/**
	 * 알케미 레시피:
	 * "재료 1개가 TrinketCatalyst"인 경우에만 매칭되며,
	 * 에너지 6을 소비해서 선택창을 띄워주는 레시피.
	 *
	 * 주의: 여기서 바로 결과 아이템을 반환하지 않고(WndTrinket에서 선택 후 지급)
	 * 선택 UI를 띄우고 null 리턴함.
	 */
	public static class Recipe extends com.shatteredpixel.shatteredpixeldungeon.items.Recipe {

		@Override
		public boolean testIngredients(ArrayList<Item> ingredients) {
			// 촉매 1개만 넣었을 때만 이 레시피가 성립
			return ingredients.size() == 1 && ingredients.get(0) instanceof TrinketCatalyst;
		}

		@Override
		public int cost(ArrayList<Item> ingredients) {
			// 알케미 에너지 소모량
			return 6;
		}

		@Override
		public Item brew(ArrayList<Item> ingredients) {

			/**
			 * 핵심 아이디어:
			 * - 선택창에서 트링킷을 고르는 동안 게임을 종료해도 촉매를 잃지 않게 "보험" 처리
			 * - 그래서 촉매를 duplicate()해서 다시 인벤에 조용히 넣고,
			 * - 원래 재료로 들어온 촉매는 quantity(0)으로 소모 처리
			 * - 그리고 선택창을 띄움
			 */
			TrinketCatalyst newCata = (TrinketCatalyst) ingredients.get(0).duplicate();
			newCata.collect();

			// 알케미 입력 슬롯의 기존 촉매는 소모된 것으로 처리
			ingredients.get(0).quantity(0);

			// 선택창 띄우기
			ShatteredPixelDungeon.scene().addToFront(new WndTrinket(newCata));

			// 알케미 화면은 pause 저장 흐름이 애매할 수 있어서 여기서 한번 강제 저장
			try {
				Dungeon.saveAll();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			// 여기서는 실제 결과 아이템을 만들어 반환하지 않음(선택창에서 지급)
			return null;
		}

		@Override
		public Item sampleOutput(ArrayList<Item> ingredients) {
			// 알케미 UI의 "출력 미리보기"용 더미(placeholder)
			return new Trinket.PlaceHolder();
		}
	}

	/**
	 * RandomTrinket:
	 * 4번째 버튼(랜덤 선택지)을 표시하기 위한 "플레이스홀더 아이템"
	 * 실제 트링킷이 아니고, 확인 시점에 진짜 트링킷을 뽑아 result로 바꿈.
	 */
	public static class RandomTrinket extends Item {

		{
			// '?’ 아이콘(무언가) 표시
			image = ItemSpriteSheet.SOMETHING;
		}

	}

	/**
	 * WndTrinket:
	 * 촉매를 알케미에서 사용했을 때 뜨는 "트링킷 선택 창"
	 * - 앞의 3개는 서로 다른 트링킷 후보
	 * - 4번째는 랜덤 버튼(RandomTrinket)
	 */
	public static class WndTrinket extends Window {

		private static final int WIDTH		= 120;
		private static final int BTN_SIZE	= 24;
		private static final int BTN_GAP	= 4;
		private static final int GAP		= 2;

		public static final int NUM_TRINKETS = 4; // last one is a random choice


		public WndTrinket( TrinketCatalyst cata ){

			// 타이틀 바(아이콘 + 제목)
			IconTitle titlebar = new IconTitle();
			titlebar.icon(new ItemSprite(cata));
			titlebar.label(Messages.titleCase(Messages.get(TrinketCatalyst.class, "window_title")));
			titlebar.setRect(0, 0, WIDTH, 0);
			add(titlebar);

			// 설명 텍스트
			RenderedTextBlock message = PixelScene.renderTextBlock(
					Messages.get(TrinketCatalyst.class, "window_text"), 6);
			message.maxWidth(WIDTH);
			message.setPos(0, titlebar.bottom() + GAP);
			add(message);

			/**
			 * [후보 생성]
			 * - 앞의 3개: 서로 다른 트링킷(클래스 기준 중복 제거)
			 */
			while (cata.rolledTrinkets.size() < NUM_TRINKETS - 1) {

				Item rolled = Generator.random(Generator.Category.TRINKET);

				// 이미 뽑힌 트링킷과 "같은 클래스"면 중복으로 판단하고 버림
				boolean dup = false;
				for (int j = 0; j < cata.rolledTrinkets.size(); j++) {
					Item existing = cata.rolledTrinkets.get(j);
					if (existing != null && existing.getClass() == rolled.getClass()) {
						dup = true;
						break;
					}
				}

				// 중복이 아니면 후보 목록에 추가
				if (!dup) {
					cata.rolledTrinkets.add(rolled);
				}
			}

			/**
			 * 4번째는 랜덤 버튼(플레이스홀더)
			 * - 실제 트링킷이 아니라, confirm 시점에 진짜 트링킷으로 바뀜
			 */
			if (cata.rolledTrinkets.size() == NUM_TRINKETS - 1) {
				cata.rolledTrinkets.add(new RandomTrinket());
			}

			/**
			 * [버튼 생성]
			 * - 4개 버튼을 가로로 배치
			 * - 버튼 클릭 시 RewardWindow(확인 창)를 띄움
			 */
			for (int i = 0; i < NUM_TRINKETS; i++){
				ItemButton btnReward = new ItemButton() {
					@Override
					protected void onClick() {
						ShatteredPixelDungeon.scene().addToFront(new RewardWindow(item()));
					}
				};

				btnReward.item(cata.rolledTrinkets.get(i));

				btnReward.setRect(
						(i+1)*(WIDTH - BTN_GAP) / NUM_TRINKETS - BTN_SIZE,
						message.top() + message.height() + BTN_GAP,
						BTN_SIZE,
						BTN_SIZE
				);

				add(btnReward);
			}

			// 창 크기 확정
			resize(WIDTH, (int)(message.top() + message.height() + 2*BTN_GAP + BTN_SIZE));
		}

		@Override
		public void onBackPressed() {
			// 뒤로가기 막음(선택창을 닫아서 촉매 소비/취소 이슈가 생기는 걸 방지)
		}

		/**
		 * RewardWindow:
		 * - WndInfoItem 기반(아이템 정보창) + Confirm/Cancel 버튼
		 * - Confirm 시에 실제 트링킷 지급/촉매 소모/저장 등을 수행
		 */
		private class RewardWindow extends WndInfoItem {

			public RewardWindow( Item item ) {
				super(item);

				RedButton btnConfirm = new RedButton(Messages.get(WndSadGhost.class, "confirm")){
					@Override
					protected void onClick() {
						// 창 닫기
						RewardWindow.this.hide();
						WndTrinket.this.hide();

						/**
						 * [결과 결정]
						 * - 일반 버튼이면 그 아이템 그대로 결과
						 * - 랜덤 버튼(RandomTrinket)이면:
						 *   "전체 트링킷 풀"에서 랜덤을 뽑되,
						 *   "앞의 3개로 이미 제시된 트링킷"은 제외하고 뽑음
						 */
						Item result = item;
						if (result instanceof RandomTrinket){

							// 앞의 3개를 제외 목록으로 수집(클래스 기준)
							TrinketCatalyst cataForExclude =
									Dungeon.hero.belongings.getItem(TrinketCatalyst.class);

							ArrayList<Class<?>> exclude = new ArrayList<>();

							if (cataForExclude != null && cataForExclude.hasRolledTrinkets()){
								for (int i = 0; i < NUM_TRINKETS - 1 && i < cataForExclude.rolledTrinkets.size(); i++){
									Item it = cataForExclude.rolledTrinkets.get(i);
									if (it != null) exclude.add(it.getClass());
								}
							}

							// 제외 목록에 걸리면 다시 뽑는 방식(무한루프 방지 safety 포함)
							Item rolled;
							int safety = 100;
							do {
								rolled = Generator.random(Generator.Category.TRINKET);
								safety--;
							} while (safety > 0 && exclude.contains(rolled.getClass()));

							result = rolled;
						}

						/**
						 * [촉매 소모 + 지급 처리]
						 * - 인벤에서 촉매를 찾아서 제거(detach)해야 최종 소비 확정
						 */
						TrinketCatalyst cata = Dungeon.hero.belongings.getItem(TrinketCatalyst.class);

						if (cata != null) {

							// 촉매 소모 확정
							cata.detach(Dungeon.hero.belongings.backpack);

							// 사용 기록(저널/카탈로그 용도)
							Catalog.countUse(cata.getClass());

							// 결과 아이템 식별 처리
							result.identify();

							// 현재 씬이 알케미면 알케미 제작 흐름(craftItem)로 처리
							if (ShatteredPixelDungeon.scene() instanceof AlchemyScene) {

								// craftItem 내부에서 연출/통계/저장 처리까지 같이 정리됨
								((AlchemyScene) ShatteredPixelDungeon.scene()).craftItem(null, result);

							} else {
								// 알케미 씬이 아닐 경우: 직접 지급/드랍/통계/저장 처리
								Sample.INSTANCE.play(Assets.Sounds.PUFF);

								if (result.doPickUp(Dungeon.hero)){
									GLog.p(Messages.capitalize(
											Messages.get(Hero.class, "you_now_have", item.name())
									));
								} else {
									Dungeon.level.drop(result, Dungeon.hero.pos);
								}

								// 통계/배지 업데이트
								Statistics.itemsCrafted++;
								Badges.validateItemsCrafted();

								// 저장
								try {
									Dungeon.saveAll();
								} catch (IOException e) {
									ShatteredPixelDungeon.reportException(e);
								}
							}
						}
					}
				};

				// Confirm 버튼 배치
				btnConfirm.setRect(0, height+2, width/2-1, 16);
				add(btnConfirm);

				// Cancel 버튼(정보창만 닫고 다시 선택창으로 돌아감)
				RedButton btnCancel = new RedButton(Messages.get(WndSadGhost.class, "cancel")){
					@Override
					protected void onClick() {
						hide();
					}
				};

				btnCancel.setRect(btnConfirm.right()+2, height+2, btnConfirm.width(), 16);
				add(btnCancel);

				// confirm/cancel까지 포함한 창 크기 조정
				resize(width, (int)btnCancel.bottom());
			}
		}
	}
}