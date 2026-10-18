// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.patches.todoist.test
import app.morphe.patcher.patch.*
import app.morphe.patcher.extensions.InstructionExtensions.replaceInstruction
import com.android.tools.smali.dexlib2.iface.ClassDef
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import org.w3c.dom.Element

val todoistTestIdentityPatch = bytecodePatch(name="Todoist isolated test identity", default=false) {
    compatibleWith(Compatibility(name="Todoist",packageName="com.todoist",targets=listOf(AppTarget(version="v12314"))))
    dependsOn(resourcePatch {
        execute {
            document("AndroidManifest.xml").use { xml ->
                xml.documentElement.setAttribute("package","com.todoist.catppuccin.test")
                val nodes=xml.getElementsByTagName("*")
                for(i in 0 until nodes.length) {
                    val node=nodes.item(i) as Element
                    for(name in listOf("android:authorities","android:permission","android:readPermission","android:writePermission")) {
                        if(node.hasAttribute(name)) node.setAttribute(name,node.getAttribute(name).replace("com.todoist", "com.todoist.catppuccin.test"))
                    }
                    if(node.tagName in listOf("permission","uses-permission") && node.getAttribute("android:name").startsWith("com.todoist.")) node.setAttribute("android:name",node.getAttribute("android:name").replace("com.todoist", "com.todoist.catppuccin.test"))
                }
                (xml.getElementsByTagName("application").item(0) as Element).apply {
                    setAttribute("android:label","Todoist Theme Test")
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
                if(value=="com.todoist") index to (instruction as OneRegisterInstruction).registerA else null
            }.orEmpty()
            if(changes.isNotEmpty()) {
                val mutable=mutableClassDefBy(klass).methods.single {it.name==method.name&&it.parameterTypes==method.parameterTypes&&it.returnType==method.returnType}
                changes.forEach {(index,reg)->mutable.replaceInstruction(index,"const-string v$reg, \"com.todoist.catppuccin.test\"")}
            }
        }}
    }
}
