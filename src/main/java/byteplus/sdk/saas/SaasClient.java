package byteplus.sdk.saas;

import byteplus.sdk.common.CommonClient;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;


import static byteplus.sdk.saas.protocol.ByteplusSaas.*;

public interface SaasClient extends CommonClient {

    // writeUsers
    //
    // Writes at most 100 users data at a time. Exceeding 100 in a request results in
    // a rejection.Each element of dataList array is a json serialized string of data.
    // One can use this to upload new data, or update existing data.
    WriteResponse writeUsers(WriteDataRequest request, Option... opts) throws NetException, BizException;

    // writeProducts
    //
    // Writes at most 100 products data at a time. Exceeding 100 in a request results in
    // a rejection.Each element of dataList array is a json serialized string of data.
    // One can use this to upload new data, or update existing data.
    WriteResponse writeProducts(WriteDataRequest request, Option... opts) throws NetException, BizException;

    // writeUserEvents
    //
    // Writes at most 100 user events data at a time. Exceeding 100 in a request results in
    // a rejection.Each element of dataList array is a json serialized string of data.
    // One can use this to upload new data, or update existing data (by providing all the fields,
    // some data type not support update, e.g. user event).
    WriteResponse writeUserEvents(WriteDataRequest request, Option... opts) throws NetException, BizException;

    // Predict
    //
    // Gets the list of products (ranked).
    // The updated user data will take effect in 24 hours.
    // The updated product data will take effect in 30 mins.
    // Depending how (realtime or batch) the UserEvents are sent back, it will
    // be fed into the models and take effect after that.
    PredictResponse predict(
            PredictRequest request, Option... opts) throws NetException, BizException;

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
