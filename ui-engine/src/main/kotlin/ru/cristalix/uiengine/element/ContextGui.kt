package ru.cristalix.uiengine.element

import dev.xdark.clientapi.gui.Screen
import org.lwjgl.input.Keyboard
import ru.cristalix.uiengine.ClickEvent
import ru.cristalix.uiengine.UIEngine
import ru.cristalix.uiengine.utility.MouseButton
import ru.cristalix.uiengine.utility.V3
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

inline fun safe(action: () -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    try {
        action()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

open class ContextGui(builder: Screen.Builder = Screen.Builder.builder()) : Context2D(V3()) {

    val keyTypedHandlers = ArrayList<(char: Char, code: Int) -> Unit>()

    init {
        onKeyTyped { _, code ->
            if (code == Keyboard.KEY_ESCAPE) close()
        }
    }

    val screen: Screen = builder
        .draw { _, _, _, _ -> safe(this::transformAndRender) }
        .keyTyped { _, key, code ->
            keyTypedHandlers.forEach {
                safe { it(key, code) }
            }
        }
        .mouseClick { _, _, _, button ->
            safe {
                getForemostHovered()?.run {
                    this.onClick?.invoke(ClickEvent(true, MouseButton.values()[button]))
                }
            }
        }
        .mouseRelease { _, _, _, button ->
            safe {
                getForemostHovered()?.run {
                    this.onClick?.invoke(ClickEvent(false, MouseButton.values()[button]))
                }
            }
        }
        .build()

    fun open() = UIEngine.clientApi.minecraft().displayScreen(screen)

    fun close() = UIEngine.clientApi.minecraft().displayScreen(null)

    fun onKeyTyped(action: (char: Char, code: Int) -> Unit) =
        keyTypedHandlers.add(action)

}
