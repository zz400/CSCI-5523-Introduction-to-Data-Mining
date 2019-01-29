import java.util.Comparator;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Dec 13, 2018
 */

public class PairDoubleComparator implements Comparator<PairDouble> {

	@Override
	public int compare(PairDouble p1, PairDouble p2) {
		// TODO Auto-generated method stub
		if (p1.attrValue < p2.attrValue) {
			return -1;
		}
		
		if (p1.attrValue > p2.attrValue) {
			return 1;
		}
		
		if (p1.recID < p2.recID) {
			return -1;
		}
		
		if (p1.recID > p2.recID) {
			return 1;
		}
		
		return 0;
	}

}
