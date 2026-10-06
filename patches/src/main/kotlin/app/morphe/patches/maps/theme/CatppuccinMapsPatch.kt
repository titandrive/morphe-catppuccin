package app.morphe.patches.maps.theme

import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstruction
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.Opcode
import org.w3c.dom.Element

private const val BASE = "#ff24273a"
private const val MANTLE = "#ff1e2030"
private const val SURFACE = "#ff363a4f"
private const val SURFACE_HIGH = "#ff494d64"
private const val TEXT = "#ffcad3f5"
private const val SUBTEXT = "#ffb8c0e0"
private const val MAUVE = "#ffc6a0f6"
private const val BLUE = "#ff8aadf4"
private const val GREEN = "#ffa6da95"
private const val RED = "#ffed8796"
private const val YELLOW = "#ffeed49f"

// Map semantic UI tokens to each palette; map tiles use a separate renderer.
private fun themedRole(role: String, dark: Boolean): String? {
    val BASE = if (dark) "#ff24273a" else "#ffeff1f5"
    val MANTLE = if (dark) "#ff1e2030" else "#ffe6e9ef"
    val SURFACE = if (dark) "#ff363a4f" else "#ffccd0da"
    val SURFACE_HIGH = if (dark) "#ff494d64" else "#ffbcc0cc"
    val TEXT = if (dark) "#ffcad3f5" else "#ff4c4f69"
    val SUBTEXT = if (dark) "#ffb8c0e0" else "#ff5c5f77"
    val MAUVE = if (dark) "#ffc6a0f6" else "#ff8839ef"
    val BLUE = if (dark) "#ff8aadf4" else "#ff1e66f5"
    val GREEN = if (dark) "#ffa6da95" else "#ff40a02b"
    val RED = if (dark) "#ffed8796" else "#ffd20f39"
    val YELLOW = if (dark) "#ffeed49f" else "#ffdf8e1d"
    return when {
    role == "transparent_surface" || role == "outline_transparent" -> "#00000000"
    role == "surface_tint" -> MAUVE
    role == "shadow" -> "#99000000"
    role.startsWith("on_primary_container") -> BASE
    role == "primary_container" || role == "constant_dark_primary" -> MAUVE
    role == "constant_dark" || role == "constant_dark_immersive" || role == "immersive" || role == "surface_immersive" -> BASE
    role == "constant_dark_outline" -> SURFACE_HIGH
    role == "constant_dark_selected_container" || role == "constant_dark_surface1" -> SURFACE
    role == "inverse_surface" || role == "surface_inverse" -> TEXT
    role == "on_inverse_surface" || role == "inverse_on_surface" || role == "on_surface_inverse" -> BASE
    role.startsWith("on_") && role.endsWith("container") -> TEXT
    role.startsWith("on_selected_container") -> BASE
    role in listOf("on_primary", "on_secondary", "on_tertiary", "on_error", "on_negative", "on_positive", "on_info") -> BASE
    role.contains("on_disabled") -> if (dark) "#ff8087a2" else "#ff8c8fa1"
    role.startsWith("on_surface_variant") || role == "on_neutral_container_variant" -> SUBTEXT
    role.startsWith("on_") -> TEXT
    role.contains("outline_decorative") || role == "outline_variant" || role == "outline_disabled" -> SURFACE_HIGH
    role.contains("outline") -> if (dark) "#ff6e738d" else "#ff9ca0b0"
    role == "selected_container" -> MAUVE
    role == "surface_container" || role == "surface_container_lowest" -> MANTLE
    role == "background" || role == "surface" || role == "surface_dim" || role == "surface_container_low" -> BASE
    role == "surface_container_highest" || role == "surface_container_bold" || role == "surface_bright" || role in listOf("surface4", "surface5") -> SURFACE_HIGH
    role.contains("surface") || role.contains("container") -> SURFACE
    role.contains("caution") || role.contains("starred") -> YELLOW
    role.contains("error") || role.contains("negative") || role.contains("favorite") -> RED
    role.contains("positive") || role.contains("want_to_go") -> GREEN
    role.contains("info") || role.contains("saved_custom") || role.contains("saved_default") -> BLUE
    role.contains("real_time") || role.contains("tertiary") -> if (dark) "#fff5bde6" else "#ffea76cb"
    role.contains("local_guides") -> if (dark) "#fff5a97f" else "#fffe640b"
    role.contains("secondary") -> if (dark) "#ffb7bdf8" else "#ff7287fd"
    role.contains("primary") || role == "elevation_overlay" -> MAUVE
    else -> null
    }
}

private val mapsResources = resourcePatch {
    execute {
        val legacy = mapOf(
            "background_night_grey" to SURFACE, "night_grey900" to BASE,
            "divider_night_grey" to SURFACE_HIGH, "divider_grey_nightmode" to SURFACE_HIGH,
            "color_surface_elevation_plus_two_dark" to SURFACE,
            "callout_nightmode_background" to SURFACE,
            "car_card_night" to BASE,
            "background_material_dark" to BASE, "background_floating_material_dark" to SURFACE,
            "cardview_dark_background" to SURFACE,
            "directions_greentraffic_nightmode_text" to GREEN,
            "directions_redtraffic_nightmode_text" to RED,
            "directions_yellowtraffic_nightmode_text" to YELLOW,
            "directions_unknowntraffic_nightmode_text" to TEXT,
            "ask_maps_suggestion_icon_background_night" to MAUVE,
            "ask_maps_suggestion_icon_color_night" to BASE,
            "ask_maps_suggestion_sub_description_night" to MAUVE,
        )
        val nightLiterals = mapOf(
            "#000" to BASE, "#ff000000" to BASE, "#fff" to TEXT, "#ffffffff" to TEXT,
            "#ff131314" to BASE, "#202124" to BASE, "#3c4043" to SURFACE,
            "#ffe3e3e3" to TEXT, "#e8eaed" to TEXT, "#ffababab" to SUBTEXT,
            "#ff303030" to SURFACE, "#ff333537" to SURFACE_HIGH,
            "#5f6368" to "#ff8087a2",
        )
        val dayLegacy = mapOf(
            "background_grey" to "#ffeff1f5", "background_daynight_grey" to "#ffeff1f5",
            "daynight_page_background" to "#ffeff1f5", "divider_grey" to "#ff9ca0b0",
            "divider_daynight_grey" to "#ff9ca0b0", "daynight_black" to "#ff4c4f69",
            "daynight_white" to "#ffeff1f5", "daynight_white_with_elevation_3" to "#ffe6e9ef",
            "cf_surface" to "#ffeff1f5", "cf_bar_surface" to "#ffe6e9ef", "cf_divider" to "#ff9ca0b0",
            "cf_primary" to "#ff8839ef", "cf_text_primary" to "#ff4c4f69", "cf_text_secondary" to "#ff5c5f77",
        )
        // Legacy view widgets refer directly to Google neutrals instead of semantic tokens.
        val neutralTones = mapOf(
            "white" to Pair("#ffeff1f5", "#ffcad3f5"),
            "black" to Pair("#ff4c4f69", "#ff24273a"),
            "grey50" to Pair("#ffeff1f5", "#ffcad3f5"),
            "grey100" to Pair("#ffe6e9ef", "#ffb8c0e0"),
            "grey200" to Pair("#ffdce0e8", "#ffa5adcb"),
            "grey300" to Pair("#ffccd0da", "#ff939ab7"),
            "grey400" to Pair("#ffbcc0cc", "#ff8087a2"),
            "grey500" to Pair("#ff8c8fa1", "#ff6e738d"),
            "grey600" to Pair("#ff6c6f85", "#ff5b6078"),
            "grey700" to Pair("#ff5c5f77", "#ff494d64"),
            "grey800" to Pair("#ff4c4f69", "#ff363a4f"),
            "grey900" to Pair("#ff4c4f69", "#ff24273a"),
        )
        val existingNeutrals = mutableSetOf<String>()
        document("res/values/colors.xml").use { xml ->
            val nodes = xml.getElementsByTagName("color")
            for (index in 0 until nodes.length) {
                val color = nodes.item(index) as Element
                val name = color.getAttribute("name")
                val tone = when {
                    name.startsWith("mod_google_") -> name.removePrefix("mod_google_")
                    name.startsWith("google_") -> name.removePrefix("google_")
                    else -> null
                }
                neutralTones[tone]?.let { palette ->
                    color.textContent = palette.first
                    existingNeutrals.add(name)
                }
            }
        }
        document("res/values-night/colors.xml").use { xml ->
            for (name in existingNeutrals) {
                val tone = name.removePrefix("mod_google_").removePrefix("google_")
                val color = xml.createElement("color")
                color.setAttribute("name", name)
                color.textContent = neutralTones.getValue(tone).second
                xml.documentElement.appendChild(color)
            }
        }
        var changed = 0
        for (directory in listOf("values", "values-night")) {
            document("res/$directory/colors.xml").use { xml ->
                val nodes = xml.getElementsByTagName("color")
                for (index in 0 until nodes.length) {
                    val color = nodes.item(index) as Element
                    val name = color.getAttribute("name")
                    val dark = name.contains("_dynamic_dark_") || directory == "values-night" || name.startsWith("gm3_sys_color_dark_") || name.startsWith("gm3_dark_") || name.startsWith("gm_sys_color_dark_") || name.startsWith("google_dark_") || name.startsWith("design_dark_") || name.startsWith("m3_sys_color_dark_") || name.startsWith("gm3_legacy_sys_color_dark_")
                    val role = when {
                        name.startsWith("gm3_sys_color_dynamic_dark_") -> name.removePrefix("gm3_sys_color_dynamic_dark_")
                        name.startsWith("gm3_sys_color_dynamic_light_") -> name.removePrefix("gm3_sys_color_dynamic_light_")
                        name.startsWith("gm_sys_color_dynamic_dark_") -> name.removePrefix("gm_sys_color_dynamic_dark_")
                        name.startsWith("gm_sys_color_dynamic_light_") -> name.removePrefix("gm_sys_color_dynamic_light_")
                        name.startsWith("m3_sys_color_dynamic_dark_") -> name.removePrefix("m3_sys_color_dynamic_dark_")
                        name.startsWith("m3_sys_color_dynamic_light_") -> name.removePrefix("m3_sys_color_dynamic_light_")
                        name.startsWith("gm3_dynamic_color_dark_") -> name.removePrefix("gm3_dynamic_color_dark_")
                        name.startsWith("gm3_dynamic_color_light_") -> name.removePrefix("gm3_dynamic_color_light_")
                        name.startsWith("m3_sys_color_light_") -> name.removePrefix("m3_sys_color_light_")
                        name.startsWith("m3_sys_color_dark_") -> name.removePrefix("m3_sys_color_dark_")
                        name.startsWith("gm3_legacy_sys_color_dark_") -> name.removePrefix("gm3_legacy_sys_color_dark_")
                        name.startsWith("gm_sys_color_light_") -> name.removePrefix("gm_sys_color_light_")
                        name.startsWith("gm_sys_color_dark_") -> name.removePrefix("gm_sys_color_dark_")
                        name.startsWith("google_dark_default_color_") -> name.removePrefix("google_dark_default_color_")
                        name.startsWith("google_default_color_") -> name.removePrefix("google_default_color_")
                        name.startsWith("design_dark_default_color_") -> name.removePrefix("design_dark_default_color_")
                        name.startsWith("design_default_color_") -> name.removePrefix("design_default_color_")
                        name.startsWith("geo_sys_color_aqua_") -> name.removePrefix("geo_sys_color_aqua_")
                        name.startsWith("geo_sys_color_") -> name.removePrefix("geo_sys_color_")
                        name.startsWith("gm3_sys_color_light_") -> name.removePrefix("gm3_sys_color_light_")
                        name.startsWith("gm3_default_color_") -> name.removePrefix("gm3_default_color_")
                        name.startsWith("gm3_sys_color_dark_") -> name.removePrefix("gm3_sys_color_dark_")
                        name.startsWith("gm3_dark_default_color_") -> name.removePrefix("gm3_dark_default_color_")
                        name.startsWith("gm3_dark_legacy_color_") -> name.removePrefix("gm3_dark_legacy_color_")
                        else -> null
                    }
                    val replacement = (if (directory == "values") dayLegacy[name] else null) ?: legacy[name] ?: role?.let { themedRole(it, dark) }
                        ?: if (directory == "values-night") nightLiterals[color.textContent.trim().lowercase()] else null
                    if (replacement != null) {
                        color.textContent = replacement; changed++
                    }
                }
            }
        }
        require(changed >= 150) { "This Maps APK has a different dark palette; refusing to apply an incomplete theme." }
    }
}
@Suppress("unused")
val catppuccinMapsPatch = bytecodePatch(
    name = "Google Maps Catppuccin theme",
    description = "Replaces Google Maps' light interface with Catppuccin Latte and dark interface with Macchiato. Map tiles retain their original styling.",
    default = false,
) {
    compatibleWith(Compatibility(name = "Google Maps", packageName = "com.google.android.apps.maps",
        targets = listOf(AppTarget(version = "26.39.06.984891338"))))
    dependsOn(mapsResources)
    extendWith("catppuccin/maps.dex")
    execute {
        val resume = mutableClassDefBy("Lnbm;").methods.single { it.name == "onResume" }
        resume.addInstruction(0,
            "invoke-static/range {p0 .. p0}, Lapp/morphe/extension/maps/MapsTheme;->attach(Landroid/app/Activity;)V")
        // Maps clears its GL canvas separately from Android theme backgrounds.
        val renderer = mutableClassDefBy("Lbkxw;")
        var clearCalls = 0
        renderer.methods.forEach { method ->
            method.implementation?.instructions?.forEachIndexed { index, instruction ->
                if (instruction is com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction &&
                    instruction.reference.toString() == "Landroid/opengl/GLES20;->glClearColor(FFFF)V") {
                    val call = instruction as com.android.tools.smali.dexlib2.iface.instruction.FiveRegisterInstruction
                    method.replaceInstruction(index, "invoke-static {v${call.registerC}, v${call.registerD}, v${call.registerE}, v${call.registerF}}, Lapp/morphe/extension/maps/MapsTheme;->clearMapColor(FFFF)V")
                    clearCalls++
                }
            }
        }
        require(clearCalls == 1) { "Maps' renderer clear-color call has changed." }

        // Compose's fallback Material scheme supplies backgrounds in several cards.
        val material = mutableClassDefBy("Lblru;").methods.single { it.name == "P" }
        val constructorIndex = material.implementation!!.instructions.indexOfFirst {
            it is com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction &&
                it.reference.toString().startsWith("Ldkf;-><init>(")
        }
        require(constructorIndex >= 0) { "Maps' Material fallback scheme has changed." }
        val materialRoles = "primary on_primary primary_container on_primary_container inverse_primary secondary on_secondary secondary_container on_secondary_container tertiary on_tertiary tertiary_container on_tertiary_container background on_background surface on_surface surface_variant on_surface_variant surface_tint inverse_surface inverse_on_surface error on_error error_container on_error_container outline outline_variant".split(" ")
        val scheme = materialRoles.mapIndexed { index, role ->
            val argb = themedRole(role, true)!!.removePrefix("#").toULong(16)
            "const-wide v${4 + index * 2}, 0x${(argb shl 32).toString(16)}L"
        }.joinToString("\n")
        material.addInstructions(constructorIndex, scheme)
        // The UI scaffold overrides four palette surfaces with literal colors.
        // Keep replacements inside that scaffold, including its existing light/dark branches.
        val scaffold = mutableClassDefBy("Ldat;").methods.single { it.name == "a" }
        val surfaces = mapOf(
            0xff393939.toInt() to 0xff363a4f.toInt(),
            0xff131314.toInt() to 0xff24273a.toInt(),
            0xffffffff.toInt() to 0xffeff1f5.toInt(),
            0xffd3dbe5.toInt() to 0xffccd0da.toInt(),
        )
        var scaffoldChanges = 0
        scaffold.implementation!!.instructions.toList().forEachIndexed { index, instruction ->
            if (instruction.opcode in setOf(Opcode.CONST, Opcode.CONST_4, Opcode.CONST_16, Opcode.CONST_HIGH16)) {
                val replacement = surfaces[(instruction as NarrowLiteralInstruction).narrowLiteral]
                if (replacement != null) {
                    val register = (instruction as OneRegisterInstruction).registerA
                    scaffold.replaceInstruction(index, "const v$register, 0x${replacement.toUInt().toString(16)}")
                    scaffoldChanges++
                }
            }
        }
        require(scaffoldChanges == 4) { "Maps' UI scaffold has changed; refusing an ambiguous surface replacement." }
        // Use each generated field's semantic role. Equal stock literals are reused
        // for backgrounds and foregrounds, so a literal-to-literal map loses contrast.
        val roles = """
            caution caution_container caution_container_variant caution_variant constant_dark constant_dark_immersive
            constant_dark_outline constant_dark_primary constant_dark_selected_container constant_dark_surface1 disabled_surface immersive
            info info_container info_container_variant inverse_primary inverse_surface local_guides
            local_guides_variant negative negative_container negative_container_variant neutral_container neutral_container_variant
            on_caution on_caution_container on_constant_dark on_constant_dark_immersive on_constant_dark_selected_container on_constant_dark_variant
            on_disabled_surface on_info on_info_container on_inverse_surface on_negative on_negative_container
            on_neutral_container on_neutral_container_variant on_positive on_positive_container on_primary_container on_secondary_container
            on_selected_container on_selected_container_favorites on_selected_container_starred on_selected_container_travel on_selected_container_want_to_go on_surface
            on_surface_immersive on_surface_tonal on_surface_variant on_tonal_surface outline_decorative outline_disabled
            outline_interactive outline_transparent positive positive_container primary primary_container
            real_time saved_custom_list saved_default saved_favorite saved_starred saved_want_to_go
            secondary_container secondary_container_variant selected_container selected_container_favorites selected_container_starred selected_container_travel
            selected_container_want_to_go surface1 surface_container_bold shadow surface5 star_rating
            surface3 surface2 surface4 surface_bright surface surface_container
            surface_container_lowest surface_container_low surface_dim surface_immersive tonal_surface transparent_surface
            surface_container_high surface_container_highest
        """.trim().split(Regex("\\s+"))
        for ((owner, dark) in mapOf("Lahxo;" to true, "Lahxq;" to true, "Lahxp;" to false, "Lahxr;" to false)) {
            val initializer = mutableClassDefBy(owner).methods.single { it.name == "<clinit>" }
            val writes = initializer.implementation!!.instructions.toList().withIndex()
                .filter { it.value.opcode == Opcode.SPUT_WIDE }
            require(writes.size == roles.size) { "Maps' generated color fields have changed: $owner" }
            for ((roleIndex, write) in writes.withIndex().toList().asReversed()) {
                val color = themedRole(roles[roleIndex], dark) ?: continue
                val argb = color.removePrefix("#").toULong(16)
                val register = (write.value as OneRegisterInstruction).registerA
                initializer.addInstruction(write.index, "const-wide v$register, 0x${(argb shl 32).toString(16)}L")
            }
        }
    }
}
