package com.jay.client18.module.modules;

import com.jay.client18.module.Module;

/** Flag module — drawing is done in HudRenderer. */
public class ArrayListMod extends Module {

    public ArrayListMod() {
        super("ArrayList", "Show enabled modules", Category.RENDER);
        setEnabled(true);
    }
}
