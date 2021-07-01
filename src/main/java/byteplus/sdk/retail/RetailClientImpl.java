package byteplus.sdk.retail;

import byteplus.sdk.common.CommonClientImpl;
import byteplus.sdk.common.protocol.ByteplusCommon.OperationResponse;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.retail.protocol.ByteplusRetail.AckServerImpressionsRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.AckServerImpressionsResponse;
import byteplus.sdk.retail.protocol.ByteplusRetail.ImportProductsRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.ImportUserEventsRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.ImportUsersRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.PredictRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.PredictResponse;
import byteplus.sdk.retail.protocol.ByteplusRetail.WriteProductsRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.WriteProductsResponse;
import byteplus.sdk.retail.protocol.ByteplusRetail.WriteUserEventsRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.WriteUserEventsResponse;
import byteplus.sdk.retail.protocol.ByteplusRetail.WriteUsersRequest;
import byteplus.sdk.retail.protocol.ByteplusRetail.WriteUsersResponse;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import static byteplus.sdk.core.Constant.MAX_IMPORT_ITEM_COUNT;
import static byteplus.sdk.core.Constant.MAX_WRITE_ITEM_COUNT;


@Slf4j
class RetailClientImpl extends CommonClientImpl implements RetailClient {
    private final static String ERR_MSG_TOO_MANY_WRITE_ITEMS =
            String.format("Only can receive %d items in one write request", MAX_WRITE_ITEM_COUNT);

    private final static String ERR_MSG_TOO_MANY_IMPORT_ITEMS =
            String.format("Only can receive %d items in one import request", MAX_IMPORT_ITEM_COUNT);

    private final RetailURL retailUrl;

    RetailClientImpl(Context.Param param) {
        super(param);
        this.retailUrl = new RetailURL(context);
    }

    @Override
    public WriteUsersResponse writeUsers(
            WriteUsersRequest request, Option... opts) throws NetException, BizException {
        if (request.getUsersCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteUsersResponse> parser = WriteUsersResponse.parser();
        String url = retailUrl.getWriteUsersUrl();
        WriteUsersResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteUsers] rsp:\n{}", response);
        return response;
    }

    @Override
    public OperationResponse importUsers(
            ImportUsersRequest request, Option... opts) throws NetException, BizException {
        if (request.getInputConfig().getUsersInlineSource().getUsersCount() > MAX_IMPORT_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_IMPORT_ITEMS);
        }
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getImportUsersUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ImportUsers] rsp:\n{}", response);
        return response;
    }

    @Override
    public WriteProductsResponse writeProducts(
            WriteProductsRequest request, Option... opts) throws NetException, BizException {
        if (request.getProductsCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteProductsResponse> parser = WriteProductsResponse.parser();
        String url = retailUrl.getWriteProductsUrl();
        WriteProductsResponse response =
                httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteProducts] rsp:\n{}", response);
        return response;
    }

    @Override
    public OperationResponse importProducts(
            ImportProductsRequest request, Option... opts) throws NetException, BizException {
        if (request.getInputConfig().getProductsInlineSource().getProductsCount() > MAX_IMPORT_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_IMPORT_ITEMS);
        }
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getImportProductsUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ImportProducts] rsp:\n{}", response);
        return response;
    }

    @Override
    public WriteUserEventsResponse writeUserEvents(
            WriteUserEventsRequest request, Option... opts) throws NetException, BizException {
        if (request.getUserEventsCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteUserEventsResponse> parser = WriteUserEventsResponse.parser();
        String url = retailUrl.getWriteUserEventsUrl();
        WriteUserEventsResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteUserEvents] rsp:\n{}", response);
        return response;
    }

    @Override
    public OperationResponse importUserEvents(
            ImportUserEventsRequest request, Option... opts) throws NetException, BizException {
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getImportUserEventsUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ImportUserEvents] rsp:\n{}", response);
        return response;
    }


    @Override
    public PredictResponse predict(
            PredictRequest request, String scene, Option... opts) throws NetException, BizException {
        String urlFormat = retailUrl.getPredictUrlFormat();
        String url = urlFormat.replace("{}", scene);
        Parser<PredictResponse> parser = PredictResponse.parser();
        PredictResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][Predict] rsp:\n{}", response);
        return response;
    }

    @Override
    public AckServerImpressionsResponse ackServerImpressions(
            AckServerImpressionsRequest request, Option... opts) throws NetException, BizException {
        Parser<AckServerImpressionsResponse> parser = AckServerImpressionsResponse.parser();
        String url = retailUrl.getAckImpressionUrl();
        AckServerImpressionsResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][AckImpressions] rsp:\n{}", response);
        return response;
    }
}