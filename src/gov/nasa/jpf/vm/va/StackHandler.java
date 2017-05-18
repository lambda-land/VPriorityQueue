package gov.nasa.jpf.vm.va;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Types;

/**
 * Stack implementation where locals are separated from stack.<br>
 * Locals: Conditional[]<br>
 * Stack: Conditional -Stack-
 * 
 * @author Jens Meinicke
 *
 */
public class StackHandler implements Cloneable, IStackHandler {

  /** Locals are directly accessed with index **/
  protected Conditional<Entry>[] locals;

  //protected Conditional<Stack> stack;
  protected IVStack stack = StackFactory.createVStack();
  
  protected int length = 0;

  //public FeatureExpr stackCTX;
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#getStackWidth()
   */
  @Override
  public int getStackWidth() {
    return stack.getStackWidth();
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#toString()
   */
  @Override
  public String toString() {
    StringBuilder string = new StringBuilder();
    string.append("Locals: [");

    for (int i = 0; i < locals.length; i++) {
      if (locals[i] == null) {
        string.append("null");
      } else {
        string.append(locals[i]);
      }
      string.append(" ");
    }
    string.append("] \nStack: ");
    string.append(stack);
    return string.toString();
  }
  
  protected static final One<Entry> nullValue = new One<>(new Entry(MJIEnv.NULL, false)); 

  @SuppressWarnings("unchecked")
  public StackHandler(FeatureExpr ctx, int nLocals, int nOperands) {
    if (ctx == null) {
      // if loading class inside jetty, the ctx is null
      System.err.println("CAUTIOUS! CTX == NULL, creating True");
      ctx = FeatureExprFactory.True();
//      throw new RuntimeException("CTX == NULL");
    }
    length = nLocals + nOperands;
    locals = new Conditional[nLocals];
    Arrays.fill(locals, nullValue);
    
    //stack = new One<>(new Stack(nOperands));
    stack = StackFactory.createVStack(nOperands);
    stack.setCtx(ctx);
  }

  @SuppressWarnings("unchecked")
  public StackHandler() {
    locals = new Conditional[0];
    stack = StackFactory.createVStack();
    stack.setCtx(FeatureExprFactory.True());
  }
  
  
  @SuppressWarnings("unchecked")
  public StackHandler(FeatureExpr ctx, Stack st, Entry[] locals) {
    stack = StackFactory.createVStack();
    this.setCtx(ctx);
    stack.init(st);
    this.locals = new Conditional[locals.length];
    for (int i = 0; i < locals.length; i++) {
      this.locals[i] = new One<>(locals[i]);
    }
    length = st.slots.length + locals.length;
  }
  
  
  @Override
  public FeatureExpr getCtx() {
    return stack.getCtx();
  }
  
  @Override
  public void setCtx(FeatureExpr ctx) {
    stack.setCtx(ctx);
  }


  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#clone()
   */
  @Override
  @SuppressWarnings("unchecked")
  public StackHandler clone() {
    StackHandler clone = new StackHandler();
//    clone.setCtx(stackCTX); // TODO ThreadStopTest.testStopRunning() fails
    clone.length = length;
    clone.locals = new Conditional[locals.length];
    for (int i = 0; i < locals.length; i++) {
      Conditional<Entry> local = locals[i];
      if (local != null) {
        clone.locals[i] = local.map(CopyEntry);
      }
    }

    clone.stack = this.stack.clone();
    return clone;
  }
  
  private static final Function<Entry, Entry> CopyEntry = new Function<Entry, Entry>() {
    @Override
    public Entry apply(final Entry entry) {
      return entry.copy();
    }
  };
  

  /*
   * ############################################################
   * Handling of local variables
   * ############################################################
   */

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#pushLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public void pushLocal(final FeatureExpr ctx, final int index) {
    Conditional<Entry> value = locals[index];
    if (value == null) {
      value = new One<>(new Entry(0, false));
    }
    stack.pushEntry(ctx, value);

  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#pushLongLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public void pushLongLocal(FeatureExpr ctx, int index) {
    Conditional<Entry> value = locals[index];
    if (value == null) {
      value = new One<>(new Entry(0, false));
    }
    stack.pushEntry(ctx, value);

    value = locals[index + 1];
    if (value == null) {
      value = new One<>(new Entry(0, false));
    }
    stack.pushEntry(ctx, value);

  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#storeOperand(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public void storeOperand(final FeatureExpr ctx, final int index) {
    if (Conditional.isTautology(ctx)) {
      locals[index] = popEntry(ctx, true);
    } else {
      if (locals[index] == null) {
        locals[index] = new One<>(new Entry(MJIEnv.NULL, false));
      }
      locals[index] = ChoiceFactory.create(ctx, popEntry(ctx, true), locals[index]).simplify();
    }
  }

  private Conditional<Entry> popEntry(FeatureExpr ctx, final boolean copyRef) {
    return stack.popEntry(ctx, copyRef);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#storeLongOperand(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public void storeLongOperand(final FeatureExpr ctx, final int index) {
    /*
    stack.mapf(ctx, new VoidBiFunction<FeatureExpr, Stack>() {

      @Override
      public void apply(final FeatureExpr f, final Stack stack) {
        if (Conditional.isContradiction(f)) {
          return;
        }
        locals[index + 1] = ChoiceFactory.create(f, popEntry(f, false), locals[index + 1]);
        locals[index] = ChoiceFactory.create(f, popEntry(f, false), locals[index]);
      }
    });
    locals[index] = locals[index].simplify();
    locals[index + 1] = locals[index + 1].simplify();
    stack = stack.simplify();
    */
    locals[index + 1] = ChoiceFactory.create(ctx, popEntry(ctx, false), locals[index + 1]);
    locals[index] = ChoiceFactory.create(ctx, popEntry(ctx, false), locals[index]);
  
    locals[index] = locals[index].simplify();
    locals[index + 1] = locals[index + 1].simplify();
    
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#setLocal(de.fosd.typechef.featureexpr.FeatureExpr, int, cmu.conditional.Conditional, boolean)
   */
  @Override
  public void setLocal(FeatureExpr ctx, final int index, final Conditional<Integer> value, final boolean isRef) {
    value.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

      @Override
      public void apply(final FeatureExpr x, final Integer value) {
        setLocal(x, index, value, isRef);
      }

    });
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#setLocal(de.fosd.typechef.featureexpr.FeatureExpr, int, int, boolean)
   */
  @Override
  public void setLocal(final FeatureExpr ctx, final int index, final int value, final boolean isRef) {
    if (Conditional.isTautology(ctx)) {
      locals[index] = new One<>(new Entry(value, isRef));
    } else {
      if (locals[index] == null) {
        locals[index] = new One<>(new Entry(0, false));
      }
      locals[index] = ChoiceFactory.create(ctx, new One<>(new Entry(value, isRef)), locals[index]).simplify();
    }
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#getLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public Conditional<Integer> getLocal(FeatureExpr ctx, final int index) {
    if (index < 0) {
      return new One<>(-1);
    }
    if (index < locals.length) {
      if (locals[index] == null) {
        return One.MJIEnvNULL;
      }
      return locals[index].simplify(ctx).map(GetLocal).simplifyValues();
    } else {
      final int i = index - locals.length;
      return stack.getInteger(ctx, i);
    }
  }

  @Override
  public Object getLocal(int index) {
    return locals[index].simplify(getCtx());
  }
  
  private static final Function<Entry, Integer> GetLocal = new Function<Entry, Integer>() {
    @Override
    public Integer apply(final Entry x) {
      if (x == null) {
        return MJIEnv.NULL;
      }
      return x.value;
    }
  };

  // TODO change to conditional
  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#isRefLocal(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public boolean isRefLocal(FeatureExpr ctx, final int index) {
    if (index < 0) {
      return false;
    }

    if (index < locals.length) {
      if (locals[index] == null) {
        return false;
      }
      // TODO check calls of isRefLocal
      for (boolean b : locals[index].simplify(ctx).map(IsRefLocal).toList()) {
        if (b) {
          return true;
        }
      }
      return false;
//      return locals[index].simplify(ctx).map(new IsRefLocal()).simplifyValues().getValue();
    } else {
      final int i = index - locals.length;
      return stack.isRefLocal(ctx, i);
    }
  }
  
  private static final Function<Entry, Boolean> IsRefLocal = new Function<Entry, Boolean>() {
    @Override
    public Boolean apply(final Entry y) {
      return y.isRef;
    }
  };

  /*
   * #######################################################
   * Handling of the stack
   * ########################################################
   */

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#push(de.fosd.typechef.featureexpr.FeatureExpr, T)
   */
  @Override
  public <T> void push(final FeatureExpr ctx, final T value) {
    push(ctx, value, false);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#push(de.fosd.typechef.featureexpr.FeatureExpr, java.lang.Object, boolean)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void push(final FeatureExpr ctx, final Object value, final boolean isRef) {
    stack.push(ctx, value, isRef);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#pop(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public Conditional<Integer> pop(final FeatureExpr ctx) {
    return pop(ctx, Type.INT);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#popLong(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public Conditional<Long> popLong(final FeatureExpr ctx) {
    return pop(ctx, Type.LONG);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#popDouble(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public Conditional<Double> popDouble(final FeatureExpr ctx) {
    return pop(ctx, Type.DOUBLE);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#popFloat(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public Conditional<Float> popFloat(final FeatureExpr ctx) {
    return pop(ctx, Type.FLOAT);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#pop(de.fosd.typechef.featureexpr.FeatureExpr, gov.nasa.jpf.vm.StackHandler.Type)
   */
  @Override
  public <T> Conditional<T> pop(final FeatureExpr ctx, final Type t) {
    return stack.pop(ctx, t);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#pop(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public void pop(FeatureExpr ctx, final int n) {
    stack.pop(ctx, n);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#peek(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public Conditional<Integer> peek(FeatureExpr ctx) {
    return peek(ctx, 0);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#peek(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public Conditional<Integer> peek(FeatureExpr ctx, final int offset) {
    return peek(ctx, offset, Type.INT);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#peekDouble(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public Conditional<Double> peekDouble(FeatureExpr ctx, final int offset) {
    return peek(ctx, offset, Type.DOUBLE);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#peekLong(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public Conditional<Long> peekLong(FeatureExpr ctx, final int offset) {
    return peek(ctx, offset, Type.LONG);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#peekFloat(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public Conditional<Float> peekFloat(FeatureExpr ctx, final int offset) {
    return peek(ctx, offset, Type.FLOAT);
  }

  protected <T> Conditional<T> peek(FeatureExpr ctx, final int offset, final Type t) {
    return stack.peek(ctx, offset, t);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#isRef(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public boolean isRef(final FeatureExpr ctx, final int offset) {// change to Conditional<Boolean>
    return stack.isRef(ctx, offset);

  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#set(de.fosd.typechef.featureexpr.FeatureExpr, int, int, boolean)
   */
  @Override
  public void set(final FeatureExpr ctx, final int offset, final int value, final boolean isRef) {
    stack.set(ctx, offset, value, isRef);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#getTop()
   */
  @Override
  public Conditional<Integer> getTop() {
    return stack.getTop();
  }
  

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#setTop(de.fosd.typechef.featureexpr.FeatureExpr, int)
   */
  @Override
  public void setTop(final FeatureExpr ctx, final int i) {
    stack.setTop(ctx, i);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#clear(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void clear(final FeatureExpr ctx) {
    stack.clear(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#getSlots()
   */
  @Override
  public int[] getSlots() {
    return getSlots(FeatureExprFactory.True());
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#getSlots(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public int[] getSlots(FeatureExpr ctx) {
    int[] slots = new int[length];
    int i = 0;
    for (Conditional<Entry> l : locals) {
      if (l == null) {
        slots[i++] = MJIEnv.NULL;
        continue;
      }
      slots[i++] = l.simplify(ctx).getValue(true).value;
    }
    int[] stackSlots;
    stackSlots = stack.getSlots(ctx);
    if(stackSlots.length == 0) return slots;
    int j = 0; 
    while(i < length && j < stackSlots.length) {
      slots[i++] = stackSlots[j++];
    }
    
    while(i < length) {
        slots[i++] = MJIEnv.NULL;
    }

    return slots;
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (!(o instanceof StackHandler)) {
      return false;
    }
    StackHandler other = (StackHandler) o;
    for (int i = 0; i < locals.length; i++) {
      Conditional<Entry> l1 = locals[i];
      Conditional<Entry> l2 = other.locals[i];
      if (l1 == l2) {
        continue;
      }
      if (l1 == null) {
        return false;
      }
      if (!l1.equals(l2)) {
        return false;
      }
    }
    if (!other.stack.equals(stack)) {
      return false;
    }
    return true;
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#hashCode()
   */
  @Override
  public int hashCode() {
    return 42;
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#hasAnyRef(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public boolean hasAnyRef(FeatureExpr ctx) {
    for (Conditional<Entry> local : locals) {
      if (local == null) {
        continue;
      }
      for (Entry entry : local.simplify(ctx).toList()) {
        if (entry.isRef) {
          return true;
        }
      }
    }
    return false || stack.hasAnyRef(ctx);
  }

  /*
   * Stack Instructions
   */

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#dup_x1(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void dup_x1(final FeatureExpr ctx) {
    stack.dup_x1(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#dup2_x2(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void dup2_x2(final FeatureExpr ctx) {
    stack.dup2_x2(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#dup2_x1(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void dup2_x1(final FeatureExpr ctx) {
    stack.dup2_x1(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#dup2(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void dup2(final FeatureExpr ctx) {
    stack.dup2(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#dup(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void dup(final FeatureExpr ctx) {
    stack.dup(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#dup_x2(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void dup_x2(final FeatureExpr ctx) {
    stack.dup_x2(ctx);
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.vm.IStackHandler#swap(de.fosd.typechef.featureexpr.FeatureExpr)
   */
  @Override
  public void swap(final FeatureExpr ctx) {
    stack.swap(ctx);
  }



  @Override
  public int getLength() {
    return length;
  }

  @Override
  public Conditional<Stack> getStack() {
    return stack.getStack();
  }

  @Override
  public Set<Integer> getAllReferences() {
    Set<Integer> references = new HashSet<>();
    for (Conditional<Entry> cl : locals) {
      for (Entry l: cl.toList()) {
        if (l.isRef) {
          references.add(l.value);
        }
      }
    }
    references.addAll(stack.getAllReferences());

    return references;
  }

  @Override
  public int getLocalWidth() {
    int width = -locals.length;
    for (Conditional<Entry> local : locals) {
      width += local.simplify(getCtx()).toMap().size();
    }
    return width;
  }
  
  @Override
  public String getMaxLocal() {
    StringBuilder builder = new StringBuilder();
    for (Conditional<Entry> local : locals) {   
      int size = local.simplify(getCtx()).toMap().size();
      builder.append(local.simplify(getCtx()));
      builder.append(":");
      builder.append(size);
      builder.append('\n');
    }
    return builder.toString();
  }

  @Override
  public void IINC(FeatureExpr ctx, int index, final int increment) {
    locals[index] = locals[index].mapf(ctx, new BiFunction<FeatureExpr, Entry, Conditional<Entry>>() {

      @Override
      public Conditional<Entry> apply(FeatureExpr ctx, Entry y) {
        if (Conditional.isContradiction(ctx)) {
          return new One<>(y);
        }
        Entry copy = new Entry(y.value + increment, y.isRef);
        if (Conditional.isTautology(ctx)) {
          return new One<>(copy);
        }
        return ChoiceFactory.create(ctx, new One<>(copy), new One<>(y));
      }
      
      
    }).simplify();
  }

}