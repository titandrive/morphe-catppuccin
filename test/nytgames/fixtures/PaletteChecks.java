import com.nytimes.games.designsystem.theme.*;
import app.morphe.extension.nytgames.NytTheme;
import androidx.compose.runtime.CompositionLocal;
public class PaletteChecks {
 public static class Composer {public Object k(CompositionLocal local){return local.value;}}
 public static void main(String[] args){
 CoreThemeColors original=new CoreThemeColors(0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L,0xffffffff00000000L);
 CoreThemeColors dark=(CoreThemeColors)NytTheme.core(original);
 if(dark.a!=0xff24273a00000000L || dark.d!=0xffcad3f500000000L || original.a!=0xffffffff00000000L)throw new AssertionError("Core palette or original mutated");
 ExtendedComponentColors component=new ExtendedComponentColors();
 ExtendedColors colors=new ExtendedColors(component,component,component,component,component,component,component);
 ExtendedColorsKt.b=dark;ExtendedColorsKt.c=new CompositionLocal(original);ExtendedColorsKt.d=new CompositionLocal(colors);
 Composer composer=new Composer();
 if(NytTheme.extended(composer)!=colors)throw new AssertionError("Stock light changed");
 ExtendedColorsKt.c.value=dark;
 ExtendedColors themed=(ExtendedColors)NytTheme.extended(composer);
 if(themed==colors || themed.a==component || component.a!=0xffcccccc00000000L)throw new AssertionError("Original nested palette changed");
 if(themed.a.a!=0xff24273a00000000L || themed.a.e!=0xffc6a0f600000000L || themed.a.f!=0xff24273a00000000L)throw new AssertionError("Background/button contrast incorrect");
 if(themed.a.k!=0.45f || NytTheme.extended(composer)!=themed)throw new AssertionError("Alpha or cache broken");
 System.out.println("Core/extended dark palettes, button contrast, unchanged light palette, immutable originals and caching passed");
 }}