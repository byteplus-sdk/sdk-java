package byteplus.sdk.common;

import byteplus.sdk.common.protocol.ByteplusCommon.*;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;

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
}
