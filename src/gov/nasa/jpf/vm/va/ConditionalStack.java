package gov.nasa.jpf.vm.va;

import java.util.Collection;
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
import gov.nasa.jpf.vm.va.IStackHandler.StackInstruction;
import gov.nasa.jpf.vm.va.IStackHandler.Type;

public class ConditionalStack implements IVStack {
	private Conditional<Stack> stack;
	public FeatureExpr stackCTX;
	
	public ConditionalStack() {
		stack = new One<>(new Stack(0));
		stackCTX = FeatureExprFactory.True();
	}
	
	public ConditionalStack(int nOperands) {
		stack = new One<>(new Stack(nOperands));
		stackCTX = FeatureExprFactory.True();
	}
	
	public ConditionalStack(Conditional<Stack> cs) {
		stack = cs;
	}
	
	public void init(Stack st) {
		stack = new One<>(st);
	}
	
	public FeatureExpr getCtx() {
		return stackCTX;
	}
	
	public void setCtx(FeatureExpr ctx) {
		stackCTX = ctx;
	}
	
	public int getStackWidth() {
		return stack.simplify().toList().size();
	}
	
	@Override
	public void clear(final FeatureExpr ctx) {
		stack = stack.mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Stack>>() {

			@Override
			public Conditional<Stack> apply(final FeatureExpr f, final Stack stack) {
				if (Conditional.isContradiction(f)) {
					return new One<>(stack);
				}
				Stack clone = stack.copy();
				clone.clear();
				
				if (Conditional.isTautology(f)) {
					return new One<>(clone);
				}
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(stack));
			}
		}).simplify();
	}
    

	@Override
	@SuppressWarnings("unchecked")
	public void push(final FeatureExpr ctx, final Object value, final boolean isRef) {
		if (value instanceof Conditional) {
			((Conditional<Object>) value).mapf(ctx, new VoidBiFunction<FeatureExpr, Object>() {

				@Override
				public void apply(final FeatureExpr ctx, final Object value) {
					push(ctx, value, isRef);
				}

			});
			return;
		}

		stack = stack.mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Stack>>() {

			@Override
			public Conditional<Stack> apply(final FeatureExpr f, final Stack stack) {
				if (Conditional.isContradiction(f)) {
					return new One<>(stack);
				}
				Stack clone = stack.copy();
				if (value instanceof Integer) {
					clone.push((Integer) value, isRef);
				} else if (value instanceof Long) {
					long v = ((Long) value).longValue();
					clone.push((int) (v >> 32), isRef);
					clone.push((int) v, isRef);
				} else if (value instanceof Double) {
					long v = Double.doubleToLongBits((Double) value);
					clone.push((int) (v >> 32), isRef);
					clone.push((int) v, isRef);
				} else if (value instanceof Float) {
					clone.push(Float.floatToIntBits((Float) value), isRef);
				} else if (value instanceof Byte) {
					clone.push(((Byte) value).intValue(), isRef);
				} else if (value instanceof Short) {
					clone.push((int) (Short) value, isRef);
				} else if (value == null) {
					clone.push(MJIEnv.NULL, isRef);
				} else {
					throw new RuntimeException(value + " of type " + value.getClass() + " cannot be pushed to the stack.");
				}

				if (stackCTX.equivalentTo(f)) {
					return new One<>(clone);
				}
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(stack));
			}
		}).simplify();
	}
	@Override
	public void setRef(FeatureExpr ctx, int index, boolean ref) {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> Conditional<T> peek(FeatureExpr ctx, final int offset, final Type t) {
		return stack.simplify(ctx).map(new Function<Stack, T>() {

			@SuppressWarnings("unchecked")
			@Override
			public T apply(final Stack stack) {
				switch (t) {
				case DOUBLE:
					return (T) (Double) Types.intsToDouble(stack.peek(offset), stack.peek(offset + 1));
				case FLOAT:
					return (T) (Float) Types.intToFloat(stack.peek(offset));
				case INT:
					return (T) stack.peek(offset);
				case LONG:
					return (T) (Long) Types.intsToLong(stack.peek(offset), stack.peek(offset + 1));
				default:
					return null;
				}
			}

		}).simplifyValues();
	}
	@Override
	public Conditional<Entry> popEntry(FeatureExpr ctx, final boolean copyRef) {
		Conditional<Entry> result = stack.simplify(ctx).mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Entry>>() {

			@Override
			public Conditional<Entry> apply(final FeatureExpr f, final Stack s) {
				Stack clone = s.copy();
				boolean ref = copyRef ? clone.isRef(0) : false;
				int res = clone.pop();
				if (stackCTX.equivalentTo(f)) {
					stack = new One<>(clone);
				} else {
					stack = ChoiceFactory.create(f, new One<>(clone), stack);
				}
				return new One<>(new Entry(res, ref));
			}
			
		}).simplify();
		stack = stack.simplify();
		return result;
	}
	public <T> Conditional<T> pop(final FeatureExpr ctx, final Type t) {
		Conditional<T> result = stack.simplify(ctx).mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<T>>() {

			@SuppressWarnings("unchecked")
			@Override
			public Conditional<T> apply(final FeatureExpr f, final Stack s) {
				Stack clone = s.copy();
				Number res;
				final int lo = clone.pop();

				switch (t) {
				case INT:
					res = Integer.valueOf(lo);
					break;
				case DOUBLE:
					res = Types.intsToDouble(lo, clone.pop());
					break;
				case FLOAT:
					res = Types.intToFloat(lo);
					break;
				case LONG:
					res = Types.intsToLong(lo, clone.pop());
					break;
				default:
					return null;
				}
				if (stackCTX.equivalentTo(f)) {
					stack = new One<>(clone);
				} else {
					stack = ChoiceFactory.create(f, new One<>(clone), stack);
				}
				return (Conditional<T>) new One<>(res);

			}
		}).simplifyValues();
		stack = stack.simplify();
		return result;
	}
	
	public void pop(FeatureExpr ctx, final int n) {
		stack = stack.mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Stack>>() {

			@Override
			public Conditional<Stack> apply(final FeatureExpr f, final Stack s) {
				if (Conditional.isContradiction(f)) {
					return new One<>(s);
				}

				Stack clone = s.copy();
				for (int i = n; i > 0; i--) {
					clone.pop();
				}

				if (Conditional.isTautology(f)) {
					return new One<>(clone);
				}
				return ChoiceFactory.create(f, new One<>(clone), new One<>(s));
			}
		}).simplify();
	}
	

	@Override
	public boolean isRef(final FeatureExpr ctx, final int offset) {// change to Conditional<Boolean>
		return stack.simplify(ctx).map(new Function<Stack, Boolean>() {

			@Override
			public Boolean apply(final Stack y) {
				return y.isRef(offset);
			}

		}).simplifyValues().getValue();
	}


	@Override
	public void set(final FeatureExpr ctx, final int offset, final int value, final boolean isRef) {
		stack = stack.mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Stack>>() {

			@Override
			public Conditional<Stack> apply(FeatureExpr f, Stack stack) {
				if (f.isContradiction()) {
					return new One<>(stack);
				}
				Stack clone = stack.copy();
				clone.set(offset, value, isRef);
				if (f.isTautology()) {
					return new One<>(clone);
				}
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(stack));
			}
			
		}).simplify();

	}
	
	public void setTop(final FeatureExpr ctx, final int i) {
		stack = stack.mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Stack>>() {

			@Override
			public Conditional<Stack> apply(final FeatureExpr f, final Stack stack) {
				if (Conditional.isContradiction(f)) {
					return new One<>(stack);
				}
				Stack clone = stack.copy();
				clone.top = i;
				
				if (ctx.equals(f)) {
					return new One<>(clone); 
				}
				if (Conditional.isTautology(f)) {
					return new One<>(clone);
				}
				
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(stack));
			}
		}).simplify();
	}
	

	@Override
	public void dup_x1(FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP_X1);

	}

	@Override
	public void dup2_x2(FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP2_X2);

	}

	@Override
	public void dup2_x1(FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP2_X1);

	}

	@Override
	public void dup2(FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP2);

	}

	@Override
	public void dup(FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP);

	}

	@Override
	public void dup_x2(FeatureExpr ctx) {
		function(ctx, StackInstruction.DUP_X2);

	}

	@Override
	public void swap(FeatureExpr ctx) {
		function(ctx, StackInstruction.SWAP);

	}
	
	void function(final FeatureExpr ctx, final StackInstruction instruction) {
		stack = stack.mapf(ctx, new BiFunction<FeatureExpr, Stack, Conditional<Stack>>() {

			@Override
			public Conditional<Stack> apply(final FeatureExpr f, final Stack stack) {
				if (Conditional.isContradiction(f)) {
					return new One<>(stack);
				}
				Stack clone = stack.copy();
				switch (instruction) {
				case DUP_X1:
					clone.dup_x1();
					break;
				case DUP2_X2:
					clone.dup2_x2();
					break;
				case DUP:
					clone.dup();
					break;
				case DUP2:
					clone.dup2();
					break;
				case DUP2_X1:
					clone.dup2_x1();
					break;
				case DUP_X2:
					clone.dup_x2();
					break;
				case SWAP:
					clone.swap();
					break;
				default:
					throw new RuntimeException(instruction + "not supported");
				}

				if (Conditional.isTautology(f)) {
					return new One<>(clone);
				}
				
				if (stackCTX.equivalentTo(f)) {
					return new One<>(clone);
				}
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(stack));
			}
		}).simplify();
	}
	
	@Override
	public ConditionalStack clone() {
		ConditionalStack clone = new ConditionalStack(); 
		clone.stack = stack.map(CopyStack);
		clone.stackCTX = this.stackCTX;
		return clone;
	}
	private static final Function<Stack, Stack> CopyStack = new Function<Stack, Stack>() {
		@Override
		public Stack apply(final Stack stack) {
			return stack.copy();
		}
	};
	public Conditional<Stack> getStack() {
		return stack;
	}

	public Conditional<Integer> getInteger(final FeatureExpr ctx, final int index) {
		return stack.map(new Function<Stack, Integer>() {

			@Override
			public Integer apply(final Stack stack) {
				return stack.get(index);
			}

		});
	}
	
	public boolean hasAnyRef(FeatureExpr ctx) {
		for (Stack s : stack.simplify(ctx).toList()) {
			if (s.hasAnyRef()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Set<Integer> getAllReferences() {
		Set<Integer> references = new HashSet<>();
		for (Stack s: stack.toList()) {
			references.addAll(s.getReferences());
		}
		return references;
	}
	
	public int[] getSlots(FeatureExpr ctx) {
		int[] slots = new int[stack.simplify(ctx).getValue(true).getSlots().length];
		int i = 0 ;
		for (int o : stack.simplify(ctx).getValue(true).getSlots()) {
			slots[i++] = o;
		}

		return slots;
	}
	
	public Conditional<Integer> getTop() {
		return stack.map(GetTop);
	}
	
	private static final Function<Stack, Integer> GetTop = new Function<Stack, Integer>() {
		@Override
		public Integer apply(final Stack y) {
			return y.top;
		}
	};
	
	public boolean isRefLocal(FeatureExpr ctx, final int index) {
		
		return stack.simplify(ctx).map(new Function<Stack, Boolean>() {

			@Override
			public Boolean apply(final Stack stack) {
				return stack.isRefIndex(index);
			}

		}).simplifyValues().getValue();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof ConditionalStack)) return false;
		return stack.equals(((ConditionalStack)o).stack);
	}

  @Override
  @SuppressWarnings("unchecked")
  public void pushEntry(final FeatureExpr ctx, final Conditional<Entry> value) {
    value.mapf(ctx, new VoidBiFunction<FeatureExpr, Entry>() {
      @Override
      public void apply(final FeatureExpr ctx, final Entry entry) {
        push(ctx, entry.value, entry.isRef);
      }
    });
  }

	
}
