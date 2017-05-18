package gov.nasa.jpf.vm.va;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cmu.conditional.Function;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;



class VPoint implements Comparable<VPoint>{
	public static final Comparator<VPoint> Y_ORDER = new YOrder();
	public Comparator<VPoint> POLAR_ORDER;
	public FeatureExpr ctx;
	public Point2D point2D;
	public VPoint(FeatureExpr ctx, Point2D p) {
		this.ctx = ctx;
		this.point2D = p;
		POLAR_ORDER = new PolarOrder();
	}
	@Override
	public int compareTo(VPoint o) {
		return point2D.compareTo(o.point2D);
	}
	
	private static class YOrder implements Comparator<VPoint> {
		public int compare(VPoint p, VPoint q) {
			if (p.point2D.y < q.point2D.y)
				return -1;
			if (p.point2D.y > q.point2D.y)
				return +1;
			return 0;
		}
	}

	private class PolarOrder implements Comparator<VPoint> {
		private Comparator<Point2D> c = point2D.POLAR_ORDER;
		public int compare(VPoint q1, VPoint q2) {
			return c.compare(q1.point2D, q2.point2D);
		}
	}
	
	@Override
	public String toString() {
		return ctx.toString() + " " + point2D.toString();
	}
}


public class VGrahamScan {

	public static GStack<Point2D> hull = new GStack<>(5);
	
	
	public static FeatureExpr isSamePoint(VPoint p1, VPoint p2) {
		if(!p1.point2D.equals(p2.point2D)) {
			return FeatureExprFactory.False();
		} 
		return p1.ctx.and(p2.ctx);
	}
	
	public static Conditional<Double> ccw(final Conditional<Point2D> a, final Conditional<Point2D> b, final Conditional<Point2D> c) {
		
		Conditional<Double> ret = a.mapfr(null, new BiFunction<FeatureExpr, Point2D, Conditional<Double>>() {
			public Conditional<Double> apply(FeatureExpr f1, final Point2D a) {
				if(a == null) return (Conditional<Double>) One.NULL;
				return b.mapfr(null, new BiFunction<FeatureExpr, Point2D, Conditional<Double>>() {
					public Conditional<Double> apply(FeatureExpr f, final  Point2D b) { 
						if(b == null) return (Conditional<Double>) One.NULL;
						return c.mapfr(null, new BiFunction<FeatureExpr, Point2D, Conditional<Double>>() {
							public Conditional<Double> apply(FeatureExpr ctx, Point2D c) {
								if(c == null) return (Conditional<Double>) One.NULL;
								//double tmp = a.x()+ b.x() + c.x();
								double tmp = (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
								return new One<>(tmp);
							}
						});
					}
				});
			}
		}).simplify();
		//System.out.println("ccw " + a + "\n" + b + "\n" + c + "\nret " + ret);

		return ret;
	}
	
	
	public static Conditional<Double> ccw_construct(VPoint a, VPoint b, VPoint c) {
		return ChoiceFactory.create(a.ctx.and(b.ctx.and(c.ctx)),new One((b.point2D.x - a.point2D.x) * 
				(c.point2D.y - a.point2D.y) - (b.point2D.y - a.point2D.y) * (c.point2D.x - a.point2D.x)), 
				(Conditional<Double>) One.NULL).simplify(a.ctx.and(b.ctx.and(c.ctx)));
	}
	

	private static Conditional<Point2D> convert(VPoint v) {
		return ChoiceFactory.create(v.ctx, new One(v.point2D), (Conditional<Point2D>)One.NULL);
	}
	
	private static VPoint convert(Conditional<Point2D> v) {
	      Map<Point2D, FeatureExpr> map = v.toMap();
	      if(map.size() == 1) {
	    	  for(Map.Entry<Point2D, FeatureExpr> entry: map.entrySet())
	    	  return new VPoint(entry.getValue(), entry.getKey());
	      }
	      return null;
	}
	
	private static void CH(final VPoint[] VPointsArr) {
		int N = VPointsArr.length;
		
		Arrays.sort(VPointsArr);		
		Arrays.sort(VPointsArr, 1, N, VPointsArr[0].POLAR_ORDER);
		
		Conditional<Integer> k0 = (Conditional<Integer>) One.NEG_ONE, k1 = (Conditional<Integer>) One.NEG_ONE, k2 = (Conditional<Integer>) One.NEG_ONE;
		for(int _i = 0; _i < N; ++_i) {
			final int i = _i;
			FeatureExpr now = VPointsArr[i].ctx;
			System.out.println("i " + i + " " + VPointsArr[i]);
			FeatureExpr kf0 = k0.getFeatureExpr(-1);
			FeatureExpr kf0And = kf0.and(now);
			if(!kf0And.isContradiction()) {
				hull.push(kf0And, VPointsArr[i].point2D);
				//System.out.println("k0  " + k0);
				k0 = k0.mapfr(kf0And, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
					public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
						//System.out.println(ctx + " " + y);
						if(ctx.isContradiction()) {
							return One.valueOf(y);
						} else {
							return ChoiceFactory.create(ctx, One.valueOf(i), One.valueOf(y));
						}
					}
				}).simplify();
				//System.out.println("k0  " + k0);

			}
			System.out.println("k0  " + k0);
			now = now.andNot(kf0And);
			if(now.isContradiction()) continue;

			//System.out.println("now " + now);
			//System.out.println("k1  " + k1);

			FeatureExpr kf1 = k1.getFeatureExpr(-1);
			FeatureExpr kf1And = kf1.and(now);
			if(!kf1And.isContradiction()) {
				Conditional<Boolean> ke = k0.mapfr(VPointsArr[i].ctx, new BiFunction<FeatureExpr, Integer, Conditional<Boolean>>() {
					@Override
					public Conditional<Boolean> apply(FeatureExpr ctx, Integer y) {
						if(ctx.isContradiction() || y.intValue() == -1) {
							return One.FALSE;
						}
						if(VPointsArr[y.intValue()].point2D.equals(VPointsArr[i].point2D)) {
							return One.FALSE;
						} else {
							return One.TRUE;
						}
					}
				}).simplify();
				final FeatureExpr kef = ke.getFeatureExpr(Boolean.TRUE);
				if(!kef.isContradiction()) {
					k1 = k1.mapfr(kf1And, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
						public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
							if(ctx.isContradiction() || ctx.and(kef).isContradiction()) {
								return One.valueOf(y);
							} else {
								return ChoiceFactory.create(ctx.and(kef), One.valueOf(i), One.valueOf(y));
							}
						}
					}).simplify();
				}
			}
			
			
			
			
			
			now = now.andNot(kf1);
			
			System.out.println("now " + now);
			System.out.println("k1  " + k1);
			
			if(now.isContradiction()) continue;

			


			FeatureExpr kf2 = k2.getFeatureExpr(-1);
			FeatureExpr kf2And = kf2.and(now);
			System.out.println("kf2And " + kf2And);

			if(!kf2And.isContradiction()) {
				Conditional<Point2D> p0, p1, p2 = new One(VPointsArr[i].point2D);
				
				p0 = k0.simplify(kf2And).map(new Function<Integer, Point2D>() {
					@Override
					public Point2D apply(Integer x) {
						return VPointsArr[x].point2D;
					}
				});
				
				p1 = k1.simplify(kf2And).map(new Function<Integer, Point2D>() {
					@Override
					public Point2D apply(Integer x) {
						return VPointsArr[x].point2D;
					}
				});
				
				Conditional<Double> cd = ccw(p0, p1, p2);
				
				Conditional<Boolean> cde = cd.map(new Function<Double, Boolean>() {
					@Override
					public Boolean apply(Double x) {
						return new Boolean(x != 0);
					}
				});
				
				//Conditional<Integer> k2tmp = One.NEG_ONE;
				
				
				//update k1				
				FeatureExpr sf = cde.getFeatureExpr(Boolean.FALSE).and(kf2And);
				if(!sf.isContradiction()) {
					//final Conditional<Integer> fk2 = k2tmp;
					k1 = k1.mapfr(sf, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
						public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
							if(ctx.isContradiction()) return One.valueOf(y);
							return ChoiceFactory.create(ctx, One.valueOf(i), One.valueOf(y));
						}
					}).simplify(); 
					//System.out.println("sf  " + sf);
					//System.out.println("update k1 " + k1);
					/*
					k2tmp = k2tmp.mapfr(sf, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
						public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
							if(ctx.isContradiction()) return One.valueOf(y);
							return ChoiceFactory.create(ctx, One.valueOf(i), One.valueOf(y));
						}
					});
					*/
				}
				
				//push k1
				FeatureExpr cdef = cde.getFeatureExpr(Boolean.TRUE).and(kf2And);
				if(!cdef.isContradiction()) {
					
					Conditional<Point2D> pk1 = k1.simplify(cdef).map(new Function<Integer, Point2D>() {
						public Point2D apply(Integer x) {
							return VPointsArr[x].point2D;
						}
						
					}).simplify();
					
					//System.out.println("cdef  " + cdef);
					//System.out.println("pk1 " + pk1);
					
					hull.push(cdef, pk1);
					
					//System.out.println("cdef " + cdef);
					//System.out.println("---hull---\n" + hull + "------");
					
					k2 = k2.mapfr(cdef, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
						public Conditional<Integer> apply(FeatureExpr ctx, Integer y) {
							if(ctx.isContradiction()) return One.valueOf(y);
							return ChoiceFactory.create(ctx, One.valueOf(i), One.valueOf(y));
	
						}
					});
				}
				now = now.andNot(kf2).or(cdef.and(kf2));
				
			}
			System.out.println("k2 " + k2);

			System.out.println("now " + now);

			if(now.isContradiction()) continue;

			//System.out.println("now " + now);
			//System.out.println("------");
			//System.out.println(k0);
			//System.out.println(k1);
			//System.out.println(k2);
			
			//System.out.println("--hull--");
			//System.out.println(hull);

			Conditional<Point2D> top = hull.pop(now);
			System.out.println("top  " + top);
			System.out.println("---hull---\n" + hull + "------");
			//System.out.println("isEmpty " + hull.isEmpty(now));

			Conditional<Point2D> pi = new One(VPointsArr[i].point2D);
			while((!now.isContradiction()) && (!hull.isEmpty(now).isSatisfiable())) {
				final Conditional<Point2D>  pk = hull.peek(now);
				Conditional<Double> cd = ccw(pk, top, pi).simplify(now);
				Conditional<Boolean> cde = cd.mapfr(now, new BiFunction<FeatureExpr, Double, Conditional<Boolean>>() {
					@Override
					public Conditional<Boolean> apply(FeatureExpr ctx, Double x) {
						//System.out.println("pk " + ctx + " " + x);
						if(x == null) return (Conditional<Boolean> )One.NULL;
						return One.valueOf(x <= 0);
					}
				});
				
				FeatureExpr cdef = cde.getFeatureExpr(Boolean.FALSE);
				if(!cdef.and(now).isContradiction()) {
					hull.push(cdef.and(now), top);
					hull.push(cdef.and(now), pi);
				}
				
				
				now = now.andNot(cdef);
				top = hull.pop(now);

				
				
				//System.out.println("top " + top);
				//System.out.println("pk " + pk);
				//System.out.println("cd " + cd);
				//System.out.println("cde  " + cde);

				//System.out.println("while cdef " + cdef);

				System.out.println("now " + now + " " + hull.isEmpty(now));
				System.out.println("---\n" + hull);

			}
		}
	}
	public static void VPointTrans (List<VPoint> points, Conditional<Point2D>[] inputPoint) {
		for(int i = 0; i < inputPoint.length; i++) {
			//System.out.println(i);
			Map<Point2D, FeatureExpr> map  = inputPoint[i].toMap();
			for(Map.Entry<Point2D, FeatureExpr> m : map.entrySet()) {
				points.add(new VPoint(m.getValue(), m.getKey()));
			}
		}
	}
	
	public static void init(List<VPoint> points, VPoint[] VPointsArr) {
		for(int i = 0; i < points.size(); i++) {
			VPointsArr[i] = points.get(i);
		}
	}
	
	public static void print(VPoint[] VPointsArr) {
		for(int i = 0; i < VPointsArr.length; i++) {
			System.out.println(VPointsArr[i]);
		}
	}
	
	
	public static void computeVGS(VPoint[] VPointsArr) {
		int N = VPointsArr.length;
		Arrays.sort(VPointsArr);
		//Arrays.sort(VPointsArr, 0, N, VPoint.Y_ORDER);
		
		Arrays.sort(VPointsArr, 1, N, VPointsArr[0].POLAR_ORDER);
		print(VPointsArr);
		CH(VPointsArr);
		System.out.println(hull);
	}
	
	
	public static void main(String[] args) {
		//System.setProperty("FEATUREEXPR", "BDD");
		//ChoiceFactory.activateMapChoice();
		FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
		FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
		FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");
		
//		GStack<Point2D> test = new GStack<>(5);
//		test.push(a, new Point2D(3,2));
//		System.out.println(test);
	
		
		Conditional<Point2D>[] inputPoint = new Conditional[4];
		//test1
		Conditional<Point2D> p0 = ChoiceFactory.create(a, new One<>(new Point2D(0, 0)), new One<>(new Point2D(0, 0)));
		Conditional<Point2D> p1 = ChoiceFactory.create(b, new One<>(new Point2D(1, 1)), new One<>(new Point2D(1, 1)));
		Conditional<Point2D> p2 = ChoiceFactory.create(c, new One<>(new Point2D(0, 1)), new One<>(new Point2D(1, 4)));
		Conditional<Point2D> p4 = ChoiceFactory.create(c, new One<>(new Point2D(-1, 3)), new One<>(new Point2D(-1, 3)));
		inputPoint[0] = p0;
		inputPoint[1] = p1;
		inputPoint[2] = p2;
		inputPoint[3] = p4;
		
		
		// test2
//		Conditional<Point2D> p0 = ChoiceFactory.create(a, new One<>(new Point2D(0, 0)), new One<>(new Point2D(3, 3)));
//		Conditional<Point2D> p1 = ChoiceFactory.create(a, new One<>(new Point2D(1, 1)), new One<>(new Point2D(4, 4)));
//		Conditional<Point2D> p2 = ChoiceFactory.create(a, new One<>(new Point2D(1, 4)), new One<>(new Point2D(1, 7)));
//		Conditional<Point2D> p4 = ChoiceFactory.create(a, new One<>(new Point2D(-1, 3)), new One<>(new Point2D(0, 5)));
//		inputPoint[0] = p0;
//		inputPoint[1] = p1;
//		inputPoint[2] = p2;
//		inputPoint[3] = p4;
		
		
		
		//test3
		
//		Conditional<Point2D> p0 = ChoiceFactory.create(a, new One<>(new Point2D(3, 3)), new One<>(new Point2D(4, 2)));
//		Conditional<Point2D> p1 = ChoiceFactory.create(a, new One<>(new Point2D(3, 5)), new One<>(new Point2D(5, 1)));
//		Conditional<Point2D> p2 = ChoiceFactory.create(a, new One<>(new Point2D(0, 1)), new One<>(new Point2D(-5,1)));
//		Conditional<Point2D> p3 = ChoiceFactory.create(a, new One<>(new Point2D(-2, 2)), new One<>(new Point2D(3, -2)));
//		Conditional<Point2D> p4 = ChoiceFactory.create(a, new One<>(new Point2D(2, 5)), new One<>(new Point2D(0, 0)));
//		Conditional<Point2D> p5 = ChoiceFactory.create(b, new One<>(new Point2D(-3, 2)), new One<>(new Point2D(0, 5)));
//		inputPoint[0] = p0;
//		inputPoint[1] = p1;
//		inputPoint[2] = p2;
//		inputPoint[3] = p3;
//		inputPoint[4] = p4;
//		inputPoint[5] = p5;
		
		//init
		
		List<VPoint> points = new ArrayList<VPoint>();
		for(int i = 0; i < inputPoint.length; i++) {
			//System.out.println(i);
			Map<Point2D, FeatureExpr> map  = inputPoint[i].toMap();
			for(Map.Entry<Point2D, FeatureExpr> m : map.entrySet()) {
				points.add(new VPoint(m.getValue(), m.getKey()));
			}
		}
		//System.out.println(points.size());
		
		Conditional<Point2D>[] pointsWithCtx = new Conditional[points.size()]; 
		VPoint[] VPointsArr = new VPoint[points.size()];
		for(int i = 0; i < points.size(); i++) {
			VPointsArr[i] = points.get(i);
			//System.out.println(i + " " + points.get(i));
			pointsWithCtx[i] = ChoiceFactory.create(points.get(i).ctx, new One<>(points.get(i).point2D), (Conditional<Point2D>) One.NULL);
		}
		
//		System.out.println(pointsWithCtx[0]);
//		System.out.println(pointsWithCtx[1]);
//		System.out.println(pointsWithCtx[2]);
//		
//		System.out.println("1 " + ccw(pointsWithCtx[0], pointsWithCtx[2], pointsWithCtx[4]));
//		System.out.println("2 " +ccw_construct(VPointsArr[0], VPointsArr[2], VPointsArr[4]));
		/*
		VPoint a1 = VPointsArr[0];
		VPoint a2 = VPointsArr[2];
		VPoint a5 = VPointsArr[5];
		VPointsArr = new VPoint[3];
		VPointsArr[0] = a1;
		VPointsArr[1] = a2;
		VPointsArr[2] = a5;
		*/
		
		int N = VPointsArr.length;
		Arrays.sort(VPointsArr);
		//Arrays.sort(VPointsArr, 0, N, VPoint.Y_ORDER);
		
		Arrays.sort(VPointsArr, 1, N, VPointsArr[0].POLAR_ORDER);
		print(VPointsArr);
		CH(VPointsArr);
		
		/*
		hull.push(VPointsArr[0].ctx, VPointsArr[0].point2D);
		FeatureExpr cur = VPointsArr[0].ctx;
		int k1;
		for(k1 = 1; k1 < N; k1++) {
			if(cur.and(VPointsArr[k1].ctx).equivalentTo(FeatureExprFactory.False())) {
				continue;
			} else {
				hull.push(VPointsArr[k1].ctx, VPointsArr[k1].point2D);
				cur.and(VPointsArr[k1].ctx);
				break;
			}
		}
		
		int k2;
		for(k2 = k1 + 1; k2 < N; k2++) {
			if(cur.and(VPointsArr[k2].ctx).equivalentTo(FeatureExprFactory.False())) {
				continue;
			} else {
				Conditional<Point2D> top = hull.pop(cur);
				
				while (ccw_construct(convert(hull.peek(cur)), convert(top), VPointsArr[k2]).simplify(cur.and(VPointsArr[k2].ctx)).getValue() <= 0) {
					top = hull.pop(cur);
				}
				hull.push(cur, top);
				hull.push(cur, convert(VPointsArr[k2]));
			}
		}
		*/
		System.out.println(hull);
		
	}
	
}
