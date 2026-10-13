// SPDX-License-Identifier: GPL-3.0-or-later
package app.morphe.extension.anki;
import java.nio.file.*;
import java.nio.ByteBuffer;
public class FontValidation {
 public static void main(String[] args)throws Exception {
  byte[] valid=Files.readAllBytes(Path.of(args[0]));FontSettingsActivity.validate(valid);
  for(int kind=0;kind<5;kind++) {
   byte[] invalid=valid.clone();
   switch(kind) {case 0: invalid=new byte[0];break;case 1:invalid[0]=1;break;case 2:ByteBuffer.wrap(invalid).putShort(4,(short)257);break;case 3:ByteBuffer.wrap(invalid).putInt(20,Integer.MAX_VALUE);break;case 4:for(int i=12;i<12+(ByteBuffer.wrap(invalid).getShort(4)&65535)*16;i+=16) if(new String(invalid,i,4,java.nio.charset.StandardCharsets.US_ASCII).equals("cmap"))invalid[i]='x';break;}
   try {FontSettingsActivity.validate(invalid);throw new AssertionError("Invalid font accepted: "+kind);}catch(java.io.IOException expected){}
  }
  System.out.println("Font validator: valid TTF accepted; empty, wrong signature, bad directory, invalid bounds, and missing required tables rejected.");
 }
}
