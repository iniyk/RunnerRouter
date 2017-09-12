package com.iniyk.runnerrouter;

import android.content.Context;
import android.util.Log;

import com.amap.api.maps.*;
import com.amap.api.maps.AMap.*;
import com.amap.api.maps.model.*;
import com.amap.api.services.core.LatLonPoint;
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
    private ArrayList<String> routerName;
    private Polyline routerView = null;

    public RouterHelper() {
        routerPoint = new ArrayList<>();
        routerName = new ArrayList<>();
        routerView = null;
    }

    public ArrayList<LatLng> GetRouterPoints() {
        return routerPoint;
    }

    public ArrayList<String> GetRouterNames() {
        return routerName;
    }

    public int SetNextPoint(LatLng point) {
        return this.SetNextPoint(point, null, null, null);
    }

    public int SetNextPoint(LatLng point,
                            String strategy,
                            final CallBack callBack,
                            Context context) {
        switch (strategy) {
//            case "line":
//                routerPoint.add(point);
//                routerName.add(String.format("路径点 %d", routerPoint.size()));
//                break;
            case "onRoad":
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
                                    routerName.add(roadName + String.format(" %d", cnt));
                                }
                            }
                        }
                        callBack.CallBackFunc();
                    }

                    @Override
                    public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {

                    }
                });
                break;
            default:
                routerPoint.add(point);
                routerName.add(String.format("路径点 %d", routerPoint.size()));
                return 0;
        }
        return 0;
    }
}
