package com.rusher.lightly;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GIBSService {


    @GET("best/wmts.cgi")
    Call<ResponseBody> getImageData(
            @Query("Service") String service,
            @Query("Request") String request,
            @Query("Version") String version,
            @Query("layer") String layer,
            @Query("tilematrixset") String tileMatrixSet,
            @Query("TileMatrix") String tileMatrix,
            @Query("TileCol") String tileCol,
            @Query("TileRow") String tileRow,
            @Query("TIME") String time,
            @Query("style") String style,
            @Query("Format") String format
    );
}
