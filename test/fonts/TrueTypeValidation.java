// Run against build/catppuccin/classes and the cached Morphe desktop JAR.
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.ByteBuffer;
public class TrueTypeValidation {
    public static void main(String[] args) throws Exception {
        var read = Class.forName("app.morphe.patches.shared.fonts.TrueTypeKt")
            .getDeclaredMethod("readTrueType", String.class);
        byte[] font = (byte[]) read.invoke(null, args[0]);
        Path directory = Files.createTempDirectory("boost-font-validation");
        for (String scenario : new String[]{"empty", "signature", "directory", "bounds"}) {
            byte[] invalid = font.clone();
            if (scenario.equals("empty")) invalid = new byte[0];
            if (scenario.equals("signature")) invalid[0] = 1;
            if (scenario.equals("directory")) ByteBuffer.wrap(invalid).putShort(4, (short) 257);
            if (scenario.equals("bounds")) ByteBuffer.wrap(invalid).putInt(20, Integer.MAX_VALUE);
            Path file = directory.resolve(scenario + ".ttf");
            Files.write(file, invalid);
            try {
                read.invoke(null, file.toString());
                throw new AssertionError("Accepted invalid font: " + scenario);
            } catch (InvocationTargetException error) {
                if (!(error.getCause() instanceof IllegalArgumentException)) throw error;
            } finally { Files.delete(file); }
        }
        Files.delete(directory);
        System.out.println("Valid font accepted; invalid signatures, directories and table bounds rejected.");
    }
}
