package gov.nasa.jpf.vm.va;
import java.util.*;
import cmu.conditional.*;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.va.Stack;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
/**
 * Interface for stacks
 * @author Meng Meng
 *
 */
public interface IVStack {
	
	public abstract void init(Stack st);
		
	public abstract int getStackWidth();
	
	public FeatureExpr getCtx();
	
	public void setCtx(FeatureExpr ctx);
	
	public abstract boolean hasAnyRef(FeatureExpr ctx);
	
	public abstract boolean isRefLocal(FeatureExpr ctx, final int index);
	
	public abstract Conditional<Integer> getTop();
	
	public abstract int[] getSlots(FeatureExpr ctx);
	
	public abstract Set<Integer> getAllReferences();
	
	public abstract Conditional<Integer> getInteger(final FeatureExpr ctx, final int index);
	
	public abstract void clear(FeatureExpr ctx);
	
	public abstract void push(final FeatureExpr ctx, final Object value, final boolean isRef);
	
    public void pushEntry(final FeatureExpr ctx, final Conditional<Entry> value);
	
	public abstract Conditional<Entry> popEntry(final FeatureExpr ctx, final boolean copyRef);
	
	public void pop(FeatureExpr ctx, final int n);
	
	public <T> Conditional<T> pop(final FeatureExpr ctx, final Type t); 
	
	public abstract <T> Conditional<T> peek(FeatureExpr ctx, final int offset, final Type t);
	
	public abstract void setRef(final FeatureExpr ctx, final int index, final boolean ref);
		
	//public abstract <T> void remove(final FeatureExpr ctx);
		
	public abstract boolean isRef(FeatureExpr ctx, int offset);
		
	public abstract  void set(FeatureExpr ctx, final int offset, final int value, final boolean isRef);
	
	public abstract void setTop(final FeatureExpr ctx, final int i);
	
	public abstract boolean equals(Object o); 
	
	public abstract IVStack clone();
	
	public abstract Conditional<Stack> getStack();
		
	/**
	 * .. A B => .. B A B
	 */
	public abstract void dup_x1(FeatureExpr ctx);

	/**
	 * .. A B C D => .. C D A B C D
	 */
	public abstract void dup2_x2(FeatureExpr ctx);

	/**
	 * .. A B C => .. B C A B C
	 */
	public abstract void dup2_x1(FeatureExpr ctx);

	/**
	 * .. A B => .. A B A B
	 */
	public abstract void dup2(FeatureExpr ctx);

	/**
	 * .. A => .. A A
	 */
	public abstract void dup(FeatureExpr ctx);

	/**
	 * .. A B C => .. C A B C
	 */
	public abstract void dup_x2(FeatureExpr ctx);

	/**
	 * .. A B => .. B A
	 */
	public abstract void swap(FeatureExpr ctx);
	
	public String toString();
}
