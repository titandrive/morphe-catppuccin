// SPDX-License-Identifier: GPL-3.0-or-later
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;
import com.android.tools.smali.dexlib2.*;
import com.android.tools.smali.dexlib2.iface.*;
import com.android.tools.smali.dexlib2.iface.instruction.*;
import com.android.tools.smali.dexlib2.iface.reference.*;
public final class ThemeHooks {
 public static void main(String[] args)throws Exception {
  Map<String,String> schema=new HashMap<>();
  Matcher rows=Pattern.compile("add\\(\"([^\"]+)\", \"([^\"]+)\"\\)").matcher(Files.readString(Path.of(args[1])));
  while(rows.find())schema.put(rows.group(1),rows.group(2));
  var container=DexFileFactory.loadDexContainer(new File(args[0]),Opcodes.getDefault());
  int init=0, hooks=0, returns=0, fields=0, navigation=0, catalog=0, guards=0, selection=0;
  for(String entry:container.getDexEntryNames())for(ClassDef klass:container.getEntry(entry).getDexFile().getClasses()) {
   for(Method method:klass.getMethods()) {
    if(method.getImplementation()==null)continue;
    boolean resolver=klass.getType().equals("Le77;")&&method.getReturnType().equals("Ld77;");
    for(Instruction instruction:method.getImplementation().getInstructions()) {
     if(resolver&&instruction.getOpcode()==Opcode.RETURN_OBJECT)returns++;
     if(!(instruction instanceof ReferenceInstruction))continue;
     var reference=((ReferenceInstruction)instruction).getReference();
     if(reference instanceof MethodReference) {
      var call=(MethodReference)reference;
      if(call.getDefiningClass().equals("Lapp/morphe/extension/todoist/TodoistTheme;")) {
       if(call.getName().equals("install"))init++;
       if(resolver&&call.getName().equals("customPalette"))hooks++;
       if(call.getName().equals("navigationIcon"))navigation++;
       if(call.getName().equals("selectedTheme"))selection++;
       if(call.getName().equals("nativeStyle")||call.getName().equals("previewStyle"))guards++;

      }
     }
     if(reference instanceof MethodReference) {
      var call=(MethodReference)reference;
      if(call.getDefiningClass().equals("Lapp/morphe/extension/todoist/TodoistIdentities;")) {
       if(call.getName().equals("append"))catalog++;
       if(call.getName().equals("fromPreference"))guards++;
      }
     }
     if(method.getName().equals("toString")&&klass.getType().startsWith("Ld77$")&&reference instanceof StringReference) {
      String text=((StringReference)reference).getString(); int bracket=text.indexOf('(');
      if(bracket<0||!schema.containsKey(text.substring(0,bracket)))continue;
      for(String pair:schema.get(text.substring(0,bracket)).split(" ")) {
       String field=pair.split(":")[0];boolean found=false;
       for(Field f:klass.getFields())if(f.getName().equals(field)&&f.getType().equals("J"))found=true;
       if(!found)throw new AssertionError("Color schema field missing: "+klass.getType()+"->"+field);
       fields++;
      }
     }
    }
   }
  }
  if(init!=1||returns<10||hooks!=1||fields!=413||navigation<6||catalog!=2||guards!=3||selection!=1)throw new AssertionError("Picker hook/schema mismatch: "+init+", "+returns+", "+hooks+", "+fields+", "+navigation+", "+catalog+", "+guards+", "+selection);
  System.out.println("Verified isolated palettes, catalog additions, native/Compose guards, persistence, conditional icons and "+fields+" semantic fields");
 }
}
