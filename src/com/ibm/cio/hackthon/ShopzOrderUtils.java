package com.ibm.cio.hackthon;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ibm.watson.developer_cloud.conversation.v1.model.MessageResponse;

public class ShopzOrderUtils
{
  public static final String URL_ORDER_STATUS_QUERY = "http://cap-sg-prd-3.integration.ibmcloud.com:16311/serviceorder/getOrderStatus";
  
  public static final String URL_ORDER_STATS_QUERY = "http://cap-sg-prd-3.integration.ibmcloud.com:16311/serviceorder/countOrderStatus";
  
  private static final String ORDER_STATUS_QUERY = "check order status";
  
  private static final String ORDER_STATS_QUERY = "check shopz order status";
  
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
        else if(outputTexts.get(i).contains(ORDER_STATS_QUERY))
        {
          orderStatsQueryProcess(response);
          break;
        }
      }
    }
  }
  
  public static void orderStatusQueryProcess(MessageResponse response)
  {
    System.out.println("##################### orderStatusQueryProcess #####################");
    // retrieve Order Number
    String orderid = (String)response.getContext().get("orderid");
    
    try
    {
      HttpEntity entity = Utility.invokeRequest(URL_ORDER_STATUS_QUERY + "/" + orderid);
      String entityJsonString = Utility.convertHttpEntityToString(entity);
      System.out.println("##################### entityJsonString: " + entityJsonString);
      
      List<String> texts = new ArrayList<String>();
      JSONArray orderStatusArray = JSONObject.parseArray(entityJsonString);
      if(orderStatusArray.size() > 0)
      {
        JSONObject orderStatus = orderStatusArray.getJSONObject(0);
        texts.add("orderid: " + orderid);
        texts.add("datecreated: " + orderStatus.getString("datecreated"));
        
        JSONArray torderstatesArray = orderStatus.getJSONArray("torderstates");
        JSONObject[] torderstates = torderstatesArray.toArray(new JSONObject[0]);
        for(JSONObject orderstate : torderstates)
        {
          texts.add(orderstate.getString("stateType") + ": " + orderstate.getString("datemodified"));
        }
        
        response.getOutput().put("text", texts);
      }
      else
      {
        texts.add("The order number you specified doesn't exist.");
      }
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  public static void orderStatsQueryProcess(MessageResponse response)
  {
    System.out.println("##################### orderStatsQueryProcess #####################");
    // retrieve Order Period
    String timePeriod = (String)response.getContext().get("number");
    String timeUnit = (String)response.getContext().get("timeUnit");
    
    if(timePeriod == null || timePeriod.length() == 0)
    {
      timePeriod = "365";
    }
    
    if(timeUnit == null || timeUnit.length() == 0)
    {
      timeUnit = "days";
    }
    
    System.out.println("##################### number: " + timePeriod);
    System.out.println("##################### timeUnit: " + timeUnit);
    
    String bodyJson = "{\"timePeriod\":" + "\"" + timePeriod + "\"" + "," + "\"timeUnit\":" + "\"" + timeUnit + "\"" + "}";
    
    try
    {
      HttpEntity entity = Utility.invokePostRequest(URL_ORDER_STATS_QUERY, bodyJson);
      String entityJsonString = Utility.convertHttpEntityToString(entity);
      System.out.println("##################### entityJsonString: " + entityJsonString);
      
      List<String> texts = new ArrayList<String>();
      
      JSONArray counterArray = JSONObject.parseArray(entityJsonString);
      JSONObject[] counters = counterArray.toArray(new JSONObject[0]);
      
      for(JSONObject counter : counters)
      {
        texts.add(counter.getString("LEVEL") + ": " + counter.getDouble("COUNT").intValue());
      }
      
      response.getOutput().put("text", texts);
    }
    catch (Exception e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
