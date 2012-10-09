/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.malhartech.demos.performance;

import com.malhartech.annotation.ModuleAnnotation;
import com.malhartech.annotation.PortAnnotation;
import com.malhartech.annotation.PortAnnotation.PortType;
import com.malhartech.dag.AbstractModule;
import com.malhartech.dag.Component;

/**
 *
 * @author Chetan Narsude <chetan@malhar-inc.com>
 */
@ModuleAnnotation(ports = {
  @PortAnnotation(name = Component.INPUT, type = PortType.INPUT),
  @PortAnnotation(name = Component.OUTPUT, type = PortType.OUTPUT)
})
public class DoNothingModule extends AbstractModule
{
  private static final long serialVersionUID = 201208061821L;

  @Override
  public void process(Object payload)
  {
    emit(payload);
  }
}