package com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Bible;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.UnholyBible;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.alchemy.HolySword;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

/**
 * Cleric Temple Blessing (Temple ritual reward)
 *
 * Randomly grants 1 of 5 passive blessings. Effects are applied via hook methods
 * that you will call from Hero damage/defense/strength code, and from Healing Potion execute.
 *
 * NOTE:
 * - Potion ban is "healing potions only" (other potions allowed).
 * - Some weapon-class checks are implemented via class-name matching to keep this file decoupled.
 *   Replace those with instanceof checks if you have concrete classes (recommended).
 */
public class ClericTempleBlessing extends Buff {

    public enum Type {
        CORRUPT_PRIEST,   // 타락한 성직자
        MELEE_PILGRIM,    // 근접 순례자
        INQUISITOR,       // 신성 고문관
        PALADIN_VOW,      // 거룩한 성기사
        SAINTESS_PRAYER   // 순결한 성녀
    }

    private static final String B_TYPE = "b_type";

    public Type type;

    // ======= TUNABLE CONSTANTS (easy balancing) =======

    // Corrupt Priest
    public static final float CORRUPT_CURSED_WEAPON_DMG_MULT = 1.20f; // +10%
    public static final float CORRUPT_CURSED_ARMOR_DR_MULT   = 1.20f; // +10%

    // Melee Pilgrim
    public static final float PILGRIM_RANGED_DMG_MULT = 0.50f; // -50%
    public static final float PILGRIM_MELEE_DMG_MULT  = 1.20f; // +20%

    // Inquisitor
    public static final float INQUISITOR_LIFESTEAL_FRACTION = 0.50f; // 50%
    public static final int   INQUISITOR_DR_PENALTY         = 2; // -3 DR
    public static final int   INQUISITOR_INCOMING_DAMAGE    = 2; // -3 DR
    public static final boolean INQUISITOR_LIFESTEAL_MELEE_ONLY = true;

    // Paladin Vow
    public static final float PALADIN_HEROSWORD_DMG_MULT = 1.50f; // +50%
    public static final float PALADIN_OTHER_DMG_MULT     = 0.80f; // -20%
    public static final float PALADIN_UNARMED_DMG_MULT   = 5.00f; // +500%
    public static final int   PALADIN_DR_BONUS           = 2;


    // Saintess Prayer
    public static final int   SAINTESS_STR_PENALTY       = 4;    // -4 STR
    public static final float SAINTESS_BOOK_DMG_MULT     = 1.5f; // +50%

    // =================================================

    /**
     * Grants a random Cleric blessing.
     * If a previous ClericTempleBlessing exists, it will be replaced.
     */
    public static ClericTempleBlessing grantRandom(Hero hero) {
        ClericTempleBlessing existing = hero.buff(ClericTempleBlessing.class);
        if (existing != null) existing.detach();

        ClericTempleBlessing b = Buff.affect(hero, ClericTempleBlessing.class);
        Type[] all = Type.values();
        b.type = all[Random.Int(all.length)];
        return b;
    }

    /** Convenience check */
    public boolean is(Type t) {
        return type == t;
    }

    // =================== Hook Methods ===================
    // Call these from Hero / Combat calculation sites.

    /**
     * Modify outgoing damage.
     *
     * @param hero   attacker
     * @param dmg    base damage
     * @param weapon used weapon (may be null)
     * @param ranged whether this attack is ranged
     * @return modified damage (>=0)
     */
    public int modifyOutgoingDamage(Hero hero, int dmg, Item weapon, boolean ranged) {
        if (type == null) return dmg;
        if (dmg <= 0) return dmg;

        switch (type) {

            case CORRUPT_PRIEST:
                // cursed weapon => +10% damage (weapons only)
                if (weapon instanceof KindOfWeapon && weapon.cursed) {
                    dmg = Math.round(dmg * CORRUPT_CURSED_WEAPON_DMG_MULT);
                }
                break;

            case MELEE_PILGRIM:
                dmg = Math.round(dmg * (ranged ? PILGRIM_RANGED_DMG_MULT : PILGRIM_MELEE_DMG_MULT));
                break;

            case INQUISITOR:
                if (!INQUISITOR_LIFESTEAL_MELEE_ONLY || !ranged) {
                    // Apply lifesteal (healing potion ban should not affect this)
                    int heal = Math.round(dmg * INQUISITOR_LIFESTEAL_FRACTION);
                    if (heal > 0) hero.heal(heal);
                }
                break;

            case PALADIN_VOW:
                if (weapon == null) {
                    // 맨손 전투: 신의 가호
                    dmg = Math.round(dmg * PALADIN_UNARMED_DMG_MULT);
                } else if (isHeroSword(weapon)) {
                    // 성검
                    dmg = Math.round(dmg * PALADIN_HEROSWORD_DMG_MULT);
                } else {
                    // 그 외 무기
                    dmg = Math.round(dmg * PALADIN_OTHER_DMG_MULT);
                }
                break;


            case SAINTESS_PRAYER:
                if (isHolyBook(weapon) || isCorruptedHolyBook(weapon)) {
                    dmg = Math.round(dmg * SAINTESS_BOOK_DMG_MULT);
                }
                break;
        }

        return Math.max(dmg, 0);
    }

    /**
     * Modify defense / DR (incoming mitigation).
     *
     * @param dr    base dr
     * @param armor equipped armor (may be null)
     */
    public int modifyDefenseDR(int dr, Item armor) {
        if (type == null) return dr;

        switch (type) {
            case INQUISITOR:
                dr -= INQUISITOR_DR_PENALTY;
                break;

            case CORRUPT_PRIEST:
                if (armor instanceof Armor && armor.cursed) {
                    dr = Math.round(dr * CORRUPT_CURSED_ARMOR_DR_MULT);
                }
                break;

            case PALADIN_VOW:
                dr += PALADIN_DR_BONUS;
                break;

            default:
                break;
        }

        return Math.max(dr, 0);
    }

    public int modifyIncomingDamage(Hero hero, int damage) {
        if (type == null || damage <= 0) return damage;

        switch (type) {
            case INQUISITOR:
                damage += INQUISITOR_INCOMING_DAMAGE;
                break;
            default:
                break;
        }

        return Math.max(damage, 0);
    }


    /** Modify strength (for equip requirements, etc.) */
    public int modifyStrength(int str) {
        if (type == Type.SAINTESS_PRAYER) {
            str -= SAINTESS_STR_PENALTY;
        }
        return str;
    }

    /**
     * Returns true iff healing potion use should be forbidden.
     * You will typically call this from PotionOfHealing.execute(...) to block drinking.
     */
    public boolean forbidsHealingPotion() {
        return type == Type.INQUISITOR;
    }

    // =================== Curse Handling Helpers ===================
    // Call these from Equip/Unequip restrictions (weapons/armor only).

    /** For CORRUPT_PRIEST: weapons/armor cursed-unequip restriction should be bypassed */
    public boolean bypassCursedEquipRestrictionsFor(Item item) {
        if (type != Type.CORRUPT_PRIEST) return false;
        return (item instanceof KindOfWeapon) || (item instanceof Armor);
    }

    // =================== Identification Helpers ===================

    private static boolean isHeroSword(Item weapon) {
        return weapon instanceof HolySword;
    }


    private static boolean isHolyBook(Item weapon) {
        return weapon instanceof Bible;
    }
    
    private static boolean isCorruptedHolyBook(Item weapon) {
        return weapon instanceof UnholyBible;
    }


    // =================== Save / Load ===================

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(B_TYPE, type == null ? Type.CORRUPT_PRIEST.name() : type.name());
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        String s = bundle.getString(B_TYPE);
        try {
            type = Type.valueOf(s);
        } catch (Exception e) {
            type = Type.CORRUPT_PRIEST;
        }
    }

    // =================== Optional UI / Debug ===================

    @Override
    public String toString() {
        // If you want localized names later, map this to Messages.
        return "ClericTempleBlessing(" + (type == null ? "null" : type.name()) + ")";
    }
    // ===========================================================

    @Override
    public String name() {
        return uiName();
    }

    @Override
    public String desc() {
        return uiDesc();
    }

    public String uiName() {
        if (type == null) return Messages.get(this, "name_unknown");
        switch (type) {
            case CORRUPT_PRIEST:    return Messages.get(this, "name_corrupt_priest");
            case MELEE_PILGRIM:     return Messages.get(this, "name_melee_pilgrim");
            case INQUISITOR:        return Messages.get(this, "name_inquisitor");
            case PALADIN_VOW:       return Messages.get(this, "name_paladin_vow");
            case SAINTESS_PRAYER:   return Messages.get(this, "name_saintess_prayer");
            default:                return Messages.get(this, "name_unknown");
        }
    }

    public String uiDesc() {
        if (type == null) return Messages.get(this, "desc_unknown");
        switch (type) {
            case CORRUPT_PRIEST:
                return Messages.get(this, "desc_corrupt_priest");
            case MELEE_PILGRIM:
                return Messages.get(this, "desc_melee_pilgrim");
            case INQUISITOR:
                return Messages.get(this, "desc_inquisitor");
            case PALADIN_VOW:
                return Messages.get(this, "desc_paladin_vow");
            case SAINTESS_PRAYER:
                return Messages.get(this, "desc_saintess_prayer");
            default:
                return Messages.get(this, "desc_unknown");
        }
    }
    
    @Override
    public int icon() {
        return BuffIndicator.BLESS; // 일단 아무거나 설정
    }
}




