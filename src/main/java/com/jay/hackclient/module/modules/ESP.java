package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class ESP extends Module {

    public ESP() {
        super("ESP", "See players through walls", Category.RENDER);
    }

    // Rendering will be added with WorldRenderEvents later.
}
