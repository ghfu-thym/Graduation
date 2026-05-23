package com.spike.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamoService {

    private final DynamoDbClient dynamoDbClient;
    private final String TABLE_NAME = "EventConfig";


    public void updateEventStatus(String eventId, String status, int shardCount) {
        try {
            // create partition key
            //cau truc Map<Map<ten_cot, ten_key>, Map<ten_cot, gia_tri>>
            Map<String, AttributeValue> keyToGet = new HashMap<>();
            keyToGet.put("eventId", AttributeValue.builder().s(eventId).build());

            Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
            updatedValues.put("vwr_status", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(status).build())
                    .action(AttributeAction.PUT)
                    .build());
            updatedValues.put("shardCount", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().n(String.valueOf(shardCount)).build())
                    .action(AttributeAction.PUT)
                    .build());

            UpdateItemRequest request = UpdateItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .key(keyToGet)
                    .attributeUpdates(updatedValues)
                    .returnValues(ReturnValue.ALL_NEW)
                    .build();

            UpdateItemResponse response = dynamoDbClient.updateItem(request);
            System.out.println("[DynamoDB Realtime Data trên Server]: " + response.attributes());
            System.out.println("[DynamoDB] Cập nhật thành công sự kiện " + eventId + " thành trạng thái: " + status);

        } catch (DynamoDbException e) {
            System.err.println("[DynamoDB Lỗi] Không thể cập nhật trạng thái sự kiện: " + e.getMessage());
        }
    }
}

