gen_sdk_metrics:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/sdk_metrics.proto

gen_byteair:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/byteplus_byteair.proto

gen_common:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/byteplus_common.proto

gen_general:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/byteplus_general.proto

gen_retail:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/byteplus_retail.proto

gen_retailv2:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/byteplus_retailv2.proto

gen_media:
	protoc --java_out=src/main/java -I=src/main/resources src/main/resources/byteplus_media.proto