/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.speedtracer.client.util;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Tests the WorkQueue class.
 */
public class WorkQueueTests extends GWTTestCase {
  WorkQueue workQueue;

  private class PrependWorkNode implements WorkQueue.Node {
    private int value;

    public PrependWorkNode(int value) {
      this.value = value;
    }

    public void execute() {
      workOrder++;
      sum += workOrder * value;
    }

    public String getDescription() {
      return "prepend worker";
    }
  }

  private class InitialWorkNode implements WorkQueue.Node {
    public InitialWorkNode() {
    }

    public void execute() {
      int expectedValue = 1 * 10 + 2 * 100 + 3 * 1000 + 4 * 10000 + 5 * 100000;
      assertEquals("Sum", expectedValue, sum);
      finishTest();
    }

    public String getDescription() {
      return "sum worker";
    }
  }

  private int sum = 0;
  private int workOrder = 0;

  @Override
  public String getModuleName() {
    return "com.google.speedtracer.Common";
  }

  public void testWorkQueue1() {
    delayTestFinish(2000);
    workQueue = new WorkQueue();
    workQueue.append(new InitialWorkNode());

    workQueue.prepend(new PrependWorkNode(100000));
    workQueue.prepend(new PrependWorkNode(10000));
    workQueue.prepend(new PrependWorkNode(1000));
    workQueue.prepend(new PrependWorkNode(100));
    workQueue.prepend(new PrependWorkNode(10));
  }
}
