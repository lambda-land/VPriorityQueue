package gov.nasa.jpf.vm.va;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gov.nasa.jpf.vm.va.IStackHandler.Type;

import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class Main {
	public static void main(String[] args) {
		VStackTest();
	    /*StackHandlerFactory.activateHybridStackHandler();
		IStackHandler stack = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 2);
		//IVStack stack = new ConditionalStack(2);

		FeatureExpr f1 = FeatureExprFactory.createDefinedExternal("f1" + System.currentTimeMillis());

		Conditional<Integer> n1 = One.valueOf((int) (Math.random() * 10 + 1));
		stack.push(FeatureExprFactory.True(), n1, true);
		assertEquals(1, stack.getStackWidth());
		Conditional<Integer> n2 = new One<>((int) (Math.random() * 1000));
		stack.push(f1, n2, false);
		stack.push(f1.not(), n2, false);
		if (!(stack instanceof BufferedStackHandler)) {
			System.out.println(stack);
			System.out.println("width " + stack.getStackWidth());
		}*/
	}
	public static void VStackTest() {
		StackFactory.activateCStack();
	    IStackHandler t = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 2);
	    
	    FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
	    FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
	    FeatureExpr c = FeatureExprFactory.createDefinedExternal("c");
	    t.push(a, new One(2), false);
	    System.out.println(t.getTop());
	    
	    StackFactory.activateVStack();
	    IStackHandler t1 = StackHandlerFactory.createStack(FeatureExprFactory.True(), 0, 2);
	    t1.push(a, new One(2), false);
	    System.out.println(t1.getTop());
	}
}