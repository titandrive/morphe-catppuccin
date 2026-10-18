// SPDX-License-Identifier: GPL-3.0-or-later
import java.io.File;
import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.iface.instruction.*;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
public final class FontHooks {
 public static void main(String[] args) throws Exception {
  var dex = DexFileFactory.loadDexContainer(new File(args[0]), Opcodes.getDefault());
  int init=0, hooks=0, weighted=0, nativeCalls=0;
  for(String entry:dex.getDexEntryNames()) for(ClassDef klass:dex.getEntry(entry).getDexFile().getClasses()) {
   boolean extension=klass.getType().equals("Lapp/morphe/extension/todoist/TodoistFont;");
   for(Method method:klass.getMethods()) {
    if(method.getImplementation()==null) continue;
    for(Instruction instruction:method.getImplementation().getInstructions()) {
     if(!(instruction instanceof ReferenceInstruction))continue;
     var ref=((ReferenceInstruction)instruction).getReference();
     if(!(ref instanceof MethodReference))continue;
     var call=(MethodReference)ref;
     if(call.getDefiningClass().equals("Lapp/morphe/extension/todoist/TodoistFont;")) {
      if(extension) {
       if(method.getName().equals("create")) throw new AssertionError("Recursive font hook: "+method);
       continue;
      }
      if(call.getName().equals("install")) {
       if(!klass.getType().equals("Lcom/todoist/App;")||!method.getName().equals("onCreate"))throw new AssertionError("Wrong initialization hook");
       init++;
      } else {hooks++; if(call.getParameterTypes().size()==3) weighted++;}
     }
     if(extension&&call.getDefiningClass().equals("Landroid/graphics/Typeface;")&&call.getName().equals("create"))nativeCalls++;
    }
   }
  }
  if(init!=1||hooks<10||weighted<1||nativeCalls<3)throw new AssertionError("Missing font hooks: "+init+", "+hooks+", "+weighted+", "+nativeCalls);
  System.out.println("Font initialization, "+hooks+" calls, weighted Compose fonts and nonrecursive extension verified");
 }
}
