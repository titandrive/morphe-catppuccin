// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.nytgames

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import app.morphe.patcher.patch.filePathOption
import app.morphe.patches.shared.fonts.readTrueType
import org.w3c.dom.Element

private val nytGames = Compatibility(name = "NYT Games", packageName = "com.nytimes.crossword",
    targets = listOf(AppTarget(version = "6.46.1")))

private val nytGamesResources = resourcePatch {
    compatibleWith(nytGames)
    execute {
        val colors = linkedMapOf(
            "backgroundPrimary" to "24273a", "backgroundPage" to "24273a", "backgroundModal" to "363a4f",
            "backgroundPageSubdued" to "1e2030", "backgroundSecondary" to "1e2030", "backgroundTertiary" to "363a4f",
            "backgroundElevated" to "363a4f", "backgroundHighlight" to "494d64", "backgroundScrim" to "99181926",
            "contentPrimary" to "cad3f5", "contentPrimaryDim" to "b8c0e0", "contentText" to "cad3f5",
            "contentSecondary" to "b8c0e0", "contentSecondaryDim" to "a5adcb", "contentTextSecondarySubdued" to "a5adcb",
            "contentPlaceholder" to "8087a2", "content_placeholder" to "8087a2", "contentTextTertiary_static" to "8087a2",
            "contentAccent" to "c6a0f6", "contentAccentDim" to "b7bdf8", "contentNegative" to "ed8796",
            "contentPositive" to "a6da95", "contentBreaking" to "ed8796",
            "strokePrimary" to "cad3f5", "strokeSecondary" to "8087a2", "strokeTertiary" to "494d64",
            "settingsBg" to "24273a", "settingsTopBarBg" to "1e2030", "settingsRowText" to "cad3f5",
            "settingsRowDetailText" to "a5adcb", "settingsTopBarText" to "cad3f5", "settingsTopBarButton" to "c6a0f6",
            "settingsSwitch" to "c6a0f6", "settings_text_color_primary" to "cad3f5", "about_background" to "24273a",
            "about_divider" to "494d64", "about_value_color" to "a5adcb", "pref_divider" to "494d64",
            "listSeparator" to "494d64", "separator_color" to "494d64", "puzzle_browser_divider" to "494d64",
            "archiveBackground" to "24273a", "archiveTextColor" to "cad3f5", "archiveCalendarDaysTextColor" to "b8c0e0",
            "archiveCalendarCellDayNumberTextColor" to "cad3f5", "archiveDaysDividerColor" to "494d64",
            "archiveLoginLinkTextColor" to "c6a0f6", "archiveNonEntitledDaysDividerColor" to "494d64",
            "archive_arrow_normal" to "cad3f5", "archive_arrow_disabled" to "6e738d", "archive_arrow_pressed" to "c6a0f6",
            "nav_tab_background" to "1e2030", "nav_indicator_color" to "363a4f", "nav_tab_selected_text" to "c6a0f6",
            "nav_tab_unselected_text" to "a5adcb", "toolbar_color_control_normal" to "cad3f5",
            "colorAccent" to "c6a0f6", "colorPrimary" to "cad3f5", "colorPrimaryDark" to "1e2030",
            "gameScreenToolbar" to "1e2030", "game_screen_toolbar_color" to "1e2030", "unityNavHeader" to "1e2030",
            "blackButton" to "c6a0f6", "blackButtonActivated" to "b7bdf8", "blackButtonBorder" to "c6a0f6",
            "blackButtonText" to "24273a", "blackButtonBackground" to "c6a0f6", "blackButtonDisabled" to "446e738d",
            "errorButtonBackgroundColor" to "c6a0f6", "errorButtonTextColor" to "24273a", "errorNoticeTextColor" to "ed8796",
            "puzzleInfoBg" to "24273a", "puzzleInfoTopBarBg" to "1e2030", "puzzleInfoButton" to "c6a0f6",
            "puzzleInfoText" to "cad3f5", "puzzleInfoTopBarText" to "cad3f5",
            "solvingScreenBg" to "24273a", "solvingScreenTopBarBg" to "1e2030", "solvingScreenTopBarText" to "cad3f5",
            "solvingScreenTopBarIconNormal" to "c6a0f6", "solvingScreenTopBarIconBgSelected" to "c6a0f6",
            "kbBg" to "1e2030", "kbKeyBg" to "363a4f", "kbHelperKeyBg" to "494d64",
            "kbKeyBgHighlighted" to "c6a0f6", "kbHelperKeyBgHighlighted" to "5b6078",
            "kbKeyText" to "cad3f5", "kbHelperKeyText" to "cad3f5",
            "squareBgUnselectedEmpty" to "363a4f", "squareBgUnselectedBlack" to "181926", "squareBgUnselectedShaded" to "494d64",
            "squareBgPrimary" to "8aadf4", "squareBgPrimaryShaded" to "b7bdf8", "squareBgSelected" to "c6a0f6",
            "squareBgSelectedShaded" to "b7bdf8", "squareBgRelated" to "494d64", "squareBorder" to "181926",
            "squareTextSelectedOrPrimaryInk" to "24273a", "squareTextSelectedOrPrimaryPencil" to "9924273a",
            "squareTextUnselectedInk" to "cad3f5", "squareClueNumberUnselected" to "cad3f5",
            "squareTextConfirmedUnselected" to "8aadf4", "squareCheckedIncorrectSlash" to "ed8796",
            "squareCircleStroke" to "8087a2", "squareRevealFlag" to "ed8796", "squareRevealFlagBall" to "24273a",
            "squareRebusBg" to "363a4f", "squareRebusText" to "cad3f5", "squareRebusLabelText" to "cad3f5",
            "squareRebusBorder" to "c6a0f6", "clueBarArrow" to "cad3f5", "clueBarBgAlternate" to "1e2030",
            "clueBarBgPrimary" to "363a4f", "clueBarDirectionTextAlternate" to "b8c0e0",
            "clueBarDirectionTextPrimary" to "cad3f5", "clueBarTextPrimary" to "cad3f5",
            "clueBrowserHeaderBgNormal" to "1e2030", "clueBrowserHeaderText" to "cad3f5",
            "clueBrowserSquareBgPrimary" to "363a4f", "clueBrowserSquareBorderPrimary" to "494d64",
            "clueListBgUnselected" to "24273a", "clueListBgPrimary" to "363a4f", "clueListBgRelated" to "494d64",
            "clueListHeaderBgNormal" to "1e2030", "clueListHeaderText" to "cad3f5", "clueListTextPrimaryColor" to "cad3f5",
            "wordlePrimary" to "24273a", "wordleErrorText" to "cad3f5", "wordleErrorBtnText" to "24273a",
            "rounded_blue_button_border" to "c6a0f6", "preferences_highlight_color" to "c6a0f6",
            "external_link_text" to "8aadf4", "pref_promo_color" to "c6a0f6", "try_for_free_color" to "c6a0f6",
        )
        // Update the original night table in place, retaining every resource ID.
        // Never recolor bitmap logos or game artwork.
        document("res/values-night/colors.xml").use { xml ->
            val existing = mutableMapOf<String, Element>()
            val nodes = xml.getElementsByTagName("color")
            for (i in 0 until nodes.length) {
                val node = nodes.item(i) as Element
                existing[node.getAttribute("name")] = node
            }
            for ((name, color) in colors) {
                val node = existing[name] ?: xml.createElement("color").also {
                    it.setAttribute("name", name); xml.documentElement.appendChild(it)
                }
                node.textContent = "#$color"
            }
        }
        // Compose can request the AlwaysDark semantic tokens regardless of configuration.
        document("res/values/colors.xml").use { xml ->
            val nodes = xml.getElementsByTagName("color")
            for (i in 0 until nodes.length) {
                val node = nodes.item(i) as Element
                val name = node.getAttribute("name")
                if (name.endsWith("AlwaysDark")) colors[name.removeSuffix("AlwaysDark")]?.let { node.textContent = "#$it" }
            }
        }
        println("NYT Games: mapped ${colors.size} dark-mode UI and crossword color resources")
    }
}

val nytGamesCustomFontPatch = resourcePatch(
    name = "NYT Games custom font",
    description = "Embed your TTF as NYT Games' bundled interface fonts. Experimental coverage across individual games.",
    default = false,
) {
    compatibleWith(nytGames)
    val fontFile = filePathOption(key = "font-file", default = null, title = "Custom TTF font",
        description = "Choose a TTF file to use in NYT Games.", required = true, allowedExtensions = listOf("ttf"))
    execute {
        val bytes = readTrueType(requireNotNull(fontFile.value) { "Choose a TTF font file." })
        var count = 0
        // Replace font payloads, preserving resource IDs and family definitions used by Compose and Canvas.
        for (folder in listOf(get("res/font"), get("assets/fonts"))) {
            for (file in folder.walkTopDown()) {
                if (!file.isFile || file.extension.lowercase() !in listOf("ttf", "otf")) continue
                file.writeBytes(bytes); count++
            }
        }
        require(count > 0) { "NYT Games bundled font layout changed" }
        println("NYT Games: embedded custom TTF in $count bundled font files")
    }
}

val nytGamesThemePatch = bytecodePatch(
    name = "NYT Games Catppuccin theme",
    description = "Catppuccin Macchiato for NYT Games dark mode, including native Compose screens and crossword resources.",
    default = false,
) {
    compatibleWith(nytGames)
    dependsOn(nytGamesResources)
    extendWith("catppuccin/nytgames.dex")
    execute {
        val holder = "Lcom/nytimes/games/designsystem/theme/ExtendedColorsKt;"
        val init = mutableClassDefBy(holder).methods.single { it.name == "<clinit>" }
        val targets = init.implementation!!.instructions.mapIndexedNotNull { index, instruction ->
            val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
            if (instruction.opcode == com.android.tools.smali.dexlib2.Opcode.SPUT_OBJECT && field?.definingClass == holder && field.name == "b")
                index to (instruction as OneRegisterInstruction).registerA else null
        }
        require(targets.size == 1) { "NYT dark Compose palette changed" }
        val (index, register) = targets.single()
        init.addInstructions(index, "invoke-static/range {v$register .. v$register}, Lapp/morphe/extension/nytgames/NytTheme;->core(Ljava/lang/Object;)Ljava/lang/Object;\nmove-result-object v$register\ncheck-cast v$register, Lcom/nytimes/games/designsystem/theme/CoreThemeColors;")
        val getter = mutableClassDefBy("Lcom/nytimes/games/designsystem/theme/GamesTheme;").methods.single {
            it.name == "a" && it.returnType == "Lcom/nytimes/games/designsystem/theme/ExtendedColors;"
        }
        require(getter.implementation!!.registerCount >= 2) { "NYT palette getter registers changed" }
        getter.addInstructions(0, "invoke-static/range {p0 .. p0}, Lapp/morphe/extension/nytgames/NytTheme;->extended(Ljava/lang/Object;)Ljava/lang/Object;\nmove-result-object v0\ncheck-cast v0, Lcom/nytimes/games/designsystem/theme/ExtendedColors;\nreturn-object v0")
    }
}
