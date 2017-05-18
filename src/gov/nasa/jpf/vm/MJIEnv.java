//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
//
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
//
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.vm;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;



/**
 * MJIEnv is the call environment for "native" methods, i.e. code that
 * is executed by the VM, not by JPF.
 *
 * Since library abstractions are supposed to be "user code", we provide
 * this class as a (little bit of) insulation towards the inner JPF workings.
 *
 * There are two APIs exported by this class. The public methods (like
 * getStringObject) don't expose JPF internals, and can be used from non
 * gov.nasa.jpf.vm NativePeer classes). The rest is package-default
 * and can be used to fiddle around as much as you like to (if you are in
 * the ..jvm package)
 *
 * Note that MJIEnv objects are now per-ThreadInfo (i.e. the variable
 * call envionment only includes MethodInfo and ClassInfo), which means
 * MJIEnv can be used in non-native methods (but only carefully, if you
 * don't need mi or ciMth).
 *
 * Note also this only works because we are not getting recursive in
 * native method calls. In fact, the whole DirectCallStackFrame / repeatTopInstruction
 * mechanism is there to turn logial recursion (JPF calling native, calling
 * JPF, calling native,..) into iteration. Otherwise we couldn't backtrack
 */
public class MJIEnv {
  public static final int NULL = 0;
}
