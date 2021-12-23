package byteplus.sdk.common;

import byteplus.sdk.common.protocol.ByteplusCommon.*;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.general.protocol.ByteplusGeneral;

import java.time.LocalDate;
import java.util.List;

public interface CommonClient {
    // GetOperation
    //
    // Gets the operation of a previous long running call.
    OperationResponse getOperation(GetOperationRequest request,
                                   Option... opts) throws NetException, BizException;

    // ListOperations
    //
    // Lists operations that match the specified filter in the request.
    ListOperationsResponse listOperations(ListOperationsRequest request,
                                          Option... opts) throws NetException, BizException;

    // Done
    //
    // When the data of a day is imported completely,
    // you should notify bytedance through `done` method,
    // then bytedance will start handling the data in this day
    // @param dateList, optional, if dataList is empty, indicate target date is previous day
    DoneResponse done(List<LocalDate> dateList, String topic,
                                      Option... opts) throws NetException, BizException;

    void release();
}
