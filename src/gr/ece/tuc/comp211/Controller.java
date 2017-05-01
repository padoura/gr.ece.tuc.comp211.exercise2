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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Controller {
	
	protected int diskAccessBTreeInsert;
	protected int numberOfInsertions;
	protected int numberUnique;
	protected int accessUnique;
	private String[] fileName;
	private int pageSize;
	private String postingFilename;
	private String bTreeName;
	
	public Controller(String[] fileName, int pageSize) throws IOException{
		for (int i=0; i<fileName.length;i++){
			if (fileName[i].length()>8){
				File oldFile = new File(fileName[i]);
				fileName[i] = fileName[i].trim(); // remove whitespaces
				if (fileName[i].length()>8)
					fileName[i] = fileName[i].replaceAll(".\\w+\\z",""); // remove filename extension if filename is still bigger than 8 bytes
				if (fileName[i].length()>8)
					fileName[i] = fileName[i].substring(0, 8);
				File newFile = new File(fileName[i]);
				if (newFile.exists())
					throw new java.io.IOException("A file with the same 8-character name already exists!"
							+ "\n Rename files so that the respective substring of their filename is unique!");
				oldFile.renameTo(newFile);
			}
		}
		this.fileName = fileName;
		this.pageSize = pageSize;
		diskAccessBTreeInsert = 0;
		numberOfInsertions = 0;
		numberUnique = 0;
		accessUnique = 0;
		bTreeName = "BTreeFile.bin";
		postingFilename = "PostingFile.bin";
	}
	
	public void createStructure() throws IOException{
		// Read text files
			ArrayList<Word> wordList = new ArrayList<Word>();
			for (int i=0;i<fileName.length;i++){
				AsciiFileHandler file = new AsciiFileHandler(fileName[i]);
				wordList.addAll(file.readAsciiFile());
			}

			BTreeSearchResult bTreeInsertResult = new BTreeSearchResult();
			BTreeFile bTree = new BTreeFile(pageSize, bTreeName);
			PostingFile posting = new PostingFile(pageSize, postingFilename);
			Iterator<Word> iter = wordList.iterator();
			
			while(iter.hasNext()){
				Word word = iter.next();
				if (bTree.length() == 0){
					posting.insert(word.filename, word.pos, 0); // 1 diskAccess but is not asked by exercise
					bTreeInsertResult = bTree.insert(word.key, 0);
					diskAccessBTreeInsert += bTreeInsertResult.diskAccessNum;
					accessUnique += bTreeInsertResult.diskAccessNum;
					numberUnique++;
				}else{
					bTreeInsertResult = bTree.searchKey(word.key);
					diskAccessBTreeInsert += bTreeInsertResult.diskAccessNum;
					if (bTreeInsertResult.index < 0){
						accessUnique += bTreeInsertResult.diskAccessNum;
						posting.insert(word.filename, word.pos, (int) (posting.length()/pageSize)); // insert new node in Posting File
						bTreeInsertResult = bTree.insert(word.key, (int) (posting.length()/pageSize)-1);
						diskAccessBTreeInsert += bTreeInsertResult.diskAccessNum;
						accessUnique += bTreeInsertResult.diskAccessNum;
						numberUnique++;
					}else{
						posting.insert(word.filename, word.pos, bTreeInsertResult.node.info[bTreeInsertResult.index]); // insert new entry for existing word
					}
				}
				numberOfInsertions++;
			}
		System.out.println("Number of B-tree Nodes: " + bTree.length()/pageSize);
		bTree.close();
		posting.close();
	}
	
	public DiskAccessNum searchKey(String key) throws IOException{
		BTreeFile bTree = new BTreeFile(pageSize, bTreeName);
		PostingFile posting = new PostingFile(pageSize, postingFilename);
		DiskAccessNum diskAccessNum = new DiskAccessNum();
		
		BTreeSearchResult bTreeInsertResult = bTree.searchKey(key); // Find key in BTree
		diskAccessNum.bTree = bTreeInsertResult.diskAccessNum;
		if (bTreeInsertResult.index < 0){ // not found
//			System.out.println("No text contains word '" + key + "'");
		}else{  // found
			int postingPage = bTreeInsertResult.node.info[bTreeInsertResult.index];
			while(postingPage >=0){
				PostingFileNode postingNode = posting.getPostingFilePage(postingPage);
				diskAccessNum.posting++;
				for (int i=0;i<postingNode.numPos;i++){
//					System.out.println("Text '" + postingNode.filenames[i].trim() + "' contains word '" + key + "' in position " + postingNode.position[i]);
				}
				postingPage = postingNode.nextPage;
			}
		}

		bTree.close();
		posting.close();
		return diskAccessNum;
	}
	
	
	public int[] getRandomIntegers(int amountOfSearches, int numberOfInt){ // return an array of random integers
		Random RNG = new Random();
		int [] randomInteger = new int[amountOfSearches];
		for (int i=0; i<amountOfSearches;i++)
			randomInteger[i] = RNG.nextInt(numberOfInt);
		return randomInteger;
	}
	
	
	
}