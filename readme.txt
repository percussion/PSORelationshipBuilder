Relationship Builder

This project was created to have the ability to 
add relationships through the content editor instead of the active assembly editors.


Installation:
 
  1. Download and install apache ant if you have not done so already. 
  2. Set the evironment variable RHYTHMYX_HOME to your Rhythmyx directory.
  3. Run the command:
      ant install
     While in the Relationship Builder directory.
  
  
Setup:

For creating relationships where the content item being edited is the owner of the relationship
you will use the following exits:
	* pso_ExtractDependentIdsFromSlot - field output translation exit
	* pso_BuildOwnerRelationshipsFromIds - a post result document exist


Create a slot and a template to be used in the relationship. 
!!! Make sure that when you create the slot in the workbench that relationship 
type/name does get set. You may have to save the slot twice to get this working in 6.0 !!!

Create a query resource that follows the sys_lookup.dtd that queries all the items you 
want to be able to select from. See rxs_Category_auto in FF as an example.

In your content editor you are going to create a dummy field with 
whatever control you like so long as it supports multiple properties.
That field will be used to collect the content ids of the items we are 
trying to build relationships with.

Now for the display mapping with the control sys_CheckBoxGroup and lookup resource of
../pso_YourLookupResourceHere_auto/all_briefs.xml (change to your settings):

<PSXDisplayMapping>
  <FieldRef>categories</FieldRef>
  <PSXUISet>
    <Label>
      <PSXDisplayText>Categories:</PSXDisplayText>
    </Label>
    <PSXControlRef id="3256" name="sys_CheckBoxGroup"/>
    <ErrorLabel>
      <PSXDisplayText>Categories:</PSXDisplayText>
    </ErrorLabel>
    <PSXChoices sortOrder="ascending" type="internalLookup">
      <PSXUrlRequest>
	<Href>../pso_YourLookupResourceHere_auto/all_briefs.xml</Href>
	<PSXParam name="sys_contentid">
	  <DataLocator>
	    <PSXHtmlParameter id="0">
	      <name>sys_contentid</name>
	    </PSXHtmlParameter>
	  </DataLocator>
	</PSXParam>
	<PSXParam name="sys_revision">
	  <DataLocator>
	    <PSXHtmlParameter id="0">
	      <name>sys_revision</name>
	    </PSXHtmlParameter>
	  </DataLocator>
	</PSXParam>
	<Anchor/>
      </PSXUrlRequest>
    </PSXChoices>                 
  </PSXUISet>
</PSXDisplayMapping>


You may need to make your own custom control to get it so that 
the items that do have a relationship with the item being edited show up already selected.
The provided pso_template.xsl shows an example of how you would make that custom control 
for checkbox group work.

For creating relationships where the content item being edited is the dependent of the relationship
you use pso_ExtractOwnerIdsFromSlot instead of pso_ExtractDependentIdsFromSlot 
and pso_BuildDependentRelationshipsFromIds instead of pso_BuildOwnerRelationshipsFromIds.
