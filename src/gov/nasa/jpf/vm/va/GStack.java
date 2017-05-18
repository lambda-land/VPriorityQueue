package gov.nasa.jpf.vm.va;

import cmu.conditional.BiFunction;
import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import java.util.*;

import javax.management.RuntimeErrorException;

import cmu.conditional.*;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import de.fosd.typechef.conditional.Choice;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.Types;
/**
 * choice of stack implementation.
 * @author Meng Meng
 *
 */
class GStack<T>{
    public int size;
    public Conditional<T>[] slots;
    Conditional<Integer> top;
    
    public GStack() {
        size = -1;
        slots = (Conditional<T>[]) new Conditional[0];
        top = new One<>(0);
    }

    public GStack(int nOperands) {
        size = -1;
        slots = (Conditional<T>[]) new Conditional[nOperands];
        top = new One<>(0);
    }
    

    /**
     * resize, cleanup and fillholes
     */

    private void resize() {
        simpleCleanup();
        Conditional<T>[] tmp;
        // while(size >= 0 && slots[size].equals(One.NULL)) size--;
        if (size >= slots.length - 1) {
            tmp = (Conditional<T>[]) new Conditional[(size + 2) * 2];
            for (int i = 0; i <= size; ++i) {
                tmp[i] = slots[i];
            }
            slots = tmp;
            return;
        }

        if (slots.length > 1000 && size < slots.length / 4) {
            tmp = (Conditional<T>[]) new Conditional[slots.length / 2];
            for (int i = 0; i <= size; ++i) {
                tmp[i] = slots[i];
            }
            slots = tmp;
            return;
        }

    }

    private void cleanup() {
        int i = 0, j = 0;
        while (i <= size) {
            // System.out.println("remove " + slots[i] + " " +
            // slots[i].equals(One.NULL));
            //if(slots[i].getFeatureExpr(null).orNot(stackCTX).isTautology()) {
            if (slots[i].equals(One.NULL)) {
                if (j <= i)
                    j = i + 1;
                //while (j <= size && slots[j].getFeatureExpr(null).orNot(stackCTX).isTautology()) {
                while (j <= size && slots[j].equals(One.NULL)) {
                    j++;
                }
                if (j > size) {
                    size = i - 1;
                    break;
                } else {
                    slots[i++] = slots[j];
                    slots[j++] = (Conditional<T>) One.NULL;
                }
            } else {
                i++;
            }
        }
    }

    private void simpleCleanup() {
        while (size >= 0 && slots[size].equals(One.NULL)) {
        //while (size >= 0 && slots[size].getFeatureExpr(null).orNot(stackCTX).isTautology()) {
            --size;
        }
    }


    public void fillholes(FeatureExpr ctx) {
        for (int i = size; i >= 0; --i)
            fillholes(ctx, i);
        return;
    }

    private void fillholes(final FeatureExpr ctx, final int count) {
        if (count == -1) {
            return;
        }
        FeatureExpr hole = slots[count].getFeatureExpr(null).and(ctx);
        if (hole.isContradiction()) return;
        slots[count] = ChoiceFactory.create(hole, popTHelper(hole, count - 1), slots[count]).simplify();
        return;
    }


    
    /*
     * top 
     */
    private int topSize_tmp;
    private <T> Conditional<Integer> topSize(final FeatureExpr ctx, final int count) {   
        if (count == -1)
            return One.NEG_ONE;
        Conditional<Integer> tmp;
     
        topSize_tmp = 0;
        top = One.NEG_ONE;
        for (int i = 0; i <= count; i++) {
            final int j = i;
            if (slots[i].equals(One.NULL))
                continue;
            tmp = ChoiceFactory.create(slots[i].getFeatureExpr(null), One.ZERO, One.ONE).simplify(ctx);
            if (tmp.equals(One.ZERO))
                continue;
            if (tmp.equals(One.ONE)) {
                ++topSize_tmp;
                continue;
            }
            top = top.fastApply(tmp, new BiFunction<Integer, Integer, Conditional<Integer>>() {
            public Conditional<Integer> apply(Integer x, Integer y) {
                return new One<>(x + y);
            }
            });
        }
        
        if(topSize_tmp == 0) return top;
        
        return top.map(new Function<Integer, Integer>() {
            public Integer apply(Integer x) {
                return x + topSize_tmp;
            }
        });
    }
    

    public Conditional<Integer> getTop() {
        //printStack();
        return topSize(FeatureExprFactory.True(), size);
    }
    
    private void pushTrueT(final Conditional<T> value) {
        //resize();
        slots[++size] = value;
    }
    
    public void push(FeatureExpr ctx, T value) {
    	push(ctx, new One(value));
    }
    
    public void push(FeatureExpr ctx, Conditional<T> value) {
        resize();
        if(ctx.isTautology()) {
            pushTrueT(value);
            return;
        }
        if(size == -1) {
            slots[++size] = ChoiceFactory.create(ctx, value, (Conditional<T>) One.NULL);
            return;
        }
        
        FeatureExpr topNull = slots[size].getFeatureExpr(null);
        if(topNull.isContradiction()) {
            slots[++size] = ChoiceFactory.create(ctx, value, (Conditional<T>) One.NULL);
            return;
        }
        
        FeatureExpr valueNull = value.getFeatureExpr(null).and(ctx).orNot(ctx);
        if(valueNull.or(topNull).isTautology()) {
            slots[size] = ChoiceFactory.create(ctx, value, slots[size]).simplify();
            return;
        }
                
        slots[++size] = ChoiceFactory.create(ctx, value, (Conditional<T>) One.NULL);
    }
    
    /*
     * pop operations 
     * 
     * 
     */
    /*
    private void popHelper(FeatureExpr ctx) {
        Conditional<T>[] tmp;
        for(int i = size; i >= 0; i--) {
            tmp = slots[i].split(ctx);
            FeatureExpr and = tmp[0].getFeatureExpr(null).and(ctx);
            if(!ctx.equivalentTo(and)) {
               if(ctx.isTautology()) {
                    slots[i] = (Conditional<T>) One.NULL; 
                } else if(ctx.isContradiction()) {
                    slots[i] = tmp[1].simplify();
                } else {
                    slots[i] = ChoiceFactory.create(ctx.not(), tmp[1], (Conditional<T>)One.NULL).simplify();
                } 
                ctx = and;
            }
          
            if(ctx.isContradiction()) break;
        }
    }
    */
    
    public FeatureExpr isEmpty(final FeatureExpr ctx) {
    	Conditional<T> i = peek(ctx);
    	return i.getFeatureExpr(null);
    }
    
    public Conditional<T> pop(final FeatureExpr ctx) {
        Conditional<T> res = popTHelper(ctx, size);
        cleanup();
        return res;
    }

    private Conditional<T> popTHelper(FeatureExpr ctx, int count) {
        if (count == -1) return (Conditional<T>) One.NULL;
        Conditional<T>[][] tmp = new Conditional[count + 2][];
        FeatureExpr[] fs = new FeatureExpr[count + 2];
        FeatureExpr and = null;
        Conditional<T> ret;
        fs[count + 1] = ctx;
        
        int i;
        for (i = count; i >= 0; --i) {
            tmp[i] = slots[i].split(fs[i + 1]);
            and = tmp[i][0].getFeatureExpr(null).and(fs[i + 1]);
            if (!fs[i + 1].equivalentTo(and)) {
                if (fs[i + 1].isTautology()) {
                    slots[i] = (Conditional<T>) One.NULL;
                } else if (fs[i + 1].isContradiction()) {
                    slots[i] = tmp[i][1].simplify();
                } else {
                    slots[i] = ChoiceFactory.create(fs[i + 1].not(), tmp[i][1], (Conditional<T>) One.NULL).simplify();
                }
            }
            fs[i] = and;
            if (and.isContradiction())
                break;
        }
      
        if (i == -1)
            i = 0;
        ret = tmp[i][0];
        

        for (int j = i + 1; j <= count; ++j) {
            //System.out.println(fs[j] + " " + ret + " " + tmp[j][0]);
            if (!tmp[j][0].equals(One.NULL))
                ret = ChoiceFactory.create(fs[j], ret, tmp[j][0].simplify(fs[j].not()));
        }
        return ret.simplify(ctx);
    }
    
  
    /**
     * peek 
     */
    public Conditional<T> peekHelper(final FeatureExpr ctx, final Conditional<Integer> offset) {
        fillholes(ctx);
        return offset.mapfr(ctx, new BiFunction<FeatureExpr, Integer, Conditional<T>>() {
        public Conditional<T> apply(FeatureExpr c, Integer offset) {
            if (Conditional.isContradiction(c) || offset == null) {
                return (Conditional<T>) One.NULL;
            } else {
                if (size - offset >= 0)
                    return slots[size - offset].simplify(c);
                else
                    return (Conditional<T>) One.NULL;
            }
        }
        }).simplify();
    }
    
    
    Conditional<T> peek(final FeatureExpr ctx) {
    	if(size == -1) return (Conditional<T>)One.NULL;
    	simpleCleanup();
    	fillholes(ctx, size);
    	cleanup();
        return slots[size].simplify(ctx);
    }
    
    
    

    public void clear(FeatureExpr ctx) {
        simpleCleanup();
        pop(ctx);
        cleanup();
    }
    
 

    public GStack<T> clone() {
        GStack<T> clone = new GStack<T>(slots.length);
        cleanup();
        clone.size = this.size;
        for (int i = 0; i <= size; i++) {
            clone.slots[i] = slots[i];
        }
        return clone;
    }


    
    private GStack<T> copy() {
        GStack<T> clone = new GStack<T>(slots.length);
        clone.size = this.size;
        for (int i = 0; i <= size; i++) {
            clone.slots[i] = slots[i];
        }
        return clone;
    }
    
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o instanceof GStack) {
            if (((GStack<T>) o).size == -1 && this.size == -1) {
                return true;
            }
            GStack<T> s = this.copy();
            GStack<T> t = ((GStack<T>) o).copy();
            while (s.size != -1 && t.size != -1) {
                Conditional<T> st = s.pop(FeatureExprFactory.True());
                Conditional<T> tt = t.pop(FeatureExprFactory.True());
                if (!st.equals(tt))
                    return false;
            }
            if (s.size == -1 && t.size == -1)
                return true;
        }
        return false;
    }
    
    public T[] toArray(FeatureExpr ctx) {
    	List<T> tmp = new ArrayList<T>();
    	for(int i = 0; i < slots.length; i++) {
    		if(slots[i] == null) continue;
    		Conditional<T> x = slots[i].simplify(ctx);
    		if(x instanceof One) {
    			if(x == null) continue;
    			if(x.getValue() != null)
    				tmp.add(x.getValue());
    		} else {
    			throw new UnsupportedOperationException("cannot support Choice");
    		}
    		
    	}
    	return (T[]) tmp.toArray();
    }

    public String toString() {
        String s = "";
        for (int i = 0; i <= size; i++) {
            s += slots[i].toString() + "\n";
        }
        return s;
        // return slots[0].toString();
    }

    public void printStack() {
        printStack(null);
    }
    
    private void printStack(String s) {
        if (s != null) {
            System.out.println(s);
        }
        for (int i = 0; i <= size; ++i) {
            System.out.println(slots[i]);
        }
    }

  
}