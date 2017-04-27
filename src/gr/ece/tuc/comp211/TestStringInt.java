package gr.ece.tuc.comp211;
import java.io.*;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

public class TestStringInt {

            private static final int DataPageSize = 1024; // Default Data Page size

            public static void main(String args[]) throws IOException {
                ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
                DataOutputStream out = new DataOutputStream(bos);
                String s = "Hello";
                byte dst[] = new byte[10];
                byte[] src = s.getBytes();
                System.arraycopy(src, 0, dst, 1, src.length);
                out.write(dst, 0, 10);  // Write fixed size string (10 bytes)

                out.writeInt(16);  // Write Int (4 bytes)

                s = "Sir";
                src = s.getBytes();
                dst = new byte[10];
                System.arraycopy(src, 0, dst, 1, src.length);
                out.write(dst, 0, 10);  // Write fixed size string (10 bytes)

                out.writeInt(17);  // Write Int (4 bytes)

                out.writeUTF("Hello there"); // Write variable size string (string size + 2 bytes)

                out.writeInt(18);  // Write Int (4 bytes)
                out.close();

                // Get the bytes of the serialized object
                byte[] buf = bos.toByteArray(); // Creates a newly allocated byte array.
                System.out.println("\nbuf size: " + buf.length + " bytes");
                byte[] DataPage = new byte[DataPageSize];
                System.arraycopy( buf, 0, DataPage, 0, buf.length); // Copy buf data to DataPage of DataPageSize
                bos.close();

                // write to the file
                RandomAccessFile MyFile = new RandomAccessFile ("newbabis", "rw");
                MyFile.seek(0);
                MyFile.write(DataPage);

                // Read from file
                byte[] ReadDataPage = new byte[DataPageSize];
                MyFile.seek(0);
                MyFile.read(ReadDataPage);
                ByteArrayInputStream bis= new ByteArrayInputStream(ReadDataPage);
                DataInputStream ois= new DataInputStream(bis);
                byte bb[] = new byte[10];
                ois.read(bb);
                String ss = new String(bb);
                System.out.println("\nstring1 = " + ss);
                System.out.println("\nint1 = " + ois.readInt());
                ois.read(bb);
                ss = new String(bb);
                System.out.println("\nstring2 = " + ss);
                System.out.println("\nint2 = " + ois.readInt());
                System.out.println("\nstring3 = " + ois.readUTF());
                System.out.println("\nint3 = " + ois.readInt());
                MyFile.close();
           }
}