
/**
 * Title:        null<p>
 * Description:  null<p>
 * Copyright:    null<p>
 * Company:      ECONorthwest<p>
 * @author
 * @version null
 */
package com.pb.tlumip.ed;

import com.pb.common.util.Debug;

import java.util.Stack;
import java.util.Vector;


public class SimpleSolver {

private static Vector equation;
private static Stack stack;
private static Object currentElement;
private static DoubleInterface value;

  protected static void solve(Equation e) throws InvalidEquationException, UnknownValueException {
    equation = e.getEquationElements();
    stack = new Stack();
    value = new Parameter(0);
    Variable dependant = (Variable) equation.get(0);
    compute();
    Debug.println("Size = " + String.valueOf(stack.size()));
    if (stack.size() == 1){
      dependant.setValue(((DoubleInterface)stack.pop()).getValue());
    } else {
      throw new InvalidEquationException("Malformed equation: " + e.getName());
    }
  }

  private static void compute() throws InvalidEquationException, UnknownValueException {
    for(int i = 1; i<equation.size(); i++) {
      currentElement = equation.get(i);
      if(currentElement instanceof Operator) {
        compute(((Operator)currentElement).getOperator());
      } else if (currentElement instanceof DoubleInterface) {
        stack.push((DoubleInterface) currentElement);
      } else {
        throw new InvalidEquationException("Invalid object in equation: " + currentElement.toString());
      }
    }
  }



  private static void compute(String o) throws UnknownValueException, InvalidEquationException {
    double d1;
    double d2;

    if (o.equals(Operator.PLUS)) {
      d1 = ((DoubleInterface)stack.pop()).getValue();
      d2 = ((DoubleInterface)stack.pop()).getValue();
      stack.push(new Parameter(d2+d1));
    } else if (o.equals(Operator.MINUS)){
      d1 = ((DoubleInterface)stack.pop()).getValue();
      d2 = ((DoubleInterface)stack.pop()).getValue();
      stack.push(new Parameter(d2-d1));
    }else if (o.equals(Operator.MULTIPLY)){
      d1 = ((DoubleInterface)stack.pop()).getValue();
      d2 = ((DoubleInterface)stack.pop()).getValue();
      stack.push(new Parameter(d2*d1));
    }else if (o.equals(Operator.DIVIDE)){
      d1 = ((DoubleInterface)stack.pop()).getValue();
      d2 = ((DoubleInterface)stack.pop()).getValue();
      stack.push(new Parameter(d2/d1));
    }else if (o.equals(Operator.NATURALLOG)) {
       d1 = ((DoubleInterface)stack.pop()).getValue();
      stack.push(new Parameter(Math.log(d1)));
    }else if (o.equals(Operator.POWER)) {
       d1 = ((DoubleInterface)stack.pop()).getValue();
       d2 = ((DoubleInterface)stack.pop()).getValue();
       stack.push(new Parameter(Math.pow(d2, d1)));
    }else if (o.equals(Operator.EXP)) {
    	d1 = ((DoubleInterface)stack.pop()).getValue();
    	stack.push(new Parameter(Math.exp(d1)));
    }else {
      throw new InvalidEquationException("Invalid Operator");
    }
  }

}
