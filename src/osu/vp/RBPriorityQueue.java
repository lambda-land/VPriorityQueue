package osu.vp;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import osu.vp.kvpair.IVPriorityQueue;

/**
 * RBPriorityQueue implementation.
 * @author Meng Meng
 *
 */


/**
 * u = #nodes to update
 * a = time to update and access
 * l = size of labels
 * w = work on each label
 * O(n*a*w)
 * 
 * the worst case for poll operation : N^3log(N) could be N^2log(N)
 */
class RBNode {
	int val;
	RBNode left;
	RBNode right;
	int color;
	Conditional<Integer> nums;
    Conditional<Integer> mins;

    public RBNode(int val, RBNode l, RBNode r) {
    	this.val = val;
    	this.left = l;
    	this.right = r;
    	this.color = 1;
    }

    public void print() {
		if(left != null) left.print();
		System.out.println("val = " + val + " " + nums + " " + mins);
		if(right != null) right.print();
	}
}

public class RBPriorityQueue implements IVPriorityQueue {
	public RBNode root;
	private RBNode current;
	private RBNode parent;
	private RBNode grand;
	private RBNode great;
	
	private static RBNode nullNode;
	static {
		nullNode = new RBNode(0, nullNode, nullNode);
		nullNode.left = nullNode;
		nullNode.right = nullNode;
		nullNode.nums = One.ZERO;
		nullNode.mins = (Conditional<Integer>)One.NULL;
	}
	
	static final int BLACK = 1;
	static final int RED = 0;
	
	public RBPriorityQueue() {
		root = nullNode;
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
	
	private Conditional<Integer> vadd(Conditional<Integer> num, final FeatureExpr ctx, final int v) {
		if(num == null) return num;
		Conditional<Integer> ret = num.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				Integer av = a + v;
				if(av < 0) av = 0;
				if(f.isContradiction() || av == a) return One.valueOf(a);
				if(f.isTautology()) return One.valueOf(av);
				return ChoiceFactory.create(f, One.valueOf(av), One.valueOf(a)).simplify();
			}
		}).simplify();
		return ret;
	}
	
	public void add(int val, FeatureExpr e) {
		if(root == nullNode) {
			root = new RBNode(val, nullNode, nullNode);
			Conditional<Integer> min = ChoiceFactory.create(e, One.valueOf(val), (Conditional<Integer>)One.NULL);
			Conditional<Integer> num = ChoiceFactory.create(e, One.ONE, One.ZERO);
			root.mins = min;
			root.nums = num;
			return;
		}
		
		current = parent = grand = root;
		nullNode.val = val;
		while (current.val != val) {
			great = grand;
			grand = parent;
			parent = current;
			current = val < current.val ? current.left : current.right;
			if (current.left.color == RED && current.right.color == RED) {
				handleReorient(val);
			}
		}
		if (current != nullNode) {
			Conditional<Integer> min = ChoiceFactory.create(e, new One<>(val), (Conditional<Integer>)One.NULL);
			current.nums = vadd(current.nums, e, 1);
			current.mins = vmin(min, current.mins);
		} else {
			
			current = new RBNode(val, nullNode, nullNode);
			Conditional<Integer> min = ChoiceFactory.create(e, One.valueOf(val), (Conditional<Integer>)One.NULL);
			Conditional<Integer> num = ChoiceFactory.create(e, One.ONE, One.ZERO);
			current.mins = min;
			current.nums = num;
			// Attach to parent
			if (val < parent.val)
				parent.left = current;
			else
				parent.right = current;
			handleReorient(val);
		}
		
		update(root, val);
		nullNode.val = -1;
	}
	
	private void updateMin(RBNode root) {
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
	
	private void update(RBNode root, int val) {
		if(root == nullNode) return;
		if(root.val == val) {
			return;
		}
		if(val < root.val) {
			update(root.left, val);
		} else {
			update(root.right, val);
		}
		updateMin(root);
		return;
	}
	
	private void handleReorient(int item) {
		// Do the color flip
		current.color = RED;
		current.left.color = BLACK;
		current.right.color = BLACK;

		if (parent.color == RED) {
			// Have to rotate
			grand.color = RED;
			if (item < grand.val != item < parent.val)
				parent = rotate(item, grand); // Start dbl rotate
			current = rotate(item, great);
			current.color = BLACK;
		}
		// Make root black
		root.right.color = BLACK;
	}
	
	private RBNode rotate(int item, RBNode parent) {
		if (item < parent.val)
			return parent.left = item < parent.left.val ? rotateWithLeftChild(parent.left)
					: rotateWithRightChild(parent.left);
		else
			return parent.right = item < parent.right.val ? rotateWithLeftChild(parent.right)
					: rotateWithRightChild(parent.right);
	}
	
	private RBNode rotateWithRightChild(RBNode k1) {
		if(k1 == nullNode) System.out.println("null k1");
		if(k1.nums == null) System.out.println("nums null k1");
		RBNode k2 = k1.right;
		k1.right = k2.left;
		k2.left = k1;
		FeatureExpr fe = k1.nums.getFeatureExpr(0);
		k2.mins = k1.mins;
		updateMin(k1);
		return k2;
	}
	
	private RBNode rotateWithLeftChild(RBNode k2) {
		RBNode k1 = k2.left;
		k2.left = k1.right;
		k1.right = k2;
		k1.mins = k2.mins;
		updateMin(k2);
		return k1;
	}
	
	public Conditional<Integer> peek(FeatureExpr e) {
		return root.mins.simplify(e);
	}
	
	public void printNode(RBNode r) {
		if (r != nullNode) {
			char c = 'B';
			if (r.color == 0)
				c = 'R';
			//System.out.println(r.val + "" + c + " ");
			
			System.out.println(r.val + " " + c + " " + r.left.val + " " + r.right.val);
			System.out.println(r.mins + " " + r.nums);
			
			printNode(r.left);
			printNode(r.right);
		}
	}
	
	public void print() {
		printNode(root);
	}
	
	private RBNode update(RBNode root) {
		if(root == nullNode)  {
			return nullNode;
		}
		if(root.left != null && root.left.mins == null) update(root.left);
		if(root.right != null && root.right.mins == null) update(root.right);
		updateMin(root);
		return root;
	}
	
	public Conditional<Integer> pollMin() {
		throw new UnsupportedOperationException();
	}
	
	public Conditional<Integer> peekMin() {
		throw new UnsupportedOperationException();
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
	
	private void pollHelper(RBNode root, final FeatureExpr ctx, int i) {
		if(root == nullNode) return;
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
	
	public Conditional<Boolean> isEmpty(FeatureExpr e) {
		if(root.right.mins.equals(One.NULL)) return new One<>(true);
		return new One<>(false);
	}
}

