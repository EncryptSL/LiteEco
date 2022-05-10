import encryptsl.cekuj.net.api.UpdateNotifier
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class TestNotifierUpdate {

    @Test
    fun assertTest() {
        val updateNotifier =  UpdateNotifier("101934", "1.0.0-SNAPSHOT")
        assertSame("<green>You are using current version !", updateNotifier.checkPluginVersion())
    }

}