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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class PostingFile extends RandomAccessFile{
	
	protected int pageSize;
	protected int nodeLength; // maximum number of word occurrences that a node can hold
	
	public PostingFile(int pageSize, String filename) throws FileNotFoundException{
		super(filename, "rw");
		if (pageSize < 20){
			throw new IllegalArgumentException("The Posting List cannot be created for pageSize lower than 20.");
		}
		this.pageSize = pageSize;
		// nodeLength as a function of pageSize for 8-bytes filename, 4-bytes pointers and 8-bytes overhead
		nodeLength = (int) (pageSize-8)/12;
	}
	
	public int insert(String filename, int position, int pageFound) throws IOException{
		int diskAccessNum = 1;
		filename = String.format("%1$-8s", filename).substring(0,8); //Append whitespaces to keep String size fixed to 8
		if (pageFound*pageSize >= this.length()){ // Insert new word
			PostingFileNode node = this.createNewNode(filename, position);
			this.writePage(node.serialize(pageSize), pageSize*pageFound);
		}else{
			PostingFileNode node = this.getPostingFilePage(pageFound);
			while (node.isFull() && node.hasNext()){
				pageFound = node.nextPage;
				node = this.getPostingFilePage(node.nextPage);
				diskAccessNum++;
			}
			
			if (node.isFull()){ // Last node reached and is full
				node.nextPage = (int) (this.length()/pageSize);
				this.writePage(node.serialize(pageSize), pageSize*pageFound);
				PostingFileNode newNode = this.createNewNode(filename, position);
				this.writePage(newNode.serialize(pageSize), pageSize*node.nextPage);
				diskAccessNum += 2;
			}else{
				node.filenames[node.numPos] = filename;
				node.position[node.numPos] = position;
				node.numPos++;
				this.writePage(node.serialize(pageSize), pageSize*pageFound);
				diskAccessNum++;
			}
		}
		return diskAccessNum;
	}
	
	
	
	
	
	private void writePage(byte[] byteBuffer, long offset) throws IOException{
		this.seek(offset);
		this.write(byteBuffer);
	}

	private byte[] readPage(long offset) throws IOException{
		byte[] byteBuffer = new byte[this.pageSize];
		this.seek(offset);
		this.read(byteBuffer);
		return byteBuffer;
	}
	
	public PostingFileNode getPostingFilePage(int pageFound) throws IOException{
		byte[] byteBuffer = this.readPage(pageFound*pageSize);
		PostingFileNode node = new PostingFileNode(byteBuffer, nodeLength);
		return node;
	}
	
	private PostingFileNode createNewNode(String filename, int position){
		PostingFileNode node = new PostingFileNode(nodeLength);
		node.filenames[0] = filename;
		node.position[0] = position;
		node.numPos = 1;
		return node;
	}
	

}
