package app.morphe.patches.gboard.theme

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch
import java.io.ByteArrayOutputStream
import org.w3c.dom.Element

private fun varint(value: Long): ByteArray {
    var remaining = value
    val out = ByteArrayOutputStream()
    do {
        val byte = (remaining and 127).toInt()
        remaining = remaining ushr 7
        out.write(byte or if (remaining != 0L) 128 else 0)
    } while (remaining != 0L)
    return out.toByteArray()
}
private data class Field(val number: Int, val wire: Int, val value: ByteArray, val raw: ByteArray)
private fun fields(bytes: ByteArray): List<Field> {
    var offset = 0
    fun read(): Long {
        var result = 0L
        for (shift in 0..63 step 7) {
            require(offset < bytes.size) { "Truncated Gboard theme" }
            val byte = bytes[offset++].toInt() and 255
            result = result or ((byte and 127).toLong() shl shift)
            if (byte and 128 == 0) return result
        }
        error("Invalid Gboard theme varint")
    }
    val result = mutableListOf<Field>()
    while (offset < bytes.size) {
        val start = offset
        val tag = read().toInt()
        val wire = tag and 7
        var valueStart = offset
        when (wire) {
            0 -> read()
            1 -> offset += 8
            2 -> {
                val length = read()
                require(length <= Int.MAX_VALUE && length >= 0)
                valueStart = offset
                offset += length.toInt()
            }
            5 -> offset += 4
            else -> error("Unsupported Gboard theme wire type $wire")
        }
        require(offset <= bytes.size && offset >= valueStart)
        result += Field(tag ushr 3, wire, bytes.copyOfRange(valueStart, offset), bytes.copyOfRange(start, offset))
    }
    return result
}
private fun message(number: Int, value: ByteArray): ByteArray =
    varint((number shl 3 or 2).toLong()) + varint(value.size.toLong()) + value
private fun text(number: Int, value: String) = message(number, value.toByteArray(Charsets.UTF_8))

// Preserve the input APK's selector rules and unknown protobuf fields.
// Only this new theme's named color bindings are replaced or appended.
private fun palette(source: ByteArray, colors: Map<String, Long>): ByteArray {
    val out = ByteArrayOutputStream()
    val seen = mutableSetOf<String>()
    fun binding(name: String, color: Long) = message(2,
        text(1, name) + message(2, varint(8) + varint(color)))
    for (field in fields(source)) {
        val name = if (field.number == 2 && field.wire == 2)
            fields(field.value).firstOrNull { it.number == 1 && it.wire == 2 }?.value?.toString(Charsets.UTF_8)
            else null
        val replacement = colors[name]
        if (name != null && replacement != null) {
            out.write(binding(name, replacement)); seen += name
        } else out.write(field.raw)
    }
    for ((name, color) in colors) if (name !in seen) out.write(binding(name, color))
    return out.toByteArray()
}
private fun metadata(source: ByteArray): ByteArray {
    val replacements = mapOf(
        "style_sheet_google_blue_dark.binarypb" to "style_sheet_catppuccin_macchiato_feedback.binarypb",
        "style_sheet_google_blue_dark_border.binarypb" to "style_sheet_catppuccin_macchiato_feedback_border.binarypb",
    )
    val out = ByteArrayOutputStream()
    for (field in fields(source)) {
        if (field.number == 2 && field.wire == 2) {
            val original = field.value.toString(Charsets.UTF_8)
            out.write(text(2, replacements[original] ?: original))
        } else if (field.number == 3 && field.wire == 2) {
            out.write(message(3, metadata(field.value)))
        } else out.write(field.raw)
    }
    return out.toByteArray()
}

@Suppress("unused")
val catppuccinGboardPatch = resourcePatch(
    name = "Gboard Catppuccin theme",
    description = "Adds a Macchiato keyboard theme with mauve accents, inspired by HeliBoard.",
    default = false,
) {
    compatibleWith(Compatibility(name = "Gboard", packageName = "com.google.android.inputmethod.latin",
        targets = listOf(AppTarget(version = "18.4.1.985164140-release-arm64-v8a"))))
    execute {
        val colors = linkedMapOf<String, Long>()
        fun roles(color: Long, vararg names: String) { for (name in names) colors[name] = color }
        roles(0xff24273a, "default_keyboard_background_primary_color", "color_expression_footer_background")
        roles(0xff30344d, "default_keyboard_background_secondary_color", "default_keyboard_background_header_color",
            "color_keyboard_editing_overlay", "default_popup_background_color")
        roles(0xffcad3f5, "default_label_color", "color_state_label_candidate", "color_state_label_candidate_selected",
            "color_keyboard_editing_button", "default_label_color_variant", "default_borderless_key_dark_color_contrast")
        roles(0xffc6a0f6, "default_generic_accent_color", "default_generic_accent_color_strong",
            "default_borderless_space_bar_color",
            "default_action_key_background_color", "color_bottom_indicator_active", "color_label_secondary",
            "color_label_dynamic", "color_expression_content_accented", "default_popup_item_color_pressed",
            "color_state_popup_item_pressed")
        roles(0xffb7bdf8, "default_generic_accent_color_pressed",
            "default_borderless_space_bar_color_pressed", "default_borderless_space_bar_color_hovered",
            "default_action_key_background_color_pressed", "default_action_key_background_color_hovered")
        roles(0xff24273a, "default_generic_accent_color_contrast", "default_action_key_label_color")
        roles(0xff363a4f, "default_bordered_key_color", "default_bordered_key_dark_color",
            "color_generic_extension_background", "color_icon_more_candidates_background",
            "default_pill_shaped_key_color", "default_pill_shaped_key_color_hovered")
        roles(0xff494d64, "default_bordered_key_color_pressed", "default_bordered_key_dark_color_pressed",
            "color_generic_extension_background_activated", "color_keyboard_separator", "color_keyboard_top_separator",
            "color_candidate_separator", "color_candidate_panel_separator", "color_expression_corpus_selector_background_active",
            "color_jarvis_background_active")
        roles(0xff494d64, "default_pill_shaped_key_color_pressed")
        roles(0xff363a4f, "default_transparent_key_color_pressed", "default_transparent_key_color_hovered",
            "color_softkey_highlight_fill")
        val theme = "assets/theme/"
        for (suffix in listOf("", "_border")) {
            val source = get(theme + "style_sheet_google_blue_dark$suffix.binarypb")
            require(source.isFile) { "This Gboard APK has a different bundled theme format" }
            get(theme + "style_sheet_catppuccin_macchiato_feedback$suffix.binarypb").writeBytes(palette(source.readBytes(), colors))
        }
        get(theme + "theme_package_metadata_catppuccin_macchiato_feedback.binarypb").writeBytes(
            metadata(get(theme + "theme_package_metadata_google_blue_dark.binarypb").readBytes()))
        val spec = "assets:theme_package_metadata_catppuccin_macchiato_feedback.binarypb"
        document("res/values/strings.xml").use { xml ->
            for ((name, value) in mapOf("catppuccin_gboard_spec" to spec, "catppuccin_gboard_label" to "Catppuccin Macchiato")) {
                val nodes = xml.documentElement.childNodes
                (0 until nodes.length).mapNotNull { nodes.item(it) as? Element }
                    .filter { it.getAttribute("name") == name }.forEach { xml.documentElement.removeChild(it) }
                xml.documentElement.appendChild(xml.createElement("string").apply {
                    setAttribute("name", name); textContent = value
                })
            }
        }
        document("res/values/arrays.xml").use { xml ->
            val arrays = xml.documentElement.childNodes
            var catalog = false
            var labels = false
            for (i in 0 until arrays.length) {
                val array = arrays.item(i) as? Element ?: continue
                val nodes = array.childNodes
                val values = (0 until nodes.length).mapNotNull { (nodes.item(it) as? Element)?.textContent }
                if ("@string/string_0x7f1408d8" !in values) continue
                val withLabels = "@string/string_0x7f140595" in values
                if (withLabels) labels = true else catalog = true
                if ("@string/catppuccin_gboard_spec" !in values) {
                    array.appendChild(xml.createElement("item").apply { textContent = "@string/catppuccin_gboard_spec" })
                    if (withLabels) array.appendChild(xml.createElement("item").apply { textContent = "@string/catppuccin_gboard_label" })
                }
            }
            require(catalog && labels) { "Could not locate Gboard's bundled theme catalog" }
        }
    }
}
