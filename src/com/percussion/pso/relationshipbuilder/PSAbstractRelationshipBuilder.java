/******************************************************************************
 *
 * [ PSAbstractRelationshipBuilder.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.pso.relationshipbuilder;

import java.util.ArrayList;
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
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSRelationshipFilter;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.IPSAssemblyTemplate;
import com.percussion.services.assembly.IPSTemplateSlot;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.assembly.PSAssemblyServiceLocator;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;

/**
 * Base class for querying and updating "category"-style auto relationships.
 * 
 * @author James Schultz
 * @author Adam Gent
 * @since 6.0
 */
public abstract class PSAbstractRelationshipBuilder
{
   /**
    * Creates an instance of this class that will use the specified processor
    * for querying and updating relationships.
    * 
    * @param relProcessor processor for querying and updating relationships, not
    *           <code>null</code>
    */
   protected PSAbstractRelationshipBuilder(IPSRelationshipProcessor relProcessor)
   {
      if (relProcessor == null)
      {
         throw new IllegalArgumentException(
               "IPSRelationshipProcessor may not be null");
      }
      setRelationshipProcessor(relProcessor);
   }

   /**
    * Finds the definition for a slot given its name, using the assembly
    * service.
    * 
    * @param slotname name of the slot to find. not <code>null</code>, must
    *           exist.
    * @return the slot definition for the specified name
    * @throws PSAssemblyException propagated from assembly service if the slot
    *            is not found
    */
   protected IPSTemplateSlot findSlot(String slotname)
         throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      return asm.findSlotByName(slotname);
   }

   /**
    * Validates that the slot is setup correctly to add relationships to. Emits
    * log messages to help the user find errors.
    * 
    * @param slot
    * @return 0 if successful, non-zero otherwise.
    */
   protected int validateSlot(IPSTemplateSlot slot)
   {
      int rvalue = 1;
      if (slot.getRelationshipName() == null
            || StringUtils.isBlank(slot.getRelationshipName()))
      {
         m_log
               .warn("The slot does not have relationship name set."
                     + "The relationship name should be active assembly."
                     + "Check the Slot type table to make sure the relationship name is set.");
         rvalue = 1;
      }
      else
      {
         rvalue = 0;
      }
      
      return rvalue;
   }

   /**
    * Finds the definition for a slot given its name, using the assembly
    * service.
    * 
    * @param templateName name of the template to find. not <code>null</code>,
    *           must exist.
    * @return the template definition for the specified name, never
    *         <code>null</code>
    * @throws PSAssemblyException if the template is not found
    */
   protected IPSAssemblyTemplate findTemplate(String templateName, IPSGuid type)
         throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      IPSAssemblyTemplate template = asm.findTemplateByNameAndType(
            templateName, type);
      return template;
   }

   /**
    * Finds the definition for a slot given its name, using the assembly
    * service.
    * 
    * @param templateName name of the template to find. not <code>null</code>,
    *           must exist.
    * @throws PSAssemblyException if the template is not found
    */
   protected IPSAssemblyTemplate findTemplate(String templateName)
         throws PSAssemblyException
   {
      IPSAssemblyService asm = PSAssemblyServiceLocator.getAssemblyService();
      return asm.findTemplateByName(templateName);
   }

   /**
    * Gets all relationships in the specified slot whose dependent item matches
    * the specified content item, limited to only the current/edit revision of
    * the owner items.
    * 
    * @param ownerContentId the owner content id to filter if -1 or null do not
    *           filter
    * @param dependentContentId content item that is the dependent of all
    *           relationships, if -1 or null do not filter on
    * @param slot slot whose relationships will be queried, assumed not
    *           <code>null</code>
    * 
    * @return the set of relationships for the specified slot with the specified
    *         dependent item. never <code>null</code>, may be emptty.
    */
   protected PSRelationshipSet getRelationships(int ownerContentId,
         int dependentContentId, IPSTemplateSlot slot) throws PSCmsException
   {
      // setup the relationship filter
      PSRelationshipFilter filter = new PSRelationshipFilter();
      if (ownerContentId != -1)
      {
         filter.setOwner(new PSLocator(ownerContentId));
      }
      if (dependentContentId != -1)
      {
         filter.setDependent(new PSLocator(dependentContentId));
      }

      filter.limitToEditOrCurrentOwnerRevision(true);
      filter.setProperty(IPSHtmlParameters.SYS_SLOTID, String.valueOf(slot
            .getGUID().longValue()));

      // get the relationships
      PSRelationshipSet relationships = m_relationshipProcessor
            .getRelationships(filter);
      return relationships;
   }

   /**
    * Deletes the specified relationships using the
    * <code>PSRelationshipProcessor</code>.
    * 
    * @param toBeDeleted set of relationships to be deleted. assumed not
    *           <code>null</code>, may be empty.
    * @throws PSCmsException propagated from
    *            <code>PSRelationshipProcessor</code>
    */
   protected void deleteRelationships(PSRelationshipSet toBeDeleted)
         throws PSCmsException
   {
      if (toBeDeleted.size() > 0) 
      {
         m_relationshipProcessor.delete(toBeDeleted);
      }
   }

   /**
    * Saves the specified relationships using the
    * <code>PSRelationshipProcessor</code>.
    * 
    * @param toBeSaved set of relationships to be saved. assumed not
    *           <code>null</code>, may be empty.
    * @throws PSCmsException propagated from
    *            <code>PSRelationshipProcessor</code>
    */
   protected void saveRelationships(PSRelationshipSet toBeSaved)
         throws PSCmsException
   {
      if (toBeSaved.size() > 0)
      {
         m_relationshipProcessor.save(toBeSaved);
      }
   }

   /**
    * Extracts the dependent content ids from the specified relationships.
    * 
    * @param relationships set whose owner content ids will be extracted.
    *           Assumed not <code>null</code>, may be empty.
    * @return a list of the relationship set's owner content ids. never
    *         <code>null</code> but will be empty if the relationship set is
    *         empty.
    */
   protected List<Integer> extractDependentIds(PSRelationshipSet relationships)
   {
      // extract owner content ids from the relationship set
      @SuppressWarnings("unchecked")
      Iterator<PSRelationship> iter = relationships.iterator();
      List<Integer> cids = new ArrayList<Integer>();
      while (iter.hasNext())
      {
         PSRelationship rel = iter.next();
         PSLocator owner = rel.getDependent();
         if (cids.contains(owner.getId())){
        	 m_log.debug("\tDuplicate dependent ids " +
        	 		"in relationship set due to revisions." +
        	 		" Skipping id: " + owner.getId());
         }
         else {
        	 cids.add(owner.getId());
         }
      }
      return cids;
   }

   /**
    * Extracts the owner content ids from the specified relationships.
    * 
    * @param relationships set whose owner content ids will be extracted.
    *           Assumed not <code>null</code>, may be empty.
    * @return a list of the relationship set's owner content ids. never
    *         <code>null</code> but will be empty if the relationship set is
    *         empty.
    */
   protected List<Integer> extractOwnerIds(PSRelationshipSet relationships)
   {
      // extract owner content ids from the relationship set
      @SuppressWarnings("unchecked")
      Iterator<PSRelationship> iter = relationships.iterator();
      List<Integer> cids = new ArrayList<Integer>();
      while (iter.hasNext())
      {
         PSRelationship rel = iter.next();
         PSLocator owner = rel.getOwner();
         cids.add(owner.getId());
      }
      return cids;
   }

   /**
    * Creates a new list of the elements in <code>retain</code> that are not
    * in <code>suppress</code>.
    * 
    * @param retain all integers except those also in <code>suppress</code>
    *           are copied to returned list. Assumed not <code>null</code>
    * @param suppress integers to be suppressed from being copied from
    *           <code>retain</code>. Assumed not <code>null</code>
    * @return a new list of the elements in <code>retain</code> that are not
    *         in <code>suppress</code>, never <code>null</code>
    */
   protected Set<Integer> createComplement(final Set<Integer> retain,
         final Set<Integer> suppress)
   {
      Set<Integer> complement = new HashSet<Integer>();
      for (Integer id : retain)
      {
         if (!suppress.contains(id))
         {
            complement.add(id);
         }
      }
      return complement;
   }

   /**
    * Builds a collection of <code>PSLocator</code> using the edit locator for
    * each content id in the <code>idsToAdd</code> parameter.
    * 
    * @param ownerIds content ids to be converted to edit revision locators,
    *           assumed not <code>null</code>, may be empty.
    * @return collection of edit <code>PSLocator</code>s for the content ids
    *         provided. never <code>null</code>, will be empty if
    *         <code>ownerIds</code> parameter is empty.
    */
   protected Collection<PSLocator> asLocators(Collection<Integer> ownerIds)
   {
      Collection<PSLocator> owners = new ArrayList<PSLocator>(ownerIds.size());
      IPSCmsObjectMgr cms = PSCmsObjectMgrLocator.getObjectManager();
      for (Integer contentid : ownerIds)
      {
         PSComponentSummary sum = cms.loadComponentSummary(contentid);
         PSLocator loc = sum.getHeadLocator();
         owners.add(loc);
      }
      return owners;
   }

   /**
    * Extracts content ids from all relationships using the specified slot.
    * 
    * @param itemId of an item in the relationship
    * @param slotName name of the slot to be queried, not blank.
    * @return ids that are related to the item from the specified slot with the
    *         or <code>null</code> if there are no matching relationships
    * @throws PSAssemblyException propagated from assembly service, if there are
    *            problems loading the slot
    * @throws PSCmsException propagated from relationship API, if there are
    *            problems querying relationships
    */
   public abstract List<Integer> extractIdsFromSlot(int itemId, String slotName)
         throws PSAssemblyException, PSCmsException;

   /**
    * Synchronizes relationships in a slot with the specified item to match the
    * supplied related items array. Relationships are created for items in the
    * relatedIds array without them, using the specified template. Any existing
    * relationships owned by items not in the owner array are removed.
    * 
    * @param itemId item that is either the dependent or owner of all
    *           relationships
    * @param relatedIds items that should be related to itemId
    * @param slotName the name of the slot whose relationships between
    *           <code>ownerIds</code> and <code>dependentId</code> will be
    *           synchronized.
    * @param templateName template to assign to any created relationships
    * @throws PSCmsException propagated from relationship api errors
    * @throws PSAssemblyException if the slot or template cannot be found by
    *            assembly service.
    */

   public abstract void build(int itemId, Set<Integer> relatedIds, String slotName,
         String templateName) throws PSCmsException, PSAssemblyException;

   /**
    * Sets the processor that will be used to query, save, and delete
    * relationships.
    * 
    * @param processor processor for querying and updating relationships, not
    *           <code>null</code>
    */
   private void setRelationshipProcessor(IPSRelationshipProcessor processor)
   {
      if (processor == null) {
         throw new IllegalArgumentException(
               "relationship processor may not be null");
      }
      m_relationshipProcessor = processor;
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log m_log = LogFactory.getLog(PSAbstractRelationshipBuilder.class);

   /**
    * Processor for querying and updating relationships. Assigned in ctor, never
    * <code>null</code>.
    */
   private transient IPSRelationshipProcessor m_relationshipProcessor;

}
