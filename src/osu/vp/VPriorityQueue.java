package osu.vp;
import cmu.conditional.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.va.Stack;

/**
 * VPriorityQueue implementation.
 * @author Meng Meng
 *
 */

public class VPriorityQueue {
	public class node {
		int val;
		node left;
		node right;
	    Conditional<Integer> nums;
	    Conditional<Integer> mins;
	    public node() {
	    }
	    public node(int val) {
	    	this.val = val;
	    }
	    public node(int val, Conditional<Integer> nums, Conditional mins) {
	    	this.val = val;
	    	this.nums = nums;
	    	this.mins = mins;
	    	this.left = null;
	    	this.right = null;
	    }
	    
		public void print() {
			if(left != null) left.print();
			System.out.println("val = " + val + " " + nums + " " + mins);
			if(right != null) right.print();
		}
		
	}
	
	public node root = null;
	public VPriorityQueue() {
	}
	
	private Conditional<Integer> vmin(Conditional<Integer> num1, final Conditional<Integer> num2) {
		if(num1 == null) return num2;
		if(num2 == null) return num1;
		Conditional<Integer> mins = num1.mapfr(null, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				return num2.map(new Function<Integer, Integer>(){
					@Override
					public Integer apply(final Integer b) {
						if(a == null) return b == null ? null : b;
						if(b == null) return a == null ? null : a;
						if(a < b) return a;
						else return b;
					}
				}); 
			}
		});
		return mins.simplify();
	}
	
	private Conditional<Integer> vadd(Conditional<Integer> num1, final Conditional<Integer> num2) {
		if(num1 == null) return num2;
		if(num2 == null) return num1;
		Conditional<Integer> num3 = num1.mapfr(null, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				return num2.map(new Function<Integer, Integer>(){
					@Override
					public Integer apply(final Integer b) {
						if(a == null) return b == null ? null : b;
						if(b == null) return a == null ? null : a;
						return a + b;
					}
				}); 
			}
		});
		return num3;
	}
	
	private Conditional<Integer> vadd(Conditional<Integer> num, final FeatureExpr ctx, final int v) {
		if(num == null) return num;
		Conditional<Integer> ret = num.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				if(f.isContradiction()) return One.valueOf(a);
				if(f.isTautology()) return One.valueOf(a + v);
				return ChoiceFactory.create(f, One.valueOf(a + v), One.valueOf(a)).simplify();
			}
		}).simplify();
		return ret;
	}
	
	private void updateMin(node root) {
		FeatureExpr fe = root.nums.getFeatureExpr(0);
		Conditional<Integer> mins = ChoiceFactory.create(fe, (Conditional<Integer>)One.NULL, new One<>(root.val)).simplify();
		if(root.right != null) {
			mins = vmin(mins, root.right.mins);
		}
		if(root.left != null) {
			mins = vmin(mins, root.left.mins);
		}
		root.mins = mins;
	}
	

	public void add(int val, FeatureExpr e){
		if(root == null) {
			Conditional<Integer> min = ChoiceFactory.create(e, new One<>(val), (Conditional<Integer>)One.NULL);
			Conditional<Integer> num = ChoiceFactory.create(e, new One<>(1), new One<>(0));
			root = new node(val, num, min);
			return;
		} 
		root = addHelper(root, val, e);
		return;
	}
	private node addHelper(node root, int val, FeatureExpr e) {
		if(root == null) {
			Conditional<Integer> min = ChoiceFactory.create(e, One.valueOf(val), (Conditional<Integer>)One.NULL);
			Conditional<Integer> num = ChoiceFactory.create(e, One.ONE, One.ZERO);
			return new node(val, num, min);
		} 
		if(root.val == val) {
			Conditional<Integer> min = ChoiceFactory.create(e, new One<>(val), (Conditional<Integer>)One.NULL);
			root.nums = vadd(root.nums, e, 1);
			root.mins = vmin(min, root.mins);
			return root;
		}
		
		final Conditional<Integer> subMin;
		if(val < root.val) {
			root.left = addHelper(root.left, val, e);
			subMin = root.left.mins;
			root.mins = vmin(subMin,root.mins);
		} else {
			root.right = addHelper(root.right, val, e);
			subMin = root.right.mins;
			root.mins = vmin(root.mins, subMin);
			
		} 
		return root;
	}
	
	
	private node helper(node root) {
	        while(root.left != null) {
	            root = root.left;
	        }
	        return root;
	}
	public node removeNode(node root, int value) {
		if(root == null) return null;
		if(root.val == value && root.left == null && root.right == null) return null;
		if(root.val == value) {
			int tmp = root.val;

			if(root.right != null) {
				root.val = helper(root.right).val;
				helper(root.right).val = tmp;
				removeNode(root, value);
				return root;
			} else {
				return root.left;
			} 
		}
		root.left = removeNode(root.left, value);
		root.right = removeNode(root.right, value);
		return root;
	}
	

	public boolean remove(int val, FeatureExpr e) {
		 node ret = removeNode(root, val);
		 if(ret == null) return false;
		 return true;
	}
	
	public Conditional<Integer> peek(FeatureExpr e) {
		return root.mins.simplify(e);
	}
	private node update(node root) {
		if(root == null)  {
			return null;
		}
		if(root.left != null && root.left.mins == null) update(root.left);
		if(root.right != null && root.right.mins == null) update(root.right);
		updateMin(root);
		return root;
	}
	
	private void pollHelper(node root, final FeatureExpr ctx, int i) {
		if(root.val == i) {
			root.nums = vadd(root.nums, ctx, -1);
			root.mins = null;
			return;
		}
		root.mins = null;
		if(root.val > i) {
			pollHelper(root.left, ctx, i);
		} else {
			pollHelper(root.right, ctx, i);
		}
	}
	
	public Conditional<Integer> poll(final FeatureExpr e){
		Conditional<Integer> ret = root.mins.simplify(e);

		ret.mapf(e, new VoidBiFunction<FeatureExpr, Integer>() {
			public void apply(FeatureExpr ctx, Integer i) {
				if(ctx.isContradiction()) return;
				if(i == null) return;
				//System.out.println("poll " + ctx + " " + i);
				pollHelper(root, ctx, i);
			}
		});
		
		update(root);
		return ret;
	}
	
	public boolean contains(FeatureExpr e, int val) {
		return false;
	}
	
	public void clear() {
		
	}

	
	public static void main(String[] args) {
		
	}

	
}
