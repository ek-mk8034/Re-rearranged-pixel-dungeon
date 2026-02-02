package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.watabou.noosa.ColorBlock;
import com.watabou.noosa.ui.Component;

public class HealthBar extends Component {

    private static final int COLOR_BG   = 0xFFCC0000;
    private static final int COLOR_HP   = 0xFF00EE00;
    private static final int COLOR_SHLD = 0xFFFFFFFF;

    private static final int HEIGHT = 2;

    private ColorBlock Bg;
    private ColorBlock Shld;
    private ColorBlock Hp;

    private float health;
    private float shield;

    @Override
    protected void createChildren() {
        Bg = new ColorBlock( 1, 1, COLOR_BG );
        add( Bg );

        Shld = new ColorBlock( 1, 1, COLOR_SHLD );
        add( Shld );

        Hp = new ColorBlock( 1, 1, COLOR_HP );
        add( Hp );

        height = HEIGHT;
    }

    @Override
    protected void layout() {

        // 챌린지 상태면 아예 안 보이게 + 레이아웃 계산도 스킵
        if (Dungeon.isChallenged(Challenges.NO_HP_UI)) {
            visible = false;
            return;
        } else {
            visible = true;
        }

        Bg.x = Shld.x = Hp.x = x;
        Bg.y = Shld.y = Hp.y = y;

        Bg.size( width, height );

        float pixelWidth = width;
        if (camera() != null) pixelWidth *= camera().zoom;
        Shld.size( width * (float)Math.ceil(shield * pixelWidth)/pixelWidth, height );
        Hp.size( width * (float)Math.ceil(health * pixelWidth)/pixelWidth, height );
    }

    public void level( float value ) {
        level( value, 0f );
    }

    public void level( float health, float shield ){
        this.health = health;
        this.shield = shield;
        layout();
    }

    public void level(Char c){

        // 여기서도 안전장치 (level 호출될 때 즉시 숨김 처리)
        if (Dungeon.isChallenged(Challenges.NO_HP_UI)) {
            visible = false;
            return;
        } else {
            visible = true;
        }

        float health = c.HP;
        float shield = c.shielding();
        float max = Math.max(health+shield, c.HT);

        level(health/max, (health+shield)/max);
    }
}
