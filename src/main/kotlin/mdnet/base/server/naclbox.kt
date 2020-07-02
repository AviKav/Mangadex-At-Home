/*
Mangadex@Home
Copyright (c) 2020, MangaDex Network
This file is part of MangaDex@Home.

MangaDex@Home is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

MangaDex@Home is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this MangaDex@Home.  If not, see <http://www.gnu.org/licenses/>.
 */
/* ktlint-disable no-wildcard-imports */
package mdnet.base.server

import com.goterl.lazycode.lazysodium.LazySodiumJava
import com.goterl.lazycode.lazysodium.exceptions.SodiumException
import com.goterl.lazycode.lazysodium.interfaces.Box

@Throws(SodiumException::class)
fun LazySodiumJava.cryptoBoxOpenEasyAfterNm(cipherBytes: ByteArray, nonce: ByteArray, sharedKey: ByteArray): String {
    if (!Box.Checker.checkNonce(nonce.size)) {
        throw SodiumException("Incorrect nonce length.")
    }

    if (!Box.Checker.checkBeforeNmBytes(sharedKey.size)) {
        throw SodiumException("Incorrect shared secret key length.")
    }

    val message = ByteArray(cipherBytes.size - Box.MACBYTES)
    val res: Boolean = cryptoBoxOpenEasyAfterNm(message, cipherBytes, cipherBytes.size.toLong(), nonce, sharedKey)
    if (!res) {
        throw SodiumException("Could not fully complete shared secret key decryption.")
    }
    return str(message)
}
