// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.fastmail

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
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

val catppuccinFastmailPatch = bytecodePatch(
    name = "Fastmail Catppuccin theme",
    default = false,
    description = "Replace Fastmail dark mode with Catppuccin Macchiato and mauve accents. Light mode remains available.",
) {
    compatibleWith(Compatibility(name = "Fastmail", packageName = "com.fastmail.app", targets = listOf(AppTarget(version = "4.24.0"))))
    dependsOn(fastmailAssets)
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
