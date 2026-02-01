package com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.watabou.utils.Bundle;

/**
 * 사무라이 전용 "신전 축복" (OldAmulet로 선택한 요검/명검 경로를 영구 저장)
 */
public class SamuraiTempleBlessing extends Buff {

    public enum Path { YOK, MYEONG }

    private Path path = Path.YOK; // 기본값

    public Path path() { return path; }

    public void setPath(Path p) {
        if (p != null) path = p;
    }

    /** hero에게 축복을 보장(없으면 생성, 있으면 갱신) */
    public static SamuraiTempleBlessing ensure(Hero hero, Path p) {
        SamuraiTempleBlessing b = hero.buff(SamuraiTempleBlessing.class);
        if (b == null) b = Buff.affect(hero, SamuraiTempleBlessing.class);
        b.setPath(p);
        return b;
    }

    // ---------- save/load ----------
    private static final String B_PATH = "path";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(B_PATH, path.name());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        try {
            path = Path.valueOf(bundle.getString(B_PATH));
        } catch (Exception e) {
            path = Path.YOK;
        }
    }

}
