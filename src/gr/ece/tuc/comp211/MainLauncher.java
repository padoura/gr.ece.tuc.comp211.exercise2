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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class MainLauncher {

	public static void main(String[] args) throws IOException {
		
		// Input parameters
		String[] fileName = { "Obama", "Kennedy",  "MartinLu"};
		String bTreeName = "BTreeFile.bin";
		String postingFilename = "PostingFile.bin";
		int pageSize = 128;
		int amountOfSearches = 100;
		double fractionNotExistingWords = 1;
		
		// Delete previous files
		File deleting = new File(bTreeName); 
		deleting.delete();
		deleting = new File(postingFilename);
		deleting.delete();

		// Inserting words
		Controller controller = new Controller(fileName, pageSize);
		controller.createStructure();
		
//		System.out.println("Total disk accesses: " + controller.diskAccessBTreeInsert);
		System.out.println("Number of total words: " + controller.numberOfInsertions);
		System.out.println("Average cost of all insertions (including duplicates): " + (float) controller.diskAccessBTreeInsert/controller.numberOfInsertions);
//		System.out.println("Total disk accesses without repetitions: " + controller.accessUnique);
		System.out.println("Number of unique words: " + controller.numberUnique);
		System.out.println("Average cost of unique insertions: " + (float) controller.accessUnique/controller.numberUnique);
		
		// Get all unique words in files
		ArrayList<Word> wordList = new ArrayList<Word>();
		for (int i=0;i<fileName.length;i++){
			AsciiFileHandler file = new AsciiFileHandler(fileName[i]);
			wordList.addAll(file.readAsciiFile());
		}
		ArrayList<String> wlist = new ArrayList<String>();
		for (int i=0;i<wordList.size();i++){
			wlist.add(wordList.get(i).key);
		}
		HashSet<String> keyList = new HashSet<String>(wlist);
		
		// Same random words to be used in both cases below
		int[] randomIndices = controller.getRandomIntegers(amountOfSearches, keyList.size());
		
		// Search random words, all included
		double meanBTreeAccesses = 0;
		double meanPostingAccesses = 0;
		DiskAccessNum diskAccessNum;
		for(int i=0; i<amountOfSearches;i++){
			String key = (String) keyList.toArray()[randomIndices[i]];
			diskAccessNum = controller.searchKey(key);
			meanBTreeAccesses += diskAccessNum.bTree;
			meanPostingAccesses += diskAccessNum.posting;
		}
		meanBTreeAccesses = meanBTreeAccesses/amountOfSearches;
		meanPostingAccesses = meanPostingAccesses/amountOfSearches;
		System.out.println("Number of B-tree disk accesses per successful search: " + meanBTreeAccesses);
		System.out.println("Number of Posting File disk accesses per successful search: " + meanPostingAccesses);
		
		// Search 100 random words, some not included
		Random RNG = new Random();
		meanBTreeAccesses = 0;
		meanPostingAccesses = 0;
		for(int i=0; i<amountOfSearches;i++){
			String key = (String) keyList.toArray()[randomIndices[i]];
			if (RNG.nextDouble() < fractionNotExistingWords){
				char[] randomChar = new char[12];
				Arrays.fill(randomChar, (char) (RNG.nextInt(26) + 'a'));
				key = new String(randomChar); //search a random 12-same-letter word that doesn't exist
			}
			diskAccessNum =  controller.searchKey(key);
			meanBTreeAccesses += diskAccessNum.bTree;
			meanPostingAccesses += diskAccessNum.posting;
		}
		meanBTreeAccesses = meanBTreeAccesses/amountOfSearches;
		meanPostingAccesses = meanPostingAccesses/amountOfSearches;
		System.out.println("Number of B-tree disk accesses per random search: " + meanBTreeAccesses);
		System.out.println("Number of Posting File disk accesses per random search: " + meanPostingAccesses);
	}
}