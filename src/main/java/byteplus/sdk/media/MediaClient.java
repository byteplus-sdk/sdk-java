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
import byteplus.sdk.media.protocol.ByteplusMedia.PredictRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.PredictResponse;
import byteplus.sdk.media.protocol.ByteplusMedia.AckServerImpressionsRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.AckServerImpressionsResponse;

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

    // Predict
    //
    // Gets the list of contents (ranked).
    // The updated user data will take effect in 24 hours.
    // The updated content data will take effect in 30 mins.
    // Depending on how (realtime or batch) the UserEvents are sent back, it will
    // be fed into the models and take effect after that.
    PredictResponse predict(
            PredictRequest request, String scene, Option... opts) throws NetException, BizException;

    // AckServerImpressions
    //
    // Sends back the actual content list shown to the users based on the
    // customized changes from `PredictResponse`.
    // example: our Predict call returns the list of items [1, 2, 3, 4].
    // Your custom logic have decided that content 3 has been sold out and
    // content 10 needs to be inserted before 2 based on some promotion rules,
    // and because the number of Byteplus recommendations is insufficient,
    // fill in your recommended content 20 after content 4,
    // the AckServerImpressionsRequest content items should looks like
    // [
    //   {content_id:1, altered_reason: "kept", rank:1},
    //   {content_id:10, altered_reason: "inserted", rank:2},
    //   {content_id:2, altered_reason: "kept", rank:3},
    //   {content_id:4, altered_reason: "kept", rank:4},
    //   {content_id:20, altered_reason: "filled", rank:5},
    //   {content_id:3, altered_reason: "filtered", rank:0},
    // ].
    AckServerImpressionsResponse ackServerImpressions(
            AckServerImpressionsRequest request, Option... opts) throws NetException, BizException;
}
