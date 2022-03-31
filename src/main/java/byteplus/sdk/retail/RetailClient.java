package byteplus.sdk.retail;

import byteplus.sdk.common.CommonClient;
import byteplus.sdk.common.protocol.ByteplusCommon.OperationResponse;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;

import static byteplus.sdk.retail.protocol.ByteplusRetail.AckServerImpressionsRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.AckServerImpressionsResponse;
import static byteplus.sdk.retail.protocol.ByteplusRetail.ImportProductsRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.ImportUserEventsRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.ImportUsersRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.PredictRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.PredictResponse;
import static byteplus.sdk.retail.protocol.ByteplusRetail.WriteProductsRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.WriteProductsResponse;
import static byteplus.sdk.retail.protocol.ByteplusRetail.WriteUserEventsRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.WriteUserEventsResponse;
import static byteplus.sdk.retail.protocol.ByteplusRetail.WriteUsersRequest;
import static byteplus.sdk.retail.protocol.ByteplusRetail.WriteUsersResponse;

public interface RetailClient extends CommonClient {
    // WriteUsers
    //
    // Writes at most 2000 users at a time. Exceeding 2000 in a request results in
    // a rejection. One can use this to upload new users, or update existing
    // users (by providing all the fields).
    WriteUsersResponse writeUsers(
            WriteUsersRequest request, Option... opts) throws BizException, NetException;

    // ImportUsers
    //
    // Bulk import of Users.
    //
    // `Operation.response` is of type ImportUsersResponse. Note that it is
    // possible for a subset of the items to be successfully inserted.
    // Operation.metadata is of type Metadata.
    // This call returns immediately after the server finishes the
    // preliminary validations and persists the request. The caller should
    // keep polling `OperationResponse.operation.name` using `GetOperation`
    // call below to check the status.
    // Note: This can also be used to update the existing data by providing the
    // existing ids. In this case, please make sure you provide all fields.
    OperationResponse importUsers(
            ImportUsersRequest request, Option... opts) throws NetException, BizException;

    // WriteProducts
    //
    // Writes at most 2000 products at a time. Exceeding 2000 in a request results
    // in a rejection.
    // One can use this to upload new products, or update existing products (by
    // providing all the fields).  Deleting a product is unsupported. One can
    // update the existing product by
    // setting `product.is_recommendable` to False.
    WriteProductsResponse writeProducts(
            WriteProductsRequest request, Option... opts) throws NetException, BizException;

    // ImportProducts
    //
    // Bulk import of Products.
    //
    // `Operation.response` is of type ImportUsersResponse. Note that it is
    // possible for a subset of the items to be successfully inserted.
    // Operation.metadata is of type Metadata.
    // This call returns immediately after the server finishes the preliminary
    // validations and persists the request.  The caller should keep polling
    // `OperationResponse.operation.name` using `GetOperation` call below to
    // check the status.
    // Note: This can also be used to update the existing data by providing the
    // existing ids. In this case, please make sure you provide all fields.
    OperationResponse importProducts(
            ImportProductsRequest request, Option... opts) throws NetException, BizException;

    // WriteUserEvents
    //
    // Writes at most 2000 UserEvents at a time. Exceeding 2000 in a request
    // results in a rejection. One should use this to upload new realtime
    // UserEvents.  Note: This is processing realtime data, so we won't dedupe
    // the requests.
    // Please make sure the requests are deduplicated before sending over.
    WriteUserEventsResponse writeUserEvents(
            WriteUserEventsRequest request, Option... opts) throws NetException, BizException;

    //ImportUserEvents
    //
    // Bulk import of User events.
    //
    // `Operation.response` is of type ImportUsersResponse. Note that it is
    // possible for a subset of the items to be successfully inserted.
    // Operation.metadata is of type Metadata.
    // This call returns immediately after the server finishes the preliminary
    // validations and persists the request.  The caller should keep polling
    // `OperationResponse.operation.name` using `GetOperation` call below to
    // check the status.
    // Please make sure the requests are deduplicated before sending over.
    OperationResponse importUserEvents(
            ImportUserEventsRequest request, Option... opts) throws NetException, BizException;

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
