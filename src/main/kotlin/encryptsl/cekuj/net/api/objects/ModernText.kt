package encryptsl.cekuj.net.api.objects

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags

object ModernText {

    fun miniModernText(message: String): Component {
        return miniModernText().deserialize(message)
    }

    fun miniModernText(message: String, resolver: TagResolver): Component {
        return miniModernText().deserialize(message, resolver)
    }

    private fun miniModernText(): MiniMessage {
        return MiniMessage.builder()
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
                .build()
            )
            .build()
    }

}