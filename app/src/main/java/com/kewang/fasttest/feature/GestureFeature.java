package com.kewang.fasttest.feature;


import com.kewang.fasttest.model.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AD on 2016/9/1.
 */
public class GestureFeature {

    private static GestureFeature instance = null;
    private static List<Point> mGestureDatas = new ArrayList<Point>();

    private GestureFeature(){}

    public static GestureFeature getInstance(){
        if(instance == null){
            synchronized (GestureFeature.class){
                if(instance == null){
                    instance = new GestureFeature();
                }
            }
        }
        return instance;
    }

    public static void addGestureDatas(List<Point> datas){
        mGestureDatas.clear();
        mGestureDatas.addAll(datas);
    }


}
