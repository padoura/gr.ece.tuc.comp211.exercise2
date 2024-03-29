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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class AsciiFileHandler extends File{
	// Class based on ReadFromAsciiFile.java example code given in class
	public AsciiFileHandler(String fileName){
		super(fileName);
	}
	
	public ArrayList<Word> readAsciiFile() throws IOException{
		ArrayList<Word> wordList = new ArrayList<Word>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(this)));
		String line = null;
        int line_count=0;
        int byte_count;
        int total_byte_count=0;
        int fromIndex;
        while( (line = reader.readLine())!= null /*&& line_count < 1*/){
            line_count++;
            fromIndex=0;
            String [] tokens = 
            		line.split(",\\s+|\\s*\\\"\\s*|\\s+|\\.\\s*|\\s*:\\s*|\\s*'\\w*\\s+|;\\s*|\\s*[|]\\s*|\\s*-+\\s*|\\s*\\,\\s*|\\s*!\\s*|\\s*\\?\\s*");
            String line_rest=line;
            for (int i=1; i <= tokens.length; i++) {
                    byte_count = line_rest.indexOf(tokens[i-1]);
                    if ( tokens[i-1].length() != 0)
                    	wordList.add(new Word(tokens[i-1].toLowerCase(), total_byte_count + fromIndex, this.getName()));
                    fromIndex = fromIndex + byte_count + 1 + tokens[i-1].length();
                    if (fromIndex < line.length())
                      line_rest = line.substring(fromIndex);
            }
            total_byte_count += fromIndex;
        }
        reader.close();
        
        return wordList;
	}
}
