package encryptsl.cekuj.net.api.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

object ModernText {

    private val miniMessage: MiniMessage by lazy { initMiniMessage() }

    @JvmStatic
    fun miniModernText(message: String): Component {
        return miniMessage.deserialize(convertVariables(message))
    }

    @JvmStatic
    fun miniModernText(message: String, resolver: TagResolver): Component {
        return miniMessage.deserialize(convertVariables(message), resolver)
    }

    private fun initMiniMessage(): MiniMessage {
        return MiniMessage.builder()
            .strict(false)
            .tags(
                TagResolver.builder()
                .resolver(StandardTags.color())
                .resolver(StandardTags.clickEvent())
                .resolver(StandardTags.decorations())
                .resolver(StandardTags.font())
                .resolver(StandardTags.hoverEvent())
                .resolver(StandardTags.insertion())
                .resolver(StandardTags.rainbow())
                .resolver(StandardTags.newline())
                .resolver(StandardTags.transition())
                .resolver(StandardTags.gradient())
                .build()
            )
            .build()
    }

    private fun convertVariables(value: String): String {
        val regex = """[{](\w+)[}]""".toRegex()
        return regex.replace(value) { matchResult ->
            "<${matchResult.groupValues[1]}>"
        }
    }
}