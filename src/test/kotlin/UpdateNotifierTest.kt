import encryptsl.cekuj.net.api.UpdateNotifier
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UpdateNotifierTest {

    @Test
    fun testVersion() {
        val update = UpdateNotifier("101934", "1.2.1-SNAPSHOT")
        val response = update.checkPluginVersion()
        assertEquals("You are using current version !", response)
    }

}