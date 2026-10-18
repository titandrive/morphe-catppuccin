import java.util.List;
import app.morphe.extension.todoist.TodoistIdentities;
public class IdentityTest {
 public static class Preference { public Integer a; Preference(Integer id) { a=id; } }
 public static void main(String[] args) {
  Object stock = new Object(); List<?> all = TodoistIdentities.append(List.of(stock));
  if(all.size()!=3||all.get(0)!=stock)throw new AssertionError("Stock catalog changed");
  gel latte=(gel)all.get(1), macchiato=(gel)all.get(2);
  if(!latte.toString().equals("Catppuccin Latte")||!macchiato.toString().equals("Catppuccin Macchiato"))throw new AssertionError("Labels");
  if(latte.getId()!=1001||macchiato.getId()!=1002||latte.e()||macchiato.e())throw new AssertionError("Local-only IDs");
  if(!latte.b()||macchiato.b()||latte.equals(macchiato)||latte.equals(stock))throw new AssertionError("Identity equality/mode");
  if(TodoistIdentities.append(all).size()!=3)throw new AssertionError("Duplicate entries");
  for(int id:new int[]{1001,1002}) {
   wdl resolved=(wdl)TodoistIdentities.fromPreference(new Preference(id));
   if(resolved.a.getId()!=id||resolved.c!=(id==1001)||resolved.b!=(id==1001?ydl.c:ydl.d))throw new AssertionError("Saved selection failed");
  }
  for(Integer id:new Integer[]{null,0,6,11})if(TodoistIdentities.fromPreference(new Preference(id))!=null)throw new AssertionError("Stock preference intercepted");
  System.out.println("Catalog, labels, local-only IDs, equality, persistence and untouched stock selections verified");
 }
}
