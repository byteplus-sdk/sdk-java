package byteplus.sdk.retailv2;

import byteplus.sdk.common.CommonClient;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;

import java.sql.Time;
import java.time.LocalDate;
import java.util.List;

import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.AckServerImpressionsRequest;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.AckServerImpressionsResponse;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.PredictRequest;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.PredictResponse;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.WriteProductsRequest;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.WriteProductsResponse;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.WriteUserEventsRequest;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.WriteUserEventsResponse;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.WriteUsersRequest;
import static byteplus.sdk.retailv2.protocol.ByteplusRetailv2.WriteUsersResponse;

public interface RetailClient extends CommonClient {
    // WriteUsers
    //
    // Writes at most 100 users at a time. Exceeding 100 in a request results in
    // a rejection. One can use this to upload new users, or update existing
    // users (by providing all the fields).
    WriteUsersResponse writeUsers(
            WriteUsersRequest request, Option... opts) throws BizException, NetException;

    // WriteProducts
    //
    // Writes at most 100 products at a time. Exceeding 100 in a request results
    // in a rejection.
    // One can use this to upload new products, or update existing products (by
    // providing all the fields).  Deleting a product is unsupported. One can
    // update the existing product by
    // setting `product.is_recommendable` to False.
    WriteProductsResponse writeProducts(
            WriteProductsRequest request, Option... opts) throws NetException, BizException;

    // WriteUserEvents
    //
    // Writes at most 100 UserEvents at a time. Exceeding 100 in a request
    // results in a rejection. One should use this to upload new realtime
    // UserEvents.  Note: This is processing realtime data, so we won't dedupe
    // the requests.
    // Please make sure the requests are deduplicated before sending over.
    WriteUserEventsResponse writeUserEvents(
            WriteUserEventsRequest request, Option... opts) throws NetException, BizException;

    // Predict
    //
    // Gets the list of products (ranked).
    // The updated user data will take effect in 24 hours.
    // The updated product data will take effect in 30 mins.
    // Depending how (realtime or batch) the UserEvents are sent back, it will
    // be fed into the models and take effect after that.
    PredictResponse predict(
            PredictRequest request, String scene, Option... opts) throws NetException, BizException;

    // AckServerImpressions
    //
    // Sends back the actual product list shown to the users based on the
    // customized changes from `PredictResponse`.
    // example: our Predict call returns the list of items [1, 2, 3, 4].
    // Your custom logic have decided that product 3 has been sold out and
    // product 10 needs to be inserted before 2 based on some promotion rules,
    // the AckServerImpressionsRequest content items should looks like
    // [
    //   {id:1, altered_reason: "kept", rank:1},
    //   {id:10, altered_reason: "inserted", rank:2},
    //   {id:2, altered_reason: "kept", rank:3},
    //   {id:4, altered_reason: "kept", rank:4},
    //   {id:3, altered_reason: "filtered", rank:0},
    // ].
    AckServerImpressionsResponse ackServerImpressions(
            AckServerImpressionsRequest request, Option... opts) throws NetException, BizException;
}
