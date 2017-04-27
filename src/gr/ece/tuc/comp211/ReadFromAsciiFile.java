package gr.ece.tuc.comp211;
import java.io.*;

class ReadFromAsciiFile {
        public static void main(String[] args) throws IOException {
                File file = new File("Obama.txt");
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = null;
                int line_count=0;
                int byte_count;
                int total_byte_count=0;
                int fromIndex;
                while( (line = br.readLine())!= null){
                        line_count++;
                        fromIndex=0;
                        String [] tokens = 
                        		line.split(",\\s+|\\s*\\\"\\s*|\\s+|\\.\\s*|\\s*:\\s*|\\s*'\\w*\\s+|;\\s*|\\s*[|]\\s*|\\s*-+\\s*|\\s*\\,\\s*|\\s*!\\s*|\\s*\\?\\s*");
                        String line_rest=line;
                        for (int i=1; i <= tokens.length; i++) {
                                byte_count = line_rest.indexOf(tokens[i-1]);
                                if ( tokens[i-1].length() != 0)
                                	
                                  System.out.println("\n(line:" + line_count + ", word:" + i + ", start_byte:" + (total_byte_count + fromIndex) 
                                		  + "' word_length:" + tokens[i-1].length() + ") = " + tokens[i-1]);
                                fromIndex = fromIndex + byte_count + 1 + tokens[i-1].length();
                                if (fromIndex < line.length())
                                  line_rest = line.substring(fromIndex);
//                                if (tokens[i-1].matches("\\w*\\W\\w*|\\w*[0-9_]\\w*"))
//                                	return;
                        }
                        total_byte_count += fromIndex;
                }
        }
}