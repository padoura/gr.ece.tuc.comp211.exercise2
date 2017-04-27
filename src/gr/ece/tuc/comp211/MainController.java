package gr.ece.tuc.comp211;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class MainController {

	public static void main(String[] args) throws IOException {
		String[] fileName = { "Obama.txt", "Kennedy.txt", "MartinLutherKing.txt"};
		String bTreeName = "BTreeFile.bin";
		File deletingBtree = new File(bTreeName);
		deletingBtree.delete();
		int pageSize = 128;
		int diskAccessBTreeInsert = 0;
		int numberOfInsertions = 0;
		ArrayList<Word> wordList = new ArrayList<Word>();
		BTreeSearchResult bTreeInsertResult = new BTreeSearchResult();
				
		for (int i=0;i<1;i++){
			AsciiFileHandler file = new AsciiFileHandler(fileName[i]);
//			wordList.removeAll(file.readAsciiFile());
			wordList.addAll(file.readAsciiFile());
		}

		
		BTreeFile bTree = new BTreeFile(pageSize, bTreeName);
		Iterator<Word> iter = wordList.iterator();
		
		while(iter.hasNext()){
			Word word = iter.next();
			bTreeInsertResult = bTree.insert(word.key, word.pos);
			if (bTreeInsertResult.index < 0){
				diskAccessBTreeInsert += bTreeInsertResult.diskAccessNum;
				numberOfInsertions++;
			}
		}
		
		System.out.println(diskAccessBTreeInsert);
		System.out.println("Average cost of insertion: " + (float) diskAccessBTreeInsert/numberOfInsertions);
		
//		BTreeSearchResult test = bTree.searchKey("y");
		System.out.println("Number of Nodes: " + bTree.length()/pageSize + " " + numberOfInsertions);
		bTree.close();
	}
}