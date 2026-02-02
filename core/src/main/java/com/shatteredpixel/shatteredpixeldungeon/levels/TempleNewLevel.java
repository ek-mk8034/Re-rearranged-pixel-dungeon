package com.shatteredpixel.shatteredpixeldungeon.levels;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Statistics;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.changer.OldAmulet;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.CrystalKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.GoldenKey;
import com.shatteredpixel.shatteredpixeldungeon.items.keys.IronKey;
import com.shatteredpixel.shatteredpixeldungeon.journal.Notes;
import com.shatteredpixel.shatteredpixeldungeon.levels.features.LevelTransition;
import com.shatteredpixel.shatteredpixeldungeon.levels.painters.Painter;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.AlchemyChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.Chamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.ConfusionChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.MazeChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.MimicInTheGrassChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.MineFieldChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.MoistureChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.PiranhaPoolChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.SentryChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.SentryMazeChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.SpearGnollChamber;
import com.shatteredpixel.shatteredpixeldungeon.levels.templeChambers.WarpStoneChamber;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.watabou.noosa.audio.Music;
import com.watabou.utils.Bundle;
import com.watabou.utils.Point;
import com.watabou.utils.Random;
import com.watabou.utils.Rect;
import com.watabou.utils.Reflection;

public class TempleNewLevel extends Level {

    {
        color1 = 0x64bb4c;
        color2 = 0x569545;
    }

    // ----------------------------
    // ✅ 사원에서 "얻은 키" 자동 삭제를 위한 스냅샷
    // ----------------------------
    private int snapshotDepth = -1;      // 스냅샷을 찍은 depth(본층 depth) 기록
    private int snapIron = -1;
    private int snapGold = -1;
    private int snapCrystal = -1;

    private static final String B_SNAP_DEPTH   = "temple_snap_depth";
    private static final String B_SNAP_IRON    = "temple_snap_iron";
    private static final String B_SNAP_GOLD    = "temple_snap_gold";
    private static final String B_SNAP_CRYSTAL = "temple_snap_crystal";

    public static final String[] CAVES_TRACK_LIST
            = new String[]{Assets.Music.CAVES_1, Assets.Music.CAVES_2, Assets.Music.CAVES_2,
            Assets.Music.CAVES_1, Assets.Music.CAVES_3, Assets.Music.CAVES_3};
    public static final float[] CAVES_TRACK_CHANCES = new float[]{1f, 1f, 0.5f, 0.25f, 1f, 0.5f};

    @Override
    public void playLevelMusic() {
        if (Statistics.amuletObtained || Dungeon.templeCompleted) {
            Music.INSTANCE.play(Assets.Music.CAVES_TENSE, true);
        } else {
            Music.INSTANCE.playTracks(CAVES_TRACK_LIST, CAVES_TRACK_CHANCES, false);
        }
    }

    public static final int PEDESTAL_WALL_OFFSET = 3; // top wall ~ amulet cell 거리
    public static final int PEDESTAL_CHAMBER_OFFSET = 3; // amulet cell ~ chambers top wall 거리
    public static final int ENTRANCE_WALL_OFFSET = 2; // entrance cell ~ bottom wall 거리
    public static final int ENTRANCE_CHAMBER_OFFSET = 3;
    public static final int CHAMBER_X_NUM      = 2;
    public static final int CHAMBER_Y_NUM      = 6;
    public static final int CHAMBER_WIDTH      = 17;
    public static final int CHAMBER_HEIGHT     = 17;
    public static final int CHAMBER_FIRST_X    = 0;
    public static final int CHAMBER_FIRST_Y    = PEDESTAL_WALL_OFFSET + PEDESTAL_CHAMBER_OFFSET + 2;
    public static final int WIDTH  = 1 + CHAMBER_X_NUM*(CHAMBER_WIDTH+1);
    public static final int HEIGHT = ENTRANCE_CHAMBER_OFFSET + ENTRANCE_WALL_OFFSET + CHAMBER_FIRST_Y + CHAMBER_Y_NUM*(CHAMBER_HEIGHT+1) + 3;

    Rect rect = new Rect(0, 0, WIDTH, HEIGHT);

    private int entranceCell() {
        return (int)(WIDTH/2f) + WIDTH*(HEIGHT-4);
    }

    private Point entrancePoint() {
        return this.cellToPoint(entranceCell());
    }

    private int amuletCell() {
        return (int)(WIDTH/2f) + WIDTH*(PEDESTAL_WALL_OFFSET+1);
    }

    private Point amuletPoint() {
        return this.cellToPoint(amuletCell());
    }

    private int centerCell() {
        return (int)(WIDTH/2f) + WIDTH*(int)(WIDTH/2f);
    }

    private Point centerPoint() {
        return this.cellToPoint(centerCell());
    }

    @Override
    public String tilesTex() {
        return Assets.Environment.TILES_TEMPLE;
    }

    @Override
    public String waterTex() {
        return Assets.Environment.WATER_TEMPLE;
    }

    @Override
    protected boolean build() {
        setSize(WIDTH, HEIGHT);

        // ----------------------------
        // 사원 진입/퇴장 전환점 설정
        // ----------------------------
        transitions.add(new LevelTransition(this,
                entranceCell(),
                LevelTransition.Type.BRANCH_ENTRANCE,
                Dungeon.depth,
                0,
                LevelTransition.Type.BRANCH_EXIT));

        transitions.add(new LevelTransition(this,
                amuletCell(),
                LevelTransition.Type.BRANCH_EXIT,
                Dungeon.depth,
                3,
                LevelTransition.Type.BRANCH_ENTRANCE));

        buildLevel();

        // ----------------------------
        // ✅ 사원 레벨이 생성된 시점에 "현재 키 보유량"을 스냅샷으로 저장
        //    - 이후 사원에서 키를 얻더라도, 나갈 때 증가분만 제거 가능
        // ----------------------------
        snapshotTempleKeyCounts();

        return true;
    }

    // ----------------------------
    // chamber build logic
    // ----------------------------
    Class<?>[] chamberClasses = {
            MimicInTheGrassChamber.class,
            PiranhaPoolChamber.class,
            AlchemyChamber.class,
            SentryChamber.class,
            MineFieldChamber.class,
            SentryMazeChamber.class,
            WarpStoneChamber.class,
            SpearGnollChamber.class,
            MazeChamber.class,
            ConfusionChamber.class,
            MoistureChamber.class,
    };
    float[] deck = {1,1,1,1,1,1,1,1,1,1,1};

    private void createChamber(Level level, int left, int top, int right, int bottom, Point center) {
        int index = Random.chances(deck);
        if (index == -1) return;
        deck[index] -= 1;

        Class<?> chamberClass;
        try {
            chamberClass = chamberClasses[index];
        } catch (ArrayIndexOutOfBoundsException e) {
            chamberClass = Chamber.class;
        }

        Chamber finalChamber = (Chamber) Reflection.newInstance(chamberClass);
        if (finalChamber != null) {
            finalChamber.set(level, left, top, right, bottom, center);
            finalChamber.build();
        }
    }

    private void buildLevel() {
        Painter.fill(this, rect, Terrain.WALL);
        Painter.fill(this, rect, 1, Terrain.EMPTY);

        // 사원 중앙 출구(OldAmulet 드랍 위치)
        Painter.set(this, amuletPoint(), Terrain.EXIT);

        for (int x = 0; x < CHAMBER_X_NUM; x++) {
            for (int y = 0; y < CHAMBER_Y_NUM; y++) {

                int left   = CHAMBER_FIRST_X + x*(CHAMBER_WIDTH+1);
                int top    = CHAMBER_FIRST_Y + y*(CHAMBER_HEIGHT+1);
                int right  = left + CHAMBER_WIDTH+2;
                int bottom = top + CHAMBER_HEIGHT+2;
                Point center = new Point(
                        left + Math.round(CHAMBER_WIDTH/2f),
                        top  + Math.round(CHAMBER_HEIGHT/2f)
                );

                // 기본 방 생성 + 랜덤 챔버 생성
                Chamber chamber = new Chamber();
                chamber.set(this, left, top, right, bottom, center);
                chamber.build();

                createChamber(this, left, top, right, bottom, center);

                int door = Terrain.DOOR;

                // door placing logic
                int doorX;

                // 좌/우 문
                doorX = center.x - Math.round(CHAMBER_WIDTH/2f);
                if (doorX != 0 && doorX != WIDTH-1) Painter.set(this, doorX, center.y, door);

                doorX = center.x + Math.round(CHAMBER_WIDTH/2f);
                if (doorX != 0 && doorX != WIDTH-1) Painter.set(this, doorX, center.y, door);

                // 상/하 문
                Painter.set(this, center.x, center.y - Math.round(CHAMBER_HEIGHT/2f), door);
                Painter.set(this, center.x, center.y + Math.round(CHAMBER_HEIGHT/2f), door);

                // reward placing logic
                int remains = this.pointToCell(center);

                int n = Random.IntRange(1, 2);
                for (int i = 0; i < n; i++) {
                    this.drop(prize(this), remains).setHauntedIfCursed().type = Heap.Type.SKELETON;
                }
            }
        }

        // key item placing logic
        this.drop(new OldAmulet(), amuletCell());

        // 입구 타일
        Painter.set(this, entrancePoint(), Terrain.ENTRANCE);
    }

    private static Item prize(Level level) {
        return Generator.randomUsingDefaults(Random.oneOf(
                Generator.Category.POTION,
                Generator.Category.SCROLL,
                Generator.Category.FOOD,
                Generator.Category.GOLD
        ));
    }

    @Override
    protected void createMobs() {
        // 템플은 별도 스폰 없음(현재 코드 유지)
    }

    @Override
    protected void createItems() {
        // 템플은 buildLevel에서 아이템 생성(현재 코드 유지)
    }

    // =========================================================
    // ✅ 핵심: 사원에서 키를 들고 나가면 "사원에서 증가한 키"만 자동 삭제
    // =========================================================

    /**
     * 사원 입장 시점의 Notes 키 보유량을 스냅샷으로 기록한다.
     * - 이때의 depth는 "사원이 연결된 본층 depth" (Dungeon.depth)로 저장.
     * - 스냅샷은 저장/로드 대비해서 Bundle에도 저장한다.
     */
    private void snapshotTempleKeyCounts() {
        // 이미 스냅샷이 찍혀있으면 중복 실행 방지
        if (snapshotDepth >= 0) return;

        snapshotDepth = Dungeon.depth;

        // Notes(키 UI)에 기록된 "현재 키 보유량"을 기준으로 한다.
        snapIron    = Notes.keyCount(new IronKey(snapshotDepth));
        snapGold    = Notes.keyCount(new GoldenKey(snapshotDepth));
        snapCrystal = Notes.keyCount(new CrystalKey(snapshotDepth));
    }

    /**
     * 사원에서 얻어서 "늘어난 키"를 제거한다.
     * - 스냅샷보다 많은 만큼만 제거 => 본층에서 원래 갖고 있던 키는 보존됨.
     */
    private void purgeTempleKeyGainsOnExit() {
        if (snapshotDepth < 0) return; // 스냅샷이 없으면 아무것도 하지 않음

        // 혹시 depth가 바뀐 이상상황 방어
        int d = snapshotDepth;

        // 증가분만 제거 (while로 초과분 제거)
        while (Notes.keyCount(new IronKey(d)) > snapIron) {
            Notes.remove(new IronKey(d));
        }
        while (Notes.keyCount(new GoldenKey(d)) > snapGold) {
            Notes.remove(new GoldenKey(d));
        }
        while (Notes.keyCount(new CrystalKey(d)) > snapCrystal) {
            Notes.remove(new CrystalKey(d));
        }

        // UI 키 표시 갱신
        GameScene.updateKeyDisplay();
    }

    /**
     * Level.activateTransition을 TempleNewLevel에서 override.
     * - 사원에서 "본층으로 나가는" 전환(BRANCH_EXIT)일 때만 키를 정리한다.
     * - Level.java는 수정할 필요 없음.
     */
    @Override
    public boolean activateTransition(com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero hero,
                                      LevelTransition transition) {

        // 사원 -> 본층(원래 던전)으로 나가는 순간만 처리
        if (transition != null && transition.type == LevelTransition.Type.BRANCH_EXIT) {
            purgeTempleKeyGainsOnExit();
        }

        return super.activateTransition(hero, transition);
    }

    // =========================================================
    // ✅ 저장/로드 대응: 스냅샷 값도 같이 저장해줘야 함
    // =========================================================
    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);

        bundle.put(B_SNAP_DEPTH, snapshotDepth);
        bundle.put(B_SNAP_IRON, snapIron);
        bundle.put(B_SNAP_GOLD, snapGold);
        bundle.put(B_SNAP_CRYSTAL, snapCrystal);
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);

        snapshotDepth = bundle.getInt(B_SNAP_DEPTH);
        snapIron      = bundle.getInt(B_SNAP_IRON);
        snapGold      = bundle.getInt(B_SNAP_GOLD);
        snapCrystal   = bundle.getInt(B_SNAP_CRYSTAL);
    }
}
