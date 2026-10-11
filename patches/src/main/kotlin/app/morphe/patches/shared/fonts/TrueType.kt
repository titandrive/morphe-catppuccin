package app.morphe.patches.shared.fonts

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

// Validate the SFNT directory before embedding a file that Android will load.
internal fun readTrueType(path: String): ByteArray {
    val file = File(path)
    require(file.extension.equals("ttf", true) && file.isFile && file.canRead()) {
        "Choose a readable .ttf font file in the custom font patch options."
    }
    require(file.length() in 12..20_971_520) { "The TTF file is empty or larger than 20 MiB." }
    val data = file.readBytes()
    val buffer = ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN)
    require(buffer.int == 0x00010000) { "This file is not a TrueType font." }
    val count = buffer.short.toInt() and 65535
    require(count in 1..256 && 12 + count * 16 <= data.size) { "Invalid TTF table directory." }
    val tables = mutableSetOf<String>()
    repeat(count) { index ->
        buffer.position(12 + index * 16)
        val tag = ByteArray(4).also { buffer.get(it) }.toString(Charsets.US_ASCII)
        buffer.int // checksum
        val offset = buffer.int.toLong() and 0xffffffffL
        val length = buffer.int.toLong() and 0xffffffffL
        require(offset + length <= data.size && offset >= 12 + count * 16) { "Invalid TTF table: $tag" }
        tables += tag
    }
    require(tables.containsAll(listOf("cmap", "head", "hhea", "hmtx", "maxp", "name", "glyf", "loca"))) {
        "The TTF is missing required TrueType tables."
    }
    return data
}

