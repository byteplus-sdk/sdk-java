package byteplus.sdk.common;

import byteplus.sdk.common.protocol.ByteplusCommon;
import byteplus.sdk.common.protocol.ByteplusCommon.*;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.HostAvailabler;
import byteplus.sdk.core.HttpCaller;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.core.URLCenter;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class CommonClientImpl implements CommonClient, URLCenter {

    protected final Context context;

    protected final HttpCaller httpCaller;

    protected CommonURL commonURL;

    private final HostAvailabler hostAvailabler;

    protected CommonClientImpl(Context.Param param) {
        this.context = new Context(param);
        this.httpCaller = new HttpCaller(context);
        this.commonURL = new CommonURL(context);
        this.hostAvailabler = new HostAvailabler(context, this);
    }

    @Override
    public final void refresh(String host) {
        this.commonURL.refresh(host);
        doRefresh(host);
    }

    public abstract void doRefresh(String host);

    public final void release() {
        this.hostAvailabler.shutdown();
        doRelease();
    }

    public void doRelease() {

    }

    @Override
    public OperationResponse getOperation(
            GetOperationRequest request, Option... opts) throws NetException, BizException {
        Parser<OperationResponse> parser = OperationResponse.parser();
        String url = commonURL.getGetOperationUrl();
        OperationResponse response = httpCaller.doPbRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][GetOperations] rsp:\n{}", response);
        return response;
    }

    @Override
    public ListOperationsResponse listOperations(
            ListOperationsRequest request, Option... opts) throws NetException, BizException {
        Parser<ListOperationsResponse> parser = ListOperationsResponse.parser();
        String url = commonURL.getListOperationsUrl();
        ListOperationsResponse response = httpCaller.doPbRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][ListOperations] rsp:\n{}", response);
        return response;
    }

    @Override
    public Response done(DoneRequest request, String topic, Option... opts) throws NetException, BizException {
        String urlFormat = commonURL.getDoneUrlFormat();
        String url = urlFormat.replace("{}", topic);
        Parser<Response> parser =  ByteplusCommon.Response.parser();
        Response response = httpCaller.doPbRequest(url, request, parser, opts);
        log.debug("[ByteplusSDK][Done] rsp:\n{}", response);
        return response;
    }
}
