/******************************************************************************
 *
 * [ PSAaDependentRelationshipBuilder.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import static java.util.Collections.singleton;

import java.util.Collection;

import com.percussion.error.PSException;
import com.percussion.services.assembly.PSAssemblyException;

/**
 * Creates and deletes auto relationships where the parent item (item being
 * updated) is the <em>dependent</em> of the relationship and the child items
 * (items to be related) are the <em>owner</em>.
 * 
 * @author Adam Gent
 * @author James Schultz
 * @since 6.0
 * @see com.percussion.pso.relationshipbuilder.PSAaOwnerRelationshipBuilder
 * @see #retrieve(int)
 */
public class PSAaDependentRelationshipBuilder 
    extends PSActiveAssemblyRelationshipBuilder
{
   
   @Override
   public void add(int sourceId, Collection<Integer> targetIds) throws PSAssemblyException,
           PSException {
       m_relationshipHelperService.addRelationships(targetIds, singleton(sourceId),
                m_slotName, m_templateName);

   }

   @Override
   public void delete(int sourceId, Collection<Integer> targetIds) throws PSException {
       m_relationshipHelperService.deleteRelationships(targetIds, singleton(sourceId),
                m_slotName, m_templateName);

   }

   public Collection<Integer> retrieve(int sourceId) throws PSException {
       return m_relationshipHelperService.getOwners(sourceId, m_slotName,
               m_templateName);
   }

}
