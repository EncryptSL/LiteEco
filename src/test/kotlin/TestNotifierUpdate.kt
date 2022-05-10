import encryptsl.cekuj.net.api.UpdateNotifier
import org.junit.jupiter.api.Test
import kotlin.test.assertSame

class TestNotifierUpdate {

    @Test
    fun assertTest() {
        val updateNotifier =  UpdateNotifier("85531", "1.0.1.9")
        assertSame("<green>You are using current version !", updateNotifier.checkPluginVersion())
    }

}