/******************************************************************************
 *
 * [ PSDependentRelationshipBuilder.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.IPSRelationshipProcessor;
import com.percussion.cms.objectstore.PSAaRelationship;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;

/**
 * Creates and deletes auto relationships where the parent item (item being
 * updated) is the <em>dependent</em> of the relationship and the child items
 * (items to be related) are the <em>owner</em>.
 * 
 * @author Adam Gent
 * @author James Schultz
 * @since 6.0
 * @see com.percussion.pso.relationshipbuilder.PSOwnerRelationshipBuilder
 * @see #extractIdsFromSlot(int, String)
 */
public class PSDependentRelationshipBuilder extends PSAbstractRelationshipBuilder
{
   /**
    * Constructs an instance of <code>PSChildRelationshipBuilder</code> that
    * will use the specified relationship processor for managing relationships.
    * 
    * @param relProcessor used to query, add, and delete relationships, not
    *           <code>null</code>
    * @throws IllegalArgumentException if relProcessor is <code>null</code>
    */
   public PSDependentRelationshipBuilder(IPSRelationshipProcessor relProcessor)
   {
      super(relProcessor);
   }

   /**
    * Synchronizes relationships in a slot with the specified dependent item to
    * match the supplied owner item array. Relationships are created for items
    * in the owner array without them, using the specified template. Any
    * existing relationships owned by items not in the owner array are removed.
    * 
    * @param dependentId item that is the dependent of all relationships
    * @param desiredOwnerIds only relationships owned by these ids should have the
    *           dependentId
    * @param slotName the name of the slot whose relationships between
    *           <code>ownerIds</code> and <code>dependentId</code> will be
    *           synchronized.
    * @param templateName template to assign to any created relationships
    * @throws PSCmsException propagated from relationship api errors
    * @throws PSAssemblyException if the slot or template cannot be found by
    *            assembly service.
    */
   @Override
   public void build(final int dependentId, final Set<Integer> desiredOwnerIds, final String slotName,
         final String templateName) throws PSCmsException, PSAssemblyException
   {
      if (StringUtils.isBlank(slotName)) 
      {
         throw new IllegalArgumentException("slotName may not be blank");
      }


      ms_log.debug("\tdesired ids: " + desiredOwnerIds);

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary item = cms.loadComponentSummary(dependentId);
      PSLocator child = item.getHeadLocator();

      IPSAssemblyTemplate template = findTemplate(templateName, item
            .getContentTypeGUID());
      IPSTemplateSlot slot = findSlot(slotName);

      PSRelationshipSet currentRelations = getRelationships(-1, dependentId,
            slot);
      if (currentRelations.isEmpty())
      {
         ms_log.debug("\tno current ids");
         ms_log.debug("\tadd ids = desired ids");
         ms_log.debug("\tno remove ids");
         addRelationships(child, asLocators(desiredOwnerIds), slot, template);

      }
      else
      {
         Set<Integer> currentOwnerIds = new HashSet<Integer>(extractOwnerIds(currentRelations));
         ms_log.debug("\tcurrent ids: " + currentOwnerIds);

         // desired - current = add
         Set<Integer> idsToAdd = createComplement(desiredOwnerIds,
               currentOwnerIds);
         ms_log.debug("\tadd ids: " + idsToAdd);
         validateSlot(slot);
         addRelationships(child, asLocators(idsToAdd), slot, template);

         // current - desired = remove
         Set<Integer> idsToRemove = createComplement(currentOwnerIds,
               desiredOwnerIds);
         ms_log.debug("\tremove ids: " + idsToRemove);
         filterForRelationshipsToRemove(currentRelations, idsToRemove);
         deleteRelationships(currentRelations);
      }
   }

   /**
    * Creates an active assembly relationship between the "active" revision
    * (current or edit) of each item in <code>ownerIds</code> and the
    * dependent item, using the specified slot and template.
    * 
    * @param dependent the item that will be dependent of the relationship,
    *           assumed not <code>null</code>
    * @param owners the items to own the relationships, assumed not
    *           <code>null</code>
    * @param slot the slot that will be assigned to the relationship, assumed
    *           not <code>null</code>
    * @param template the template that will be assigned to the relationship,
    *           assumed not <code>null</code>
    * 
    * @throws PSCmsException propagated if an error occurs saving the
    *            relationships
    */
   private void addRelationships(PSLocator dependent,
         Collection<PSLocator> owners, IPSTemplateSlot slot,
         IPSAssemblyTemplate template) throws PSCmsException
   {

      // create relationships
      PSRelationshipSet newRelationships = new PSRelationshipSet();
      for (PSLocator owner : owners)
      {
         PSAaRelationship newRelationship = new PSAaRelationship(owner,
               dependent, slot, template);
         newRelationships.add(newRelationship);
      }
      saveRelationships(newRelationships);
   }

   /**
    * Removes any relationships from the set that are not owned by an item id in
    * the <code>idsToRemove</code> collection. This leaves the set with only
    * those relationships that should be deleted.
    * 
    * @param relationships modified
    * @param idsToRemove list of owner ids whose relationships should remain in
    *           the set, because those relationships should be deleted. Assumed
    *           not <code>null</code>, may be empty.
    */
   private void filterForRelationshipsToRemove(PSRelationshipSet relationships,
         Collection<Integer> idsToRemove)
   {
      ms_log.debug("to be removed: " + idsToRemove);

      // remove any relationships from the set that are not being removed
      for (Iterator iter = relationships.iterator(); iter.hasNext();)
      {
         PSRelationship relationship = (PSRelationship) iter.next();
         Integer ownerId = Integer.valueOf((relationship.getOwner().getId()));
         if (!idsToRemove.contains(ownerId))
         {
            iter.remove();
         }
      }
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log ms_log = LogFactory
         .getLog(PSDependentRelationshipBuilder.class);

   /**
    * Extracts the content ids from the owners of relationships in the specified
    * slot with the specified item as the dependent.
    * 
    * @param dependentId id of the item that must be the dependent of
    *           relationship
    * @param slotName name of the slot to be queried, not blank.
    * @return ids of relationship owners from the specified slot with the
    *         specified child id, or <code>null</code> if there are no
    *         matching relationships
    * @throws PSAssemblyException propagated from assembly service, if there are
    *            problems loading the slot
    * @throws PSCmsException propagated from relationship API, if there are
    *            problems querying relationships
    */

   @Override
   public List<Integer> extractIdsFromSlot(int dependentId, String slotName)
         throws PSAssemblyException, PSCmsException
   {
      List<Integer> ownerContentIds = null;
      if (StringUtils.isBlank(slotName)) 
      {
         throw new IllegalArgumentException("slotName may not be blank");
      }
      IPSTemplateSlot slot = findSlot(slotName);
      PSRelationshipSet relationships = getRelationships(-1, dependentId, slot);
      if (relationships != null)
      {
         ownerContentIds = extractOwnerIds(relationships);
      }

      // a null return means do not change field value
      return ownerContentIds;
   }

}
