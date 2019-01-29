package project1;
import java.util.ArrayList;

/**
 * This is the class to store association rules.
 */

/**
 * @author Zhengyang Zhao
 * Oct 21, 2018
 */

public class Rule {

	ArrayList<Integer> leftSet = null;
	ArrayList<Integer> rightSet = null;
	int supFullSet = 0;
	int supLeftSet = 0;
	double conf = -1.0;
	String rule = null;
	
	public Rule (ArrayList<Integer> leftSet, ArrayList<Integer> rightSet,
			int supFullSet, int supLeftSet, double conf) {
		this.leftSet = leftSet;
		this.rightSet = rightSet;
		this.supFullSet = supFullSet;
		this.supLeftSet = supLeftSet;
		this.conf = conf;
		// this.conf = (double) this.supFullSet / (double) this.supLeftSet;
	}
	
	public Rule (ArrayList<Integer> leftSet,  int supFullSet) {
		this.leftSet = leftSet;
		this.supFullSet = supFullSet;
	}
	
	public String getRule() {
		
		if (conf == -1.0) {
			// "LHS|{}|SUPPORT|-1"
			String lhs = leftSet.toString();
			String rhs = "{}";
			rule = lhs + " | " + rhs + " | " + supFullSet + " | " + "-1";
		} else {
			// "LHS|RHS|SUPPORT|CONFIDENCE";
			String lhs = leftSet.toString();
			String rhs = rightSet.toString();
			rule = lhs + " | " + rhs + " | " + supFullSet + " | " + conf;
		}
		return rule;
	}
	
}
