package byteplus.sdk.saas;

import byteplus.sdk.common.CommonClientImpl;
import byteplus.sdk.core.*;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static byteplus.sdk.core.Constant.*;
import static byteplus.sdk.saas.protocol.ByteplusSaas.*;


@Slf4j
class SaasClientImpl extends CommonClientImpl implements SaasClient {
    private final static String ERR_MSG_TOO_MANY_WRITE_ITEMS =
            String.format("Only can receive max to %d items in one write request", MAX_IMPORT_WRITE_ITEM_COUNT);

    private final SaasURL SaasUrl;

    private final static int DATE_INIT_OPTIONS_COUNT = 1;
    private final static int PREDICT_INIT_OPTIONS_COUNT = 1;
    private final static String SaasTenant = "saas";

    SaasClientImpl(Context.Param param) {
        super(param.setTenant(SaasTenant));
        this.SaasUrl = new SaasURL(context);
    }

    private boolean noneEmptySting(String str) {
        if (str != null && str.length() != 0) {
            return true;
        }
        return false;
    }

    private void checkProjectIdAndModelId(String projectId, String modelId) throws BizException {
        final String ERR_MSG_FORMAT = "%s,field can not empty";
        final String ERR_FIELD_PROJECT_ID = "projectId";
        final String ERR_FIELD_MODEL_ID = "modelId";
        if (noneEmptySting(projectId) && noneEmptySting(modelId)) {
            return;
        }
        List<String> emptyParams = new ArrayList<>();
        if (noneEmptySting(projectId)) {
            emptyParams.add(ERR_FIELD_PROJECT_ID);
        }
        if (noneEmptySting(modelId)) {
            emptyParams.add(ERR_FIELD_MODEL_ID);
        }
        throw new BizException(String.format(ERR_MSG_FORMAT, String.join(",", emptyParams)));
    }

    private void checkProjectIdAndStage(String projectId, String stage) throws BizException {
        final String ERR_MSG_FORMAT = "%s,field can not empty";
        final String ERR_FIELD_PROJECT_ID = "projectId";
        final String ERR_FIELD_STAGE = "stage";
        if (noneEmptySting(projectId) && noneEmptySting(stage)) {
            return;
        }
        List<String> emptyParams = new ArrayList<>();
        if (noneEmptySting(projectId)) {
            emptyParams.add(ERR_FIELD_PROJECT_ID);
        }
        if (noneEmptySting(stage)) {
            emptyParams.add(ERR_FIELD_STAGE);
        }
        throw new BizException(String.format(ERR_MSG_FORMAT, String.join(",", emptyParams)));
    }

    private Option[] addSaasFlag(Option[] opts) {
        Option[] newOpts = new Option[opts.length + DATE_INIT_OPTIONS_COUNT];
        System.arraycopy(opts, 0, newOpts, 0, opts.length);
        opts[opts.length - 1] = withSaasHeader();
        return newOpts;
    }

    static Option withSaasHeader() {
        final String HTTPHeaderServerFrom = "Server-From";
        final String SaasFlag = "saas";
        return options -> {
            Map<String, String> headers = options.getHeaders();
            if (Objects.isNull(headers)) {
                headers = new HashMap<String, String>() {
                    {
                        put(HTTPHeaderServerFrom, SaasFlag);
                    }
                };
                options.setHeaders(headers);
                return;
            }
            headers.put(HTTPHeaderServerFrom, SaasFlag);
        };

    }

    @Override
    public void doRefresh(String host) {
        this.SaasUrl.refresh(host);
    }

    private WriteResponse doWriteData(WriteDataRequest request, String url, Option... opts) throws NetException, BizException {
        checkProjectIdAndStage(request.getProjectId(), request.getStage());
        if (request.getDataCount() > MAX_IMPORT_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Option[] newOpts = addSaasFlag(opts);
        Parser<WriteResponse> parser = WriteResponse.parser();
        WriteResponse response = httpCaller.doPbRequest(url, request, parser, newOpts);
        log.debug("[ByteplusSDK][WriteData] rsp:\n{}", response);
        return response;
    }

    @Override
    public WriteResponse writeUsers(WriteDataRequest request, Option... opts) throws NetException, BizException {
        return doWriteData(request, SaasUrl.getWriteUsersUrl(), opts);
    }

    @Override
    public WriteResponse writeProducts(WriteDataRequest request, Option... opts) throws NetException, BizException {
        return doWriteData(request, SaasUrl.getWriteProductsUrl(), opts);
    }

    @Override
    public WriteResponse writeUserEvents(WriteDataRequest request, Option... opts) throws NetException, BizException {
        return doWriteData(request, SaasUrl.getWriteUserEventUrl(), opts);
    }

    @Override
    public PredictResponse predict(
            PredictRequest request, Option... opts) throws NetException, BizException {
        checkProjectIdAndModelId(request.getProjectId(), request.getModelId());
        Option[] newOpts = addSaasFlag(opts);
        Parser<PredictResponse> parser = PredictResponse.parser();
        PredictResponse response = httpCaller.doPbRequest(SaasUrl.getPredictUrl(), request, parser, newOpts);
        log.debug("[ByteplusSDK][Predict] rsp:\n{}", response);
        return response;
    }

    @Override
    public AckServerImpressionsResponse ackServerImpressions(
            AckServerImpressionsRequest request, Option... opts) throws NetException, BizException {
        checkProjectIdAndModelId(request.getProjectId(), request.getModelId());
        Parser<AckServerImpressionsResponse> parser = AckServerImpressionsResponse.parser();
        Option[] newOpts = addSaasFlag(opts);
        AckServerImpressionsResponse response = httpCaller.doPbRequest(SaasUrl.getAckImpressionUrl(), request, parser, newOpts);
        log.debug("[ByteplusSDK][AckImpressions] rsp:\n{}", response);
        return response;
    }
}