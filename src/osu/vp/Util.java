package osu.vp;

/**
 * @author Meng Meng 
 */


import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class Util {
	public static Conditional<Integer> vmin(Conditional<Integer> num1, final Conditional<Integer> num2) {
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

	
	public static Conditional<Integer> vmin(Conditional<Integer> num, final FeatureExpr ctx, final int v) {
		if(num == null) return ChoiceFactory.create(ctx, One.valueOf(v), (Conditional<Integer>)One.NULL);
		
		Conditional<Integer> ret = num.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				if(a == null) {
					if(f.isContradiction()) return (Conditional<Integer>)One.NULL;
					if(f.isTautology()) return One.valueOf(v);
					return ChoiceFactory.create(f, One.valueOf(v), (Conditional<Integer>)One.NULL).simplify();
				}
				
				if(f.isContradiction() || a <= v) return One.valueOf(a);
				if(f.isTautology()) return One.valueOf(v);
				return ChoiceFactory.create(f, One.valueOf(v), One.valueOf(a)).simplify();
			}
		}).simplify();
		return ret;
	}
	public static void main(String[] args) {
		FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
		FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
		@SuppressWarnings("unchecked")
		Conditional<Integer> v1 = ChoiceFactory.create(a, new One<>(2), (Conditional<Integer>)One.NULL);
		@SuppressWarnings("unchecked")
		Conditional<Integer> v2 = ChoiceFactory.create(b, new One<>(5), (Conditional<Integer>)One.NULL);
		System.out.println(vmin(v1, v2));
	}
}
