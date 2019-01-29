package project1;
import java.util.Comparator;

/**
 * 
 */

/**
 * @author Zhengyang Zhao
 * Oct 19, 2018
 */

public class FrequencyComparator implements Comparator<Pair> {

	int option;
	
	FrequencyComparator(int option) {
		this.option = option;
	}
	
	@Override
	public int compare(Pair o1, Pair o2) {
		
		if (option == 2) {
			// increasing order, with itemID = 0 at head.
			if (o1.itemID == 0) {
				return -1;
			} 
			if (o2.itemID == 0) {
				return 1;
			}
			if (o1.freq == o2.freq) {
				return o1.itemID.compareTo(o2.itemID);
			}
			return o1.freq.compareTo(o2.freq);
			
		} else {
			// decreasing order, with itemID = 0 at head.
			if (o1.itemID == 0) {
				return -1;
			} 
			if (o2.itemID == 0) {
				return 1;
			}
			if (o1.freq == o2.freq) {
				return o2.itemID.compareTo(o1.itemID);
			}
			return o2.freq.compareTo(o1.freq);
		}
	}
}


