/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Dec 13, 2018
 */

public class PairDouble {
	Integer recID;
	Double attrValue;
	
	PairDouble(int recID, double attrValue) {
		this.recID = recID;
		this.attrValue = attrValue;
	}
	
	public String toString() {
		String s = "" + recID + " : " + attrValue;
		return s;
	}
}
