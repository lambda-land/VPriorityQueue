package osu.vp;
import cmu.conditional.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import de.fosd.typechef.featureexpr.*;

import gov.nasa.jpf.vm.va.Stack;

public class Test {
	public static Conditional<Integer>  vmin(Conditional<Integer> num1, final Conditional<Integer> num2) {
		Conditional<Integer> mins = num1.mapfr(null, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
			@Override
			public Conditional<Integer> apply(FeatureExpr f, final Integer a) {
				return num2.map(new Function<Integer, Integer>(){
					@Override
					public Integer apply(final Integer b) {
						if(a < b) return a;
						else return b;
					}
				}); 
			}
		});
		return mins;
	}
	public static void main(String[] args) {
		FeatureExprFactory.setDefault(FeatureExprFactory.bdd());
		FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
		FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
		FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");
		System.out.println(a.and(b).simplify(c));
		Conditional<Integer> p0 = ChoiceFactory.create(a, new One<>(1), new One<>(4));
		Conditional<Integer> p1 = ChoiceFactory.create(b, new One<>(3), new One<>(2));
		System.out.println(vmin(p0, p1));
	}
}
