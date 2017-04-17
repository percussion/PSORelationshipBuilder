package com.percussion.pso.relationshipbuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

public class PSRelationshipBuilderExitHelperTest extends TestCase
{

   private Set<Integer> m_output;

   /* (non-Javadoc)
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      m_output = new HashSet<Integer>();
   }

   public void testConvertRejectsNullOutput()
   {
      boolean threw = false;
      try
      {
         PSRelationshipBuilderExitHelper.convert(null, null);
      }
      catch (IllegalArgumentException e)
      {
         threw = true;
      }
      assertTrue(threw);
   }

   public void testConvertHandlesNullInput()
   {
      Collection<Object> invalids = PSRelationshipBuilderExitHelper.convert(null, m_output);
      assertNotNull(invalids);
      assertEquals(0, invalids.size());
   }

   public void testConvertHandlesAllNullsInInput()
   {
      Object[] input = new Object[] {null, null};
      Collection<Object> invalids = PSRelationshipBuilderExitHelper.convert(input, m_output);
      assertNotNull(invalids);
      assertEquals(2, invalids.size());
   }

   public void testConvertHandlesNullsInInput()
   {
      Object[] input = new Object[] {"700", null, 301};
      Collection<Object> invalids = PSRelationshipBuilderExitHelper.convert(input, m_output);
      assertNotNull(invalids);
      assertEquals(1, invalids.size());
      assertEquals(2, m_output.size());
   }
   
   public void testConvertHandlesEmptysInInput()
   {
      Object[] input = new Object[] {"700", "", 301};
      Collection<Object> invalids = PSRelationshipBuilderExitHelper.convert(input, m_output);
      assertNotNull(invalids);
      assertEquals(1, invalids.size());
      assertEquals(2, m_output.size());
   }
   
   public void testConvertSingleEmptyString() {
	      Object[] input = new Object[] {""};
	      Collection<Object> invalids = PSRelationshipBuilderExitHelper.convert(input, m_output);
	      assertNotNull(invalids);
	      assertEquals(1, invalids.size());
	      assertEquals(0, m_output.size());
          if (invalids.size() == 1 && invalids.contains("")) {
       	   
          }
          else {
        	  fail();
          }
   }
 

}
