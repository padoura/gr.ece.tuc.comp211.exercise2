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

public class BTreeSearchResult {

	protected BTreeNode node;
	protected int diskAccessNum;
	protected int pageFound;
	protected int index;
	
	public BTreeSearchResult(){
		diskAccessNum = 0;
		pageFound = -1;
		index = -1;
	}
	
	public BTreeSearchResult(BTreeNode node, int pageFound){
		this.node = node;
		diskAccessNum = 0;
		index = -1;
		this.pageFound = pageFound;
	}
}
