/******************************************************************************
 *
 * [ PSExtractDependentIdsFromSlotExit.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSFieldOutputTransformer;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.server.IPSRequestContext;

/**
 * This class is intended to be used as a FieldOutputTranslation within a
 * content editor, to populate a field's value with a list of content ids of the
 * relationship dependents in a specific slot.
 * 
 * @author Adam Gent
 * @since 6.0
 */
public class PSExtractDependentIdsFromSlotExit extends PSDefaultExtension
      implements
         IPSUdfProcessor, IPSFieldOutputTransformer
{

   /**
    * Returns a list of the dependent content ids from the relationships in the
    * slot identified by the "slotname" parameter that have the request's
    * content item as their dependent.
    * 
    * @param params the parameter values. required: "slotname" for name of the
    *           slot whose relationships will be used.
    * @param request the current request context.
    * @return a list of dependent content ids as ";" delimited string from the
    *         slot's matching relationships, or <code>null</code> if there are
    *         no matching relationships, e.g. <code>692;651;339</code>.
    * @throws PSConversionException if request does not include a sys_contentid
    *            parameter, if "slotname" parameter is missing or empty, if slot
    *            cannot be found, or if relationship API throws exception.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {

      PSAbstractRelationshipBuilder builder;
      try
      {
         builder = new PSOwnerRelationshipBuilder(new PSRelationshipProcessor(
               request));
         PSRelationshipBuilderExitHelper helper = new PSRelationshipBuilderExitHelper(
               builder, getParameters(params));
         return helper.processUdf(request);
      }
      catch (PSCmsException e)
      {
         throw new RuntimeException(e); // NOPMD by agent on 11/28/06 10:43 AM
      }
   }

}
