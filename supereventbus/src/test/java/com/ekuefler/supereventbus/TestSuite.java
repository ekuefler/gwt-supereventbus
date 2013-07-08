/*
 * Copyright 2013 Erik Kuefler
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.ekuefler.supereventbus;

import com.ekuefler.supereventbus.gwt.rebind.EventRegistrationWriterTest;
import com.ekuefler.supereventbus.shared.BasicTest;
import com.ekuefler.supereventbus.shared.CacheTest;
import com.ekuefler.supereventbus.shared.DeadEventTest;
import com.ekuefler.supereventbus.shared.EventBusAdapterTest;
import com.ekuefler.supereventbus.shared.ExceptionTest;
import com.ekuefler.supereventbus.shared.FilteringTest;
import com.ekuefler.supereventbus.shared.MultiEventTest;
import com.ekuefler.supereventbus.shared.OrderingTest;
import com.ekuefler.supereventbus.shared.PolymorphismTest;
import com.ekuefler.supereventbus.shared.PriorityTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BasicTest.class,
    CacheTest.class,
    DeadEventTest.class,
    EventBusAdapterTest.class,
    EventRegistrationWriterTest.class,
    ExceptionTest.class,
    FilteringTest.class,
    MultiEventTest.class,
    OrderingTest.class,
    PolymorphismTest.class,
    PriorityTest.class})
public class TestSuite {}
