package com.github.encryptsl.lite.eco.common.hook

abstract class HookListener(
    val pluginName: String, val description: String,
) {
    var registered: Boolean = false
        protected set

    abstract fun canRegister(): Boolean

    abstract fun register()

    abstract fun unregister()
}