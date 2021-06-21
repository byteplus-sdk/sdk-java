package byteplus.sdk.retail;

import byteplus.retail.sdk.protocol.ByteplusRetail.AckServerImpressionsRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.AckServerImpressionsResponse;
import byteplus.retail.sdk.protocol.ByteplusRetail.GetOperationRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.ImportProductsRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.ImportUserEventsRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.ImportUsersRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.ListOperationsRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.ListOperationsResponse;
import byteplus.retail.sdk.protocol.ByteplusRetail.OperationResponse;
import byteplus.retail.sdk.protocol.ByteplusRetail.PredictRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.PredictResponse;
import byteplus.retail.sdk.protocol.ByteplusRetail.WriteProductsRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.WriteProductsResponse;
import byteplus.retail.sdk.protocol.ByteplusRetail.WriteUserEventsRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.WriteUserEventsResponse;
import byteplus.retail.sdk.protocol.ByteplusRetail.WriteUsersRequest;
import byteplus.retail.sdk.protocol.ByteplusRetail.WriteUsersResponse;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.HttpCaller;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Options;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import static byteplus.sdk.core.Constant.MAX_IMPORT_ITEM_COUNT;
import static byteplus.sdk.core.Constant.MAX_WRITE_ITEM_COUNT;


@Slf4j
class RetailClientImpl implements RetailClient {
    private final static String TOO_MANY_WRITE_ITEMS_ERR_MSG =
            String.format("Only can receive %d items in one write request", MAX_WRITE_ITEM_COUNT);

    private final static String TOO_MANY_IMPORT_ITEMS_ERR_MSG =
            String.format("Only can receive %d items in one import request", MAX_IMPORT_ITEM_COUNT);

    private final RetailURL retailUrl;

    private final HttpCaller httpCaller;

    RetailClientImpl(Context.Param param) {
        Context context = new Context(param);
        this.retailUrl = new RetailURL(context);
        this.httpCaller = new HttpCaller(context);
    }

    @Override
    public WriteUsersResponse writeUsers(
            WriteUsersRequest request, Options.Filler... opts) throws NetException, BizException {
        if (request.getUsersCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(TOO_MANY_WRITE_ITEMS_ERR_MSG);
        }
        Parser<WriteUsersResponse> parser = WriteUsersResponse.parser();
        String url = retailUrl.getWriteUsersUrl();
        WriteUsersResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteUsers] rsp: {}", response);
        return response;
    }

    @Override
    public OperationResponse importUsers(
            ImportUsersRequest request, Options.Filler... opts) throws NetException, BizException {
        if (request.getInputConfig().getUsersInlineSource().getUsersCount() > MAX_IMPORT_ITEM_COUNT) {
            throw new BizException(TOO_MANY_IMPORT_ITEMS_ERR_MSG);
        }
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getImportUsersUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ImportUsers] rsp: {}", response);
        return response;
    }

    @Override
    public WriteProductsResponse writeProducts(
            WriteProductsRequest request, Options.Filler... opts) throws NetException, BizException {
        if (request.getProductsCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(TOO_MANY_WRITE_ITEMS_ERR_MSG);
        }
        Parser<WriteProductsResponse> parser = WriteProductsResponse.parser();
        String url = retailUrl.getWriteProductsUrl();
        WriteProductsResponse response =
                httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteProducts] rsp: {}", response);
        return response;
    }

    @Override
    public OperationResponse importProducts(
            ImportProductsRequest request, Options.Filler... opts) throws NetException, BizException {
        if (request.getInputConfig().getProductsInlineSource().getProductsCount() > MAX_IMPORT_ITEM_COUNT) {
            throw new BizException(TOO_MANY_IMPORT_ITEMS_ERR_MSG);
        }
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getImportProductsUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ImportProducts] rsp: {}", response);
        return response;
    }

    @Override
    public WriteUserEventsResponse writeUserEvents(
            WriteUserEventsRequest request, Options.Filler... opts) throws NetException, BizException {
        if (request.getUserEventsCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(TOO_MANY_WRITE_ITEMS_ERR_MSG);
        }
        Parser<WriteUserEventsResponse> parser = WriteUserEventsResponse.parser();
        String url = retailUrl.getWriteUserEventsUrl();
        WriteUserEventsResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteUserEvents] rsp: {}", response);
        return response;
    }

    @Override
    public OperationResponse importUserEvents(
            ImportUserEventsRequest request, Options.Filler... opts) throws NetException, BizException {
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getImportUserEventsUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ImportUserEvents] rsp: {}", response);
        return response;
    }

    @Override
    public OperationResponse getOperation(
            GetOperationRequest request, Options.Filler... opts) throws NetException, BizException {
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = retailUrl.getGetOperationUrl();
        OperationResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][GetOperations] rsp:\n{}", response);
        return response;
    }

    @Override
    public ListOperationsResponse listOperations(
            ListOperationsRequest request, Options.Filler... opts) throws NetException, BizException {
        Parser<ListOperationsResponse> parser = ListOperationsResponse.parser();
        String url = retailUrl.getListOperationsUrl();
        ListOperationsResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][ListOperations] rsp:\n{}", response);
        return response;
    }

    @Override
    public PredictResponse predict(
            PredictRequest request, String scene, Options.Filler... opts) throws NetException, BizException {
        String urlFormat = retailUrl.getPredictUrlFormat();
        String url = urlFormat.replace("{}", scene);
        Parser<PredictResponse> parser = PredictResponse.parser();
        PredictResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][Predict] rsp:\n{}", response);
        return response;
    }

    @Override
    public AckServerImpressionsResponse ackServerImpressions(
            AckServerImpressionsRequest request, Options.Filler... opts) throws NetException, BizException {
        Parser<AckServerImpressionsResponse> parser = AckServerImpressionsResponse.parser();
        String url = retailUrl.getAckImpressionUrl();
        AckServerImpressionsResponse response = httpCaller.doRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][AckImpressions] rsp:\n{}", response);
        return response;
    }
}