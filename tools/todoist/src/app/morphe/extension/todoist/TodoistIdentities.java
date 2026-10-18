package app.morphe.extension.todoist;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

/** Adds local-only identities through Todoist's existing theme interface. */
public final class TodoistIdentities {
    public static final int LATTE = 1001, MACCHIATO = 1002;
    private static Object latte, macchiato;
    private static synchronized Object identity(final boolean light) throws ReflectiveOperationException {
        Object cached = light ? latte : macchiato;
        if (cached != null) return cached;
        Class<?> contract = Class.forName("gel");
        Object result = Proxy.newProxyInstance(contract.getClassLoader(), new Class<?>[]{contract}, (proxy, method, args) -> {
            switch (method.getName()) {
                case "getId": return light ? LATTE : MACCHIATO;
                case "getKey": return light ? "catppuccin_latte" : "catppuccin_macchiato";
                case "d": return TodoistTheme.resource(light ? "catppuccin_latte_title" : "catppuccin_macchiato_title", "string");
                case "a": return false; // No premium requirement.
                case "b": return light;
                case "c": return 1; // Native legacy fallback; custom overlays are resolved separately.
                case "e": return false; // These theme IDs are local-only, never sync to Todoist.
                case "f": return 0;
                case "equals": return proxy == args[0];
                case "hashCode": return light ? LATTE : MACCHIATO;
                case "toString": return light ? "Catppuccin Latte" : "Catppuccin Macchiato";
                default: throw new UnsupportedOperationException(method.toString());
            }
        });
        if (light) latte = result; else macchiato = result;
        return result;
    }
    public static List<?> append(List<?> original) {
        try {
            ArrayList<Object> result = new ArrayList<>(original);
            Object first = identity(true), second = identity(false);
            if (!result.contains(first)) result.add(Math.min(1, result.size()), first);
            if (!result.contains(second)) result.add(Math.min(2, result.size()), second);
            return result;
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Todoist theme interface changed", error); }
    }
    public static int id(Object candidate) {
        if (candidate == latte && latte != null) return LATTE;
        if (candidate == macchiato && macchiato != null) return MACCHIATO;
        return 0;
    }
    public static Object fromPreference(Object preference) {
        if (preference == null) return null;
        try {
            Object saved = preference.getClass().getField("a").get(preference);
            if (!(saved instanceof Integer)) return null;
            int id = ((Integer)saved).intValue();
            if (id != LATTE && id != MACCHIATO) return null;
            boolean light = id == LATTE;
            Class<?> gel = Class.forName("gel"), appearance = Class.forName("ydl");
            Object selected = identity(light);
            return Class.forName("wdl").getConstructor(gel, appearance, boolean.class, gel).newInstance(
                selected, appearance.getField(light ? "c" : "d").get(null), light, selected);
        } catch (ReflectiveOperationException error) { throw new IllegalStateException("Todoist theme preferences changed", error); }
    }
    public static Object appearance(Object identity) {
        try { return Class.forName("ydl").getField(id(identity) == LATTE ? "c" : "d").get(null); }
        catch (ReflectiveOperationException error) { throw new IllegalStateException(error); }
    }
    public static int themeId(Object theme) {
        if (theme == null) return 0;
        try { return id(theme.getClass().getField("a").get(theme)); }
        catch (ReflectiveOperationException error) { return 0; }
    }
}
