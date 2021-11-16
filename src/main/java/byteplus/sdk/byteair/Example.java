package byteplus.sdk.byteair;

import byteplus.sdk.byteair.protocol.ByteplusByteair;
import byteplus.sdk.core.BizException;
import byteplus.sdk.core.NetException;
import byteplus.sdk.core.Option;
import byteplus.sdk.core.Region;
import byteplus.sdk.general.protocol.ByteplusGeneral;
import com.alibaba.fastjson.JSON;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

public final class Example {
    private static ByteairClient client;
    private static List<String> AIR_HOSTS = Arrays.asList("byteair-api-cn1.snssdk.com");
    private static String DEFAULT_PREDICT_SCENE = "default";
    private static Duration DEFAULT_PREDICT_TIMEOUT = Duration.ofMillis(800);

    public static void main(String[] args) {
        init();
        writeDataExample();
        doneExample();
        predictAndCallbackDemo();
    }

    private static void init() {
        //创建client
        client = new ByteairClientBuilder()
                //Required，区域, 推荐平台统一使用Region.AIR
                .region(Region.AIR)
                //Optional，域名list，如果设置了region，会默认填入byteair-api-cn1.snssdk.com
                .hosts(AIR_HOSTS)
                //Required，鉴权token，以字节实际分配的为准
                .token("776147e1c52c62c2b1e3600734f5d944")
                //Required，租户id
                .tenantId("1324123")
                //Required，项目id
                .projectId("3241331")
                //Optional，url schema
                .schema("http")
                // Optional，请求头设置
                //.headers(Collections.singletonMap("Customer-Header", "value"))
                //进行构建client
                .build();
    }

    public static void writeDataExample() {
        //构造数据
        Map<String, Object> item = new HashMap<>();
        // 以下数据皆为测试数据，实际调用时需注意字段类型和格式
        item.put("id", "2133121");
        item.put("register_time", 12345678);
        item.put("age","20-30");
        item.put("gender","female");

        List<Map<String, Object>> datas = new ArrayList<>();
        datas.add(item);
        Option[] opts = writeOptions();
        ByteplusByteair.WriteResponse writeResponse = null;
        //同步执行上传
        try {
            writeResponse = client.writeData(datas, "user", opts);
        } catch (NetException | BizException e) {
            System.out.printf("upload occur error, msg:%s\n", e.getMessage());
        }
        System.out.printf("upload success, msg:%s\n", writeResponse);
    }

    private static Option[] writeOptions() {
        Map<String, String> customerHeaders = Collections.emptyMap();
        return new Option[]{
                Option.withRequestId(UUID.randomUUID().toString()),
                Option.withTimeout(Duration.ofMillis(800)),
                // Optional. Add custom headers.
                // Option.withHeaders(customerHeaders),

                // Optional. The server is expected to return within a certain period，
                // to avoid response can't return before client is timeout.
                // This server timeout should be less than total write timeout.
                Option.withServerTimeout(Duration.ofMillis(800).minus(Duration.ofMillis(100))),
                // Required. The stage of write data(real-time data), must be "incremental_sync_streaming"
                Option.withStage("pre_sync"),
                Option.withDataDate(LocalDate.now())
        };
    }

    private static void doneExample() {
        LocalDate date = LocalDate.of(2021, 8, 1); //替换成对应需要执行done的日期
        List<LocalDate> dateList = Collections.singletonList(date);
        String topic = "user"; //替换成对应需要执行done的topic，
        Option[] opts = doneOptions();
        ByteplusByteair.DoneResponse doneResponse = null;
        try {
            doneResponse= client.done(dateList, topic, opts);
        } catch (BizException | NetException e) {
            System.out.printf("[Done] occur error, msg:%s \n", e.getMessage());
            return;
        }
        System.out.printf("[Done] done rsp:%s \n", doneResponse);
    }

    private static Option[] doneOptions() {
        Map<String, String> customerHeaders = Collections.emptyMap();
        return new Option[]{
                Option.withRequestId(UUID.randomUUID().toString()),
                Option.withTimeout(Duration.ofMillis(800)),
                // Optional. Add custom headers.
                // Option.withHeaders(Option.withHeaders(customerHeaders)),

                // Required. The stage of import data, include "pre_sync", "history_sync" and "incremental_sync_daily",
                // should be with withStage option
                Option.withStage("pre_sync")
        };
    }

    private static ByteplusByteair.PredictRequest buildPredictRequest() {
        ByteplusByteair.PredictUser user = ByteplusByteair.PredictUser.newBuilder()
                .setUid("21322")
                .build();
        ByteplusByteair.PredictContext context = ByteplusByteair.PredictContext.newBuilder()
                .setSpm("xx$$xxx$$xx")
                .build();
        ByteplusByteair.PredictCandidateItem candidateItem = ByteplusByteair.PredictCandidateItem.newBuilder()
                .setId("12312")
                .build();
        ByteplusByteair.PredictRelatedItem relatedItem = ByteplusByteair.PredictRelatedItem.newBuilder()
                .setId("32122")
                .build();

        ByteplusByteair.PredictExtra extra = ByteplusByteair.PredictExtra.newBuilder()
                .putExtra("extra_key", "value")
                .build();

        return ByteplusByteair.PredictRequest.newBuilder()
                .setUser(user)
                .setContext(context)
                .setSize(20)
                .addCandidateItems(candidateItem)
                .setRelatedItem(relatedItem)
                .setExtra(extra)
                .build();
    }

    public static void predictAndCallbackDemo() {
        ByteplusByteair.PredictRequest predictRequest = buildPredictRequest();
        Map<String, String> customerHeaders = Collections.emptyMap();
        Option[] predictOpts = new Option[]{
                Option.withRequestId(UUID.randomUUID().toString()),
                Option.withTimeout(Duration.ofMillis(800)),
                Option.withHeaders(customerHeaders),
                Option.withScene(DEFAULT_PREDICT_SCENE)
        };
        ByteplusByteair.PredictResponse predictResponse = null;
        // The `scene` is provided by ByteDance, according to tenant's situation
//        String scene = "home";
        try {
            predictResponse = client.predict(predictRequest, predictOpts);
        } catch (Exception e) {
            System.out.printf("predict occur error, msg:%s \n", e.getMessage());
            return;
        } finally {
            if (predictResponse != null && predictResponse.getCode() == 0)
                System.out.printf("predict success info, msg: %s \n", predictResponse);
            else{
                System.out.printf("predict failure info, msg: %s \n", predictResponse);
            }
        }

        List<ByteplusByteair.CallbackItem> callbackItems = conv2CallbackItems(predictResponse.getValue().getItemsList());
        ByteplusByteair.CallbackRequest callbackRequest = ByteplusByteair.CallbackRequest.newBuilder()
                .setPredictRequestId(predictResponse.getRequestId())
                .setUid(predictRequest.getUser().getUid())
                .setScene(DEFAULT_PREDICT_SCENE)
                .addAllItems(callbackItems)
                .build();
        Option[] callbackOpts = new Option[]{
                Option.withRequestId(UUID.randomUUID().toString()),
                Option.withTimeout(DEFAULT_PREDICT_TIMEOUT)
        };
        ByteplusByteair.CallbackResponse callbackResponse = null;
        try {
            callbackResponse = client.callback(callbackRequest, callbackOpts);
        } catch (NetException | BizException e) {
            e.printStackTrace();
        } finally {
            System.out.printf("callback rsp info: %s \n", callbackResponse);
        }
    }

    //callback这里利用了predict的返回结果进行处理，生成callbackItems
    private static List<ByteplusByteair.CallbackItem> conv2CallbackItems(List<ByteplusByteair.PredictResultItem> resultItems) {
        if (Objects.isNull(resultItems) || resultItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<ByteplusByteair.CallbackItem> callbackItems = new ArrayList<>(resultItems.size());
        for (int i = 0; i < resultItems.size(); i++) {
            ByteplusByteair.PredictResultItem resultItem = resultItems.get(i);
            Map<String, String> extraMap = Collections.singletonMap("reason", "kept");
            ByteplusByteair.CallbackItem callbackItem = ByteplusByteair.CallbackItem.newBuilder()
                    .setId(resultItem.getId())
                    .setPos((i + 1) + "")
                    .setExtra(JSON.toJSONString(extraMap))
                    .build();
            callbackItems.add(callbackItem);
        }
        return callbackItems;
    }

    public void callbackDemo() {

    }
}
