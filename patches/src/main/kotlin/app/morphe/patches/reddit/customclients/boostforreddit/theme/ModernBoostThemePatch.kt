// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.reddit.customclients.boostforreddit.theme

import app.morphe.patcher.patch.ResourcePatchContext
import app.morphe.patcher.patch.BytecodePatchContext
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.NarrowLiteralInstruction
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

private object ModernBoostResources
private const val modernExt = "Lapp/morphe/extension/boost/BoostTheme;"

internal fun ResourcePatchContext.applyModernBoostResources() {
    fun template(name: String) = ModernBoostResources::class.java.classLoader
        .getResourceAsStream("catppuccin/$name")!!.use {
            DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(it)
        }
    // Keep native 1.14 resources intact: old restore.xml belongs only to 1.12.12.
    for ((path, source) in listOf("res/values/colors.xml" to template("palette.xml"),
        "res/values/styles.xml" to template("styles.xml"))) {
        document(path).use { xml ->
            val nodes = source.documentElement.childNodes
            val original = xml.documentElement.childNodes
            val names = (0 until original.length).mapNotNull {
                (original.item(it) as? Element)?.getAttribute("name")
            }.toMutableSet()
            for (i in 0 until nodes.length) {
                val node = nodes.item(i) as? Element ?: continue
                require(names.add(node.getAttribute("name"))) { "Use an original Boost APK; Catppuccin resources already exist." }
                xml.documentElement.appendChild(xml.importNode(node,true))
            }
        }
    }
    document("res/values/strings.xml").use { xml ->
        for (flavor in listOf("Latte", "Macchiato")) xml.documentElement.appendChild(xml.createElement("string").apply {
            setAttribute("name", "catppuccin_theme_${flavor.lowercase()}"); textContent="Catppuccin $flavor"
        })
    }
    document("res/values/public.xml").use { xml ->
        for ((type,name,id) in listOf(
            Triple("style","Catppuccin.Latte",0x7f141000), Triple("style","Catppuccin.Macchiato",0x7f141001),
            Triple("style","Catppuccin.Latte.Toolbar",0x7f141003), Triple("style","Catppuccin.Macchiato.Toolbar",0x7f141006),
            Triple("string","catppuccin_theme_latte",0x7f131000), Triple("string","catppuccin_theme_macchiato",0x7f131001))) {
            val value="0x${id.toString(16)}"
            val nodes=xml.documentElement.childNodes
            require((0 until nodes.length).none { (nodes.item(it) as? Element)?.getAttribute("id")==value }) {
                "Reserved Catppuccin resource ID is already in use: $value"
            }
            xml.documentElement.appendChild(xml.createElement("public").apply {
                setAttribute("type",type);setAttribute("name",name);setAttribute("id",value)
            })
        }
    }
    document("res/values/arrays.xml").use { xml ->
        val arrays=xml.documentElement.childNodes
        for (i in 0 until arrays.length) {
            val array=arrays.item(i) as? Element ?: continue
            if (!array.getAttribute("name").startsWith("pref_theme_values")) continue
            for (id in listOf("17","18")) array.appendChild(xml.createElement("item").apply { textContent=id })
        }
    }
}

internal fun BytecodePatchContext.applyModernBoostTheme() {
    val utils=mutableClassDefBy("Lk83;")
    val init=utils.methods.single { it.name=="<clinit>" }
    val code=StringBuilder()
    for ((field,type,values) in listOf(
        Triple("b","I",listOf("17","18")), Triple("c","I",listOf("0x7f141000","0x7f141001")),
        Triple("e","I",listOf("0x7f131000","0x7f131001")),
        Triple("d","Ljava/lang/String;",listOf("Catppuccin.Latte","Catppuccin.Macchiato")))) {
        val copy=if(type=="I") "I" else "Ljava/lang/Object;"
        code.append("sget-object v0, Lk83;->$field:[$type\nconst/16 v1, 19\ninvoke-static {v0, v1}, Ljava/util/Arrays;->copyOf([${copy}I)[$copy\nmove-result-object v0\ncheck-cast v0, [$type\n")
        values.forEachIndexed { i,value ->
            code.append("const/16 v1, ${17+i}\n")
            code.append(if(type=="I") "const v2, $value\naput v2, v0, v1\n" else "const-string v2, \"$value\"\naput-object v2, v0, v1\n")
        }
        code.append("sput-object v0, Lk83;->$field:[$type\n")
    }
    init.addInstructions(init.implementation!!.instructions.indexOfFirst { it.opcode==Opcode.RETURN_VOID },code.toString())
    val select=mutableClassDefBy("Lcom/rubenmayayo/reddit/ui/activities/a;").methods.single { it.name=="setCurrentTheme" }
    val limits=select.implementation!!.instructions.mapIndexedNotNull { i,ins ->
        if(ins.opcode==Opcode.CONST_16 && (ins as NarrowLiteralInstruction).narrowLiteral==17)
            i to (ins as OneRegisterInstruction).registerA else null
    }
    require(limits.size==1) { "Boost theme-selection loop changed" }
    for((i,reg) in limits) select.replaceInstruction(i,"const/16 v$reg, 19")
    mutableClassDefBy(modernExt).methods.single { it.name=="mode" }.addInstructions(0,"""
        invoke-static {}, Lps2;->n()Lps2;
        move-result-object v0
        invoke-virtual {v0}, Lps2;->R()I
        move-result v0
        return v0
    """)
    fun hook(type: String, name: String, helper: String, boolean: Boolean=false) {
        val method=mutableClassDefBy(type).methods.single { it.name==name }
        val returns=method.implementation!!.instructions.mapIndexedNotNull { i, ins ->
            if(ins.opcode==Opcode.RETURN) i to (ins as OneRegisterInstruction).registerA else null
        }
        require(returns.isNotEmpty()) { "Boost 1.14.2 method changed: $type->$name" }
        val valueType=if(boolean) "Z" else "I"
        for ((index,reg) in returns.reversed()) {
            method.replaceInstruction(index,"invoke-static/range {v$reg .. v$reg}, $modernExt->$helper($valueType)$valueType")
            method.addInstructions(index+1,"move-result v$reg\nreturn v$reg")
        }
    }
    for ((name,helper) in mapOf("c" to "accent","d" to "text","e" to "text","f" to "accent",
        "g" to "header","h" to "header","i" to "secondary","j" to "sticky","k" to "secondary",
        "l" to "title","n" to "secondary","o" to "text","p" to "secondary")) hook("Lk83;",name,helper)
    hook("Lk83;","t","material",true);hook("Lk83;","s","dynamic",true);hook("Lk83;","r","amoled",true)
    hook("Lps2;","V","toolbarMode")
    val index=utils.methods.single { it.name=="q" }
    val indexLimits=index.implementation!!.instructions.mapIndexedNotNull { i,ins ->
        if(ins.opcode==Opcode.CONST_16 && (ins as NarrowLiteralInstruction).narrowLiteral==17)
            i to (ins as OneRegisterInstruction).registerA else null
    }
    require(indexLimits.size==1) { "Boost theme-index loop changed" }
    for((i,reg) in indexLimits) index.replaceInstruction(i,"const/16 v$reg, 19")
    // Tint the new native Reply send icon; use the Menu parameter instead of a
    // resource ID from an older Boost release.
    val menu=mutableClassDefBy("Lcom/rubenmayayo/reddit/ui/compose/FormatActivity;").methods.single { it.name=="onCreateOptionsMenu" }
    val menuReturns=menu.implementation!!.instructions.mapIndexedNotNull { i,ins -> if(ins.opcode==Opcode.RETURN) i else null }
    for (i in menuReturns.reversed()) {
        val reg=(menu.implementation!!.instructions[i] as OneRegisterInstruction).registerA
        menu.replaceInstruction(i,"invoke-static {p1}, $modernExt->tintMenu(Landroid/view/Menu;)V")
        menu.addInstructions(i+1,"return v$reg")
    }
    val media=mutableClassDefBy("Lcom/rubenmayayo/reddit/ui/activities/MediaActivity;").methods.single { it.name=="onCreate" }
    val mediaReturns=media.implementation!!.instructions.mapIndexedNotNull { i,ins -> if(ins.opcode==Opcode.RETURN_VOID) i else null }
    for (i in mediaReturns.reversed()) {
        media.replaceInstruction(i,"invoke-static/range {p0 .. p0}, $modernExt->media(Landroid/app/Activity;)V")
        media.addInstructions(i+1,"return-void")
    }
}
