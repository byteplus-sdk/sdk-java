package byteplus.sdk.general;

import byteplus.sdk.common.CommonClientImpl;
import byteplus.sdk.common.protocol.ByteplusCommon.OperationResponse;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.general.protocol.ByteplusGeneral.CallbackRequest;
import byteplus.sdk.general.protocol.ByteplusGeneral.CallbackResponse;
import byteplus.sdk.general.protocol.ByteplusGeneral.DoneResponse;
import byteplus.sdk.general.protocol.ByteplusGeneral.PredictRequest;
import byteplus.sdk.general.protocol.ByteplusGeneral.PredictResponse;
import byteplus.sdk.general.protocol.ByteplusGeneral.WriteResponse;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static byteplus.sdk.core.Constant.MAX_IMPORT_ITEM_COUNT;
import static byteplus.sdk.core.Constant.MAX_WRITE_ITEM_COUNT;

@Slf4j
public class GeneralClientImpl extends CommonClientImpl implements GeneralClient {

    private final static String ERR_MSG_TOO_MANY_ITEMS =
            String.format("Only can receive max to %d items in one request", MAX_IMPORT_ITEM_COUNT);

    private final GeneralURL generalURL;

    GeneralClientImpl(Context.Param param) {
        super(param);
        this.generalURL = new GeneralURL(context);
    }

    @Override
    public void doRefresh(String host) {
        this.generalURL.refresh(host);
    }

    @Override
    public WriteResponse writeData(List<Map<String, Object>> dataList, String topic,
                                   Option... opts) throws NetException, BizException {
        if (Objects.nonNull(dataList) && dataList.size() > MAX_WRITE_ITEM_COUNT) {
            log.warn("[ByteplusSDK][WriteData] item count more than '{}'", MAX_WRITE_ITEM_COUNT);
            if (dataList.size() > MAX_IMPORT_ITEM_COUNT) {
                throw new BizException(ERR_MSG_TOO_MANY_ITEMS);
            }
        }
        Parser<WriteResponse> parser = WriteResponse.parser();
        String urlFormat = generalURL.getWriteDataUrlFormat();
        String url = urlFormat.replace("{}", topic);
        WriteResponse response = httpCaller.doJsonRequest(url, dataList, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][WriteData] rsp:\n{}", response);
        return response;
    }

    @Override
    public OperationResponse importData(List<Map<String, Object>> dataList, String topic,
                                        Option... opts) throws NetException, BizException {
        if (Objects.nonNull(dataList) && dataList.size() > MAX_IMPORT_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_ITEMS);
        }
        String urlFormat = generalURL.getImportDataUrlFormat();
        String url = urlFormat.replace("{}", topic);
        Parser<OperationResponse> parser = OperationResponse.parser();
        OperationResponse response = httpCaller.doJsonRequest(url, dataList, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][ImportData] rsp:\n{}", response);
        return response;
    }

    @Override
    public DoneResponse done(List<LocalDate> dateList, String topic,
                             Option... opts) throws NetException, BizException {
        List<Map<String, String>> dateMapList = new ArrayList<>();
        if (Objects.isNull(dateList) || dateList.isEmpty()) {
            LocalDate previousDay = LocalDate.now().plusDays(-1);
            addDoneDate(dateMapList, previousDay);
        } else {
            for (LocalDate date : dateList) {
                addDoneDate(dateMapList, date);
            }
        }
        String urlFormat = generalURL.getDoneUrlFormat();
        String url = urlFormat.replace("{}", topic);
        Parser<DoneResponse> parser = DoneResponse.parser();
        DoneResponse response = httpCaller.doJsonRequest(url, dateMapList, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][Done] rsp:\n{}", response);
        return response;
    }

    private void addDoneDate(List<Map<String, String>> dateMapList, LocalDate date) {
        dateMapList.add(Collections.singletonMap("partition_date", formatDoneDate(date)));
    }

    private String formatDoneDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    @Override
    public PredictResponse predict(PredictRequest request, String scene,
                                   Option... opts) throws NetException, BizException {
        String url = generalURL.getPredictUrlFormat().replace("{}", scene);
        Parser<PredictResponse> parser = PredictResponse.parser();
        PredictResponse response = httpCaller.doPbRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][Predict] rsp:\n{}", response);
        return response;
    }

    @Override
    public CallbackResponse callback(CallbackRequest request,
                                     Option... opts) throws NetException, BizException {
        Parser<CallbackResponse> parser = CallbackResponse.parser();
        String url = generalURL.getCallbackUrl();
        CallbackResponse response = httpCaller.doPbRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][Callback] rsp:\n{}", response);
        return response;
    }
}
