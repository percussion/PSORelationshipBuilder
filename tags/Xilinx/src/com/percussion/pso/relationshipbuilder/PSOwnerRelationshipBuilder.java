/******************************************************************************
 *
 * [ PSOwnerRelationshipBuilder.java ]
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
 * updated) is the <em>owner</em> of the relationship and the child items
 * (items to be related) are the <em>dependent</em>.
 * 
 * @author Adam Gent
 * @author James Schultz
 * @since 6.0
 * @see com.percussion.pso.relationshipbuilder.PSDependentRelationshipBuilder
 * @see #extractIdsFromSlot(int, String)
 */
public class PSOwnerRelationshipBuilder extends PSAbstractRelationshipBuilder
{

   public PSOwnerRelationshipBuilder(IPSRelationshipProcessor relProcessor)
   {
      super(relProcessor);
   }

   /**
    * Extracts the content ids from the depedents of relationships in the
    * specified slot with the specified item as the owner.
    * 
    * @param ownerId id of the item that must be the owner of the relationship
    * @param slotName name of the slot to be queried, not blank.
    * @return ids of relationship dependents from the specified slot with the
    *         specified ownerId, or <code>null</code> if there are no matching
    *         relationships
    * @throws PSAssemblyException propagated from assembly service, if there are
    *            problems loading the slot
    * @throws PSCmsException propagated from relationship API, if there are
    *            problems querying relationships
    */
   @Override
   public List<Integer> extractIdsFromSlot(int ownerId, String slotName)
         throws PSAssemblyException, PSCmsException
   {
      List<Integer> dependentContentIds = null;
      if (StringUtils.isBlank(slotName))
      {
         throw new IllegalArgumentException("slotName may not be blank");
      }
      IPSTemplateSlot slot = findSlot(slotName);
      PSRelationshipSet relationships = getRelationships(ownerId, -1, slot);
      if (relationships != null)
      {
         dependentContentIds = extractDependentIds(relationships);
         
      }

      // a null return means do not change field value
      return dependentContentIds;
   }

   /**
    * Builds relationships as follows:
    * 
    * @param ownerId item that is the owner of all relationships
    * @param desiredDependentIds ids of the items that should be the dependents
    * @param slotName the name of the slot whose relationships between
    *           <code>ownerIds</code> and <code>dependentId</code> will be
    *           synchronized.
    * @param templateName template to assign to any created relationships
    * @throws PSCmsException propagated from relationship api errors
    * @throws PSAssemblyException if the slot or template cannot be found by
    *            assembly service.
    */
   @Override
   public void build(int ownerId, Set <Integer> desiredDependentIds, String slotName,
         String templateName) throws PSCmsException, PSAssemblyException
   {

      if (StringUtils.isBlank(slotName)) 
      {
         throw new IllegalArgumentException("slotName may not be blank");
      }
      
      ms_log.debug("\tdesired ids: " + desiredDependentIds);

      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      PSComponentSummary item = cms.loadComponentSummary(ownerId);
      PSLocator owner = item.getHeadLocator();

      // Notice we only find the templates by name instead of name and type
      // (DependentRelationshipNuilder uses the name and content type)
      IPSAssemblyTemplate template = findTemplate(templateName);
      IPSTemplateSlot slot = findSlot(slotName);

      PSRelationshipSet currentRelations = getRelationships(ownerId, -1, slot);
      if (currentRelations.isEmpty())
      { 
         ms_log.debug("\tno current ids");
         ms_log.debug("\tadd ids = desired ids");
         ms_log.debug("\tno remove ids");
         addRelationships(owner, asLocators(desiredDependentIds), slot,
               template);
      }
      else
      {
         Set<Integer> currentDependentIds = new HashSet<Integer>(extractDependentIds(currentRelations));
         ms_log.debug("\tcurrent ids: " + currentDependentIds);

         // desired - current = add
         Set<Integer> idsToAdd = createComplement(desiredDependentIds,
               currentDependentIds);
         validateSlot(slot);
         ms_log.debug("\tadd ids: " + idsToAdd);
         addRelationships(owner, asLocators(idsToAdd), slot, template);

         // current - desired = remove
         Set<Integer> idsToRemove = createComplement(currentDependentIds,
               desiredDependentIds);
         ms_log.debug("\tremove ids:" + idsToRemove);
         filterForRelationshipsToRemove(currentRelations, idsToRemove);
         deleteRelationships(currentRelations);
      }
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
         Integer ownerId = Integer.valueOf(relationship.getDependent().getId());
         if (!idsToRemove.contains(ownerId))
         {
            iter.remove();
         }
      }
   }

   /**
    * Creates an active assembly relationship between the "active" revision
    * (current or edit) of each item in <code>ownerIds</code> and the
    * dependent item, using the specified slot and template.
    * 
    * @param owner the item that will be dependent of the relationship, assumed
    *           not <code>null</code>
    * @param dependents content ids of the items to own the relationships,
    *           assumed not <code>null</code>
    * @param slot the slot that will be assigned to the relationship, assumed
    *           not <code>null</code>
    * @param template the template that will be assigned to the relationship,
    *           assumed not <code>null</code>
    * 
    * @throws PSCmsException propagated if an error occurs saving the
    *            relationships
    */
   private void addRelationships(PSLocator owner,
         Collection<PSLocator> dependents, IPSTemplateSlot slot,
         IPSAssemblyTemplate template) throws PSCmsException
   {

      // create relationships
      PSRelationshipSet newRelationships = new PSRelationshipSet();
      for (PSLocator dependent : dependents)
      {
         PSAaRelationship newRelationship = new PSAaRelationship(owner,
               dependent, slot, template);
         newRelationships.add(newRelationship);
      }
      saveRelationships(newRelationships);
   }
   
   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log ms_log = LogFactory
         .getLog(PSOwnerRelationshipBuilder.class);
}
