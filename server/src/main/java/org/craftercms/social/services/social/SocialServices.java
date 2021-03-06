package org.craftercms.social.services.social;

import java.util.List;

import org.apache.commons.collections4.keyvalue.DefaultKeyValue;
import org.craftercms.social.domain.social.SocialUgc;
import org.craftercms.social.exceptions.SocialException;
import org.craftercms.social.exceptions.UGCException;

/**
 * Defines all Rest Services for Moderation of UGCs.
 * <p>Implementers Must
 * <ul>
 * <li>Audit all Calls</li>
 * <li>Check that this call are made with an authenticated user and the the UGC belongs to the user's context.</li>
 * </ul>
 * </p>
 */
public interface SocialServices<T extends SocialUgc> {


    /**
     * Executes the given UserContentInteraction for the given User Content Action.
     * <p>Implementers must check if the current user is allow to removeWatcher UGC and that the user belongs to the same
     * ugc context</p>.
     *
     * @param ugcId       Id of the UGC.
     * @param voteOptions Interaction to be executed.
     * @param userId      Id of the user that is interacting with the content.
     * @param contextId   Context of the UGC.
     * @return A new Public (secure) UGC.
     * @throws org.craftercms.social.exceptions.SocialException if attribute can be deleted.
     * @throws java.lang.IllegalArgumentException               If given UGC does not exist.
     */
    T vote(final String ugcId, final VoteOptions voteOptions, final String userId,
           final String contextId) throws SocialException;

    /**
     * Flags the given UGC, with for given reason.
     * <p>Implementers must check if the current user is allow to removeWatcher UGC and that the user belongs to the same
     * ugc context</p>.
     *
     * @param ugcId  Id of the UGC to flag.
     * @param reason The reason for this ugc is been flag.
     * @param userId Id of the user that is flagging this UGC.
     * @param contextId Context of the UGC.
     * @return A new (updated) Public (secure) UGC.
     */
     T flag(String ugcId, String contextId, String reason, String userId) throws SocialException;

    /**
     * Un flags the given UGC for the given reason.
     * <p>Implementers must check if the current user is allow to removeWatcher UGC and that the user belongs to the same
     * ugc context</p>.
     *
     * @param ugcId  Id of the UGC to un flag.
     * @param flagId Id of the flag to delete.
     * @param userId Id of the user that is unflagging this UGC.
     * @param contextId Context of the UGC.
     * @return A new (updated) Public (secure) UGC.
     */
    boolean unFlag(final String ugcId, final String flagId, final String userId, final String contextId) throws SocialException;

    /**
     * Change the moderation Status of the given UGC.
     * @param ugcId Id of the UGC to change moderation status.
     * @param moderationStatus new Moderation Status.
     * @param userId Id of the user that is changing the status.
     * @param contextId Context of the UGC.
     */
    T moderate(String ugcId, SocialUgc.ModerationStatus moderationStatus, String userId, String contextId) throws SocialException;


    /**
     * Finds all Comments with the given Moderation status. Optional filter the thread
     * @param status ModerationStatus to filter.
     * @param thread Thread owner of the comments (optional)
     * @param start Where to to start the count.
     * @param limit Amount of Comments to return.
     * @param contextId Context of the UGC.
     * @param sort Sort Fields.
     * @return A Iterable with the results.
     */
    Iterable<T> findByModerationStatus(SocialUgc.ModerationStatus status, String thread, String contextId,
                                       int start, int limit, final List<DefaultKeyValue<String, Boolean>> sort)
            throws UGCException;


    /**
     * Counts all Comments with the given Moderation status. Optional filter the thread
     * @param status ModerationStatus to filter.
     * @param thread Thread owner of the comments (optional)
     * @param contextId Context of the UGC.
     * @return Number of Results.
     */
    long countByModerationStatus(SocialUgc.ModerationStatus status, String thread, String contextId) throws UGCException;


    /**
     * Returns all Flagged UGc
     * @param context Context of the Ugc
     * @param start Where to to start the count.
     * @param pageSize  Amount of Comments to return
     * @param sortOrder Sort Fields.
     * @return A Iterable with the results.
     */
    Iterable<T> findAllFlaggedUgs(String context, int start, int pageSize, List<DefaultKeyValue<String,Boolean>> sortOrder);


    long countAllFlaggedUgs(String context, int start, int pageSize, List<DefaultKeyValue<String,Boolean>> sortOrder);

}
