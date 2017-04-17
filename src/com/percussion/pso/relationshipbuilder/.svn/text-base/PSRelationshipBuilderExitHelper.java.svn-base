package com.percussion.pso.relationshipbuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.percussion.cms.PSCmsException;
import com.percussion.cms.handlers.PSContentEditorHandler;
import com.percussion.cms.handlers.PSModifyCommandHandler;
import com.percussion.cms.handlers.PSQueryCommandHandler;
import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.IPSRequestContext;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.util.IPSHtmlParameters;

/**
 * This class is shared code between the several exits that are provided for
 * relationship building.
 * 
 * @author Adam Gent
 * @since 6.0
 */
public class PSRelationshipBuilderExitHelper
{

   private static final String DEFAULT_OUTPUT = "";

private final transient PSAbstractRelationshipBuilder m_builder;

   private final transient Map<String, String> m_parameters;

   public static final String ARRAY_DELIMETER = ";";

   public PSRelationshipBuilderExitHelper(PSAbstractRelationshipBuilder builder,
         Map<String, String> parameters)
   {
      super();
      this.m_parameters = parameters;
      this.m_builder = builder;
   }

   /**
    * Returns a list of the owner content ids from the relationships in the slot
    * identified by the "slotname" parameter that have the request's content
    * item as their dependent.
    * @param request the current request context.
    * 
    * @return a list of owner content ids as ";" delimited string from the
    *         slot's matching relationships, or <code>null</code> if there are
    *         no matching relationships, e.g. <code>692;651;339</code>.
    * @throws PSConversionException if request does not include a sys_contentid
    *            parameter, if "slotname" parameter is missing or empty, if slot
    *            cannot be found, or if relationship API throws exception.
    */
   public Object processUdf(final IPSRequestContext request) throws PSConversionException
   {
      ms_log.debug("Started extracting ids from slot exit: " + m_builder.getClass().getName());
      // get the current content item from the request
      final String contentId = request
            .getParameter(IPSHtmlParameters.SYS_CONTENTID);
      ms_log.debug("\tProcessing for content id: " + contentId);
      String idsString = DEFAULT_OUTPUT;
      StringBuffer idsStringBuffer = new StringBuffer();
      if (StringUtils.isNumeric(contentId))
      {
         final int cid = Integer.parseInt(contentId);

         // get the slot name from the supplied parameters
         final Map<String, String> paramMap = m_parameters;
         final String slotname = paramMap.get("slotname");
         ms_log.debug("\tslotname is: " + slotname);
         if (StringUtils.isBlank(slotname))
         {
            throw new PSConversionException(0,
                  "must provide non-blank slotname parameter value");
         }

         try
         {
            final List<Integer> ids = m_builder.extractIdsFromSlot(cid,
                  slotname);

            if (ids != null && ids.size() > 0)
            {
               idsStringBuffer.append("");
               for (int i = 0; i < ids.size(); i++)
               {
                  idsStringBuffer.append(ids.get(i).toString());
                  if (i < ids.size() - 1)
                  {
                     idsStringBuffer.append(ARRAY_DELIMETER);
                  }
               }
            }
            else
            {
            	idsStringBuffer.append(DEFAULT_OUTPUT);
            }

            idsString = idsStringBuffer.toString();
         }
         catch (PSAssemblyException e)
         {
            ms_log.error("\tFailed to find slot <" + slotname + ">", e);
            throw new PSConversionException(0, e);
         }
         catch (PSCmsException e)
         {
            ms_log.error("\tFailure in relationship API", e);
            throw new PSConversionException(0, e);
         }
      }
      else
      {
         /*
          * not every request will have a content id (such as creating a new
          * item), so don't throw exception if it is missing.
          */
         ms_log.debug("\tskipping extract; no content id in request");
      }
      ms_log.debug("\tReturned string for udf: " + idsString);
      ms_log.debug("Finished extracting ids from slot exit: " + m_builder.getClass().getName());
      return idsString;
   }

   /**
    * Maintains active-assembly-style relationships between the request's
    * content item and a list of content items -- missing relationships will be
    * created, existing relationships with items not in the list will be
    * deleted. The request's content item is the dependent, and the list of
    * content items become its parents. The details of the relationship are
    * provided as parameters (relationship type, slot id, and variant id).
    * @param request the current request context, not <code>null</code>.
    * @param resultDoc the request's result XML document. not modified by this
    *           exit. may be <code>null</code>.
    * 
    * @return the supplied <code>resultDoc</code>, without modification
    * @throws PSParameterMismatchException if any required parameter is blank.
    * @throws PSExtensionProcessingException if the assembly or relationship
    *            APIs report an error.
    */
   public Document processResultDocument(final IPSRequestContext request,
         final Document resultDoc)
         throws PSParameterMismatchException, PSExtensionProcessingException
   {
      /*
       * This exit should only act when editing an item, not when editing the
       * child, or performing other content editor operations like inline link
       * updates.
       */
      String command = request.getParameter(IPSHtmlParameters.SYS_COMMAND);
      ms_log.debug("Command is: " + command );
      String page = request
            .getParameter(PSContentEditorHandler.PAGE_ID_PARAM_NAME);
      String processInlineLink = request
            .getParameter(IPSHtmlParameters.SYS_INLINELINK_DATA_UPDATE);

      if (page != null
            && command.equals(PSModifyCommandHandler.COMMAND_NAME)
            && page.equals(String
                  .valueOf(PSQueryCommandHandler.ROOT_PARENT_PAGE_ID))
            && (processInlineLink == null || !processInlineLink.equals("yes")))
      {
         ms_log.debug("Starting building relationships exit with builder " + m_builder.getClass().getName());
         String contentId = request
               .getParameter(IPSHtmlParameters.SYS_CONTENTID);
         ms_log.debug("\tProcessing for content id: " + contentId);
         if (StringUtils.isNumeric(contentId))
         {
            int cid = Integer.parseInt(contentId);

            Map<String, String> paramMap = m_parameters;

            String fieldName = paramMap.get("fieldname");
            ms_log.debug("\tfieldname: " + fieldName);
            if (StringUtils.isBlank(fieldName))
            {
               throw new PSParameterMismatchException(
                     IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                     new Object[]
                     {"fieldname", "is a required parameter"});
            }

            String slotName = paramMap.get("slotname");
            ms_log.debug("\tslotname: " + slotName);
            if (StringUtils.isBlank(slotName))
            {
               throw new PSParameterMismatchException(
                     IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                     new Object[]
                     {"slotname", "is a required parameter"});
            }

            String templateName = paramMap.get("templatename");
            ms_log.debug("\ttemplatename: " + templateName);
            if (StringUtils.isBlank(templateName))
            {
               throw new PSParameterMismatchException(
                     IPSExtensionErrors.EXT_MISSING_REQUIRED_PARAMETER_ERROR,
                     new Object[]
                     {"templatename", "is a required parameter"});
            }

            try
            {
               Object[] fieldValues = request.getParameterList(fieldName);
               /*
                * Remove Duplicate entries so that we don't get multiple
                * relationships to the same item. This is usually caused by
                * child fields.
                */
               Set <Integer> fieldValuesSet = new HashSet<Integer>();
               Collection <Object> invalid = convert (fieldValues, fieldValuesSet);
               ms_log.debug("\tField values for fieldname '" + fieldName +"' is : " + 
                     fieldValuesSet);
               if (invalid.size() == 1 && invalid.contains("")) {
            	   ms_log.debug("\tEmpty String only.  No items checked.  Removing relationships");
               }
               else if (invalid.size() != 0) {
                  ms_log.debug("\tInvalid id(s) were passed. Not building any relationships");
                  ms_log.debug("\tInvalid: " + invalid);
               }
               else {
                  m_builder.build(cid, fieldValuesSet, slotName, templateName);
               }
            }
            catch (PSAssemblyException e)
            {
               ms_log.error("Failure in assembly API", e);
               throw new PSExtensionProcessingException(0, e);
            }
            catch (PSCmsException e)
            {
               ms_log.error("Failure in relationship API", e);
               throw new PSExtensionProcessingException(0, e);
            }
            finally {
               ms_log.debug("Finished processing exit with builder " + m_builder.getClass().getName());
            }
         }
      }
      return resultDoc;
   }


   /**
    * Converts an array of objects to a list of integers. Non-parsable elements
    * indicies in the inputIds array are returned.
    * 
    * @param inputIds
    * @param output list of converted ids
    * @return the collection of objects that failed to convert.
    */
   public static Collection<Object> convert(Object[] inputIds,
         final Set<Integer> output)
   {
      Collection<Object> invalid = new ArrayList<Object>();

      if (output == null)
      {
         throw new IllegalArgumentException("Output Set cannot be null.");
      }
      if (inputIds != null)
      {
         for (int i = 0; i < inputIds.length; i++)
         {
            Object contentId = inputIds[i];
            if (contentId instanceof Integer)
            {
               output.add((Integer) contentId);
            }
            else if (contentId != null 
                  && StringUtils.isNotBlank(contentId.toString())
                  && StringUtils.isNumeric(contentId.toString()))
            {
               output.add(Integer.valueOf(contentId.toString()));
            }
            else
            {
               // log and return any non-parsables
               ms_log
                     .warn("\ttaking note of non-parsable element in array <"
                           + contentId + ">");
               invalid.add(contentId);               
            }
         }
      }
      return invalid;
   }

   /**
    * The log instance to use for this class, never <code>null</code>.
    */
   private static final Log ms_log = LogFactory.getLog(PSRelationshipBuilderExitHelper.class);

}
