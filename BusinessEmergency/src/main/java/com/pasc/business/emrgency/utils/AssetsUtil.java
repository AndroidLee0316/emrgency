package com.pasc.business.emrgency.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by zhangxu678 on 2018/11/26.
 */
public class AssetsUtil {
  /**
   * @param jsonAssetsPath 资源文件对应的路径
   * @return 解析资源文件为String类型
   */
  public static String parseFromAssets(Context context, String jsonAssetsPath) {
    //将json数据变成字符串
    StringBuilder stringBuilder = new StringBuilder();
    try {
      //获取assets资源管理器
      AssetManager assetManager = context.getAssets();
      //通过管理器打开文件并读取
      BufferedReader bf =
          new BufferedReader(new InputStreamReader(assetManager.open(jsonAssetsPath)));
      String line;
      while ((line = bf.readLine()) != null) {
        stringBuilder.append(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return stringBuilder.toString();
  }
}
