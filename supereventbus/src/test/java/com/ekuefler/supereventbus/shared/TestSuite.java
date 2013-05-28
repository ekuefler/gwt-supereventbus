package com.ekuefler.supereventbus.shared;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BasicTest.class,
    CacheTest.class,
    ExceptionTest.class,
    FilteringTest.class,
    OrderingTest.class,
    PolymorphismTest.class,
    PriorityTest.class})
public class TestSuite {}
