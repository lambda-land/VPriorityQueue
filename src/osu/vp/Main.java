package osu.vp;

/**
 * @author Meng Meng 
 */

import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import cmu.conditional.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import java.lang.*;
import java.util.*;
import java.util.*;

class NonStaticFeature {
FeatureExpr a = FeatureExprFactory.createDefinedExternal("f" + Main.FeatureID++);
}

public class Main {
  public static int FeatureID = 0;
  public static boolean flag = false;
  
  public static NonStaticFeature[] getOptions(int nrOptions) {
      NonStaticFeature[] options = new NonStaticFeature[nrOptions];
      for (int i = 0; i < options.length; i++) {
          options[i] = new NonStaticFeature();
      }
      return options;
  }
  
  public static FeatureExpr randomFEGen(NonStaticFeature[] options) {
      int i = (int) (Math.random() * options.length);
      FeatureExpr f = options[i].a;
      if (Math.random() < 0.2) {
          f = f.not();
      }
      if (Math.random() < 0.5) {
          return f;
      } else {
          if (Math.random() < 0.5)
              return f.and(randomFEGen(options));
          else
              return f.or(randomFEGen(options));
      }
  }
  
  public static FeatureExpr randomFEComlexity(NonStaticFeature[] options, int size) {
      if(options.length == 0) return FeatureExprFactory.True();
      if(options.length == 1) return Math.random() < 0.5 ? options[0].a : options[0].a.not();
      if (size == 0) return FeatureExprFactory.True();
      if (options.length == size)
          return randomFEGen(options);
      else {
          int sz = (int) (Math.random() * (size + 1));
          // System.out.println(sz);
          FeatureExpr f = options[(int) (Math.random() * options.length)].a;
          for (int i = 0; i < sz - 1; i++) {
              if (Math.random() < 0.5) {
                  f = f.and(options[(int) (Math.random() * options.length)].a);
              } else {
                  f = f.or(options[(int) (Math.random() * options.length)].a);
              }
          }
          if (Math.random() < 0.8) {
              return f;
          } else {
              return f.not();
          }
      }
  }
  
  public static Conditional<Integer> randomCIGen(NonStaticFeature[] options, int sz) {
      if (sz == 0)
          return One.valueOf((int) (Math.random() * 100000));
      else {
          return ChoiceFactory.create(randomFEComlexity(options, 1), randomCIGen(options, sz - 1), randomCIGen(options, sz - 1));
      }
  }
  
  public static Conditional<Integer> ratioGen(NonStaticFeature[] options, double ratio, int sz) {
      if (Math.random() < ratio) {
          return randomCIGen(options, sz);
      } else {
          return randomCIGen(options, 0);
      }
  }
  
  

  public static void GenValues(NonStaticFeature[] options, int pushNum, double ratio, String[] operations,
      Conditional<Integer>[] conditionalValues) {
      LinkedList<Conditional<Integer>> values = new LinkedList<>();
      for (int i = 0; i < pushNum * ratio; i++) {
          values.add(randomCIGen(options, 1));
      }
      for (int i = 0; i < pushNum - pushNum * ratio; i++) {
          values.add(randomCIGen(options, 0));
      }
      if (flag) {
          System.out.println("pushNum" + pushNum);
          for (int i = 0; i < values.size(); i++) {
             // System.out.print(values.get(i) + " ");
          }
  
      }
      Collections.shuffle(values);
  
      for (int i = 0; i < operations.length; i++) {
          if (operations[i] == "pop")
              conditionalValues[i] = new One(0);
          else {
              conditionalValues[i] = values.poll();
          }
      }
  }

}
