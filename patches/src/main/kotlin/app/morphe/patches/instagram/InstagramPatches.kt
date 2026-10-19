// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.instagram

import app.morphe.patcher.patch.*
import app.morphe.patches.shared.fonts.readTrueType
import org.w3c.dom.Element
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.FieldReference
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private val instagram = Compatibility(name = "Instagram", packageName = "com.instagram.android",
    targets = listOf(AppTarget(version = "447.0.0.55.81")))

private val instagramThemeResources = resourcePatch {
    compatibleWith(instagram)
    execute {
        for ((qualifier, dark) in listOf("values" to false, "values-night" to true)) {
            val base = if (dark) "24273a" else "eff1f5"
            val mantle = if (dark) "1e2030" else "e6e9ef"
            val surface = if (dark) "363a4f" else "ccd0da"
            val text = if (dark) "cad3f5" else "4c4f69"
            val secondary = if (dark) "a5adcb" else "6c6f85"
            val mauve = if (dark) "c6a0f6" else "8839ef"
            val onAccent = if (dark) "24273a" else "eff1f5"
            val blue = if (dark) "8aadf4" else "1e66f5"
            val colors = linkedMapOf(
                "igds_primary_background" to base, "igds_secondary_background" to mantle,
                "igds_elevated_background" to mantle, "igds_elevated_highlight_background" to surface,
                "igds_primary_text" to text, "igds_primary_icon" to text,
                "igds_secondary_text" to secondary, "igds_secondary_icon" to secondary,
                "igds_primary_text_disabled" to "66$text", "igds_primary_button" to mauve,
                "igds_link" to blue, "igds_pill_active_text" to mauve,
                "igds_separator" to surface, "igds_stroke" to surface, "igds_elevated_separator" to surface,
                "igds_context_menu_background_color" to mantle,
                "igds_context_menu_item_background_color" to "22$surface",
                "igds_photo_placeholder" to surface, "igds_selected_text_background" to "55$mauve",
                "igds_error_or_destructive" to if (dark) "ed8796" else "d20f39",
                "igds_success" to if (dark) "a6da95" else "40a02b",
                "ig_splash_screen_background" to base,
                "igds_prism_primary_button_background_indigo" to mauve,
                "igds_prism_primary_button_label_indigo" to onAccent,
                "igds_prism_primary_borderless_button_indigo" to mauve,
                "igds_prism_v2_primary_borderless_button_indigo" to mauve,
                "igds_prism_indigo_borderless_button_link" to mauve,
                "igds_prism_v2_indigo_borderless_button_link" to mauve,
                "igds_prism_secondary_button_background_filled" to surface,
                "igds_prism_secondary_button_label_A" to text,
                "igds_prism_secondary_borderless_button_label_A" to text,
                "igds_icon_on_color" to onAccent,
                "ig_loader_ring_background" to surface,
                "refreshable_progress_drawable_track" to surface,
                "border_primary" to surface, "stroke_color" to surface,
                "notes_bubble_background" to surface,
                "checkbox_checked_enabled" to mauve,
                "radio_button_unchecked_enabled" to secondary,
                "radio_button_checked_disabled" to if (dark) "6e738d" else "acb0be",
                "radio_button_unchecked_disabled" to if (dark) "5b6078" else "bcc0cc",
            )
            // Some settings and server-rendered controls use palette primitives directly.
            // Recolor named UI resources only; bitmap pixels and branded artwork stay intact.
            val primitives = if (dark) mapOf(
                "bds_black" to base, "bds_white" to text,
                "bds_grey_0" to text, "bds_grey_1" to "b8c0e0", "bds_grey_2" to secondary,
                "bds_grey_3" to secondary, "bds_grey_4" to "939ab7", "bds_grey_6" to "8087a2",
                "bds_grey_7" to "6e738d", "bds_grey_8" to "494d64", "bds_grey_9" to surface,
                "bds_grey_10" to mantle, "bds_grey_11" to secondary, "bds_grey_12" to surface,
                "bds_grey_13" to text, "bds_grey_16" to mantle, "bds_grey_18" to text,
                "bds_grey_21" to surface, "bds_grey_22" to base, "bds_grey_24" to base,
                "igds_prism_black" to base,
                "igds_prism_gray_00" to text, "igds_prism_gray_01" to "b8c0e0",
                "igds_prism_gray_02" to secondary, "igds_prism_gray_03" to secondary,
                "igds_prism_gray_04" to "939ab7", "igds_prism_gray_05" to "8087a2",
                "igds_prism_gray_06" to "6e738d", "igds_prism_gray_07" to "5b6078",
                "igds_prism_gray_08" to surface, "igds_prism_gray_09" to mantle,
                "igds_prism_gray_10" to mantle, "igds_prism_gray_11" to surface,
                "igds_prism_gray_13" to "494d64", "igds_prism_gray_14" to base,
                "igds_prism_gray_1500" to mantle, "igds_prism_gray_1550" to base,
            ) else mapOf(
                "bds_black" to text, "bds_white" to base,
                "bds_grey_0" to mantle, "bds_grey_1" to mantle, "bds_grey_2" to surface,
                "bds_grey_3" to "bcc0cc", "bds_grey_4" to "acb0be", "bds_grey_6" to secondary,
                "bds_grey_7" to "5c5f77", "bds_grey_8" to text, "bds_grey_9" to text,
                "bds_grey_10" to text, "bds_grey_11" to "bcc0cc", "bds_grey_12" to text,
                "bds_grey_13" to mantle, "bds_grey_16" to text, "bds_grey_18" to base,
                "bds_grey_21" to text, "bds_grey_22" to text, "bds_grey_24" to text,
                "igds_prism_black" to text,
                "igds_prism_gray_00" to base, "igds_prism_gray_01" to mantle,
                "igds_prism_gray_02" to "dce0e8", "igds_prism_gray_03" to surface,
                "igds_prism_gray_04" to "acb0be", "igds_prism_gray_05" to "9ca0b0",
                "igds_prism_gray_06" to "8c8fa1", "igds_prism_gray_07" to "7c7f93",
                "igds_prism_gray_08" to secondary, "igds_prism_gray_09" to "5c5f77",
                "igds_prism_gray_10" to text, "igds_prism_gray_11" to text,
                "igds_prism_gray_13" to text, "igds_prism_gray_14" to text,
                "igds_prism_gray_1500" to text, "igds_prism_gray_1550" to text,
            )
            // Build overrides from the original base table, including alpha variants.
            document("res/values/colors.xml").use { xml ->
                val nodes = xml.getElementsByTagName("color")
                for (i in 0 until nodes.length) {
                    val node = nodes.item(i) as Element
                    val name = node.getAttribute("name")
                    val root = primitives.keys.firstOrNull { name == it || name.startsWith(it + "_alpha_") || name.startsWith(it + "_") && name.endsWith("_transparent") }
                    // Prism's newer onboarding screens request numeric indigo tokens.
                    if (name.startsWith("igds_prism_indigo_") && name.removePrefix("igds_prism_indigo_").all { it.isDigit() }) {
                        colors[name] = mauve
                    }
                    if (name in setOf("igds_prism_bright_text_link_dark", "igds_prism_bright_text_link_light")) colors[name] = blue
                    if (root != null) {
                        val value = node.textContent.trim().removePrefix("#")
                        val alpha = if (value.length == 8 && value.all { it in "0123456789abcdefABCDEF" }) value.take(2) else "ff"
                        colors[name] = alpha + primitives.getValue(root)
                    }
                }
            }
            val themeColors = colors + mapOf(
                "igds_controls" to mauve,
                "igds_stories_loading_background" to base,
                "igds_reaction_background" to surface,
                "igds_reaction_selected_background" to if (dark) "494d64" else "bcc0cc",
                "igds_prism_card_background" to mantle,
                "igds_generic_xma_background_color" to mantle,
                "igds_secondary_background_strong" to surface,
                "igds_secondary_button_background_strong" to surface,
                "igds_tertiary_background" to mantle,
                "igds_elevated_background_dark" to mantle,
                "igds_elevated_background_dark_pressed" to surface,
                "igds_elevated_background_intent_card" to mantle,
                "igds_elevated_background_prompt_suggestion" to surface,
                "igds_highlight_background" to surface,
                "igds_pill_background" to surface,
                "igds_pill_background_pressed" to if (dark) "494d64" else "bcc0cc",
                "igds_pill_active_background" to mauve,
                "igds_prism_pill_active_background" to mauve,
                "igds_prism_chip_background_pressed" to surface,
                "igds_prism_chip_background_selected" to surface,
                "igds_prism_chip_background_stroke" to if (dark) "494d64" else "bcc0cc",
                "igds_inbox_filter_chip_selected_background" to surface,
                "igds_inbox_filter_chip_pressed_background" to if (dark) "494d64" else "bcc0cc",
                "igds_form_field_background_default_color" to surface,
                "igds_form_field_background_focussed_color" to surface,
                "igds_form_field_background_disabled_color" to mantle,
                "igds_primary_button_icon" to onAccent,
                "igds_primary_button_indigo" to mauve,
                "igds_primary_button_pressed" to if (dark) "b7bdf8" else "7287fd",
                "igds_notification_background" to mantle,
                "igds_tag_or_toast_background" to mantle,
                "igds_toast_95_alpha_background" to "f2$mantle",
                "igds_toast_background" to "ee$mantle",
            )
            // Keep style entries as references: Instagram reads their resource IDs.
            // Both explicit palettes live in the base table, independent of configuration.
            val prefix = if (dark) "catppuccin_instagram_macchiato_" else "catppuccin_instagram_latte_"
            document("res/values/colors.xml").use { xml ->
                for ((name, color) in themeColors) {
                    val node = xml.createElement("color")
                    node.setAttribute("name", prefix + name)
                    node.textContent = "#$color"
                    xml.documentElement.appendChild(node)
                }
            }
            // Instagram's Prism and legacy themes often bypass the named color
            // resources, so update their semantic attribute tables as well.
            document("res/values/styles.xml").use { xml ->
                val styles = xml.getElementsByTagName("style")
                for (i in 0 until styles.length) {
                    val style = styles.item(i) as Element
                    val name = style.getAttribute("name")
                    val semanticStyle = name.startsWith("Igds") && name.endsWith(if (dark) "Dark" else "Light")
                    val nativeStyle = name in if (dark) setOf("BaseNightTheme", "DirectDarkMode") else setOf("Theme.Instagram", "DirectLightMode")
                    if (!semanticStyle && !nativeStyle) continue
                    val nativeTokens = mapOf(
                        "android:colorAccent" to "igds_primary_button",
                        "colorAccent" to "igds_primary_button",
                        "colorControlActivated" to "igds_primary_button",
                        "android:colorControlActivated" to "igds_primary_button",
                        "storyViewerScrimBackgroundColor" to "igds_primary_background",
                        "messageFromOthersGrayBackground" to "igds_elevated_highlight_background",
                        "commentsMessagePillBackgroundColor" to "igds_reaction_background",
                        "reactionsMessagePillBackgroundColor" to "igds_reaction_background",
                        "messageComposerBackgroundColor" to "igds_primary_background",
                        "messageComposerRedesignBackgroundColor" to "igds_elevated_highlight_background",
                        "messageComposerBorderColor" to "igds_stroke",
                        "actionBarBackgroundColor" to "igds_primary_background",
                        "backgroundColorPrimary" to "igds_primary_background",
                        "windowBackgroundColor" to "igds_primary_background",
                        "dividerColor" to "igds_stroke",
                        "elevatedBackgroundColor" to "igds_elevated_background",
                        "elevatedBackgroundColorDark" to "igds_elevated_background",
                        "elevatedHighlightBackgroundColor" to "igds_elevated_highlight_background",
                        "textColorPrimary" to "igds_primary_text",
                        "textColorSecondary" to "igds_secondary_text",
                        "glyphColorPrimary" to "igds_primary_icon",
                        "glyphColorSecondary" to "igds_secondary_icon",
                    )
                    val items = style.getElementsByTagName("item")
                    for (j in 0 until items.length) {
                        val item = items.item(j) as Element
                        val attribute = item.getAttribute("name")
                        val token = nativeTokens[attribute] ?: attribute.replace("igds_color_", "igds_")
                        if (token in themeColors) item.textContent = "@color/$prefix$token"
                    }
                }
            }
            document("res/$qualifier/colors.xml").use { xml ->
                val nodes = xml.getElementsByTagName("color")
                val existing = (0 until nodes.length).associate { i ->
                    val node = nodes.item(i) as Element
                    node.getAttribute("name") to node
                }
                // Add qualifier overrides only for IDs present in the original base table.
                for ((name, color) in colors) {
                    val node = existing[name] ?: xml.createElement("color").also {
                        it.setAttribute("name", name); xml.documentElement.appendChild(it)
                    }
                    node.textContent = "#$color"
                }
            }
        }
    }
}

val instagramThemePatch = bytecodePatch(
    name = "Instagram Catppuccin theme",
    description = "Experimental Catppuccin Latte and Macchiato for native and Compose interfaces.",
    default = false,
) {
    compatibleWith(instagram)
    dependsOn(instagramThemeResources)
    extendWith("catppuccin/instagram.dex")
    execute {
        // The viewer seeds both its transition state and background with black
        // before media loads. Use the fragment's selected theme from the first frame.
        val viewer = "Linstagram/features/stories/fragment/ReelViewerFragment;"
        val create = mutableClassDefBy(viewer).methods.single { it.name == "onCreateView" }
        val createInstructions = create.implementation!!.instructions.toList()
        val inflations = createInstructions.mapIndexedNotNull { i, instruction ->
            val target = (instruction as? ReferenceInstruction)?.reference as? MethodReference
            if (target?.definingClass == "Landroid/view/LayoutInflater;" && target.name == "inflate" &&
                target.returnType == "Landroid/view/View;") i else null
        }
        require(inflations.size == 1) { "Instagram story root inflation changed" }
        val inflated = inflations.single() + 1
        require(createInstructions[inflated].opcode == Opcode.MOVE_RESULT_OBJECT)
        val rootRegister = (createInstructions[inflated] as OneRegisterInstruction).registerA
        create.addInstructions(inflated + 1,
            "invoke-static/range {v$rootRegister .. v$rootRegister}, Lapp/morphe/extension/instagram/InstagramStories;->prepare(Landroid/view/View;)V")
        val setup = mutableClassDefBy(viewer).methods.single {
            it.name == "A0t" && it.parameterTypes.getOrNull(3) == viewer
        }
        val instructions = setup.implementation!!.instructions.toList()
        val initializers = instructions.mapIndexedNotNull { index, instruction ->
            val next = instructions.getOrNull(index + 1) as? ReferenceInstruction
            val after = instructions.getOrNull(index + 2) as? ReferenceInstruction
            val firstField = next?.reference as? FieldReference
            val secondField = after?.reference as? FieldReference
            if ((instruction as? NarrowLiteralInstruction)?.narrowLiteral == -16777216 &&
                firstField?.definingClass == "LX/AG9;" && firstField.name == "A00" &&
                secondField?.definingClass == "LX/AG9;" && secondField.name == "A01")
                index to (instruction as OneRegisterInstruction).registerA else null
        }
        require(initializers.size == 1) { "Instagram story background initializer changed" }
        val (index, register) = initializers.single()
        setup.replaceInstruction(index,
            "invoke-virtual/range {p3 .. p3}, Landroidx/fragment/app/Fragment;->requireContext()Landroid/content/Context;")
        setup.addInstructions(index + 1,
            "move-result-object v$register\ninvoke-static/range {v$register .. v$register}, Lapp/morphe/extension/instagram/InstagramStories;->background(Landroid/content/Context;)I\nmove-result v$register")
        println("Instagram: story transition starts with the selected theme background")
        for ((holder, mode) in listOf("LX/A3u;" to "light", "LX/3gH;" to "dark")) {
            val init = mutableClassDefBy(holder).methods.single { it.name == "<clinit>" }
            val targets = init.implementation!!.instructions.mapIndexedNotNull { index, instruction ->
                val field = (instruction as? ReferenceInstruction)?.reference as? FieldReference
                if (instruction.opcode == Opcode.SGET_WIDE && field?.definingClass in setOf(
                        "Lcom/instagram/compose/core/theme/BasePrismColors;",
                        "Lcom/instagram/compose/core/theme/BasePrismColorsV2;"))
                    index to (instruction as OneRegisterInstruction).registerA else null
            }
            require(targets.size >= 50) { "Instagram $mode Compose palette changed" }
            for ((index, reg) in targets.asReversed()) {
                init.addInstructions(index + 1,
                    "invoke-static/range {v$reg .. v${reg + 1}}, Lapp/morphe/extension/instagram/InstagramColors;->$mode(J)J\nmove-result-wide v$reg")
            }
            println("Instagram: mapped ${targets.size} $mode Compose palette inputs")
        }
    }
}

val instagramCustomFontPatch = resourcePatch(
    name = "Instagram custom font",
    description = "Replace bundled interface fonts with a chosen TTF. Preserve icon fonts and wordmarks.",
    default = false,
) {
    compatibleWith(instagram)
    val fontFile = filePathOption(key = "font-file", default = null, title = "Custom TTF font",
        description = "Choose a TTF for Instagram's interface.", required = true, allowedExtensions = listOf("ttf"))
    execute {
        val bytes = readTrueType(requireNotNull(fontFile.value) { "Choose a TTF font file." })
        val names = setOf("IGUIBetav9_Bold.ttf", "IGUIBetav9_Regular.ttf", "IGUIBetav9_SemiBold.ttf",
            "Optimistic_Display_A_Bd.ttf", "Optimistic_Display_A_Md.ttf", "roboto_medium_numbers.ttf")
        var count = 0
        get("res/font").walkTopDown().filter { it.isFile && it.name in names }.forEach {
            it.writeBytes(bytes); count++
        }
        val variable = get("assets/Optimistic_VF_App_Lite.ttf")
        require(variable.isFile && count >= 3) { "Instagram bundled interface font layout changed" }
        variable.writeBytes(bytes)
        println("Instagram: replaced ${count + 1} interface fonts; icon fonts and wordmarks preserved")
    }
}
