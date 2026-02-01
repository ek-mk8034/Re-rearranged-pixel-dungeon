package com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings.buffs;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings.SamuraiTempleBlessing;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

/**
 * 납검(검집 상태)에서만 유지되는 "발도 차지" 버프.
 *
 * - 납검 시작(onSheatheStart): charge=0, (요검이면) 최대HP 5% 소모
 * - 자동/수동 집중(onFocus): (요검이면) 최대HP 5% 소모, charge +1 (최대 5)
 *
 * - 요검 피해 배율: 1 + 0.5*(1+charge)  => 1.5x ~ 4.0x
 * - 명검 피해 배율: 1 + 0.10*(1+charge) => 1.1x ~ 1.6x
 * - charge>=2면 100% 명중
 */
public class IaiCharge extends Buff {

    public static final float COST_PCT = 0.05f; // 최대HP 5%
    public static final int MAX_CHARGE = 5;

    private int charge = 0;

    public int charge() {
        return charge;
    }

    /** charge>=2면 확정 명중 */
    public boolean guaranteedHit() {
        return charge >= 2;
    }

    /** 요검 피해 배율 */
    public float yokDamageMult() {
        return 1f + 0.30f * (1 + charge);
    }

    /** 명검 피해 배율 */
    public float myeongDamageMult() {
        return 1f + 0.05f * (1 + charge);
    }

    /**
     * 현재 영웅의 요/명 선택 상태를 읽어서 "지금 적용해야 하는 배율"을 반환.
     * (데미지 계산 코드에서 이 값만 곱해주면 됨)
     */
    public float damageMult(Hero h) {
        if (h == null) return 1f;

        SamuraiTempleBlessing b = h.buff(SamuraiTempleBlessing.class);
        if (b == null) return 1f;

        if (b.path() == SamuraiTempleBlessing.Path.YOK) {
            return yokDamageMult();
        } else if (b.path() == SamuraiTempleBlessing.Path.MYEONG) {
            return myeongDamageMult();
        }
        return 1f;
    }

    /** 납검 시작 시 호출 */
    public void onSheatheStart(Hero h, boolean pay) {
        charge = 0;
        if (pay) payCost(h);
        // 필요하면 디버그:
        // GLog.p("IAI CHARGE START = " + charge);
    }

    /** 집중(자동/수동 공통) 시 호출 */
    public void onFocus(Hero h, boolean pay) {
        if (pay) payCost(h);
        charge = Math.min(MAX_CHARGE, charge + 1);

        // 디버그가 필요할 때만 잠깐 켜기
        GLog.p(charge + " Stack");
    }

    /** 최대HP 5% 비용 지불 (자멸 허용) */
    private void payCost(Hero h) {
        if (h == null) return;
        int dmg = Math.max(1, Math.round(h.HT * COST_PCT));
        h.damage(dmg, this);
    }

    // ---------- save/load ----------
    private static final String B_CHARGE = "charge";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(B_CHARGE, charge);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        charge = bundle.getInt(B_CHARGE);
    }
}
