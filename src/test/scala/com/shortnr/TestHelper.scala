package com.shortnr.tests

import org.specs2.specification._
import org.specs2.mutable.Specification

trait BeforeAllAfterAll extends Specification {
  override def map(fragments: => Fragments) = 
    Step(beforeAll) ^ fragments ^ Step(afterAll)

  protected def beforeAll
  protected def afterAll
}