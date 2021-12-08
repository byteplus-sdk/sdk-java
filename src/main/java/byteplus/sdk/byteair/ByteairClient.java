package byteplus.sdk.byteair;

import byteplus.sdk.common.CommonClient;
import byteplus.sdk.common.protocol.ByteplusCommon.OperationResponse;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.byteair.protocol.ByteplusByteair.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

public interface ByteairClient extends CommonClient {
    // Write
    //
    // Writes at most 100 data at a time. Exceeding 100 in a request results in
    // a rejection. One can use this to upload new data, or update existing
    // data (by providing all the fields, some data type not support update, e.g. user event).
    WriteResponse writeData(List<Map<String, Object>> dataList, String topic,
                            Option... opts) throws NetException, BizException;

    // Import
    //
    // Bulk import of data.
    //
    // `Operation.response` is of type ImportResponse. Note that it is
    // possible for a subset of the items to be successfully inserted.
    // Operation.metadata is of type Metadata.
    // This call returns immediately after the server finishes the
    // preliminary validations and persists the request. The caller should
    // keep polling `OperationResponse.operation.name` using `GetOperation`
    // call below to check the status.
    // Note: This can also be used to update the existing data(some data type not support).
    // In this case, please make sure you provide all fields.
    OperationResponse importData(List<Map<String, Object>> dataList, String topic,
                                 Option... opts) throws NetException, BizException;

    // Done
    //
    // When the data of a day is imported completely,
    // you should notify bytedance through `done` method,
    // then bytedance will start handling the data in this day
    // @param dateList, optional, if dataList is empty, indicate target date is previous day
    DoneResponse done(List<LocalDate> dateList, String topic,
                      Option... opts) throws NetException, BizException;

    // Predict
    //
    // Gets the list of products (ranked).
    // The updated user data will take effect in 24 hours.
    // The updated product data will take effect in 30 mins.
    // Depending how (realtime or batch) the UserEvents are sent back, it will
    // be fed into the models and take effect after that.
    PredictResponse predict(PredictRequest request,
                            Option... opts) throws NetException, BizException;

    // Callback
    //
    // Sends back the actual product list shown to the users based on the
    // customized changes from `PredictResponse`.
    // example: our Predict call returns the list of items [1, 2, 3, 4].
    // Your custom logic have decided that product 3 has been sold out and
    // product 10 needs to be inserted before 2 based on some promotion rules,
    // the AckServerImpressionsRequest content items should looks like
    // [
    //   {id:1, extra: "{\"reason\": \"kept\"}", pos:1},
    //   {id:10, extra: "{\"reason\": \"inserted\"}", pos:2},
    //   {id:2, extra: "{\"reason\": \"kept\"}", pos:3},
    //   {id:4, extra: "{\"reason\": \"kept\"}", pos:4},
    //   {id:3, extra: "{\"reason\": \"filtered\"}", pos:0},
    // ].
    CallbackResponse callback(CallbackRequest request,
                              Option... opts) throws NetException, BizException;

}
