/******************************************************************************
 *
 * [ PSExtractOwnerIdsFromSlotExit.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * relationship owners in a specific slot.
 * 
 * @author James Schultz
 * @author Adam Gent
 * @since 6.0
 */
public class PSExtractOwnerIdsFromSlotExit extends PSDefaultExtension
      implements
         IPSUdfProcessor, IPSFieldOutputTransformer
{


   /**
    * Returns a list of the owner content ids from the relationships in the slot
    * identified by the "slotname" parameter that have the request's content
    * item as their dependent.
    * 
    * @param params the parameter values. required: "slotname" for name of the
    *           slot whose relationships will be used.
    * @param request the current request context.
    * @return a list of owner content ids as ";" delimited string from the
    *         slot's matching relationships, or <code>null</code> if there are
    *         no matching relationships, e.g. <code>692;651;339</code>.
    * @throws PSConversionException if request does not include a sys_contentid
    *            parameter, if "slotname" parameter is missing or empty, if slot
    *            cannot be found, or if relationship API throws exception.
    */
   public Object processUdf(Object[] params, IPSRequestContext request)
         throws PSConversionException
   {

      PSDependentRelationshipBuilder builder;
      try
      {
         builder = new PSDependentRelationshipBuilder(
               new PSRelationshipProcessor(request));
         PSRelationshipBuilderExitHelper helper = new PSRelationshipBuilderExitHelper(
               builder, getParameters(params));
         return helper.processUdf(request);
      }
      catch (PSCmsException e)
      {
         ms_log.error("Problems loading Relationship Proxy");
         throw new RuntimeException(e); // NOPMD by agent on 11/28/06 10:18 AM
      }

   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static Log ms_log = LogFactory
         .getLog(PSExtractOwnerIdsFromSlotExit.class);
}
