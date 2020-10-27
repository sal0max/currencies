package de.salomax.currencies.viewmodel.main

import org.junit.Test

import org.junit.Assert.*

class CalculationsTest {

   @Test
   fun humanReadable() {
      // regular
      assertEquals("123", "123".humanReadable())
      assertEquals("12 345 678", "12345678".humanReadable())
      // decimal
      assertEquals("1 234.12312", "1234.12312".humanReadable())
      // minus
      assertEquals("-111 222", "-111222".humanReadable())
      assertEquals("-11 222", "-11222".humanReadable())
   }

   @Test
   fun scientificToNatural() {
      // test very long input
      assertEquals("0", "NaN".scientificToNatural())
      assertEquals("123456789.12", "1.23456789123456789E8".scientificToNatural())
      // test rounding
      assertEquals("1.11", "1.111".scientificToNatural())
      assertEquals("2.22", "2.215".scientificToNatural())
      // take care of pending zeros
      assertEquals("6", "6.0".scientificToNatural())
      assertEquals("6.1", "6.10".scientificToNatural())
   }

   @Test
   fun evaluateMathExpression() {
      assertEquals("1.0", "1".evaluateMathExpression())
      // multiplication
      assertEquals("1.6801", "1*1.6801".evaluateMathExpression())
      // division
      assertEquals("0.595202666507946", "1/1.6801".evaluateMathExpression())
      assertEquals("0.5", "1/2".evaluateMathExpression())
      // subtraction
      assertEquals("50.0", "100.00001 - 50.00001".evaluateMathExpression())
      // addition
      assertEquals("100.00001", "50 + 50.00001".evaluateMathExpression())
   }

   @Test
   fun testAll() {
       assertEquals(
           "110.53",
           "51 + 100.01111 / 1.6801".evaluateMathExpression().scientificToNatural().humanReadable()
       )
       assertEquals(
           "0",
           "0 / 0".evaluateMathExpression().scientificToNatural().humanReadable()
       )
   }

}
