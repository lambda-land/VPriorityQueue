package cmu.conditional;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;

public class TreeChoiceWithHoles<T> extends TreeChoice<T> {

TreeChoiceWithHoles(FeatureExpr featureExpr, Conditional<T> thenBranch, Conditional<T> elseBranch) {
    super(featureExpr, thenBranch, elseBranch);
//    this.featureExpr = featureExpr;
//    this.thenBranch = thenBranch;
//    this.elseBranch = elseBranch;

}

@Override
public <U> Conditional<U> mapfr(final FeatureExpr inFeature, final BiFunction<FeatureExpr, T, Conditional<U>> f) {
    if (inFeature == null) {
        Conditional<U> newResultA = thenBranch.mapfr(null, f);
        Conditional<U> newResultB = elseBranch.mapfr(null, f);
        return createInstance(featureExpr, newResultA, newResultB);
    }

    Conditional<U> newResultA = thenBranch.mapfr(inFeature.and(featureExpr), f);
    Conditional<U> newResultB = elseBranch.mapfr(inFeature.and(featureExpr.not()), f);
    return createInstance(featureExpr, newResultA, newResultB);
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
    FeatureExpr and = ctx.and(featureExpr);
    if (isContradiction(and)) {
        return elseBranch.simplify(ctx.andNot(featureExpr));
    }
    //System.out.println("" + this);
    FeatureExpr andNot = ctx.andNot(featureExpr);
    if (isContradiction(andNot)) {
        return thenBranch.simplify(and);
    }

    final Conditional<T> tb = thenBranch.simplify(and);
    final Conditional<T> eb = elseBranch.simplify(andNot);

    if (tb.equals(eb)) {
        return tb;
    }

    if (tb instanceof One) {
        if (eb instanceof TreeChoiceWithHoles) {
            if (((TreeChoiceWithHoles<T>) eb).thenBranch.equals(tb)) {
                return createInstance(featureExpr.or(featureExpr.not().and(((TreeChoiceWithHoles<T>) eb).featureExpr)), tb,
                        ((TreeChoiceWithHoles<T>) eb).elseBranch);
            }
            if (((TreeChoiceWithHoles<T>) eb).elseBranch.equals(tb)) {
                return createInstance(featureExpr.or(featureExpr.not().and(((TreeChoiceWithHoles<T>) eb).featureExpr.not())),
                        tb, ((TreeChoiceWithHoles<T>) eb).thenBranch);
            }
        }
    }
    if (eb instanceof One) {
        if (tb instanceof TreeChoiceWithHoles) {
            if (((TreeChoiceWithHoles<T>) tb).thenBranch.equals(eb)) {
                return createInstance(featureExpr.not().or(featureExpr.and(((TreeChoiceWithHoles<T>) tb).featureExpr)), eb,
                        ((TreeChoiceWithHoles<T>) tb).elseBranch);
            }
            if (((TreeChoiceWithHoles<T>) tb).elseBranch.equals(eb)) {
                return createInstance(featureExpr.not().or(featureExpr.and(((TreeChoiceWithHoles<T>) tb).featureExpr.not())),
                        eb, ((TreeChoiceWithHoles<T>) tb).thenBranch);
            }
        }
    }

    return createInstance(featureExpr, tb, eb);
}

@Override
public String toString() {
    return "Choice(" + getCTXString(featureExpr) + ", " + thenBranch + ", " + elseBranch + ")";
}

@SuppressWarnings("unchecked")
@Override
public boolean equals(Object obj) {
    if (obj instanceof TreeChoice) {
        TreeChoiceWithHoles<T> c = (TreeChoiceWithHoles<T>) obj;
        return c.thenBranch.equals(thenBranch) && c.elseBranch.equals(elseBranch)
                && c.featureExpr.equivalentTo(featureExpr);
    }
    return false;
}

@Override
public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((elseBranch == null) ? 0 : elseBranch.hashCode());
    result = prime * result + ((featureExpr == null) ? 0 : featureExpr.hashCode());
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
    thenBranch.toMap(ctx.and(featureExpr), map);
    elseBranch.toMap(ctx.andNot(featureExpr), map);
}

@Override
public Conditional<T> clone() throws CloneNotSupportedException {
    return createInstance(featureExpr, thenBranch.clone(), elseBranch.clone());
}

@Override
public Conditional<T> simplifyValues() {
    final Conditional<T> tb = thenBranch.simplifyValues();
    final Conditional<T> eb = elseBranch.simplifyValues();

    if (tb.equals(eb)) {
        return tb;
    }

    if (tb instanceof One) {
        if (eb instanceof TreeChoiceWithHoles) {
            if (((TreeChoiceWithHoles<T>) eb).thenBranch.equals(tb)) {
                return createInstance(featureExpr.or(featureExpr.not().and(((TreeChoiceWithHoles<T>) eb).featureExpr)), tb,
                        ((TreeChoiceWithHoles<T>) eb).elseBranch);
            }
            if (((TreeChoiceWithHoles<T>) eb).elseBranch.equals(tb)) {
                return createInstance(featureExpr.or(featureExpr.not().and(((TreeChoiceWithHoles<T>) eb).featureExpr.not())),
                        tb, ((TreeChoiceWithHoles<T>) eb).thenBranch);
            }
        }
    }
    if (eb instanceof One) {
        if (tb instanceof TreeChoice) {
            if (((TreeChoiceWithHoles<T>) tb).thenBranch.equals(eb)) {
                return createInstance(featureExpr.not().or(featureExpr.and(((TreeChoiceWithHoles<T>) tb).featureExpr)), eb,
                        ((TreeChoiceWithHoles<T>) tb).elseBranch);
            }
            if (((TreeChoiceWithHoles<T>) tb).elseBranch.equals(eb)) {
                return createInstance(featureExpr.not().or(featureExpr.and(((TreeChoiceWithHoles<T>) tb).featureExpr.not())),
                        eb, ((TreeChoiceWithHoles<T>) tb).thenBranch);
            }
        }
    }

    return createInstance(featureExpr, tb, eb);
}

@Override
public int size() {
    return thenBranch.size() + elseBranch.size();
}

@Override
public FeatureExpr getFeatureExpr(T t) {
    if (t == null) {
        if(elseBranch.equals(One.NULL)) return featureExpr.not();
        return FeatureExprFactory.False();
    }
    FeatureExpr tf = thenBranch.getFeatureExpr(t);
    FeatureExpr ef = elseBranch.getFeatureExpr(t);
    return tf.and(featureExpr).or(ef.andNot(featureExpr));
}

public static <T> Conditional<T> createInstance(FeatureExpr featureExpr, Conditional<T> thenBranch,
        Conditional<T> elseBranch) {
    //System.out.println("create " + featureExpr + " " + thenBranch + " " + elseBranch);
    if (featureExpr.isTautology())
        return thenBranch;
    if (featureExpr.isContradiction())
        return elseBranch;

    if(thenBranch instanceof One && elseBranch instanceof One ) {
        if (thenBranch.equals(elseBranch)) {
            return thenBranch;
        }
        
        if (thenBranch.equals(One.NULL)) {
            return new TreeChoice(featureExpr.not(), elseBranch, thenBranch);
        } else {
            return new TreeChoice(featureExpr, thenBranch, elseBranch);
        }
    }
    
    FeatureExpr not = featureExpr.not();
    
    //boolean tb = thenBranch instanceof TreeChoice, eb = elseBranch instanceof TreeChoice;

    if (thenBranch instanceof TreeChoice) {
        if (featureExpr.equivalentTo(((TreeChoiceWithHoles) thenBranch).featureExpr)) {
            thenBranch = ((TreeChoiceWithHoles) thenBranch).thenBranch;
        } else if (not.equivalentTo(((TreeChoiceWithHoles) thenBranch).featureExpr)) {
            thenBranch = ((TreeChoiceWithHoles) thenBranch).elseBranch;
        }
    }

    if (elseBranch instanceof TreeChoice) {
        if (featureExpr.equivalentTo(((TreeChoiceWithHoles) elseBranch).featureExpr)) {
            elseBranch = ((TreeChoiceWithHoles) elseBranch).elseBranch;
        } else if (not.equivalentTo(((TreeChoiceWithHoles) elseBranch).featureExpr)) {
            elseBranch = ((TreeChoiceWithHoles) elseBranch).thenBranch;
        }
    }

    if (thenBranch instanceof TreeChoice) {
        if (((TreeChoiceWithHoles) thenBranch).elseBranch.equals(elseBranch)) {
            return new TreeChoiceWithHoles(((TreeChoiceWithHoles) thenBranch).featureExpr.and(featureExpr),
                    ((TreeChoiceWithHoles) thenBranch).thenBranch,  ((TreeChoiceWithHoles) thenBranch).elseBranch);
        }

        if (((TreeChoiceWithHoles) thenBranch).thenBranch.equals(elseBranch)) {
            return new TreeChoiceWithHoles(((TreeChoiceWithHoles) thenBranch).featureExpr.orNot(featureExpr),
                    ((TreeChoiceWithHoles) thenBranch).thenBranch,  ((TreeChoiceWithHoles) thenBranch).elseBranch);
        }
    }
    
    

    if (elseBranch instanceof TreeChoice) {
        if (((TreeChoiceWithHoles) elseBranch).elseBranch.equals(thenBranch)) {
            return new TreeChoiceWithHoles(((TreeChoiceWithHoles) elseBranch).featureExpr.andNot(featureExpr),
                    ((TreeChoiceWithHoles) elseBranch).thenBranch,  ((TreeChoiceWithHoles) elseBranch).elseBranch);
        }

        if (((TreeChoiceWithHoles) elseBranch).thenBranch.equals(thenBranch)) {
            return new TreeChoiceWithHoles(((TreeChoiceWithHoles) elseBranch).featureExpr.or(featureExpr),
                    ((TreeChoiceWithHoles) elseBranch).thenBranch,  ((TreeChoiceWithHoles) elseBranch).elseBranch);
        }
    }
    
    if (thenBranch.equals(elseBranch)) {
        return thenBranch;
    }
    
    FeatureExpr hole = thenBranch.getFeatureExpr(null).and(featureExpr).or(elseBranch.getFeatureExpr(null).andNot(featureExpr));
    
    if(!Conditional.isContradiction(hole)) {
        if(!thenBranch.getFeatureExpr(null).isContradiction()) {
            thenBranch = thenBranch.simplify(thenBranch.getFeatureExpr(null).not());
        }
        if(!elseBranch.getFeatureExpr(null).isContradiction()) {
            elseBranch = elseBranch.simplify(elseBranch.getFeatureExpr(null).not());
        }
        return new TreeChoice(hole.not(), 
                new TreeChoice(featureExpr, thenBranch, elseBranch),
                One.NULL);
    }    
    
    if (thenBranch.equals(One.NULL)) {
        return new TreeChoice(featureExpr.not(), elseBranch, thenBranch);
    } else {
        return new TreeChoice(featureExpr, thenBranch, elseBranch);
    }
}

@Override
public Conditional<T>[] split(FeatureExpr ctx) {
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
