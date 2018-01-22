package com.ibm.cto;

import java.util.List;

import org.apache.http.HttpEntity;

import com.alibaba.fastjson.JSONObject;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

public class ShopzOrderUtils
{
  private static final String URL_ORDER_STATUS_QUERY = "https://9.125.141.233:9443/resttest/api/hello";
  
  private static final String URL_ORDER_STATS_QUERY = "";
  
  private static final String ORDER_STATUS_QUERY = "order number";
  
  private static final String ORDER_STATS_QUERY = "order statistics";
  
  public static void postConversationProcess(MessageResponse response)
  {
    List<String> outputTexts = (List<String>)response.getOutput().get("text");
    if(outputTexts != null && outputTexts.size() > 0)
    {
      for(int i = 0; i < outputTexts.size(); i++)
      {
        if(outputTexts.get(i).contains(ORDER_STATUS_QUERY))
        {
          orderStatusQueryProcess(response);
          break;
        }
        else if(outputTexts.get(i).contains(ORDER_STATUS_QUERY))
        {
          orderStatsQueryProcess(response);
          break;
        }
      }
    }
  }
  
  public static void orderStatusQueryProcess(MessageResponse response)
  {
    System.out.println("##################### orderStatusQueryProcess");
    // retrieve Order Number
    Double orderId = (Double)response.getContext().get("orderId");
    System.out.println("##################### orderId: " + orderId);
    
    try
    {
      HttpEntity entity = Utility.invokeRequest(URL_ORDER_STATUS_QUERY);
      String entityJsonString = Utility.convertHttpEntityToString(entity);
      JSONObject jsonObject = JSONObject.parseObject(entityJsonString);
      
      response.getOutput().put("text", new String[] {(String)jsonObject.get("text") + " - " + orderId});
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void orderStatsQueryProcess(MessageResponse response)
  {
    // retrieve Order Period
  }
}
