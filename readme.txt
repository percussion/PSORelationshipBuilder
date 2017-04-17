Relationship Builder

This project was created to have the ability to 
add relationships through the content editor instead of the active assembly editors.

Installation:
 
  1. Download and install Apache ant if you have not done so already. 
  2. Set the environment variable RHYTHMYX_HOME to your Rhythmyx directory.
  3. Run the command:
      ant -f deploy.xml
     While in the Relationship Builder directory.
  
  
Setup:

For creating relationships with content editor control
you will use the following exits:
	* pso_SelectAaRelationships - a Item output exit or field output translation exit.
	* pso_BuildAaRelationships - a post result document exist

The documentation for the exits is baked into the Extensions.xml that is provided
with this package.

1. Create a slot and a template to be used in the relationship. 
!!! Make sure that when you create the slot in the workbench that relationship 
type/name does get set. You may have to save the slot twice to get this working in 6.0 !!!

2. In your content editor you are going to create a dummy field with 
whatever control you like so long as it supports multiple properties
or is a single select (examples: sys_dropdown, sys_checkboxtree, sys_multidropdown).

3. You will need to create a query resource that follows 
the sys_lookup.dtd that queries all the items you want to be able to select from.
Now register this query resource for the display choice mappings for you dummy field.

There is also folder building relationship exit for folder building (see Extensions.xml).
