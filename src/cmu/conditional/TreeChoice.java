package cmu.conditional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

/**
 * Choice implementation as tree. 
 * Each node represents a decision as {@link FeatureExpr} between two conditionals. 
 * 
 * @author Jens Meinicke
 *
 */
class TreeChoice<T> extends IChoice<T> implements Cloneable {

    protected Conditional<T> thenBranch;
    protected Conditional<T> elseBranch;
    protected FeatureExpr featureExpr;
    protected FeatureExpr nullExpr;
    
    TreeChoice(FeatureExpr featureExpr, Conditional<T> thenBranch, Conditional<T> elseBranch) {
        super(featureExpr, thenBranch, elseBranch);
        this.featureExpr = featureExpr;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
        //this.depth = Math.max(thenBranch.depth(), elseBranch.depth()) + 1;
        nullExpr = null;
    }

    @Override
    public <U> Conditional<U> mapfr(final FeatureExpr inFeature, final BiFunction<FeatureExpr, T, Conditional<U>> f) {
        if (inFeature == null) {
            Conditional<U> newResultA = thenBranch.mapfr(null, f);
            Conditional<U> newResultB = elseBranch.mapfr(null, f);
            return new TreeChoice<>(featureExpr, newResultA, newResultB);
        }

        Conditional<U> newResultA = thenBranch.mapfr(inFeature.and(featureExpr), f);
        Conditional<U> newResultB = elseBranch.mapfr(inFeature.and(featureExpr.not()), f);
        return new TreeChoice<>(featureExpr, newResultA, newResultB);
    }
    
    @Override
    public void mapfr(final FeatureExpr inFeature, final VoidBiFunction<FeatureExpr, T> f) {
        if (inFeature == null) {
            thenBranch.mapfr(null, f);
            elseBranch.mapfr(null, f);
            return;
        }

        thenBranch.mapfr(inFeature.and(featureExpr), f);
        elseBranch.mapfr(inFeature.and(featureExpr.not()), f);
    }

    @Override
    public Conditional<T> simplify(FeatureExpr ctx) {
        //if(elseBranch == this) System.out.println("error");
        //if(thenBranch == this) System.out.println("error");
        /* 
        if(depth > 5000) {
            System.out.println("depth " + depth);
        }
        */
    	
        FeatureExpr and = ctx.and(featureExpr);
        if (isContradiction(and)) {
            return elseBranch.simplify(ctx.andNot(featureExpr));
        }

        FeatureExpr andNot = ctx.andNot(featureExpr);
        if (isContradiction(andNot)) {
            return thenBranch.simplify(and);
        }

        final Conditional<T> tb = thenBranch == null ? null : thenBranch.simplify(and);
        final Conditional<T> eb = elseBranch == null ? null : elseBranch.simplify(andNot);

        if(tb == eb) {
            return tb;
        }
        /*
        if (tb.equals(eb)) {
            return tb;
        }
        
        if (tb instanceof One) {
            if (eb instanceof TreeChoice) {
                if (((TreeChoice<T>) eb).thenBranch.equals(tb)) {
                    return new TreeChoice<>(featureExpr.or(featureExpr.not().and(((TreeChoice<T>) eb).featureExpr)), tb, ((TreeChoice<T>) eb).elseBranch);
                }
                if (((TreeChoice<T>) eb).elseBranch.equals(tb)) {
                    return new TreeChoice<>(featureExpr.or(featureExpr.not().and(((TreeChoice<T>) eb).featureExpr.not())), tb, ((TreeChoice<T>) eb).thenBranch);
                }
            }
        }
        if (eb instanceof One) {
            if (tb instanceof TreeChoice) {
                if (((TreeChoice<T>) tb).thenBranch.equals(eb)) {
                    return new TreeChoice<>(featureExpr.not().or(featureExpr.and(((TreeChoice<T>) tb).featureExpr)), eb, ((TreeChoice<T>) tb).elseBranch);
                }
                if (((TreeChoice<T>) tb).elseBranch.equals(eb)) {
                    return new TreeChoice<>(featureExpr.not().or(featureExpr.and(((TreeChoice<T>) tb).featureExpr.not())), eb, ((TreeChoice<T>) tb).thenBranch);
                }
            }
        }
        */
        if(featureExpr.isContradiction()) return eb;
        if(featureExpr.isTautology()) return tb;
        
        return new TreeChoice<>(featureExpr, tb, eb);
    }

    @Override
    public String toString() {
        return "Choice(" + getCTXString(featureExpr) + ", " + thenBranch + ", " + elseBranch + ")";
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TreeChoice) {
            TreeChoice<T> c = (TreeChoice<T>) obj;
            return c.thenBranch.equals(thenBranch) && c.elseBranch.equals(elseBranch) && c.featureExpr.equivalentTo(featureExpr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((elseBranch == null) ? 0 : elseBranch.hashCode());
        //result = prime * result + ((featureExpr == null) ? 0 : featureExpr.hashCode());
        result = prime * result + ((thenBranch == null) ? 0 : thenBranch.hashCode());
        return result;
    }

    @Override
    public T getValue() {
         System.out.println("___________________________________________________");
         System.out.println("Get value of choice called: " + this);
         for (StackTraceElement e : Thread.currentThread().getStackTrace()) {
         System.out.println(e);
         }
         System.out.println("---------------------------------------------------");
        // return thenBranch.getValue(true);
        throw new RuntimeException("Get value of choice called: " + toString());
    }

    @Override
    public T getValue(boolean ignore) {
        if (ignore) {
            return thenBranch.getValue(ignore);
        }
        return getValue();
    }

    @Override
    public List<T> toList() {
        List<T> list = new LinkedList<>();
        for (T e : thenBranch.toList()) {
            if (!list.contains(e)) {
                list.add(e);
            }
        }

        for (T e : elseBranch.toList()) {
            if (!list.contains(e)) {
                list.add(e);
            }
        }
        return list;
    }

    @Override
    protected void toMap(FeatureExpr ctx, Map<T, FeatureExpr> map) {
        if(thenBranch != null) thenBranch.toMap(ctx.and(featureExpr), map);
        if(elseBranch != null) elseBranch.toMap(ctx.andNot(featureExpr), map);
    }

    @Override
    public Conditional<T> clone() throws CloneNotSupportedException {
        return new TreeChoice<>(featureExpr, thenBranch.clone(), elseBranch.clone());
    }

    @Override
    public Conditional<T> simplifyValues() {
        final Conditional<T> tb = thenBranch.simplifyValues();
        final Conditional<T> eb = elseBranch.simplifyValues();
        
        if (tb.equals(eb)) {
            return tb;
        }

        if (tb instanceof One) {
            if (eb instanceof TreeChoice) {
                if (((TreeChoice<T>) eb).thenBranch.equals(tb)) {
                    return new TreeChoice<>(featureExpr.or(featureExpr.not().and(((TreeChoice<T>) eb).featureExpr)), tb, ((TreeChoice<T>) eb).elseBranch);
                }
                if (((TreeChoice<T>) eb).elseBranch.equals(tb)) {
                    return new TreeChoice<>(featureExpr.or(featureExpr.not().and(((TreeChoice<T>) eb).featureExpr.not())), tb, ((TreeChoice<T>) eb).thenBranch);
                }
            }
        }
        if (eb instanceof One) {
            if (tb instanceof TreeChoice) {
                if (((TreeChoice<T>) tb).thenBranch.equals(eb)) {
                    return new TreeChoice<>(featureExpr.not().or(featureExpr.and(((TreeChoice<T>) tb).featureExpr)), eb, ((TreeChoice<T>) tb).elseBranch);
                }
                if (((TreeChoice<T>) tb).elseBranch.equals(eb)) {
                    return new TreeChoice<>(featureExpr.not().or(featureExpr.and(((TreeChoice<T>) tb).featureExpr.not())), eb, ((TreeChoice<T>) tb).thenBranch);
                }
            }
        }
        
        return new TreeChoice<>(featureExpr, tb, eb);
    }

    @Override
    public int size() {
        return thenBranch.size() + elseBranch.size();
    }
    /*
    @Override
    public int depth() {
        return depth;
    }
    */
    
//    private static <T> Conditional<T> buildTreeFromArray(Map.Entry<T, FeatureExpr>[] arr, int l, int r) {
//      if(r - l == 1) {
//          return 
//      }
//    }
//    
//    private static <T> Conditional<T> try2Merge(FeatureExpr featureExpr, Conditional<T> thenBranch, Conditional<T> elseBranch) {
//      Map<T, FeatureExpr> t = thenBranch.toMap();
//      Map<T, FeatureExpr> e = elseBranch.toMap();
//      if(t.keySet().equals(e.keySet())) {
//          for(T k : t.keySet()) {
//              FeatureExpr f = t.get(k).and(featureExpr).or(e.get(k).andNot(featureExpr));
//              t.put(k, f);
//          }
//          Map.Entry<T, FeatureExpr>[] arr = (Map.Entry<T, FeatureExpr>[])t.entrySet().toArray();
//      } else {
//          return null;
//      }
//    }
    public static <T> Conditional<T> createInstance(FeatureExpr featureExpr, Conditional<T> thenBranch, Conditional<T> elseBranch) {
        if(featureExpr.isTautology()) return thenBranch;
        if(featureExpr.isContradiction()) return elseBranch;
        if(thenBranch instanceof TreeChoice) {
            if(featureExpr.equivalentTo(((TreeChoice) thenBranch).featureExpr)) {
                thenBranch = ((TreeChoice) thenBranch).thenBranch;
            }
        }
        if(elseBranch instanceof TreeChoice) {
            if(featureExpr.equivalentTo(((TreeChoice) elseBranch).featureExpr)) {
                elseBranch = ((TreeChoice) elseBranch).elseBranch;
            }
        }
        
        if(thenBranch instanceof One && elseBranch instanceof One) {
            if(thenBranch.equals(elseBranch)) {
                return thenBranch;
            }
        }
        
//        if(thenBranch.hashCode() == elseBranch.hashCode()) {
//          Conditional<T> res = try2Merge(FeatureExpr featureExpr, Conditional<T> thenBranch, Conditional<T> elseBranch);
//        }
        
        if(thenBranch instanceof TreeChoice && elseBranch instanceof TreeChoice) {
            if(((TreeChoice) thenBranch).thenBranch.equals(((TreeChoice) elseBranch).thenBranch)
                    && ((TreeChoice) thenBranch).elseBranch.equals(((TreeChoice) elseBranch).elseBranch)) {
                return new TreeChoice(featureExpr.and(((TreeChoice) thenBranch).featureExpr).or(((TreeChoice) elseBranch).featureExpr.andNot(featureExpr)),
                        ((TreeChoice) thenBranch).thenBranch, ((TreeChoice) thenBranch).elseBranch);
            }
        }
        
        return new TreeChoice(featureExpr, thenBranch, elseBranch);
    }
    
    @Override
    public FeatureExpr getFeatureExpr(final T t) {
        if(t == null) {
            if(nullExpr != null) {
                return nullExpr; 
            } else {
                nullExpr = thenBranch.getFeatureExpr(t).and(featureExpr).or(elseBranch.getFeatureExpr(t).andNot(featureExpr));
                return nullExpr;
            }
        }
        return thenBranch.getFeatureExpr(t).and(featureExpr).or(elseBranch.getFeatureExpr(t).andNot(featureExpr));
        
    }
    
    @Override
    public Conditional<T>[] split(FeatureExpr ctx) {
        //System.out.println("split");
        Conditional<T>[] ret = new Conditional[2];
        Conditional<T>[] thenRet, elseRet;

        FeatureExpr and = ctx.and(featureExpr);
        FeatureExpr andNot = ctx.andNot(featureExpr);

        if (isContradiction(and)) {
            elseRet = elseBranch.split(andNot);
            ret[0] = elseRet[0];
            ret[1] = createInstance(featureExpr, thenBranch, elseRet[1]);
            return ret;
        }

        if (isContradiction(andNot)) {
            thenRet = thenBranch.split(and);
            ret[0] = thenRet[0];
            ret[1] = createInstance(featureExpr, thenRet[1], elseBranch);
            return ret;
        }

        thenRet = thenBranch.split(and);
        elseRet = elseBranch.split(andNot);

        if (thenRet[0].equals(elseRet[0])) {
            ret[0] = thenRet[0];
        } else {
            ret[0] = createInstance(featureExpr, thenRet[0], elseRet[0]);
        }

        if (thenRet[1].equals(elseRet[1])) {
            ret[1] = thenRet[1];
        } else {
            ret[1] = createInstance(featureExpr, thenRet[1], elseRet[1]);
        }

        return ret;

    }
    @Override
    public <U, Y> Conditional<U> fastApply(final Conditional<Y> rhs, final BiFunction<T, Y, Conditional<U>> f) {
        if (rhs instanceof One) {
            return super.fastApply(rhs, f);
        }

        if (!(rhs instanceof TreeChoice)) {
            return super.fastApply(rhs, f);
        }

        Conditional<Y>[] rhss = rhs.split(featureExpr);

        rhss[0].simplify();
        rhss[1].simplify();

        return createInstance(featureExpr, thenBranch.fastApply(rhss[0], f), elseBranch.fastApply(rhss[1], f)).simplify();
    }
    @Override
    public Conditional<T> fastUpdate(final Function<T, Conditional<T>> f) {
        thenBranch = thenBranch.fastUpdate(f);
        elseBranch = elseBranch.fastUpdate(f);
        return this;
    }

    @Override
    protected Conditional<T> fastUpdate(FeatureExpr path, FeatureExpr ctx, final Function<T, Conditional<T>> f) {
        if (isContradiction(ctx.and(path))) {
        } else if (isTautology(ctx.orNot(path))) {
            fastUpdate(f);
        } else {
            FeatureExpr pT = path.and(featureExpr);
            FeatureExpr pF = path.andNot(featureExpr);
            thenBranch.fastUpdate(pT, ctx, f);
            elseBranch.fastUpdate(pF, ctx, f);
        }

        if (thenBranch.equals(elseBranch)) {
            return thenBranch;
        }

        return this;

    }

    @Override
    public Conditional<T> fastUpdate(FeatureExpr ctx, final Function<T, Conditional<T>> f) {
        return fastUpdate(FeatureExprFactory.True(), ctx, f);
    }
}
