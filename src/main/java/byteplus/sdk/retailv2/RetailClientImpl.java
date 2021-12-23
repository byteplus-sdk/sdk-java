package byteplus.sdk.retailv2;

import byteplus.sdk.common.CommonClientImpl;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
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
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static byteplus.sdk.core.Constant.MAX_WRITE_ITEM_COUNT;


@Slf4j
class RetailClientImpl extends CommonClientImpl implements RetailClient {
    private final static String ERR_MSG_TOO_MANY_WRITE_ITEMS =
            String.format("Only can receive max to %d items in one write request", MAX_WRITE_ITEM_COUNT);

    private final RetailURL retailUrl;

    RetailClientImpl(Context.Param param) {
        super(param);
        this.retailUrl = new RetailURL(context);
    }

    @Override
    public void doRefresh(String host) {
        this.retailUrl.refresh(host);
    }

    @Override
    public WriteUsersResponse writeUsers(
            WriteUsersRequest request, Option... opts) throws NetException, BizException {
        if (request.getUsersCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteUsersResponse> parser = WriteUsersResponse.parser();
        String url = retailUrl.getWriteUsersUrl();
        WriteUsersResponse response = httpCaller.doPbRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteUsers] rsp:\n{}", response);
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
                httpCaller.doPbRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteProducts] rsp:\n{}", response);
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
        WriteUserEventsResponse response = httpCaller.doPbRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][WriteUserEvents] rsp:\n{}", response);
        return response;
    }

    @Override
    public PredictResponse predict(
            PredictRequest request, String scene, Option... opts) throws NetException, BizException {
        String urlFormat = retailUrl.getPredictUrlFormat();
        String url = urlFormat.replace("{}", scene);
        Parser<PredictResponse> parser = PredictResponse.parser();
        PredictResponse response = httpCaller.doPbRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][Predict] rsp:\n{}", response);
        return response;
    }

    @Override
    public AckServerImpressionsResponse ackServerImpressions(
            AckServerImpressionsRequest request, Option... opts) throws NetException, BizException {
        Parser<AckServerImpressionsResponse> parser = AckServerImpressionsResponse.parser();
        String url = retailUrl.getAckImpressionUrl();
        AckServerImpressionsResponse response = httpCaller.doPbRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][AckImpressions] rsp:\n{}", response);
        return response;
    }
}