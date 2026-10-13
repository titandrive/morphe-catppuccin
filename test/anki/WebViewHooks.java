// SPDX-License-Identifier: GPL-3.0-or-later
import java.io.File;
import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.iface.instruction.*;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;
public final class WebViewHooks {
 public static void main(String[] args)throws Exception {
  var container=DexFileFactory.loadDexContainer(new File(args[0]),Opcodes.getDefault());int overrides=0,hooks=0;
  for(String entry:container.getDexEntryNames())for(ClassDef klass:container.getEntry(entry).getDexFile().getClasses()) {
   if(klass.getType().startsWith("Lapp/morphe/extension/"))continue;
   for(Method method:klass.getMethods()) {
    if(method.getImplementation()==null)continue;
    boolean override=method.getName().equals("loadDataWithBaseURL");boolean preservedSuper=false;
    for(Instruction instruction:method.getImplementation().getInstructions()) {
     if(!(instruction instanceof ReferenceInstruction))continue;
     var ref=((ReferenceInstruction)instruction).getReference();if(!(ref instanceof MethodReference))continue;
     var call=(MethodReference)ref;
     if(call.getDefiningClass().equals("Lapp/morphe/extension/anki/AnkiTheme;")&&call.getName().equals("load")) {
      hooks++;if(override)throw new AssertionError("Recursive virtual load hook inside WebView override: "+klass.getType());
     }
     if(override&&call.getDefiningClass().equals("Landroid/webkit/WebView;")&&call.getName().equals("loadDataWithBaseURL")&&instruction.getOpcode().name().startsWith("INVOKE_SUPER"))preservedSuper=true;
    }
    if(override){if(!preservedSuper)throw new AssertionError("Superclass WebView load missing: "+klass.getType());overrides++;}
   }
  }
  if(overrides<1||hooks<2)throw new AssertionError("Missing reviewer override or HTML hooks");
  System.out.println("Superclass loads preserved in "+overrides+" WebView override; "+hooks+" outer HTML loads patched without recursion.");
 }
}
