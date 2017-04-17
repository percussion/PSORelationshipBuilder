/******************************************************************************
 *
 * [ PSBuildRelationshipsFromIdsExit.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.server.PSRelationshipProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;

/**
 * This class is intended to be used as a post-exit on a content editor
 * resource, to create relationships in a specific slot with the request's
 * current item as the dependent and owners determined by a field's value.
 * 
 * @author Adam Gent
 * @since 6.0
 */
public class PSBuildDependentRelationshipsFromIdsExit
      extends
         PSDefaultExtension implements IPSResultDocumentProcessor
{
   /**
    * @return <code>false</code> always.
    */
   public boolean canModifyStyleSheet()
   {
      return false;
   }

   /**
    * Maintains active-assembly-style relationships between the request's
    * content item and a list of content items -- missing relationships will be
    * created, existing relationships with items not in the list will be
    * deleted. The request's content item is the dependent, and the list of
    * content items become its parents. The details of the relationship are
    * provided as parameters (relationship type, slot id, and variant id).
    * 
    * @param params three expected parameters, all required:
    *           <ul>
    *           <li>
    *           <dt>fieldname</dt>
    *           <dd>name of content editor field that contains desired parent
    *           ids</dd>
    *           </li>
    *           <li>
    *           <dt>slotname</dt>
    *           <dd>name of slot whose parents will be synchronized to match
    *           field value</dd>
    *           </li>
    *           <li>
    *           <dt>templatename</dt>
    *           <dd>name of template that will be assigned to created
    *           relationships</dd>
    *           </li>
    *           </ul>
    * @param request the current request context, not <code>null</code>.
    * @param resultDoc the request's result XML document. not modified by this
    *           exit. may be <code>null</code>.
    * @return the supplied <code>resultDoc</code>, without modification
    * @throws PSParameterMismatchException if any required parameter is blank.
    * @throws PSExtensionProcessingException if the assembly or relationship
    *            APIs report an error.
    */
   public Document processResultDocument(Object[] params,
         IPSRequestContext request, final Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      PSAbstractRelationshipBuilder builder;
      try
      {
         builder = new PSDependentRelationshipBuilder(
               new PSRelationshipProcessor(request));
         PSRelationshipBuilderExitHelper helper = new PSRelationshipBuilderExitHelper(
               builder, getParameters(params));
         helper.processResultDocument(request, resultDoc);
      }
      catch (PSCmsException e)
      {
         ms_log.error("Exception while trying to create Relationship Proxy");
         throw new RuntimeException(e); // NOPMD by agent on 11/28/06 10:27 AM
      }
      return resultDoc;
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static Log ms_log = LogFactory
         .getLog(PSBuildDependentRelationshipsFromIdsExit.class);

}
