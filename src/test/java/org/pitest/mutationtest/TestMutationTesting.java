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
package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.annotations.MutationSuite;
import org.pitest.annotations.TestClass;
import org.pitest.containers.UnContainer;
import org.pitest.extension.TestListener;
import org.pitest.testutil.ConfigurationForTesting;
import org.pitest.testutil.IgnoreAnnotationForTesting;
import org.pitest.testutil.TestAnnotationForTesting;

public class TestMutationTesting {

  private Pitest       pit;

  @Mock
  private TestListener listener;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.pit = new Pitest(new UnContainer(), new ConfigurationForTesting());
    this.pit.addListener(this.listener);
  }

  public static class NoMutations {

  }

  public static class OneMutation {
    public static int returnOne() {
      return 1;
    }
  }

  public static class ThreeMutations {
    public static int returnOne() {
      return 1;
    }

    public static int returnTwo() {
      return 2;
    }

    public static int returnThree() {
      return 3;
    }
  }

  @TestClass(OneMutation.class)
  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class OneMutationFullTest {
    @TestAnnotationForTesting
    public void testReturnOne() {
      assertEquals(1, OneMutation.returnOne());
    }
  }

  @Test
  public void testMutationTestPassesIfAllMutationsKilled() {
    this.pit.run(OneMutationFullTest.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  @TestClass(ThreeMutations.class)
  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class ThreeMutationsTwoTests {
    @TestAnnotationForTesting
    public void testReturnOne() {
      assertEquals(1, ThreeMutations.returnOne());
    }

    @TestAnnotationForTesting
    public void testReturnTwo() {
      assertEquals(2, ThreeMutations.returnTwo());
    }

  }

  @Test
  public void testMutationTestReportsFailIfDetectsLessThanThresholdPercentOfMutations() {
    this.pit.run(ThreeMutationsTwoTests.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  @TestClass(ThreeMutations.class)
  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class FailingTest {
    @TestAnnotationForTesting
    public void fail() {
      assertEquals(1, 2);
    }
  }

  @Test
  public void testReportsErrorInMutationTestIfTestsFailsWithoutMutation() {
    this.pit.run(FailingTest.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
    verify(this.listener, times(1)).onTestError((any(TestResult.class)));
  }

  @TestClass(NoMutations.class)
  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class NoMutationsTest {
    @TestAnnotationForTesting
    public void pass() {

    }
  }

  @Test
  public void testSkipsTestIfNoMutationsPossible() {
    this.pit.run(NoMutationsTest.class);
    verify(this.listener, times(1)).onTestSuccess((any(TestResult.class)));
    verify(this.listener, times(1)).onTestSkipped((any(TestResult.class)));
  }

  @TestClass(ThreeMutations.class)
  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class NoTests {
    @TestAnnotationForTesting
    @IgnoreAnnotationForTesting
    public void fail() {
      assertEquals(1, 2);
    }
  }

  @Test
  public void testReportsFailureIfNoTestsAvailable() {
    this.pit.run(NoTests.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class OneMutationTest {

  }

  @Test
  public void testGuessesCorrectTesteeNameWhenTestNameEndsWithTest() {
    this.pit.run(OneMutationTest.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  @MutationSuite(threshold = 100, mutators = Mutator.RETURN_VALS)
  public class TestOneMutation {

  }

  @Test
  public void testGuessesCorrectTesteeNameWhenTestNameStartsWithTestForInnerClass() {
    this.pit.run(TestOneMutation.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  @MutationSuite(threshold = 100, mutators = Mutator.SWITCHES)
  public class TestMutable {

  }

  @Test
  public void testGuessesCorrectTesteeNameWhenTestNameStartsWithTest() {
    this.pit.run(TestMutable.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  public static class InfiniteLoop {
    public static void loop() {
      int i = 1;
      do {
        i++;
        System.out.println("loop");
      } while (i == 1);
    }
  }

  @TestClass(InfiniteLoop.class)
  @MutationSuite(threshold = 100, mutators = Mutator.INCREMENTS)
  public static class InfiniteLoopTest {
    @TestAnnotationForTesting()
    public void pass() {
      InfiniteLoop.loop();
    }
  }

  @Test(timeout = 2000)
  public void testInfiniteLoopsCausedByMutationsAreBroken() {
    this.pit.run(InfiniteLoopTest.class);
    // pass if we get here without a timeout
  }

}
