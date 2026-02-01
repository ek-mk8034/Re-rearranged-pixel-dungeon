package com.shatteredpixel.shatteredpixeldungeon.items.changer;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Belongings;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.Transmuting;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.KnightsShield;
import com.shatteredpixel.shatteredpixeldungeon.items.Rosary;
import com.shatteredpixel.shatteredpixeldungeon.items.Saddle;
import com.shatteredpixel.shatteredpixeldungeon.items.Sheath;
import com.shatteredpixel.shatteredpixeldungeon.items.artifacts.Artifact;
import com.shatteredpixel.shatteredpixeldungeon.items.bags.Bag;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.Ring;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.CorrosiveBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.ElectricBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.GoldenBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.MagicalBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.NaturesBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.PhaseBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.SpiritBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.TacticalBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.bow.WindBow;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.DeathSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.EnhancedMachete;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.HeroSword;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Machete;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MeleeWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Shovel;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.Spade;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.gun.Gun;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.Icons;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndOptions;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndTitledMessage;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings.ClericTempleBlessing;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.blessings.SamuraiTempleBlessing;

public class OldAmulet extends Item {

    private static boolean isCleric(Hero hero) {
        return hero != null && hero.heroClass == HeroClass.CLERIC;
    }

    public static final String AC_USE = "USE";

    ArrayList<Integer> abilityList = new ArrayList<>();

    {
        image = ItemSpriteSheet.OLD_AMULET;
        defaultAction = AC_USE;
        stackable = false;

        unique = true;
        bones = false;

        while (abilityList.size() < 3) {
            int index = Random.Int(16);
            if (!abilityList.contains(index)) {
                abilityList.add(index);
            }
        }
    }

    @Override
    public ArrayList<String> actions(Hero hero) {
        ArrayList<String> actions = super.actions(hero);

        if (hero.buff(TempleCurse.class) != null) return actions;

        actions.add(AC_USE);
        return actions;
    }

    @Override
    public boolean doPickUp(Hero hero, int pos) {
        if (super.doPickUp(hero, pos)) {
            if (Dungeon.depth == 14 && Dungeon.branch == 2 && hero.buff(TempleCurse.class) == null) {
                Dungeon.templeCompleted = true;
                Dungeon.level.playLevelMusic();
                Buff.affect(hero, TempleCurse.class);
            }
            return true;
        }
        return false;
    }

    @Override
    public String desc() {
        String desc = super.desc();
        if (Dungeon.hero != null && Dungeon.hero.buff(TempleCurse.class) != null) {
            desc += "\n\n" + Messages.get(this, "cannot_use");
        }
        return desc;
    }

    @Override
    public void execute(Hero hero, String action) {
        super.execute(hero, action);

        if (!action.equals(AC_USE)) return;

        if (hero.buff(TempleCurse.class) != null) {
            GLog.w(Messages.get(this, "cannot_use"));
            return;
        }

        // ===== Cleric 전용: 축복 지급 + OldAmulet 소모 =====
        if (isCleric(hero)) {
            ClericTempleBlessing b = ClericTempleBlessing.grantRandom(hero);
            GLog.p(Messages.get(ClericTempleBlessing.class, "gain", b.uiName()));
            hero.sprite.showStatus(CharSprite.POSITIVE, b.uiName());

            detach(hero.belongings.backpack);
            hero.spendAndNext(Actor.TICK);
            return;
        }

        // ===== Samurai 전용: Sheath만 선택 -> 요검/명검 선택 -> 축복 저장 -> OldAmulet 소모 =====
        if (hero.heroClass == HeroClass.SAMURAI) {

            // 이미 선택했으면 다시 못 쓰게
            if (hero.buff(SamuraiTempleBlessing.class) != null) {
                GLog.w(Messages.get(this, "already_blessed")); // strings에 추가 권장
                return;
            }

            // "Sheath"만 선택 가능하도록 강제
            GameScene.selectItem(sheathSelector);
            return;
        }

        // ===== 그 외 직업: 기존 로직(변환) =====
        GameScene.selectItem(itemSelector);
    }

    private String inventoryTitle() {
        return Messages.get(this, "inv_title");
    }

    // ----------------------------
    // Sheath 전용 선택기
    // ----------------------------
    private final WndBag.ItemSelector sheathSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return inventoryTitle();
        }        

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            // 오직 Sheath만
            return item instanceof Sheath;
        }

        @Override
        public void onSelect(Item item) {

            // 취소
            if (item == null) return;

            // safety: OldAmulet가 실행 중일 때만
            if (!(curItem instanceof OldAmulet)) return;

            // Sheath 선택했으면 이제 요검/명검 선택 창
            final Hero h = Dungeon.hero;

            GameScene.show(new WndOptions(
                    new ItemSprite(OldAmulet.this),
                    Messages.titleCase(OldAmulet.this.name()),
                    Messages.get(OldAmulet.this, "samurai_select"),
                    Messages.get(OldAmulet.this, "yok"),
                    Messages.get(OldAmulet.this, "myeong"),
                    Messages.get(OldAmulet.this, "cancel")
            ) {
                @Override
                protected void onSelect(int index) {

                    if (index == 0) {
                        // 요검
                        SamuraiTempleBlessing.ensure(h, SamuraiTempleBlessing.Path.YOK);

                        GLog.p(Messages.get(SamuraiTempleBlessing.class, "gain_yok"));
                        h.sprite.showStatus(
                                0xCC0000, // 진한 빨강
                                Messages.get(SamuraiTempleBlessing.class, "short_yok")
                        );

                        OldAmulet.this.detach(h.belongings.backpack);
                        h.spendAndNext(Actor.TICK);

                    } else if (index == 1) {
                        // 명검
                        SamuraiTempleBlessing.ensure(h, SamuraiTempleBlessing.Path.MYEONG);

                        GLog.p(Messages.get(SamuraiTempleBlessing.class, "gain_myeong"));
                        h.sprite.showStatus(
                                0xFFFFFF, // 흰색
                                Messages.get(SamuraiTempleBlessing.class, "short_myeong")
                        );

                        OldAmulet.this.detach(h.belongings.backpack);
                        h.spendAndNext(Actor.TICK);

                    } else {
                        // 취소: 아무 것도 소모하지 않음
                        hide();
                    }
                }
            });

        }
    };

    // ----------------------------
    // 기존 변환 로직
    // ----------------------------
    private static final String ABILITY_LIST_0 = "abilityList_0";
    private static final String ABILITY_LIST_1 = "abilityList_1";
    private static final String ABILITY_LIST_2 = "abilityList_2";

    @Override
    public void storeInBundle(Bundle bundle) {
        super.storeInBundle(bundle);
        bundle.put(ABILITY_LIST_0, abilityList.get(0));
        bundle.put(ABILITY_LIST_1, abilityList.get(1));
        bundle.put(ABILITY_LIST_2, abilityList.get(2));
    }

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        abilityList.clear();
        abilityList.add(bundle.getInt(ABILITY_LIST_0));
        abilityList.add(bundle.getInt(ABILITY_LIST_1));
        abilityList.add(bundle.getInt(ABILITY_LIST_2));
    }

    public static Item changeItem(Item item) {
        if (item instanceof SpiritBow) {
            return changeBow((SpiritBow) item);
        } else if (item instanceof Gun) {
            return changeGun((Gun) item);
        } else if (item instanceof Shovel) {
            return changeShovel((Shovel) item);
        } else if (item instanceof Machete) {
            return changeMachete((Machete) item);
        } else if (item instanceof KnightsShield) {
            return changeShield();
        } else {
            return null;
        }
    }

    private static float[] bowDeck = {1, 1, 1, 1, 1, 1, 1, 1};

    public static SpiritBow changeBow(SpiritBow bow) {
        SpiritBow newBow;
        switch (Random.chances(bowDeck)) {
            case 0:
            default:
                newBow = new NaturesBow();
                bowDeck[0] = 0;
                break;
            case 1:
                newBow = new GoldenBow();
                bowDeck[1] = 0;
                break;
            case 2:
                newBow = new CorrosiveBow();
                bowDeck[2] = 0;
                break;
            case 3:
                newBow = new WindBow();
                bowDeck[3] = 0;
                break;
            case 4:
                newBow = new TacticalBow();
                bowDeck[4] = 0;
                break;
            case 5:
                newBow = new PhaseBow();
                bowDeck[5] = 0;
                break;
            case 6:
                newBow = new ElectricBow();
                bowDeck[6] = 0;
                break;
            case 7:
                newBow = new MagicalBow();
                bowDeck[7] = 0;
                break;
            case -1:
                bowDeck = new float[]{1, 1, 1, 1, 1, 1, 1, 1};
                newBow = changeBow(bow);
                return newBow;
        }

        if (newBow.getClass() == bow.getClass()) {
            newBow = changeBow(bow);
            return newBow;
        }

        newBow.level(0);
        newBow.quantity(1);
        int level = bow.trueLevel();
        if (level > 0) {
            newBow.upgrade(level);
        } else if (level < 0) {
            newBow.degrade(-level);
        }

        newBow.enchantment = bow.enchantment;
        newBow.curseInfusionBonus = bow.curseInfusionBonus;
        newBow.masteryPotionBonus = bow.masteryPotionBonus;
        newBow.levelKnown = bow.levelKnown;
        newBow.cursedKnown = bow.cursedKnown;
        newBow.cursed = bow.cursed;
        newBow.augment = bow.augment;
        newBow.enchantHardened = bow.enchantHardened;

        return newBow;
    }

    private static Gun changeGun(Gun gun) {
        gun.inscribeMod = Gun.InscribeMod.INSCRIBED;
        return gun;
    }

    private static Spade changeShovel(Shovel shovel) {
        Spade newShovel = new Spade();

        newShovel.level(0);
        newShovel.quantity(1);
        int level = shovel.trueLevel();
        if (level > 0) {
            newShovel.upgrade(level);
        } else if (level < 0) {
            newShovel.degrade(-level);
        }

        newShovel.enchantment = shovel.enchantment;
        newShovel.curseInfusionBonus = shovel.curseInfusionBonus;
        newShovel.masteryPotionBonus = shovel.masteryPotionBonus;
        newShovel.levelKnown = shovel.levelKnown;
        newShovel.cursedKnown = shovel.cursedKnown;
        newShovel.cursed = shovel.cursed;
        newShovel.augment = shovel.augment;
        newShovel.enchantHardened = shovel.enchantHardened;

        return newShovel;
    }

    private static Machete changeMachete(Machete machete) {
        EnhancedMachete newMachete = new EnhancedMachete();

        newMachete.level(0);
        newMachete.quantity(1);
        int level = machete.trueLevel();
        if (level > 0) {
            newMachete.upgrade(level);
        } else if (level < 0) {
            newMachete.degrade(-level);
        }

        newMachete.enchantment = machete.enchantment;
        newMachete.curseInfusionBonus = machete.curseInfusionBonus;
        newMachete.masteryPotionBonus = machete.masteryPotionBonus;
        newMachete.levelKnown = machete.levelKnown;
        newMachete.cursedKnown = machete.cursedKnown;
        newMachete.cursed = machete.cursed;
        newMachete.augment = machete.augment;
        newMachete.enchantHardened = machete.enchantHardened;

        return newMachete;
    }

    private static Item changeShield() {
        Item newItem;
        switch (Dungeon.hero.subClass) {
            case DEATHKNIGHT:
                newItem = new DeathSword();
                break;
            case HORSEMAN:
                newItem = new Saddle();
                break;
            case CRUSADER:
                newItem = new Rosary();
                break;
            default:
                newItem = null;
                break;
        }
        return newItem;
    }

    protected void onItemSelected(Item item) {
        Item result = changeItem(item);

        if (result == null) {
            GLog.n(Messages.get(this, "nothing"));
            curItem.collect(curUser.belongings.backpack);
        } else {
            if (result != item) {
                int slot = Dungeon.quickslot.getSlot(item);
                if (item.isEquipped(Dungeon.hero)) {
                    item.cursed = false;
                    if (item instanceof Artifact && result instanceof Ring) {
                        ((EquipableItem) item).doUnequip(Dungeon.hero, false);
                        if (!result.collect()) {
                            Dungeon.level.drop(result, curUser.pos).sprite.drop();
                        }
                    } else if (item instanceof KindOfWeapon && Dungeon.hero.belongings.secondWep() == item) {
                        ((EquipableItem) item).doUnequip(Dungeon.hero, false);
                        ((KindOfWeapon) result).equipSecondary(Dungeon.hero);
                    } else {
                        ((EquipableItem) item).doUnequip(Dungeon.hero, false);
                        ((EquipableItem) result).doEquip(Dungeon.hero);
                    }
                    Dungeon.hero.spend(-Dungeon.hero.cooldown());
                } else {
                    item.detach(Dungeon.hero.belongings.backpack);
                    if (!result.collect()) {
                        Dungeon.level.drop(result, curUser.pos).sprite.drop();
                    } else if (Dungeon.hero.belongings.getSimilar(result) != null) {
                        result = Dungeon.hero.belongings.getSimilar(result);
                    }
                }
                if (slot != -1
                        && result.defaultAction() != null
                        && !Dungeon.quickslot.isNonePlaceholder(slot)
                        && Dungeon.hero.belongings.contains(result)) {
                    Dungeon.quickslot.setSlot(slot, result);
                }
            }
            if (result.isIdentified()) {
                Catalog.setSeen(result.getClass());
            }
            Sample.INSTANCE.play(Assets.Sounds.EVOKE);
            CellEmitter.center(curUser.pos).burst(Speck.factory(Speck.STAR), 7);
            new Flare(6, 32).color(0xFFFF00, true).show(curUser.sprite, 2f);
            Dungeon.hero.spendAndNext(Actor.TICK);
            Dungeon.hero.sprite.operate(Dungeon.hero.pos);
            Transmuting.show(curUser, item, result);
            curUser.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
            GLog.p(Messages.get(this, "morph"));
            detach(Dungeon.hero.belongings.backpack);
        }
    }

    @Override
    public boolean isUpgradable() {
        return false;
    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public int value() {
        return 2000;
    }

    protected WndBag.ItemSelector itemSelector = new WndBag.ItemSelector() {

        @Override
        public String textPrompt() {
            return inventoryTitle();
        }

        @Override
        public Class<? extends Bag> preferredBag() {
            return Belongings.Backpack.class;
        }

        @Override
        public boolean itemSelectable(Item item) {
            if (!item.isIdentified()) return false;
            switch (Dungeon.hero.heroClass) {
                case WARRIOR:
                default:
                    return item instanceof BrokenSeal;
                case MAGE:
                    return item instanceof MagesStaff;
                case ROGUE:
                    return item instanceof Ring;
                case HUNTRESS:
                    return item instanceof SpiritBow;
                case DUELIST:
                case SAMURAI:
                    return item instanceof MeleeWeapon && !(item instanceof MagesStaff) && !(item instanceof Gun);
                case GUNNER:
                    return item instanceof Gun;
                case ADVENTURER:
                    return item instanceof Machete || item instanceof Shovel;
                case KNIGHT:
                    return item instanceof KnightsShield;
            }
        }

        @Override
        public void onSelect(Item item) {
            if (!(curItem instanceof OldAmulet)) return;

            if (item != null && itemSelectable(item)) {
                switch (Dungeon.hero.heroClass) {
                    default:
                        onItemSelected(item);
                        break;
                    case DUELIST:
                        GameScene.show(new WndAbilitySelect((MeleeWeapon) item, abilityList.get(0), abilityList.get(1), abilityList.get(2)));
                        break;
                }
            }
        }
    };

    public static class WndAbilitySelect extends WndOptions {

        private MeleeWeapon wep;
        private ArrayList<Integer> ability = new ArrayList<>();

        public WndAbilitySelect(MeleeWeapon wep, int ability_1, int ability_2, int ability_3) {
            super(new ItemSprite(new HeroSword()),
                    Messages.titleCase(new HeroSword().name()),
                    Messages.get(HeroSword.class, "ability_select"),
                    new HeroSword(ability_1, wep).abilityName(),
                    new HeroSword(ability_2, wep).abilityName(),
                    new HeroSword(ability_3, wep).abilityName(),
                    Messages.get(HeroSword.class, "cancel"));
            ability.add(ability_1);
            ability.add(ability_2);
            ability.add(ability_3);
            this.wep = wep;
        }

        @Override
        protected void onSelect(int index) {
            if (index < 3) {
                HeroSword heroSword = new HeroSword(ability.get(index), wep);

                heroSword.level(0);
                heroSword.quantity(1);
                int level = wep.trueLevel();
                if (level > 0) {
                    heroSword.upgrade(level);
                } else if (level < 0) {
                    heroSword.degrade(-level);
                }

                heroSword.enchantment = wep.enchantment;
                heroSword.curseInfusionBonus = wep.curseInfusionBonus;
                heroSword.masteryPotionBonus = wep.masteryPotionBonus;
                heroSword.levelKnown = wep.levelKnown;
                heroSword.cursedKnown = wep.cursedKnown;
                heroSword.cursed = wep.cursed;
                heroSword.augment = wep.augment;
                heroSword.enchantHardened = wep.enchantHardened;

                int slot = Dungeon.quickslot.getSlot(wep);
                if (wep.isEquipped(Dungeon.hero)) {
                    wep.cursed = false;
                    if (Dungeon.hero.belongings.secondWep() == wep) {
                        wep.doUnequip(Dungeon.hero, false);
                        heroSword.equipSecondary(Dungeon.hero);
                    } else {
                        wep.doUnequip(Dungeon.hero, false);
                        heroSword.doEquip(Dungeon.hero);
                    }
                    Dungeon.hero.spend(-Dungeon.hero.cooldown());
                } else {
                    wep.detach(Dungeon.hero.belongings.backpack);
                    if (!heroSword.collect()) {
                        Dungeon.level.drop(heroSword, curUser.pos).sprite.drop();
                    } else if (Dungeon.hero.belongings.getSimilar(heroSword) != null) {
                        heroSword = (HeroSword) Dungeon.hero.belongings.getSimilar(heroSword);
                    }
                }
                if (slot != -1
                        && heroSword.defaultAction() != null
                        && !Dungeon.quickslot.isNonePlaceholder(slot)
                        && Dungeon.hero.belongings.contains(heroSword)) {
                    Dungeon.quickslot.setSlot(slot, heroSword);
                }

                Sample.INSTANCE.play(Assets.Sounds.EVOKE);
                CellEmitter.center(curUser.pos).burst(Speck.factory(Speck.STAR), 7);
                new Flare(6, 32).color(0xFFFF00, true).show(curUser.sprite, 2f);
                Dungeon.hero.spendAndNext(Actor.TICK);
                Dungeon.hero.sprite.operate(Dungeon.hero.pos);
                Transmuting.show(curUser, wep, heroSword);
                curUser.sprite.emitter().start(Speck.factory(Speck.CHANGE), 0.2f, 10);
                GLog.p(Messages.get(OldAmulet.class, "morph"));
                Dungeon.hero.belongings.getItem(OldAmulet.class).detach(Dungeon.hero.belongings.backpack);

            } else {
                hide();
            }
        }

        @Override
        protected boolean hasInfo(int index) {
            return index < 3;
        }

        @Override
        protected void onInfo(int index) {
            HeroSword heroSword = new HeroSword(ability.get(index), wep);
            if (wep.isIdentified()) {
                heroSword.level(wep.buffedLvl());
                heroSword.identify();
            }
            GameScene.show(new WndTitledMessage(
                    Icons.get(Icons.INFO),
                    Messages.titleCase(heroSword.abilityName()),
                    heroSword.abilityInfo()));
        }
    }

    public static class TempleCurse extends Buff {
        public void saySwitch() {
            GLog.i(Messages.get(this, "escape"));
        }
    }
}
