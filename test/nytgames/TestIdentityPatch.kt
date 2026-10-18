// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.nytgames.test
import app.morphe.patcher.patch.*
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.w3c.dom.Element

val nytGamesTestIdentityPatch = bytecodePatch(name="NYT Games isolated test identity", default=false) {
    compatibleWith(Compatibility(name="NYT Games",packageName="com.nytimes.crossword"))
    val renames = mutableMapOf("com.nytimes.crossword" to "com.nytimes.crossword.catppuccin.test")
    dependsOn(resourcePatch {
        execute {
            document("AndroidManifest.xml").use { xml ->
                xml.documentElement.setAttribute("package","com.nytimes.crossword.catppuccin.test")
                val permissionNames = mutableMapOf<String, String>()
                val declared = xml.getElementsByTagName("permission")
                for (i in 0 until declared.length) {
                    val node = declared.item(i) as Element
                    val old = node.getAttribute("android:name")
                    permissionNames[old] = "com.nytimes.crossword.catppuccin.test.permission.p$i"
                    renames[old] = permissionNames[old]!!
                }
                val providers = xml.getElementsByTagName("provider")
                for (i in 0 until providers.length) {
                    val node = providers.item(i) as Element
                    if (node.hasAttribute("android:authorities")) node.setAttribute("android:authorities",
                        node.getAttribute("android:authorities").split(';').joinToString(";") { old ->
                            val changed = if (old.startsWith("com.nytimes.crossword")) old.replace("com.nytimes.crossword", "com.nytimes.crossword.catppuccin.test") else "$old.catppuccin.test"
                            renames[old] = changed
                            changed
                        })
                }
                val nodes=xml.getElementsByTagName("*")
                for(i in 0 until nodes.length) {
                    val node=nodes.item(i) as Element
                    for (name in listOf("android:permission", "android:readPermission", "android:writePermission")) {
                        if (node.hasAttribute(name)) permissionNames[node.getAttribute(name)]?.let { node.setAttribute(name, it) }
                    }
                    if (node.tagName in listOf("permission", "uses-permission")) {
                        permissionNames[node.getAttribute("android:name")]?.let { node.setAttribute("android:name", it) }
                    }
                }
                (xml.getElementsByTagName("application").item(0) as Element).apply {
                    setAttribute("android:label","NYT Games Theme Test")
                    setAttribute("android:debuggable","true")
                }
            }

        }
    })
    execute {
        val classes=mutableListOf<ClassDef>();classDefForEach {classes.add(it)}
        classes.forEach { klass -> klass.methods.forEach { method ->
            val changes=method.implementation?.instructions?.mapIndexedNotNull { index, instruction ->
                val value=((instruction as? ReferenceInstruction)?.reference as? StringReference)?.string
                if(value in renames) Triple(index, (instruction as OneRegisterInstruction).registerA, renames[value]!!) else null
            }.orEmpty()
            if(changes.isNotEmpty()) {
                val mutable=mutableClassDefBy(klass).methods.single {it.name==method.name&&it.parameterTypes==method.parameterTypes&&it.returnType==method.returnType}
                changes.forEach {(index,reg,value)->mutable.replaceInstruction(index,"const-string v$reg, \"$value\"")}
            }
        }}
    }
}
