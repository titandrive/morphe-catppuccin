import java.io.File;
import com.android.tools.smali.dexlib2.DexFileFactory;
import com.android.tools.smali.dexlib2.Opcodes;
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction;
import com.android.tools.smali.dexlib2.iface.reference.MethodReference;

public final class VerifyStoryHook {
    public static void main(String[] args) throws Exception {
        var container = DexFileFactory.loadDexContainer(new File(args[0]), Opcodes.getDefault());
        int calls = 0;
        int preparations = 0;
        boolean helper = false;
        for (String entry : container.getDexEntryNames()) {
            for (var type : container.getEntry(entry).getDexFile().getClasses()) {
                if (type.getType().equals("Lapp/morphe/extension/instagram/InstagramStories;")) helper = true;
                if (!type.getType().equals("Linstagram/features/stories/fragment/ReelViewerFragment;")) continue;
                for (var method : type.getMethods()) {
                    if (method.getImplementation() == null) continue;
                    for (var instruction : method.getImplementation().getInstructions()) {
                        if (instruction instanceof ReferenceInstruction) {
                            var reference = ((ReferenceInstruction) instruction).getReference();
                            if (reference instanceof MethodReference) {
                                var target = (MethodReference) reference;
                                if (target.getDefiningClass().equals("Lapp/morphe/extension/instagram/InstagramStories;")
                                        && target.getName().equals("background") && method.getName().equals("A0t")) calls++;
                                if (target.getDefiningClass().equals("Lapp/morphe/extension/instagram/InstagramStories;")
                                        && target.getName().equals("prepare") && method.getName().equals("onCreateView")) preparations++;
                            }
                        }
                    }
                }
            }
        }
        if (!helper || calls != 1 || preparations != 1) throw new AssertionError("Missing or duplicated story hook: " + calls);
        System.out.println("Story root preparation and transition initializer are both hooked exactly once");
    }
}
