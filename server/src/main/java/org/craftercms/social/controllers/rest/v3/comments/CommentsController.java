package org.craftercms.social.controllers.rest.v3.comments;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.collections.IterableUtils;
import org.craftercms.profile.api.Profile;
import org.craftercms.social.controllers.rest.v3.comments.exceptions.UGCNotFound;
import org.craftercms.social.domain.social.Flag;
import org.craftercms.social.domain.social.SocialUgc;
import org.craftercms.social.exceptions.IllegalUgcException;
import org.craftercms.social.exceptions.SocialException;
import org.craftercms.social.exceptions.UGCException;
import org.craftercms.social.security.SocialSecurityUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

/**
 */
@Controller
public class CommentsController<T extends SocialUgc> extends AbstractCommentsController {

    private Logger log = LoggerFactory.getLogger(CommentsController.class);

    @RequestMapping(method = RequestMethod.POST)
    @ApiOperation(value = "Creates a new comment", consumes = MimeTypeUtils.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public T create(@ApiParam(value = "Body of the Comment,Some Html/scripts tags will be strip") @RequestParam()
                        final String body, @ApiParam(name = "thread",
        value = "Id of the thread to attach this comment") @RequestParam(required = true) final String thread,
                    @ApiParam(value = "Id of the parent for the new comment", name = "parentId") @RequestParam
                        (required = false, defaultValue = "") final String parent, @ApiParam(value = "Should This " +
        "comment be post as anonymous ", name = "anonymous") @RequestParam(required = false, defaultValue = "false",
        value = "anonymous") final boolean anonymous, @ApiParam(value = "Subject of the comment to be " + "created",
        name = "subject") @RequestParam(required = false, defaultValue = "", value = "subject") final String subject,
                    @ApiParam(value = "Json String representing any extra attributes of the comment to create",
        name = "attributes") @RequestParam(required = false,
        defaultValue = "{}") final String attributes, MultipartFile attachment) throws SocialException,
        MissingServletRequestParameterException, IOException {
        Map<String, Object> attributesMap = null;

        if (!StringUtils.isBlank(attributes)) {
            attributesMap = parseAttributes(attributes);
        }
        T newUgc = (T)ugcService.create(context(), parent, thread, body, subject, attributesMap, checkAnonymous
            (anonymous));

        if (attachment != null) {
            ugcService.addAttachment(newUgc.getId().toString(), context(), attachment.getInputStream(), attachment
                .getOriginalFilename(), getContentType(attachment.getOriginalFilename()));
        }
        return newUgc;
    }

    @RequestMapping(value = "{id}", method = RequestMethod.PUT)
    @ApiOperation(value = "Updates the given comment", notes = "As Create some HTML/scripts tags will be scripted")
    @ResponseBody
    public T update(@ApiParam(value = "Ugc id to removeWatcher") @PathVariable("id") final String id, @ApiParam(value
        = "New comment Body") @RequestParam() final String body, @ApiParam(value = "Json String representing any " +
        "extra attributes of the comment to create",
        name = "attributes") @RequestParam(required = false,
        defaultValue = "{}") final String attributes) throws SocialException,
        MissingServletRequestParameterException, UGCNotFound {
        Map<String, Object> attributesMap = null;
        if (!StringUtils.isBlank(attributes)) {
            attributesMap = parseAttributes(attributes);
        }
        return (T)ugcService.update(id, body, "", context(), attributesMap);
    }

    @RequestMapping(value = "{id}/update", method = RequestMethod.POST)
    @ApiOperation(value = "Updates the given comment", notes = "As Create some HTML/scripts tags will be scripted")
    @ResponseBody
    public T updatePost(@ApiParam(value = "Ugc id to removeWatcher") @PathVariable("id") final String id, @ApiParam
        (value = "New comment Body") @RequestParam() final String body, @ApiParam(value = "Json String representing "
        + "any " + "extra attributes of the comment to create",
        name = "attributes") @RequestParam(required = false,
        defaultValue = "{}") final String attributes) throws SocialException,
        MissingServletRequestParameterException, UGCNotFound {
        return this.update(id, body, attributes);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    @ApiOperation(value = "Deletes the comment", notes = "As Create some HTML/scripts tags will be scripted, " +
        "Also All children will be deleted")
    @ResponseBody
    public boolean delete(@ApiParam(value = "Comment id to removeWatcher") @PathVariable("id") final String id)
        throws SocialException {
        ugcService.deleteUgc(id, context());
        return true;
    }

    @RequestMapping(value = "{id}/delete", method = RequestMethod.POST)
    @ApiOperation(value = "Deletes the comment", notes = "As Create some HTML/scripts tags will be scripted, " +
        "Also All children will be deleted")
    @ResponseBody
    public boolean deletePost(@ApiParam(value = "Comment id to removeWatcher") @PathVariable("id") final String id)
        throws SocialException {
        return this.delete(id);
    }

    @RequestMapping(value = "{id}", method = RequestMethod.GET)
    @ApiOperation(value = "Gets a the comment")
    @ResponseBody
    public T read(@ApiParam(value = "Comment id to removeWatcher") @PathVariable("id") final String id) throws
        SocialException {
        return (T)ugcService.read(id, context());
    }


    @RequestMapping(value = "{id}/attributes", method = {RequestMethod.POST, RequestMethod.PUT}, consumes =
        {MimeTypeUtils.APPLICATION_FORM_URLENCODED_VALUE})
    @ResponseBody
    @ApiOperation(value = "Adds or updates the given attributes with there new value " + "if attribute does not " +
        "exists it will be created Json is expected to by the POST body",
        notes = "This operation expects any " +
            "type of valid JSON" +
            " " +
            "object Do notice that there is a current limitation and all attributes will be converted into a 'String" +
            " there for its all non array-maps. this is valid for numbers,booleans and dates. keep this in mind where" +
            " " +
            "doing the search")
    public boolean addAttributes(@ApiParam(value = "Id of the UGC") @NotBlank @PathVariable(value = "id") final
                                     String id, @ApiParam(value = "Json of the attributes to be updated or created" +
        ". All values are " + "save as string (booleans,numbers,dates)") @RequestParam final Map<String, Object>
        attributes) throws SocialException, UGCNotFound {
        log.debug("Request for deleting form  UGC {} attributes {}", id, attributes);
        attributes.remove("context");
        ugcService.setAttributes(id, context(), attributes);
        return true;//Always true unless exception.
    }


    @RequestMapping(value = "{id}/attributes", method = RequestMethod.DELETE)
    @ResponseBody
    @ApiOperation(value = "Deletes all the attributes from the given UGC", notes = "All attributes must be in dot " +
        "notation where nested values should be in its full path, to remove multiple attributes send them separated " +
        "by a ',' ")
    public boolean removeAttributes(@ApiParam(value = "Id of the comment", name = "id") @PathVariable(value = "id")
                                        final String id, @ApiParam(name = "attributes", value = "List of , " +
        "separated attributes name to delete. use dot " + "notation do delete nested attributes.") @RequestParam
    final String attributes) throws SocialException {
        log.debug("Request for deleting form  UGC {} attributes {}", id, attributes);
        ugcService.deleteAttribute(id, attributes.split(","), context());
        return true;//Always true unless exception.
    }

    @RequestMapping(value = "{id}/attributes/delete", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "Deletes all the attributes from the given UGC", notes = "All attributes must be in dot " +
        "notation where nested values should be in its full path, to remove multiple attributes send them separated " +
        "by a ',' ")
    public boolean removeAttributesPost(@ApiParam(value = "Id of the comment", name = "id") @PathVariable(value = "id")
                                    final String id, @ApiParam(name = "attributes", value = "List of , " +
        "separated attributes name to delete. use dot " + "notation do delete nested attributes.") @RequestParam
                                    final String attributes) throws SocialException {
       return this.removeAttributes(id, attributes);
    }

    @RequestMapping(value = "{id}/flags", method = RequestMethod.POST)
    @ApiOperation(value = "Flags the UGC", notes = "Reason will be cleanup for any HTML/Script")
    @ResponseBody
    public T flagUgc(@ApiParam(value = "Comment Id") @PathVariable(value = "id") final String id, @ApiParam(value =
        "Reason why the comment is been flag") @RequestParam final String reason) throws SocialException {
        return (T)socialServices.flag(id, context(), reason, userId());
    }


    @RequestMapping(value = "{id}/flags", method = RequestMethod.GET)
    @ApiOperation(value = "Flags the UGC", notes = "Reason will be cleanup for any HTML/Script")
    @ResponseBody
    public Iterable<Flag> flagUgc(@ApiParam(value = "Comment Id") @PathVariable(value = "id") final String id) throws
        SocialException {
        T ugc = (T)ugcService.read(id, context());
        if (ugc == null) {
            throw new IllegalUgcException("Given UGC does not exist for context");
        }
        return ugc.getFlags();
    }

    @RequestMapping(value = "{id}/flags/{flagId}", method = {RequestMethod.POST, RequestMethod.DELETE})
    @ApiOperation(value = "Flags the UGC", notes = "Reason will be cleanup for any HTML/Script")
    @ResponseBody
    public boolean unflagUgc(@ApiParam(value = "Comment Id") @PathVariable(value = "id") final String id, @ApiParam
        (value = "Flag id to delete") @PathVariable(value = "flagId") final String flagId) throws SocialException {
        return socialServices.unFlag(id, flagId, userId(), context());
    }

    @RequestMapping(value = "{id}/moderate", method = {RequestMethod.POST, RequestMethod.PUT})
    @ResponseBody
    @ApiOperation(value = "Changes the Status of the given UGC")
    public T moderate(@ApiParam("Id of the comment to change status") @PathVariable final String id, @ApiParam("New "
        + "Moderation Status of the Param") @RequestParam final SocialUgc.ModerationStatus status) throws
        SocialException {
        return (T)socialServices.moderate(id, status, userId(), context());
    }



    @RequestMapping(value = "moderation/{status}", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Gets all Moderation comments with the given moderation status")
    public Iterable<T> byStatus(@PathVariable("status") final SocialUgc.ModerationStatus status, @RequestParam
        (defaultValue = "", required = false) final String thread, @RequestParam(required = false, defaultValue =
        "0") final int pageNumber, @RequestParam(required = false, defaultValue = ThreadsController.MAX_INT) final
    int pageSize, @RequestParam(required = false) final List<String> sortBy, @RequestParam(required = false) final
    List<SocialSortOrder> sortOrder) throws UGCException {
        int start = 0;
        if (pageNumber > 0 && pageSize > 0) {
            start = ThreadsController.getStart(pageNumber, pageSize);
        }

        return IterableUtils.toList(socialServices.findByModerationStatus(status, thread, context(), start, pageSize,
            ThreadsController.getSortOrder(sortBy, sortOrder)));
    }


    @RequestMapping(value = "flagged", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Gets all flagged Ugs")
    public Iterable<T> flagged(@RequestParam(required = false, defaultValue = "0") final int pageNumber,
                               @RequestParam(required = false, defaultValue = ThreadsController.MAX_INT) final int
                                   pageSize, @RequestParam(required = false) final List<String> sortBy, @RequestParam
                                       (required = false) final List<SocialSortOrder> sortOrder) throws UGCException {
        int start = 0;
        if (pageNumber > 0 && pageSize > 0) {
            start = ThreadsController.getStart(pageNumber, pageSize);
        }

        return IterableUtils.toList(socialServices.findAllFlaggedUgs(context(), start, pageSize, ThreadsController
            .getSortOrder(sortBy, sortOrder)));
    }

    @RequestMapping(value = "flagged/count", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Counts all flagged Ugs")
    public long flaggedCount(@RequestParam(required = false, defaultValue = "0") final int pageNumber, @RequestParam
        (required = false, defaultValue = ThreadsController.MAX_INT) final int pageSize, @RequestParam(required =
        false) final List<String> sortBy, @RequestParam(required = false) final List<SocialSortOrder> sortOrder)
        throws UGCException {
        int start = 0;
        if (pageNumber > 0 && pageSize > 0) {
            start = ThreadsController.getStart(pageNumber, pageSize);
        }

        return socialServices.countAllFlaggedUgs(context(), start, pageSize, ThreadsController.getSortOrder(sortBy,
            sortOrder));
    }


    @RequestMapping(value = "moderation/{status}/count", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "Counts all Moderation comments with the given moderation status")
    public long byStatusCount(@PathVariable("status") final SocialUgc.ModerationStatus status, @RequestParam
        (defaultValue = "", required = false) final String thread) throws UGCException {
        return socialServices.countByModerationStatus(status, thread, context());
    }


    protected boolean checkAnonymous(final boolean anonymous) {
        final Profile profile = SocialSecurityUtils.getCurrentProfile();
        Object isAlwaysAnonymous = profile.getAttribute("isAlwaysAnonymous");
        if (isAlwaysAnonymous == null) {
            return anonymous;
        } else {
            return ((Boolean)isAlwaysAnonymous).booleanValue();
        }

    }

}
