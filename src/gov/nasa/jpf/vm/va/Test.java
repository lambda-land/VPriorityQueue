package gov.nasa.jpf.vm.va;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class Test {
	
   public static Point2D[] vpiontToArray(FeatureExpr ctx, Conditional<Point2D>[] points) {
    	List<Point2D> tmp = new ArrayList<>();
    	for(int i = 0; i < points.length; i++) {
    		if(points[i] == null) continue;
    		Conditional<Point2D> x = points[i].simplify(ctx);
    		if(x instanceof One) {
    			if(x == null) continue;
    			if(x.getValue() != null)
    				tmp.add(x.getValue());
    		} else {
    			throw new UnsupportedOperationException("cannot support Choice");
    		}
    		
    	}
    	return tmp.toArray(new Point2D[tmp.size()]);
    }

	
	public static FeatureExpr nextCtx(int s, FeatureExpr[] options) {
		FeatureExpr ret = FeatureExprFactory.True();
		for(int i = 0; i < options.length; i++) {
			if((s&(1 << i)) != 0) {
				ret = ret.and(options[i]);
			} else {
				ret = ret.andNot(options[i]);
			}
		}
		return ret;
	}
	public static void main(String[] args) {
		Set<FeatureExpr> set = new HashSet<>();
		FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
		FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
		FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");
		FeatureExpr d = FeatureExprFactory.createDefinedExternal("d");
		FeatureExpr e = FeatureExprFactory.createDefinedExternal("e");
		FeatureExpr f = FeatureExprFactory.createDefinedExternal("f");
		FeatureExpr g = FeatureExprFactory.createDefinedExternal("g");
		FeatureExpr h = FeatureExprFactory.createDefinedExternal("h");
		set.add(a);
		set.add(b);
		set.add(c);
		set.add(d);
		set.add(e);
		set.add(f);
		set.add(g);
		//set.add(h);
		
		Conditional<Point2D>[] inputPoint = new Conditional[7];
		//test1
		Conditional<Point2D> p0 = ChoiceFactory.create(a, new One<>(new Point2D(0, 0)), new One<>(new Point2D(0, 0)));
		Conditional<Point2D> p1 = ChoiceFactory.create(b, new One<>(new Point2D(1, 1)), new One<>(new Point2D(1, 1)));
		Conditional<Point2D> p2 = ChoiceFactory.create(c, new One<>(new Point2D(0, 1)), new One<>(new Point2D(1, 4)));
		Conditional<Point2D> p3 = ChoiceFactory.create(d, new One<>(new Point2D(7, 4)), new One<>(new Point2D(3, 3)));
		Conditional<Point2D> p4 = ChoiceFactory.create(e, new One<>(new Point2D(-1, 3)), new One<>(new Point2D(-1, 3)));
		Conditional<Point2D> p5 = ChoiceFactory.create(f, new One<>(new Point2D(10, 0)), new One<>(new Point2D(4, 2)));
		Conditional<Point2D> p6 = ChoiceFactory.create(g, new One<>(new Point2D(15, 20)), new One<>(new Point2D(5, 1)));
		//Conditional<Point2D> p7 = ChoiceFactory.create(a, new One<>(new Point2D(-3, -2)), new One<>(new Point2D(-5, 1)));
	//	Conditional<Point2D> p8 = ChoiceFactory.create(e, new One<>(new Point2D(6, 5)), new One<>(new Point2D(0, 0)));
		inputPoint[0] = p0;
		inputPoint[1] = p1;
		inputPoint[2] = p2;
		inputPoint[3] = p3;
		inputPoint[4] = p4;
		inputPoint[5] = p5;
		inputPoint[6] = p6;
		//inputPoint[7] = p7;
		
		
		
		List<VPoint> points = new ArrayList<VPoint>();
		VGrahamScan.VPointTrans(points, inputPoint);
		VPoint[] VPointsArr = new VPoint[points.size()];
		VGrahamScan.init(points, VPointsArr);
		VGrahamScan.computeVGS(VPointsArr);

		FeatureExpr[] options = null;
		options = set.toArray(new FeatureExpr[set.size()]);
		//get all set
		
		for(int i = 0; i < Math.pow(2, options.length); i++) {
			FeatureExpr ctx = nextCtx(i, options);
			Object[] res = VGrahamScan.hull.toArray(ctx);
			GrahamScan graham = new GrahamScan(vpiontToArray(ctx, inputPoint));
			System.out.println("\ni = " + i + "");
			/*
			printPointArr(vpiontToArray(ctx, inputPoint));
			System.out.println("**************" + ctx + "**************");
			for(Object p : res) {
				System.out.println((Point2D)p);
			}
			System.out.println("---------------graham----------------");
			for (Point2D p : graham.hull()) {
				System.out.println(p);
			}
			System.out.println("---------------compare----------------");
			*/
			if(res.length != graham.hull.size()) {
				System.out.println("false, VG length");
				VGrahamScan.hull.printStack();
				//return;
			}
			Object[] gs = graham.hull.toArray();
			for(int k = 0; k < res.length; k++) {
				if(!((Point2D)res[k]).equals((Point2D)gs[k])) {
					System.out.println("false, VG points");
					//return;
				}
			}
		}
		
	}
	
	public static void printPointArr(Point2D [] s) {
		for(Point2D p : s) {
			System.out.println(p);
		}
	}
	
}
