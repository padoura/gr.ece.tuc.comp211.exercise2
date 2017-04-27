package gr.ece.tuc.comp211;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class BTreeNode {
	
	protected String[] key;
	protected int[] info;
	protected int[] child;
	protected int parent;
	protected int numKeys;
	
	
	
	
	
	public BTreeNode(int order){
		this.key = new String[order-1];
		this.info = new int[order-1];
		this.child = new int[order];
		Arrays.fill(this.child, -1);
		Arrays.fill(key, String.format("%1$-12s",""));
		this.parent = -1;
		this.numKeys = 0;
	}
	
	public BTreeNode(byte[] byteBuffer, int order) throws IOException{
		this.key = new String[order-1];
		this.info = new int[order-1];
		this.child = new int[order];
		Arrays.fill(this.child, -1);
		Arrays.fill(key, String.format("%1$-12s",""));
		this.parent = -1;
		this.numKeys = 0;
		this.deserialize(byteBuffer);
	}
	
	
	
	
	public boolean isLeaf(){
//		if (this.numKeys > 0){
			int[] temp = Arrays.copyOfRange(this.child, 0, this.numKeys);
			 Arrays.fill(temp, -1);
			return Arrays.equals(Arrays.copyOfRange(this.child, 0, this.numKeys), temp);
//		}else{
//			return false;
//		}
	}
	
	public byte[] serialize(int pageSize) throws IOException{
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        DataOutputStream outputStream = new DataOutputStream(byteOutputStream);
        

        
        
        for	(int i=0;i<key.length;i++){  // Serialize keys
        	outputStream.write(key[i].getBytes());
        }
        for	(int i=0;i<key.length;i++){  // Serialize info
        	outputStream.writeInt(info[i]);
        }
        for	(int i=0;i<child.length;i++){  // Serialize pointers
        	outputStream.writeInt(child[i]);
        }
        outputStream.writeInt(parent);
        outputStream.writeInt(numKeys); // serialize number of keys
        outputStream.close();
        
        byte[] byteBuffer = new byte[pageSize];
        System.arraycopy(byteOutputStream.toByteArray(), 0, byteBuffer, 0, byteOutputStream.toByteArray().length);
		return byteBuffer;
	}
	
	public BTreeNode deserialize(byte[] byteBuffer) throws IOException{
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteBuffer);
        DataInputStream inputStream = new DataInputStream(byteInputStream);
        byte[] keyInBytes = new byte[12];
        for	(int i=0;i<key.length;i++){  // Get keys
        	inputStream.read(keyInBytes);
        	key[i] = new String(keyInBytes);
        }
        for	(int i=0;i<key.length;i++){ // Get info
        	info[i] = inputStream.readInt();
        }
        for	(int i=0;i<child.length;i++){ // Get children
        	child[i] = inputStream.readInt();
        }
        parent = inputStream.readInt(); // Get parent pointer
        numKeys = inputStream.readInt(); // Get number of keys in node
		
        inputStream.close();
		return this;
	}
}
