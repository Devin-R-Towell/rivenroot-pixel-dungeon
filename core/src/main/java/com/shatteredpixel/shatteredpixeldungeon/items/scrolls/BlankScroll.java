package com.shatteredpixel.shatteredpixeldungeon.items.scrolls;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

import java.util.ArrayList;

public class BlankScroll extends Item{
    {
        image = ItemSpriteSheet.BLANK_SCROLL;
        stackable = true;

        bones = true;

    }

    @Override
    public boolean isIdentified() {
        return true;
    }

    @Override
    public String name() {

        return Messages.get(BlankScroll.class, "name");
    }

    @Override
    public String desc(){
        return Messages.get(BlankScroll.class, "desc");
    }

    @Override
    public int value() {
        return 5 * quantity;
    }
}
