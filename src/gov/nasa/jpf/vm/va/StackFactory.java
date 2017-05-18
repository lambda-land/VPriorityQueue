package gov.nasa.jpf.vm.va;

import java.util.ArrayList;
import java.util.List;

import de.fosd.typechef.featureexpr.FeatureExpr;
/**
 * 
 * @author Meng Meng
 *
 */
public class StackFactory {
  public enum VSFactory {
    CStack, VStack
  }
  static VFactory f = new CStackFactory();
  //static VFactory f = new VStackFactory();

  public static List<Object> asParameter() {
		List<Object> factorys = new ArrayList<>();
		for (Object f : VSFactory.values()) {
			factorys.add(f);
		}
		return factorys;
	}
  
  public static void setFactory(VSFactory factory) {
		switch (factory) {
		case CStack:
			activateCStack();
			break;
		case VStack:
			activateVStack();
			break;
		default:
			throw new RuntimeException(factory + " not supported");
		}
		
  }

	public static VFactory getCurrent() {
		return f;
	}

	public static IVStack createVStack(int nOperands) {
		return f.createVStack(nOperands);
	}
	

	public static IVStack createVStack() {
		return f.createVStack();
	}

  public static void activateCStack() {
		f = new CStackFactory();
	}

	public static void activateVStack() {
		f = new VStackFactory();
	}
}

interface VFactory {
	IVStack createVStack(int nOperands);
	IVStack createVStack();
}

class CStackFactory implements VFactory {
  public IVStack createVStack() {
	  return new ConditionalStack(0);
  }
  
  public IVStack createVStack(int nOperands){
	  return new ConditionalStack(nOperands);
  }
}


class VStackFactory implements VFactory {
  public IVStack createVStack() {
	  return new VStack(0);
  }
  
  public IVStack createVStack(int nOperands){
	  return new VStack(nOperands);
  }
}




