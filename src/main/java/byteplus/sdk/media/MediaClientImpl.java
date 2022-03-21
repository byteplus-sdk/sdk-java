package byteplus.sdk.media;

import byteplus.sdk.common.CommonClientImpl;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.Context;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUsersRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUsersResponse;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteContentsRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteContentsResponse;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUserEventsRequest;
import byteplus.sdk.media.protocol.ByteplusMedia.WriteUserEventsResponse;
import com.google.protobuf.Parser;
import lombok.extern.slf4j.Slf4j;

import static byteplus.sdk.core.Constant.MAX_WRITE_ITEM_COUNT;

@Slf4j
public class MediaClientImpl extends CommonClientImpl implements MediaClient{
    private final static String ERR_MSG_TOO_MANY_WRITE_ITEMS =
            String.format("Only can receive max to %d items in one write request", MAX_WRITE_ITEM_COUNT);

    private final MediaURL mediaURL;

    MediaClientImpl(Context.Param param) {
        super(param);
        this.mediaURL = new MediaURL(context);
    }

    @Override
    public void doRefresh(String host) {
        this.mediaURL.refresh(host);
    }

    @Override
    public WriteUsersResponse writeUsers(
            WriteUsersRequest request, Option... opts) throws BizException, NetException {
        if (request.getUsersCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteUsersResponse> parser = WriteUsersResponse.parser();
        String url = mediaURL.getWriteUsersUrl();
        WriteUsersResponse response = httpCaller.doPBRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][WriteUsers] rsp:\n{}", response);
        return response;
    }

    @Override
    public WriteContentsResponse writeContents(
            WriteContentsRequest request, Option... opts) throws NetException, BizException {
        if (request.getContentsCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteContentsResponse> parser = WriteContentsResponse.parser();
        String url = mediaURL.getWriteContentsUrl();
        WriteContentsResponse response =
                httpCaller.doPBRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][WriteContents] rsp:\n{}", response);
        return response;
    }

    @Override
    public WriteUserEventsResponse writeUserEvents(
            WriteUserEventsRequest request, Option... opts) throws NetException, BizException {
        if (request.getUserEventsCount() > MAX_WRITE_ITEM_COUNT) {
            throw new BizException(ERR_MSG_TOO_MANY_WRITE_ITEMS);
        }
        Parser<WriteUserEventsResponse> parser = WriteUserEventsResponse.parser();
        String url = mediaURL.getWriteUserEventsUrl();
        WriteUserEventsResponse response = httpCaller.doPBRequest(url, request, parser, Option.conv2Options(opts));
        log.debug("[ByteplusSDK][WriteUserEvents] rsp:\n{}", response);
        return response;
    }
}
