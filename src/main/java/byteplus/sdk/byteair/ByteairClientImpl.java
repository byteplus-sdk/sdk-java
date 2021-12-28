package byteplus.sdk.byteair;

import byteplus.sdk.common.CommonClientImpl;
import byteplus.sdk.core.*;
import byteplus.sdk.byteair.protocol.ByteplusByteair.CallbackRequest;
import byteplus.sdk.byteair.protocol.ByteplusByteair.CallbackResponse;
import byteplus.sdk.common.protocol.ByteplusCommon.Date;
import byteplus.sdk.common.protocol.ByteplusCommon.DoneRequest;
import byteplus.sdk.common.protocol.ByteplusCommon.DoneResponse;
import byteplus.sdk.byteair.protocol.ByteplusByteair.PredictRequest;
import byteplus.sdk.byteair.protocol.ByteplusByteair.PredictResponse;
import byteplus.sdk.byteair.protocol.ByteplusByteair.WriteResponse;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static byteplus.sdk.core.Constant.MAX_IMPORT_ITEM_COUNT;

@Slf4j
public class ByteairClientImpl extends CommonClientImpl implements ByteairClient {

    private final static String ERR_MSG_TOO_MANY_ITEMS =
            String.format("Only can receive max to %d items in one request", MAX_IMPORT_ITEM_COUNT);

    public final static String DEFAULT_PREDICT_SCENE = "default";

    private final ByteairURL byteairURL;

    ByteairClientImpl(Context.Param param) {
        super(param);
        this.byteairURL = new ByteairURL(context);
    }

    @Override
    public void doRefresh(String host) {
        this.byteairURL.refresh(host);
    }

    @Override
    public WriteResponse writeData(List<Map<String, Object>> dataList, String topic,
                                   Option... opts) throws NetException, BizException {
        if (Objects.nonNull(dataList) && dataList.size() > MAX_IMPORT_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_ITEMS);
        }
        Parser<WriteResponse> parser = WriteResponse.parser();
        String urlFormat = byteairURL.getWriteDataUrlFormat();
        String url = urlFormat.replace("{}", topic);
        WriteResponse response = httpCaller.doJsonRequest(url, dataList, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][WriteData] rsp:\n{}", response);
        return response;
    }

    @Override
    public PredictResponse predict(PredictRequest request,
                                   Option... opts) throws NetException, BizException {
        Options options = Option.conv2Options(opts);
        String scene = getSceneFromOpts(options);
        String url = byteairURL.getPredictUrlFormat().replace("{}", scene);
        Parser<PredictResponse> parser = PredictResponse.parser();
        PredictResponse response = httpCaller.doPbRequest(url, request, parser, options);
        log.debug("[ByteplusSDK][Predict] rsp:\n{}", response);
        return response;
    }

    private String getSceneFromOpts(Options options) {
        if (Objects.isNull(options.getScene()) || "".equals(options.getScene())) {
            return DEFAULT_PREDICT_SCENE;
        }
        return options.getScene();
    }

    @Override
    public CallbackResponse callback(CallbackRequest request,
                                     Option... opts) throws NetException, BizException {
        Parser<CallbackResponse> parser = CallbackResponse.parser();
        String url = byteairURL.getCallbackUrl();
        CallbackResponse response = httpCaller.doPbRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][Callback] rsp:\n{}", response);
        return response;
    }
}
