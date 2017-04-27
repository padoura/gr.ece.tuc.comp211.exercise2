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
