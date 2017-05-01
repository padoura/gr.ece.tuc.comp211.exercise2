/*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License version as
* published by the Free Software Foundation, either version 3 of the License, 
* or (at your option) any later version.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
* version 3 for more details.
*
* You should have received a copy of the GNU General Public License version
* 3 along with this work; if not, see <http://www.gnu.org/licenses/>.
*
* Please contact Michail Pantourakis via Github repository 
* https://github.com/padoura/gr.ece.tuc.comp211.exercise2 if you need additional 
* information or have any questions.
*/

package gr.ece.tuc.comp211;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class PostingFileNode {
	
	protected String[] filenames;
	protected int[] position;
	protected int numPos; //stored number of occurrences
	protected int nextPage; //used if there are more occurrences than a node's capacity

	
	public PostingFileNode(int nodeLength){
		this.filenames = new String[nodeLength];
		this.position = new int[nodeLength];
		this.numPos = 0;
		this.nextPage = -1;
		Arrays.fill(this.position, -1);
		Arrays.fill(filenames, String.format("%1$-8s",""));
	}
	
	public PostingFileNode(byte[] byteBuffer, int nodeLength) throws IOException{
		this.filenames = new String[nodeLength];
		this.position = new int[nodeLength];
		this.numPos = 0;
		this.nextPage = -1;
		Arrays.fill(this.position, -1);
		Arrays.fill(filenames, String.format("%1$-8s",""));
		this.deserialize(byteBuffer);
	}
	
	public PostingFileNode deserialize(byte[] byteBuffer) throws IOException{
		ByteArrayInputStream byteInputStream = new ByteArrayInputStream(byteBuffer);
		DataInputStream inputStream = new DataInputStream(byteInputStream);
		byte[] fileNameInBytes = new byte[8];
		for (int i=0;i<filenames.length;i++){ // Get filenames
			inputStream.read(fileNameInBytes);
			filenames[i] = new String(fileNameInBytes);
		}
	    for	(int i=0;i<filenames.length;i++){ // Get position
	    	position[i] = inputStream.readInt();
	    }
	    numPos = inputStream.readInt();
	    nextPage = inputStream.readInt();
	    inputStream.close();
	    return this;
	}
	
	public byte[] serialize(int pageSize) throws IOException{
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
	    DataOutputStream outputStream = new DataOutputStream(byteOutputStream);
    
	    for	(int i=0;i<filenames.length;i++){  // Serialize filenames
	    	outputStream.write(filenames[i].getBytes());
	    }
	    for	(int i=0;i<filenames.length;i++){  // Serialize position
	    	outputStream.writeInt(position[i]);
	    }
	    outputStream.writeInt(numPos); // Serialize number of occurrences
	    outputStream.writeInt(nextPage);
	    outputStream.close();
	    
	    byte[] byteBuffer = new byte[pageSize];
	    System.arraycopy(byteOutputStream.toByteArray(), 0, byteBuffer, 0, byteOutputStream.toByteArray().length);
		return byteBuffer;
	}
	
	public boolean isFull(){
		if (this.numPos == this.filenames.length){
			return true;
		}else{
			return false;
		}
	}
	
	public boolean hasNext(){
		if (this.nextPage >= 0){
			return true;
		}else{
			return false;
		}
	}
}
