package byteplus.sdk.media;

import byteplus.sdk.common.CommonClient;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUsersRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUsersResponse;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteContentsRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteContentsResponse;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUserEventsRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUserEventsResponse;

public interface MediaClient extends CommonClient {
    // WriteUsers
    //
    // Writes at most 2000 users at a time. Exceeding 2000 in a request results in
    // a rejection. One can use this to upload new users, or update existing
    // users (by providing all the fields).
    WriteUsersResponse writeUsers(
            WriteUsersRequest request, Option... opts) throws BizException, NetException;

    // WriteProducts
    //
    // Writes at most 2000 contents at a time. Exceeding 2000 in a request results
    // in a rejection.
    // One can use this to upload new contents, or update existing contents (by
    // providing all the fields).  Deleting a content is unsupported. One can
    // update the existing content by
    // setting `content.is_recommendable` to False.
    WriteContentsResponse writeContents(
            WriteContentsRequest request, Option... opts) throws NetException, BizException;

    // WriteUserEvents
    //
    // Writes at most 2000 UserEvents at a time. Exceeding 2000 in a request
    // results in a rejection. One should use this to upload new realtime
    // UserEvents.  Note: This is processing realtime data, so we won't dedupe
    // the requests.
    // Please make sure the requests are deduplicated before sending over.
    WriteUserEventsResponse writeUserEvents(
            WriteUserEventsRequest request, Option... opts) throws NetException, BizException;
}
