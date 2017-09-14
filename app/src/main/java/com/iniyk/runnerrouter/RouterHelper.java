package com.iniyk.runnerrouter;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.*;
import com.amap.api.maps.AMap.*;
import com.amap.api.maps.model.*;
import com.amap.api.services.core.*;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by iniyk on 2017/9/4.
 */

public class RouterHelper {
    private ArrayList<LatLng> routerPoint;
    private ArrayList<Integer> routerPointStep;
    private ArrayList<String> routerName;
    private int pointStep = 0;


    public RouterHelper() {
        routerPoint = new ArrayList<>();
        routerName = new ArrayList<>();
        routerPointStep = new ArrayList<>();
        pointStep = 0;
    }

    public ArrayList<LatLng> GetRouterPoints() {
        return routerPoint;
    }

    public ArrayList<String> GetRouterNames() {
        return routerName;
    }

    public void Clear() {
        routerPoint.clear();
        routerName.clear();
        routerPointStep.clear();
        pointStep = 0;
    }

    public void ReverseLastPoint() {
        if (routerPointStep.size() <= 0) return ;
        Log.i("RouterHelper", String.format("Router Point Size : %d", routerPointStep.size()));
        while (routerPointStep.get(routerPointStep.size() - 1) == pointStep) {
            routerPointStep.remove(routerPointStep.size() - 1);
            routerName.remove(routerName.size() - 1);
            routerPoint.remove(routerPoint.size() - 1);
            if (routerPointStep.size() <= 0) {
                --pointStep;
                return ;
            }
        }
        --pointStep;
    }

    public int SetNextPoint(LatLng point,
                            String strategy,
                            final CallBack callBack,
                            Context context) throws com.amap.api.services.core.AMapException {
        switch (strategy) {
            case "onRoad":
                if (pointStep == 0) {
                    SetNextPointStraight(point);
                    callBack.CallBackFunc();
                    return 0;
                }
                RouteSearch routeSearch = new RouteSearch(context);
                routeSearch.setRouteSearchListener(new RouteSearch.OnRouteSearchListener() {
                    @Override
                    public void onBusRouteSearched(BusRouteResult busRouteResult, int i) {

                    }

                    @Override
                    public void onDriveRouteSearched(DriveRouteResult driveRouteResult, int i) {

                    }

                    @Override
                    public void onWalkRouteSearched(WalkRouteResult walkRouteResult, int i) {
                        if (i != 1000) {
                            Log.e("RouterHelper", String.format("On walk router searched returned error %d.", i));
                            return ;
                        }
                        pointStep++;
                        List<WalkPath> paths = walkRouteResult.getPaths();
                        for (WalkPath path : paths) {
                            List<WalkStep> steps = path.getSteps();
                            for (WalkStep step : steps) {
                                List<LatLonPoint> points = step.getPolyline();
                                String roadName = step.getRoad();
                                int cnt = 0;
                                for (LatLonPoint latLonPoint : points) {
                                    cnt++;
                                    routerPoint.add(new LatLng(
                                            latLonPoint.getLatitude(),
                                            latLonPoint.getLongitude()
                                    ));
                                    routerName.add(roadName);
                                    routerPointStep.add(pointStep);
                                }
                            }
                        }
                        callBack.CallBackFunc();
                    }

                    @Override
                    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

                    }
                });

                LatLonPoint lastPoint = new LatLonPoint(
                        GetLastPoint().latitude,
                        GetLastPoint().longitude
                );
                RouteSearch.WalkRouteQuery walkRouteQuery = new RouteSearch.WalkRouteQuery(
                        new RouteSearch.FromAndTo(
                                lastPoint,
                                new LatLonPoint(point.latitude, point.longitude)
                        )
                );

                routeSearch.calculateWalkRouteAsyn(walkRouteQuery);

                break;

            // Default strategy is "line"
            default:
                SetNextPointStraight(point);
                callBack.CallBackFunc();
                return 0;
        }
        return 0;
    }

    public int size() {
        return pointStep;
    }

    public int GetRouterStepSize() {
        return routerPoint.size();
    }

    public LatLng GetRouterPoint(int index) {
        return routerPoint.get(index);
    }

    public String GetRouterPointName(int index) {
        return routerName.get(index);
    }

    public String GetLastName() {
        return routerName.get(routerName.size() - 1);
    }

    public LatLng GetLastPoint() {
        return routerPoint.get(routerPoint.size() - 1);
    }

    public ArrayList<LatLng> GetSteps() {
        return routerPoint;
    }

    private void SetNextPointStraight(LatLng point) {
        pointStep++;
        routerPoint.add(point);
        routerName.add(String.format("路径点 %d", pointStep));
        routerPointStep.add(pointStep);
    }
}
