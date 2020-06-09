//The code below is adapted from from https://github.com/Mastercard/client-encryption-java/blob/master/src/main/java/com/mastercard/developer/utils/EncryptionUtils.java
//
//Copyright (c) 2019 Mastercard
//
//Permission is hereby granted, free of charge, to any person obtaining a copy
//of this software and associated documentation files (the "Software"), to deal
//in the Software without restriction, including without limitation the rights
//to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//copies of the Software, and to permit persons to whom the Software is
//furnished to do so, subject to the following conditions:
//
//The above copyright notice and this permission notice shall be included in all
//copies or substantial portions of the Software.
//
//THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
//SOFTWARE.
package mdnet.base

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec

private const val PKCS_1_PEM_HEADER = "-----BEGIN RSA PRIVATE KEY-----"
private const val PKCS_1_PEM_FOOTER = "-----END RSA PRIVATE KEY-----"
private const val PKCS_8_PEM_HEADER = "-----BEGIN PRIVATE KEY-----"
private const val PKCS_8_PEM_FOOTER = "-----END PRIVATE KEY-----"

fun loadKey(keyDataString: String): PrivateKey? {
    if (keyDataString.contains(PKCS_1_PEM_HEADER)) {
        // OpenSSL / PKCS#1 Base64 PEM encoded file
        val fixedString = keyDataString.replace(PKCS_1_PEM_HEADER, "").replace(PKCS_1_PEM_FOOTER, "")
        return readPkcs1PrivateKey(base64Decode(fixedString))
    }
    if (keyDataString.contains(PKCS_8_PEM_HEADER)) {
        // PKCS#8 Base64 PEM encoded file
        val fixedString = keyDataString.replace(PKCS_8_PEM_HEADER, "").replace(PKCS_8_PEM_FOOTER, "")
        return readPkcs1PrivateKey(base64Decode(fixedString))
    }

    return null
}

private fun readPkcs8PrivateKey(pkcs8Bytes: ByteArray): PrivateKey? {
    val keyFactory = KeyFactory.getInstance("RSA", "SunRsaSign")
    val keySpec = PKCS8EncodedKeySpec(pkcs8Bytes)
    return try {
        keyFactory.generatePrivate(keySpec)
    } catch (e: InvalidKeySpecException) {
        throw IllegalArgumentException("Unexpected key format!", e)
    }
}

private fun readPkcs1PrivateKey(pkcs1Bytes: ByteArray): PrivateKey? {
    // We can't use Java internal APIs to parse ASN.1 structures, so we build a PKCS#8 key Java can understand
    val pkcs1Length = pkcs1Bytes.size
    val totalLength = pkcs1Length + 22
    val pkcs8Header = byteArrayOf(
            0x30, 0x82.toByte(), (totalLength shr 8 and 0xff).toByte(), (totalLength and 0xff).toByte(), // Sequence + total length
            0x2, 0x1, 0x0, // Integer (0)
            0x30, 0xD, 0x6, 0x9, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0xD, 0x1, 0x1, 0x1, 0x5, 0x0, // Sequence: 1.2.840.113549.1.1.1, NULL
            0x4, 0x82.toByte(), (pkcs1Length shr 8 and 0xff).toByte(), (pkcs1Length and 0xff).toByte() // Octet string + length
    )
    val pkcs8bytes = join(pkcs8Header, pkcs1Bytes)
    return readPkcs8PrivateKey(pkcs8bytes)
}

private fun join(byteArray1: ByteArray, byteArray2: ByteArray): ByteArray {
    val bytes = ByteArray(byteArray1.size + byteArray2.size)
    System.arraycopy(byteArray1, 0, bytes, 0, byteArray1.size)
    System.arraycopy(byteArray2, 0, bytes, byteArray1.size, byteArray2.size)
    return bytes
}

private val b64ints = intArrayOf(
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54,
        55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2,
        3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
        20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30,
        31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47,
        48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
        -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)

private fun base64Decode(value: String): ByteArray {
    val valueBytes = value.toByteArray()
    val outputStream = ByteArrayOutputStream()
    var i = 0
    while (i < valueBytes.size) {
        var b: Int
        b = if (b64ints[valueBytes[i].toInt()] != -1) {
            b64ints[valueBytes[i].toInt()] and 0xFF shl 18
        } else {
            i++
            continue
        }
        var num = 0
        if (i + 1 < valueBytes.size && b64ints[valueBytes[i + 1].toInt()] != -1) {
            b = b or (b64ints[valueBytes[i + 1].toInt()] and 0xFF shl 12)
            num++
        }
        if (i + 2 < valueBytes.size && b64ints[valueBytes[i + 2].toInt()] != -1) {
            b = b or (b64ints[valueBytes[i + 2].toInt()] and 0xFF shl 6)
            num++
        }
        if (i + 3 < valueBytes.size && b64ints[valueBytes[i + 3].toInt()] != -1) {
            b = b or (b64ints[valueBytes[i + 3].toInt()] and 0xFF)
            num++
        }
        while (num > 0) {
            val c = b and 0xFF0000 shr 16
            outputStream.write(c)
            b = b shl 8
            num--
        }
        i += 4
    }
    return outputStream.toByteArray()
}
