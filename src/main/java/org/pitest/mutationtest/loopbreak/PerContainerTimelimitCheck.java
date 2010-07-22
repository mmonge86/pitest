/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.mutationtest.loopbreak;

import org.pitest.TimeoutException;

public abstract class PerContainerTimelimitCheck {

  // allthough this is static, classloader
  // containment means that it is actual a per
  // test case value
  private static long maxTime;

  public static void breakIfTimelimitExceeded() {
    if ((System.currentTimeMillis() > maxTime) && (maxTime != 0)) {
      throw new TimeoutException("Stopping test as max time exceeded");
    }
  }

  public static void setMaxEndTime(final long maxtime) {
    maxTime = maxtime;
  }

}
