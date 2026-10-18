// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.fastmail

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.filePathOption
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions

private object FastmailResources

private val fastmailAssets = resourcePatch {
    execute {
        val target = get("assets/catppuccin/fastmail/macchiato.css")
        target.parentFile.mkdirs()
        FastmailResources::class.java.classLoader.getResourceAsStream("catppuccin/fastmail/macchiato.css")!!.use { input ->
            target.outputStream().use { input.copyTo(it) }
        }
    }
}

private val fastmailWebHooks = bytecodePatch {
    extendWith("catppuccin/fastmail.dex")
    execute {
        val owners = mutableListOf<String>()
        classDefForEach { klass ->
            if (klass.superclass == "Landroid/webkit/WebViewClient;" && klass.methods.any {
                it.name == "onPageFinished" && it.parameterTypes == listOf("Landroid/webkit/WebView;", "Ljava/lang/String;")
            }) owners.add(klass.type)
        }
        require(owners.size == 1) { "Fastmail WebView callback changed; refusing ambiguous theme injection: $owners" }
        val callback = mutableClassDefBy(owners.single()).methods.single { it.name == "onPageFinished" }
        callback.addInstructions(0, "invoke-static {p1, p2}, Lapp/morphe/extension/fastmail/FastmailTheme;->apply(Landroid/webkit/WebView;Ljava/lang/String;)V")
    }
}

val catppuccinFastmailPatch = bytecodePatch(
    name = "Fastmail Catppuccin theme",
    default = false,
    description = "Replace Fastmail dark mode with Catppuccin Macchiato and mauve accents. Light mode remains available.",
) {
    compatibleWith(Compatibility(name = "Fastmail", packageName = "com.fastmail.app", targets = listOf(AppTarget(version = "4.24.0"))))
    dependsOn(fastmailAssets, fastmailWebHooks)
}


val customFontFastmailPatch = bytecodePatch(
    name = "Fastmail custom font",
    description = "Use your TTF font in Fastmail's interface. Repatch an original APK to change or remove it.",
    default = false,
) {
    compatibleWith(Compatibility(name = "Fastmail", packageName = "com.fastmail.app", targets = listOf(AppTarget(version = "4.24.0"))))
    val fontFile = filePathOption(
        key = "font-file", default = null, title = "Custom TTF font",
        description = "Choose a TTF file to embed as Fastmail's interface font.",
        required = true, allowedExtensions = listOf("ttf"),
    )
    dependsOn(fastmailWebHooks, resourcePatch {
        execute {
            val bytes = app.morphe.patches.shared.fonts.readTrueType(requireNotNull(fontFile.value) { "Choose a TTF font file." })
            get("assets/catppuccin/fastmail/custom.ttf").apply { parentFile.mkdirs(); writeBytes(bytes) }
            get("assets/catppuccin/fastmail/font-settings.js").apply {
                parentFile.mkdirs()
                FastmailResources::class.java.classLoader.getResourceAsStream("catppuccin/fastmail/font-settings.js")!!.use { input ->
                    outputStream().use { input.copyTo(it) }
                }
            }
            get("res/font/catppuccin_fastmail.ttf").apply { parentFile.mkdirs(); writeBytes(bytes) }
            for (directory in get("res").listFiles().orEmpty()) {
                if (!directory.isDirectory || !directory.name.startsWith("values")) continue
                for (file in directory.listFiles().orEmpty()) {
                    if (file.extension != "xml" || !file.readText().contains("<style")) continue
                    document("res/${directory.name}/${file.name}").use { xml ->
                        val styles = xml.getElementsByTagName("style")
                        for (i in 0 until styles.length) {
                            val style = styles.item(i) as org.w3c.dom.Element
                            for (attr in listOf("android:fontFamily", "fontFamily")) {
                                val items = style.getElementsByTagName("item")
                                val existing = (0 until items.length).map { items.item(it) as org.w3c.dom.Element }
                                    .firstOrNull { it.getAttribute("name") == attr }
                                if (existing == null && !style.getAttribute("name").startsWith("AppTheme")) continue
                                val item = existing ?: xml.createElement("item").also { it.setAttribute("name", attr); style.appendChild(it) }
                                item.textContent = "@font/catppuccin_fastmail"
                            }
                        }
                    }
                }
            }
        }
    })
}
