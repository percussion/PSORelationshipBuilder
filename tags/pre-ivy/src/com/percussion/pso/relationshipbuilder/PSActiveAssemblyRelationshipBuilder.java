package com.percussion.pso.relationshipbuilder;

public abstract class PSActiveAssemblyRelationshipBuilder extends
        PSAbstractRelationshipBuilder {
    protected String m_templateName;
    protected String m_slotName;
    
    public String getSlotName() {
        return m_slotName;
    }
    public void setSlotName(String slotName) {
        this.m_slotName = slotName;
    }
    public String getTemplateName() {
        return m_templateName;
    }
    public void setTemplateName(String templateName) {
        this.m_templateName = templateName;
    }

}
