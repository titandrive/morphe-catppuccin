package app.morphe.extension.nytgames;
import java.lang.reflect.*;
import java.util.IdentityHashMap;

/** Separate immutable Macchiato palettes; stock light objects are never mutated. */
public final class NytTheme {
 private static final IdentityHashMap<Object,Object> cache = new IdentityHashMap<>();
 private static long color(long original, int rgb) {
  // sRGB Compose colors use ARGB in the upper 32 bits. Retain semantic alpha.
  return (((original >>> 32) & 0xff000000L) | (rgb & 0xffffffL)) << 32;
 }
 public static Object core(Object original) {
  try {
   int[] rgb = CORE;
   Object[] args = new Object[rgb.length];
   Class<?>[] types = new Class<?>[rgb.length];
   for(int i=0;i<rgb.length;i++) {
    Field f=original.getClass().getDeclaredField(String.valueOf((char)('a'+i)));f.setAccessible(true);
    args[i]=color(f.getLong(original),rgb[i]);types[i]=long.class;
   }
   return original.getClass().getConstructor(types).newInstance(args);
  } catch(Exception e) { android.util.Log.e("CatppuccinNYT","Core palette failed",e);return original; }
 }
 private static Object consume(Object composer, Object local) throws Exception {
  for(Method m:composer.getClass().getMethods()) {
   if(m.getName().equals("k") && m.getParameterTypes().length==1
       && m.getParameterTypes()[0].getName().startsWith("androidx.compose.runtime.")
       && m.getParameterTypes()[0].isInstance(local)) return m.invoke(composer,local);
  }
  throw new NoSuchMethodException("Compose palette lookup changed");
 }
 public static Object extended(Object composer) {
  try {
   Class<?> holder=Class.forName("com.nytimes.games.designsystem.theme.ExtendedColorsKt");
   Object original=consume(composer,holder.getField("d").get(null));
   Object current=consume(composer,holder.getField("c").get(null));
   if(current != holder.getField("b").get(null)) return original;
   synchronized(cache) { return clonePalette(original); }
  } catch(Exception e) { throw new IllegalStateException("NYT palette lookup failed",e); }
 }
 private static Object clonePalette(Object original) throws Exception {
  Object found=cache.get(original);if(found!=null)return found;
  Class<?> type=original.getClass();
  Object copy;
  if(type.getSimpleName().equals("ExtendedColors")) {
   Class<?>[] types=new Class<?>[7];Object[] args=new Object[7];
   for(int i=0;i<7;i++) {
    Field f=type.getField(String.valueOf((char)('a'+i)));types[i]=f.getType();args[i]=clonePalette(f.get(original));
   }
   copy=type.getConstructor(types).newInstance(args);
  } else {
   copy=type.getConstructor().newInstance();
   String[] names=names(type.getSimpleName());int[] rgb=palette(type.getSimpleName());
   for(int i=0;i<names.length;i++) {
    Field f=type.getDeclaredField(names[i]);f.setAccessible(true);
    if(f.getType()==long.class) f.setLong(copy,color(f.getLong(original),rgb[i]));
    else f.set(copy,f.get(original));
   }
  }
  cache.put(original,copy);return copy;
 }
 private static final int[] CORE = new int[]{0x24273a,0x24273a,0x1e2030,0xcad3f5,0xc6a0f6,0xa5adcb,0x8087a2,0x6e738d,0xb8c0e0,0x24273a,0xc6a0f6,0x494d64,0xb7bdf8,0x24273a,0x24273a,0x6e738d,0x494d64,0x363a4f,0x181926,0x363a4f,0xeed49f,0x363a4f,0xc6a0f6,0x24273a};
 private static String[] names(String type) {switch(type){case "ExtendedArchiveColors": return new String[]{"a","b","c","d","e","f","g","h"};case "ExtendedBonusHubColors": return new String[]{"a","b","c","d","e"};case "ExtendedComponentColors": return new String[]{"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};case "ExtendedFriendsTabColors": return new String[]{"a","b","c","d","e","f","g"};case "ExtendedGameColors": return new String[]{"a","b","c","d","e","f","g","h","i","j","k","l"};case "ExtendedSponsoredCollectionColors": return new String[]{"a","b","c"};case "ExtendedTextColors": return new String[]{"a","b","c","d","e"};default:throw new IllegalArgumentException(type);}}
 private static int[] palette(String type) {switch(type){case "ExtendedArchiveColors": return new int[]{0xcad3f5,0x494d64,0x494d64,0x363a4f,0x6e738d,0xc6a0f6,0x494d64,0x24273a};case "ExtendedBonusHubColors": return new int[]{0xa6da95,0x8aadf4,0xc6a0f6,0x363a4f,0x494d64};case "ExtendedComponentColors": return new int[]{0x24273a,0x363a4f,0xc6a0f6,0x24273a,0xc6a0f6,0x24273a,0x494d64,0xcad3f5,0x6e738d,0x1e2030,0,0xc6a0f6,0x181926,0x494d64,0x363a4f,0xcad3f5,0x8087a2,0xed8796,0xeed49f,0x494d64,0x8aadf4,0x494d64,0x1e2030,0x24273a,0x24273a,0x494d64};case "ExtendedFriendsTabColors": return new int[]{0xc6a0f6,0xcad3f5,0xc6a0f6,0xb7bdf8,0x494d64,0xc6a0f6,0x8aadf4};case "ExtendedGameColors": return new int[]{0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f,0x363a4f};case "ExtendedSponsoredCollectionColors": return new int[]{0x363a4f,0x494d64,0x5b6078};case "ExtendedTextColors": return new int[]{0xcad3f5,0xcad3f5,0xcad3f5,0x24273a,0xa5adcb};default:throw new IllegalArgumentException(type);}}
}
