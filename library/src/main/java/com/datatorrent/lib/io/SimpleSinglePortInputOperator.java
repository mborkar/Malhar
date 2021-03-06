/*
 * Copyright (c) 2013 DataTorrent, Inc. ALL Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.datatorrent.lib.io;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

import com.datatorrent.api.*;
import com.datatorrent.api.Context.OperatorContext;
import com.datatorrent.api.annotation.OutputPortFieldAnnotation;

/**
 * A simple Base class for input operator with a single output port without recovery.
 * <p>
 * Handles hand over from asynchronous input to port processing thread (tuples
 * must be emitted by container thread). If derived class implements
 * {@link Runnable} to perform synchronous IO, this class will manage the thread
 * according to the operator lifecycle.
 *
 * @since 0.3.2
 */
public class SimpleSinglePortInputOperator<T> extends BaseOperator implements InputOperator, ActivationListener<OperatorContext>
{
  private transient Thread ioThread;
  private transient boolean isActive = false;
  /**
   * The single output port of this input operator.
   * Collects asynchronously emitted tuples and flushes in container thread.
   */
  @OutputPortFieldAnnotation(name = "outputPort")
  final public transient BufferingOutputPort<T> outputPort;

  public SimpleSinglePortInputOperator(int portCapacity)
  {
    outputPort = new BufferingOutputPort<T>(this, portCapacity);
  }

  public SimpleSinglePortInputOperator()
  {
    this(1024);
  }

  @Override
  final public void activate(OperatorContext ctx)
  {
    isActive = true;
    if (this instanceof Runnable) {
      ioThread = new Thread((Runnable)this, "io-" + this.getName());
      ioThread.start();
    }
  }

  @Override
  final public void deactivate()
  {
    isActive = false;
    if (ioThread != null) {
      // thread to exit sleep or blocking IO
      ioThread.interrupt();
    }
  }

  final public boolean isActive()
  {
    return isActive;
  }

  @Override
  public void emitTuples()
  {
    outputPort.flush(Integer.MAX_VALUE);
  }

  public static class BufferingOutputPort<T> extends DefaultOutputPort<T>
  {
    public final ArrayBlockingQueue<T> tuples;

    /**
     * @param operator
     */
    public BufferingOutputPort(Operator operator)
    {
      super();
      tuples = new ArrayBlockingQueue<T>(1024);
    }

    public BufferingOutputPort(Operator operator, int capacity)
    {
      super();
      tuples = new ArrayBlockingQueue<T>(capacity);
    }

    @Override
    public synchronized void emit(T tuple)
    {
      try {
        tuples.put(tuple);
      }
      catch (InterruptedException ex) {
        throw new RuntimeException(ex);
      }
    }

    public synchronized void flush(int count)
    {
      Iterator<T> iterator = tuples.iterator();
      while (count-- > 0 && iterator.hasNext()) {
        super.emit(iterator.next());
        iterator.remove();
      }
    }

  };

}
