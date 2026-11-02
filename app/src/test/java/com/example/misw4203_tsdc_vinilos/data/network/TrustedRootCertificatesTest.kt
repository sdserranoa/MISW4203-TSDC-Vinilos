package com.example.misw4203_tsdc_vinilos.data.network

import okhttp3.tls.decodeCertificatePem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest
import java.security.cert.X509Certificate

/**
 * Verifica las autoridades raíz agregadas para Android 6.0 (issue #108, defecto D-01):
 * que sean exactamente los certificados oficiales y que el cliente HTTP de la app confíe en ellas
 * sin dejar de confiar en las autoridades del sistema.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [23, 26, 34, 35, 36])
class TrustedRootCertificatesTest {

    @Test
    fun isrgRootX1_correspondeAlCertificadoOficial() = assertCertificadoOficial(
        TrustedRootCertificates.ISRG_ROOT_X1,
        "ISRG Root X1",
        "96:BC:EC:06:26:49:76:F3:74:60:77:9A:CF:28:C5:A7:CF:E8:A3:C0:AA:E1:1A:8F:FC:EE:05:C0:BD:DF:08:C6",
    )

    @Test
    fun isrgRootX2_correspondeAlCertificadoOficial() = assertCertificadoOficial(
        TrustedRootCertificates.ISRG_ROOT_X2,
        "ISRG Root X2",
        "69:72:9B:8E:15:A8:6E:FC:17:7A:57:AF:B7:17:1D:FC:64:AD:D2:8C:2F:CA:8C:F1:50:7E:34:45:3C:CB:14:70",
    )

    @Test
    fun gtsRootR4_correspondeAlCertificadoOficial() = assertCertificadoOficial(
        TrustedRootCertificates.GTS_ROOT_R4,
        "GTS Root R4",
        "34:9D:FA:40:58:C5:E2:63:12:3B:39:8A:E7:95:57:3C:4E:13:13:C8:3F:E6:8F:93:55:6C:D5:E8:03:1B:3C:7D",
    )

    @Test
    fun clienteHttp_confiaEnLasRaicesAgregadasYEnLasDelSistema() {
        val emisores = requireNotNull(VinilosApi.httpClient.x509TrustManager)
            .acceptedIssuers
            .map { it.subjectX500Principal.name }

        listOf("ISRG Root X1", "ISRG Root X2", "GTS Root R4").forEach { cn ->
            assertTrue("El cliente HTTP debe confiar en $cn", emisores.any { it.startsWith("CN=$cn,") })
        }
        assertTrue(
            "El cliente HTTP debe conservar las autoridades del sistema",
            emisores.size > TrustedRootCertificates.pems.size,
        )
    }

    private fun assertCertificadoOficial(pem: String, nombreComun: String, sha256Esperado: String) {
        val certificado = pem.trimIndent().decodeCertificatePem()

        assertTrue(certificado.subjectX500Principal.name.startsWith("CN=$nombreComun,"))
        assertEquals(sha256Esperado, huellaSha256(certificado))
        certificado.checkValidity()
    }

    private fun huellaSha256(certificado: X509Certificate): String =
        MessageDigest.getInstance("SHA-256")
            .digest(certificado.encoded)
            .joinToString(":") { "%02X".format(it) }
}
