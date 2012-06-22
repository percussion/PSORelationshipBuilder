	<!--
	   ======================
	   sys_CategoryCheckboxes
	   ======================
	-->
	<psxctl:ControlMeta name="sys_CategoryCheckboxes" dimension="single" choiceset="required">
	   <psxctl:Description>The control for presenting category checkboxes.</psxctl:Description>
	   <psxctl:ParamList>
	      <psxctl:Param name="id" datatype="String" paramtype="generic">
	         <psxctl:Description>This parameter assigns a name to an element. This name must be unique in a document.</psxctl:Description>
	      </psxctl:Param>
	      <psxctl:Param name="class" datatype="String" paramtype="generic">
	         <psxctl:Description>This parameter assigns a class name or set of class names to an element. Any number of elements may be assigned the same class name or names. Multiple class names must be separated by white space characters. The default value is "datadisplay".</psxctl:Description>
	         <psxctl:DefaultValue>datadisplay</psxctl:DefaultValue>
	      </psxctl:Param>
	      <psxctl:Param name="style" datatype="String" paramtype="generic">
	         <psxctl:Description>This parameter specifies style information for the current element. The syntax of the value of the style attribute is determined by the default style sheet language.</psxctl:Description>
	      </psxctl:Param>
	      <psxctl:Param name="tabindex" datatype="Number" paramtype="generic">
	         <psxctl:Description>This parameter specifies the position of the current element in the tabbing order for the current document. This value must be a number between 0 and 32767.</psxctl:Description>
	      </psxctl:Param>
	   </psxctl:ParamList>
	</psxctl:ControlMeta>
	<xsl:template match="Control[@name='sys_CategoryCheckboxes']" priority="10" mode="psxcontrol">
	   <!-- This template needs to have higher priority than the generic read-only template -->
	   <div name="{@paramName}" class="datadisplay">
	      <xsl:call-template name="parametersToAttributes">
	         <xsl:with-param name="controlClassName" select="'sys_CategoryCheckboxes'" />
	         <xsl:with-param name="controlNode" select="." />
	         <xsl:with-param name="paramType" select="'generic'" />
	      </xsl:call-template>
	      <xsl:if test="@accessKey!=''">
	         <xsl:attribute name="accesskey">
	            <xsl:call-template name="getaccesskey">
	               <xsl:with-param name="label" select="preceding-sibling::DisplayLabel" />
	               <xsl:with-param name="sourceType" select="preceding-sibling::DisplayLabel/@sourceType" />
	               <xsl:with-param name="paramName" select="@paramName" />
	               <xsl:with-param name="accessKey" select="@accessKey" />
	            </xsl:call-template>
	         </xsl:attribute>
	      </xsl:if>

	      <!-- 
	         When multiple items have been selected, Value will be an array list 
	         with a string representation like "[1, 2]". 
	         Use saxon:tokenize() to convert the string into a node-set.
	      -->

	      <!-- output the categories in a two column table -->
	      <xsl:variable name="midpoint" select="count(DisplayChoices/DisplayEntry) div 2" />
	      <table>
	         <tr>
	            <td>
	               <xsl:apply-templates select="DisplayChoices/DisplayEntry[position()&lt;=$midpoint]" mode="sys_CategoryCheckboxes-entries">
	                  <xsl:with-param name="selectedValues">
	                     <xsl:copy-of select="saxon:tokenize(Value, ';')" xmlns:saxon="http://icl.com/saxon" />
	                  </xsl:with-param>
	                  <xsl:with-param name="paramName" select="@paramName" />
	                  <xsl:with-param name="isReadOnly" select="@isReadOnly" />
	               </xsl:apply-templates>
	            </td>
	            <td>
	               <xsl:apply-templates select="DisplayChoices/DisplayEntry[position()&gt;$midpoint]" mode="sys_CategoryCheckboxes-entries">
	                  <xsl:with-param name="selectedValues">
	                     <xsl:copy-of select="saxon:tokenize(Value, ';')" xmlns:saxon="http://icl.com/saxon" />
	                  </xsl:with-param>
	                  <xsl:with-param name="paramName" select="@paramName" />
	                  <xsl:with-param name="isReadOnly" select="@isReadOnly" />
	               </xsl:apply-templates>
	            </td>
	         </tr>
	      </table>
	   </div>
	</xsl:template>

	<xsl:template match="DisplayEntry" mode="sys_CategoryCheckboxes-entries">
      <xsl:param name="selectedValues" />
      <xsl:param name="paramName" />
      <xsl:param name="isReadOnly" />
      <xsl:variable name="entryValue" select="Value" />
      <xsl:choose>
         <xsl:when test="$isReadOnly != 'yes'">
            <input name="{$paramName}" type="checkbox" value="{$entryValue}">
               <!-- check to see if this entry's value is in the selected values set -->
               <xsl:if test="saxon:exists($selectedValues/*, saxon:expression('. = $entryValue'))" xmlns:saxon="http://icl.com/saxon">
                  <xsl:attribute name="checked">checked</xsl:attribute>
               </xsl:if>
               <xsl:value-of select="DisplayLabel" />
            </input>
            <br />
         </xsl:when>
         <xsl:otherwise>
            <!-- when control is read-only, use images to represent checkboxes -->
            <img src="../sys_resources/images/unchecked.gif" height="16" width="16">
               <!-- check to see if this entry's value is in the selected values set -->
               <xsl:if test="saxon:exists($selectedValues/*, saxon:expression('. = $entryValue'))" xmlns:saxon="http://icl.com/saxon">
                  <xsl:attribute name="src">../sys_resources/images/checked.gif</xsl:attribute>
               </xsl:if>
            </img>
            <xsl:text>&nbsp;</xsl:text>
            <xsl:value-of select="DisplayLabel" />
            <br />
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>