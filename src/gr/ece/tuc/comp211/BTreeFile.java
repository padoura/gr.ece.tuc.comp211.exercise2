package gr.ece.tuc.comp211;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.NoSuchElementException;

public class BTreeFile extends RandomAccessFile{
	
	protected int pageSize;
	protected int order;
	
	public BTreeFile(int pageSize, String fileName) throws FileNotFoundException{
		super(fileName, "rw");
		this.pageSize = pageSize;
		// Order of B-tree as a function of pageSize for 12-bytes key and 4-bytes pointers
		this.order = (int) Math.floor(Math.floor((double) ((this.pageSize+8)/20))/2)*2 ; //rounding down to nearest even number
	}
	
	
	public BTreeSearchResult searchKey(String key) throws IOException{
		
		Assert.notFalse(this.length() != 0, "The B-tree has not been created yet!");
		
		key = String.format("%1$-12s", key).substring(0,12); //Append whitespaces to keep String size fixed to 12
		BTreeNode node = new BTreeNode( this.order );
		int child = 0;
		byte[] byteBuffer;
		BTreeSearchResult result = new BTreeSearchResult();
		int i;
		long offset = child*this.pageSize; //File offset for RandomAccessFile
		
		while(true){
			byteBuffer = this.readPage(offset);  //Read the root
			result.pageFound = child;
			result.diskAccessNum++; // increment 1 disk access
			
			node.deserialize(byteBuffer);
            result.node = node;
            i = Arrays.binarySearch(Arrays.copyOfRange(node.key, 0, node.numKeys), key); // Search key in buffer
	        if (i >= 0){
	        	result.index = i;
 	            return result;
	        }else{
	        	child = result.node.child[-1-i]; // continue down to child node
	            if (child == -1){  // leaf node reached without success
	            	result.index = i;
	            	return result;
	            }    
	        }
	        offset = child*this.pageSize;
		}
	}
	
	
	//Based on B-TREE-INSERT pseudocode found in http://staff.ustc.edu.cn/~csli/graduate/algorithms/book6/chap19.htm
	public BTreeSearchResult insert(String key, int info) throws IOException{
		int diskAccessNum = 0;
		if (this.length() == 0){
			this.create();
			diskAccessNum = 1; //create B-tree, 1 disk access
		}//else{
//			BTreeSearchResult result = this.searchKey(key);
//			if(result.index>=0){ // Key already exists!
//				return result;
//			}
//		}
		BTreeNode root = new BTreeNode(this.readPage(0), this.order); //read root, 1 disk access
		
		if (root.numKeys == root.key.length){//Create new root and split old one
			BTreeSearchResult newRoot =  new BTreeSearchResult(new BTreeNode( this.order ), 0);
			root.parent = 0; //Always keep root as first page
			BTreeSearchResult oldRoot = new BTreeSearchResult(root, (int) (this.length()/this.pageSize));  //Move old root to end of file
			newRoot.node.child[0] = oldRoot.pageFound;
			BTreeNode newChild = this.split(newRoot.node, 0, oldRoot); // 3 accesses for split
			diskAccessNum = diskAccessNum + this.updatePointerOfChildren(oldRoot); // Access all children of old root to update their parent pointer
			diskAccessNum = diskAccessNum + this.updatePointerOfChildren(new BTreeSearchResult(newChild, newRoot.node.child[1])); // Access all children of new node to update their parent pointer
			newRoot.diskAccessNum = 4 + diskAccessNum;
			return insertNonFull(newRoot, key, info);
		}else{
			BTreeSearchResult oldRoot = new BTreeSearchResult(root, 0);
			oldRoot.diskAccessNum = 1 + diskAccessNum; // 1 access for root
			return insertNonFull(oldRoot, key, info);
		}
	}

	private BTreeSearchResult insertNonFull(BTreeSearchResult input, String key, int info) throws IOException {
		key = String.format("%1$-12s", key).substring(0,12); //Append whitespaces to keep String size fixed to 12
		
		if (input.node.numKeys <= 0){
			input.node.key[0] = key;
			input.node.info[0] = info;
			input.node.numKeys++;
			byte[] byteBuffer = input.node.serialize(this.pageSize);
			this.writePage(byteBuffer, input.pageFound*this.pageSize); // + 1 disk access
			input.diskAccessNum++;
		}else{
			int i = -1-Arrays.binarySearch(Arrays.copyOfRange(input.node.key, 0, input.node.numKeys), key); // Search key first
				if (input.node.isLeaf()){
					for (int j = input.node.numKeys; j>i; j--){
						input.node.key[j] = input.node.key[j-1];
						input.node.info[j] = input.node.info[j-1];
					}
					input.node.key[i] = key;
					input.node.info[i] = info;
					input.node.numKeys++;
					
					byte[] byteBuffer = input.node.serialize(this.pageSize);
					this.writePage(byteBuffer, input.pageFound*this.pageSize); // + 1 disk access
					input.diskAccessNum++;
				}else{
					BTreeNode childNode = new BTreeNode(this.readPage(input.node.child[i]*this.pageSize), this.order); // + 1 disk access
					BTreeSearchResult child = new BTreeSearchResult(childNode, input.node.child[i]);
					child.diskAccessNum = input.diskAccessNum+1;
					
					if (child.node.numKeys == child.node.key.length){
						childNode = split(input.node, i, child); // + 3 disk access and return new child
						int diskAccessNum = this.updatePointerOfChildren(child); // Access all children of old root to update their parent pointer
						diskAccessNum = diskAccessNum + this.updatePointerOfChildren(new BTreeSearchResult(childNode, input.node.child[i+1])); // Access all children of new node to update their parent pointer
						if (key.compareTo(input.node.key[i])>0)
							child = new BTreeSearchResult(childNode, input.node.child[i+1]);
						child.diskAccessNum = input.diskAccessNum + 4 + diskAccessNum;
					}
					return insertNonFull(child, key, info);
				}
		}
		return input;
	}


	//Based on B-TREE-SPLIT-CHILD pseudocode found in http://staff.ustc.edu.cn/~csli/graduate/algorithms/book6/chap19.htm
	private BTreeNode split(BTreeNode parentNode, int index, BTreeSearchResult oldChild) throws IOException{
		
		// Allocate new node and find median
		BTreeNode newChild = new BTreeNode(this.order);
		int median = (int) Math.round( (double) this.order/2);
		
		// Move values to new node, left bias
		for (int i=0; i < median-1; i++){
			newChild.key[i] = oldChild.node.key[i+median];
			newChild.info[i] = oldChild.node.info[i+median];
		}
		newChild.numKeys = median-1;
		newChild.parent = oldChild.node.parent;
		if (!oldChild.node.isLeaf()){
			for (int i=0; i < median; i++){
				newChild.child[i] = oldChild.node.child[i+median];
			}
		}
		// Update numKeys left
		oldChild.node.numKeys = oldChild.node.numKeys - newChild.numKeys - 1;
		
		// Move median key to parent
		for (int i = parentNode.numKeys; i > index; i--){
			parentNode.child[i+1] = parentNode.child[i];
		}
		for (int i = parentNode.numKeys-1; i > index-1; i--){
			parentNode.key[i+1] = parentNode.key[i];
			parentNode.info[i+1] = parentNode.info[i];
		}
		parentNode.key[index] = oldChild.node.key[median-1];
		parentNode.info[index] = oldChild.node.info[median-1];
		parentNode.numKeys++;
		
		// Write 3 nodes to disk
		byte[] byteBuffer = new byte[this.pageSize];
		byteBuffer = oldChild.node.serialize(this.pageSize);
		this.writePage(byteBuffer, oldChild.pageFound*this.pageSize);
		
		byteBuffer = newChild.serialize(this.pageSize);
		this.writePage(byteBuffer, this.length());
		
		parentNode.child[index+1] = (int) (this.length()/this.pageSize - 1); //New node is always in last page
		byteBuffer = parentNode.serialize(this.pageSize);
		this.writePage(byteBuffer, newChild.parent*this.pageSize);
		return newChild;
	}
	
	private void create() throws IOException{
		BTreeNode node = new BTreeNode( this.order );
		this.writePage(node.serialize(this.pageSize), 0);
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
	
	private int updatePointerOfChildren(BTreeSearchResult oldRoot) throws IOException {
		if (oldRoot.node.isLeaf()){
			return 0;
		}else{
			for (int i=0; i < oldRoot.node.numKeys+1;i++){
				BTreeNode child = new BTreeNode(this.readPage(oldRoot.node.child[i]*this.pageSize), this.order);
				child.parent = oldRoot.pageFound; // Update parent's page for each child
				this.writePage(child.serialize(this.pageSize), oldRoot.node.child[i]*this.pageSize);
			}
			return oldRoot.node.numKeys+1;
		}
	}
	
	

}
