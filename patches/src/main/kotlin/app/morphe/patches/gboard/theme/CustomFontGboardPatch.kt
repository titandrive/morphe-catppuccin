package app.morphe.patches.gboard.theme

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.stringOption
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.util.proxy.mutableTypes.MutableMethod.Companion.toMutable
import app.morphe.patcher.util.proxy.mutableTypes.MutableField.Companion.toMutable
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.builder.MutableMethodImplementation
import com.android.tools.smali.dexlib2.immutable.ImmutableMethod
import com.android.tools.smali.dexlib2.immutable.ImmutableMethodParameter
import com.android.tools.smali.dexlib2.immutable.ImmutableField
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import org.w3c.dom.Element

// Validate the SFNT directory before embedding a file that Android will load.
private fun readTrueType(path: String): ByteArray {
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

@Suppress("unused")
val customFontGboardPatch = bytecodePatch(
    name = "Gboard custom font",
    description = "Imports a TTF font and adds it to Gboard's Font selector.",
    default = false,
) {
    compatibleWith(Compatibility(name = "Gboard", packageName = "com.google.android.inputmethod.latin",
        targets = listOf(AppTarget(version = "18.4.1.985164140-release-arm64-v8a"))))
    val fontFile = stringOption(
        key = "font-file", default = null, title = "Custom TTF font",
        description = "TTF file path. Choose the font file to embed in the keyboard. Required when this patch is selected.",
        required = true,
    )
    dependsOn(resourcePatch {
        execute {
            val path = requireNotNull(fontFile.value) { "Choose a TTF font file." }
            val bytes = readTrueType(path)
            get("assets/catppuccin/custom-font.ttf").apply { parentFile.mkdirs(); writeBytes(bytes) }
            document("res/values/arrays.xml").use { xml ->
                for ((name, values) in mapOf(
                    "catppuccin_font_labels" to listOf("Gboard default", "System default", File(path).nameWithoutExtension),
                    "catppuccin_font_values" to listOf("default", "system", "custom"),
                )) {
                    val array = xml.createElement("string-array").apply { setAttribute("name", name) }
                    for (value in values) array.appendChild(xml.createElement("item").apply { textContent = value })
                    xml.documentElement.appendChild(array)
                }
            }
            document("res/xml/setting_preferences.xml").use { xml ->
                val elements = xml.getElementsByTagName("com.google.android.libraries.inputmethod.preferencewidgets.list.ListBooleanPreference")
                val existing = (0 until elements.length).map { elements.item(it) as Element }
                    .single { it.getAttribute("android:key") == "@string/string_0x7f140aad" }
                val replacement = xml.createElement("com.google.android.libraries.inputmethod.preferencewidgets.list.ListPreference")
                for ((name, value) in mapOf(
                    "title" to "@string/string_0x7f140ca2", "dialogTitle" to "@string/string_0x7f140ca2",
                    "key" to "catppuccin_keyboard_font", "summary" to "%s", "persistent" to "true",
                    "defaultValue" to "custom", "entries" to "@array/catppuccin_font_labels",
                    "entryValues" to "@array/catppuccin_font_values",
                )) replacement.setAttributeNS("http://schemas.android.com/apk/res/android", "android:$name", value)
                existing.parentNode.replaceChild(replacement, existing)
            }
        }
    })
    execute {
        // Keep each key's original font so switching back restores it, even when views are reused.
        val owner = mutableClassDefBy("Lcom/google/android/libraries/inputmethod/widgets/text/AutoCenterScaleTextView;")
        val apply = owner.methods.single { it.name == "setTypeface" && it.parameterTypes == listOf("Landroid/graphics/Typeface;") }
        owner.instanceFields.add(ImmutableField(owner.type, "catppuccinOriginalFont", "Landroid/graphics/Typeface;",
            AccessFlags.PRIVATE.value, null, emptySet(), emptySet()).toMutable())
        owner.instanceFields.add(ImmutableField(owner.type, "catppuccinFontCaptured", "Z",
            AccessFlags.PRIVATE.value, null, emptySet(), emptySet()).toMutable())
        owner.staticFields.add(ImmutableField(owner.type, "catppuccinFont", "Landroid/graphics/Typeface;",
            AccessFlags.PRIVATE.value or AccessFlags.STATIC.value, null, emptySet(), emptySet()).toMutable())
        owner.directMethods.add(ImmutableMethod(owner.type, "catppuccinSelectFont", listOf(
            ImmutableMethodParameter("Landroid/view/View;", emptySet(), null),
            ImmutableMethodParameter("Landroid/graphics/Typeface;", emptySet(), null),
        ), "Landroid/graphics/Typeface;", AccessFlags.PRIVATE.value or AccessFlags.STATIC.value,
            emptySet(), emptySet(), MutableMethodImplementation(6)).toMutable().apply {
            addInstructionsWithLabels(0, """
                :font_try
                invoke-virtual {p0}, Landroid/view/View;->getContext()Landroid/content/Context;
                move-result-object v0
                invoke-static {v0}, Lqou;->I(Landroid/content/Context;)Lqou;
                move-result-object v1
                const-string v2, "catppuccin_keyboard_font"
                const-string v3, "custom"
                invoke-virtual {v1, v2, v3}, Lcdx;->d(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                move-result-object v1
                invoke-virtual {v3, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v2
                if-eqz v2, :font_system
                sget-object v1, ${owner.type}->catppuccinFont:Landroid/graphics/Typeface;
                if-nez v1, :font_custom
                invoke-virtual {v0}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
                move-result-object v0
                const-string v1, "catppuccin/custom-font.ttf"
                invoke-static {v0, v1}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
                move-result-object v1
                sput-object v1, ${owner.type}->catppuccinFont:Landroid/graphics/Typeface;
                :font_custom
                return-object v1
                :font_system
                const-string v2, "system"
                invoke-virtual {v2, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
                move-result v2
                if-eqz v2, :font_default
                sget-object v1, Landroid/graphics/Typeface;->DEFAULT:Landroid/graphics/Typeface;
                return-object v1
                :font_try_end
                .catch Ljava/lang/RuntimeException; {:font_try .. :font_try_end} :font_failed
                :font_failed
                move-exception v0
                :font_default
                check-cast p0, ${owner.type}
                iget-object p1, p0, ${owner.type}->catppuccinOriginalFont:Landroid/graphics/Typeface;
                return-object p1
            """)
        })
        owner.directMethods.add(ImmutableMethod(owner.type, "catppuccinCaptureFont", listOf(
            ImmutableMethodParameter("Landroid/graphics/Typeface;", emptySet(), null),
        ), "V", AccessFlags.PRIVATE.value, emptySet(), emptySet(), MutableMethodImplementation(3)).toMutable().apply {
            addInstructionsWithLabels(0, """
                iget-boolean v0, p0, ${owner.type}->catppuccinFontCaptured:Z
                if-nez v0, :captured
                iput-object p1, p0, ${owner.type}->catppuccinOriginalFont:Landroid/graphics/Typeface;
                const/4 v0, 0x1
                iput-boolean v0, p0, ${owner.type}->catppuccinFontCaptured:Z
                :captured
                return-void
            """)
        })
        owner.methods.single { it.name == "onDraw" }.addInstructionsWithLabels(0, """
            iget-boolean v0, p0, ${owner.type}->catppuccinFontCaptured:Z
            if-eqz v0, :font_draw
            iget-object v0, p0, ${owner.type}->catppuccinOriginalFont:Landroid/graphics/Typeface;
            invoke-static {p0, v0}, ${owner.type}->catppuccinSelectFont(Landroid/view/View;Landroid/graphics/Typeface;)Landroid/graphics/Typeface;
            move-result-object v0
            invoke-virtual {p0}, Landroid/widget/TextView;->getTypeface()Landroid/graphics/Typeface;
            move-result-object v1
            if-eq v0, v1, :font_draw
            invoke-virtual {p0, v0}, ${owner.type}->setTypeface(Landroid/graphics/Typeface;)V
            :font_draw
            nop
        """)
        apply.addInstructionsWithLabels(0, """
            invoke-direct {p0, p1}, ${owner.type}->catppuccinCaptureFont(Landroid/graphics/Typeface;)V
            invoke-static {p0, p1}, ${owner.type}->catppuccinSelectFont(Landroid/view/View;Landroid/graphics/Typeface;)Landroid/graphics/Typeface;
            move-result-object p1
        """)
    }
}
